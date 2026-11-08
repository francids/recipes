@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
)

package com.francids.recipes.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.HorizontalFloatingToolbar
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
import androidx.compose.ui.Alignment
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
                EmptyState(
                    title = viewModel.tr("recipe_controller.recipe_not_found"),
                    icon = Icons.Outlined.Share,
                )
            }
        }
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        viewModel.tr("recipe_screen.title"),
                        style = MaterialTheme.typography.titleLargeEmphasized,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                HorizontalFloatingToolbar(
                    expanded = true,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    IconButton(onClick = { viewModel.toggleFavorite(recipe.id) }) {
                        Icon(
                            imageVector = if (viewModel.isFavorite(recipe.id)) {
                                Icons.Filled.Favorite
                            } else {
                                Icons.Filled.FavoriteBorder
                            },
                            contentDescription = viewModel.tr("recipes_page.favorites_filter"),
                            tint = if (viewModel.isFavorite(recipe.id)) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
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
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    IconButton(
                        onClick = {
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
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = viewModel.tr("recipe_screen.menu_item_delete"),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        },
    ) { padding ->
        RecipeContent(
            recipe = recipe,
            language = viewModel.language,
            modifier = Modifier.padding(padding),
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

@Composable
fun BackTopBar(
    title: String,
    onBack: () -> Unit,
    actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLargeEmphasized,
            )
        },
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
