@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.francids.recipes.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.francids.recipes.R
import com.francids.recipes.data.SortOption
import com.francids.recipes.data.ViewOption
import com.francids.recipes.ui.AppViewModel
import com.francids.recipes.ui.components.EmptyState
import com.francids.recipes.ui.components.RecipeCard
import com.francids.recipes.ui.components.RecipeImage

@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onAddRecipe: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenRecipe: (String) -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenProfileInfo: () -> Unit,
    onOpenPublicSearch: () -> Unit,
    onOpenSharedRecipes: () -> Unit,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedTab == 0) {
                            viewModel.tr("inicial_screen.title")
                        } else {
                            viewModel.tr("profile_page.title")
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
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
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = onAddRecipe,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = viewModel.tr("inicial_screen.fab_tooltip_add"))
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Outlined.Home, contentDescription = null) },
                    label = { Text(viewModel.tr("inicial_screen.title")) },
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                    label = { Text(viewModel.tr("profile_page.title")) },
                )
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (selectedTab == 0) {
                RecipesPage(viewModel = viewModel, onOpenRecipe = onOpenRecipe)
            } else {
                ProfilePage(
                    viewModel = viewModel,
                    onOpenProfileInfo = onOpenProfileInfo,
                    onOpenFavorites = onOpenFavorites,
                    onOpenPublicSearch = onOpenPublicSearch,
                    onOpenSharedRecipes = onOpenSharedRecipes,
                )
            }
        }
    }
}

