package com.francids.recipes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.francids.recipes.R

val DeepOrange = Color(0xFFFF5722)

/**
 * Custom palette recovered from the original Flutter app. These are intentionally
 * plain brand/neutral values rather than Material tonal roles.
 */
@Immutable
data class AppPalette(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val card: Color,
    val popup: Color,
    val navBackground: Color,
    val border: Color,
    val shadow: Color,
    val primary: Color,
    val onPrimary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val divider: Color,
    val chipBackground: Color,
    val chipSelected: Color,
    val snackbarBackground: Color,
    val snackbarText: Color,
    val imagePlaceholder: Color,
    val imagePlaceholderIcon: Color,
)

private val LightPalette = AppPalette(
    isDark = false,
    background = Color.White,
    surface = Color.White,
    card = Color(0xFFFAFAFA),
    popup = Color.White,
    navBackground = Color.White,
    border = Color(0x42000000),
    shadow = Color(0x1F000000),
    primary = DeepOrange,
    onPrimary = Color.White,
    textPrimary = Color.Black,
    textSecondary = Color(0xE6000000),
    textTertiary = Color(0x8A000000),
    divider = Color(0x18000000),
    chipBackground = Color(0xFFF5F5F5),
    chipSelected = Color(0x33FF5722),
    snackbarBackground = Color(0xFF323232),
    snackbarText = Color.White,
    imagePlaceholder = Color(0xFFE0E0E0),
    imagePlaceholderIcon = Color(0xFF757575),
)

private val DarkPalette = AppPalette(
    isDark = true,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    card = Color(0xFF2A2A2A),
    popup = Color(0xFF2A2A2A),
    navBackground = Color(0xFF1E1E1E),
    border = Color(0x4DFFFFFF),
    shadow = Color(0x42000000),
    primary = DeepOrange,
    onPrimary = Color.White,
    textPrimary = Color.White,
    textSecondary = Color(0xE1FFFFFF),
    textTertiary = Color(0x99FFFFFF),
    divider = Color(0x18FFFFFF),
    chipBackground = Color(0xFF2A2A2A),
    chipSelected = Color(0x33FF5722),
    snackbarBackground = Color(0xFF2A2A2A),
    snackbarText = Color.White,
    imagePlaceholder = Color(0xFF424242),
    imagePlaceholderIcon = Color(0xFF9E9E9E),
)

val LocalAppPalette = staticCompositionLocalOf { LightPalette }

object AppTheme {
    val palette: AppPalette
        @Composable get() = LocalAppPalette.current
}

private val LightColors = lightColorScheme(
    primary = DeepOrange,
    onPrimary = Color.White,
    primaryContainer = DeepOrange,
    onPrimaryContainer = Color.White,
    secondary = DeepOrange,
    onSecondary = Color.White,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0x8A000000),
    outline = Color(0x42000000),
    error = Color.Red,
    onError = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = DeepOrange,
    onPrimary = Color.White,
    primaryContainer = DeepOrange,
    onPrimaryContainer = Color.White,
    secondary = DeepOrange,
    onSecondary = Color.White,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0x99FFFFFF),
    outline = Color(0x4DFFFFFF),
    error = Color.Red,
    onError = Color.White,
)

private val Montserrat = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold),
)

private fun style(
    size: Int,
    lineHeight: Int,
    weight: FontWeight,
) = TextStyle(
    fontFamily = Montserrat,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    fontWeight = weight,
)

private val AppTypography = Typography(
    displayLarge = style(28, 34, FontWeight.SemiBold),
    displayMedium = style(20, 26, FontWeight.Medium),
    displaySmall = style(24, 30, FontWeight.SemiBold),
    headlineLarge = style(24, 30, FontWeight.SemiBold),
    headlineMedium = style(20, 26, FontWeight.Medium),
    headlineSmall = style(18, 24, FontWeight.SemiBold),
    titleLarge = style(18, 24, FontWeight.SemiBold),
    titleMedium = style(16, 22, FontWeight.Medium),
    titleSmall = style(14, 20, FontWeight.Medium),
    bodyLarge = style(16, 22, FontWeight.Medium),
    bodyMedium = style(14, 20, FontWeight.Normal),
    bodySmall = style(12, 16, FontWeight.Normal),
    labelLarge = style(14, 20, FontWeight.Medium),
    labelMedium = style(12, 16, FontWeight.Normal),
    labelSmall = style(11, 16, FontWeight.Normal),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(16.dp),
)

@Composable
fun RecipesTheme(
    darkTheme: Boolean,
    followSystemTheme: Boolean,
    content: @Composable () -> Unit,
) {
    val useDarkTheme = if (followSystemTheme) isSystemInDarkTheme() else darkTheme
    androidx.compose.runtime.CompositionLocalProvider(
        LocalAppPalette provides if (useDarkTheme) DarkPalette else LightPalette,
    ) {
        MaterialTheme(
            colorScheme = if (useDarkTheme) DarkColors else LightColors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
