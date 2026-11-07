import "dart:convert";
import "dart:io";

import "package:hive_ce_flutter/adapters.dart";
import "package:material_ui/material_ui.dart";
import "package:path/path.dart" as path;
import "package:path_provider/path_provider.dart";
import "package:recipes/api/api_client.dart";
import "package:recipes/controllers/auth_controller.dart";
import "package:recipes/hive_boxes.dart";
import "package:recipes/messages.dart";
import "package:recipes/models/recipe.dart";
import "package:uuid/uuid.dart";

class SyncService {
  static final _uuid = Uuid();
  static final _api = ApiClient.instance;
  final AuthState _authState;

  SyncService(this._authState);

  Future<void> syncRecipesToCloud() async {
    if (!_authState.isLoggedIn) {
      throw Exception("sync_service.user_must_be_logged_in".tr);
    }

    final localBox = Hive.box<Recipe>(recipesBox);
    for (final recipe in localBox.values.toList()) {
      await _uploadRecipeToCloud(recipe);
    }
  }

  Future<void> syncRecipesFromCloud() async {
    if (!_authState.isLoggedIn) {
      throw Exception("sync_service.user_must_be_logged_in".tr);
    }

    final remoteRecipes = await _api.listRecipes();
    final localBox = Hive.box<Recipe>(recipesBox);
    final localRecipes = localBox.values.toList();

    for (final data in remoteRecipes) {
      final cloudId = data["cloudId"]?.toString() ?? data["id"]?.toString();
      if (cloudId == null || cloudId.isEmpty) continue;

      Recipe? existing;
      for (final local in localRecipes) {
        if (local.cloudId == cloudId) {
          existing = local;
          break;
        }
      }

      final remoteImage = data["image"]?.toString() ?? "";
      final localImage = existing?.image ?? "";
      final shouldDownloadImage =
          remoteImage.isNotEmpty &&
          (localImage.isEmpty || localImage.startsWith("http"));

      final imagePath = shouldDownloadImage
          ? await _downloadImage(remoteImage, cloudId)
          : localImage;

      if (existing != null) {
        final updated = Recipe(
          id: existing.id,
          title: data["title"] ?? "",
          description: data["description"] ?? "",
          image: imagePath,
          ingredients: List<String>.from(data["ingredients"] ?? []),
          directions: List<String>.from(data["directions"] ?? []),
          preparationTime: data["preparationTime"] ?? 0,
          ownerId: data["ownerId"] ?? "",
          isPublic: data["isPublic"] ?? false,
          cloudId: cloudId,
        );
        await localBox.put(existing.id, updated);
      } else {
        final newId = _uuid.v4();
        final newRecipe = Recipe(
          id: newId,
          title: data["title"] ?? "",
          description: data["description"] ?? "",
          image: imagePath,
          ingredients: List<String>.from(data["ingredients"] ?? []),
          directions: List<String>.from(data["directions"] ?? []),
          preparationTime: data["preparationTime"] ?? 0,
          ownerId: data["ownerId"] ?? "",
          isPublic: data["isPublic"] ?? false,
          cloudId: cloudId,
        );
        await localBox.put(newId, newRecipe);
      }
    }
  }

  Future<void> _uploadRecipeToCloud(Recipe recipe) async {
    final payload = await _recipePayload(recipe);
    Map<String, dynamic> response;

    try {
      if (recipe.cloudId != null && recipe.cloudId!.isNotEmpty) {
        response = await _api.updateRecipe(recipe.cloudId!, payload);
      } else {
        response = await _api.createRecipe(payload);
      }
    } on ApiException catch (e) {
      if (e.statusCode != 404 || recipe.cloudId == null) rethrow;
      response = await _api.createRecipe(payload);
    }

    final cloudId =
        response["cloudId"]?.toString() ?? response["id"]?.toString();
    if (cloudId == null || cloudId.isEmpty) return;

    final localBox = Hive.box<Recipe>(recipesBox);
    final updated = Recipe(
      id: recipe.id,
      title: recipe.title,
      description: recipe.description,
      image: recipe.image,
      ingredients: recipe.ingredients,
      directions: recipe.directions,
      preparationTime: recipe.preparationTime,
      ownerId: _authState.user!.id,
      isPublic: response["isPublic"] ?? recipe.isPublic,
      cloudId: cloudId,
    );
    await localBox.put(recipe.id, updated);
  }

