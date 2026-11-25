package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.AddToHomeScreen
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.IconThemeEntity
import com.example.data.local.ThemedAppEntity
import com.example.data.model.MinimalCategory
import com.example.data.model.ProcessingStatus
import com.example.data.model.RenderMode
import com.example.engine.MinimalIconLibrary
import com.example.ui.AppsFilterMode
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.components.CachedIconImage
import com.example.ui.components.LiveSymbolIcon
import com.example.ui.components.OriginalAppIcon
import com.example.ui.components.StatusPill
import com.example.ui.theme.GraphiteBorder
import com.example.ui.theme.GraphiteElevated
import com.example.ui.theme.GraphiteSurface
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SilverSecondary
import com.example.ui.theme.SyncEmerald
import com.example.ui.theme.WarningAmber
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val cacheVersion by viewModel.bitmapCacheVersion.collectAsStateWithLifecycle()
    var selectedAppForFix by remember { mutableStateOf<ThemedAppEntity?>(null) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp)
        ) {
            // Header + Search + Filters
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Aplicativos (${uiState.filteredApps.size})",
                            style = MaterialTheme.typography.headlineSmall,
                            color = PureWhite
                        )
                        Text(
                            text = "Toque em um app para corrigir símbolo ou fixar atalho",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverSecondary
                        )
                    }
                    IconButton(
                        onClick = { viewModel.refreshCatalog(forceReprocess = true) },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("apps_reprocess_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Reprocessar todos",
                            tint = PureWhite
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Buscar por nome ou pacote...", color = SilverSecondary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = SilverSecondary
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PureWhite,
                        unfocusedBorderColor = GraphiteBorder,
                        focusedContainerColor = GraphiteSurface,
                        unfocusedContainerColor = GraphiteSurface,
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("apps_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Status Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppsFilterMode.entries.forEach { mode ->
                        val selected = mode == uiState.appsFilterMode
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) PureWhite else GraphiteElevated,
                            border = BorderStroke(1.dp, if (selected) PureWhite else GraphiteBorder),
                            modifier = Modifier
                                .height(36.dp)
                                .clickable { viewModel.updateAppsFilter(mode) }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    text = mode.labelPt,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) PureBlack else SilverSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 10-Category Filter Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val allSelected = uiState.selectedCategoryFilter == null
                    CategoryPill(
                        label = "Todas categorias",
                        selected = allSelected,
                        onClick = { viewModel.updateCategoryFilter(null) }
                    )
                    MinimalCategory.entries.forEach { cat ->
                        CategoryPill(
                            label = cat.labelPt,
                            selected = uiState.selectedCategoryFilter == cat,
                            onClick = { viewModel.updateCategoryFilter(cat) }
                        )
                    }
                }
            }

            if (uiState.filteredApps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Apps,
                            contentDescription = null,
                            tint = SilverSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Nenhum aplicativo encontrado",
                            style = MaterialTheme.typography.titleMedium,
                            color = PureWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ajuste os filtros ou toque em sincronizar para detectar novos apps.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.filteredApps, key = { it.packageName }) { appEntity ->
                        AppCatalogRow(
                            appEntity = appEntity,
                            cacheVersion = cacheVersion,
                            viewModel = viewModel,
                            onClick = { selectedAppForFix = appEntity }
                        )
                    }
                }
            }
        }
    }

    val targetApp = selectedAppForFix
    val activeTheme = uiState.activeTheme
    if (targetApp != null && activeTheme != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { selectedAppForFix = null },
            sheetState = sheetState,
            containerColor = GraphiteSurface,
            contentColor = PureWhite
        ) {
            ManualAppCorrectionSheet(
                appEntity = targetApp,
                activeTheme = activeTheme,
                cacheVersion = cacheVersion,
                viewModel = viewModel,
                onApplyFix = { symbolId, mode, scale, stroke ->
                    viewModel.applyManualCorrection(
                        packageName = targetApp.packageName,
                        manualSymbolId = symbolId,
                        customRenderMode = mode,
                        customScaleOverride = scale,
                        customStrokeOverride = stroke
                    )
                    selectedAppForFix = null
                },
                onPinShortcut = {
                    viewModel.pinShortcutToHome(targetApp)
                }
            )
        }
    }
}

@Composable
private fun CategoryPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) GraphiteElevated else PureBlack,
        border = BorderStroke(1.dp, if (selected) PureWhite else GraphiteBorder),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) PureWhite else SilverSecondary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun AppCatalogRow(
    appEntity: ThemedAppEntity,
    cacheVersion: Int,
    viewModel: MainViewModel,
    onClick: () -> Unit
) {
    val status = appEntity.resolvedStatus()
    val dotColor = when (status) {
        ProcessingStatus.PROCESSED_LIBRARY, ProcessingStatus.MANUAL_OVERRIDE -> SyncEmerald
        ProcessingStatus.PROCESSED_AUTO_LINE_ART, ProcessingStatus.PROCESSED_AUTO_SILHOUETTE -> PureWhite
        else -> WarningAmber
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = GraphiteSurface,
        border = BorderStroke(1.dp, GraphiteBorder),
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("app_item_${appEntity.packageName}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OriginalAppIcon(
                packageName = appEntity.packageName,
                contentDescription = "Original ${appEntity.appName}",
                size = 38.dp,
                viewModel = viewModel
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = SilverSecondary,
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .size(14.dp)
            )

            CachedIconImage(
                path = appEntity.cachedIconPath,
                contentDescription = "Black Minimalist ${appEntity.appName}",
                size = 50.dp,
                cacheVersion = cacheVersion,
                viewModel = viewModel
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appEntity.appName,
                    style = MaterialTheme.typography.titleMedium,
                    color = PureWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = appEntity.packageName,
                    style = MaterialTheme.typography.labelSmall,
                    color = SilverSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusPill(text = status.labelPt, dotColor = dotColor)
                    if (appEntity.isNewlyDetected) {
                        StatusPill(text = "Novo", dotColor = SyncEmerald)
                    }
                }
            }
        }
    }
}

