@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.francids.recipes.ui.screens

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.francids.recipes.R
import com.francids.recipes.ui.AppViewModel
import com.francids.recipes.ui.components.AppCard
import com.francids.recipes.ui.components.AppDivider
import com.francids.recipes.ui.components.SettingsRow
import com.francids.recipes.ui.theme.AppTheme
import com.francids.recipes.ui.theme.DeepOrange
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

@Composable
fun LanguageSheet(
    viewModel: AppViewModel,
    onDismiss: () -> Unit,
) {
    val palette = AppTheme.palette
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.surface,
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
                color = palette.textPrimary,
                modifier = Modifier.padding(start = 2.dp, bottom = 16.dp),
            )

            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    languages.forEachIndexed { index, language ->
                        val selected = viewModel.language == language.code
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selected,
                                    role = Role.RadioButton,
                                    onClick = {
                                        viewModel.changeLanguage(language.code)
                                        onDismiss()
                                    },
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = selected,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = DeepOrange,
                                    unselectedColor = palette.textTertiary,
                                ),
                            )
                            Text(
                                text = flagEmoji(language.flagCode),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(horizontal = 8.dp),
                            )
                            Text(
                                text = language.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                                color = palette.textPrimary,
                            )
                        }
                        if (index < languages.lastIndex) {
                            AppDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShareRecipeSheet(
    viewModel: AppViewModel,
    recipeId: String,
    onDismiss: () -> Unit,
) {
    val palette = AppTheme.palette
    var showQr by remember { mutableStateOf(false) }
    val isPublic = viewModel.isRecipePublic(recipeId)
    val shareUrl = remember(recipeId, viewModel.recipes) { viewModel.generateShareUrl(recipeId) }
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.surface,
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
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = null,
                            tint = palette.textPrimary,
                        )
                    }
                    Text(
                        text = viewModel.tr("recipe_screen.qr_code"),
                        style = MaterialTheme.typography.displayMedium,
                        color = palette.textPrimary,
                    )
                }
                Text(
                    text = viewModel.tr("recipe_screen.qr_code_description"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textSecondary,
                    modifier = Modifier.padding(start = 2.dp),
                )
                Spacer(Modifier.height(16.dp))
                QrCode(
                    url = shareUrl,
                    contentDescription = viewModel.tr("recipe_screen.qr_code"),
                )
                return@Column
            }

            Text(
                text = viewModel.tr("recipe_screen.share_recipe"),
                style = MaterialTheme.typography.displayMedium,
                color = palette.textPrimary,
                modifier = Modifier.padding(start = 2.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (isPublic) {
                    viewModel.tr("recipe_screen.recipe_already_public")
                } else {
                    viewModel.tr("recipe_screen.share_description")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary,
                modifier = Modifier.padding(start = 2.dp),
            )
            Spacer(Modifier.height(16.dp))

            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    SettingsRow(
                        icon = if (isPublic) Icons.Outlined.Public else Icons.Outlined.Lock,
                        title = viewModel.tr("recipe_screen.make_public"),
                        supporting = if (isPublic) {
                            viewModel.tr("recipe_screen.make_private_description")
                        } else {
                            viewModel.tr("recipe_screen.make_public_description")
                        },
                        onClick = if (!viewModel.isShareLoading(recipeId)) {
                            { viewModel.setRecipePublic(recipeId, !isPublic) }
                        } else {
                            null
                        },
                        trailing = {
                            AppSwitch(
                                checked = isPublic,
                                enabled = !viewModel.isShareLoading(recipeId),
                                onCheckedChange = {
                                    viewModel.setRecipePublic(recipeId, it)
                                },
                            )
                        },
                    )

                    if (isPublic) {
                        AppDivider()
                        SettingsRow(
                            icon = Icons.Outlined.QrCode2,
                            title = viewModel.tr("recipe_screen.generate_qr"),
                            supporting = viewModel.tr("recipe_screen.generate_qr_description"),
                            onClick = { showQr = true },
                            showChevron = true,
                        )
                        AppDivider()
                        SettingsRow(
                            icon = Icons.Outlined.Share,
                            title = viewModel.tr("recipe_screen.share"),
                            supporting = viewModel.tr("recipe_screen.share_link_description"),
                            onClick = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareUrl)
                                }
                                context.startActivity(Intent.createChooser(intent, null))
                            },
                            showChevron = true,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QrCode(url: String, contentDescription: String) {
    val palette = AppTheme.palette
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

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (bitmap != null) {
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.surface)
                        .border(1.dp, palette.border, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = contentDescription,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Image(
                        painter = painterResource(R.drawable.recipe_logo),
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White, RoundedCornerShape(6.dp))
                            .padding(3.dp),
                    )
                }
            } else {
                Text(
                    text = contentDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textSecondary,
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
