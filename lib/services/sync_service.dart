import "dart:io";
import "package:appwrite/appwrite.dart";
import "package:appwrite/models.dart" as models;
import "package:flutter/material.dart";
import "package:hive_ce_flutter/adapters.dart";
import "package:path_provider/path_provider.dart";
import "package:path/path.dart" as path;
import "package:http/http.dart" as http;
import "package:the_recipes/controllers/auth_controller.dart";
import "package:the_recipes/hive_boxes.dart";
import "package:the_recipes/messages.dart";
import "package:the_recipes/models/recipe.dart";
import "package:the_recipes/appwrite_config.dart";
import "package:uuid/uuid.dart";

class SyncService {
  static final _uuid = Uuid();
  static final _tables = AppwriteConfig.tables;
  static final _storage = AppwriteConfig.storage;
  final AuthState _authState;

  SyncService(this._authState);

  Future<void> syncRecipesToCloud() async {
    if (!_authState.isLoggedIn) {
      throw Exception("sync_service.user_must_be_logged_in".tr);
    }

    try {
      final localBox = Hive.box<Recipe>(recipesBox);
      final userRecipes = localBox.values
          .where((recipe) =>
              recipe.ownerId!.isEmpty || recipe.ownerId == _authState.user!.$id)
          .toList();

      for (Recipe recipe in userRecipes) {
        if (recipe.ownerId!.isEmpty) {
          final updatedRecipe = Recipe(
            id: recipe.id,
            title: recipe.title,
            description: recipe.description,
            image: recipe.image,
            ingredients: recipe.ingredients,
            directions: recipe.directions,
            preparationTime: recipe.preparationTime,
            ownerId: _authState.user!.$id,
            isPublic: recipe.isPublic,
            cloudId: recipe.cloudId,
          );
          await localBox.put(recipe.id, updatedRecipe);
          await _uploadRecipeToCloud(updatedRecipe);
        } else {
          await _uploadRecipeToCloud(recipe);
        }
      }
    } catch (e) {
      debugPrint("Error syncing recipes to cloud: $e");
      rethrow;
    }
  }

  Future<void> syncRecipesFromCloud() async {
    if (!_authState.isLoggedIn) {
      throw Exception("sync_service.user_must_be_logged_in".tr);
    }

    try {
      final documentList = await _tables.listRows(
        databaseId: AppwriteConfig.databaseId,
        tableId: AppwriteConfig.recipesTableId,
        queries: [
          Query.equal('ownerId', _authState.user!.$id),
        ],
      );

      final localBox = Hive.box<Recipe>(recipesBox);

      for (var doc in documentList.rows) {
        final recipeData = doc.data;
        recipeData["cloudId"] = doc.$id;

        Recipe? existingRecipe;
        for (var localRecipe in localBox.values) {
          if (localRecipe.cloudId == doc.$id) {
            existingRecipe = localRecipe;
            break;
          }
        }

        if (recipeData["image"] != null &&
            recipeData["image"].toString().isNotEmpty) {
          String imageValue = recipeData["image"].toString();

          if (imageValue.startsWith("http") || !imageValue.contains("/")) {
            String recipeId = recipeData["id"]?.toString() ?? doc.$id;
            recipeData["image"] = await _downloadImage(imageValue, recipeId);
          }
        }

        if (existingRecipe != null) {
          final updatedRecipe = Recipe(
            id: existingRecipe.id,
            title: recipeData["title"] ?? "",
            description: recipeData["description"] ?? "",
            image: recipeData["image"] ?? "",
            ingredients: List<String>.from(recipeData["ingredients"] ?? []),
            directions: List<String>.from(recipeData["directions"] ?? []),
            preparationTime: recipeData["preparationTime"] ?? 0,
            ownerId: recipeData["ownerId"] ?? "",
            isPublic: recipeData["isPublic"] ?? false,
            cloudId: recipeData["cloudId"],
          );
          await localBox.put(existingRecipe.id, updatedRecipe);
        } else {
          String newLocalId = _uuid.v4();
          final newRecipe = Recipe(
            id: newLocalId,
            title: recipeData["title"] ?? "",
            description: recipeData["description"] ?? "",
            image: recipeData["image"] ?? "",
            ingredients: List<String>.from(recipeData["ingredients"] ?? []),
            directions: List<String>.from(recipeData["directions"] ?? []),
            preparationTime: recipeData["preparationTime"] ?? 0,
            ownerId: recipeData["ownerId"] ?? "",
            isPublic: recipeData["isPublic"] ?? false,
            cloudId: recipeData["cloudId"],
          );
          await localBox.put(newLocalId, newRecipe);
        }
      }
    } catch (e) {
      debugPrint("Error syncing recipes from cloud: $e");
      rethrow;
    }
  }

