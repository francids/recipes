package com.francids.recipes.ui

import android.app.Activity
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.francids.recipes.data.ConfirmRequest
import com.francids.recipes.data.DeepLinkTarget
import com.francids.recipes.data.LoadingRequest
import com.francids.recipes.ui.components.PrimaryButton
import com.francids.recipes.ui.components.SecondaryButton
import com.francids.recipes.ui.screens.AddRecipeScreen
import com.francids.recipes.ui.screens.FavoritesScreen
import com.francids.recipes.ui.screens.HomeScreen
import com.francids.recipes.ui.screens.ProfileInfoScreen
import com.francids.recipes.ui.screens.PublicSearchScreen
import com.francids.recipes.ui.screens.RecipeDetailScreen
import com.francids.recipes.ui.screens.SettingsScreen
import com.francids.recipes.ui.screens.SharedRecipeScreen
import com.francids.recipes.ui.screens.SharedRecipesScreen
import com.francids.recipes.ui.theme.AppTheme
import com.francids.recipes.ui.theme.DeepOrange
import com.francids.recipes.ui.theme.RecipesTheme

object Routes {
    const val HOME = "home"
    const val ADD = "add"
    const val SETTINGS = "settings"
    const val PROFILE_INFO = "profile-info"
    const val FAVORITES = "favorites"
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
        val palette = AppTheme.palette
        val navController = rememberNavController()
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
        var lastMessageIsError by remember { mutableStateOf(false) }

        LaunchedEffect(viewModel.message) {
            val current = viewModel.message ?: return@LaunchedEffect
            lastMessageIsError = current.isError
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
                .background(palette.background),
        ) {
            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
                enterTransition = { fadeIn(tween(180)) },
                exitTransition = { fadeOut(tween(120)) },
                popEnterTransition = { fadeIn(tween(180)) },
                popExitTransition = { fadeOut(tween(120)) },
            ) {
                composable(Routes.HOME) {
                    HomeScreen(
                        viewModel = viewModel,
                        onAddRecipe = { navController.navigate(Routes.ADD) },
                        onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                        onOpenRecipe = { id -> navController.navigate(Routes.recipe(id)) },
                        onOpenFavorites = { navController.navigate(Routes.FAVORITES) },
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

                composable(Routes.FAVORITES) {
                    FavoritesScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onOpenRecipe = { id -> navController.navigate(Routes.recipe(id)) },
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
            ) { data ->
                Row(
                    modifier = Modifier
                        .background(palette.snackbarBackground, RoundedCornerShape(10.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (lastMessageIsError) {
                            Icons.Outlined.ErrorOutline
                        } else {
                            Icons.Filled.CheckCircle
                        },
                        contentDescription = null,
                        tint = if (lastMessageIsError) Color(0xFFFF5252) else Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = data.visuals.message,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                        ),
                        color = palette.snackbarText,
                    )
                }
            }
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
    val palette = AppTheme.palette
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset(request.animationAsset),
    )
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .background(palette.surface, RoundedCornerShape(16.dp))
                .border(0.5.dp, palette.border, RoundedCornerShape(16.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LottieAnimation(
                    composition = composition,
                    modifier = Modifier.size(120.dp),
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = request.title,
                    style = MaterialTheme.typography.displayMedium,
                    color = palette.textPrimary,
                    textAlign = TextAlign.Center,
                )
                if (request.message.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = request.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfirmationDialog(request: ConfirmRequest, onDismiss: () -> Unit) {
    val palette = AppTheme.palette
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset(request.animationAsset),
    )
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .background(palette.surface, RoundedCornerShape(16.dp))
                .border(0.5.dp, palette.border, RoundedCornerShape(16.dp))
                .padding(16.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LottieAnimation(
                    composition = composition,
                    modifier = Modifier.size(120.dp),
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = request.title,
                    style = MaterialTheme.typography.displayMedium,
                    color = palette.textPrimary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = request.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textSecondary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SecondaryButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(request.cancelLabel)
                    }
                    PrimaryButton(
                        onClick = {
                            onDismiss()
                            request.onConfirm()
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(request.confirmLabel, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
