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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.IconThemeEntity
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.components.LiveSymbolIcon
import com.example.ui.components.StatusPill
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.GraphiteBorder
import com.example.ui.theme.GraphiteElevated
import com.example.ui.theme.GraphiteSurface
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SilverSecondary
import com.example.ui.theme.SyncEmerald
import kotlin.math.roundToInt

@Composable
fun ThemesScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    onEditTheme: (IconThemeEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Temas Black Minimalist",
                            style = MaterialTheme.typography.headlineSmall,
                            color = PureWhite
                        )
                        Text(
                            text = "${uiState.themes.size} estilos salvos localmente",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverSecondary
                        )
                    }
                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PureWhite,
                            contentColor = PureBlack
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("create_theme_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Novo Tema")
                    }
                }
            }

            items(uiState.themes, key = { it.id }) { theme ->
                ThemeCard(
                    theme = theme,
                    viewModel = viewModel,
                    onActivate = { viewModel.activateTheme(theme) },
                    onEdit = {
                        viewModel.loadThemeIntoEditor(theme)
                        onEditTheme(theme)
                    },
                    onDuplicate = { viewModel.duplicateTheme(theme) },
                    onDelete = { viewModel.deleteTheme(theme) }
                )
            }
        }
    }

    if (showCreateDialog) {
        CreateThemeDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, desc ->
                viewModel.createNewTheme(name, desc)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun ThemeCard(
    theme: IconThemeEntity,
    viewModel: MainViewModel,
    onActivate: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = GraphiteSurface,
        border = BorderStroke(
            width = if (theme.isActive) 1.5.dp else 1.dp,
            color = if (theme.isActive) PureWhite else GraphiteBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("theme_card_${theme.id}")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = theme.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = PureWhite
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = theme.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverSecondary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                if (theme.isActive) {
                    StatusPill(text = "Ativo", dotColor = SyncEmerald)
                } else if (theme.isPreset) {
                    StatusPill(text = "Modelo", dotColor = SilverSecondary)
                } else {
                    StatusPill(text = "Autoral", dotColor = PureWhite)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5-icon miniature strip rendered live with this theme's exact parameters
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = PureBlack,
                border = BorderStroke(1.dp, GraphiteBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("whatsapp", "instagram", "spotify", "chrome", "camera").forEach { id ->
                        LiveSymbolIcon(
                            symbolId = id,
                            theme = theme,
                            size = 42.dp,
                            viewModel = viewModel,
                            contentDescription = id
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${theme.resolvedRenderMode().labelPt} • Traço ${"%.1f".format(theme.strokeWidthDp)}dp • Cantos ${theme.cornerRadiusPercent.roundToInt()}% • Escala ${(theme.symbolScale * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = SilverSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (theme.isActive) {
                    OutlinedButton(
                        onClick = onEdit,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GraphiteBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Editar Estilo", color = PureWhite)
                    }
                } else {
                    Button(
                        onClick = onActivate,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PureWhite,
                            contentColor = PureBlack
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Aplicar")
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Editar",
                            tint = PureWhite
                        )
                    }
                }

                IconButton(
                    onClick = onDuplicate,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "Duplicar",
                        tint = SilverSecondary
                    )
                }

                if (!theme.isPreset) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Excluir",
                            tint = ErrorCrimson
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateThemeDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GraphiteSurface,
        titleContentColor = PureWhite,
        textContentColor = SilverSecondary,
        title = { Text("Novo Tema Black Minimalist") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Cria um estilo autoral baseado na sua configuração atual para editar livremente.",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do tema") },
                    placeholder = { Text("Ex: Studio Minimal Pro") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PureWhite,
                        unfocusedBorderColor = GraphiteBorder,
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        focusedContainerColor = GraphiteElevated,
                        unfocusedContainerColor = GraphiteElevated
                    )
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição (opcional)") },
                    placeholder = { Text("Traço fino em fundo preto #000000") },
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PureWhite,
                        unfocusedBorderColor = GraphiteBorder,
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        focusedContainerColor = GraphiteElevated,
                        unfocusedContainerColor = GraphiteElevated
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(name, description) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = PureBlack)
            ) {
                Text("Criar Tema")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = SilverSecondary)
            }
        }
    )
}
