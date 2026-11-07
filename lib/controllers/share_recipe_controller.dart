import "package:flutter/foundation.dart";
import "package:flutter_riverpod/flutter_riverpod.dart";
import "package:recipes/api/api_client.dart";
import "package:recipes/controllers/auth_controller.dart";
import "package:recipes/controllers/recipe_controller.dart";
import "package:recipes/models/recipe.dart";

class ShareRecipeState {
  final Map<String, bool> isLoadingMap;

  ShareRecipeState({this.isLoadingMap = const {}});

  ShareRecipeState copyWith({Map<String, bool>? isLoadingMap}) {
    return ShareRecipeState(isLoadingMap: isLoadingMap ?? this.isLoadingMap);
  }
}

class ShareRecipeController extends Notifier<ShareRecipeState> {
  final _api = ApiClient.instance;

  @override
  ShareRecipeState build() {
    return ShareRecipeState();
  }

  bool isPublic(String recipeId) {
    final recipes = ref.read(recipeControllerProvider).recipes;
    final recipe = recipes.where((r) => r.id == recipeId).firstOrNull;
    return recipe?.isPublic ?? false;
  }

  bool isLoading(String recipeId) {
    return state.isLoadingMap[recipeId] ?? false;
  }

  final String shareUrlTemplate = "https://recipes.francids.com/sharing?id=";

  void _setLoading(String recipeId, bool loading) {
    final newMap = Map<String, bool>.from(state.isLoadingMap);
    newMap[recipeId] = loading;
    state = state.copyWith(isLoadingMap: newMap);
  }

  Future<void> makeRecipePublic(String recipeId) async {
    await _setVisibility(recipeId, true);
  }

  Future<void> makeRecipePrivate(String recipeId) async {
    await _setVisibility(recipeId, false);
  }

  Future<void> _setVisibility(String recipeId, bool isPublic) async {
    _setLoading(recipeId, true);
    try {
      final recipes = ref.read(recipeControllerProvider).recipes;
      final recipe = recipes.where((r) => r.id == recipeId).firstOrNull;
      if (recipe == null || recipe.cloudId == null) {
        debugPrint("Recipe not found or has no cloudId: $recipeId");
        return;
      }

      if (!ref.read(authControllerProvider).isLoggedIn) {
        debugPrint("A signed-in user is required to share recipes");
        return;
      }

      await _api.updateRecipe(recipe.cloudId!, {"isPublic": isPublic});
      await ref
          .read(recipeControllerProvider.notifier)
          .updateLocalRecipe(recipeId, isPublic: isPublic);
    } catch (e) {
      debugPrint("Error updating recipe visibility: $e");
    } finally {
      _setLoading(recipeId, false);
    }
  }

  String generateShareUrl(String recipeId) {
    final authState = ref.read(authControllerProvider);
    if (!authState.isLoggedIn) return "";

    final recipes = ref.read(recipeControllerProvider).recipes;
    final recipe = recipes.where((r) => r.id == recipeId).firstOrNull;
    if (recipe == null || recipe.cloudId == null) {
      debugPrint("Recipe not found or has no cloudId: $recipeId");
      return "";
    }

    return "$shareUrlTemplate${recipe.cloudId}";
  }

  Future<Recipe> getSharedRecipe(String cloudId) async {
    final data = await _api.getPublicRecipe(cloudId);
    data["cloudId"] = cloudId;
    return Recipe.fromMap(data);
  }

  String? getCloudId(String recipeId) {
    final recipes = ref.read(recipeControllerProvider).recipes;
    final recipe = recipes.where((r) => r.id == recipeId).firstOrNull;
    return recipe?.cloudId;
  }

  bool canShare(String recipeId) {
    return getCloudId(recipeId) != null;
  }

  Recipe? findLocalRecipeByCloudId(String cloudId) {
    final recipes = ref.read(recipeControllerProvider).recipes;
    return recipes.where((recipe) => recipe.cloudId == cloudId).firstOrNull;
  }

  Recipe? findLocalRecipeByTitle(String title) {
    final recipes = ref.read(recipeControllerProvider).recipes;
    final exactMatch = recipes
        .where((recipe) => recipe.title.toLowerCase() == title.toLowerCase())
        .firstOrNull;

    if (exactMatch != null) return exactMatch;

    return recipes
        .where(
          (recipe) =>
              recipe.title.toLowerCase().contains(title.toLowerCase()) ||
              title.toLowerCase().contains(recipe.title.toLowerCase()),
        )
        .firstOrNull;
  }

  bool isRecipeAlreadySaved(Recipe sharedRecipe) {
    if (sharedRecipe.cloudId != null) {
      final byCloudId = findLocalRecipeByCloudId(sharedRecipe.cloudId!);
      if (byCloudId != null) return true;
    }

    final byTitle = findLocalRecipeByTitle(sharedRecipe.title);
    if (byTitle != null) {
      final sameIngredients =
          byTitle.ingredients.length == sharedRecipe.ingredients.length &&
          byTitle.ingredients.every(
            (ingredient) => sharedRecipe.ingredients.any(
              (sharedIngredient) =>
                  ingredient.toLowerCase() == sharedIngredient.toLowerCase(),
            ),
          );

      if (sameIngredients) return true;
    }

    return false;
  }
}

final shareRecipeControllerProvider =
    NotifierProvider<ShareRecipeController, ShareRecipeState>(() {
      return ShareRecipeController();
    });
