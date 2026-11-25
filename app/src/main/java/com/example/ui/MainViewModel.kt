package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.AutoIconApplication
import com.example.data.local.AutomationSettings
import com.example.data.local.DetectionEventEntity
import com.example.data.local.IconThemeEntity
import com.example.data.local.ThemedAppEntity
import com.example.data.model.MinimalCategory
import com.example.data.model.NovaLauncherStatus
import com.example.data.model.PreviewGridSize
import com.example.data.model.ProcessingStatus
import com.example.data.model.RenderMode
import com.example.engine.BlackMinimalistIconProcessor
import com.example.engine.MinimalIconLibrary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AppsFilterMode(val labelPt: String) {
    ALL("Todos"),
    LIBRARY("Biblioteca"),
    AUTO_LINE_ART("Line Art Auto"),
    AUTO_SILHOUETTE("Silhueta Auto"),
    NEWLY_DETECTED("Novos")
}

data class MainUiState(
    val activeTheme: IconThemeEntity? = null,
    val draftTheme: IconThemeEntity? = null,
    val themes: List<IconThemeEntity> = emptyList(),
    val apps: List<ThemedAppEntity> = emptyList(),
    val filteredApps: List<ThemedAppEntity> = emptyList(),
    val recentEvents: List<DetectionEventEntity> = emptyList(),
    val settings: AutomationSettings = AutomationSettings(),
    val novaStatus: NovaLauncherStatus = NovaLauncherStatus(
        isInstalled = false,
        isPrimeInstalled = false,
        versionName = null,
        supportsAutoTheming = true,
        mappedLibraryCount = 0,
        totalExportedCount = 0
    ),
    val appFilterPreviewXml: String = "",
    val searchQuery: String = "",
    val appsFilterMode: AppsFilterMode = AppsFilterMode.ALL,
    val selectedCategoryFilter: MinimalCategory? = null,
    val isSyncing: Boolean = false,
    val isReprocessing: Boolean = false
)

sealed interface UiFeedbackEvent {
    data class Message(val text: String) : UiFeedbackEvent
    data class ShareZipUri(val uri: Uri) : UiFeedbackEvent
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as AutoIconApplication
    private val repository = app.repository
    private val monitorManager = app.monitorManager
    private val processor = BlackMinimalistIconProcessor(application)

    private val _draftTheme = MutableStateFlow<IconThemeEntity?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _appsFilterMode = MutableStateFlow(AppsFilterMode.ALL)
    private val _selectedCategoryFilter = MutableStateFlow<MinimalCategory?>(null)
    private val _isSyncing = MutableStateFlow(false)
    private val _isReprocessing = MutableStateFlow(false)
    private val _appFilterPreviewXml = MutableStateFlow(repository.readAppFilterPreview())

    private val _events = MutableSharedFlow<UiFeedbackEvent>()
    val events: SharedFlow<UiFeedbackEvent> = _events.asSharedFlow()

    private val _bitmapCacheVersion = MutableStateFlow(0)
    val bitmapCacheVersion: StateFlow<Int> = _bitmapCacheVersion.asStateFlow()

