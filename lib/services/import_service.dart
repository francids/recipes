import "dart:convert";
import "dart:io";
import "package:archive/archive_io.dart";
import "package:flutter/material.dart";
import "package:hive_ce_flutter/adapters.dart";
import "package:path_provider/path_provider.dart";
import "package:the_recipes/controllers/auth_controller.dart";
import "package:the_recipes/hive_boxes.dart";
import "package:the_recipes/messages.dart";
import "package:the_recipes/models/recipe.dart";
import "package:path/path.dart" as path;
import "package:uuid/uuid.dart";
import "package:the_recipes/views/widgets/ui_helpers.dart";

class ImportService {
  static final Uuid _uuid = Uuid();
  final AuthState _authState;

  ImportService(this._authState);

  Future<ImportResult> importRecipes(
    String zipFilePath,
    BuildContext context,
  ) async {
    try {
      UIHelpers.showLoadingDialog(
        context,
        "import_service.processing_import".tr,
        "import_service.extracting_files".tr,
      );

      File zipFile = File(zipFilePath);
      if (!await zipFile.exists()) {
        Navigator.of(context).pop();
        throw ImportException("import_service.file_not_found".tr);
      }

      final bytes = await zipFile.readAsBytes();
      final archive = ZipDecoder().decodeBytes(bytes);

      ArchiveFile? jsonFile;
      Map<String, ArchiveFile> imageFiles = {};

      for (final file in archive) {
        if (file.name == "recipes.json") {
          jsonFile = file;
        } else if (file.name.startsWith("images/") && file.isFile) {
          imageFiles[file.name] = file;
        }
      }

      if (jsonFile == null) {
        Navigator.of(context).pop();
        throw ImportException("import_service.invalid_format".tr);
      }

      String jsonContent;
      try {
        jsonContent = utf8.decode(jsonFile.content as List<int>);
      } catch (e) {
        try {
          jsonContent = latin1.decode(jsonFile.content as List<int>);
        } catch (e2) {
          Navigator.of(context).pop();
          throw ImportException("import_service.encoding_error".tr);
        }
      }

      final Map<String, dynamic> exportData = jsonDecode(jsonContent);

      if (!exportData.containsKey("recipes") ||
          exportData["recipes"] is! List) {
        Navigator.of(context).pop();
        throw ImportException("import_service.invalid_format".tr);
      }

      List<dynamic> recipesData = exportData["recipes"];
      if (recipesData.isEmpty) {
        Navigator.of(context).pop();
        UIHelpers.showErrorSnackbar(
            "import_service.no_recipes_found".tr, context);
        return ImportResult(importedCount: 0, skippedCount: 0, errors: []);
      }

      final result = await _processRecipes(recipesData, imageFiles);
      Navigator.of(context).pop();
      return result;
    } catch (e) {
      Navigator.of(context).pop();
      if (e is ImportException) {
        UIHelpers.showErrorSnackbar(e.message, context);
        rethrow;
      }
      UIHelpers.showErrorSnackbar(
          "import_service.unexpected_error".trParams({"0": e.toString()}),
          context);
      throw ImportException(
          "import_service.unexpected_error".trParams({"0": e.toString()}));
    }
  }

  Future<ImportResult> _processRecipes(
    List<dynamic> recipesData,
    Map<String, ArchiveFile> imageFiles,
  ) async {
    Directory appDocDir = await getApplicationDocumentsDirectory();
    String imagesPath = path.join(appDocDir.path, "recipe-images");
    Directory imagesDir = Directory(imagesPath);
    if (!await imagesDir.exists()) {
      await imagesDir.create(recursive: true);
    }

    List<String> errors = [];
    int importedCount = 0;
    int skippedCount = 0;

    final recipeBox = Hive.box<Recipe>(recipesBox);

    for (final recipeData in recipesData) {
      try {
        if (recipeData is! Map<String, dynamic>) {
          errors.add("import_service.invalid_recipe_format".tr);
          continue;
        }

        String title = recipeData["title"] ?? "";
        bool recipeExists = recipeBox.values.any(
          (existingRecipe) =>
              existingRecipe.title.toLowerCase() == title.toLowerCase(),
        );

        if (recipeExists) {
          skippedCount++;
          continue;
        }

        String newId = _uuid.v4();
        String? newImagePath;

        String originalImageName = recipeData["image"] ?? "";
        if (originalImageName.isNotEmpty && imageFiles.isNotEmpty) {
          String imageKey = "images/$originalImageName";
          if (imageFiles.containsKey(imageKey)) {
            ArchiveFile imageFile = imageFiles[imageKey]!;

            String fileExtension = path.extension(originalImageName);
            String newImageName =
                "${newId}_${DateTime.now().millisecondsSinceEpoch}$fileExtension";
            newImagePath = path.join(imagesPath, newImageName);

            File imageDestination = File(newImagePath);
            await imageDestination.writeAsBytes(imageFile.content as List<int>);
          }
        }

        Recipe recipe = Recipe(
          id: newId,
          title: recipeData["title"] ?? "",
          description: recipeData["description"] ?? "",
          image: newImagePath ?? "",
          ingredients: List<String>.from(recipeData["ingredients"] ?? []),
          directions: List<String>.from(recipeData["directions"] ?? []),
          preparationTime: recipeData["preparationTime"] ?? 0,
          ownerId: _authState.isLoggedIn ? _authState.user!.$id : "",
          isPublic: false,
          cloudId: null,
        );

        if (recipe.title.isEmpty ||
            recipe.ingredients.isEmpty ||
            recipe.directions.isEmpty) {
          errors.add(
              "import_service.incomplete_recipe".trParams({"0": recipe.title}));
          continue;
        }

        await recipeBox.put(newId, recipe);
        importedCount++;
      } catch (e) {
        errors.add(
            "import_service.recipe_import_error".trParams({"0": e.toString()}));
      }
    }

    return ImportResult(
      importedCount: importedCount,
      skippedCount: skippedCount,
      errors: errors,
    );
  }
}

class ImportResult {
  final int importedCount;
  final int skippedCount;
  final List<String> errors;

  ImportResult({
    required this.importedCount,
    required this.skippedCount,
    required this.errors,
  });

  bool get hasErrors => errors.isNotEmpty;
  bool get hasImportedRecipes => importedCount > 0;
  int get totalProcessed => importedCount + skippedCount + errors.length;
}

class ImportException implements Exception {
  final String message;

  ImportException(this.message);

  @override
  String toString() => message;
}
