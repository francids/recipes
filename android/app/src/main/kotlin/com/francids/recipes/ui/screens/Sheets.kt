package com.francids.recipes.ui.screens

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.francids.recipes.data.Messages
import com.francids.recipes.ui.AppViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

private data class AppLanguage(val code: String, val name: String, val flagCode: String)

private val languages = listOf(
    AppLanguage("es", "Español", "ES"),
    AppLanguage("en", "English", "GB"),
    AppLanguage("de", "Deutsch", "DE"),
    AppLanguage("it", "Italiano", "IT"),
    AppLanguage("fr", "Français", "FR"),
    AppLanguage("pt", "Português", "PT"),
    AppLanguage("zh", "中文", "CN"),
    AppLanguage("ja", "日本語", "JP"),
    AppLanguage("ko", "한국어", "KR"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSheet(
    viewModel: AppViewModel,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
        ) {
            Text(
                text = viewModel.tr("settings_screen.language"),
                style = MaterialTheme.typography.displayMedium,
            )
            Spacer(Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column {
                    languages.forEachIndexed { index, language ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.changeLanguage(language.code)
                                    onDismiss()
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = viewModel.language == language.code,
                                onClick = {
                                    viewModel.changeLanguage(language.code)
                                    onDismiss()
                                },
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = flagEmoji(language.flagCode),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(language.name, style = MaterialTheme.typography.bodyLarge)
                        }
                        if (index < languages.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(viewModel.tr("ui_helpers.cancel"))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareRecipeSheet(
    viewModel: AppViewModel,
    recipeId: String,
    onDismiss: () -> Unit,
) {
    var showQr by remember { mutableStateOf(false) }
    val isPublic = viewModel.isRecipePublic(recipeId)
    val shareUrl = remember(recipeId, viewModel.recipes) { viewModel.generateShareUrl(recipeId) }
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
        ) {
            if (showQr) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showQr = false }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                    Text(
                        text = viewModel.tr("recipe_screen.qr_code"),
                        style = MaterialTheme.typography.displayMedium,
                    )
                }
                Text(
                    text = viewModel.tr("recipe_screen.qr_code_description"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                QrCode(url = shareUrl)
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text(viewModel.tr("ui_helpers.cancel"))
                }
                return@Column
            }

            Text(
                text = viewModel.tr("recipe_screen.share_recipe"),
                style = MaterialTheme.typography.displayMedium,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (isPublic) {
                    viewModel.tr("recipe_screen.recipe_already_public")
                } else {
                    viewModel.tr("recipe_screen.share_description")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column {
                    ListItem(
                        headlineContent = {
                            Text(
                                viewModel.tr("recipe_screen.make_public"),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        },
                        supportingContent = {
                            Text(
                                if (isPublic) {
                                    viewModel.tr("recipe_screen.make_private_description")
                                } else {
                                    viewModel.tr("recipe_screen.make_public_description")
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = if (isPublic) Icons.Outlined.Public
                                else Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        },
                        trailingContent = {
                            Switch(
                                checked = isPublic,
                                enabled = !viewModel.isShareLoading(recipeId),
                                onCheckedChange = {
                                    viewModel.setRecipePublic(recipeId, it)
                                },
                            )
                        },
                    )

                    if (isPublic) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        )
                        ListItem(
                            modifier = Modifier.clickable { showQr = true },
                            headlineContent = {
                                Text(
                                    viewModel.tr("recipe_screen.generate_qr"),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            },
                            supportingContent = {
                                Text(
                                    viewModel.tr("recipe_screen.generate_qr_description"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            leadingContent = {
                                Icon(
                                    Icons.Outlined.QrCode2,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            },
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        )
                        ListItem(
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareUrl)
                                }
                                context.startActivity(
                                    Intent.createChooser(intent, null),
                                )
                            },
                            headlineContent = {
                                Text(
                                    viewModel.tr("recipe_screen.share"),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            },
                            supportingContent = {
                                Text(
                                    viewModel.tr("recipe_screen.share_link_description"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            leadingContent = {
                                Icon(
                                    Icons.Outlined.Share,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(viewModel.tr("ui_helpers.cancel"))
            }
        }
    }
}

@Composable
private fun QrCode(url: String) {
    val context = LocalContext.current
    val bitmap = remember(url) { generateQrBitmap(url, 900) }

    DisposableEffect(Unit) {
        val activity = context.findActivity()
        val window = activity?.window
        val original = window?.attributes?.screenBrightness
        window?.let {
            val attributes = it.attributes
            attributes.screenBrightness = 1f
            it.attributes = attributes
        }
        onDispose {
            window?.let {
                val attributes = it.attributes
                attributes.screenBrightness = original ?: -1f
                it.attributes = attributes
            }
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = Messages.translate("en", "recipe_screen.qr_code"),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(260.dp)
                        .background(androidx.compose.ui.graphics.Color.White)
                        .padding(8.dp),
                )
            } else {
                Text(
                    text = Messages.translate("en", "ui_helpers.error"),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

private fun generateQrBitmap(content: String, size: Int): Bitmap? {
    return runCatching {
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
            EncodeHintType.MARGIN to 1,
        )
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        val width = matrix.width
        val height = matrix.height
        val pixels = IntArray(width * height)
        val foreground = AndroidColor.rgb(234, 88, 12)
        val background = AndroidColor.WHITE
        for (y in 0 until height) {
            for (x in 0 until width) {
                pixels[y * width + x] = if (matrix[x, y]) foreground else background
            }
        }
        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, width, 0, 0, width, height)
        }
    }.getOrNull()
}

private fun flagEmoji(countryCode: String): String {
    val first = Character.toChars(countryCode[0].code - 0x41 + 0x1F1E6)
    val second = Character.toChars(countryCode[1].code - 0x41 + 0x1F1E6)
    return String(first) + String(second)
}

private fun Context.findActivity(): android.app.Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is android.app.Activity) return context
        context = context.baseContext
    }
    return null
}
