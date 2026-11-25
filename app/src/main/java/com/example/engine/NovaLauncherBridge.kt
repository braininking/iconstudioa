package com.example.engine

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.data.local.IconThemeEntity
import com.example.data.local.ThemedAppEntity
import com.example.data.model.NovaLauncherStatus
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.math.roundToInt

class NovaLauncherBridge(private val context: Context) {

    companion object {
        const val NOVA_PACKAGE = "com.teslacoilsw.launcher"
        const val NOVA_PRIME_PACKAGE = "com.teslacoilsw.launcher.prime"
        const val ACTION_APPLY_NOVA_THEME = "com.novalauncher.APPLY_ICON_THEME"
        const val EXTRA_ICON_THEME_PACKAGE = "com.novalauncher.extra.ICON_THEME_PACKAGE"
        const val EXTRA_ICON_THEME_TYPE = "com.novalauncher.extra.ICON_THEME_TYPE"
    }

    private val exportDir: File by lazy {
        File(context.filesDir, "nova_export").apply { mkdirs() }
    }

    fun inspectNovaStatus(apps: List<ThemedAppEntity>): NovaLauncherStatus {
        val pm = context.packageManager
        val novaInfo = getPackageInfoCompat(pm, NOVA_PACKAGE)
        val primeInstalled = getPackageInfoCompat(pm, NOVA_PRIME_PACKAGE) != null
        val mappedCount = apps.count { it.matchedLibraryIconId != null }
        val processedCount = apps.count { !it.cachedIconPath.isNullOrBlank() }

        return NovaLauncherStatus(
            isInstalled = novaInfo != null,
            isPrimeInstalled = primeInstalled,
            versionName = novaInfo?.versionName,
            supportsAutoTheming = true,
            mappedLibraryCount = mappedCount,
            totalExportedCount = processedCount
        )
    }

