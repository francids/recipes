import "package:cupertino_ui/cupertino_ui.dart";
import "package:flutter_riverpod/flutter_riverpod.dart";
import "package:material_ui/material_ui.dart";
import "package:the_recipes/controllers/recipe_controller.dart";
import "package:the_recipes/messages.dart";
import "package:the_recipes/views/screens/recipes_page.dart";
import "package:the_recipes/views/widgets/recipe_card.dart";

class SharedRecipesScreen extends ConsumerWidget {
  const SharedRecipesScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final recipeState = ref.watch(recipeControllerProvider);

    return Scaffold(
      appBar: AppBar(
        title: Text("shared_recipes_screen.title".tr),
        leading: IconButton(
          onPressed: () {
            Navigator.of(context).pop();
          },
          icon: const Icon(CupertinoIcons.back),
        ),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Builder(
              builder: (context) {
                final publicRecipes = recipeState.recipes
                    .where((recipe) => recipe.isPublic == true)
                    .toList();

                return publicRecipes.isEmpty
                    ? SizedBox(
                        width: MediaQuery.of(context).size.width,
                        height: MediaQuery.of(context).size.height - 200,
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Text(
                              "shared_recipes_screen.no_public_recipes".tr,
                              textAlign: TextAlign.center,
                            ),
                          ],
                        ),
                      )
                    : ListView.separated(
                        physics: const NeverScrollableScrollPhysics(),
                        shrinkWrap: true,
                        itemCount: publicRecipes.length,
                        itemBuilder: (context, index) {
                          return RecipeCard(
                            recipe: publicRecipes[index],
                            viewOption: ViewOption.list,
                          );
                        },
                        separatorBuilder: (context, index) =>
                            const SizedBox(height: 12),
                      );
              },
            ),
          ],
        ),
      ),
    );
  }
}
