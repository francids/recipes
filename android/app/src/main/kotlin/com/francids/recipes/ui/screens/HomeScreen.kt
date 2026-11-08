@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.francids.recipes.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.Logout
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
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.francids.recipes.R
import com.francids.recipes.data.SortOption
import com.francids.recipes.data.ViewOption
import com.francids.recipes.ui.AppViewModel
import com.francids.recipes.ui.components.AppCard
import com.francids.recipes.ui.components.AppChip
import com.francids.recipes.ui.components.AppDivider
import com.francids.recipes.ui.components.EmptyState
import com.francids.recipes.ui.components.PrimaryButton
import com.francids.recipes.ui.components.RecipeCard
import com.francids.recipes.ui.components.RecipeImage
import com.francids.recipes.ui.components.SettingsRow
import com.francids.recipes.ui.theme.AppTheme
import com.francids.recipes.ui.theme.DeepOrange

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
    val palette = AppTheme.palette
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var menuExpanded by remember { mutableStateOf(false) }
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val titles = listOf(
        viewModel.tr("inicial_screen.title"),
        viewModel.tr("profile_page.title"),
    )

    Box(modifier = Modifier.fillMaxSize().background(palette.background)) {
        when (selectedTab) {
            0 -> RecipesPage(
                viewModel = viewModel,
                onOpenRecipe = onOpenRecipe,
                modifier = Modifier.padding(top = statusBarTop + 64.dp),
            )

            else -> ProfilePage(
                viewModel = viewModel,
                onOpenProfileInfo = onOpenProfileInfo,
                onOpenFavorites = onOpenFavorites,
                onOpenPublicSearch = onOpenPublicSearch,
                onOpenSharedRecipes = onOpenSharedRecipes,
                modifier = Modifier.padding(top = statusBarTop + 64.dp),
            )
        }

        HomeTopBar(
            title = titles[selectedTab],
            menuExpanded = menuExpanded,
            onMenuExpandedChange = { menuExpanded = it },
            reloadLabel = viewModel.tr("inicial_screen.menu_item_reload"),
            settingsLabel = viewModel.tr("inicial_screen.menu_item_settings"),
            menuContentDescription = viewModel.tr("inicial_screen.menu_tooltip"),
            onReload = { viewModel.refreshRecipes() },
            onOpenSettings = onOpenSettings,
            showBorder = selectedTab == 1,
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(140.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            palette.background.copy(alpha = 0f),
                            palette.background.copy(alpha = 0.55f),
                            palette.background,
                        ),
                    ),
                ),
        )

        BoxWithConstraints(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 32.dp),
        ) {
            val pillWidth = maxWidth * 0.6f
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FloatingTabBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    titles = titles,
                    modifier = Modifier
                        .width(pillWidth)
                        .height(56.dp),
                )
                if (selectedTab == 0) {
                    Spacer(Modifier.weight(1f))
                    PressableFab(
                        onClick = onAddRecipe,
                        contentDescription = viewModel.tr("inicial_screen.fab_tooltip_add"),
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    title: String,
    menuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    reloadLabel: String,
    settingsLabel: String,
    menuContentDescription: String,
    onReload: () -> Unit,
    onOpenSettings: () -> Unit,
    showBorder: Boolean,
) {
    val palette = AppTheme.palette
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        palette.background,
                        palette.background.copy(alpha = 0.30f),
                        palette.background.copy(alpha = 0f),
                    ),
                ),
            ),
    ) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = palette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Box {
                IconButton(onClick = { onMenuExpandedChange(true) }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = menuContentDescription,
                        tint = palette.textPrimary,
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { onMenuExpandedChange(false) },
                    shape = RoundedCornerShape(10.dp),
                    containerColor = palette.popup,
                    tonalElevation = 0.dp,
                    shadowElevation = 4.dp,
                    border = BorderStroke(0.5.dp, palette.border),
                ) {
                    MenuRow(
                        icon = Icons.Outlined.Refresh,
                        text = reloadLabel,
                        onClick = {
                            onMenuExpandedChange(false)
                            onReload()
                        },
                    )
                    MenuRow(
                        icon = Icons.Outlined.Settings,
                        text = settingsLabel,
                        onClick = {
                            onMenuExpandedChange(false)
                            onOpenSettings()
                        },
                    )
                }
            }
        }
        if (showBorder) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(palette.border),
            )
        }
    }
}

