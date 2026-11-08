package com.francids.recipes.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.francids.recipes.data.LocalStore
import com.francids.recipes.data.Recipe
import com.francids.recipes.ui.screens.HomeScreen
import com.francids.recipes.ui.theme.RecipesTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "en-rUS-w411dp-h891dp-420dpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HomeScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun homeRendersWithoutDesignExceptions() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = AppViewModel(application)

        composeRule.setContent {
            RecipesTheme(darkTheme = false, followSystemTheme = false) {
                HomeScreen(
                    viewModel = viewModel,
                    onAddRecipe = {},
                    onOpenSettings = {},
                    onOpenRecipe = {},
                    onOpenProfileInfo = {},
                    onOpenPublicSearch = {},
                    onOpenSharedRecipes = {},
                )
            }
        }

        composeRule.waitForIdle()
        val bitmap = composeRule.onRoot().captureToImage().asAndroidBitmap()
        val output = File("build/reports/screenshots/home.png")
        output.parentFile?.mkdirs()
        output.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test
    fun homeRendersRecipeCollection() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val store = LocalStore(application)
        store.saveRecipes(
            listOf(
                Recipe(
                    id = "1",
                    title = "Sopa de tomate",
                    description = "Una sopa sencilla y caliente para los días fríos.",
                    ingredients = listOf("tomate", "cebolla"),
                    directions = listOf("Cocinar"),
                    preparationTime = 1800,
                ),
                Recipe(
                    id = "2",
                    title = "Tortilla de patatas",
                    description = "Receta tradicional con patata y huevo.",
                    ingredients = listOf("patata", "huevo"),
                    directions = listOf("Freír"),
                    preparationTime = 2400,
                ),
                Recipe(
                    id = "3",
                    title = "Ensalada César",
                    description = "Fresca, crujiente y con aderezo casero.",
                    ingredients = listOf("lechuga", "pollo"),
                    directions = listOf("Mezclar"),
                    preparationTime = 900,
                ),
            ),
        )
        store.saveFavorites(setOf("2"))
        val viewModel = AppViewModel(application)

        composeRule.setContent {
            RecipesTheme(darkTheme = false, followSystemTheme = false) {
                HomeScreen(
                    viewModel = viewModel,
                    onAddRecipe = {},
                    onOpenSettings = {},
                    onOpenRecipe = {},
                    onOpenProfileInfo = {},
                    onOpenPublicSearch = {},
                    onOpenSharedRecipes = {},
                )
            }
        }

        composeRule.waitForIdle()
        val bitmap = composeRule.onRoot().captureToImage().asAndroidBitmap()
        val output = File("build/reports/screenshots/home_with_recipes.png")
        output.parentFile?.mkdirs()
        output.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test
    fun homeMenuRendersWithExpressiveGrouping() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = AppViewModel(application)

        composeRule.setContent {
            RecipesTheme(darkTheme = false, followSystemTheme = false) {
                HomeScreen(
                    viewModel = viewModel,
                    onAddRecipe = {},
                    onOpenSettings = {},
                    onOpenRecipe = {},
                    onOpenProfileInfo = {},
                    onOpenPublicSearch = {},
                    onOpenSharedRecipes = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("Options").performClick()
        composeRule.waitForIdle()
        val bitmap = composeRule.onRoot().captureToImage().asAndroidBitmap()
        val output = File("build/reports/screenshots/home_menu.png")
        output.parentFile?.mkdirs()
        output.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test
    fun homeRendersInDarkTheme() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = AppViewModel(application)

        composeRule.setContent {
            RecipesTheme(darkTheme = true, followSystemTheme = false) {
                HomeScreen(
                    viewModel = viewModel,
                    onAddRecipe = {},
                    onOpenSettings = {},
                    onOpenRecipe = {},
                    onOpenProfileInfo = {},
                    onOpenPublicSearch = {},
                    onOpenSharedRecipes = {},
                )
            }
        }

        composeRule.waitForIdle()
        val bitmap = composeRule.onRoot().captureToImage().asAndroidBitmap()
        val output = File("build/reports/screenshots/home_dark.png")
        output.parentFile?.mkdirs()
        output.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
