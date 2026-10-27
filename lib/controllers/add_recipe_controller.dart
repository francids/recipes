import "dart:io";

import "package:flutter_riverpod/flutter_riverpod.dart";
import "package:image_picker/image_picker.dart";
import "package:path/path.dart" as path;
import "package:path_provider/path_provider.dart";
import "package:the_recipes/controllers/recipe_controller.dart";

class AddRecipeState {
  final String title;
  final String description;
  final List<String> ingredientsList;
  final List<String> directionsList;
  final String fullPath;
  final int preparationTime; // in seconds
  final bool isRecipeGenerated;
  final String generationError;
  final String? fileName;
  final File? image;

  AddRecipeState({
    this.title = "",
    this.description = "",
    this.ingredientsList = const [],
    this.directionsList = const [],
    this.fullPath = "",
    this.preparationTime = 0,
    this.isRecipeGenerated = false,
    this.generationError = "",
    this.fileName,
    this.image,
  });

  AddRecipeState copyWith({
    String? title,
    String? description,
    List<String>? ingredientsList,
    List<String>? directionsList,
    String? fullPath,
    int? preparationTime,
    bool? isRecipeGenerated,
    String? generationError,
    String? fileName,
    File? image,
  }) {
    return AddRecipeState(
      title: title ?? this.title,
      description: description ?? this.description,
      ingredientsList: ingredientsList ?? this.ingredientsList,
      directionsList: directionsList ?? this.directionsList,
      fullPath: fullPath ?? this.fullPath,
      preparationTime: preparationTime ?? this.preparationTime,
      isRecipeGenerated: isRecipeGenerated ?? this.isRecipeGenerated,
      generationError: generationError ?? this.generationError,
      fileName: fileName ?? this.fileName,
      image: image ?? this.image,
    );
  }
}

class AddRecipeController extends Notifier<AddRecipeState> {
  @override
  AddRecipeState build() {
    return AddRecipeState();
  }

  void updateTitle(String title) {
    state = state.copyWith(title: title);
  }

  void updateDescription(String description) {
    state = state.copyWith(description: description);
  }

  void updateIngredients(List<String> ingredients) {
    state = state.copyWith(ingredientsList: ingredients);
  }

  void updateDirections(List<String> directions) {
    state = state.copyWith(directionsList: directions);
  }

  void updatePreparationTime(int time) {
    state = state.copyWith(preparationTime: time);
  }

  void setRecipeGenerated(bool generated) {
    state = state.copyWith(isRecipeGenerated: generated);
  }

  void setGenerationError(String error) {
    state = state.copyWith(generationError: error);
  }

  void setImagePath(XFile file) {
    final fileName = DateTime.now().toString().replaceAll(" ", "") + file.name;
    state = state.copyWith(
      fileName: fileName,
      fullPath: file.path,
      image: File(file.path),
    );
  }

  Future<void> saveImageLocally() async {
    if (state.image == null) return;
    final Directory appDocDir = await getApplicationDocumentsDirectory();
    final String imagesPath = path.join(appDocDir.path, "recipe-images");
    final Directory imagesDir = Directory(imagesPath);
    if (!await imagesDir.exists()) {
      await imagesDir.create(recursive: true);
    }
    final String finalImagePath = path.join(imagesPath, state.fileName!);
    await state.image!.copy(finalImagePath);
    state = state.copyWith(fullPath: finalImagePath);
  }

  Future<void> addRecipe() async {
    await saveImageLocally();

    await ref
        .read(recipeControllerProvider.notifier)
        .addRecipe(
          state.title,
          state.description,
          state.fullPath,
          List<String>.from(state.ingredientsList),
          List<String>.from(state.directionsList),
          state.preparationTime,
        );
  }

  void resetState() {
    state = AddRecipeState();
  }
}

final addRecipeControllerProvider =
    NotifierProvider<AddRecipeController, AddRecipeState>(() {
      return AddRecipeController();
    });
