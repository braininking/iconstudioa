package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.IconThemeEntity
import com.example.data.model.RenderMode
import com.example.data.model.StrokeStyle
import com.example.engine.MinimalIconLibrary
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.components.HomeScreenGridPreview
import com.example.ui.components.LiveSymbolIcon
import com.example.ui.theme.GraphiteBorder
import com.example.ui.theme.GraphiteElevated
import com.example.ui.theme.GraphiteSurface
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SilverSecondary
import kotlin.math.roundToInt

@Composable
fun EditorScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val theme = uiState.draftTheme ?: return
    var showHomeGridPreview by remember { mutableStateOf(true) }
    var focusedSymbolId by remember { mutableStateOf("whatsapp") }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header + Preview Switcher
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Editor Black Minimalist",
                            style = MaterialTheme.typography.headlineSmall,
                            color = PureWhite
                        )
                        Text(
                            text = "Editando: ${theme.name} • Tempo real",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverSecondary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PreviewToggleChip(
                            icon = Icons.Outlined.GridView,
                            label = "Grade",
                            selected = showHomeGridPreview,
                            onClick = { showHomeGridPreview = true }
                        )
                        PreviewToggleChip(
                            icon = Icons.Outlined.ZoomIn,
                            label = "Foco",
                            selected = !showHomeGridPreview,
                            onClick = { showHomeGridPreview = false }
                        )
                    }
                }
            }

            // Live Preview Area
            item {
                if (showHomeGridPreview) {
                    HomeScreenGridPreview(
                        theme = theme,
                        installedApps = uiState.apps,
                        gridSize = uiState.settings.previewGridSize,
                        onGridSizeSelected = { viewModel.setPreviewGridSize(it) },
                        viewModel = viewModel
                    )
                } else {
                    FocusInspectorPreview(
                        theme = theme,
                        focusedSymbolId = focusedSymbolId,
                        onSelectSymbol = { focusedSymbolId = it },
                        viewModel = viewModel
                    )
                }
            }

            // Render Mode & Stroke Style
            item {
                EditorCard(title = "Identidade do Traço") {
                    Text(
                        text = "Modo de Renderização",
                        style = MaterialTheme.typography.labelLarge,
                        color = SilverSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RenderMode.entries.forEach { mode ->
                            val selected = theme.resolvedRenderMode() == mode
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clickable {
                                        viewModel.updateDraftTheme { it.copy(renderMode = mode.name) }
                                    },
                                shape = RoundedCornerShape(14.dp),
                                color = if (selected) PureWhite else GraphiteElevated,
                                border = BorderStroke(1.dp, if (selected) PureWhite else GraphiteBorder)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = mode.labelPt,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (selected) PureBlack else PureWhite
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Estilo de Contorno",
                        style = MaterialTheme.typography.labelLarge,
                        color = SilverSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StrokeStyle.entries.forEach { style ->
                            val selected = theme.resolvedStrokeStyle() == style
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) PureWhite else GraphiteElevated,
                                border = BorderStroke(1.dp, if (selected) PureWhite else GraphiteBorder),
                                modifier = Modifier
                                    .height(44.dp)
                                    .clickable {
                                        viewModel.updateDraftTheme { it.copy(strokeStyle = style.name) }
                                    }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 14.dp)
                                ) {
                                    Text(
                                        text = style.labelPt,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (selected) PureBlack else SilverSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    EditorSlider(
                        label = "Espessura dos Traços",
                        valueText = "${"%.1f".format(theme.strokeWidthDp)} dp",
                        value = theme.strokeWidthDp,
                        valueRange = 1.0f..4.0f,
                        onValueChange = { v ->
                            viewModel.updateDraftTheme { it.copy(strokeWidthDp = v) }
                        }
                    )

                    EditorSlider(
                        label = "Intensidade do Branco (#FFFFFF)",
                        valueText = "${(theme.whiteIntensity * 100).roundToInt()}%",
                        value = theme.whiteIntensity,
                        valueRange = 0.70f..1.0f,
                        onValueChange = { v ->
                            viewModel.updateDraftTheme { it.copy(whiteIntensity = v) }
                        }
                    )
                }
            }

            // Geometry & Position
            item {
                EditorCard(title = "Geometria, Escala e Posição") {
                    Text(
                        text = "Formato / Raio dos Cantos",
                        style = MaterialTheme.typography.labelLarge,
                        color = SilverSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "Quadrado" to 6f,
                            "Squircle" to 26f,
                            "Circular" to 50f
                        ).forEach { (label, radiusPct) ->
                            val selected = kotlin.math.abs(theme.cornerRadiusPercent - radiusPct) < 3f
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clickable {
                                        viewModel.updateDraftTheme { it.copy(cornerRadiusPercent = radiusPct) }
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) PureWhite else GraphiteElevated,
                                border = BorderStroke(1.dp, if (selected) PureWhite else GraphiteBorder)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (selected) PureBlack else SilverSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    EditorSlider(
                        label = "Raio dos Cantos",
                        valueText = "${theme.cornerRadiusPercent.roundToInt()}%",
                        value = theme.cornerRadiusPercent,
                        valueRange = 0f..50f,
                        onValueChange = { v ->
                            viewModel.updateDraftTheme { it.copy(cornerRadiusPercent = v) }
                        }
                    )

                    EditorSlider(
                        label = "Escala do Símbolo",
                        valueText = "${(theme.symbolScale * 100).roundToInt()}%",
                        value = theme.symbolScale,
                        valueRange = 0.42f..0.85f,
                        onValueChange = { v ->
                            viewModel.updateDraftTheme { it.copy(symbolScale = v) }
                        }
                    )

                    EditorSlider(
                        label = "Posição Horizontal (X)",
                        valueText = "${(theme.offsetX * 100).roundToInt()}%",
                        value = theme.offsetX,
                        valueRange = -0.15f..0.15f,
                        onValueChange = { v ->
                            viewModel.updateDraftTheme { it.copy(offsetX = v) }
                        }
                    )

                    EditorSlider(
                        label = "Posição Vertical (Y)",
                        valueText = "${(theme.offsetY * 100).roundToInt()}%",
                        value = theme.offsetY,
                        valueRange = -0.15f..0.15f,
                        onValueChange = { v ->
                            viewModel.updateDraftTheme { it.copy(offsetY = v) }
                        }
                    )
                }
            }

            // Mandatory Black Background & Optional Hairline Border
            item {
                EditorCard(title = "Fundo Preto e Filete Opcional") {
                    Text(
                        text = "Base Negra (Padrão Obrigatório #000000)",
                        style = MaterialTheme.typography.labelLarge,
                        color = SilverSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(
                            "#000000" to "Preto Puro",
                            "#050507" to "Obsidiana",
                            "#0C0C10" to "Grafite Dark"
                        ).forEach { (hex, label) ->
                            val selected = theme.backgroundHex.equals(hex, ignoreCase = true)
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .clickable {
                                        viewModel.updateDraftTheme { it.copy(backgroundHex = hex) }
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = GraphiteElevated,
                                border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) PureWhite else GraphiteBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(Color(android.graphics.Color.parseColor(hex)))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) PureWhite else SilverSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    EditorSlider(
                        label = "Contorno Externo Fino (Opcional)",
                        valueText = if (theme.borderWidthDp < 0.1f) "Desligado" else "${"%.1f".format(theme.borderWidthDp)} dp",
                        value = theme.borderWidthDp,
                        valueRange = 0f..1.8f,
                        onValueChange = { v ->
                            viewModel.updateDraftTheme { it.copy(borderWidthDp = v) }
                        }
                    )
                }
            }

            // Save & Reset Action Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.resetDraftToActive() },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, GraphiteBorder),
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("reset_editor_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.RestartAlt,
                            contentDescription = "Restaurar",
                            tint = PureWhite
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Desfazer", color = PureWhite)
                    }

                    Button(
                        onClick = { viewModel.saveDraftThemeAndApply() },
                        enabled = !uiState.isReprocessing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PureWhite,
                            contentColor = PureBlack
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("save_theme_button")
                    ) {
                        if (uiState.isReprocessing) {
                            CircularProgressIndicator(
                                color = PureBlack,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reprocessando...")
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Salvar e Aplicar Estilo")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun FocusInspectorPreview(
    theme: IconThemeEntity,
    focusedSymbolId: String,
    onSelectSymbol: (String) -> Unit,
    viewModel: MainViewModel
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = GraphiteSurface,
        border = BorderStroke(1.dp, GraphiteBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF0A0A0C))
                    .drawBehind {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        drawLine(Color(0x18FFFFFF), Offset(cx, 0f), Offset(cx, size.height), 1f)
                        drawLine(Color(0x18FFFFFF), Offset(0f, cy), Offset(size.width, cy), 1f)
                    },
                contentAlignment = Alignment.Center
            ) {
                LiveSymbolIcon(
                    symbolId = focusedSymbolId,
                    theme = theme,
                    size = 136.dp,
                    viewModel = viewModel,
                    contentDescription = focusedSymbolId
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MinimalIconLibrary.allSymbols.take(12).forEach { spec ->
                    val selected = spec.id == focusedSymbolId
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selected) PureWhite else GraphiteElevated,
                        border = BorderStroke(1.dp, if (selected) PureWhite else GraphiteBorder),
                        modifier = Modifier.clickable { onSelectSymbol(spec.id) }
                    ) {
                        Text(
                            text = spec.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) PureBlack else SilverSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewToggleChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) PureWhite else GraphiteElevated,
        border = BorderStroke(1.dp, if (selected) PureWhite else GraphiteBorder),
        modifier = Modifier
            .height(40.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) PureBlack else SilverSecondary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) PureBlack else SilverSecondary
            )
        }
    }
}

@Composable
private fun EditorCard(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = GraphiteSurface,
        border = BorderStroke(1.dp, GraphiteBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = PureWhite
            )
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun EditorSlider(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = PureWhite
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelMedium,
                color = SilverSecondary
            )
        }
        Slider(
            value = value.coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = PureWhite,
                activeTrackColor = PureWhite,
                inactiveTrackColor = GraphiteBorder
            )
        )
    }
}
