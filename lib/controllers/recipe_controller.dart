import "dart:async";
import "dart:io";

import "package:flutter_easyloading/flutter_easyloading.dart";
import "package:flutter_riverpod/flutter_riverpod.dart";
import "package:hive_ce_flutter/adapters.dart";
import "package:material_ui/material_ui.dart";
import "package:the_recipes/controllers/auth_controller.dart";
import "package:the_recipes/hive_boxes.dart";
import "package:the_recipes/messages.dart";
import "package:the_recipes/models/recipe.dart";
import "package:the_recipes/services/sync_service.dart";
import "package:uuid/uuid.dart";

class RecipeState {
  final List<Recipe> recipes;
  final bool isLoading;

  RecipeState({required this.recipes, this.isLoading = false});

  RecipeState copyWith({List<Recipe>? recipes, bool? isLoading}) {
    return RecipeState(
      recipes: recipes ?? this.recipes,
      isLoading: isLoading ?? this.isLoading,
    );
  }
}

class RecipeController extends Notifier<RecipeState> {
  final uuid = Uuid();

  @override
  RecipeState build() {
    List<Recipe> initialRecipes = [];
    try {
      if (Hive.isBoxOpen(recipesBox)) {
        final box = Hive.box<Recipe>(recipesBox);
        initialRecipes = box.values.toList();
      }
    } catch (e) {
      debugPrint("Error loading initial recipes: $e");
    }

    Future.microtask(() => getRecipes());

    return RecipeState(recipes: initialRecipes);
  }

  void refreshRecipes() async {
    if (!state.isLoading) {
      getRecipes();
    }
  }

  void getRecipes() async {
    if (state.isLoading) return;

    try {
      state = state.copyWith(isLoading: true);

      // Solo mostrar loading si no hay recetas cargadas
      if (state.recipes.isEmpty) {
        unawaited(
          EasyLoading.show(status: "recipe_controller.loading_recipes".tr),
        );
      }

      if (!Hive.isBoxOpen(recipesBox)) {
        await Hive.openBox<Recipe>(recipesBox);
      }
      final box = Hive.box<Recipe>(recipesBox);

      state = state.copyWith(recipes: box.values.toList());
    } catch (e) {
      unawaited(
        EasyLoading.showError(
          "recipe_controller.load_error".trParams({"0": e.toString()}),
        ),
      );
    } finally {
      state = state.copyWith(isLoading: false);
      unawaited(EasyLoading.dismiss());
    }
  }

  Future<void> addRecipe(
    String title,
    String description,
    String imagePath,
    List<String> ingredients,
    List<String> directions,
    int preparationTime,
  ) async {
    try {
      final authState = ref.read(authControllerProvider);
      if (!Hive.isBoxOpen(recipesBox)) {
        await Hive.openBox<Recipe>(recipesBox);
      }
      final box = Hive.box<Recipe>(recipesBox);
      final String id = const Uuid().v4();

      final Recipe recipe = Recipe(
        id: id,
        title: title,
        description: description,
        image: imagePath,
        ingredients: ingredients,
        directions: directions,
        preparationTime: preparationTime,
        ownerId: authState.isLoggedIn ? authState.user!.$id : "",
        isPublic: false,
        cloudId: null,
      );

      await box.put(id, recipe);
      final currentRecipes = List<Recipe>.from(state.recipes);
      currentRecipes.add(recipe);
      state = state.copyWith(recipes: currentRecipes);

      if (authState.isLoggedIn && authState.autoSyncEnabled) {
        try {
          final syncService = SyncService(authState);
          await syncService.syncRecipesToCloud();
          getRecipes();
        } catch (e) {
          unawaited(
            EasyLoading.showError(
              "recipe_controller.sync_error".trParams({"0": e.toString()}),
            ),
          );
        }
      }
    } catch (e) {
      unawaited(
        EasyLoading.showError(
          "recipe_controller.add_error".trParams({"0": e.toString()}),
        ),
      );
    }
  }

