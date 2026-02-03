import "dart:async";

import "package:flutter/cupertino.dart";
import "package:flutter/material.dart";
import "package:flutter_riverpod/flutter_riverpod.dart";
import "package:the_recipes/controllers/public_recipes_search_controller.dart";
import "package:the_recipes/messages.dart";
import "package:the_recipes/views/screens/recipes_page.dart";
import "package:the_recipes/views/screens/shared_recipe_screen.dart";
import "package:the_recipes/views/widgets/form_field.dart";
import "package:the_recipes/views/widgets/recipe_card.dart";

class PublicRecipesSearchScreen extends ConsumerStatefulWidget {
  const PublicRecipesSearchScreen({super.key});

  @override
  ConsumerState<PublicRecipesSearchScreen> createState() =>
      _PublicRecipesSearchScreenState();
}

class _PublicRecipesSearchScreenState
    extends ConsumerState<PublicRecipesSearchScreen> {
  final TextEditingController _searchController = TextEditingController();
  Timer? _debounceTimer;

  @override
  void initState() {
    super.initState();
    _searchController.addListener(() => setState(() {}));
  }

  @override
  void dispose() {
    _debounceTimer?.cancel();
    _searchController.dispose();
    super.dispose();
  }

  void _onSearchChanged(String value) {
    _debounceTimer?.cancel();
    _debounceTimer = Timer(const Duration(milliseconds: 300), () {
      ref
          .read(publicRecipesSearchControllerProvider.notifier)
          .searchRecipes(value);
    });
  }

  @override
  Widget build(BuildContext context) {
    final searchState = ref.watch(publicRecipesSearchControllerProvider);

    return Scaffold(
      appBar: AppBar(
        leading: IconButton(
          onPressed: () {
            Navigator.of(context).pop();
          },
          icon: const Icon(CupertinoIcons.back),
        ),
        title: ModernFormField(
          hintText: "public_recipes_search.search_hint".tr,
          controller: _searchController,
          autofocus: true,
          onChanged: _onSearchChanged,
        ),
      ),
      body: Column(
        children: [
          Expanded(
            child: switch (searchState) {
              _ when searchState.isLoading =>
                const Center(child: CircularProgressIndicator()),
              _ when searchState.error != null => Center(
                  child: Padding(
                    padding: const EdgeInsets.all(16.0),
                    child: Text(
                      "public_recipes_search.error"
                          .trParams({"0": searchState.error!}),
                      textAlign: TextAlign.center,
                    ),
                  ),
                ),
              _ when searchState.recipes.isEmpty => Center(
                  child: Text(
                    "public_recipes_search.no_recipes".tr,
                    textAlign: TextAlign.center,
                  ),
                ),
              _ => ListView.separated(
                  padding: const EdgeInsets.all(16),
                  itemCount: searchState.recipes.length,
                  separatorBuilder: (context, index) =>
                      const SizedBox(height: 12),
                  itemBuilder: (context, index) {
                    final recipe = searchState.recipes[index];
                    return RecipeCard(
                      recipe: recipe,
                      viewOption: ViewOption.list,
                      onTap: () => Navigator.of(context).push(
                        MaterialPageRoute(
                          builder: (context) =>
                              SharedRecipeScreen(recipe: recipe),
                        ),
                      ),
                    );
                  },
                ),
            },
          ),
        ],
      ),
    );
  }
}
