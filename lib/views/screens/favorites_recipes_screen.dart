import "package:flutter/cupertino.dart";
import "package:flutter/material.dart";
import "package:flutter_riverpod/flutter_riverpod.dart";
import "package:the_recipes/controllers/recipe_controller.dart";
import "package:the_recipes/controllers/favorites_controller.dart";
import "package:the_recipes/messages.dart";
import "package:the_recipes/views/screens/recipes_page.dart";
import "package:the_recipes/views/widgets/recipe_card.dart";

class FavoritesRecipesScreen extends ConsumerWidget {
  const FavoritesRecipesScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final recipeState = ref.watch(recipeControllerProvider);
    final favoritesAsyncState = ref.watch(favoritesControllerProvider);

    return Scaffold(
      appBar: AppBar(
        title: Text("favorites_recipes_screen.title".tr),
        leading: IconButton(
          onPressed: () {
            Navigator.of(context).pop();
          },
          icon: const Icon(CupertinoIcons.back),
        ),
      ),
      body: favoritesAsyncState.when(
        data: (favoritesState) {
          final favoriteRecipes = recipeState.recipes
              .where((recipe) =>
                  favoritesState.favoriteRecipeIds.contains(recipe.id))
              .toList();

          return SingleChildScrollView(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                favoriteRecipes.isEmpty
                    ? Container(
                        width: MediaQuery.of(context).size.width,
                        height: MediaQuery.of(context).size.height - 200,
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Text(
                              "favorites_recipes_screen.no_favorites".tr,
                              textAlign: TextAlign.center,
                            ),
                          ],
                        ),
                      )
                    : ListView.separated(
                        physics: const NeverScrollableScrollPhysics(),
                        shrinkWrap: true,
                        itemCount: favoriteRecipes.length,
                        itemBuilder: (context, index) {
                          return RecipeCard(
                            recipe: favoriteRecipes[index],
                            viewOption: ViewOption.list,
                          );
                        },
                        separatorBuilder: (context, index) =>
                            const SizedBox(height: 12),
                      ),
              ],
            ),
          );
        },
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, stack) => Center(
          child: Text(':( $error'),
        ),
      ),
    );
  }
}
