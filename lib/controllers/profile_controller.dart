import "package:file_picker/file_picker.dart";
import "package:flutter_riverpod/flutter_riverpod.dart";
import "package:hive_ce_flutter/hive_flutter.dart";
import "package:material_ui/material_ui.dart";
import "package:the_recipes/controllers/auth_controller.dart";
import "package:the_recipes/controllers/recipe_controller.dart";
import "package:the_recipes/hive_boxes.dart";
import "package:the_recipes/messages.dart";
import "package:the_recipes/models/recipe.dart";
import "package:the_recipes/services/export_service.dart";
import "package:the_recipes/services/import_service.dart";
import "package:the_recipes/services/sync_service.dart";
import "package:the_recipes/views/widgets/ui_helpers.dart";

class ProfileController {
  final Ref ref;

  ProfileController(this.ref);

  Future<void> handleAutoSyncToggle(bool enabled, BuildContext context) async {
    if (enabled) {
      await ref
          .read(authControllerProvider.notifier)
          .setAutoSyncEnabled(enabled);
      if (context.mounted) {
        UIHelpers.showLoadingDialog(
          context,
          "profile_page.syncing_recipes".tr,
          "profile_page.syncing_recipes_description".tr,
          lottieAsset: "assets/lottie/sync.json",
        );
      }

      try {
        await _assignOwnerIdToLocalRecipes();
        final authState = ref.read(authControllerProvider);
        final syncService = SyncService(authState);
        await syncService.fullSync();
        ref.read(recipeControllerProvider.notifier).refreshRecipes();
        if (context.mounted) {
          Navigator.of(context).pop();
        }

        if (context.mounted) {
          UIHelpers.showSuccessSnackbar(
            "profile_page.sync_success_message".tr,
            context,
          );
        }
      } catch (e) {
        if (context.mounted) {
          Navigator.of(context).pop();
        }
        await ref
            .read(authControllerProvider.notifier)
            .setAutoSyncEnabled(false);
        if (context.mounted) {
          UIHelpers.showErrorSnackbar(
            "profile_page.sync_error_message".trParams({"0": e.toString()}),
            context,
          );
        }
      }
    } else {
      await _handleSyncDisable(enabled, context);
    }
  }

  Future<void> _handleSyncDisable(bool enabled, BuildContext context) async {
    UIHelpers.showConfirmationDialog(
      context: context,
      title: "profile_page.disable_sync_confirmation_title".tr,
      message: "profile_page.disable_sync_confirmation_message".tr,
      lottieAsset: "assets/lottie/alert.json",
      confirmAction: () async {
        if (!context.mounted) return;

        await ref
            .read(authControllerProvider.notifier)
            .setAutoSyncEnabled(enabled);
        if (!context.mounted) return;

        try {
          UIHelpers.showLoadingDialog(
            context,
            "profile_page.deleting_cloud_recipes_title".tr,
            "profile_page.deleting_cloud_recipes_description".tr,
          );

          final authState = ref.read(authControllerProvider);
          final syncService = SyncService(authState);
          await syncService.deleteAllUserRecipesFromCloud();
          if (!context.mounted) return;
          Navigator.of(context).pop();

          UIHelpers.showSuccessSnackbar(
            "profile_page.auto_sync_disabled".tr,
            context,
          );
        } catch (e) {
          if (context.mounted) {
            Navigator.of(context).pop();
          }
          await ref
              .read(authControllerProvider.notifier)
              .setAutoSyncEnabled(true);
          if (context.mounted) {
            UIHelpers.showErrorSnackbar(
              "profile_page.delete_cloud_recipes_error".trParams({
                "0": e.toString(),
              }),
              context,
            );
          }
        }
      },
    );
  }

  Future<void> _assignOwnerIdToLocalRecipes() async {
    final authState = ref.read(authControllerProvider);
    if (!authState.isLoggedIn) return;

    try {
      final box = Hive.box<Recipe>(recipesBox);
      final recipesToUpdate = <String, Recipe>{};

      for (var recipe in box.values) {
        if (recipe.ownerId == null ||
            recipe.ownerId!.isEmpty ||
            recipe.ownerId != authState.user!.$id) {
          final updatedRecipe = Recipe(
            id: recipe.id,
            title: recipe.title,
            description: recipe.description,
            image: recipe.image,
            ingredients: recipe.ingredients,
            directions: recipe.directions,
            preparationTime: recipe.preparationTime,
            ownerId: authState.user!.$id,
            isPublic: recipe.isPublic,
            cloudId: recipe.cloudId,
          );
          recipesToUpdate[recipe.id] = updatedRecipe;
        }
      }

      for (var entry in recipesToUpdate.entries) {
        await box.put(entry.key, entry.value);
      }
    } catch (e) {
      debugPrint("Error assigning ownerId to local recipes: $e");
    }
  }

