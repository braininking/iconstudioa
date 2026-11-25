package com.example

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.IconThemeEntity
import com.example.data.model.MinimalCategory
import com.example.data.model.ProcessingStatus
import com.example.data.repository.AutoIconRepository
import com.example.engine.BlackMinimalistIconProcessor
import com.example.engine.MinimalIconLibrary
import com.example.engine.NovaLauncherBridge
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `app name and nova theme intent filter are properly configured`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("AutoIcon Studio", context.getString(R.string.app_name))

        val themeIntent = Intent("com.novalauncher.THEME")
        val resolved = context.packageManager.queryIntentActivities(themeIntent, 0)
        assertTrue(
            "MainActivity must expose com.novalauncher.THEME intent filter",
            resolved.any { it.activityInfo.packageName == context.packageName }
        )
    }

    @Test
    fun `minimal icon library matches known apps and infers categories`() {
        val whatsapp = MinimalIconLibrary.findSymbolForApp("com.whatsapp", "WhatsApp")
        assertNotNull(whatsapp)
        assertEquals("whatsapp", whatsapp?.id)
        assertEquals(MinimalCategory.COMUNICACAO, whatsapp?.category)

        val instagram = MinimalIconLibrary.findSymbolForApp("com.instagram.android", "Instagram")
        assertNotNull(instagram)
        assertEquals("instagram", instagram?.id)

        val bankMatch = MinimalIconLibrary.findSymbolForApp("com.custom.unknown.bank", "Meu Banco Digital")
        assertNotNull(bankMatch)
        assertEquals("bank", bankMatch?.id)
        assertEquals(MinimalCategory.BANCOS, bankMatch?.category)

        val unknown = MinimalIconLibrary.findSymbolForApp("com.acme.fintech.pix", "Minha Carteira Pix")
        assertNull(unknown)
        assertEquals(MinimalCategory.BANCOS, MinimalIconLibrary.inferCategory("com.acme.fintech.pix", "Minha Carteira Pix"))
    }

    @Test
    fun `black minimalist processor converts colorful unknown icon strictly to monochrome black and white`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val processor = BlackMinimalistIconProcessor(context)
        val theme = IconThemeEntity(
            id = "test_bm",
            name = "Black Minimalist",
            description = "Test",
            backgroundHex = "#000000"
        )

        // Create a vibrant red/blue test icon
        val srcBmp = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(srcBmp)
        canvas.drawColor(Color.TRANSPARENT)
        val paint = Paint().apply { color = Color.RED }
        canvas.drawCircle(64f, 64f, 40f, paint)
        val drawable = BitmapDrawable(context.resources, srcBmp)

        val result = processor.processAndSaveAppIcon(
            packageName = "com.example.colorful.app",
            appName = "Colorful App",
            originalDrawable = drawable,
            theme = theme
        )

        assertTrue(
            result.status == ProcessingStatus.PROCESSED_AUTO_LINE_ART ||
                result.status == ProcessingStatus.PROCESSED_AUTO_SILHOUETTE
        )
        val outFile = File(result.filePath)
        assertTrue(outFile.exists())

        val rendered = android.graphics.BitmapFactory.decodeFile(outFile.absolutePath)
        assertNotNull(rendered)
        // Verify every opaque pixel has R == G == B (strictly monochrome, zero color leakage!)
        val samplePixels = IntArray(rendered.width * rendered.height)
        rendered.getPixels(samplePixels, 0, rendered.width, 0, 0, rendered.width, rendered.height)
        val hasColorLeak = samplePixels.any { px ->
            val a = Color.alpha(px)
            if (a == 0) false else {
                val r = Color.red(px)
                val g = Color.green(px)
                val b = Color.blue(px)
                r != g || g != b
            }
        }
        assertTrue("Processed icon must be strictly monochrome without original colors", !hasColorLeak)
        rendered.recycle()
        srcBmp.recycle()
    }

    @Test
    fun `repository seeds 8 themes and generates nova appfilter xml`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = AutoIconRepository(context)
        repo.ensureDefaultThemesSeeded()

        val themes = repo.themesFlow.first()
        assertTrue("Should seed 8 preset Black Minimalist themes", themes.size >= 8)
        val active = repo.getOrCreateActiveTheme()
        assertEquals("Black Minimalist", active.name)

        val bridge = NovaLauncherBridge(context)
        val xmlFile = bridge.syncDynamicNovaExport(active, emptyList())
        assertTrue(xmlFile.exists())
        val content = xmlFile.readText()
        assertTrue(content.contains("<iconback"))
        assertTrue(content.contains("<iconmask"))
        assertTrue(content.contains("<iconupon"))
        assertTrue(content.contains("<scale"))
    }
}
