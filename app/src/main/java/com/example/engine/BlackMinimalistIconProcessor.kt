package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.graphics.PathParser
import com.example.data.local.IconThemeEntity
import com.example.data.model.ProcessingStatus
import com.example.data.model.RenderMode
import com.example.data.model.StrokeStyle
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class ProcessedIconResult(
    val filePath: String,
    val status: ProcessingStatus,
    val matchedSymbolId: String?
)

class BlackMinimalistIconProcessor(private val context: Context) {

    private val iconsDir: File by lazy {
        File(context.filesDir, "themed_icons").apply { mkdirs() }
    }

    fun getCachedIconFile(packageName: String): File =
        File(iconsDir, "${packageName.replace(Regex("[^a-zA-Z0-9._-]"), "_")}.png")

    fun processAndSaveAppIcon(
        packageName: String,
        appName: String,
        originalDrawable: Drawable?,
        theme: IconThemeEntity,
        manualSymbolId: String? = null,
        customRenderMode: RenderMode? = null,
        customScaleOverride: Float? = null,
        customStrokeOverride: Float? = null
    ): ProcessedIconResult {
        val effectiveTheme = theme.copy(
            symbolScale = customScaleOverride ?: theme.symbolScale,
            strokeWidthDp = customStrokeOverride ?: theme.strokeWidthDp,
            renderMode = (customRenderMode ?: theme.resolvedRenderMode()).name
        )

        val matchedSpec = if (manualSymbolId != null) {
            MinimalIconLibrary.getById(manualSymbolId)
        } else {
            MinimalIconLibrary.findSymbolForApp(packageName, appName)
        }

        val bitmap: Bitmap
        val status: ProcessingStatus

        if (matchedSpec != null) {
            bitmap = renderLibrarySymbolBitmap(matchedSpec, effectiveTheme, sizePx = 512)
            status = if (manualSymbolId != null) ProcessingStatus.MANUAL_OVERRIDE else ProcessingStatus.PROCESSED_LIBRARY
        } else if (originalDrawable != null) {
            val (fallbackBitmap, usedMode) = renderUnknownAppFallbackBitmap(
                drawable = originalDrawable,
                theme = effectiveTheme,
                sizePx = 512
            )
            bitmap = fallbackBitmap
            status = when {
                customRenderMode != null -> ProcessingStatus.MANUAL_OVERRIDE
                usedMode == RenderMode.LINE_ART -> ProcessingStatus.PROCESSED_AUTO_LINE_ART
                else -> ProcessingStatus.PROCESSED_AUTO_SILHOUETTE
            }
        } else {
            val fallbackSpec = MinimalIconLibrary.getById("productivity") ?: MinimalIconLibrary.allSymbols.first()
            bitmap = renderLibrarySymbolBitmap(fallbackSpec, effectiveTheme, sizePx = 512)
            status = ProcessingStatus.PROCESSED_AUTO_SILHOUETTE
        }

        val outFile = getCachedIconFile(packageName)
        val tempFile = File(iconsDir, "${outFile.name}.tmp")
        FileOutputStream(tempFile).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
        if (outFile.exists()) outFile.delete()
        tempFile.renameTo(outFile)
        bitmap.recycle()

        return ProcessedIconResult(
            filePath = outFile.absolutePath,
            status = status,
            matchedSymbolId = matchedSpec?.id
        )
    }

