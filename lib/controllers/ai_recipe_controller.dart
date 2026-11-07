import "dart:convert";
import "dart:io";

import "package:flutter/foundation.dart";
import "package:flutter_image_compress/flutter_image_compress.dart";
import "package:flutter_riverpod/flutter_riverpod.dart";
import "package:intl/intl.dart";
import "package:recipes/api/api_client.dart";
import "package:recipes/messages.dart";
import "package:recipes/models/recipe.dart";
import "package:uuid/uuid.dart";

class AIRecipeController {
  final _api = ApiClient.instance;

  Future<Uint8List?> _compressImage(String imagePath) async {
    try {
      final compressedImage = await FlutterImageCompress.compressWithFile(
        imagePath,
        quality: 65,
        minWidth: 600,
        minHeight: 600,
      );
      return compressedImage;
    } catch (error) {
      debugPrint("Error compressing image: $error");
      return null;
    }
  }

  Future<Recipe> generateRecipeFromImage(String imagePath) async {
    try {
      final compressedImageBytes = await _compressImage(imagePath);
      final imageBytes =
          compressedImageBytes ?? await File(imagePath).readAsBytes();

      final recipeData = await _api.generateRecipe(
        imageBase64: base64Encode(imageBytes),
        language: Intl.getCurrentLocale().split('_')[0],
      );

      return Recipe(
        id: const Uuid().v4(),
        title: recipeData["title"] ?? "",
        description: recipeData["description"] ?? "",
        image: imagePath,
        ingredients: List<String>.from(recipeData["ingredients"] ?? []),
        directions: List<String>.from(recipeData["directions"] ?? []),
        preparationTime: recipeData["preparationTime"] ?? 0,
      );
    } on ApiException catch (e) {
      throw AIRecipeException(
        _getErrorMessage(e.statusCode, e.message),
        e.statusCode,
      );
    } catch (error) {
      if (error is AIRecipeException) {
        rethrow;
      }
      throw AIRecipeException("ai_errors.network_error".tr, 0);
    }
  }

  String _getErrorMessage(int statusCode, String serverMessage) {
    switch (statusCode) {
      case 401:
        return "ai_errors.authentication_failed".tr;
      case 429:
        return "ai_errors.rate_limit_exceeded".tr;
      case 400:
        if (serverMessage.contains("Image too large")) {
          return "ai_errors.image_too_large".tr;
        }
        return "ai_errors.invalid_request".tr;
      case 500:
      case 502:
        return "ai_errors.server_error".tr;
      default:
        return "ai_errors.unknown_error".tr;
    }
  }
}

class AIRecipeException implements Exception {
  final String message;
  final int statusCode;

  AIRecipeException(this.message, this.statusCode);

  @override
  String toString() => message;
}

final aiRecipeControllerProvider = Provider<AIRecipeController>((ref) {
  return AIRecipeController();
});
