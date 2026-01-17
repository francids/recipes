import "package:flutter/material.dart";
import "package:flutter_riverpod/flutter_riverpod.dart";
import "package:the_recipes/controllers/add_recipe_controller.dart";
import "package:the_recipes/messages.dart";
import "package:the_recipes/views/widgets/form_field.dart";
import "package:flutter_animate/flutter_animate.dart";

class TextFieldsStepWidget extends ConsumerWidget {
  final VoidCallback? onChanged;

  const TextFieldsStepWidget({Key? key, this.onChanged}) : super(key: key);

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final state = ref.watch(addRecipeControllerProvider);
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16.0),
      child: Column(
        children: [
          _buildTextField(
            "text_fields_step.title_label".tr,
            state.title,
            "text_fields_step.title_hint".tr,
            (text) {
              ref.read(addRecipeControllerProvider.notifier).updateTitle(text);
              onChanged?.call();
            },
          )
              .animate()
              .fadeIn(delay: 100.ms, duration: 400.ms)
              .slideX(begin: -0.1, curve: Curves.easeOutCubic),
          _buildTextField(
            "text_fields_step.description_label".tr,
            state.description,
            "text_fields_step.description_hint".tr,
            (text) {
              ref
                  .read(addRecipeControllerProvider.notifier)
                  .updateDescription(text);
              onChanged?.call();
            },
          )
              .animate()
              .fadeIn(delay: 200.ms, duration: 400.ms)
              .slideX(begin: -0.1, curve: Curves.easeOutCubic),
          _buildTimeField(ref, state, context)
              .animate()
              .fadeIn(delay: 300.ms, duration: 400.ms)
              .slideX(begin: -0.1, curve: Curves.easeOutCubic),
        ],
      ),
    );
  }

  Widget _buildTextField(
      String label, String value, String hint, Function(String) onChanged) {
    return ModernFormField(
      topLabel: label,
      initialValue: value,
      onChanged: onChanged,
      keyboardType: TextInputType.text,
      hintText: hint,
    );
  }

  Widget _buildTimeField(
      WidgetRef ref, AddRecipeState state, BuildContext context) {
    return ModernFormField(
      topLabel: "text_fields_step.preparation_time_label".tr,
      initialValue: state.preparationTime > 0
          ? (state.preparationTime / 60).round().toString()
          : "",
      onChanged: (text) {
        final minutes = int.tryParse(text) ?? 0;
        ref
            .read(addRecipeControllerProvider.notifier)
            .updatePreparationTime(minutes * 60); // convert to seconds
        onChanged?.call();
      },
      keyboardType: TextInputType.number,
      hintText: "text_fields_step.preparation_time_hint".tr,
      decoration: ModernFormFieldDecoration(
        suffixIcon: Container(
          alignment: Alignment.center,
          width: 40,
          height: 40,
          child: Text(
            "text_fields_step.minutes".tr,
            style: TextStyle(
              color: Theme.of(context).colorScheme.onSurface.withAlpha(153),
              fontWeight: FontWeight.w500,
            ),
          ),
        ),
      ),
    );
  }
}