  Future<void> _uploadRecipeToCloud(Recipe recipe) async {
    try {
      if (recipe.ownerId == null || recipe.ownerId!.isEmpty) {
        throw Exception("Recipe must have an owner ID to sync to cloud");
      }

      String cloudId = recipe.cloudId ?? _uuid.v4();
      String? cloudImageUrl;
      models.Row? existingDoc;

      try {
        existingDoc = await _tables.getRow(
          databaseId: AppwriteConfig.databaseId,
          tableId: AppwriteConfig.recipesTableId,
          rowId: cloudId,
        );
      } on AppwriteException catch (e) {
        if (e.type == "user_unauthorized") {
          debugPrint("User is not authorized: $e");
        }
        if (e.type != "document_not_found") {
          debugPrint("Error getting existing document: $e");
        }
        existingDoc = null;
      } catch (e) {
        existingDoc = null;
      }

      if (recipe.image != null &&
          recipe.image!.isNotEmpty &&
          !recipe.image!.startsWith("http")) {
        if (existingDoc != null) {
          final existingImage = existingDoc.data["image"] as String?;
          if (existingImage != null && existingImage.isNotEmpty) {
            if (existingImage != recipe.image) {
              try {
                if (existingImage.startsWith("http")) {
                  final uri = Uri.parse(existingImage);
                  if (uri.pathSegments.length >= 3) {
                    final fileId = uri.pathSegments[3];
                    await _storage.deleteFile(
                      bucketId: AppwriteConfig.bucketId,
                      fileId: fileId,
                    );
                  }
                } else {
                  await _storage.deleteFile(
                    bucketId: AppwriteConfig.bucketId,
                    fileId: existingImage,
                  );
                }
              } catch (e) {
                debugPrint("Error deleting old image: $e");
              }

              cloudImageUrl = await _uploadImage(
                  recipe.image!, cloudId, recipe.isPublic ?? false);
            } else {
              cloudImageUrl = existingImage;
            }
          } else {
            cloudImageUrl = await _uploadImage(
                recipe.image!, cloudId, recipe.isPublic ?? false);
          }
        } else {
          cloudImageUrl = await _uploadImage(
              recipe.image!, cloudId, recipe.isPublic ?? false);
        }
      }

      final recipeData = recipe.toMap();
      if (cloudImageUrl != null) {
        recipeData["image"] = cloudImageUrl;
      }
      recipeData.remove("id");
      recipeData.remove("cloudId");

      final bool isPublic = recipe.isPublic ?? false;

      if (existingDoc != null) {
        await _tables.updateRow(
          databaseId: AppwriteConfig.databaseId,
          tableId: AppwriteConfig.recipesTableId,
          rowId: cloudId,
          data: recipeData,
          permissions: [
            Permission.read(
              isPublic ? Role.any() : Role.user(_authState.user!.$id),
            ),
            Permission.update(Role.user(_authState.user!.$id)),
            Permission.delete(Role.user(_authState.user!.$id)),
          ],
        );
      } else {
        try {
          await _tables.createRow(
            databaseId: AppwriteConfig.databaseId,
            tableId: AppwriteConfig.recipesTableId,
            rowId: cloudId,
            data: recipeData,
            permissions: [
              Permission.read(
                isPublic ? Role.any() : Role.user(_authState.user!.$id),
              ),
              Permission.update(Role.user(_authState.user!.$id)),
              Permission.delete(Role.user(_authState.user!.$id)),
            ],
          );

          final localBox = Hive.box<Recipe>(recipesBox);
          final updatedRecipe = Recipe(
            id: recipe.id,
            title: recipe.title,
            description: recipe.description,
            image: recipe.image,
            ingredients: recipe.ingredients,
            directions: recipe.directions,
            preparationTime: recipe.preparationTime,
            ownerId: recipe.ownerId,
            isPublic: recipe.isPublic,
            cloudId: cloudId,
          );
          await localBox.put(recipe.id, updatedRecipe);
          debugPrint("Updated recipe ${recipe.id} with cloudId: $cloudId");
        } catch (e) {
          if (e.toString().contains('document_already_exists')) {
            await _tables.updateRow(
              databaseId: AppwriteConfig.databaseId,
              tableId: AppwriteConfig.recipesTableId,
              rowId: cloudId,
              data: recipeData,
              permissions: [
                Permission.read(
                  isPublic ? Role.any() : Role.user(_authState.user!.$id),
                ),
                Permission.update(Role.user(_authState.user!.$id)),
                Permission.delete(Role.user(_authState.user!.$id)),
              ],
            );
          } else {
            rethrow;
          }
        }
      }
    } catch (e) {
      debugPrint("Error uploading recipe ${recipe.id}: $e");
      rethrow;
    }
  }

