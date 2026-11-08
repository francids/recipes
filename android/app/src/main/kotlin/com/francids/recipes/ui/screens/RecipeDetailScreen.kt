package com.francids.recipes.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.francids.recipes.data.ConfirmRequest
import com.francids.recipes.ui.AppViewModel
import com.francids.recipes.ui.components.BackTopBar
import com.francids.recipes.ui.components.EmptyState
import com.francids.recipes.ui.components.PopupMenuRow
import com.francids.recipes.ui.components.RecipeContent
import com.francids.recipes.ui.theme.AppTheme
import com.francids.recipes.ui.theme.DeepOrange

@Composable
fun RecipeDetailScreen(
    viewModel: AppViewModel,
    recipeId: String,
    onBack: () -> Unit,
) {
    val palette = AppTheme.palette
    val recipe = viewModel.recipeById(recipeId)
    var showShareSheet by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    if (recipe == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(palette.background),
        ) {
            BackTopBar(title = viewModel.tr("recipe_screen.title"), onBack = onBack)
            EmptyState(
                text = viewModel.tr("recipe_controller.recipe_not_found"),
                modifier = Modifier.weight(1f),
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background),
    ) {
        BackTopBar(
            title = viewModel.tr("recipe_screen.title"),
            onBack = onBack,
            actions = {
                IconButton(onClick = { viewModel.toggleFavorite(recipe.id) }) {
                    Icon(
                        imageVector = if (viewModel.isFavorite(recipe.id)) {
                            Icons.Filled.Favorite
                        } else {
                            Icons.Filled.FavoriteBorder
                        },
                        contentDescription = viewModel.tr("recipes_page.favorites_filter"),
                        tint = if (viewModel.isFavorite(recipe.id)) {
                            Color.Red
                        } else {
                            palette.textPrimary
                        },
                    )
                }
                if (viewModel.isLoggedIn && viewModel.autoSync) {
                    IconButton(onClick = { showShareSheet = true }) {
                        Icon(
                            imageVector = if (viewModel.isRecipePublic(recipe.id)) {
                                Icons.Outlined.Public
                            } else {
                                Icons.Outlined.Share
                            },
                            contentDescription = viewModel.tr("recipe_screen.share_recipe"),
                            tint = palette.textPrimary,
                        )
                    }
                }
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = viewModel.tr("inicial_screen.menu_tooltip"),
                            tint = palette.textPrimary,
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        shape = RoundedCornerShape(10.dp),
                        containerColor = palette.popup,
                        tonalElevation = 0.dp,
                        shadowElevation = 4.dp,
                        border = BorderStroke(0.5.dp, palette.border),
                    ) {
                        PopupMenuRow(
                            icon = Icons.Filled.Delete,
                            text = viewModel.tr("recipe_screen.menu_item_delete"),
                            onClick = {
                                menuExpanded = false
                                viewModel.showConfirmation(
                                    ConfirmRequest(
                                        title = viewModel.tr("recipe_screen.delete_confirmation_title"),
                                        message = viewModel.tr("recipe_screen.delete_confirmation_message"),
                                        confirmLabel = viewModel.tr("recipe_screen.delete_button"),
                                        cancelLabel = viewModel.tr("recipe_screen.cancel_button"),
                                        destructive = true,
                                        onConfirm = {
                                            viewModel.deleteRecipe(recipe, onFinished = onBack)
                                        },
                                    ),
                                )
                            },
                        )
                    }
                }
            },
        )

        RecipeContent(
            recipe = recipe,
            language = viewModel.language,
            modifier = Modifier
                .weight(1f)
                .navigationBarsPadding(),
            bottomPadding = 24.dp,
        )
    }

    if (showShareSheet) {
        ShareRecipeSheet(
            viewModel = viewModel,
            recipeId = recipe.id,
            onDismiss = { showShareSheet = false },
        )
    }
}
