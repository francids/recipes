package com.francids.recipes.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import com.francids.recipes.data.LocalStore
import com.francids.recipes.data.Recipe
import com.francids.recipes.ui.screens.AddRecipeScreen
import com.francids.recipes.ui.screens.RecipeDetailScreen
import com.francids.recipes.ui.screens.SettingsScreen
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
class ScreensScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    private fun capture(name: String) {
        composeRule.waitForIdle()
        val bitmap = composeRule.onRoot().captureToImage().asAndroidBitmap()
        val output = File("build/reports/screenshots/$name.png")
        output.parentFile?.mkdirs()
        output.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test
    fun addRecipeRendersFirstStep() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = AppViewModel(application)

        composeRule.setContent {
            RecipesTheme(darkTheme = false, followSystemTheme = false) {
                AddRecipeScreen(viewModel = viewModel, onBack = {})
            }
        }

        capture("add_recipe")
    }

    @Test
    fun settingsRenders() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = AppViewModel(application)

        composeRule.setContent {
            RecipesTheme(darkTheme = false, followSystemTheme = false) {
                SettingsScreen(viewModel = viewModel, onBack = {})
            }
        }

        capture("settings")
    }

    @Test
    fun recipeDetailRenders() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        LocalStore(application).saveRecipes(
            listOf(
                Recipe(
                    id = "detail",
                    title = "Sopa de tomate",
                    description = "Una sopa sencilla y caliente para los días fríos, con tomate, cebolla y un toque de albahaca.",
                    ingredients = listOf("Tomates maduros", "Cebolla", "Albahaca", "Aceite de oliva"),
                    directions = listOf("Sofríe la cebolla", "Añade el tomate", "Cocina 20 minutos"),
                    preparationTime = 1800,
                ),
            ),
        )
        val viewModel = AppViewModel(application)

        composeRule.setContent {
            RecipesTheme(darkTheme = false, followSystemTheme = false) {
                RecipeDetailScreen(
                    viewModel = viewModel,
                    recipeId = "detail",
                    onBack = {},
                )
            }
        }

        capture("recipe_detail")
    }
}
