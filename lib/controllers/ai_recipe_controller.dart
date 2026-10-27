import "dart:convert";
import "dart:io";

import "package:flutter/foundation.dart";
import "package:flutter_image_compress/flutter_image_compress.dart";
import "package:flutter_riverpod/flutter_riverpod.dart";
import "package:intl/intl.dart";
import "package:the_recipes/appwrite_config.dart";
import "package:the_recipes/messages.dart";
import "package:the_recipes/models/recipe.dart";
import "package:uuid/uuid.dart";

class AIRecipeController {
  final functions = AppwriteConfig.functions;

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

      final base64Image = base64Encode(imageBytes);

      final body = {
        "image": base64Image,
        "language": Intl.getCurrentLocale().split('_')[0],
      };

      final execution = await functions.createExecution(
        functionId: "the-recipes-ai",
        body: jsonEncode(body),
        path: "/generate-recipe",
      );

      if (execution.responseStatusCode == 200) {
        final recipeData = jsonDecode(execution.responseBody);
        return Recipe(
          id: const Uuid().v4(),
          title: recipeData["title"] ?? "",
          description: recipeData["description"] ?? "",
          image: imagePath,
          ingredients: List<String>.from(recipeData["ingredients"] ?? []),
          directions: List<String>.from(recipeData["directions"] ?? []),
          preparationTime: recipeData["preparationTime"] ?? 0,
        );
      } else {
        final String errorMessage = _getErrorMessage(
          execution.responseStatusCode,
          execution.responseBody,
        );
        throw AIRecipeException(errorMessage, execution.responseStatusCode);
      }
    } catch (error) {
      if (error is AIRecipeException) {
        rethrow;
      }
      throw AIRecipeException("ai_errors.network_error".tr, 0);
    }
  }

  String _getErrorMessage(int? statusCode, dynamic responseBody) {
    switch (statusCode) {
      case 401:
        return "ai_errors.authentication_failed".tr;
      case 429:
        return "ai_errors.rate_limit_exceeded".tr;
      case 400:
        if (responseBody != null && responseBody['error'] != null) {
          final String error = responseBody['error'].toString();
          if (error.contains("Image too large")) {
            return "ai_errors.image_too_large".tr;
          }
        }
        return "ai_errors.invalid_request".tr;
      case 500:
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
