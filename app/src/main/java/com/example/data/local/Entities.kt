package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.DetectionEventType
import com.example.data.model.MinimalCategory
import com.example.data.model.ProcessingStatus
import com.example.data.model.RenderMode
import com.example.data.model.StrokeStyle

@Entity(tableName = "icon_themes")
data class IconThemeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val isPreset: Boolean = false,
    val isActive: Boolean = false,
    val cornerRadiusPercent: Float = 26f,      // 0f = square, 26f = rounded square, 50f = circle
    val strokeWidthDp: Float = 2.0f,           // 1.1f..4.5f
    val symbolScale: Float = 0.64f,            // 0.42f..0.86f
    val offsetX: Float = 0f,                   // -0.15f..0.15f
    val offsetY: Float = 0f,                   // -0.15f..0.15f
    val whiteIntensity: Float = 1.0f,          // 0.70f..1.0f
    val backgroundHex: String = "#000000",     // Mandatory black base with optional subtle deep obsidian tweaks
    val borderWidthDp: Float = 0f,             // 0f..2.0f optional subtle hairline border
    val borderAlpha: Float = 0.16f,            // 0f..0.45f
    val strokeStyle: String = StrokeStyle.ROUND.name,
    val renderMode: String = RenderMode.LINE_ART.name,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun resolvedStrokeStyle(): StrokeStyle =
        StrokeStyle.entries.firstOrNull { it.name == strokeStyle } ?: StrokeStyle.ROUND

    fun resolvedRenderMode(): RenderMode =
        RenderMode.entries.firstOrNull { it.name == renderMode } ?: RenderMode.LINE_ART
}

@Entity(tableName = "themed_apps")
data class ThemedAppEntity(
    @PrimaryKey val packageName: String,
    val componentName: String,
    val appName: String,
    val category: String = MinimalCategory.SISTEMA.name,
    val versionCode: Long = 0L,
    val lastUpdateTime: Long = 0L,
    val processingStatus: String = ProcessingStatus.PENDING.name,
    val matchedLibraryIconId: String? = null,
    val customRenderMode: String? = null,
    val customScaleOverride: Float? = null,
    val customStrokeOverride: Float? = null,
    val cachedIconPath: String? = null,
    val isNewlyDetected: Boolean = false,
    val detectedAt: Long = System.currentTimeMillis(),
    val processedAt: Long = 0L
) {
    fun resolvedCategory(): MinimalCategory =
        MinimalCategory.entries.firstOrNull { it.name == category } ?: MinimalCategory.SISTEMA

    fun resolvedStatus(): ProcessingStatus =
        ProcessingStatus.entries.firstOrNull { it.name == processingStatus } ?: ProcessingStatus.PENDING

    fun resolvedCustomRenderMode(): RenderMode? =
        customRenderMode?.let { mode -> RenderMode.entries.firstOrNull { it.name == mode } }
}

@Entity(tableName = "detection_events")
data class DetectionEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val packageName: String,
    val appName: String,
    val eventType: String,
    val resultStatus: String,
    val processingTimeMs: Long = 0L,
    val novaSynced: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun resolvedEventType(): DetectionEventType =
        DetectionEventType.entries.firstOrNull { it.name == eventType } ?: DetectionEventType.ADDED
}
