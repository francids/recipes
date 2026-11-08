package com.francids.recipes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.francids.recipes.data.ViewOption
import com.francids.recipes.ui.AppViewModel
import com.francids.recipes.ui.components.BackTopBar
import com.francids.recipes.ui.components.EmptyState
import com.francids.recipes.ui.components.RecipeCard
import com.francids.recipes.ui.components.RecipeContent
import com.francids.recipes.ui.theme.AppTheme
import com.francids.recipes.ui.theme.DeepOrange

@Composable
fun FavoritesScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onOpenRecipe: (String) -> Unit,
) {
    val palette = AppTheme.palette
    val favoriteRecipes = viewModel.recipes.filter { it.id in viewModel.favorites }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background),
    ) {
        BackTopBar(
            title = viewModel.tr("favorites_recipes_screen.title"),
            onBack = onBack,
        )
        if (favoriteRecipes.isEmpty()) {
            EmptyState(
                text = viewModel.tr("favorites_recipes_screen.no_favorites"),
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(favoriteRecipes, key = { it.id }) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        viewOption = ViewOption.LIST,
                        onClick = { onOpenRecipe(recipe.id) },
                    )
                }
            }
        }
    }
}

@Composable
fun SharedRecipesScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onOpenRecipe: (String) -> Unit,
) {
    val palette = AppTheme.palette
    val publicRecipes = viewModel.recipes.filter { it.isPublic }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background),
    ) {
        BackTopBar(
            title = viewModel.tr("shared_recipes_screen.title"),
            onBack = onBack,
        )
        if (publicRecipes.isEmpty()) {
            EmptyState(
                text = viewModel.tr("shared_recipes_screen.no_public_recipes"),
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(publicRecipes, key = { it.id }) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        viewOption = ViewOption.LIST,
                        onClick = { onOpenRecipe(recipe.id) },
                    )
                }
            }
        }
    }
}

@Composable
fun SharedRecipeScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
) {
    val palette = AppTheme.palette
    val recipe = viewModel.pendingSharedRecipe
    val alreadySaved = recipe?.let(viewModel::isRecipeAlreadySaved) == true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background),
    ) {
        BackTopBar(
            title = viewModel.tr("shared_recipe.title"),
            onBack = {
                viewModel.clearPendingSharedRecipe()
                onBack()
            },
            actions = {
                if (recipe != null) {
                    IconButton(
                        onClick = {
                            if (!alreadySaved) {
                                viewModel.saveSharedRecipe(recipe) { saved ->
                                    if (saved) {
                                        viewModel.clearPendingSharedRecipe()
                                        onBack()
                                    }
                                }
                            }
                        },
                    ) {
                        Icon(
                            imageVector = if (alreadySaved) {
                                Icons.Filled.CheckCircle
                            } else {
                                Icons.Filled.Download
                            },
                            contentDescription = if (alreadySaved) {
                                viewModel.tr("shared_recipe.already_saved")
                            } else {
                                viewModel.tr("shared_recipe.save_locally")
                            },
                            tint = if (alreadySaved) Color(0xFF4CAF50) else DeepOrange,
                        )
                    }
                }
            },
        )

        if (recipe == null) {
            EmptyState(
                text = viewModel.tr("shared_recipe.not_found_error"),
                modifier = Modifier.weight(1f),
            )
        } else {
            RecipeContent(
                recipe = recipe,
                language = viewModel.language,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