  Future<String> _uploadImage(
    String localPath,
    String recipeId,
    bool isPublic,
  ) async {
    try {
      final File imageFile = File(localPath);
      if (!await imageFile.exists()) {
        throw Exception("sync_service.image_file_does_not_exist".trParams({
          "0": localPath,
        }));
      }

      final String fileName = path.basename(localPath);

      try {
        await _storage.deleteFile(
          bucketId: AppwriteConfig.bucketId,
          fileId: recipeId,
        );
        debugPrint("Deleted existing file with ID: $recipeId");
      } catch (e) {
        debugPrint("No existing file to delete with ID: $recipeId");
      }

      final result = await _storage.createFile(
        bucketId: AppwriteConfig.bucketId,
        fileId: recipeId,
        file: InputFile.fromPath(path: localPath, filename: fileName),
        permissions: [
          Permission.read(
            isPublic ? Role.any() : Role.user(_authState.user!.$id),
          ),
          Permission.update(Role.user(_authState.user!.$id)),
          Permission.delete(Role.user(_authState.user!.$id)),
        ],
      );

      return result.$id;
    } catch (e) {
      debugPrint("Error uploading image: $e");
      rethrow;
    }
  }

  static Future<String> _downloadImage(String cloudUrl, String recipeId) async {
    try {
      Directory appDocDir = await getApplicationDocumentsDirectory();
      String imagesPath = path.join(appDocDir.path, "recipe-images");
      Directory imagesDir = Directory(imagesPath);

      if (!await imagesDir.exists()) {
        await imagesDir.create(recursive: true);
      }

      String fileName =
          "${recipeId}_${DateTime.now().millisecondsSinceEpoch}.jpg";
      String localPath = path.join(imagesPath, fileName);

      if (cloudUrl.startsWith("http")) {
        final response = await http.get(Uri.parse(cloudUrl));
        if (response.statusCode == 200) {
          final File imageFile = File(localPath);
          await imageFile.writeAsBytes(response.bodyBytes);
          return localPath;
        } else {
          throw Exception("sync_service.failed_to_download_image".trParams({
            "0": cloudUrl,
            "1": response.statusCode.toString(),
          }));
        }
      } else {
        final bytes = await _storage.getFileDownload(
          bucketId: AppwriteConfig.bucketId,
          fileId: cloudUrl,
        );

        final File imageFile = File(localPath);
        await imageFile.writeAsBytes(bytes);
        return localPath;
      }
    } catch (e) {
      debugPrint("Error downloading image: $e");
      return cloudUrl;
    }
  }