@Composable
private fun ManualAppCorrectionSheet(
    appEntity: ThemedAppEntity,
    activeTheme: IconThemeEntity,
    cacheVersion: Int,
    viewModel: MainViewModel,
    onApplyFix: (String?, RenderMode?, Float?, Float?) -> Unit,
    onPinShortcut: () -> Unit
) {
    var chosenSymbolId by remember { mutableStateOf(appEntity.matchedLibraryIconId) }
    var chosenMode by remember { mutableStateOf(appEntity.resolvedCustomRenderMode()) }
    var scaleOverride by remember { mutableFloatStateOf(appEntity.customScaleOverride ?: activeTheme.symbolScale) }
    var strokeOverride by remember { mutableFloatStateOf(appEntity.customStrokeOverride ?: activeTheme.strokeWidthDp) }
    var selectedCategory by remember { mutableStateOf<MinimalCategory?>(appEntity.resolvedCategory()) }

    val previewTheme = activeTheme.copy(
        symbolScale = scaleOverride,
        strokeWidthDp = strokeOverride,
        renderMode = (chosenMode ?: activeTheme.resolvedRenderMode()).name
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Ajuste Manual & Correção",
            style = MaterialTheme.typography.titleLarge,
            color = PureWhite
        )
        Text(
            text = "${appEntity.appName} • ${appEntity.packageName}",
            style = MaterialTheme.typography.labelSmall,
            color = SilverSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Side-by-side Original vs Processed
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = GraphiteElevated,
            border = BorderStroke(1.dp, GraphiteBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    OriginalAppIcon(
                        packageName = appEntity.packageName,
                        contentDescription = "Original",
                        size = 56.dp,
                        viewModel = viewModel
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Original", style = MaterialTheme.typography.labelSmall, color = SilverSecondary)
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = PureWhite
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (chosenSymbolId != null) {
                        LiveSymbolIcon(
                            symbolId = chosenSymbolId!!,
                            theme = previewTheme,
                            size = 64.dp,
                            viewModel = viewModel
                        )
                    } else {
                        CachedIconImage(
                            path = appEntity.cachedIconPath,
                            contentDescription = "Processado",
                            size = 64.dp,
                            cacheVersion = cacheVersion,
                            viewModel = viewModel
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Black Minimalist", style = MaterialTheme.typography.labelSmall, color = PureWhite)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Modo para Fallback Automático", style = MaterialTheme.typography.labelLarge, color = SilverSecondary)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                null to "Auto",
                RenderMode.LINE_ART to "Line Art",
                RenderMode.SILHOUETTE to "Silhueta"
            ).forEach { (mode, label) ->
                val selected = chosenMode == mode && chosenSymbolId == null
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clickable {
                            chosenSymbolId = null
                            chosenMode = mode
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

        Text("Escala: ${(scaleOverride * 100).roundToInt()}%", style = MaterialTheme.typography.labelMedium, color = PureWhite)
        Slider(
            value = scaleOverride,
            onValueChange = { scaleOverride = it },
            valueRange = 0.42f..0.85f,
            colors = SliderDefaults.colors(thumbColor = PureWhite, activeTrackColor = PureWhite)
        )

        Text("Espessura do Traço: ${"%.1f".format(strokeOverride)} dp", style = MaterialTheme.typography.labelMedium, color = PureWhite)
        Slider(
            value = strokeOverride,
            onValueChange = { strokeOverride = it },
            valueRange = 1.0f..4.0f,
            colors = SliderDefaults.colors(thumbColor = PureWhite, activeTrackColor = PureWhite)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.AutoFixHigh,
                contentDescription = null,
                tint = PureWhite,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Ou escolha um símbolo da Biblioteca Interna:",
                style = MaterialTheme.typography.titleSmall,
                color = PureWhite
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Category pills for library
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CategoryPill(
                label = "Todos",
                selected = selectedCategory == null,
                onClick = { selectedCategory = null }
            )
            MinimalCategory.entries.forEach { cat ->
                CategoryPill(
                    label = cat.labelPt,
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val symbols = remember(selectedCategory) {
            if (selectedCategory == null) MinimalIconLibrary.allSymbols
            else MinimalIconLibrary.allSymbols.filter { it.category == selectedCategory }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            symbols.forEach { spec ->
                val isSelected = chosenSymbolId == spec.id
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) GraphiteHighestCompat else PureBlack,
                    border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) PureWhite else GraphiteBorder),
                    modifier = Modifier.clickable { chosenSymbolId = spec.id }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        LiveSymbolIcon(
                            symbolId = spec.id,
                            theme = previewTheme,
                            size = 46.dp,
                            viewModel = viewModel,
                            contentDescription = spec.displayName
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = spec.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) PureWhite else SilverSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onPinShortcut,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, GraphiteBorder),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AddToHomeScreen,
                    contentDescription = "Fixar atalho",
                    tint = PureWhite,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Fixar Atalho", color = PureWhite)
            }

            Button(
                onClick = {
                    onApplyFix(chosenSymbolId, chosenMode, scaleOverride, strokeOverride)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PureWhite,
                    contentColor = PureBlack
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("save_manual_fix_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Aplicar Ajuste")
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

private val GraphiteHighestCompat = GraphiteElevated
