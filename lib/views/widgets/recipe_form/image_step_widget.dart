import "dart:io";

import "package:cupertino_ui/cupertino_ui.dart";
import "package:flutter_animate/flutter_animate.dart";
import "package:flutter_riverpod/flutter_riverpod.dart";
import "package:image_picker/image_picker.dart";
import "package:material_ui/material_ui.dart";
import "package:the_recipes/controllers/add_recipe_controller.dart";
import "package:the_recipes/controllers/ai_recipe_controller.dart";
import "package:the_recipes/controllers/auth_controller.dart";
import "package:the_recipes/messages.dart";
import "package:the_recipes/views/widgets/pressable_button.dart";
import "package:the_recipes/views/widgets/ui_helpers.dart";

class ImageStepWidget extends ConsumerWidget {
  final ImagePicker _imagePicker = ImagePicker();
  final VoidCallback? onImageSelected;

  ImageStepWidget({super.key, this.onImageSelected});

  Future<void> _pickImage(WidgetRef ref) async {
    final XFile? image = await _imagePicker.pickImage(
      source: ImageSource.gallery,
    );
    if (image != null) {
      ref.read(addRecipeControllerProvider.notifier).setImagePath(image);
      onImageSelected?.call();
    }
  }

  void _generateRecipeFromImage(BuildContext context, WidgetRef ref) async {
    UIHelpers.showLoadingDialog(
      context,
      "image_step.analyzing_image_title".tr,
      "image_step.analyzing_image_message".tr,
    );

    try {
      final aiController = ref.read(aiRecipeControllerProvider);
      final state = ref.read(addRecipeControllerProvider);
      final recipe = await aiController.generateRecipeFromImage(state.fullPath);

      ref.read(addRecipeControllerProvider.notifier).updateTitle(recipe.title);
      ref
          .read(addRecipeControllerProvider.notifier)
          .updateDescription(recipe.description);
      ref
          .read(addRecipeControllerProvider.notifier)
          .updateIngredients(recipe.ingredients);
      ref
          .read(addRecipeControllerProvider.notifier)
          .updateDirections(recipe.directions);
      ref
          .read(addRecipeControllerProvider.notifier)
          .updatePreparationTime(recipe.preparationTime);

      if (!context.mounted) return;

      Navigator.of(context).pop();
      UIHelpers.showSuccessSnackbar(
        "image_step.recipe_generated_success".tr,
        context,
      );
    } on AIRecipeException catch (e) {
      if (!context.mounted) return;
      Navigator.of(context).pop();
      UIHelpers.showErrorSnackbar(e.message, context);
    } catch (e) {
      if (!context.mounted) return;
      Navigator.of(context).pop();
      UIHelpers.showErrorSnackbar(
        "image_step.recipe_generated_error".trParams({"0": e.toString()}),
        context,
      );
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final state = ref.watch(addRecipeControllerProvider);
    final authState = ref.watch(authControllerProvider);
    final isDarkMode = Theme.of(context).brightness == Brightness.dark;

    return Center(
      child: Column(
        children: [
          GestureDetector(
                onTap: () => _pickImage(ref),
                child: _buildImageContainer(state, ref),
              )
              .animate()
              .fadeIn(duration: 400.ms)
              .scaleXY(begin: 0.8, curve: Curves.easeOutBack),
          const SizedBox(height: 16),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 32),
            child: Text(
              "image_step.tap_to_select".tr,
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: 16,
                color: isDarkMode ? Colors.white70 : Colors.black54,
              ),
            ),
          ).animate().fadeIn(delay: 200.ms, duration: 400.ms),
          const SizedBox(height: 24),
          _buildAISection(
            context,
            ref,
            authState,
            isDarkMode,
          ).animate().fadeIn(delay: 400.ms, duration: 400.ms),
        ],
      ),
    );
  }

  Widget _buildImageContainer(AddRecipeState state, WidgetRef ref) {
    return Material(
      borderRadius: BorderRadius.circular(10),
      child: InkWell(
        borderRadius: BorderRadius.circular(10),
        child: Container(
          width: 200,
          height: 200,
          decoration: BoxDecoration(
            border: Border.all(color: Colors.deepOrange, width: 3),
            borderRadius: BorderRadius.circular(10),
            image: state.fullPath.isNotEmpty
                ? DecorationImage(
                    image: FileImage(File(state.fullPath)),
                    fit: BoxFit.cover,
                  )
                : null,
          ),
          child: state.fullPath.isEmpty
              ? const Icon(
                  CupertinoIcons.photo_on_rectangle,
                  color: Colors.deepOrange,
                  size: 40,
                )
              : null,
        ),
      ),
    );
  }

  Widget _buildAISection(
    BuildContext context,
    WidgetRef ref,
    AuthState authState,
    bool isDarkMode,
  ) {
    final state = ref.watch(addRecipeControllerProvider);

    if (state.fullPath.isEmpty) {
      return const SizedBox();
    }

    if (authState.isLoggedIn) {
      return Container(
        width: double.infinity,
        padding: const EdgeInsets.symmetric(horizontal: 32),
        child: PressableButton(
          child: FilledButton.icon(
            onPressed: () => _generateRecipeFromImage(context, ref),
            icon: const Icon(Icons.auto_awesome, color: Colors.white),
            label: Text(
              "image_step.generate_recipe_info".tr,
              style: const TextStyle(fontSize: 16, color: Colors.white),
            ),
          ),
        ),
      );
    }

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.symmetric(horizontal: 32),
      child: Text(
        "image_step.feature_not_available".tr,
        textAlign: TextAlign.center,
        style: TextStyle(
          fontSize: 14,
          color: isDarkMode ? Colors.white70 : Colors.black54,
        ),
      ),
    );
  }
}
