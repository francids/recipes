package com.francids.recipes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.francids.recipes.data.Messages
import com.francids.recipes.data.Recipe
import com.francids.recipes.data.ViewOption
import com.francids.recipes.ui.theme.AppTheme
import com.francids.recipes.ui.theme.DeepOrange
import java.io.File

private val CardShape = RoundedCornerShape(10.dp)
private val FieldShape = RoundedCornerShape(12.dp)

@Composable
fun RecipeImage(
    path: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val palette = AppTheme.palette
    if (path.isNullOrBlank()) {
        ImagePlaceholder(modifier)
        return
    }

    val context = LocalContext.current
    val model = if (path.startsWith("http://") || path.startsWith("https://")) {
        ImageRequest.Builder(context).data(path).crossfade(true).build()
    } else {
        ImageRequest.Builder(context).data(File(path)).crossfade(true).build()
    }

    SubcomposeAsyncImage(
        model = model,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier,
    ) {
        when (painter.state) {
            is AsyncImagePainter.State.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(palette.imagePlaceholder),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = palette.primary,
                    )
                }
            }

            is AsyncImagePainter.State.Error -> ImagePlaceholder(Modifier.fillMaxSize())
            else -> SubcomposeAsyncImageContent()
        }
    }
}

@Composable
private fun ImagePlaceholder(modifier: Modifier = Modifier) {
    val palette = AppTheme.palette
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.imagePlaceholder),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.ImageNotSupported,
            contentDescription = null,
            tint = palette.imagePlaceholderIcon,
        )
    }
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    elevation: Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    val palette = AppTheme.palette
    Box(
        modifier = modifier
            .then(
                if (elevation > 0.dp) {
                    Modifier.shadow(
                        elevation = elevation,
                        shape = CardShape,
                        ambientColor = palette.shadow,
                        spotColor = palette.shadow,
                    )
                } else {
                    Modifier
                },
            )
            .clip(CardShape)
            .background(palette.card)
            .border(0.5.dp, palette.border, CardShape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(role = Role.Button, onClick = onClick)
                } else {
                    Modifier
                },
            ),
    ) {
        content()
    }
}

@Composable
fun RecipeCard(
    recipe: Recipe,
    viewOption: ViewOption,
    onClick: () -> Unit,
) {
    if (viewOption == ViewOption.GRID) {
        GridRecipeCard(recipe, onClick)
    } else {
        ListRecipeCard(recipe, onClick)
    }
}

@Composable
private fun ListRecipeCard(recipe: Recipe, onClick: () -> Unit) {
    val palette = AppTheme.palette
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = CardShape,
                ambientColor = palette.shadow,
                spotColor = palette.shadow,
            )
            .clip(CardShape)
            .background(palette.surface)
            .border(0.5.dp, palette.border, CardShape)
            .clickable(onClick = onClick),
    ) {
        Row(modifier = Modifier.height(80.dp)) {
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight()
                    .clip(
                        RoundedCornerShape(
                            topStart = 10.dp,
                            bottomStart = 10.dp,
                        ),
                    ),
            ) {
                RecipeImage(
                    path = recipe.image,
                    contentDescription = recipe.title,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Box(
                modifier = Modifier
                    .width(0.5.dp)
                    .fillMaxHeight()
                    .background(palette.border),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.displayMedium,
                    color = palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = recipe.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textTertiary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun GridRecipeCard(recipe: Recipe, onClick: () -> Unit) {
    val palette = AppTheme.palette
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(
                elevation = 2.dp,
                shape = CardShape,
                ambientColor = palette.shadow,
                spotColor = palette.shadow,
            )
            .clip(CardShape)
            .background(palette.surface)
            .border(0.5.dp, palette.border, CardShape)
            .clickable(onClick = onClick),
    ) {
        RecipeImage(
            path = recipe.image,
            contentDescription = recipe.title,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = if (palette.isDark) 0.24f else 0.08f),
                            Color.Black.copy(alpha = if (palette.isDark) 0.47f else 0.15f),
                            Color.Black.copy(alpha = if (palette.isDark) 0.78f else 0.25f),
                        ),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.70f),
                            Color.Transparent,
                        ),
                    ),
                )
                .padding(8.dp),
        ) {
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = MaterialTheme.typography.displayMedium.fontSize,
                    fontWeight = FontWeight.Bold,
                ),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun RecipeContent(
    recipe: Recipe,
    language: String,
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 40.dp,
) {
    val palette = AppTheme.palette
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = bottomPadding),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(0.5.dp, palette.border, CardShape)
                .clip(CardShape)
                .background(palette.surface),
        ) {
            RecipeImage(
                path = recipe.image,
                contentDescription = recipe.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
            )
        }

        Spacer(Modifier.height(24.dp))
        Text(
            text = recipe.title,
            style = MaterialTheme.typography.displayLarge,
            color = palette.textPrimary,
        )

        if (recipe.preparationTime > 0) {
            Spacer(Modifier.height(8.dp))
            AppChip(
                text = formatPreparationTime(recipe.preparationTime, language),
                leadingIcon = Icons.Outlined.Schedule,
            )
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = recipe.description,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.textSecondary,
        )

        SectionDivider()
        SectionTitle(Messages.translate(language, "recipe_screen.ingredients_title"))
        Spacer(Modifier.height(12.dp))
        recipe.ingredients.forEach { ingredient ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(palette.textTertiary),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = ingredient,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textSecondary,
                )
            }
        }

        SectionDivider()
        SectionTitle(Messages.translate(language, "recipe_screen.instructions_title"))
        Spacer(Modifier.height(12.dp))
        recipe.directions.forEachIndexed { index, direction ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CardShape)
                        .background(palette.chipBackground)
                        .border(0.5.dp, palette.textTertiary, CardShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = (index + 1).toString(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                        ),
                        color = palette.textTertiary,
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = direction,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textSecondary,
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 2.dp),
                )
            }
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    val palette = AppTheme.palette
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(20.dp)
                .clip(CardShape)
                .background(palette.primary),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.displayMedium,
            color = palette.textPrimary,
        )
    }
}

