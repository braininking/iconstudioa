package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.local.IconThemeEntity
import com.example.data.local.ThemedAppEntity
import com.example.data.model.PreviewGridSize
import com.example.engine.MinimalIconLibrary
import com.example.ui.MainViewModel
import com.example.ui.theme.GraphiteBorder
import com.example.ui.theme.GraphiteElevated
import com.example.ui.theme.GraphiteSurface
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SilverSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LiveSymbolIcon(
    symbolId: String,
    theme: IconThemeEntity,
    size: Dp,
    viewModel: MainViewModel,
    contentDescription: String? = null,
    modifier: Modifier = Modifier
) {
    val bitmap by produceState<Bitmap?>(initialValue = null, symbolId, theme) {
        value = viewModel.renderLiveLibraryPreview(symbolId, theme, sizePx = 224)
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        val current = bitmap
        if (current != null) {
            Image(
                bitmap = current.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
                    .background(PureBlack)
            )
        }
    }
}

@Composable
fun LiveAppIcon(
    appEntity: ThemedAppEntity,
    theme: IconThemeEntity,
    size: Dp,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val bitmap by produceState<Bitmap?>(initialValue = null, appEntity.packageName, theme) {
        value = viewModel.renderLiveAppPreview(appEntity, theme, sizePx = 224)
    }
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        val current = bitmap
        if (current != null) {
            Image(
                bitmap = current.asImageBitmap(),
                contentDescription = appEntity.appName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
                    .background(PureBlack)
            )
        }
    }
}

@Composable
fun CachedIconImage(
    path: String?,
    contentDescription: String,
    size: Dp,
    cacheVersion: Int,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val bitmap by produceState<Bitmap?>(initialValue = null, path, cacheVersion) {
        value = viewModel.loadCachedIconBitmap(path)
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        val current = bitmap
        if (current != null) {
            Image(
                bitmap = current.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(12.dp),
                color = PureBlack,
                border = BorderStroke(1.dp, GraphiteBorder)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Apps,
                        contentDescription = null,
                        tint = SilverSecondary,
                        modifier = Modifier.size(size * 0.45f)
                    )
                }
            }
        }
    }
}

@Composable
fun OriginalAppIcon(
    packageName: String,
    contentDescription: String,
    size: Dp,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val bitmap by produceState<Bitmap?>(initialValue = null, packageName) {
        value = viewModel.loadOriginalIconBitmap(packageName)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .background(GraphiteElevated),
        contentAlignment = Alignment.Center
    ) {
        val current = bitmap
        if (current != null) {
            Image(
                bitmap = current.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun HomeScreenGridPreview(
    theme: IconThemeEntity,
    installedApps: List<ThemedAppEntity>,
    gridSize: PreviewGridSize,
    onGridSizeSelected: (PreviewGridSize) -> Unit,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val showcaseSymbols = listOf(
        "whatsapp" to "WhatsApp",
        "instagram" to "Instagram",
        "youtube" to "YouTube",
        "spotify" to "Spotify",
        "chrome" to "Chrome",
        "settings" to "Config."
    )
    // Add up to 2 real unknown/auto-processed apps from device if available
    val unknownApps = installedApps
        .filter { it.matchedLibraryIconId == null }
        .take(2)

    val dockSymbols = listOf("phone", "telegram", "camera", "files")

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = PureBlack,
        border = BorderStroke(1.dp, GraphiteBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    val step = 28.dp.toPx()
                    var x = step
                    while (x < size.width) {
                        var y = step
                        while (y < size.height) {
                            drawCircle(
                                color = Color(0x14FFFFFF),
                                radius = 1.dp.toPx(),
                                center = Offset(x, y)
                            )
                            y += step
                        }
                        x += step
                    }
                }
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0E0E12), Color(0xFF050507))
                    )
                )
                .padding(16.dp)
        ) {
            // Top bar: Preview title & Size selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TELA INICIAL ANDROID",
                    style = MaterialTheme.typography.labelMedium,
                    color = SilverSecondary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PreviewGridSize.entries.forEach { sizeOption ->
                        val selected = sizeOption == gridSize
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) PureWhite else GraphiteElevated,
                            border = BorderStroke(1.dp, if (selected) PureWhite else GraphiteBorder),
                            modifier = Modifier.clickable { onGridSizeSelected(sizeOption) }
                        ) {
                            Text(
                                text = sizeOption.labelPt,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) PureBlack else SilverSecondary,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4x2 Grid Row 1
            val iconDp = gridSize.dpSize.dp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                showcaseSymbols.take(4).forEach { (id, label) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(68.dp)
                    ) {
                        LiveSymbolIcon(
                            symbolId = id,
                            theme = theme,
                            size = iconDp,
                            viewModel = viewModel,
                            contentDescription = label
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = PureWhite.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4x2 Grid Row 2 (2 library + up to 2 real device apps)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                showcaseSymbols.drop(4).forEach { (id, label) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(68.dp)
                    ) {
                        LiveSymbolIcon(
                            symbolId = id,
                            theme = theme,
                            size = iconDp,
                            viewModel = viewModel,
                            contentDescription = label
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = PureWhite.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                if (unknownApps.isNotEmpty()) {
                    unknownApps.forEach { appEntity ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(68.dp)
                        ) {
                            LiveAppIcon(
                                appEntity = appEntity,
                                theme = theme,
                                size = iconDp,
                                viewModel = viewModel
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                text = appEntity.appName,
                                style = MaterialTheme.typography.labelSmall,
                                color = PureWhite.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                val remaining = 2 - unknownApps.size
                if (remaining > 0) {
                    val extras = listOf("gmail" to "Gmail", "maps" to "Maps").take(remaining)
                    extras.forEach { (id, label) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(68.dp)
                        ) {
                            LiveSymbolIcon(
                                symbolId = id,
                                theme = theme,
                                size = iconDp,
                                viewModel = viewModel,
                                contentDescription = label
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = PureWhite.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dock container
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF141418).copy(alpha = 0.88f),
                border = BorderStroke(1.dp, Color(0xFF23232A))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    dockSymbols.forEach { id ->
                        val spec = MinimalIconLibrary.getById(id)
                        LiveSymbolIcon(
                            symbolId = id,
                            theme = theme,
                            size = (gridSize.dpSize - 4).dp,
                            viewModel = viewModel,
                            contentDescription = spec?.displayName
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusPill(
    text: String,
    dotColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = GraphiteElevated,
        border = BorderStroke(1.dp, GraphiteBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = PureWhite,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = PureWhite
        )
        if (!subtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = SilverSecondary
            )
        }
    }
}

fun formatTimestampPt(timestamp: Long): String {
    if (timestamp <= 0L) return "Ainda não executado"
    val formatter = SimpleDateFormat("dd/MM HH:mm:ss", Locale("pt", "BR"))
    return formatter.format(Date(timestamp))
}