@Composable
private fun MenuRow(icon: ImageVector, text: String, onClick: () -> Unit) {
    val palette = AppTheme.palette
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = palette.textPrimary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = palette.textPrimary,
        )
    }
}

@Composable
private fun FloatingTabBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    titles: List<String>,
    modifier: Modifier = Modifier,
) {
    val palette = AppTheme.palette
    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(10.dp),
                ambientColor = palette.shadow,
                spotColor = palette.shadow,
            )
            .clip(RoundedCornerShape(10.dp))
            .background(palette.navBackground)
            .border(0.5.dp, palette.border, RoundedCornerShape(10.dp))
            .padding(4.dp),
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            TabItem(
                selected = selectedTab == 0,
                title = titles[0],
                icon = if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                onClick = { onTabSelected(0) },
                modifier = Modifier.weight(1f),
            )
            TabItem(
                selected = selectedTab == 1,
                title = titles[1],
                icon = if (selectedTab == 1) Icons.Filled.Person else Icons.Outlined.Person,
                onClick = { onTabSelected(1) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun TabItem(
    selected: Boolean,
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = AppTheme.palette
    Row(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) palette.primary else Color.Transparent)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) Color.White else palette.textTertiary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.White else palette.textTertiary,
            maxLines = 1,
        )
    }
}

@Composable
private fun PressableFab(onClick: () -> Unit, contentDescription: String) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.975f else 1f,
        label = "fab-press",
    )
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier.scale(scale),
        shape = RoundedCornerShape(10.dp),
        containerColor = DeepOrange,
        contentColor = Color.White,
        elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(0.dp),
        interactionSource = interactionSource,
    ) {
        Icon(Icons.Filled.Add, contentDescription = contentDescription)
    }
}

