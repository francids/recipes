@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.francids.recipes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.francids.recipes.R

/**
 * Material 3 Expressive palette generated from the Recipes brand orange (#FF5722)
 * with the official material-color-utilities SchemeFidelity algorithm.
 */
private val BrandOrange = Color(0xFFFF5722)

private val LightColors = lightColorScheme(
    primary = Color(0xFFB02F00),
    onPrimary = Color.White,
    primaryContainer = BrandOrange,
    onPrimaryContainer = Color(0xFF541200),
    inversePrimary = Color(0xFFFFB5A0),
    secondary = Color(0xFF9F4125),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFC8866),
    onSecondaryContainer = Color(0xFF722107),
    tertiary = Color(0xFF00628C),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF007CAF),
    onTertiaryContainer = Color(0xFFFCFCFF),
    background = Color(0xFFFFF8F6),
    onBackground = Color(0xFF271813),
    surface = Color(0xFFFFF8F6),
    onSurface = Color(0xFF271813),
    surfaceVariant = Color(0xFFFFDBD1),
    onSurfaceVariant = Color(0xFF5B4039),
    surfaceTint = Color(0xFFB02F00),
    inverseSurface = Color(0xFF3E2C27),
    inverseOnSurface = Color(0xFFFFEDE8),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    outline = Color(0xFF907067),
    outlineVariant = Color(0xFFE4BEB4),
    scrim = Color.Black,
    surfaceBright = Color(0xFFFFF8F6),
    surfaceContainer = Color(0xFFFFE9E4),
    surfaceContainerHigh = Color(0xFFFFE2DA),
    surfaceContainerHighest = Color(0xFFFADCD4),
    surfaceContainerLow = Color(0xFFFFF1ED),
    surfaceContainerLowest = Color.White,
    surfaceDim = Color(0xFFF1D4CC),
    primaryFixed = Color(0xFFFFDBD1),
    primaryFixedDim = Color(0xFFFFB5A0),
    onPrimaryFixed = Color(0xFF3B0900),
    onPrimaryFixedVariant = Color(0xFF862200),
    secondaryFixed = Color(0xFFFFDBD1),
    secondaryFixedDim = Color(0xFFFFB5A0),
    onSecondaryFixed = Color(0xFF3B0900),
    onSecondaryFixedVariant = Color(0xFF7F2A10),
    tertiaryFixed = Color(0xFFC8E6FF),
    tertiaryFixedDim = Color(0xFF86CFFF),
    onTertiaryFixed = Color(0xFF001E2E),
    onTertiaryFixedVariant = Color(0xFF004C6D),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB5A0),
    onPrimary = Color(0xFF5F1500),
    primaryContainer = BrandOrange,
    onPrimaryContainer = Color(0xFF541200),
    inversePrimary = Color(0xFFB02F00),
    secondary = Color(0xFFFFB5A0),
    onSecondary = Color(0xFF5F1500),
    secondaryContainer = Color(0xFF7F2A10),
    onSecondaryContainer = Color(0xFFFF9B7E),
    tertiary = Color(0xFF86CFFF),
    onTertiary = Color(0xFF00344C),
    tertiaryContainer = Color(0xFF019AD8),
    onTertiaryContainer = Color(0xFF002D42),
    background = Color(0xFF1E100C),
    onBackground = Color(0xFFFADCD4),
    surface = Color(0xFF1E100C),
    onSurface = Color(0xFFFADCD4),
    surfaceVariant = Color(0xFF5B4039),
    onSurfaceVariant = Color(0xFFE4BEB4),
    surfaceTint = Color(0xFFFFB5A0),
    inverseSurface = Color(0xFFFADCD4),
    inverseOnSurface = Color(0xFF3E2C27),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFFAB8980),
    outlineVariant = Color(0xFF5B4039),
    scrim = Color.Black,
    surfaceBright = Color(0xFF483530),
    surfaceContainer = Color(0xFF2C1C17),
    surfaceContainerHigh = Color(0xFF372621),
    surfaceContainerHighest = Color(0xFF43302B),
    surfaceContainerLow = Color(0xFF271813),
    surfaceContainerLowest = Color(0xFF180B07),
    surfaceDim = Color(0xFF1E100C),
    primaryFixed = Color(0xFFFFDBD1),
    primaryFixedDim = Color(0xFFFFB5A0),
    onPrimaryFixed = Color(0xFF3B0900),
    onPrimaryFixedVariant = Color(0xFF862200),
    secondaryFixed = Color(0xFFFFDBD1),
    secondaryFixedDim = Color(0xFFFFB5A0),
    onSecondaryFixed = Color(0xFF3B0900),
    onSecondaryFixedVariant = Color(0xFF7F2A10),
    tertiaryFixed = Color(0xFFC8E6FF),
    tertiaryFixedDim = Color(0xFF86CFFF),
    onTertiaryFixed = Color(0xFF001E2E),
    onTertiaryFixedVariant = Color(0xFF004C6D),
)