  Future<void> addSharedRecipe(Recipe sharedRecipe) async {
    try {
      final authState = ref.read(authControllerProvider);
      if (!Hive.isBoxOpen(recipesBox)) {
        await Hive.openBox<Recipe>(recipesBox);
      }
      final box = Hive.box<Recipe>(recipesBox);
      final String id = const Uuid().v4();

      String localImagePath = sharedRecipe.image!;

      if (sharedRecipe.image!.isNotEmpty &&
          sharedRecipe.image!.startsWith("http")) {
        try {
          localImagePath = await SyncService.downloadSharedRecipeImage(
            sharedRecipe.image!,
            id,
          );
        } catch (e) {
          debugPrint("Error downloading shared recipe image: $e");
          localImagePath = sharedRecipe.image!;
        }
      }

      final Recipe recipe = Recipe(
        id: id,
        title: sharedRecipe.title,
        description: sharedRecipe.description,
        image: localImagePath,
        ingredients: sharedRecipe.ingredients,
        directions: sharedRecipe.directions,
        preparationTime: sharedRecipe.preparationTime,
        ownerId: authState.isLoggedIn ? authState.user!.$id : "",
        isPublic: false,
        cloudId: null,
      );

      await box.put(id, recipe);
      final currentRecipes = List<Recipe>.from(state.recipes);
      currentRecipes.add(recipe);
      state = state.copyWith(recipes: currentRecipes);

      if (authState.isLoggedIn && authState.autoSyncEnabled) {
        try {
          final syncService = SyncService(authState);
          await syncService.syncRecipesToCloud();
          getRecipes();
        } catch (e) {
          unawaited(
            EasyLoading.showError(
              "recipe_controller.sync_error".trParams({"0": e.toString()}),
            ),
          );
        }
      }
    } catch (e) {
      rethrow;
    }
  }

  Future<void> updateLocalRecipe(
    String id, {
    String? title,
    String? description,
    String? imagePath,
    List<String>? ingredients,
    List<String>? directions,
    int? preparationTime,
    bool? isPublic,
  }) async {
    try {
      if (!Hive.isBoxOpen(recipesBox)) {
        await Hive.openBox<Recipe>(recipesBox);
      }
      final box = Hive.box<Recipe>(recipesBox);

      if (box.containsKey(id)) {
        final Recipe recipe = box.get(id)!;

        recipe.title = title ?? recipe.title;
        recipe.description = description ?? recipe.description;
        recipe.image = imagePath ?? recipe.image;
        recipe.ingredients = ingredients ?? recipe.ingredients;
        recipe.directions = directions ?? recipe.directions;
        recipe.preparationTime = preparationTime ?? recipe.preparationTime;
        recipe.isPublic = isPublic ?? recipe.isPublic;

        await box.put(id, recipe);

        final currentRecipes = List<Recipe>.from(state.recipes);
        final int index = currentRecipes.indexWhere((r) => r.id == id);
        if (index != -1) {
          currentRecipes[index] = recipe;
          state = state.copyWith(recipes: currentRecipes);
        }
      } else {
        unawaited(
          EasyLoading.showError("recipe_controller.recipe_not_found".tr),
        );
        return;
      }
    } catch (e) {
      unawaited(
        EasyLoading.showError(
          "recipe_controller.update_error".trParams({"0": e.toString()}),
        ),
      );
    }
  }

  Future<void> deleteRecipe(String id, String image) async {
    try {
      final authState = ref.read(authControllerProvider);
      if (!Hive.isBoxOpen(recipesBox)) {
        await Hive.openBox<Recipe>(recipesBox);
      }
      final box = Hive.box<Recipe>(recipesBox);

      if (box.containsKey(id)) {
        final recipe = box.get(id)!;

        if (authState.isLoggedIn &&
            recipe.cloudId != null &&
            recipe.cloudId!.isNotEmpty) {
          try {
            final syncService = SyncService(authState);
            await syncService.deleteRecipeFromCloud(id);
          } catch (e) {
            debugPrint("Error deleting recipe from cloud: $e");
          }
        }

        await box.delete(id);
        final currentRecipes = List<Recipe>.from(state.recipes);
        currentRecipes.removeWhere((recipe) => recipe.id == id);
        state = state.copyWith(recipes: currentRecipes);
      } else {
        unawaited(
          EasyLoading.showError("recipe_controller.recipe_not_found".tr),
        );
        return;
      }

      if (image.isNotEmpty) {
        try {
          final File imageFile = File(image);
          if (await imageFile.exists()) {
            await imageFile.delete();
          }
        } catch (e) {
          unawaited(
            EasyLoading.showError(
              "recipe_controller.image_delete_error".trParams({
                "0": e.toString(),
              }),
            ),
          );
        }
      }
    } catch (e) {
      unawaited(
        EasyLoading.showError(
          "recipe_controller.delete_error".trParams({"0": e.toString()}),
        ),
      );
    }
  }

  List<Recipe> getAllRecipesForExport() {
    if (!Hive.isBoxOpen(recipesBox)) {
      Hive.openBox<Recipe>(recipesBox);
    }
    return Hive.box<Recipe>(recipesBox).values.toList();
  }
}

final recipeControllerProvider =
    NotifierProvider<RecipeController, RecipeState>(() {
      return RecipeController();
    });