  void handleExportRecipes(BuildContext context, {bool fromSettings = false}) {
    UIHelpers.showConfirmationDialog(
      context: context,
      title: "profile_page.export_confirmation_title".tr,
      message: "profile_page.export_confirmation_message".tr,
      lottieAsset: "assets/lottie/save_file.json",
      confirmAction: () async {
        if (!fromSettings) {
          Navigator.of(context).pop();
        }
        final recipes = ref
            .read(recipeControllerProvider.notifier)
            .getAllRecipesForExport();
        await ExportService.exportRecipes(recipes, context);
      },
    );
  }

  void handleImportRecipes(BuildContext context, {bool fromSettings = false}) {
    UIHelpers.showConfirmationDialog(
      context: context,
      title: "profile_page.import_confirmation_title".tr,
      message: "profile_page.import_confirmation_message".tr,
      lottieAsset: "assets/lottie/load_file.json",
      confirmAction: () async {
        if (!fromSettings) {
          Navigator.of(context).pop();
        }
        await _performImport(context);
      },
    );
  }

  Future<void> _performImport(BuildContext context) async {
    try {
      final PlatformFile? file = await FilePicker.pickFile(
        type: FileType.custom,
        allowedExtensions: ["zip"],
      );

      if (file?.path != null) {
        final filePath = file!.path!;

        final authState = ref.read(authControllerProvider);
        final importService = ImportService(authState);
        if (!context.mounted) return;
        final importResult = await importService.importRecipes(
          filePath,
          context,
        );

        if (context.mounted) {
          UIHelpers.showSuccessSnackbar(
            "profile_page.import_success_message".trParams({
              "0": importResult.importedCount.toString(),
              "1": importResult.skippedCount.toString(),
            }),
            context,
          );
        }

        ref.read(recipeControllerProvider.notifier).refreshRecipes();

        if (context.mounted &&
            authState.isLoggedIn &&
            authState.autoSyncEnabled &&
            importResult.importedCount > 0) {
          await _syncImportedRecipes(context);
        }
      }
    } catch (e) {
      if (context.mounted) {
        UIHelpers.showErrorSnackbar(
          "profile_page.import_error_message".trParams({"0": e.toString()}),
          context,
        );
      }
    }
  }

  Future<void> _syncImportedRecipes(BuildContext context) async {
    try {
      if (context.mounted) {
        UIHelpers.showLoadingDialog(
          context,
          "profile_page.syncing_recipes".tr,
          "profile_page.syncing_recipes_description".tr,
        );
      }
      await _assignOwnerIdToImportedRecipes();
      final authState = ref.read(authControllerProvider);
      final syncService = SyncService(authState);
      await syncService.syncRecipesToCloud();
      if (context.mounted) {
        Navigator.of(context).pop();
      }
    } catch (e) {
      if (context.mounted) {
        UIHelpers.showErrorSnackbar(
          "profile_page.sync_error_message".trParams({"0": e.toString()}),
          context,
        );
      }
    }
  }

  Future<void> _assignOwnerIdToImportedRecipes() async {
    final authState = ref.read(authControllerProvider);
    if (!authState.isLoggedIn) return;

    try {
      final box = Hive.box<Recipe>(recipesBox);
      final recipesToUpdate = <String, Recipe>{};

      for (var recipe in box.values) {
        if (recipe.ownerId == null || recipe.ownerId!.isEmpty) {
          final updatedRecipe = Recipe(
            id: recipe.id,
            title: recipe.title,
            description: recipe.description,
            image: recipe.image,
            ingredients: recipe.ingredients,
            directions: recipe.directions,
            preparationTime: recipe.preparationTime,
            ownerId: authState.user!.$id,
            isPublic: recipe.isPublic,
            cloudId: recipe.cloudId,
          );
          recipesToUpdate[recipe.id] = updatedRecipe;
        }
      }

      for (var entry in recipesToUpdate.entries) {
        await box.put(entry.key, entry.value);
      }
    } catch (e) {
      debugPrint("Error assigning ownerId to imported recipes: $e");
    }
  }
}

final profileControllerProvider = Provider<ProfileController>((ref) {
  return ProfileController(ref);
});