    val uiState: StateFlow<MainUiState> = combine(
        repository.themesFlow,
        repository.activeThemeFlow,
        repository.appsFlow,
        repository.recentEventsFlow,
        repository.settingsFlow,
        _draftTheme,
        _searchQuery,
        _appsFilterMode,
        _selectedCategoryFilter,
        _isSyncing,
        _isReprocessing,
        _appFilterPreviewXml
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val themes = values[0] as List<IconThemeEntity>
        val activeTheme = values[1] as IconThemeEntity?
        @Suppress("UNCHECKED_CAST")
        val apps = values[2] as List<ThemedAppEntity>
        @Suppress("UNCHECKED_CAST")
        val recentEvents = values[3] as List<DetectionEventEntity>
        val settings = values[4] as AutomationSettings
        val draft = (values[5] as IconThemeEntity?) ?: activeTheme
        val query = values[6] as String
        val filterMode = values[7] as AppsFilterMode
        val categoryFilter = values[8] as MinimalCategory?
        val isSyncing = values[9] as Boolean
        val isReprocessing = values[10] as Boolean
        val xmlPreview = values[11] as String

        val filtered = apps.filter { item ->
            val matchesQuery = query.isBlank() ||
                item.appName.contains(query, ignoreCase = true) ||
                item.packageName.contains(query, ignoreCase = true)
            val matchesMode = when (filterMode) {
                AppsFilterMode.ALL -> true
                AppsFilterMode.LIBRARY -> item.processingStatus == ProcessingStatus.PROCESSED_LIBRARY.name ||
                    item.processingStatus == ProcessingStatus.MANUAL_OVERRIDE.name
                AppsFilterMode.AUTO_LINE_ART -> item.processingStatus == ProcessingStatus.PROCESSED_AUTO_LINE_ART.name
                AppsFilterMode.AUTO_SILHOUETTE -> item.processingStatus == ProcessingStatus.PROCESSED_AUTO_SILHOUETTE.name
                AppsFilterMode.NEWLY_DETECTED -> item.isNewlyDetected
            }
            val matchesCategory = categoryFilter == null || item.category == categoryFilter.name
            matchesQuery && matchesMode && matchesCategory
        }

        MainUiState(
            activeTheme = activeTheme,
            draftTheme = draft,
            themes = themes,
            apps = apps,
            filteredApps = filtered,
            recentEvents = recentEvents,
            settings = settings,
            novaStatus = repository.getNovaStatus(apps),
            appFilterPreviewXml = xmlPreview,
            searchQuery = query,
            appsFilterMode = filterMode,
            selectedCategoryFilter = categoryFilter,
            isSyncing = isSyncing,
            isReprocessing = isReprocessing
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    init {
        refreshCatalog(forceReprocess = false)
    }

    fun refreshCatalog(forceReprocess: Boolean = false) {
        if (_isSyncing.value) return
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                val processed = repository.syncInstalledApps(forceReprocessAll = forceReprocess)
                _appFilterPreviewXml.value = repository.readAppFilterPreview()
                _bitmapCacheVersion.value++
                if (forceReprocess) {
                    _events.emit(UiFeedbackEvent.Message("$processed ícones atualizados com sucesso."))
                }
            } catch (e: Exception) {
                _events.emit(UiFeedbackEvent.Message("Falha ao sincronizar aplicativos: ${e.message}"))
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun updateDraftTheme(transform: (IconThemeEntity) -> IconThemeEntity) {
        val base = _draftTheme.value ?: uiState.value.activeTheme ?: return
        _draftTheme.value = transform(base)
    }

    fun resetDraftToActive() {
        _draftTheme.value = uiState.value.activeTheme
    }

    fun loadThemeIntoEditor(theme: IconThemeEntity) {
        _draftTheme.value = theme
    }

    fun saveDraftThemeAndApply() {
        val draft = _draftTheme.value ?: return
        viewModelScope.launch {
            _isReprocessing.value = true
            try {
                repository.saveAndActivateTheme(draft, reprocessApps = true)
                _draftTheme.value = null
                _appFilterPreviewXml.value = repository.readAppFilterPreview()
                _bitmapCacheVersion.value++
                _events.emit(UiFeedbackEvent.Message("Estilo '${draft.name}' salvo e aplicado a todos os ícones."))
            } catch (e: Exception) {
                _events.emit(UiFeedbackEvent.Message("Erro ao salvar estilo: ${e.message}"))
            } finally {
                _isReprocessing.value = false
            }
        }
    }

    fun activateTheme(theme: IconThemeEntity) {
        viewModelScope.launch {
            _isReprocessing.value = true
            try {
                repository.activateThemeById(theme.id)
                _draftTheme.value = null
                _appFilterPreviewXml.value = repository.readAppFilterPreview()
                _bitmapCacheVersion.value++
                _events.emit(UiFeedbackEvent.Message("Tema '${theme.name}' ativado."))
            } catch (e: Exception) {
                _events.emit(UiFeedbackEvent.Message("Erro ao ativar tema: ${e.message}"))
            } finally {
                _isReprocessing.value = false
            }
        }
    }

    fun createNewTheme(name: String, description: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            try {
                val created = repository.createTheme(name, description, uiState.value.draftTheme)
                repository.activateThemeById(created.id)
                _draftTheme.value = created.copy(isActive = true)
                _bitmapCacheVersion.value++
                _events.emit(UiFeedbackEvent.Message("Tema '${created.name}' criado e ativado."))
            } catch (e: Exception) {
                _events.emit(UiFeedbackEvent.Message("Erro ao criar tema: ${e.message}"))
            }
        }
    }

    fun duplicateTheme(theme: IconThemeEntity) {
        viewModelScope.launch {
            try {
                val copy = repository.duplicateTheme(theme)
                _events.emit(UiFeedbackEvent.Message("Tema '${copy.name}' duplicado."))
            } catch (e: Exception) {
                _events.emit(UiFeedbackEvent.Message("Erro ao duplicar tema: ${e.message}"))
            }
        }
    }

    fun deleteTheme(theme: IconThemeEntity) {
        if (theme.isPreset) return
        viewModelScope.launch {
            try {
                repository.deleteTheme(theme.id)
                _events.emit(UiFeedbackEvent.Message("Tema '${theme.name}' excluído."))
            } catch (e: Exception) {
                _events.emit(UiFeedbackEvent.Message("Erro ao excluir tema: ${e.message}"))
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateAppsFilter(mode: AppsFilterMode) {
        _appsFilterMode.value = mode
    }

    fun updateCategoryFilter(category: MinimalCategory?) {
        _selectedCategoryFilter.value = category
    }

    fun applyManualCorrection(
        packageName: String,
        manualSymbolId: String?,
        customRenderMode: RenderMode?,
        customScaleOverride: Float?,
        customStrokeOverride: Float?
    ) {
        viewModelScope.launch {
            try {
                repository.applyManualAppCorrection(
                    packageName = packageName,
                    manualSymbolId = manualSymbolId,
                    customRenderMode = customRenderMode,
                    customScaleOverride = customScaleOverride,
                    customStrokeOverride = customStrokeOverride
                )
                _appFilterPreviewXml.value = repository.readAppFilterPreview()
                _bitmapCacheVersion.value++
                _events.emit(UiFeedbackEvent.Message("Ícone ajustado manualmente e salvo em cache."))
            } catch (e: Exception) {
                _events.emit(UiFeedbackEvent.Message("Erro no ajuste manual: ${e.message}"))
            }
        }
    }

    fun applyToNovaLauncher() {
        viewModelScope.launch {
            val result = repository.applyThemeInNova()
            result.onSuccess { msg ->
                _events.emit(UiFeedbackEvent.Message(msg))
            }.onFailure { err ->
                _events.emit(UiFeedbackEvent.Message(err.message ?: "Não foi possível abrir o Nova Launcher."))
            }
        }
    }

    fun exportNovaBundleZip() {
        viewModelScope.launch {
            try {
                val uri = repository.buildNovaExportZipUri()
                _events.emit(UiFeedbackEvent.ShareZipUri(uri))
            } catch (e: Exception) {
                _events.emit(UiFeedbackEvent.Message("Erro ao exportar pacote: ${e.message}"))
            }
        }
    }

    fun pinShortcutToHome(appEntity: ThemedAppEntity) {
        viewModelScope.launch {
            val result = repository.pinAppShortcut(appEntity)
            result.onSuccess { msg ->
                _events.emit(UiFeedbackEvent.Message(msg))
            }.onFailure { err ->
                _events.emit(UiFeedbackEvent.Message(err.message ?: "Não foi possível fixar atalho."))
            }
        }
    }

    fun setAutoDetectionEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setAutoDetectionEnabled(enabled)
            if (enabled) {
                monitorManager.startMonitoring()
                _events.emit(UiFeedbackEvent.Message("Detecção automática ativada em segundo plano."))
            } else {
                monitorManager.stopMonitoring()
                _events.emit(UiFeedbackEvent.Message("Detecção automática pausada."))
            }
        }
    }

    fun setAutoProcessOnInstall(enabled: Boolean) {
        viewModelScope.launch { repository.setAutoProcessOnInstall(enabled) }
    }

    fun setNovaAutoSync(enabled: Boolean) {
        viewModelScope.launch { repository.setNovaAutoSync(enabled) }
    }

    fun setPreviewGridSize(size: PreviewGridSize) {
        viewModelScope.launch { repository.setPreviewGridSize(size) }
    }

    fun clearDetectionLogs() {
        viewModelScope.launch {
            repository.clearDetectionLogs()
            _events.emit(UiFeedbackEvent.Message("Histórico de detecção limpo."))
        }
    }

    suspend fun renderLiveLibraryPreview(
        symbolId: String,
        theme: IconThemeEntity,
        sizePx: Int = 192
    ): Bitmap = withContext(Dispatchers.Default) {
        val spec = MinimalIconLibrary.getById(symbolId) ?: MinimalIconLibrary.allSymbols.first()
        processor.renderLibrarySymbolBitmap(spec, theme, sizePx)
    }

    suspend fun renderLiveAppPreview(
        appEntity: ThemedAppEntity,
        theme: IconThemeEntity,
        sizePx: Int = 192
    ): Bitmap = withContext(Dispatchers.Default) {
        val spec = MinimalIconLibrary.getById(appEntity.matchedLibraryIconId)
        if (spec != null) {
            processor.renderLibrarySymbolBitmap(spec, theme, sizePx)
        } else {
            val drawable = repository.loadAppIconDrawable(appEntity.packageName)
            if (drawable != null) {
                processor.renderUnknownAppFallbackBitmap(drawable, theme, sizePx).first
            } else {
                processor.renderLibrarySymbolBitmap(MinimalIconLibrary.allSymbols.first(), theme, sizePx)
            }
        }
    }

    suspend fun loadCachedIconBitmap(path: String?): Bitmap? = withContext(Dispatchers.IO) {
        if (path.isNullOrBlank()) return@withContext null
        runCatching { BitmapFactory.decodeFile(path) }.getOrNull()
    }

    suspend fun loadOriginalIconBitmap(packageName: String): Bitmap? = withContext(Dispatchers.Default) {
        val drawable = repository.loadAppIconDrawable(packageName) ?: return@withContext null
        val size = 96
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bmp)
        drawable.setBounds(0, 0, size, size)
        drawable.draw(canvas)
        bmp
    }
}
