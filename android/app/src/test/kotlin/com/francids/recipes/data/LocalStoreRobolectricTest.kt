package com.francids.recipes.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LocalStoreRobolectricTest {
    @Test
    fun recipesAndFavoritesArePersistedInPrivateStorage() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = LocalStore(context)
        val recipe = Recipe(
            id = "recipe-1",
            title = "Tortilla",
            description = "Patatas y huevo",
            ingredients = listOf("patata", "huevo"),
            directions = listOf("Freír"),
            preparationTime = 900,
        )

        store.saveRecipes(listOf(recipe))
        store.saveFavorites(setOf(recipe.id))

        val restored = LocalStore(context)
        assertEquals(listOf(recipe), restored.loadRecipes())
        assertEquals(setOf(recipe.id), restored.loadFavorites())
    }

    @Test
    fun exportAndImportRoundTripUsesTheSameZipFormat() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = LocalStore(context)
        val original = Recipe(
            id = "recipe-1",
            title = "Sopa",
            description = "Caliente",
            ingredients = listOf("tomate"),
            directions = listOf("Cocinar"),
            preparationTime = 600,
        )
        val service = ExportImportService(store)

        val zip = service.buildExportZip(listOf(original))
        val result = service.importZip(zip, emptyList(), "user-1")

        assertEquals(1, result.importedCount)
        assertEquals(0, result.skippedCount)
        assertEquals("Sopa", store.loadRecipes().single().title)
        assertEquals("user-1", store.loadRecipes().single().ownerId)
    }

    @Test
    fun translationsAreLoadedForEverySupportedLanguage() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        Messages.load(context)

        Messages.supportedLanguages.forEach { language ->
            val title = Messages.translate(language, "inicial_screen.title")
            assertTrue("Missing translation for $language", title.isNotBlank())
        }
        assertEquals("Recipe saved", Messages.translate("en", "add_recipe_screen.recipe_saved"))
        assertTrue(
            Messages.translate("es", "profile_page.import_success_message", listOf(2, 1))
                .contains("2"),
        )
    }
}
