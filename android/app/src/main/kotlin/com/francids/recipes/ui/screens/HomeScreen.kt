@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
)

package com.francids.recipes.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FolderShared
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumExtendedFloatingActionButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.francids.recipes.R
import com.francids.recipes.data.SortOption
import com.francids.recipes.data.ViewOption
import com.francids.recipes.ui.AppViewModel
import com.francids.recipes.ui.components.EmptyState
import com.francids.recipes.ui.components.RecipeCard
import com.francids.recipes.ui.components.RecipeImage
import com.francids.recipes.ui.components.SettingsRow

@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onAddRecipe: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenRecipe: (String) -> Unit,
    onOpenProfileInfo: () -> Unit,
    onOpenPublicSearch: () -> Unit,
    onOpenSharedRecipes: () -> Unit,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var menuExpanded by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val titles = listOf(
        viewModel.tr("inicial_screen.title"),
        viewModel.tr("favorites_recipes_screen.title"),
        viewModel.tr("profile_page.title"),
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(
                        text = titles[selectedTab],
                        style = MaterialTheme.typography.headlineLargeEmphasized,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                actions = {
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
                                text = { Text(viewModel.tr("inicial_screen.menu_item_reload")) },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Refresh, contentDescription = null)
                                },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.refreshRecipes()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(viewModel.tr("inicial_screen.menu_item_settings")) },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Settings, contentDescription = null)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onOpenSettings()
                                },
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            if (selectedTab == 0 && viewModel.recipes.isNotEmpty()) {
                MediumExtendedFloatingActionButton(
                    onClick = onAddRecipe,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = viewModel.tr("inicial_screen.fab_tooltip_add"),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        },
        bottomBar = {
            ShortNavigationBar {
                ShortNavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) {
                                Icons.Filled.Home
                            } else {
                                Icons.Outlined.Home
                            },
                            contentDescription = null,
                        )
                    },
                    label = { Text(viewModel.tr("inicial_screen.title")) },
                )
                ShortNavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) {
                                Icons.Filled.Favorite
                            } else {
                                Icons.Outlined.FavoriteBorder
                            },
                            contentDescription = null,
                        )
                    },
                    label = { Text(viewModel.tr("recipes_page.favorites_filter")) },
                )
                ShortNavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) {
                                Icons.Filled.Person
                            } else {
                                Icons.Outlined.Person
                            },
                            contentDescription = null,
                        )
                    },
                    label = { Text(viewModel.tr("profile_page.title")) },
                )
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
        ) {
            when (selectedTab) {
                0 -> RecipesPage(
                    viewModel = viewModel,
                    onOpenRecipe = onOpenRecipe,
                    onAddRecipe = onAddRecipe,
                )

                1 -> FavoritesPage(
                    viewModel = viewModel,
                    onOpenRecipe = onOpenRecipe,
                    onExploreRecipes = { selectedTab = 0 },
                )

                else -> ProfilePage(
                    viewModel = viewModel,
                    onOpenProfileInfo = onOpenProfileInfo,
                    onOpenFavorites = { selectedTab = 1 },
                    onOpenPublicSearch = onOpenPublicSearch,
                    onOpenSharedRecipes = onOpenSharedRecipes,
                )
            }
        }
    }
}

