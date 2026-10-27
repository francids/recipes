import "package:cupertino_ui/cupertino_ui.dart";
import "package:flutter_riverpod/flutter_riverpod.dart";
import "package:material_ui/material_ui.dart";
import "package:the_recipes/controllers/recipe_controller.dart";
import "package:the_recipes/controllers/share_recipe_controller.dart";
import "package:the_recipes/messages.dart";
import "package:the_recipes/models/recipe.dart";
import "package:the_recipes/views/widgets/common_recipe_view.dart";
import "package:the_recipes/views/widgets/ui_helpers.dart";

class SharedRecipeScreen extends ConsumerWidget {
  const SharedRecipeScreen({super.key, required this.recipe});

  final Recipe recipe;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final shareController = ref.watch(shareRecipeControllerProvider.notifier);

    final bool recipeExistsLocally = shareController.isRecipeAlreadySaved(
      recipe,
    );

    return Scaffold(
      appBar: AppBar(
        title: Text("shared_recipe.title".tr),
        leading: IconButton(
          onPressed: () {
            Navigator.of(context).pop();
          },
          icon: const Icon(CupertinoIcons.back),
        ),
        actions: [
          if (!recipeExistsLocally)
            IconButton(
              onPressed: () => _saveRecipeLocally(context, ref),
              icon: const Icon(CupertinoIcons.download_circle),
              tooltip: "shared_recipe.save_locally".tr,
            ),
          if (recipeExistsLocally)
            IconButton(
              onPressed: null,
              icon: const Icon(
                CupertinoIcons.checkmark_circle_fill,
                color: Colors.green,
              ),
              tooltip: "shared_recipe.already_saved".tr,
            ),
        ],
      ),
      body: Column(children: [CommonRecipeView(recipe: recipe)]),
    );
  }

  Future<void> _saveRecipeLocally(BuildContext context, WidgetRef ref) async {
    try {
      UIHelpers.showLoadingDialog(
        context,
        "shared_recipe.saving_title".tr,
        "shared_recipe.saving_message".tr,
        lottieAsset: "assets/lottie/save_file.json",
      );

      await ref.read(recipeControllerProvider.notifier).addSharedRecipe(recipe);

      if (!context.mounted) return;

      Navigator.of(context).pop();

      UIHelpers.showSuccessSnackbar("shared_recipe.save_success".tr, context);

      Navigator.of(context).pop();
    } catch (e) {
      if (!context.mounted) return;
      Navigator.of(context).pop();
      UIHelpers.showErrorSnackbar(
        "shared_recipe.save_error".trParams({"0": e.toString()}),
        context,
      );
    }
  }
}
