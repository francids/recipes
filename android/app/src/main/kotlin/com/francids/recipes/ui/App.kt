@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.francids.recipes.ui

import android.app.Activity
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.core.view.WindowCompat
import com.francids.recipes.data.ConfirmRequest
import com.francids.recipes.data.DeepLinkTarget
import com.francids.recipes.data.LoadingRequest
import com.francids.recipes.ui.screens.AddRecipeScreen
import com.francids.recipes.ui.screens.HomeScreen
import com.francids.recipes.ui.screens.ProfileInfoScreen
import com.francids.recipes.ui.screens.PublicSearchScreen
import com.francids.recipes.ui.screens.RecipeDetailScreen
import com.francids.recipes.ui.screens.SettingsScreen
import com.francids.recipes.ui.screens.SharedRecipeScreen
import com.francids.recipes.ui.screens.SharedRecipesScreen
import com.francids.recipes.ui.theme.RecipesTheme

object Routes {
    const val HOME = "home"
    const val ADD = "add"
    const val SETTINGS = "settings"
    const val PROFILE_INFO = "profile-info"
    const val SHARED_RECIPES = "shared-recipes"
    const val PUBLIC_SEARCH = "public-search"
    const val SHARED_RECIPE = "shared-recipe"
    const val RECIPE = "recipe/{recipeId}"

    fun recipe(id: String) = "recipe/$id"
}

@Composable
fun RecipesApp(viewModel: AppViewModel) {
    RecipesTheme(
        darkTheme = viewModel.darkTheme,
        followSystemTheme = viewModel.followSystemTheme,
    ) {
        val navController = rememberNavController()
        val motionScheme = MaterialTheme.motionScheme
        val isDarkTheme = if (viewModel.followSystemTheme) isSystemInDarkTheme()
        else viewModel.darkTheme
        val view = LocalView.current
        val context = LocalContext.current

        SideEffect {
            val activity = context as? Activity ?: return@SideEffect
            WindowCompat.getInsetsController(activity.window, view).apply {
                isAppearanceLightStatusBars = !isDarkTheme
                isAppearanceLightNavigationBars = !isDarkTheme
            }
        }
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(viewModel.message) {
            val current = viewModel.message ?: return@LaunchedEffect
            snackbarHostState.showSnackbar(current.text)
            viewModel.consumeMessage()
        }

        LaunchedEffect(viewModel.deepLinkTarget) {
            when (val target = viewModel.deepLinkTarget) {
                is DeepLinkTarget.LocalRecipe -> {
                    navController.navigate(Routes.recipe(target.recipeId)) { launchSingleTop = true }
                    viewModel.consumeDeepLink()
                }

                is DeepLinkTarget.SharedRecipe -> {
                    navController.navigate(Routes.SHARED_RECIPE) { launchSingleTop = true }
                    viewModel.consumeDeepLink()
                }

                null -> Unit
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
                enterTransition = {
                    fadeIn(animationSpec = motionScheme.fastEffectsSpec()) +
                        scaleIn(
                            initialScale = 0.97f,
                            animationSpec = motionScheme.fastSpatialSpec(),
                        )
                },
                exitTransition = {
                    fadeOut(animationSpec = motionScheme.fastEffectsSpec()) +
                        scaleOut(
                            targetScale = 1.02f,
                            animationSpec = motionScheme.fastSpatialSpec(),
                        )
                },
                popEnterTransition = {
                    fadeIn(animationSpec = motionScheme.fastEffectsSpec()) +
                        scaleIn(
                            initialScale = 1.02f,
                            animationSpec = motionScheme.fastSpatialSpec(),
                        )
                },
                popExitTransition = {
                    fadeOut(animationSpec = motionScheme.fastEffectsSpec()) +
                        scaleOut(
                            targetScale = 0.97f,
                            animationSpec = motionScheme.fastSpatialSpec(),
                        )
                },
            ) {
                composable(Routes.HOME) {
                    HomeScreen(
                        viewModel = viewModel,
                        onAddRecipe = { navController.navigate(Routes.ADD) },
                        onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                        onOpenRecipe = { id -> navController.navigate(Routes.recipe(id)) },
                        onOpenProfileInfo = { navController.navigate(Routes.PROFILE_INFO) },
                        onOpenPublicSearch = { navController.navigate(Routes.PUBLIC_SEARCH) },
                        onOpenSharedRecipes = { navController.navigate(Routes.SHARED_RECIPES) },
                    )
                }

                composable(Routes.RECIPE, arguments = listOf(navArgument("recipeId") {
                    type = NavType.StringType
                })) { entry ->
                    RecipeDetailScreen(
                        viewModel = viewModel,
                        recipeId = entry.arguments?.getString("recipeId").orEmpty(),
                        onBack = { navController.popBackStack() },
                    )
                }

                composable(Routes.ADD) {
                    AddRecipeScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                    )
                }

                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                    )
                }

                composable(Routes.PROFILE_INFO) {
                    ProfileInfoScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                    )
                }

                composable(Routes.SHARED_RECIPES) {
                    SharedRecipesScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onOpenRecipe = { id -> navController.navigate(Routes.recipe(id)) },
                    )
                }

                composable(Routes.PUBLIC_SEARCH) {
                    PublicSearchScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onOpenSharedRecipe = { recipe ->
                            viewModel.openSharedRecipe(recipe)
                            navController.navigate(Routes.SHARED_RECIPE)
                        },
                    )
                }

                composable(Routes.SHARED_RECIPE) {
                    SharedRecipeScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                    )
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 16.dp, end = 16.dp, bottom = 104.dp),
            )
        }

        viewModel.loading?.let { LoadingDialog(it) }
        viewModel.confirmation?.let { request ->
            ConfirmationDialog(
                request = request,
                onDismiss = viewModel::dismissConfirmation,
            )
        }
    }
}

@Composable
private fun LoadingDialog(request: LoadingRequest) {
    Dialog(onDismissRequest = {}) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ContainedLoadingIndicator(
                    modifier = Modifier.size(64.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    indicatorColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(Modifier.size(20.dp))
                Text(
                    request.title,
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                if (request.message.isNotBlank()) {
                    Spacer(Modifier.size(8.dp))
                    Text(
                        request.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfirmationDialog(request: ConfirmRequest, onDismiss: () -> Unit) {
    val accentContainer = if (request.destructive) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val accentContent = if (request.destructive) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = accentContainer,
                contentColor = accentContent,
            ) {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    modifier = Modifier.padding(14.dp).size(28.dp),
                )
            }
        },
        title = {
            Text(
                request.title,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        },
        text = {
            Text(
                request.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    request.onConfirm()
                },
            ) {
                Text(
                    request.confirmLabel,
                    color = if (request.destructive) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(request.cancelLabel) }
        },
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    )
}