@Composable
private fun RecipesPage(
    viewModel: AppViewModel,
    onOpenRecipe: (String) -> Unit,
    onAddRecipe: () -> Unit,
) {
    var sortOption by rememberSaveable { mutableStateOf(SortOption.NONE) }

    val displayedRecipes = remember(viewModel.recipes, sortOption) {
        when (sortOption) {
            SortOption.TITLE -> viewModel.recipes.sortedBy { it.title.lowercase() }
            SortOption.DURATION -> viewModel.recipes.sortedBy { it.preparationTime }
            SortOption.NONE -> viewModel.recipes
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (viewModel.recipes.isNotEmpty()) {
            val sortLabel = when (sortOption) {
                SortOption.TITLE -> viewModel.tr("recipes_page.sort_title")
                SortOption.DURATION -> viewModel.tr("recipes_page.sort_duration")
                SortOption.NONE -> viewModel.tr("recipes_page.sort_by_label")
            }
            val sortIcon = when (sortOption) {
                SortOption.NONE -> Icons.AutoMirrored.Outlined.Sort
                SortOption.TITLE -> Icons.AutoMirrored.Outlined.Sort
                SortOption.DURATION -> Icons.Outlined.Schedule
            }

            ButtonGroup(
                overflowIndicator = { state ->
                    ButtonGroupDefaults.OverflowIndicator(menuState = state)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                toggleableItem(
                    checked = sortOption != SortOption.NONE,
                    label = sortLabel,
                    onCheckedChange = {
                        sortOption = when (sortOption) {
                            SortOption.NONE -> SortOption.TITLE
                            SortOption.TITLE -> SortOption.DURATION
                            SortOption.DURATION -> SortOption.NONE
                        }
                    },
                    icon = { Icon(sortIcon, contentDescription = null) },
                )
                toggleableItem(
                    checked = viewModel.viewOption == ViewOption.GRID,
                    label = if (viewModel.viewOption == ViewOption.LIST) {
                        viewModel.tr("recipes_page.view_list")
                    } else {
                        viewModel.tr("recipes_page.view_grid")
                    },
                    onCheckedChange = { viewModel.toggleViewOption() },
                    icon = {
                        Icon(
                            imageVector = if (viewModel.viewOption == ViewOption.LIST) {
                                Icons.AutoMirrored.Filled.List
                            } else {
                                Icons.Filled.GridView
                            },
                            contentDescription = null,
                        )
                    },
                )
            }
        }

        when {
            viewModel.recipes.isEmpty() -> EmptyState(
                title = viewModel.tr("inicial_screen.empty_list"),
                icon = Icons.Outlined.RestaurantMenu,
                modifier = Modifier.weight(1f),
                action = {
                    FilledTonalButton(onClick = onAddRecipe) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(viewModel.tr("inicial_screen.fab_tooltip_add"))
                    }
                },
            )

            viewModel.viewOption == ViewOption.GRID -> LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 120.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(displayedRecipes, key = { it.id }) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        viewOption = ViewOption.GRID,
                        isFavorite = recipe.id in viewModel.favorites,
                        language = viewModel.language,
                        onClick = { onOpenRecipe(recipe.id) },
                    )
                }
            }

            else -> LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 120.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(displayedRecipes, key = { it.id }) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        viewOption = ViewOption.LIST,
                        isFavorite = recipe.id in viewModel.favorites,
                        language = viewModel.language,
                        onClick = { onOpenRecipe(recipe.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoritesPage(
    viewModel: AppViewModel,
    onOpenRecipe: (String) -> Unit,
    onExploreRecipes: () -> Unit,
) {
    val favoriteRecipes = viewModel.recipes.filter { it.id in viewModel.favorites }

    if (favoriteRecipes.isEmpty()) {
        EmptyState(
            title = viewModel.tr("favorites_recipes_screen.no_favorites"),
            icon = Icons.Outlined.FavoriteBorder,
            action = {
                FilledTonalButton(onClick = onExploreRecipes) {
                    Icon(Icons.Outlined.RestaurantMenu, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(viewModel.tr("inicial_screen.title"))
                }
            },
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(favoriteRecipes, key = { it.id }) { recipe ->
                RecipeCard(
                    recipe = recipe,
                    viewOption = ViewOption.LIST,
                    isFavorite = true,
                    language = viewModel.language,
                    onClick = { onOpenRecipe(recipe.id) },
                )
            }
        }
    }
}

@Composable
private fun ProfilePage(
    viewModel: AppViewModel,
    onOpenProfileInfo: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenPublicSearch: () -> Unit,
    onOpenSharedRecipes: () -> Unit,
) {
    val user = viewModel.user
    if (user == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.recipe_logo),
                contentDescription = null,
                modifier = Modifier.size(104.dp),
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = viewModel.tr("profile_page.sign_in_required"),
                style = MaterialTheme.typography.headlineMediumEmphasized,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = viewModel.tr("profile_page.sign_in_description"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))
            androidx.compose.material3.Button(onClick = viewModel::signInWithGoogle) {
                Icon(Icons.Outlined.PersonAdd, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(viewModel.tr("profile_page.sign_in_with_google"))
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Card(
                onClick = onOpenProfileInfo,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (user.picture.isNullOrBlank()) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(64.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Outlined.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                )
                            }
                        }
                    } else {
                        RecipeImage(
                            path = user.picture,
                            contentDescription = user.name,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape),
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.name.ifBlank {
                                viewModel.tr("profile_page.unknown_user")
                            },
                            style = MaterialTheme.typography.titleLargeEmphasized,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                    )
                }
            }
        }

        item {
            Text(
                text = viewModel.tr("profile_page.data_section"),
                style = MaterialTheme.typography.titleLargeEmphasized,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp),
            )
        }

        item {
            ProfileCard {
                SettingsRow(
                    icon = Icons.Outlined.FavoriteBorder,
                    title = viewModel.tr("profile_page.favorite_recipes"),
                    supporting = viewModel.tr("profile_page.favorite_recipes_description"),
                    onClick = onOpenFavorites,
                    showChevron = true,
                )
                HorizontalCardDivider()
                SettingsRow(
                    icon = Icons.Outlined.CloudUpload,
                    title = viewModel.tr("profile_page.backup_recipes"),
                    supporting = viewModel.tr("profile_page.backup_recipes_description"),
                    trailing = {
                        Switch(
                            checked = viewModel.autoSync,
                            onCheckedChange = viewModel::toggleAutoSync,
                        )
                    },
                )
                if (viewModel.autoSync) {
                    HorizontalCardDivider()
                    SettingsRow(
                        icon = Icons.Outlined.FolderShared,
                        title = viewModel.tr("profile_page.shared_recipes"),
                        supporting = viewModel.tr("profile_page.shared_recipes_description"),
                        onClick = onOpenSharedRecipes,
                        showChevron = true,
                    )
                }
                HorizontalCardDivider()
                SettingsRow(
                    icon = Icons.Outlined.Search,
                    title = viewModel.tr("profile_page.search_public_recipes"),
                    supporting = viewModel.tr("profile_page.search_public_recipes_description"),
                    onClick = onOpenPublicSearch,
                    showChevron = true,
                )
            }
        }

        item {
            ProfileCard {
                SettingsRow(
                    icon = Icons.AutoMirrored.Outlined.Logout,
                    title = viewModel.tr("profile_page.sign_out"),
                    supporting = viewModel.tr("profile_page.sign_out_description"),
                    onClick = {
                        viewModel.showConfirmation(
                            com.francids.recipes.data.ConfirmRequest(
                                title = viewModel.tr("profile_page.sign_out_confirmation_title"),
                                message = viewModel.tr("profile_page.sign_out_confirmation_message"),
                                confirmLabel = viewModel.tr("ui_helpers.confirm"),
                                cancelLabel = viewModel.tr("ui_helpers.cancel"),
                                destructive = true,
                                onConfirm = { viewModel.signOut() },
                            ),
                        )
                    },
                    iconContainerColor = MaterialTheme.colorScheme.errorContainer,
                    iconContentColor = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
    }
}

@Composable
private fun ProfileCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column { content() }
    }
}

@Composable
fun HorizontalCardDivider() {
    androidx.compose.material3.HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    )
}
