import "package:cupertino_ui/cupertino_ui.dart";
import "package:flutter_riverpod/flutter_riverpod.dart";
import "package:material_ui/material_ui.dart";
import "package:the_recipes/controllers/auth_controller.dart";
import "package:the_recipes/controllers/favorites_controller.dart";
import "package:the_recipes/controllers/recipe_controller.dart";
import "package:the_recipes/controllers/share_recipe_controller.dart";
import "package:the_recipes/messages.dart";
import "package:the_recipes/models/recipe.dart";
import "package:the_recipes/views/widgets/common_recipe_view.dart";
import "package:the_recipes/views/widgets/share_recipe_bottom_sheet.dart";
import "package:the_recipes/views/widgets/ui_helpers.dart";

class RecipeScreen extends ConsumerWidget {
  const RecipeScreen({super.key, required this.recipe});

  final Recipe recipe;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final favoritesAsyncState = ref.watch(favoritesControllerProvider);
    final authState = ref.watch(authControllerProvider);
    final shareController = ref.watch(shareRecipeControllerProvider.notifier);
    ref.watch(recipeControllerProvider);

    return favoritesAsyncState.when(
      data: (favoritesState) => Scaffold(
        appBar: AppBar(
          title: Text("recipe_screen.title".tr),
          leading: IconButton(
            onPressed: () {
              Navigator.of(context).pop();
            },
            icon: const Icon(CupertinoIcons.back),
          ),
          actions: [
            IconButton(
              onPressed: () async => await ref
                  .read(favoritesControllerProvider.notifier)
                  .toggleFavorite(recipe.id),
              icon: Icon(
                favoritesState.favoriteRecipeIds.contains(recipe.id)
                    ? CupertinoIcons.heart_fill
                    : CupertinoIcons.heart,
                color: favoritesState.favoriteRecipeIds.contains(recipe.id)
                    ? Colors.red
                    : null,
              ),
            ),
            if (authState.isLoggedIn && authState.autoSyncEnabled)
              IconButton(
                icon: Icon(
                  shareController.isPublic(recipe.id)
                      ? CupertinoIcons.globe
                      : CupertinoIcons.share,
                ),
                onPressed: () =>
                    ShareRecipeBottomSheet.show(context, recipe.id),
              ),
            IconButton(
              icon: const Icon(CupertinoIcons.ellipsis_vertical),
              tooltip: "inicial_screen.menu_tooltip".tr,
              onPressed: () {
                final navigatorContext = Navigator.of(context);
                showMenu(
                  context: context,
                  position: const RelativeRect.fromLTRB(100, 100, 0, 0),
                  items: [
                    PopupMenuItem(
                      child: ListTile(
                        leading: Icon(CupertinoIcons.delete),
                        title: Text("recipe_screen.menu_item_delete".tr),
                        horizontalTitleGap: 8,
                      ),
                      onTap: () {
                        Future.delayed(Duration.zero, () {
                          if (!context.mounted) return;
                          UIHelpers.showConfirmationDialog(
                            context: context,
                            title: "recipe_screen.delete_confirmation_title".tr,
                            message:
                                "recipe_screen.delete_confirmation_message".tr,
                            lottieAsset: "assets/lottie/delete.json",
                            confirmAction: () async {
                              navigatorContext.pop();
                              UIHelpers.showLoadingDialog(
                                context,
                                "recipe_screen.deleting_recipe".tr,
                                "recipe_screen.deleting_recipe_message".tr,
                              );
                              await ref
                                  .read(recipeControllerProvider.notifier)
                                  .deleteRecipe(recipe.id, recipe.image!);
                              navigatorContext.pop();
                            },
                          );
                        });
                      },
                    ),
                  ],
                );
              },
            ),
          ],
        ),
        body: Column(children: [CommonRecipeView(recipe: recipe)]),
      ),
      loading: () => Scaffold(
        appBar: AppBar(
          title: Text("recipe_screen.title".tr),
          leading: IconButton(
            onPressed: () {
              Navigator.of(context).pop();
            },
            icon: const Icon(CupertinoIcons.back),
          ),
        ),
        body: const Center(child: CircularProgressIndicator()),
      ),
      error: (error, stack) => Scaffold(
        appBar: AppBar(
          title: Text("recipe_screen.title".tr),
          leading: IconButton(
            onPressed: () {
              Navigator.of(context).pop();
            },
            icon: const Icon(CupertinoIcons.back),
          ),
        ),
        body: Center(child: Text('Error loading favorites: $error')),
      ),
    );
  }
}
