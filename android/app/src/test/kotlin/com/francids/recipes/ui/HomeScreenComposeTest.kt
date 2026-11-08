package com.francids.recipes.ui

import android.app.Application
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.core.app.ApplicationProvider
import com.francids.recipes.ui.screens.HomeScreen
import com.francids.recipes.ui.theme.RecipesTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "en-rUS-w411dp-h891dp-420dpi")
class HomeScreenComposeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun homeExposesNavigationMenuAndPrimaryAction() {
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
        composeRule.onNodeWithContentDescription("Options").assertExists()
        composeRule.onAllNodesWithText("Recipes").assertCountEquals(2)
        composeRule.onAllNodesWithText("Add recipe").assertCountEquals(1)
    }
}