@Composable
private fun SectionDivider() {
    val palette = AppTheme.palette
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp)
            .height(0.5.dp)
            .background(palette.divider),
    )
}

@Composable
fun AppChip(
    text: String,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    leadingIcon: ImageVector? = null,
) {
    val palette = AppTheme.palette
    Row(
        modifier = Modifier
            .clip(CardShape)
            .background(if (selected) palette.chipSelected else palette.chipBackground)
            .border(0.5.dp, palette.border, CardShape)
            .then(
                if (onClick != null) {
                    Modifier.selectable(
                        selected = selected,
                        role = Role.Checkbox,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = palette.textTertiary,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = palette.textTertiary,
        )
    }
}

@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    suffix: (@Composable () -> Unit)? = null,
) {
    val palette = AppTheme.palette
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        if (!label.isNullOrBlank()) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = palette.textPrimary,
                modifier = Modifier.padding(start = 2.dp, bottom = 6.dp),
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = hint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textPrimary.copy(alpha = 0.19f),
                )
            },
            textStyle = MaterialTheme.typography.bodyMedium,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = singleLine,
            minLines = minLines,
            shape = FieldShape,
            trailingIcon = suffix,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = palette.textPrimary,
                unfocusedTextColor = palette.textPrimary,
                cursorColor = palette.primary,
                focusedContainerColor = palette.surface,
                unfocusedContainerColor = palette.surface,
                disabledContainerColor = palette.surface,
                focusedBorderColor = palette.primary,
                unfocusedBorderColor = palette.border,
                disabledBorderColor = palette.border,
            ),
        )
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    supporting: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    showChevron: Boolean = false,
    enabled: Boolean = true,
) {
    val palette = AppTheme.palette
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null && enabled) {
                    Modifier.clickable(role = Role.Button, onClick = onClick)
                } else {
                    Modifier
                },
            )
            .heightIn(min = 64.dp)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = palette.primary,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) palette.textPrimary else palette.textTertiary,
            )
            if (!supporting.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textTertiary,
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        } else if (showChevron) {
            Spacer(Modifier.width(12.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = palette.textTertiary,
            )
        }
    }
}

@Composable
fun SectionHeading(text: String) {
    val palette = AppTheme.palette
    Text(
        text = text,
        style = MaterialTheme.typography.displayMedium,
        color = palette.textPrimary,
        modifier = Modifier.padding(start = 2.dp),
    )
}

@Composable
fun AppDivider() {
    val palette = AppTheme.palette
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(palette.divider),
    )
}

@Composable
fun EmptyState(
    text: String,
    modifier: Modifier = Modifier,
) {
    val palette = AppTheme.palette
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}


@Composable
fun PopupMenuRow(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
) {
    val palette = AppTheme.palette
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = palette.textPrimary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = palette.textPrimary,
        )
    }
}

@Composable
fun PrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = CardShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = DeepOrange,
            contentColor = Color.White,
            disabledContainerColor = DeepOrange.copy(alpha = 0.35f),
            disabledContentColor = Color.White.copy(alpha = 0.8f),
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
        content = content,
    )
}

@Composable
fun SecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    val palette = AppTheme.palette
    TextButton(
        onClick = onClick,
        modifier = modifier,
        shape = CardShape,
        colors = ButtonDefaults.textButtonColors(
            containerColor = if (palette.isDark) Color(0xFF424242) else Color(0xFFEEEEEE),
            contentColor = if (palette.isDark) Color.White else Color(0xDD000000),
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
        content = content,
    )
}

@Composable
fun OutlinedAppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = CardShape,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, DeepOrange),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepOrange),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
        content = content,
    )
}

fun formatPreparationTime(seconds: Int, language: String): String {
    if (seconds <= 0) return ""
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60
    val labels = when (language.substringBefore('-')) {
        "de" -> Triple("Std", "Min", "Sek")
        "ja" -> Triple("時間", "分", "秒")
        "ko" -> Triple("시간", "분", "초")
        "zh" -> Triple("小时", "分钟", "秒")
        else -> Triple("h", "m", "s")
    }

    return buildList {
        if (hours > 0) add("$hours${labels.first}")
        if (minutes > 0) add("$minutes${labels.second}")
        if (remainingSeconds > 0 && hours == 0) add("$remainingSeconds${labels.third}")
    }.joinToString(" ")
}