private val Montserrat = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold),
)

private fun textStyle(
    size: Int,
    lineHeight: Int,
    weight: FontWeight,
    letterSpacing: Double = 0.0,
) = TextStyle(
    fontFamily = Montserrat,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    fontWeight = weight,
    letterSpacing = letterSpacing.sp,
)

private fun emphasized(style: TextStyle, weight: FontWeight = FontWeight.Bold) =
    style.copy(fontWeight = weight, letterSpacing = (-0.25).sp)

private val AppTypography = Typography(
    displayLarge = textStyle(57, 64, FontWeight.SemiBold),
    displayMedium = textStyle(45, 52, FontWeight.SemiBold),
    displaySmall = textStyle(36, 44, FontWeight.SemiBold),
    headlineLarge = textStyle(32, 40, FontWeight.SemiBold),
    headlineMedium = textStyle(28, 36, FontWeight.SemiBold),
    headlineSmall = textStyle(24, 32, FontWeight.SemiBold),
    titleLarge = textStyle(22, 28, FontWeight.SemiBold),
    titleMedium = textStyle(16, 24, FontWeight.SemiBold),
    titleSmall = textStyle(14, 20, FontWeight.Medium),
    bodyLarge = textStyle(16, 24, FontWeight.Normal),
    bodyMedium = textStyle(14, 20, FontWeight.Normal),
    bodySmall = textStyle(12, 16, FontWeight.Normal),
    labelLarge = textStyle(14, 20, FontWeight.SemiBold),
    labelMedium = textStyle(12, 16, FontWeight.Medium),
    labelSmall = textStyle(11, 16, FontWeight.Medium),
    displayLargeEmphasized = emphasized(textStyle(57, 64, FontWeight.SemiBold)),
    displayMediumEmphasized = emphasized(textStyle(45, 52, FontWeight.SemiBold)),
    displaySmallEmphasized = emphasized(textStyle(36, 44, FontWeight.SemiBold)),
    headlineLargeEmphasized = emphasized(textStyle(32, 40, FontWeight.SemiBold)),
    headlineMediumEmphasized = emphasized(textStyle(28, 36, FontWeight.SemiBold)),
    headlineSmallEmphasized = emphasized(textStyle(24, 32, FontWeight.SemiBold)),
    titleLargeEmphasized = emphasized(textStyle(22, 28, FontWeight.SemiBold)),
    titleMediumEmphasized = emphasized(textStyle(16, 24, FontWeight.SemiBold)),
    titleSmallEmphasized = emphasized(textStyle(14, 20, FontWeight.Medium)),
    bodyLargeEmphasized = emphasized(textStyle(16, 24, FontWeight.Medium), FontWeight.Medium),
    bodyMediumEmphasized = emphasized(textStyle(14, 20, FontWeight.Medium), FontWeight.Medium),
    bodySmallEmphasized = emphasized(textStyle(12, 16, FontWeight.Medium), FontWeight.Medium),
    labelLargeEmphasized = emphasized(textStyle(14, 20, FontWeight.SemiBold)),
    labelMediumEmphasized = emphasized(textStyle(12, 16, FontWeight.Medium)),
    labelSmallEmphasized = emphasized(textStyle(11, 16, FontWeight.Medium)),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun RecipesTheme(
    darkTheme: Boolean,
    followSystemTheme: Boolean,
    content: @Composable () -> Unit,
) {
    val useDarkTheme = if (followSystemTheme) isSystemInDarkTheme() else darkTheme
    MaterialTheme(
        colorScheme = if (useDarkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        motionScheme = MotionScheme.expressive(),
        content = content,
    )
}

val BrandOrangeColor: Color = BrandOrange
