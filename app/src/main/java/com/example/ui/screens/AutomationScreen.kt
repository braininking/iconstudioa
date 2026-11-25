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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Launch
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.DetectionEventEntity
import com.example.data.model.DetectionEventType
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
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
fun AutomationScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val newlyDetectedCount = uiState.apps.count { it.isNewlyDetected }

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
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Automação & Nova Sync",
                            style = MaterialTheme.typography.headlineSmall,
                            color = PureWhite
                        )
                        Text(
                            text = "Detecção silenciosa de pacotes (Android 10–16)",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverSecondary
                        )
                    }
                    StatusPill(
                        text = if (uiState.settings.autoDetectionEnabled) "Ativo" else "Desativado",
                        dotColor = if (uiState.settings.autoDetectionEnabled) SyncEmerald else WarningAmber
                    )
                }
            }

            // Automation Toggles Card
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = GraphiteSurface,
                    border = BorderStroke(1.dp, GraphiteBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        AutomationSwitchRow(
                            title = "Detecção automática de pacotes",
                            subtitle = "Monitora ACTION_PACKAGE_ADDED, REPLACED e REMOVED + WorkManager periódico.",
                            checked = uiState.settings.autoDetectionEnabled,
                            onCheckedChange = { viewModel.setAutoDetectionEnabled(it) },
                            tag = "toggle_auto_detection"
                        )

                        AutomationSwitchRow(
                            title = "Processamento imediato ao instalar",
                            subtitle = "Converte automaticamente o ícone de novos apps para Black Minimalist sem confirmação.",
                            checked = uiState.settings.autoProcessOnInstall,
                            onCheckedChange = { viewModel.setAutoProcessOnInstall(it) },
                            tag = "toggle_auto_process"
                        )

                        AutomationSwitchRow(
                            title = "Atualizar exportação Nova Launcher",
                            subtitle = "Regera appfilter.xml, drawable.xml e camadas após cada detecção.",
                            checked = uiState.settings.novaAutoSync,
                            onCheckedChange = { viewModel.setNovaAutoSync(it) },
                            tag = "toggle_nova_sync"
                        )
                    }
                }
            }

            // Sync Status & Manual Scan Trigger
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = GraphiteSurface,
                    border = BorderStroke(1.dp, GraphiteBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        SectionHeader(
                            title = "Estado da Sincronização",
                            subtitle = "Última verificação: ${formatTimestampPt(uiState.settings.lastSyncTimestamp)}"
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MiniStatBox(
                                label = "Novos Detectados",
                                value = newlyDetectedCount.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            MiniStatBox(
                                label = "Mapeados Biblioteca",
                                value = uiState.novaStatus.mappedLibraryCount.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            MiniStatBox(
                                label = "Cache PNG",
                                value = uiState.novaStatus.totalExportedCount.toString(),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.refreshCatalog(forceReprocess = false) },
                                enabled = !uiState.isSyncing,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PureWhite,
                                    contentColor = PureBlack
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("sync_now_button")
                            ) {
                                if (uiState.isSyncing) {
                                    CircularProgressIndicator(
                                        color = PureBlack,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.Sync,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Verificar Agora")
                            }

                            OutlinedButton(
                                onClick = { viewModel.applyToNovaLauncher() },
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, GraphiteBorder),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Launch,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Nova", color = PureWhite)
                            }
                        }
                    }
                }
            }

            // Generated appfilter.xml preview + ZIP export
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
                                title = "Nova Theme (appfilter.xml)",
                                subtitle = "Camadas iconback, iconmask, iconupon e mapeamentos",
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedButton(
                                onClick = { viewModel.exportNovaBundleZip() },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, GraphiteBorder)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Download,
                                    contentDescription = "Exportar ZIP",
                                    tint = PureWhite,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ZIP", color = PureWhite)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = PureBlack,
                            border = BorderStroke(1.dp, GraphiteBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = uiState.appFilterPreviewXml,
                                style = MaterialTheme.typography.labelSmall,
                                color = SilverSecondary,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            // Detection Event Log
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(
                        title = "Histórico de Detecção",
                        subtitle = "Eventos capturados automaticamente",
                        modifier = Modifier.weight(1f)
                    )
                    if (uiState.recentEvents.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearDetectionLogs() }) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteSweep,
                                contentDescription = "Limpar histórico",
                                tint = SilverSecondary
                            )
                        }
                    }
                }
            }

            if (uiState.recentEvents.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = GraphiteSurface,
                        border = BorderStroke(1.dp, GraphiteBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Nenhum evento recente registrado ainda. Ao instalar ou atualizar qualquer aplicativo no dispositivo, ele aparecerá aqui automaticamente.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverSecondary,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(uiState.recentEvents, key = { it.id }) { event ->
                    DetectionEventRow(event)
                }
            }
        }
    }
}

@Composable
private fun AutomationSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = PureWhite)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = SilverSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PureBlack,
                checkedTrackColor = PureWhite,
                uncheckedThumbColor = SilverSecondary,
                uncheckedTrackColor = GraphiteElevated,
                uncheckedBorderColor = GraphiteBorder
            ),
            modifier = Modifier.testTag(tag)
        )
    }
}

@Composable
private fun MiniStatBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = GraphiteElevated,
        border = BorderStroke(1.dp, GraphiteBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = value, style = MaterialTheme.typography.titleLarge, color = PureWhite)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = SilverSecondary)
        }
    }
}

@Composable
private fun DetectionEventRow(event: DetectionEventEntity) {
    val evType = event.resolvedEventType()
    val dotColor = when (evType) {
        DetectionEventType.ADDED, DetectionEventType.REPLACED -> SyncEmerald
        DetectionEventType.REMOVED -> WarningAmber
        DetectionEventType.BATCH_SYNC -> PureWhite
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = GraphiteSurface,
        border = BorderStroke(1.dp, GraphiteBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = event.appName,
                    style = MaterialTheme.typography.titleSmall,
                    color = PureWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${event.packageName} • ${formatTimestampPt(event.timestamp)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = SilverSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (event.processingTimeMs > 0) {
                    Text(
                        text = "${event.resultStatus} (${event.processingTimeMs} ms)",
                        style = MaterialTheme.typography.labelSmall,
                        color = SilverSecondary
                    )
                }
            }
            StatusPill(text = evType.labelPt, dotColor = dotColor)
        }
    }
}
