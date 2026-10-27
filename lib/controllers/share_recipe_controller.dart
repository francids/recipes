import "package:appwrite/appwrite.dart";
import "package:flutter/foundation.dart";
import "package:flutter_riverpod/flutter_riverpod.dart";
import "package:the_recipes/appwrite_config.dart";
import "package:the_recipes/controllers/auth_controller.dart";
import "package:the_recipes/controllers/recipe_controller.dart";
import "package:the_recipes/models/recipe.dart";

class ShareRecipeState {
  final Map<String, bool> isLoadingMap;

  ShareRecipeState({this.isLoadingMap = const {}});

  ShareRecipeState copyWith({Map<String, bool>? isLoadingMap}) {
    return ShareRecipeState(isLoadingMap: isLoadingMap ?? this.isLoadingMap);
  }
}

class ShareRecipeController extends Notifier<ShareRecipeState> {
  final _tables = AppwriteConfig.tables;
  final _storage = AppwriteConfig.storage;

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
    _setLoading(recipeId, true);
    try {
      final recipes = ref.read(recipeControllerProvider).recipes;
      final recipe = recipes.where((r) => r.id == recipeId).firstOrNull;
      if (recipe == null || recipe.cloudId == null) {
        debugPrint("Recipe not found or has no cloudId: $recipeId");
        return;
      }

      final authState = ref.read(authControllerProvider);
      final cloudId = recipe.cloudId!;

      await _tables.updateRow(
        databaseId: AppwriteConfig.databaseId,
        tableId: AppwriteConfig.recipesTableId,
        rowId: cloudId,
        data: {"isPublic": true},
        permissions: [
          Permission.read(Role.any()),
          Permission.update(Role.user(authState.user!.$id)),
          Permission.delete(Role.user(authState.user!.$id)),
        ],
      );
      await _storage.updateFile(
        bucketId: AppwriteConfig.bucketId,
        fileId: cloudId,
        permissions: [
          Permission.read(Role.any()),
          Permission.update(Role.user(authState.user!.$id)),
          Permission.delete(Role.user(authState.user!.$id)),
        ],
      );
      await ref
          .read(recipeControllerProvider.notifier)
          .updateLocalRecipe(recipeId, isPublic: true);
    } catch (e) {
      debugPrint("Error making recipe public: $e");
    } finally {
      _setLoading(recipeId, false);
    }
  }

  Future<void> makeRecipePrivate(String recipeId) async {
    _setLoading(recipeId, true);
    try {
      final recipes = ref.read(recipeControllerProvider).recipes;
      final recipe = recipes.where((r) => r.id == recipeId).firstOrNull;
      if (recipe == null || recipe.cloudId == null) {
        debugPrint("Recipe not found or has no cloudId: $recipeId");
        return;
      }

      final authState = ref.read(authControllerProvider);
      final cloudId = recipe.cloudId!;

      await _tables.updateRow(
        databaseId: AppwriteConfig.databaseId,
        tableId: AppwriteConfig.recipesTableId,
        rowId: cloudId,
        data: {"isPublic": false},
        permissions: [
          Permission.read(Role.user(authState.user!.$id)),
          Permission.update(Role.user(authState.user!.$id)),
          Permission.delete(Role.user(authState.user!.$id)),
        ],
      );
      await _storage.updateFile(
        bucketId: AppwriteConfig.bucketId,
        fileId: cloudId,
        permissions: [
          Permission.read(Role.user(authState.user!.$id)),
          Permission.update(Role.user(authState.user!.$id)),
          Permission.delete(Role.user(authState.user!.$id)),
        ],
      );
      await ref
          .read(recipeControllerProvider.notifier)
          .updateLocalRecipe(recipeId, isPublic: false);
    } catch (e) {
      debugPrint("Error making recipe private: $e");
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
    try {
      final document = await _tables.getRow(
        databaseId: AppwriteConfig.databaseId,
        tableId: AppwriteConfig.recipesTableId,
        rowId: cloudId,
      );

      final recipeData = document.data;
      recipeData["image"] =
          "${AppwriteConfig.endpoint}/storage/buckets/${AppwriteConfig.bucketId}/files/$cloudId/view?project=${AppwriteConfig.projectId}";
      recipeData["cloudId"] = cloudId;

      return Recipe.fromMap(recipeData);
    } catch (e) {
      debugPrint("Error fetching shared recipe: $e");
      rethrow;
    }
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