  Future<Map<String, dynamic>> _recipePayload(Recipe recipe) async {
    final payload = <String, dynamic>{
      "title": recipe.title,
      "description": recipe.description,
      "ingredients": recipe.ingredients,
      "directions": recipe.directions,
      "preparationTime": recipe.preparationTime,
      "isPublic": recipe.isPublic ?? false,
    };

    final imagePath = recipe.image;
    if (imagePath != null &&
        imagePath.isNotEmpty &&
        !imagePath.startsWith("http")) {
      final file = File(imagePath);
      if (await file.exists()) {
        payload["image"] = base64Encode(await file.readAsBytes());
      }
    }
    return payload;
  }

  Future<void> fullSync() async {
    await syncRecipesToCloud();
    await syncRecipesFromCloud();
  }

  Future<void> deleteRecipeFromCloud(String recipeId) async {
    if (!_authState.isLoggedIn) {
      throw Exception("sync_service.user_must_be_logged_in".tr);
    }

    final localBox = Hive.box<Recipe>(recipesBox);
    final recipe = localBox.get(recipeId);
    if (recipe == null || recipe.cloudId == null) {
      debugPrint("Recipe not found locally or has no cloudId: $recipeId");
      return;
    }

    await _api.deleteRecipe(recipe.cloudId!);
  }

  Future<void> deleteAllUserRecipesFromCloud() async {
    if (!_authState.isLoggedIn) {
      throw Exception("sync_service.user_must_be_logged_in".tr);
    }

    final remoteRecipes = await _api.listRecipes();
    for (final recipe in remoteRecipes) {
      final id = recipe["cloudId"]?.toString() ?? recipe["id"]?.toString();
      if (id != null && id.isNotEmpty) {
        await _api.deleteRecipe(id);
      }
    }
  }

  static Future<void> clearLocalRecipesOnSignOut() async {
    try {
      final localBox = Hive.box<Recipe>(recipesBox);
      final recipesToRemove = <String>[];

      for (var recipe in localBox.values) {
        if (recipe.ownerId != null && recipe.ownerId!.isNotEmpty) {
          recipesToRemove.add(recipe.id);

          try {
            if (recipe.image != null &&
                recipe.image!.isNotEmpty &&
                !recipe.image!.startsWith("http")) {
              final imageFile = File(recipe.image!);
              if (await imageFile.exists()) {
                await imageFile.delete();
              }
            }
          } catch (e) {
            debugPrint("Error deleting local image: $e");
          }
        }
      }

      for (String recipeId in recipesToRemove) {
        await localBox.delete(recipeId);
      }

      debugPrint(
        "Cleared ${recipesToRemove.length} synced recipes from local storage",
      );
    } catch (e) {
      debugPrint("Error clearing local recipes on sign out: $e");
    }
  }

  static Future<String> downloadSharedRecipeImage(
    String cloudUrl,
    String recipeId,
  ) async {
    return await _downloadImage(cloudUrl, recipeId);
  }

  static Future<String> _downloadImage(String cloudUrl, String recipeId) async {
    try {
      final appDocDir = await getApplicationDocumentsDirectory();
      final imagesPath = path.join(appDocDir.path, "recipe-images");
      final imagesDir = Directory(imagesPath);

      if (!await imagesDir.exists()) {
        await imagesDir.create(recursive: true);
      }

      final fileName =
          "${recipeId}_${DateTime.now().millisecondsSinceEpoch}.jpg";
      final localPath = path.join(imagesPath, fileName);
      final bytes = await _api.download(cloudUrl);
      await File(localPath).writeAsBytes(bytes);
      return localPath;
    } catch (e) {
      debugPrint("Error downloading image: $e");
      return cloudUrl;
    }
  }

  Future<List<Recipe>> searchPublicRecipes(String query) async {
    final data = await _api.searchPublicRecipes(query);
    return data.map((map) => Recipe.fromMap(map)).toList();
  }
}