@Composable
private fun RecipesPage(viewModel: AppViewModel, onOpenRecipe: (String) -> Unit) {
    var sortOption by rememberSaveable { mutableStateOf(SortOption.NONE) }
    var showFavoritesOnly by rememberSaveable { mutableStateOf(false) }

    val displayedRecipes = remember(
        viewModel.recipes,
        viewModel.favorites,
        sortOption,
        showFavoritesOnly,
    ) {
        val filtered = if (showFavoritesOnly) {
            viewModel.recipes.filter { it.id in viewModel.favorites }
        } else {
            viewModel.recipes
        }
        when (sortOption) {
            SortOption.TITLE -> filtered.sortedBy { it.title.lowercase() }
            SortOption.DURATION -> filtered.sortedBy { it.preparationTime }
            SortOption.NONE -> filtered
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (viewModel.recipes.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (!viewModel.isLoggedIn) {
                    FilterChip(
                        selected = showFavoritesOnly,
                        onClick = { showFavoritesOnly = !showFavoritesOnly },
                        label = { Text(viewModel.tr("recipes_page.favorites_filter")) },
                        leadingIcon = {
                            Icon(
                                if (showFavoritesOnly) Icons.Filled.Favorite
                                else Icons.Filled.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                    )
                }

                val sortLabel = when (sortOption) {
                    SortOption.TITLE -> viewModel.tr("recipes_page.sort_title")
                    SortOption.DURATION -> viewModel.tr("recipes_page.sort_duration")
                    SortOption.NONE -> viewModel.tr("recipes_page.sort_by_label")
                }
                FilterChip(
                    selected = sortOption != SortOption.NONE,
                    onClick = {
                        sortOption = when (sortOption) {
                            SortOption.NONE -> SortOption.TITLE
                            SortOption.TITLE -> SortOption.DURATION
                            SortOption.DURATION -> SortOption.NONE
                        }
                    },
                    label = { Text(sortLabel) },
                    leadingIcon = {
                        Icon(
                            imageVector = when (sortOption) {
                                SortOption.TITLE -> Icons.AutoMirrored.Outlined.Sort
                                SortOption.DURATION -> Icons.Outlined.Schedule
                                SortOption.NONE -> Icons.AutoMirrored.Outlined.Sort
                            },
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                )

                FilterChip(
                    selected = true,
                    onClick = viewModel::toggleViewOption,
                    label = {
                        Text(
                            if (viewModel.viewOption == ViewOption.LIST) {
                                viewModel.tr("recipes_page.view_list")
                            } else {
                                viewModel.tr("recipes_page.view_grid")
                            },
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (viewModel.viewOption == ViewOption.LIST) {
                                Icons.AutoMirrored.Filled.List
                            } else {
                                Icons.Default.GridView
                            },
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                )
            }
        }

        when {
            viewModel.recipes.isEmpty() -> EmptyState(
                text = viewModel.tr("inicial_screen.empty_list"),
                modifier = Modifier.weight(1f),
            )

            displayedRecipes.isEmpty() -> EmptyState(
                text = viewModel.tr("recipes_page.no_favorites"),
                modifier = Modifier.weight(1f),
            )

            viewModel.viewOption == ViewOption.GRID -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(displayedRecipes, key = { it.id }) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        viewOption = ViewOption.GRID,
                        onClick = { onOpenRecipe(recipe.id) },
                    )
                }
            }

            else -> LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(displayedRecipes, key = { it.id }) { recipe ->
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.recipe_logo),
                contentDescription = null,
                modifier = Modifier.size(96.dp),
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = viewModel.tr("profile_page.sign_in_required"),
                style = MaterialTheme.typography.displayMedium,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = viewModel.tr("profile_page.sign_in_description"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            androidx.compose.material3.Button(
                onClick = viewModel::signInWithGoogle,
            ) {
                Icon(Icons.Outlined.Person, contentDescription = null)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenProfileInfo),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape),
                    ) {
                        if (user.picture.isNullOrBlank()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Outlined.Person, contentDescription = null)
                            }
                        } else {
                            RecipeImage(
                                path = user.picture,
                                contentDescription = user.name,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = user.name.ifBlank {
                                viewModel.tr("profile_page.unknown_user")
                            },
                            style = MaterialTheme.typography.displayMedium,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        item {
            SettingsActionCard(
                title = viewModel.tr("profile_page.favorite_recipes"),
                subtitle = viewModel.tr("profile_page.favorite_recipes_description"),
                icon = Icons.Outlined.FavoriteBorder,
                onClick = onOpenFavorites,
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column {
                    ListItem(
                        headlineContent = {
                            Text(
                                viewModel.tr("profile_page.backup_recipes"),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        },
                        supportingContent = {
                            Text(
                                viewModel.tr("profile_page.backup_recipes_description"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        leadingContent = {
                            Icon(
                                Icons.Outlined.CloudUpload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        },
                        trailingContent = {
                            Switch(
                                checked = viewModel.autoSync,
                                onCheckedChange = viewModel::toggleAutoSync,
                            )
                        },
                    )

                    if (viewModel.autoSync) {
                        HorizontalCardDivider()
                        ListItem(
                            modifier = Modifier.clickable(onClick = onOpenSharedRecipes),
                            headlineContent = {
                                Text(
                                    viewModel.tr("profile_page.shared_recipes"),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            },
                            supportingContent = {
                                Text(
                                    viewModel.tr("profile_page.shared_recipes_description"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            leadingContent = {
                                Icon(
                                    Icons.Outlined.Star,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            },
                        )
                    }

                    HorizontalCardDivider()
                    ListItem(
                        modifier = Modifier.clickable(onClick = onOpenPublicSearch),
                        headlineContent = {
                            Text(
                                viewModel.tr("profile_page.search_public_recipes"),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        },
                        supportingContent = {
                            Text(
                                viewModel.tr("profile_page.search_public_recipes_description"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        leadingContent = {
                            Icon(
                                Icons.Outlined.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        },
                    )
                }
            }
        }

        item {
            SettingsActionCard(
                title = viewModel.tr("profile_page.sign_out"),
                subtitle = viewModel.tr("profile_page.sign_out_description"),
                icon = Icons.AutoMirrored.Outlined.Logout,
                onClick = {
                    viewModel.showConfirmation(com.francids.recipes.data.ConfirmRequest(
                        title = viewModel.tr("profile_page.sign_out_confirmation_title"),
                        message = viewModel.tr("profile_page.sign_out_confirmation_message"),
                        confirmLabel = viewModel.tr("ui_helpers.confirm"),
                        cancelLabel = viewModel.tr("ui_helpers.cancel"),
                        destructive = true,
                        onConfirm = { viewModel.signOut() },
                    ))
                },
            )
        }
    }
}

@Composable
fun SettingsActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        ListItem(
            headlineContent = { Text(title, style = MaterialTheme.typography.bodyLarge) },
            supportingContent = {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            leadingContent = {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            trailingContent = {
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            },
        )
    }
}

@Composable
fun HorizontalCardDivider() {
    androidx.compose.material3.HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    )
}

