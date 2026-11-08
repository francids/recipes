@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.francids.recipes.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.francids.recipes.BuildConfig
import com.francids.recipes.data.ConfirmRequest
import com.francids.recipes.ui.AppViewModel
import com.francids.recipes.ui.components.SettingsRow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
) {
    var showLanguageSheet by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip"),
    ) { uri ->
        if (uri != null) viewModel.exportRecipes(uri)
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) viewModel.importRecipes(uri)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BackTopBar(
                title = viewModel.tr("settings_screen.title"),
                onBack = onBack,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SectionHeader(viewModel.tr("settings_screen.appearance_section"))
            }
            item {
                SettingsCard {
                    SettingsRow(
                        icon = if (viewModel.darkTheme) {
                            Icons.Outlined.DarkMode
                        } else {
                            Icons.Outlined.LightMode
                        },
                        title = viewModel.tr("settings_screen.dark_theme"),
                        supporting = viewModel.tr("settings_screen.dark_theme_description"),
                        enabled = !viewModel.followSystemTheme,
                        trailing = {
                            Switch(
                                checked = viewModel.darkTheme,
                                enabled = !viewModel.followSystemTheme,
                                onCheckedChange = viewModel::updateDarkTheme,
                            )
                        },
                        iconContainerColor = if (viewModel.followSystemTheme) {
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        },
                        iconContentColor = if (viewModel.followSystemTheme) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        },
                    )
                    HorizontalCardDivider()
                    SettingsRow(
                        icon = Icons.Outlined.Contrast,
                        title = viewModel.tr("settings_screen.system_theme"),
                        supporting = viewModel.tr("settings_screen.system_theme_description"),
                        trailing = {
                            Switch(
                                checked = viewModel.followSystemTheme,
                                onCheckedChange = viewModel::updateFollowSystemTheme,
                            )
                        },
                        iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        iconContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                SectionHeader(viewModel.tr("settings_screen.language_section"))
            }
            item {
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Outlined.Language,
                        title = viewModel.tr("settings_screen.language"),
                        supporting = viewModel.tr("settings_screen.language_description"),
                        onClick = { showLanguageSheet = true },
                        showChevron = true,
                    )
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                SectionHeader(viewModel.tr("profile_page.data_section"))
            }
            item {
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Outlined.Download,
                        title = viewModel.tr("profile_page.export_recipes"),
                        supporting = viewModel.tr("profile_page.export_recipes_description"),
                        onClick = {
                            viewModel.showConfirmation(
                                ConfirmRequest(
                                    title = viewModel.tr("profile_page.export_confirmation_title"),
                                    message = viewModel.tr("profile_page.export_confirmation_message"),
                                    confirmLabel = viewModel.tr("ui_helpers.confirm"),
                                    cancelLabel = viewModel.tr("ui_helpers.cancel"),
                                    onConfirm = {
                                        val stamp = SimpleDateFormat(
                                            "yyyyMMdd_HHmmss",
                                            Locale.US,
                                        ).format(Date())
                                        exportLauncher.launch("recipes_export_$stamp.zip")
                                    },
                                ),
                            )
                        },
                        showChevron = true,
                    )
                    HorizontalCardDivider()
                    SettingsRow(
                        icon = Icons.Outlined.Upload,
                        title = viewModel.tr("profile_page.import_recipes"),
                        supporting = viewModel.tr("profile_page.import_recipes_description"),
                        onClick = {
                            viewModel.showConfirmation(
                                ConfirmRequest(
                                    title = viewModel.tr("profile_page.import_confirmation_title"),
                                    message = viewModel.tr("profile_page.import_confirmation_message"),
                                    confirmLabel = viewModel.tr("ui_helpers.confirm"),
                                    cancelLabel = viewModel.tr("ui_helpers.cancel"),
                                    onConfirm = {
                                        importLauncher.launch(
                                            arrayOf(
                                                "application/zip",
                                                "application/octet-stream",
                                                "*/*",
                                            ),
                                        )
                                    },
                                ),
                            )
                        },
                        showChevron = true,
                    )
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                SectionHeader(viewModel.tr("settings_screen.about_section"))
            }
            item {
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Outlined.Info,
                        title = viewModel.tr("settings_screen.version"),
                        supporting = BuildConfig.VERSION_NAME,
                    )
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    if (showLanguageSheet) {
        LanguageSheet(
            viewModel = viewModel,
            onDismiss = { showLanguageSheet = false },
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLargeEmphasized,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp),
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column { content() }
    }
}