  Future<void> fullSync() async {
    try {
      await syncRecipesToCloud();
      await syncRecipesFromCloud();
    } catch (e) {
      debugPrint("Error in full sync: $e");
      rethrow;
    }
  }

  Future<void> deleteRecipeFromCloud(String recipeId) async {
    if (!_authState.isLoggedIn) {
      throw Exception("sync_service.user_must_be_logged_in".tr);
    }

    try {
      final localBox = Hive.box<Recipe>(recipesBox);
      final recipe = localBox.get(recipeId);

      if (recipe == null || recipe.cloudId == null) {
        debugPrint("Recipe not found locally or has no cloudId: $recipeId");
        return;
      }

      final cloudId = recipe.cloudId!;

      try {
        final doc = await _tables.getRow(
          databaseId: AppwriteConfig.databaseId,
          tableId: AppwriteConfig.recipesTableId,
          rowId: cloudId,
        );

        final imageUrl = doc.data["image"] as String?;
        if (imageUrl != null && imageUrl.isNotEmpty) {
          try {
            if (imageUrl.startsWith("http")) {
              final uri = Uri.parse(imageUrl);
              if (uri.pathSegments.length >= 3) {
                final fileId = uri.pathSegments[3];
                await _storage.deleteFile(
                  bucketId: AppwriteConfig.bucketId,
                  fileId: fileId,
                );
                debugPrint("Image deleted from Appwrite Storage: $imageUrl");
              }
            } else {
              await _storage.deleteFile(
                bucketId: AppwriteConfig.bucketId,
                fileId: imageUrl,
              );
              debugPrint("Image deleted from Appwrite Storage: $imageUrl");
            }
          } catch (e) {
            debugPrint("Error deleting image from Appwrite Storage: $e");
          }
        }
      } catch (e) {
        debugPrint("Error getting document before deletion: $e");
      }

      await _tables.deleteRow(
        databaseId: AppwriteConfig.databaseId,
        tableId: AppwriteConfig.recipesTableId,
        rowId: cloudId,
      );
      debugPrint("Recipe document deleted from Appwrite: $cloudId");
    } catch (e) {
      debugPrint("Error deleting recipe from cloud: $e");
      rethrow;
    }
  }

  Future<void> deleteAllUserRecipesFromCloud() async {
    if (!_authState.isLoggedIn) {
      throw Exception("sync_service.user_must_be_logged_in".tr);
    }

    try {
      final documentList = await _tables.listRows(
        databaseId: AppwriteConfig.databaseId,
        tableId: AppwriteConfig.recipesTableId,
        queries: [
          Query.equal("ownerId", _authState.user!.$id),
        ],
      );

      for (var doc in documentList.rows) {
        final imageUrl = doc.data["image"] as String?;

        if (imageUrl != null && imageUrl.isNotEmpty) {
          try {
            if (imageUrl.startsWith("http")) {
              final uri = Uri.parse(imageUrl);
              if (uri.pathSegments.length >= 3) {
                final fileId = uri.pathSegments[3];
                await _storage.deleteFile(
                  bucketId: AppwriteConfig.bucketId,
                  fileId: fileId,
                );
                debugPrint(
                  "Image deleted from Appwrite Storage during mass delete: $imageUrl",
                );
              }
            } else {
              await _storage.deleteFile(
                bucketId: AppwriteConfig.bucketId,
                fileId: imageUrl,
              );
              debugPrint(
                "Image deleted from Appwrite Storage during mass delete: $imageUrl",
              );
            }
          } catch (e) {
            debugPrint(
              "Error deleting image from Appwrite Storage during mass delete: $e",
            );
          }
        }

        await _tables.deleteRow(
          databaseId: AppwriteConfig.databaseId,
          tableId: AppwriteConfig.recipesTableId,
          rowId: doc.$id,
        );
        debugPrint(
          "Recipe document deleted from Appwrite during mass delete: ${doc.$id}",
        );
      }
    } catch (e) {
      debugPrint("Error deleting all user recipes from cloud: $e");
      rethrow;
    }
  }

