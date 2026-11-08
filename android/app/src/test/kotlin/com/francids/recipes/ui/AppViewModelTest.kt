package com.francids.recipes.ui

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.francids.recipes.data.LocalStore
import com.francids.recipes.data.Recipe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppViewModelTest {
    @Test
    fun initialStateLoadsRecipesFavoritesAndTranslations() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val store = LocalStore(application)
        val recipe = Recipe(
            id = "recipe-1",
            title = "Pasta",
            description = "Con tomate",
            ingredients = listOf("pasta", "tomate"),
            directions = listOf("Cocinar"),
            preparationTime = 600,
        )
        store.saveRecipes(listOf(recipe))
        store.saveFavorites(setOf(recipe.id))

        val viewModel = AppViewModel(application)

        assertEquals(1, viewModel.recipes.size)
        assertEquals("Pasta", viewModel.recipes.single().title)
        assertTrue(viewModel.isFavorite(recipe.id))
        assertFalse(viewModel.isLoggedIn)
        assertTrue(viewModel.tr("inicial_screen.title").isNotBlank())
    }
}
