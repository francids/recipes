package com.francids.recipes.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import com.francids.recipes.ui.components.AppCard
import com.francids.recipes.ui.components.AppDivider
import com.francids.recipes.ui.components.BackTopBar
import com.francids.recipes.ui.components.SectionHeading
import com.francids.recipes.ui.components.SettingsRow
import com.francids.recipes.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
) {
    val palette = AppTheme.palette
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background),
    ) {
        BackTopBar(
            title = viewModel.tr("settings_screen.title"),
            onBack = onBack,
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SectionHeading(viewModel.tr("settings_screen.appearance_section"))
            }
            item {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        SettingsRow(
                            icon = if (viewModel.darkTheme) {
                                Icons.Outlined.DarkMode
                            } else {
                                Icons.Outlined.LightMode
                            },
                            title = viewModel.tr("settings_screen.dark_theme"),
                            supporting = viewModel.tr("settings_screen.dark_theme_description"),
                            enabled = !viewModel.followSystemTheme,
                            onClick = if (!viewModel.followSystemTheme) {
                                { viewModel.updateDarkTheme(!viewModel.darkTheme) }
                            } else {
                                null
                            },
                            trailing = {
                                AppSwitch(
                                    checked = viewModel.darkTheme,
                                    onCheckedChange = viewModel::updateDarkTheme,
                                    enabled = !viewModel.followSystemTheme,
                                )
                            },
                        )
                        AppDivider()
                        SettingsRow(
                            icon = Icons.Outlined.Contrast,
                            title = viewModel.tr("settings_screen.system_theme"),
                            supporting = viewModel.tr("settings_screen.system_theme_description"),
                            onClick = {
                                viewModel.updateFollowSystemTheme(!viewModel.followSystemTheme)
                            },
                            trailing = {
                                AppSwitch(
                                    checked = viewModel.followSystemTheme,
                                    onCheckedChange = viewModel::updateFollowSystemTheme,
                                )
                            },
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                SectionHeading(viewModel.tr("settings_screen.language_section"))
            }
            item {
                AppCard(modifier = Modifier.fillMaxWidth()) {
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
                Spacer(Modifier.height(16.dp))
                SectionHeading(viewModel.tr("profile_page.data_section"))
            }
            item {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
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
                                        animationAsset = "lottie/save_file.json",
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
                        AppDivider()
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
                                        animationAsset = "lottie/load_file.json",
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
            }

            item {
                Spacer(Modifier.height(16.dp))
                SectionHeading(viewModel.tr("settings_screen.about_section"))
            }
            item {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    SettingsRow(
                        icon = Icons.Outlined.Info,
                        title = viewModel.tr("settings_screen.version"),
                        supporting = BuildConfig.VERSION_NAME,
                    )
                }
            }
            item {
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (showLanguageSheet) {
        LanguageSheet(
            viewModel = viewModel,
            onDismiss = { showLanguageSheet = false },
        )
    }
}