  static Future<void> ensureRecipesHaveCloudIds() async {
    try {
      final localBox = Hive.box<Recipe>(recipesBox);
      bool hasChanges = false;

      for (var recipe in localBox.values) {
        if (recipe.cloudId == null) {
          final updatedRecipe = Recipe(
            id: recipe.id,
            title: recipe.title,
            description: recipe.description,
            image: recipe.image,
            ingredients: recipe.ingredients,
            directions: recipe.directions,
            preparationTime: recipe.preparationTime,
            ownerId: recipe.ownerId,
            isPublic: recipe.isPublic,
            cloudId: _uuid.v4(),
          );
          await localBox.put(recipe.id, updatedRecipe);
          hasChanges = true;
          debugPrint("Assigned cloudId to recipe: ${recipe.id}");
        }
      }

      if (hasChanges) {
        debugPrint(
            "Updated ${localBox.values.where((r) => r.cloudId != null).length} recipes with cloudIds");
      }
    } catch (e) {
      debugPrint("Error ensuring recipes have cloudIds: $e");
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
            if (recipe.image != null && recipe.image!.isNotEmpty) {
              final File imageFile = File(recipe.image!);
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
          "Cleared ${recipesToRemove.length} synced recipes from local storage");
    } catch (e) {
      debugPrint("Error clearing local recipes on sign out: $e");
    }
  }

  static Future<String> downloadSharedRecipeImage(
      String cloudUrl, String recipeId) async {
    return await _downloadImage(cloudUrl, recipeId);
  }

  String _getStorageViewUrl(String fileId) {
    return "${AppwriteConfig.endpoint}/storage/buckets/${AppwriteConfig.bucketId}/files/$fileId/view?project=${AppwriteConfig.projectId}";
  }

  Future<List<Recipe>> searchPublicRecipes(String query) async {
    try {
      final List<String> queries = [
        Query.equal("isPublic", true),
        Query.limit(20),
      ];

      if (query.isNotEmpty) {
        queries.add(Query.search("title", query));
      } else {
        queries.add(Query.orderDesc("\$createdAt"));
      }

      final documentList = await _tables.listRows(
        databaseId: AppwriteConfig.databaseId,
        tableId: AppwriteConfig.recipesTableId,
        queries: queries,
      );

      final recipes = <Recipe>[];

      for (var doc in documentList.rows) {
        final recipeData = doc.data;
        recipeData["cloudId"] = doc.$id;

        if (_authState.isLoggedIn &&
            recipeData["ownerId"] == _authState.user!.$id) {
          continue;
        }

        if (recipeData["image"] != null &&
            recipeData["image"].toString().isNotEmpty) {
          String imageValue = recipeData["image"].toString();
          if (!imageValue.startsWith("http") && !imageValue.contains("/")) {
            recipeData["image"] = _getStorageViewUrl(imageValue);
          }
        }

        recipes.add(Recipe(
          id: const Uuid().v4(),
          title: recipeData["title"] ?? "",
          description: recipeData["description"] ?? "",
          image: recipeData["image"] ?? "",
          ingredients: List<String>.from(recipeData["ingredients"] ?? []),
          directions: List<String>.from(recipeData["directions"] ?? []),
          preparationTime: recipeData["preparationTime"] ?? 0,
          ownerId: recipeData["ownerId"] ?? "",
          isPublic: true,
          cloudId: recipeData["cloudId"],
        ));
      }

      return recipes;
    } catch (e) {
      debugPrint("Error searching public recipes: \$e");
      rethrow;
    }
  }
}