@Composable
private fun RecipesPage(
    viewModel: AppViewModel,
    onOpenRecipe: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = AppTheme.palette
    var sortOption by rememberSaveable { mutableStateOf(SortOption.NONE) }
    var showFavoritesOnly by rememberSaveable { mutableStateOf(false) }

    val displayedRecipes = remember(viewModel.recipes, viewModel.favorites, sortOption, showFavoritesOnly) {
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

    Column(modifier = modifier.fillMaxSize()) {
        if (viewModel.recipes.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (!viewModel.isLoggedIn) {
                    AppChip(
                        text = viewModel.tr("recipes_page.favorites_filter"),
                        selected = showFavoritesOnly,
                        onClick = { showFavoritesOnly = !showFavoritesOnly },
                        leadingIcon = Icons.Outlined.FavoriteBorder,
                    )
                }
                AppChip(
                    text = when (sortOption) {
                        SortOption.TITLE -> viewModel.tr("recipes_page.sort_title")
                        SortOption.DURATION -> viewModel.tr("recipes_page.sort_duration")
                        SortOption.NONE -> viewModel.tr("recipes_page.sort_by_label")
                    },
                    selected = sortOption != SortOption.NONE,
                    onClick = {
                        sortOption = when (sortOption) {
                            SortOption.NONE -> SortOption.TITLE
                            SortOption.TITLE -> SortOption.DURATION
                            SortOption.DURATION -> SortOption.NONE
                        }
                    },
                    leadingIcon = if (sortOption == SortOption.DURATION) {
                        Icons.Outlined.Schedule
                    } else {
                        Icons.AutoMirrored.Outlined.Sort
                    },
                )
                AppChip(
                    text = if (viewModel.viewOption == ViewOption.LIST) {
                        viewModel.tr("recipes_page.view_list")
                    } else {
                        viewModel.tr("recipes_page.view_grid")
                    },
                    selected = true,
                    onClick = viewModel::toggleViewOption,
                    leadingIcon = if (viewModel.viewOption == ViewOption.LIST) {
                        Icons.AutoMirrored.Filled.List
                    } else {
                        Icons.Filled.GridView
                    },
                )
            }
        }

        if (viewModel.recipes.isEmpty()) {
            EmptyState(
                text = viewModel.tr("inicial_screen.empty_list"),
                modifier = Modifier.weight(1f),
            )
        } else if (displayedRecipes.isEmpty()) {
            EmptyState(
                text = viewModel.tr("recipes_page.no_favorites"),
                modifier = Modifier.weight(1f),
            )
        } else if (viewModel.viewOption == ViewOption.GRID) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 140.dp,
                ),
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
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 140.dp,
                ),
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
    modifier: Modifier = Modifier,
) {
    val palette = AppTheme.palette
    val user = viewModel.user
    if (user == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.recipe_logo),
                contentDescription = null,
                modifier = Modifier.size(80.dp),
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = viewModel.tr("profile_page.sign_in_required"),
                style = MaterialTheme.typography.displayMedium,
                color = palette.textPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = viewModel.tr("profile_page.sign_in_description"),
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            PrimaryButton(onClick = viewModel::signInWithGoogle) {
                Icon(Icons.Outlined.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(viewModel.tr("profile_page.sign_in_with_google"))
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            AppCard(
                onClick = onOpenProfileInfo,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(DeepOrange.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (user.picture.isNullOrBlank()) {
                            Icon(
                                Icons.Outlined.Person,
                                contentDescription = null,
                                tint = DeepOrange,
                                modifier = Modifier.size(32.dp),
                            )
                        } else {
                            RecipeImage(
                                path = user.picture,
                                contentDescription = user.name,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.name.ifBlank {
                                viewModel.tr("profile_page.unknown_user")
                            },
                            style = MaterialTheme.typography.displayMedium,
                            color = palette.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        item {
            AppCard(modifier = Modifier.fillMaxWidth()) {
                SettingsRow(
                    icon = Icons.Outlined.FavoriteBorder,
                    title = viewModel.tr("profile_page.favorite_recipes"),
                    supporting = viewModel.tr("profile_page.favorite_recipes_description"),
                    onClick = onOpenFavorites,
                    showChevron = true,
                )
            }
        }

        item {
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    SettingsRow(
                        icon = Icons.Outlined.CloudUpload,
                        title = viewModel.tr("profile_page.backup_recipes"),
                        supporting = viewModel.tr("profile_page.backup_recipes_description"),
                        onClick = { viewModel.toggleAutoSync(!viewModel.autoSync) },
                        trailing = {
                            AppSwitch(
                                checked = viewModel.autoSync,
                                onCheckedChange = viewModel::toggleAutoSync,
                            )
                        },
                    )
                    if (viewModel.autoSync) {
                        AppDivider()
                        SettingsRow(
                            icon = Icons.Outlined.FolderShared,
                            title = viewModel.tr("profile_page.shared_recipes"),
                            supporting = viewModel.tr("profile_page.shared_recipes_description"),
                            onClick = onOpenSharedRecipes,
                            showChevron = true,
                        )
                    }
                    AppDivider()
                    SettingsRow(
                        icon = Icons.Outlined.Search,
                        title = viewModel.tr("profile_page.search_public_recipes"),
                        supporting = viewModel.tr("profile_page.search_public_recipes_description"),
                        onClick = onOpenPublicSearch,
                        showChevron = true,
                    )
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(0.5.dp)
                    .background(palette.divider),
            )
        }

        item {
            AppCard(modifier = Modifier.fillMaxWidth()) {
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
                    showChevron = true,
                )
            }
        }
    }
}

@Composable
fun AppSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    val palette = AppTheme.palette
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = DeepOrange,
            checkedBorderColor = Color.Transparent,
            uncheckedThumbColor = if (palette.isDark) Color(0xFFBDBDBD) else Color.White,
            uncheckedTrackColor = if (palette.isDark) Color(0xFF424242) else Color(0xFFE0E0E0),
            uncheckedBorderColor = Color.Transparent,
        ),
    )
}

@Composable
fun HorizontalCardDivider() {
    AppDivider()
}
