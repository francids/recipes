import "dart:convert";
import "dart:io";
import "dart:typed_data";
import "package:archive/archive_io.dart";
import "package:file_picker/file_picker.dart";
import "package:flutter/material.dart";
import "package:the_recipes/messages.dart";
import "package:the_recipes/models/recipe.dart";
import "package:the_recipes/views/widgets/ui_helpers.dart";

class ExportService {
  static Future<void> exportRecipes(
      List<Recipe> recipes, BuildContext context) async {
    if (recipes.isEmpty) {
      UIHelpers.showErrorSnackbar(
          "export_service.no_recipes_to_export".tr, context);
      return;
    }

    await _saveRecipes(recipes, context);
  }

  static Future<void> _saveRecipes(
      List<Recipe> recipes, BuildContext context) async {
    try {
      showDialog(
        context: context,
        barrierDismissible: false,
        builder: (context) => const Center(
          child: CircularProgressIndicator(),
        ),
      );

      final zipData = await _createExportArchive(recipes);

      Navigator.of(context).pop();

      String fileName =
          "recipes_export_${DateTime.now().millisecondsSinceEpoch}.zip";

      String? outputFile = await FilePicker.platform.saveFile(
        dialogTitle: "export_service.save_file".tr,
        type: FileType.custom,
        fileName: fileName,
        allowedExtensions: ["zip"],
        bytes: Uint8List.fromList(zipData),
      );

      if (outputFile == null) {
        UIHelpers.showErrorSnackbar(
            "export_service.save_cancelled".tr, context);
        return;
      }

      UIHelpers.showSuccessSnackbar(
          "export_service.saved_successfully".tr + "\n$outputFile", context);
    } catch (e) {
      Navigator.of(context).pop();
      UIHelpers.showErrorSnackbar("export_service.save_failed".tr, context);
      print("Error saving recipes: $e");
    }
  }

  static Future<List<int>> _createExportArchive(List<Recipe> recipes) async {
    final archive = Archive();

    List<Map<String, dynamic>> exportData = [];
    Set<String> addedImages = {};

    for (Recipe recipe in recipes) {
      String? imageFileName;

      if (recipe.image != null && recipe.image!.isNotEmpty) {
        File imageFile = File(recipe.image!);
        if (await imageFile.exists()) {
          String originalName = imageFile.path.split("/").last;
          imageFileName = "${recipe.id}_$originalName";

          if (!addedImages.contains(imageFileName)) {
            final imageBytes = await imageFile.readAsBytes();
            final imageArchiveFile = ArchiveFile(
              "images/$imageFileName",
              imageBytes.length,
              imageBytes,
            );
            archive.addFile(imageArchiveFile);
            addedImages.add(imageFileName);
          }
        }
      }

      exportData.add({
        "id": recipe.id,
        "title": recipe.title,
        "description": recipe.description,
        "ingredients": recipe.ingredients,
        "directions": recipe.directions,
        "preparationTime": recipe.preparationTime,
        "image": imageFileName ?? "",
        "exportDate": DateTime.now().toIso8601String(),
      });
    }

    Map<String, dynamic> exportJson = {
      "recipes": exportData,
      "exportDate": DateTime.now().toIso8601String(),
      "totalRecipes": recipes.length,
      "appVersion": "1.0.0",
      "format": "compressed_with_images",
    };

    String jsonString = const JsonEncoder.withIndent("  ").convert(exportJson);

    final jsonBytes = jsonString.codeUnits;
    final jsonArchiveFile = ArchiveFile(
      "recipes.json",
      jsonBytes.length,
      jsonBytes,
    );
    archive.addFile(jsonArchiveFile);

    final zipEncoder = ZipEncoder();
    return zipEncoder.encode(archive);
  }
}
