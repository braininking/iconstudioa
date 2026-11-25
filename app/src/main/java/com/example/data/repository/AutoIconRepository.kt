package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.os.Build
import com.example.data.local.AutoIconDatabase
import com.example.data.local.AutoIconPreferences
import com.example.data.local.AutomationSettings
import com.example.data.local.DetectionEventEntity
import com.example.data.local.IconThemeEntity
import com.example.data.local.ThemedAppEntity
import com.example.data.model.DetectionEventType
import com.example.data.model.NovaLauncherStatus
import com.example.data.model.PreviewGridSize
import com.example.data.model.RenderMode
import com.example.data.model.StrokeStyle
import com.example.engine.BlackMinimalistIconProcessor
import com.example.engine.MinimalIconLibrary
import com.example.engine.NovaLauncherBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class AutoIconRepository(private val context: Context) {

    private val database = AutoIconDatabase.getInstance(context)
    private val themeDao = database.iconThemeDao()
    private val appDao = database.themedAppDao()
    private val eventDao = database.detectionEventDao()
    private val preferences = AutoIconPreferences(context)
    private val processor = BlackMinimalistIconProcessor(context)
    private val novaBridge = NovaLauncherBridge(context)
    private val syncMutex = Mutex()

    val themesFlow: Flow<List<IconThemeEntity>> = themeDao.observeAllThemes()
    val activeThemeFlow: Flow<IconThemeEntity?> = themeDao.observeActiveTheme()
    val appsFlow: Flow<List<ThemedAppEntity>> = appDao.observeAllApps()
    val recentEventsFlow: Flow<List<DetectionEventEntity>> = eventDao.observeRecentEvents()
    val settingsFlow: Flow<AutomationSettings> = preferences.settingsFlow

    suspend fun ensureDefaultThemesSeeded() = withContext(Dispatchers.IO) {
        if (themeDao.countThemes() == 0) {
            themeDao.insertThemes(buildPresetThemes())
        }
    }

    suspend fun getOrCreateActiveTheme(): IconThemeEntity {
        ensureDefaultThemesSeeded()
        return themeDao.getActiveTheme() ?: buildPresetThemes().first().also {
            themeDao.insertTheme(it)
        }
    }

    suspend fun syncInstalledApps(forceReprocessAll: Boolean = false): Int = syncMutex.withLock {
        withContext(Dispatchers.IO) {
            val activeTheme = getOrCreateActiveTheme()
            val pm = context.packageManager
            val launcherApps = queryLauncherApps(pm)
            val existingMap = appDao.getAllApps().associateBy { it.packageName }
            val activePackages = mutableListOf<String>()
            val upserts = mutableListOf<ThemedAppEntity>()
            var newlyProcessedCount = 0

            val isInitialCatalog = existingMap.isEmpty()

            for (resolved in launcherApps) {
                val pkg = resolved.activityInfo.packageName
                if (pkg == context.packageName) continue // Keep self optional or skip self loop
                activePackages.add(pkg)

                val component = "$pkg/${resolved.activityInfo.name}"
                val appLabel = resolved.loadLabel(pm).toString().ifBlank { pkg }
                val pkgInfo = getPackageInfoCompat(pm, pkg)
                val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pkgInfo?.longVersionCode ?: 0L
                } else {
                    @Suppress("DEPRECATION")
                    (pkgInfo?.versionCode ?: 0).toLong()
                }
                val lastUpdate = pkgInfo?.lastUpdateTime ?: 0L
                val existing = existingMap[pkg]
                val cachedExists = existing?.cachedIconPath?.let { File(it).exists() } == true

                val needsProcess = forceReprocessAll ||
                    existing == null ||
                    !cachedExists ||
                    existing.versionCode != versionCode ||
                    existing.lastUpdateTime != lastUpdate

                if (needsProcess) {
                    val startMs = System.currentTimeMillis()
                    val drawable = runCatching { resolved.loadIcon(pm) }.getOrNull()
                    val result = processor.processAndSaveAppIcon(
                        packageName = pkg,
                        appName = appLabel,
                        originalDrawable = drawable,
                        theme = activeTheme,
                        manualSymbolId = existing?.matchedLibraryIconId?.takeIf { existing.processingStatus == com.example.data.model.ProcessingStatus.MANUAL_OVERRIDE.name },
                        customRenderMode = existing?.resolvedCustomRenderMode(),
                        customScaleOverride = existing?.customScaleOverride,
                        customStrokeOverride = existing?.customStrokeOverride
                    )
                    val elapsed = (System.currentTimeMillis() - startMs).coerceAtLeast(1L)
                    val isNew = existing == null && !isInitialCatalog

                    val entity = ThemedAppEntity(
                        packageName = pkg,
                        componentName = component,
                        appName = appLabel,
                        category = MinimalIconLibrary.inferCategory(pkg, appLabel).name,
                        versionCode = versionCode,
                        lastUpdateTime = lastUpdate,
                        processingStatus = result.status.name,
                        matchedLibraryIconId = result.matchedSymbolId,
                        customRenderMode = existing?.customRenderMode,
                        customScaleOverride = existing?.customScaleOverride,
                        customStrokeOverride = existing?.customStrokeOverride,
                        cachedIconPath = result.filePath,
                        isNewlyDetected = isNew || (existing?.isNewlyDetected == true),
                        detectedAt = existing?.detectedAt ?: System.currentTimeMillis(),
                        processedAt = System.currentTimeMillis()
                    )
                    upserts.add(entity)
                    newlyProcessedCount++

                    if (!isInitialCatalog && existing == null) {
                        eventDao.insertEvent(
                            DetectionEventEntity(
                                packageName = pkg,
                                appName = appLabel,
                                eventType = DetectionEventType.ADDED.name,
                                resultStatus = result.status.labelPt,
                                processingTimeMs = elapsed,
                                novaSynced = true
                            )
                        )
                    } else if (existing != null && existing.versionCode != versionCode) {
                        eventDao.insertEvent(
                            DetectionEventEntity(
                                packageName = pkg,
                                appName = appLabel,
                                eventType = DetectionEventType.REPLACED.name,
                                resultStatus = result.status.labelPt,
                                processingTimeMs = elapsed,
                                novaSynced = true
                            )
                        )
                    }
                }
            }

            if (upserts.isNotEmpty()) {
                appDao.upsertApps(upserts)
            }

            // Remove uninstalled packages from cache and DB
            val removed = existingMap.keys.filter { it !in activePackages }
            for (pkg in removed) {
                val removedApp = existingMap[pkg]
                removedApp?.cachedIconPath?.let { File(it).delete() }
                appDao.deleteByPackageName(pkg)
                eventDao.insertEvent(
                    DetectionEventEntity(
                        packageName = pkg,
                        appName = removedApp?.appName ?: pkg,
                        eventType = DetectionEventType.REMOVED.name,
                        resultStatus = "Removido do catálogo",
                        novaSynced = true
                    )
                )
            }

            val allApps = appDao.getAllApps()
            novaBridge.syncDynamicNovaExport(activeTheme, allApps)
            preferences.updateLastSyncTimestamp(System.currentTimeMillis())

            if (isInitialCatalog && allApps.isNotEmpty()) {
                eventDao.insertEvent(
                    DetectionEventEntity(
                        packageName = context.packageName,
                        appName = "${allApps.size} aplicativos",
                        eventType = DetectionEventType.BATCH_SYNC.name,
                        resultStatus = "Catálogo inicial Black Minimalist",
                        novaSynced = true
                    )
                )
            }
            newlyProcessedCount
        }
    }

    suspend fun handlePackageChanged(
        packageName: String,
        eventType: DetectionEventType
    ) = syncMutex.withLock {
        withContext(Dispatchers.IO) {
            val settings = preferences.settingsFlow.first()
            if (!settings.autoDetectionEnabled) return@withContext

            if (eventType == DetectionEventType.REMOVED) {
                val existing = appDao.getApp(packageName)
                existing?.cachedIconPath?.let { File(it).delete() }
                appDao.deleteByPackageName(packageName)
                eventDao.insertEvent(
                    DetectionEventEntity(
                        packageName = packageName,
                        appName = existing?.appName ?: packageName,
                        eventType = DetectionEventType.REMOVED.name,
                        resultStatus = "Cache limpo",
                        novaSynced = settings.novaAutoSync
                    )
                )
                val activeTheme = getOrCreateActiveTheme()
                novaBridge.syncDynamicNovaExport(activeTheme, appDao.getAllApps())
                preferences.updateLastSyncTimestamp()
                return@withContext
            }

            if (!settings.autoProcessOnInstall) return@withContext

            val pm = context.packageManager
            val resolveIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                setPackage(packageName)
            }
            val resolved = pm.queryIntentActivities(resolveIntent, 0).firstOrNull() ?: return@withContext
            val activeTheme = getOrCreateActiveTheme()
            val startMs = System.currentTimeMillis()
            val appLabel = resolved.loadLabel(pm).toString().ifBlank { packageName }
            val component = "$packageName/${resolved.activityInfo.name}"
            val pkgInfo = getPackageInfoCompat(pm, packageName)
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pkgInfo?.longVersionCode ?: 0L
            } else {
                @Suppress("DEPRECATION")
                (pkgInfo?.versionCode ?: 0).toLong()
            }
            val existing = appDao.getApp(packageName)
            val drawable = runCatching { resolved.loadIcon(pm) }.getOrNull()
            val result = processor.processAndSaveAppIcon(
                packageName = packageName,
                appName = appLabel,
                originalDrawable = drawable,
                theme = activeTheme,
                manualSymbolId = existing?.matchedLibraryIconId?.takeIf { existing.processingStatus == com.example.data.model.ProcessingStatus.MANUAL_OVERRIDE.name },
                customRenderMode = existing?.resolvedCustomRenderMode(),
                customScaleOverride = existing?.customScaleOverride,
                customStrokeOverride = existing?.customStrokeOverride
            )
            val elapsed = (System.currentTimeMillis() - startMs).coerceAtLeast(1L)

            appDao.upsertApp(
                ThemedAppEntity(
                    packageName = packageName,
                    componentName = component,
                    appName = appLabel,
                    category = MinimalIconLibrary.inferCategory(packageName, appLabel).name,
                    versionCode = versionCode,
                    lastUpdateTime = pkgInfo?.lastUpdateTime ?: System.currentTimeMillis(),
                    processingStatus = result.status.name,
                    matchedLibraryIconId = result.matchedSymbolId,
                    customRenderMode = existing?.customRenderMode,
                    customScaleOverride = existing?.customScaleOverride,
                    customStrokeOverride = existing?.customStrokeOverride,
                    cachedIconPath = result.filePath,
                    isNewlyDetected = true,
                    detectedAt = System.currentTimeMillis(),
                    processedAt = System.currentTimeMillis()
                )
            )

            val allApps = appDao.getAllApps()
            if (settings.novaAutoSync) {
                novaBridge.syncDynamicNovaExport(activeTheme, allApps)
            }

            eventDao.insertEvent(
                DetectionEventEntity(
                    packageName = packageName,
                    appName = appLabel,
                    eventType = eventType.name,
                    resultStatus = result.status.labelPt,
                    processingTimeMs = elapsed,
                    novaSynced = settings.novaAutoSync
                )
            )
            preferences.updateLastSyncTimestamp()
        }
    }

    suspend fun saveAndActivateTheme(theme: IconThemeEntity, reprocessApps: Boolean = true) {
        withContext(Dispatchers.IO) {
            val updated = theme.copy(isActive = true, updatedAt = System.currentTimeMillis())
            themeDao.insertTheme(updated)
            themeDao.setActiveTheme(updated.id)
        }
        if (reprocessApps) {
            syncInstalledApps(forceReprocessAll = true)
        }
    }

    suspend fun activateThemeById(themeId: String) {
        withContext(Dispatchers.IO) {
            themeDao.setActiveTheme(themeId)
        }
        syncInstalledApps(forceReprocessAll = true)
    }

    suspend fun createTheme(name: String, description: String, baseTheme: IconThemeEntity? = null): IconThemeEntity {
        val template = baseTheme ?: getOrCreateActiveTheme()
        val created = template.copy(
            id = "custom_${UUID.randomUUID().toString().take(8)}",
            name = name.trim(),
            description = description.trim().ifBlank { "Tema personalizado Black Minimalist" },
            isPreset = false,
            isActive = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        withContext(Dispatchers.IO) {
            themeDao.insertTheme(created)
        }
        return created
    }

    suspend fun duplicateTheme(source: IconThemeEntity): IconThemeEntity {
        val copy = source.copy(
            id = "custom_${UUID.randomUUID().toString().take(8)}",
            name = "${source.name} (Cópia)",
            isPreset = false,
            isActive = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        withContext(Dispatchers.IO) {
            themeDao.insertTheme(copy)
        }
        return copy
    }

    suspend fun deleteTheme(themeId: String) {
        withContext(Dispatchers.IO) {
            val target = themeDao.getThemeById(themeId) ?: return@withContext
            if (target.isPreset) return@withContext
            val wasActive = target.isActive
            themeDao.deleteCustomTheme(themeId)
            if (wasActive) {
                themeDao.setActiveTheme("preset_black_minimalist")
            }
        }
    }

    suspend fun applyManualAppCorrection(
        packageName: String,
        manualSymbolId: String?,
        customRenderMode: RenderMode?,
        customScaleOverride: Float?,
        customStrokeOverride: Float?
    ) = withContext(Dispatchers.IO) {
        val existing = appDao.getApp(packageName) ?: return@withContext
        val activeTheme = getOrCreateActiveTheme()
        val drawable = loadAppIconDrawable(packageName)
        val result = processor.processAndSaveAppIcon(
            packageName = packageName,
            appName = existing.appName,
            originalDrawable = drawable,
            theme = activeTheme,
            manualSymbolId = manualSymbolId,
            customRenderMode = customRenderMode,
            customScaleOverride = customScaleOverride,
            customStrokeOverride = customStrokeOverride
        )
        appDao.upsertApp(
            existing.copy(
                matchedLibraryIconId = result.matchedSymbolId,
                customRenderMode = customRenderMode?.name,
                customScaleOverride = customScaleOverride,
                customStrokeOverride = customStrokeOverride,
                processingStatus = result.status.name,
                cachedIconPath = result.filePath,
                isNewlyDetected = false,
                processedAt = System.currentTimeMillis()
            )
        )
        novaBridge.syncDynamicNovaExport(activeTheme, appDao.getAllApps())
    }

    fun loadAppIconDrawable(packageName: String): Drawable? = runCatching {
        context.packageManager.getApplicationIcon(packageName)
    }.getOrNull()

    fun getNovaStatus(apps: List<ThemedAppEntity>): NovaLauncherStatus =
        novaBridge.inspectNovaStatus(apps)

    fun readAppFilterPreview(): String = novaBridge.readGeneratedAppFilterPreview()

    fun applyThemeInNova(): Result<String> = novaBridge.applyIconPackToNovaLauncher()

    fun pinAppShortcut(app: ThemedAppEntity): Result<String> = novaBridge.pinThemedShortcutToHome(app)

    suspend fun buildNovaExportZipUri() = withContext(Dispatchers.IO) {
        val theme = getOrCreateActiveTheme()
        val apps = appDao.getAllApps()
        novaBridge.createExportZipUri(theme, apps)
    }

    suspend fun setAutoDetectionEnabled(enabled: Boolean) = preferences.setAutoDetectionEnabled(enabled)
    suspend fun setAutoProcessOnInstall(enabled: Boolean) = preferences.setAutoProcessOnInstall(enabled)
    suspend fun setNovaAutoSync(enabled: Boolean) = preferences.setNovaAutoSync(enabled)
    suspend fun setPreviewGridSize(size: PreviewGridSize) = preferences.setPreviewGridSize(size)
    suspend fun clearDetectionLogs() = withContext(Dispatchers.IO) { eventDao.clearEvents() }

    private fun queryLauncherApps(pm: PackageManager): List<ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, 0)
        }.distinctBy { it.activityInfo.packageName }
    }

    @Suppress("DEPRECATION")
    private fun getPackageInfoCompat(pm: PackageManager, pkg: String) = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0))
        } else {
            pm.getPackageInfo(pkg, 0)
        }
    } catch (_: PackageManager.NameNotFoundException) {
        null
    }

    private fun buildPresetThemes(): List<IconThemeEntity> = listOf(
        IconThemeEntity(
            id = "preset_black_minimalist",
            name = "Black Minimalist",
            description = "Identidade principal: fundo preto #000000 com traços brancos #FFFFFF em line art uniforme.",
            isPreset = true,
            isActive = true,
            cornerRadiusPercent = 26f,
            strokeWidthDp = 1.9f,
            symbolScale = 0.64f,
            whiteIntensity = 1.0f,
            backgroundHex = "#000000",
            borderWidthDp = 0f,
            strokeStyle = StrokeStyle.ROUND.name,
            renderMode = RenderMode.LINE_ART.name
        ),
        IconThemeEntity(
            id = "preset_amoled_pure",
            name = "AMOLED Pure Line",
            description = "Traços refinados e ultra limpos sobre preto absoluto para telas OLED.",
            isPreset = true,
            cornerRadiusPercent = 24f,
            strokeWidthDp = 1.5f,
            symbolScale = 0.60f,
            whiteIntensity = 0.96f,
            backgroundHex = "#000000",
            borderWidthDp = 0f,
            strokeStyle = StrokeStyle.THIN.name,
            renderMode = RenderMode.LINE_ART.name
        ),
        IconThemeEntity(
            id = "preset_mono_silhouette",
            name = "Mono Silhouette",
            description = "Símbolos em silhueta branca sólida com máximo destaque e legibilidade.",
            isPreset = true,
            cornerRadiusPercent = 26f,
            strokeWidthDp = 2.1f,
            symbolScale = 0.64f,
            whiteIntensity = 1.0f,
            backgroundHex = "#000000",
            borderWidthDp = 0f,
            strokeStyle = StrokeStyle.ROUND.name,
            renderMode = RenderMode.SILHOUETTE.name
        ),
        IconThemeEntity(
            id = "preset_stealth_thin",
            name = "Stealth Frame",
            description = "Preto profundo com filete de borda sutil e traço minimalista discreto.",
            isPreset = true,
            cornerRadiusPercent = 28f,
            strokeWidthDp = 1.4f,
            symbolScale = 0.58f,
            whiteIntensity = 0.88f,
            backgroundHex = "#050507",
            borderWidthDp = 0.7f,
            borderAlpha = 0.20f,
            strokeStyle = StrokeStyle.THIN.name,
            renderMode = RenderMode.LINE_ART.name
        ),
        IconThemeEntity(
            id = "preset_geometric_crisp",
            name = "Geometric Crisp",
            description = "Cantos precisos e junções geométricas inspiradas no design suíço.",
            isPreset = true,
            cornerRadiusPercent = 16f,
            strokeWidthDp = 2.0f,
            symbolScale = 0.64f,
            whiteIntensity = 1.0f,
            backgroundHex = "#000000",
            borderWidthDp = 0f,
            strokeStyle = StrokeStyle.GEOMETRIC.name,
            renderMode = RenderMode.LINE_ART.name
        ),
        IconThemeEntity(
            id = "preset_circular_noir",
            name = "Circular Noir",
            description = "Moldura circular pura com símbolos line art perfeitamente centralizados.",
            isPreset = true,
            cornerRadiusPercent = 50f,
            strokeWidthDp = 1.9f,
            symbolScale = 0.58f,
            whiteIntensity = 1.0f,
            backgroundHex = "#000000",
            borderWidthDp = 0.6f,
            borderAlpha = 0.14f,
            strokeStyle = StrokeStyle.ROUND.name,
            renderMode = RenderMode.LINE_ART.name
        ),
        IconThemeEntity(
            id = "preset_square_sharp",
            name = "Square Sharp",
            description = "Formato quadrado arquitetônico com leve suavização nos vértices.",
            isPreset = true,
            cornerRadiusPercent = 8f,
            strokeWidthDp = 2.0f,
            symbolScale = 0.66f,
            whiteIntensity = 1.0f,
            backgroundHex = "#000000",
            borderWidthDp = 0f,
            strokeStyle = StrokeStyle.GEOMETRIC.name,
            renderMode = RenderMode.LINE_ART.name
        ),
        IconThemeEntity(
            id = "preset_high_contrast",
            name = "High-Contrast Double",
            description = "Contorno duplo de alta presença visual para fácil leitura em grades compactas.",
            isPreset = true,
            cornerRadiusPercent = 26f,
            strokeWidthDp = 2.6f,
            symbolScale = 0.68f,
            whiteIntensity = 1.0f,
            backgroundHex = "#000000",
            borderWidthDp = 0.9f,
            borderAlpha = 0.24f,
            strokeStyle = StrokeStyle.DOUBLE.name,
            renderMode = RenderMode.LINE_ART.name
        )
    )
}
