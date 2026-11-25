package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Launch
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ProcessingStatus
import com.example.engine.MinimalIconLibrary
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.components.LiveSymbolIcon
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusPill
import com.example.ui.components.formatTimestampPt
import com.example.ui.theme.GraphiteBorder
import com.example.ui.theme.GraphiteElevated
import com.example.ui.theme.GraphiteSurface
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SilverSecondary
import com.example.ui.theme.SyncEmerald
import com.example.ui.theme.WarningAmber

@Composable
fun HomeScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    onOpenEditor: () -> Unit,
    onOpenApps: () -> Unit,
    onOpenAutomation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTheme = uiState.activeTheme
    val identifiedCount = uiState.apps.size
    val processedCount = uiState.apps.count { !it.cachedIconPath.isNullOrBlank() }
    val libraryMatchesCount = uiState.apps.count {
        it.processingStatus == ProcessingStatus.PROCESSED_LIBRARY.name ||
            it.processingStatus == ProcessingStatus.MANUAL_OVERRIDE.name
    }

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
            // Brand Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AutoIcon Studio",
                            style = MaterialTheme.typography.headlineMedium,
                            color = PureWhite
                        )
                        Text(
                            text = "BLACK MINIMALIST ICON ENGINE",
                            style = MaterialTheme.typography.labelSmall,
                            color = SilverSecondary
                        )
                    }
                    StatusPill(
                        text = if (uiState.settings.autoDetectionEnabled) "Auto Ativo" else "Auto Pausado",
                        dotColor = if (uiState.settings.autoDetectionEnabled) SyncEmerald else WarningAmber
                    )
                }
            }

            // Hero Active Theme Card
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = GraphiteSurface,
                    border = BorderStroke(1.dp, GraphiteBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ESTILO ATIVO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SilverSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = activeTheme?.name ?: "Black Minimalist",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = PureWhite
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = activeTheme?.description
                                        ?: "Fundo #000000 • Símbolo #FFFFFF • Line Art",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SilverSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = PureBlack,
                                border = BorderStroke(1.dp, GraphiteBorder)
                            ) {
                                Text(
                                    text = activeTheme?.resolvedRenderMode()?.labelPt ?: "Line Art",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = PureWhite,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Showcase row of 6 real Black Minimalist icons
                        if (activeTheme != null) {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = PureBlack,
                                border = BorderStroke(1.dp, GraphiteBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 14.dp, horizontal = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    listOf(
                                        "whatsapp",
                                        "instagram",
                                        "youtube",
                                        "spotify",
                                        "chrome",
                                        "settings"
                                    ).forEach { symbolId ->
                                        LiveSymbolIcon(
                                            symbolId = symbolId,
                                            theme = activeTheme,
                                            size = 44.dp,
                                            viewModel = viewModel,
                                            contentDescription = symbolId
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onOpenEditor,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PureWhite,
                                    contentColor = PureBlack
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("open_editor_cta")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Tune,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Abrir Editor",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }

                            OutlinedButton(
                                onClick = { viewModel.refreshCatalog(forceReprocess = false) },
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, GraphiteBorder),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("sync_catalog_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Refresh,
                                    contentDescription = "Sincronizar aplicativos",
                                    tint = PureWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Metrics Grid (3 KPI cards)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        value = identifiedCount.toString(),
                        label = "Apps\nDetectados",
                        onClick = onOpenApps,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        value = processedCount.toString(),
                        label = "Ícones\nProcessados",
                        onClick = onOpenApps,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        value = "${MinimalIconLibrary.allSymbols.size}",
                        label = "Símbolos\nVetoriais ($libraryMatchesCount)",
                        onClick = onOpenApps,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Nova Launcher Integration Card
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = GraphiteSurface,
                    border = BorderStroke(1.dp, GraphiteBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SectionHeader(
                                title = "Integração Nova Launcher",
                                subtitle = if (uiState.novaStatus.isInstalled) {
                                    "Nova Launcher v${uiState.novaStatus.versionName ?: "?"} pronto"
                                } else {
                                    "Nova não detectado • Modo Standalone + Exportação"
                                },
                                modifier = Modifier.weight(1f)
                            )
                            StatusPill(
                                text = if (uiState.novaStatus.isInstalled) "Detectado" else "Pronto XML",
                                dotColor = if (uiState.novaStatus.isInstalled) SyncEmerald else PureWhite
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = GraphiteElevated,
                            border = BorderStroke(1.dp, GraphiteBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.AutoAwesome,
                                        contentDescription = null,
                                        tint = PureWhite,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Automatic App Icon Theming ativo (appfilter.xml)",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = PureWhite
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "O APK declara com.novalauncher.THEME com iconback (#000000), iconmask e iconupon para estilizar apps novos no Nova sem recompilar. Novos apps também são processados localmente em cache para prévia, exportação ZIP e atalhos na tela inicial.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SilverSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.applyToNovaLauncher() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PureWhite,
                                    contentColor = PureBlack
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("apply_nova_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Launch,
                                    contentDescription = null,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Aplicar no Nova",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }

                            OutlinedButton(
                                onClick = { viewModel.exportNovaBundleZip() },
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, GraphiteBorder),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("export_nova_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Download,
                                    contentDescription = "Exportar pacote Nova ZIP",
                                    tint = PureWhite,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Exportar",
                                    color = PureWhite,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }

            // Recent Detection Events Card
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = GraphiteSurface,
                    border = BorderStroke(1.dp, GraphiteBorder),
                    onClick = onOpenAutomation,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SectionHeader(
                                title = "Automação em Segundo Plano",
                                subtitle = "Última verificação: ${formatTimestampPt(uiState.settings.lastSyncTimestamp)}",
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Ver automação",
                                tint = SilverSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val recent = uiState.recentEvents.take(3)
                        if (recent.isEmpty()) {
                            Text(
                                text = "Monitorando instalações via BroadcastReceiver e WorkManager.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SilverSecondary
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                recent.forEach { ev ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.CheckCircle,
                                                contentDescription = null,
                                                tint = SyncEmerald,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "${ev.resolvedEventType().labelPt}: ${ev.appName}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = PureWhite,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text(
                                            text = ev.resultStatus,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SilverSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    value: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = GraphiteSurface,
        border = BorderStroke(1.dp, GraphiteBorder),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = PureWhite
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = SilverSecondary,
                minLines = 2
            )
        }
    }
}
