@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.francids.recipes.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.francids.recipes.data.ConfirmRequest
import com.francids.recipes.ui.AppViewModel
import com.francids.recipes.ui.components.EmptyState
import com.francids.recipes.ui.components.RecipeContent

@Composable
fun RecipeDetailScreen(
    viewModel: AppViewModel,
    recipeId: String,
    onBack: () -> Unit,
) {
    val recipe = viewModel.recipeById(recipeId)
    var menuExpanded by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }

    if (recipe == null) {
        Scaffold(
            topBar = {
                BackTopBar(
                    title = viewModel.tr("recipe_screen.title"),
                    onBack = onBack,
                )
            },
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                EmptyState(viewModel.tr("recipe_controller.recipe_not_found"))
            }
        }
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(viewModel.tr("recipe_screen.title")) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite(recipe.id) }) {
                        Icon(
                            imageVector = if (viewModel.isFavorite(recipe.id)) {
                                Icons.Filled.Favorite
                            } else {
                                Icons.Filled.FavoriteBorder
                            },
                            contentDescription = null,
                            tint = if (viewModel.isFavorite(recipe.id)) Color.Red else {
                                MaterialTheme.colorScheme.onSurface
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
                            )
                        }
                    }

                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = viewModel.tr("inicial_screen.menu_tooltip"),
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(viewModel.tr("recipe_screen.menu_item_delete")) },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, contentDescription = null)
                                },
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
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            RecipeContent(
                recipe = recipe,
                language = viewModel.language,
            )
        }
    }

    if (showShareSheet) {
        ShareRecipeSheet(
            viewModel = viewModel,
            recipeId = recipe.id,
            onDismiss = { showShareSheet = false },
        )
    }
}

@Composable
fun BackTopBar(
    title: String,
    onBack: () -> Unit,
    actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
    )
}