    /**
     * Renders a crisp vector symbol from MinimalIconLibrary with live theme parameters.
     * Used both for disk export (512px) and real-time Compose editor previews.
     */
    fun renderLibrarySymbolBitmap(
        spec: VectorSymbolSpec,
        theme: IconThemeEntity,
        sizePx: Int = 256
    ): Bitmap {
        val output = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        drawBlackRoundedBase(canvas, sizePx, theme)

        val whiteAlpha = (theme.whiteIntensity.coerceIn(0.6f, 1.0f) * 255).roundToInt()
        val symbolColor = Color.argb(whiteAlpha, 255, 255, 255)
        val renderMode = theme.resolvedRenderMode()
        val strokeStyle = theme.resolvedStrokeStyle()

        // Vector coordinates live in 48x48 viewport centered around (24,24) spanning ~22 units.
        // Scale factor maps symbol to fill `symbolScale` of the icon canvas.
        val scaleFactor = (sizePx / 24f) * theme.symbolScale.coerceIn(0.35f, 0.92f)
        val centerPx = sizePx / 2f
        val tx = centerPx + theme.offsetX * sizePx
        val ty = centerPx + theme.offsetY * sizePx

        val matrix = Matrix().apply {
            postTranslate(-24f, -24f)
            postScale(scaleFactor, scaleFactor)
            postTranslate(tx, ty)
        }

        val baseStrokePx = (theme.strokeWidthDp / 24f) * scaleFactor * when (strokeStyle) {
            StrokeStyle.THIN -> 0.72f
            StrokeStyle.DOUBLE -> 0.85f
            else -> 1.0f
        }

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = if (renderMode == RenderMode.SILHOUETTE) Paint.Style.FILL_AND_STROKE else Paint.Style.STROKE
            color = symbolColor
            strokeWidth = baseStrokePx.coerceAtLeast(1.5f)
            strokeCap = if (strokeStyle == StrokeStyle.GEOMETRIC) Paint.Cap.SQUARE else Paint.Cap.ROUND
            strokeJoin = if (strokeStyle == StrokeStyle.GEOMETRIC) Paint.Join.MITER else Paint.Join.ROUND
        }

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = symbolColor
        }

        for (pathData in spec.strokePaths) {
            val path = PathParser.createPathFromPathData(pathData) ?: continue
            path.transform(matrix)
            if (strokeStyle == StrokeStyle.DOUBLE && renderMode == RenderMode.LINE_ART) {
                val outerPaint = Paint(strokePaint).apply {
                    strokeWidth = (baseStrokePx * 2.0f).coerceAtLeast(3f)
                    alpha = (whiteAlpha * 0.35f).roundToInt()
                }
                canvas.drawPath(path, outerPaint)
            }
            canvas.drawPath(path, strokePaint)
        }

        for (pathData in spec.fillPaths) {
            val path = PathParser.createPathFromPathData(pathData) ?: continue
            path.transform(matrix)
            canvas.drawPath(path, fillPaint)
        }

        drawOptionalSubtleBorder(canvas, sizePx, theme)
        return output
    }

    /**
     * Fallback extractor for unknown apps:
     * 1. Extracts AdaptiveIconDrawable foreground (avoiding background plate) or legacy icon.
     * 2. Strips uniform corner background & computes tight bounding box.
     * 3. Converts to pure #FFFFFF line art (Sobel) or clean #FFFFFF silhouette on #000000.
     * 4. Guards against noisy photographic edges by switching cleanly to Silhouette mode.
     */
    fun renderUnknownAppFallbackBitmap(
        drawable: Drawable,
        theme: IconThemeEntity,
        sizePx: Int = 256
    ): Pair<Bitmap, RenderMode> {
        val workSize = 128
        val rawBitmap = extractForegroundWorkingBitmap(drawable, workSize)
        val pixels = IntArray(workSize * workSize)
        rawBitmap.getPixels(pixels, 0, workSize, 0, 0, workSize, workSize)
        rawBitmap.recycle()

        // Detect uniform background color at corners if opaque
        val bgSample = sampleCornerColor(pixels, workSize)
        val fgMask = BooleanArray(workSize * workSize)
        val lum = FloatArray(workSize * workSize)

        var minX = workSize
        var minY = workSize
        var maxX = -1
        var maxY = -1

        for (y in 0 until workSize) {
            for (x in 0 until workSize) {
                val idx = y * workSize + x
                val c = pixels[idx]
                val a = Color.alpha(c)
                val r = Color.red(c)
                val g = Color.green(c)
                val b = Color.blue(c)
                lum[idx] = (0.299f * r + 0.587f * g + 0.114f * b) / 255f

                val isForeground = if (a < 40) {
                    false
                } else if (bgSample != null) {
                    val dist = colorDistance(c, bgSample)
                    dist > 34f
                } else {
                    true
                }

                fgMask[idx] = isForeground
                if (isForeground) {
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                }
            }
        }

        // Fallback if whole icon was flagged transparent/uniform
        if (maxX <= minX || maxY <= minY) {
            minX = workSize / 6
            maxX = workSize - workSize / 6
            minY = workSize / 6
            maxY = workSize - workSize / 6
            for (y in minY..maxY) {
                for (x in minX..maxX) {
                    val c = pixels[y * workSize + x]
                    if (Color.alpha(c) > 40) fgMask[y * workSize + x] = true
                }
            }
        }

        // Compute Sobel edges on smoothed luminance + alpha
        val smoothed = FloatArray(workSize * workSize)
        for (y in 1 until workSize - 1) {
            for (x in 1 until workSize - 1) {
                var sum = 0f
                for (dy in -1..1) {
                    for (dx in -1..1) {
                        val nIdx = (y + dy) * workSize + (x + dx)
                        val alphaWeight = Color.alpha(pixels[nIdx]) / 255f
                        sum += lum[nIdx] * alphaWeight
                    }
                }
                smoothed[y * workSize + x] = sum / 9f
            }
        }

        val edgeMap = FloatArray(workSize * workSize)
        var edgeHits = 0
        val fgArea = max(1, (maxX - minX + 1) * (maxY - minY + 1))

        for (y in 1 until workSize - 1) {
            for (x in 1 until workSize - 1) {
                val gx = -smoothed[(y - 1) * workSize + (x - 1)] + smoothed[(y - 1) * workSize + (x + 1)] -
                    2f * smoothed[y * workSize + (x - 1)] + 2f * smoothed[y * workSize + (x + 1)] -
                    smoothed[(y + 1) * workSize + (x - 1)] + smoothed[(y + 1) * workSize + (x + 1)]

                val gy = -smoothed[(y - 1) * workSize + (x - 1)] - 2f * smoothed[(y - 1) * workSize + x] - smoothed[(y - 1) * workSize + (x + 1)] +
                    smoothed[(y + 1) * workSize + (x - 1)] + 2f * smoothed[(y + 1) * workSize + x] + smoothed[(y + 1) * workSize + (x + 1)]

                val mag = sqrt(gx * gx + gy * gy)
                val idx = y * workSize + x
                edgeMap[idx] = mag
                if (mag > 0.22f && x in minX..maxX && y in minY..maxY) {
                    edgeHits++
                }
            }
        }

        val requestedMode = theme.resolvedRenderMode()
        val edgeDensity = edgeHits.toFloat() / fgArea.toFloat()
        // If edges are too sparse or overly noisy, silhouette preserves clean recognition
        val effectiveMode = if (requestedMode == RenderMode.LINE_ART && edgeDensity in 0.03f..0.42f) {
            RenderMode.LINE_ART
        } else {
            RenderMode.SILHOUETTE
        }

        val monoBitmap = Bitmap.createBitmap(workSize, workSize, Bitmap.Config.ARGB_8888)
        val monoPixels = IntArray(workSize * workSize)
        val whiteAlpha = (theme.whiteIntensity.coerceIn(0.6f, 1.0f) * 255).roundToInt()

        if (effectiveMode == RenderMode.LINE_ART) {
            // Line-art dilation thickness scaled by strokeWidthDp
            val radius = if (theme.strokeWidthDp >= 2.6f) 1 else 0
            for (y in 1 until workSize - 1) {
                for (x in 1 until workSize - 1) {
                    var maxMag = edgeMap[y * workSize + x]
                    if (radius > 0) {
                        for (dy in -radius..radius) {
                            for (dx in -radius..radius) {
                                val m = edgeMap[(y + dy) * workSize + (x + dx)]
                                if (m > maxMag) maxMag = m
                            }
                        }
                    }
                    // Also include crisp outer contour of foreground mask
                    val isBoundary = fgMask[y * workSize + x] && (
                        !fgMask[(y - 1) * workSize + x] || !fgMask[(y + 1) * workSize + x] ||
                            !fgMask[y * workSize + (x - 1)] || !fgMask[y * workSize + (x + 1)]
                        )
                    val strength = if (isBoundary) 1f else ((maxMag - 0.16f) / 0.30f).coerceIn(0f, 1f)
                    val pxAlpha = (strength * whiteAlpha).roundToInt()
                    monoPixels[y * workSize + x] = Color.argb(pxAlpha, 255, 255, 255)
                }
            }
        } else {
            // Clean monochrome white silhouette of foreground symbol
            for (y in 0 until workSize) {
                for (x in 0 until workSize) {
                    val idx = y * workSize + x
                    if (fgMask[idx]) {
                        val origAlpha = Color.alpha(pixels[idx]) / 255f
                        val pxAlpha = (origAlpha * whiteAlpha).roundToInt()
                        monoPixels[idx] = Color.argb(pxAlpha, 255, 255, 255)
                    } else {
                        monoPixels[idx] = Color.TRANSPARENT
                    }
                }
            }
        }
        monoBitmap.setPixels(monoPixels, 0, workSize, 0, 0, workSize, workSize)

        val output = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        drawBlackRoundedBase(canvas, sizePx, theme)

        val srcRect = Rect(minX, minY, maxX + 1, maxY + 1)
        val bw = max(1, srcRect.width()).toFloat()
        val bh = max(1, srcRect.height()).toFloat()
        val targetSpan = sizePx * theme.symbolScale.coerceIn(0.35f, 0.85f)
        val fitScale = min(targetSpan / bw, targetSpan / bh)
        val drawW = bw * fitScale
        val drawH = bh * fitScale
        val cx = sizePx / 2f + theme.offsetX * sizePx
        val cy = sizePx / 2f + theme.offsetY * sizePx
        val dstRect = RectF(cx - drawW / 2f, cy - drawH / 2f, cx + drawW / 2f, cy + drawH / 2f)

        val bmpPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(monoBitmap, srcRect, dstRect, bmpPaint)
        monoBitmap.recycle()

        drawOptionalSubtleBorder(canvas, sizePx, theme)
        return output to effectiveMode
    }

    private fun extractForegroundWorkingBitmap(drawable: Drawable, size: Int): Bitmap {
        val targetDrawable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && drawable is AdaptiveIconDrawable) {
            drawable.foreground ?: drawable
        } else {
            drawable
        }
        if (targetDrawable is BitmapDrawable && targetDrawable.bitmap != null) {
            return Bitmap.createScaledBitmap(targetDrawable.bitmap, size, size, true)
        }
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        targetDrawable.setBounds(0, 0, size, size)
        targetDrawable.draw(canvas)
        return bmp
    }

    private fun sampleCornerColor(pixels: IntArray, size: Int): Int? {
        val corners = listOf(
            pixels[2 * size + 2],
            pixels[2 * size + (size - 3)],
            pixels[(size - 3) * size + 2],
            pixels[(size - 3) * size + (size - 3)]
        )
        if (corners.any { Color.alpha(it) < 200 }) return null
        val ref = corners.first()
        val uniform = corners.all { colorDistance(it, ref) < 28f }
        return if (uniform) ref else null
    }

    private fun colorDistance(c1: Int, c2: Int): Float {
        val dr = abs(Color.red(c1) - Color.red(c2))
        val dg = abs(Color.green(c1) - Color.green(c2))
        val db = abs(Color.blue(c1) - Color.blue(c2))
        return (dr + dg + db) / 3f
    }

    private fun drawBlackRoundedBase(canvas: Canvas, sizePx: Int, theme: IconThemeEntity) {
        val bgColor = try {
            Color.parseColor(theme.backgroundHex)
        } catch (_: Exception) {
            Color.BLACK
        }
        val pad = sizePx * 0.04f
        val rect = RectF(pad, pad, sizePx - pad, sizePx - pad)
        val radius = (rect.width() / 2f) * (theme.cornerRadiusPercent.coerceIn(0f, 50f) / 50f)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = bgColor
        }
        canvas.drawRoundRect(rect, radius, radius, bgPaint)
    }

    private fun drawOptionalSubtleBorder(canvas: Canvas, sizePx: Int, theme: IconThemeEntity) {
        if (theme.borderWidthDp <= 0.05f || theme.borderAlpha <= 0.01f) return
        val pad = sizePx * 0.04f
        val strokePx = (theme.borderWidthDp / 48f) * sizePx
        val inset = pad + strokePx / 2f
        val rect = RectF(inset, inset, sizePx - inset, sizePx - inset)
        val radius = (rect.width() / 2f) * (theme.cornerRadiusPercent.coerceIn(0f, 50f) / 50f)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = strokePx
            color = Color.argb((theme.borderAlpha.coerceIn(0f, 0.5f) * 255).roundToInt(), 255, 255, 255)
        }
        canvas.drawRoundRect(rect, radius, radius, borderPaint)
    }
}