    /**
     * Generates up-to-date Nova Launcher XML theme descriptors (appfilter.xml, drawable.xml)
     * plus iconback.png, iconmask.png, and iconupon.png calibrated to the active Black Minimalist theme.
     */
    fun syncDynamicNovaExport(theme: IconThemeEntity, apps: List<ThemedAppEntity>): File {
        val iconsOutDir = File(exportDir, "drawable-nodpi").apply { mkdirs() }
        val xmlOutDir = File(exportDir, "xml").apply { mkdirs() }

        generateIconBackPng(File(iconsOutDir, "nova_iconback_black.png"), theme)
        generateIconMaskPng(File(iconsOutDir, "nova_iconmask_squircle.png"), theme)
        generateIconUponPng(File(iconsOutDir, "nova_iconupon_frame.png"), theme)

        val appfilterSb = StringBuilder()
        appfilterSb.appendLine("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
        appfilterSb.appendLine("<resources>")
        appfilterSb.appendLine("    <!-- AutoIcon Studio: Automatic App Icon Theming (${theme.name}) -->")
        appfilterSb.appendLine("    <iconback img1=\"nova_iconback_black\" />")
        appfilterSb.appendLine("    <iconmask img1=\"nova_iconmask_squircle\" />")
        appfilterSb.appendLine("    <iconupon img1=\"nova_iconupon_frame\" />")
        appfilterSb.appendLine("    <scale factor=\"${"%.2f".format(java.util.Locale.US, theme.symbolScale)}\" />")
        appfilterSb.appendLine()

        val drawableSb = StringBuilder()
        drawableSb.appendLine("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
        drawableSb.appendLine("<resources>")
        drawableSb.appendLine("    <version>1</version>")

        apps.forEach { app ->
            val safeName = "ic_auto_${app.packageName.lowercase().replace(Regex("[^a-z0-9_]"), "_")}"
            val cached = app.cachedIconPath?.let { File(it) }
            if (cached != null && cached.exists()) {
                cached.copyTo(File(iconsOutDir, "$safeName.png"), overwrite = true)
            }
            if (app.componentName.isNotBlank()) {
                appfilterSb.appendLine(
                    "    <item component=\"ComponentInfo{${app.componentName}}\" drawable=\"$safeName\" />"
                )
            }
            drawableSb.appendLine("    <item drawable=\"$safeName\" />")
        }

        appfilterSb.appendLine("</resources>")
        drawableSb.appendLine("</resources>")

        File(xmlOutDir, "appfilter.xml").writeText(appfilterSb.toString())
        File(xmlOutDir, "drawable.xml").writeText(drawableSb.toString())

        return File(xmlOutDir, "appfilter.xml")
    }

    fun readGeneratedAppFilterPreview(): String {
        val file = File(exportDir, "xml/appfilter.xml")
        if (file.exists()) {
            return file.readText().lineSequence().take(28).joinToString("\n")
        }
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n  <iconback img1=\"nova_iconback_black\" />\n  <iconmask img1=\"nova_iconmask_squircle\" />\n  <iconupon img1=\"nova_iconupon_frame\" />\n  <scale factor=\"0.64\" />\n</resources>"
    }

    fun createExportZipUri(theme: IconThemeEntity, apps: List<ThemedAppEntity>): Uri {
        syncDynamicNovaExport(theme, apps)
        val zipFile = File(exportDir, "AutoIcon_NovaTheme_${theme.name.replace(" ", "_")}.zip")
        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
            exportDir.walkTopDown()
                .filter { it.isFile && !it.name.endsWith(".zip") }
                .forEach { file ->
                    val entryName = file.relativeTo(exportDir).path
                    zos.putNextEntry(ZipEntry(entryName))
                    file.inputStream().use { it.copyTo(zos) }
                    zos.closeEntry()
                }
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            zipFile
        )
    }

    fun applyIconPackToNovaLauncher(): Result<String> {
        val pm = context.packageManager
        val isNovaPresent = getPackageInfoCompat(pm, NOVA_PACKAGE) != null
        if (!isNovaPresent) {
            return Result.failure(
                IllegalStateException("Nova Launcher não encontrado neste dispositivo. Pacote de tema e appfilter.xml permanecem prontos para exportação.")
            )
        }

        return try {
            val intent = Intent(ACTION_APPLY_NOVA_THEME).apply {
                setPackage(NOVA_PACKAGE)
                putExtra(EXTRA_ICON_THEME_PACKAGE, context.packageName)
                putExtra(EXTRA_ICON_THEME_TYPE, "GO")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Result.success("Solicitação enviada ao Nova Launcher com sucesso.")
        } catch (e: Exception) {
            try {
                val fallback = pm.getLaunchIntentForPackage(NOVA_PACKAGE)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (fallback != null) {
                    context.startActivity(fallback)
                    Result.success("Nova Launcher aberto. Selecione 'AutoIcon Studio' em Aparência > Estilo dos ícones.")
                } else {
                    Result.failure(e)
                }
            } catch (ex: Exception) {
                Result.failure(ex)
            }
        }
    }

    fun pinThemedShortcutToHome(app: ThemedAppEntity): Result<String> {
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            return Result.failure(IllegalStateException("O launcher atual não suporta fixação direta de atalhos."))
        }
        val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
            ?: return Result.failure(IllegalStateException("Não foi possível obter a Activity inicial de ${app.appName}."))

        val iconBitmap = app.cachedIconPath?.let { path ->
            val file = File(path)
            if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
        } ?: return Result.failure(IllegalStateException("Ícone processado não encontrado em cache."))

        val shortcut = ShortcutInfoCompat.Builder(context, "autoicon_${app.packageName}")
            .setShortLabel(app.appName)
            .setLongLabel(app.appName)
            .setIcon(IconCompat.createWithBitmap(iconBitmap))
            .setIntent(launchIntent)
            .build()

        val pinned = ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
        return if (pinned) {
            Result.success("Solicitação de atalho Black Minimalist enviada para ${app.appName}.")
        } else {
            Result.failure(IllegalStateException("Não foi possível solicitar a fixação do atalho."))
        }
    }

    private fun generateIconBackPng(outFile: File, theme: IconThemeEntity) {
        val size = 192
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val pad = size * 0.04f
        val rect = RectF(pad, pad, size - pad, size - pad)
        val radius = (rect.width() / 2f) * (theme.cornerRadiusPercent.coerceIn(0f, 50f) / 50f)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = try { Color.parseColor(theme.backgroundHex) } catch (_: Exception) { Color.BLACK }
        }
        canvas.drawRoundRect(rect, radius, radius, paint)
        FileOutputStream(outFile).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bmp.recycle()
    }

    private fun generateIconMaskPng(outFile: File, theme: IconThemeEntity) {
        val size = 192
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.BLACK)
        val pad = size * 0.14f
        val rect = RectF(pad, pad, size - pad, size - pad)
        val radius = (rect.width() / 2f) * (theme.cornerRadiusPercent.coerceIn(0f, 50f) / 50f)
        val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
        canvas.drawRoundRect(rect, radius, radius, clearPaint)
        FileOutputStream(outFile).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bmp.recycle()
    }

    private fun generateIconUponPng(outFile: File, theme: IconThemeEntity) {
        val size = 192
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        if (theme.borderWidthDp > 0.05f) {
            val pad = size * 0.04f
            val strokePx = (theme.borderWidthDp / 48f) * size
            val rect = RectF(pad + strokePx / 2f, pad + strokePx / 2f, size - pad - strokePx / 2f, size - pad - strokePx / 2f)
            val radius = (rect.width() / 2f) * (theme.cornerRadiusPercent.coerceIn(0f, 50f) / 50f)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = strokePx
                color = Color.argb((theme.borderAlpha.coerceIn(0f, 0.5f) * 255).roundToInt(), 255, 255, 255)
            }
            canvas.drawRoundRect(rect, radius, radius, paint)
        }
        FileOutputStream(outFile).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bmp.recycle()
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
}
