import "package:flutter/cupertino.dart";
import "package:flutter/material.dart";
import "package:flutter_riverpod/flutter_riverpod.dart";
import "package:flutter_animate/flutter_animate.dart";
import "package:the_recipes/controllers/recipe_controller.dart";
import "package:the_recipes/controllers/favorites_controller.dart";
import "package:the_recipes/controllers/view_option_controller.dart";
import "package:the_recipes/controllers/auth_controller.dart";
import "package:the_recipes/messages.dart";
import "package:the_recipes/views/widgets/recipe_card.dart";

enum SortOption { none, title, duration }

enum ViewOption { list, grid }

class RecipesPage extends ConsumerStatefulWidget {
  const RecipesPage({super.key});

  @override
  ConsumerState<RecipesPage> createState() => _RecipesPageState();
}

class _RecipesPageState extends ConsumerState<RecipesPage> {
  SortOption currentSortOption = SortOption.none;

  @override
  Widget build(BuildContext context) {
    final recipeState = ref.watch(recipeControllerProvider);
    final favoritesAsyncState = ref.watch(favoritesControllerProvider);
    final viewOption = ref.watch(viewOptionControllerProvider);
    final authState = ref.watch(authControllerProvider);

    return favoritesAsyncState.when(
      data: (favoritesState) => Stack(
        children: [
          SingleChildScrollView(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                SizedBox(
                  height: MediaQuery.of(context).padding.top,
                ),
                if (recipeState.recipes.isEmpty)
                  const SizedBox.shrink()
                else
                  _buildFilterChip(ref, currentSortOption, viewOption,
                          favoritesState, authState)
                      .animate()
                      .fadeIn(duration: 300.ms)
                      .slideY(begin: -0.2, curve: Curves.easeOutCubic),
                _buildRecipesList(ref, recipeState, favoritesState, viewOption),
              ],
            ),
          ),
          Positioned(
            top: 0,
            left: 0,
            right: 0,
            child: IgnorePointer(
              child: Container(
                height: 140,
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    begin: Alignment.bottomCenter,
                    end: Alignment.topCenter,
                    colors: [
                      Theme.of(context).scaffoldBackgroundColor.withAlpha(0),
                      Theme.of(context).scaffoldBackgroundColor.withAlpha(18),
                      Theme.of(context).scaffoldBackgroundColor.withAlpha(36),
                      Theme.of(context).scaffoldBackgroundColor.withAlpha(54),
                      Theme.of(context).scaffoldBackgroundColor.withAlpha(72),
                      Theme.of(context).scaffoldBackgroundColor.withAlpha(108),
                      Theme.of(context).scaffoldBackgroundColor.withAlpha(144),
                      Theme.of(context).scaffoldBackgroundColor.withAlpha(180),
                      Theme.of(context).scaffoldBackgroundColor.withAlpha(196),
                      Theme.of(context).scaffoldBackgroundColor,
                    ],
                    stops: const [
                      0.0,
                      0.05,
                      0.1,
                      0.15,
                      0.2,
                      0.25,
                      0.3,
                      0.35,
                      0.4,
                      1.0
                    ],
                  ),
                ),
              ),
            ),
          ),
          Positioned(
            bottom: 0,
            left: 0,
            right: 0,
            child: IgnorePointer(
              child: Container(
                height: 140,
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    begin: Alignment.topCenter,
                    end: Alignment.bottomCenter,
                    colors: [
                      Theme.of(context).scaffoldBackgroundColor.withAlpha(0),
                      Theme.of(context).scaffoldBackgroundColor.withAlpha(18),
                      Theme.of(context).scaffoldBackgroundColor.withAlpha(36),
                      Theme.of(context).scaffoldBackgroundColor.withAlpha(54),
                      Theme.of(context).scaffoldBackgroundColor.withAlpha(72),
                      Theme.of(context).scaffoldBackgroundColor.withAlpha(90),
                      Theme.of(context).scaffoldBackgroundColor.withAlpha(108),
                      Theme.of(context).scaffoldBackgroundColor.withAlpha(126),
                      Theme.of(context).scaffoldBackgroundColor.withAlpha(144),
                      Theme.of(context).scaffoldBackgroundColor.withAlpha(162),
                      Theme.of(context).scaffoldBackgroundColor.withAlpha(180),
                      Theme.of(context).scaffoldBackgroundColor,
                    ],
                    stops: const [
                      0.0,
                      0.1,
                      0.2,
                      0.3,
                      0.4,
                      0.5,
                      0.6,
                      0.7,
                      0.8,
                      0.9,
                      0.95,
                      1.0
                    ],
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
      loading: () => const Center(child: CircularProgressIndicator()),
      error: (error, stack) => Center(
        child: Text('Error loading favorites: $error'),
      ),
    );
  }

  Widget _buildRecipesList(
    WidgetRef ref,
    RecipeState recipeState,
    FavoritesState favoritesState,
    ViewOption viewOption,
  ) {
    var displayedRecipes = favoritesState.showFavoritesOnly
        ? recipeState.recipes
            .where((recipe) =>
                favoritesState.favoriteRecipeIds.contains(recipe.id))
            .toList()
        : recipeState.recipes.toList();

    if (currentSortOption == SortOption.title) {
      displayedRecipes.sort(
          (a, b) => a.title.toLowerCase().compareTo(b.title.toLowerCase()));
    } else if (currentSortOption == SortOption.duration) {
      displayedRecipes
          .sort((a, b) => a.preparationTime.compareTo(b.preparationTime));
    }

    if (displayedRecipes.isEmpty) {
      return Container(
        width: MediaQuery.of(context).size.width,
        height: MediaQuery.of(context).size.height -
            MediaQuery.of(context).padding.top -
            MediaQuery.of(context).padding.bottom,
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(
              favoritesState.showFavoritesOnly
                  ? "inicial_screen.empty_favorites".tr
                  : "inicial_screen.empty_list".tr,
              textAlign: TextAlign.center,
            ),
          ],
        ),
      );
    } else {
      return RefreshIndicator(
        onRefresh: () async {
          ref.read(recipeControllerProvider.notifier).refreshRecipes();
        },
        child: viewOption == ViewOption.list
            ? ListView.separated(
                physics: const NeverScrollableScrollPhysics(),
                shrinkWrap: true,
                padding: EdgeInsets.only(
                  top: 8,
                  left: 16,
                  right: 16,
                  bottom: MediaQuery.of(context).padding.bottom + 100,
                ),
                itemCount: displayedRecipes.length,
                itemBuilder: (context, index) {
                  return RecipeCard(
                    recipe: displayedRecipes[index],
                    viewOption: ViewOption.list,
                  );
                },
                separatorBuilder: (context, index) =>
                    const SizedBox(height: 12),
              )
            : GridView.builder(
                physics: const NeverScrollableScrollPhysics(),
                shrinkWrap: true,
                padding: EdgeInsets.only(
                  top: 8,
                  bottom: MediaQuery.of(context).padding.bottom + 16.0 + 80.0,
                  left: 16,
                  right: 16,
                ),
                gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                  crossAxisCount: 2,
                  crossAxisSpacing: 12,
                  mainAxisSpacing: 12,
                  childAspectRatio: 1.0,
                ),
                itemCount: displayedRecipes.length,
                itemBuilder: (context, index) {
                  return RecipeCard(
                    recipe: displayedRecipes[index],
                    viewOption: viewOption,
                  )
                      .animate()
                      .fadeIn(
                        delay: (50 * index).ms,
                        duration: 300.ms,
                      )
                      .slideX(
                        begin: -0.1,
                        delay: (50 * index).ms,
                        duration: 300.ms,
                        curve: Curves.easeOutCubic,
                      );
                },
              ),
      );
    }
  }

  Widget _buildFilterChip(
    WidgetRef ref,
    SortOption currentSortOption,
    ViewOption viewOption,
    FavoritesState favoritesState,
    AuthState authState,
  ) {
    return Padding(
      padding: const EdgeInsets.only(top: 8),
      child: SingleChildScrollView(
        padding: const EdgeInsets.symmetric(horizontal: 16),
        scrollDirection: Axis.horizontal,
        child: Row(
          spacing: 8,
          children: [
            if (!authState.isLoggedIn) _buildFavoritesChip(ref, favoritesState),
            _buildSortChip(currentSortOption),
            _buildViewChip(viewOption),
          ],
        ),
      ),
    );
  }

  Widget _buildViewChip(ViewOption viewOption) {
    return FilterChip(
      showCheckmark: false,
      label: Text(viewOption == ViewOption.list
          ? "recipes_page.view_list".tr
          : "recipes_page.view_grid".tr),
      selected: true,
      onSelected: (selected) {
        ref.read(viewOptionControllerProvider.notifier).toggleViewOption();
      },
      avatar: Icon(
        viewOption == ViewOption.list
            ? CupertinoIcons.list_bullet
            : CupertinoIcons.square_grid_2x2_fill,
        size: 18,
      ),
    );
  }

  Widget _buildSortChip(SortOption currentSortOption) {
    String sortLabel;
    IconData sortIcon;
    switch (currentSortOption) {
      case SortOption.title:
        sortLabel = "recipes_page.sort_title".tr;
        sortIcon = CupertinoIcons.sort_up;
        break;
      case SortOption.duration:
        sortLabel = "recipes_page.sort_duration".tr;
        sortIcon = CupertinoIcons.clock;
        break;
      case SortOption.none:
        sortLabel = "recipes_page.sort_by_label".tr;
        sortIcon = CupertinoIcons.sort_down;
        break;
    }
    return FilterChip(
      showCheckmark: false,
      selected: currentSortOption != SortOption.none,
      avatar: Icon(sortIcon, size: 18),
      label: Text(sortLabel),
      onSelected: (_) {
        setState(() {
          if (currentSortOption == SortOption.none) {
            this.currentSortOption = SortOption.title;
          } else if (currentSortOption == SortOption.title) {
            this.currentSortOption = SortOption.duration;
          } else if (currentSortOption == SortOption.duration) {
            this.currentSortOption = SortOption.none;
          }
        });
      },
    );
  }

  Widget _buildFavoritesChip(
    WidgetRef ref,
    FavoritesState favoritesState,
  ) {
    return FilterChip(
      showCheckmark: false,
      label: Text("recipes_page.favorites_filter".tr),
      selected: favoritesState.showFavoritesOnly,
      onSelected: (selected) async {
        await ref
            .read(favoritesControllerProvider.notifier)
            .setFilter(selected);
      },
      avatar: Icon(
        favoritesState.showFavoritesOnly
            ? CupertinoIcons.heart_fill
            : CupertinoIcons.heart,
        size: 18,
      ),
    );
  }
}
