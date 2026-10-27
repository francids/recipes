import "package:cupertino_ui/cupertino_ui.dart";
import "package:flutter_animate/flutter_animate.dart";
import "package:flutter_riverpod/flutter_riverpod.dart";
import "package:material_ui/material_ui.dart";
import "package:the_recipes/controllers/add_recipe_controller.dart";
import "package:the_recipes/messages.dart";
import "package:the_recipes/views/widgets/pressable_button.dart";
import "package:the_recipes/views/widgets/recipe_form/dynamic_list_step_widget.dart";
import "package:the_recipes/views/widgets/recipe_form/image_step_widget.dart";
import "package:the_recipes/views/widgets/recipe_form/text_fields_step_widget.dart";
import "package:the_recipes/views/widgets/ui_helpers.dart";

class AddRecipeScreen extends ConsumerStatefulWidget {
  const AddRecipeScreen({super.key});

  @override
  ConsumerState<AddRecipeScreen> createState() => _AddRecipeScreenState();
}

class _AddRecipeScreenState extends ConsumerState<AddRecipeScreen> {
  final PageController _pageController = PageController();
  int _currentStep = 0;
  bool _isCurrentStepValid = false;
  static const _animationDuration = Duration(milliseconds: 300);
  final _stepTitles = [
    "add_recipe_screen.step_image".tr,
    "add_recipe_screen.step_details".tr,
    "add_recipe_screen.step_ingredients".tr,
    "add_recipe_screen.step_instructions".tr,
  ];

  @override
  void initState() {
    super.initState();
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    _validateStep();
  }

  void _validateStep() {
    setState(() {
      _isCurrentStepValid = _isValid();
    });
  }

  bool _isValid() {
    final state = ref.watch(addRecipeControllerProvider);
    switch (_currentStep) {
      case 0:
        return state.fullPath.isNotEmpty;
      case 1:
        return state.title.isNotEmpty &&
            state.description.isNotEmpty &&
            state.preparationTime > 0;
      case 2:
        return _isListValid(state.ingredientsList);
      case 3:
        return _isListValid(state.directionsList);
      default:
        return true;
    }
  }

  bool _isListValid(List<String> list) =>
      list.isNotEmpty && list.every((item) => item.isNotEmpty);

  void _handleNavigation(bool isNext) {
    if (isNext) {
      if (_isCurrentStepValid && _currentStep < 3) {
        _pageController.nextPage(
          duration: _animationDuration,
          curve: Curves.easeInOut,
        );
        setState(() {
          _currentStep++;
        });
        _validateStep();
      } else if (!_isCurrentStepValid) {
        UIHelpers.showErrorSnackbar(
          "add_recipe_screen.error_complete_fields".tr,
          context,
        );
      }
    } else if (_currentStep > 0) {
      _pageController.previousPage(
        duration: _animationDuration,
        curve: Curves.easeInOut,
      );
      setState(() {
        _currentStep--;
      });
      _validateStep();
    }
  }

  Future<void> _handleSaveRecipe() async {
    UIHelpers.showLoadingDialog(
      context,
      "add_recipe_screen.saving_recipe".tr,
      "add_recipe_screen.please_wait".tr,
    );

    if (!_isCurrentStepValid) {
      Navigator.of(context).pop(); // Close loading dialog
      UIHelpers.showErrorSnackbar(
        "add_recipe_screen.error_complete_all".tr,
        context,
      );
      return;
    }

    try {
      await ref.read(addRecipeControllerProvider.notifier).addRecipe();
      ref.read(addRecipeControllerProvider.notifier).resetState();
      if (!mounted) return;
      Navigator.of(context).pop(); // Close loading dialog
      Navigator.of(context).pop(); // Close add recipe screen
      UIHelpers.showSuccessSnackbar(
        "add_recipe_screen.recipe_saved".tr,
        context,
      );
    } catch (e) {
      if (!mounted) return;
      Navigator.of(context).pop(); // Close loading dialog
      UIHelpers.showErrorSnackbar("add_recipe_screen.error_saving".tr, context);
    }
  }

  void _handleBackButton() {
    final state = ref.watch(addRecipeControllerProvider);
    final hasData =
        state.fullPath.isNotEmpty ||
        state.title.isNotEmpty ||
        state.description.isNotEmpty ||
        state.preparationTime > 0 ||
        state.ingredientsList.isNotEmpty ||
        state.directionsList.isNotEmpty;

    if (hasData) {
      UIHelpers.showConfirmationDialog(
        context: context,
        title: "add_recipe_screen.exit_title".tr,
        message: "add_recipe_screen.exit_without_saving".tr,
        lottieAsset: "assets/lottie/back.json",
        confirmAction: () {
          ref.read(addRecipeControllerProvider.notifier).resetState();
          Navigator.of(context).pop();
        },
      );
    } else {
      ref.read(addRecipeControllerProvider.notifier).resetState();
      Navigator.of(context).pop();
    }
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(addRecipeControllerProvider);
    return PopScope(
      canPop: false,
      onPopInvokedWithResult: (bool didPop, dynamic) {
        if (didPop) return;

        final hasData =
            state.fullPath.isNotEmpty ||
            state.title.isNotEmpty ||
            state.description.isNotEmpty ||
            state.preparationTime > 0 ||
            state.ingredientsList.isNotEmpty ||
            state.directionsList.isNotEmpty;

        if (hasData) {
          UIHelpers.showConfirmationDialog(
            context: context,
            title: "add_recipe_screen.exit_title".tr,
            message: "add_recipe_screen.exit_without_saving".tr,
            lottieAsset: "assets/lottie/back.json",
            confirmAction: () {
              ref.read(addRecipeControllerProvider.notifier).resetState();
              Navigator.of(context).pop();
            },
          );
        } else {
          ref.read(addRecipeControllerProvider.notifier).resetState();
          Navigator.of(context).pop();
        }
      },
      child: Scaffold(
        appBar: AppBar(
          title: Text("add_recipe_screen.title".tr),
          leading: IconButton(
            icon: const Icon(CupertinoIcons.back),
            onPressed: _handleBackButton,
          ),
        ),
        body: Stack(
          children: [
            PageView(
              physics: const NeverScrollableScrollPhysics(),
              controller: _pageController,
              onPageChanged: (index) {
                setState(() {
                  _currentStep = index;
                });
                _validateStep();
              },
              children: [
                _wrapInScrollView(
                  ImageStepWidget(onImageSelected: _validateStep),
                ),
                _wrapInScrollView(
                  TextFieldsStepWidget(onChanged: _validateStep),
                ),
                _wrapInScrollView(
                  DynamicListStepWidget(
                    type: "ingredients",
                    label: "add_recipe_screen.ingredient".tr,
                    onChanged: _validateStep,
                  ),
                ),
                _wrapInScrollView(
                  DynamicListStepWidget(
                    type: "directions",
                    label: "add_recipe_screen.step".tr,
                    onChanged: _validateStep,
                  ),
                ),
              ],
            ),
            Positioned(
              top: 0,
              left: 0,
              right: 0,
              child: IgnorePointer(
                child: Container(
                  height: 80,
                  decoration: BoxDecoration(
                    gradient: LinearGradient(
                      begin: Alignment.bottomCenter,
                      end: Alignment.topCenter,
                      colors: [
                        Theme.of(context).scaffoldBackgroundColor.withAlpha(0),
                        Theme.of(context).scaffoldBackgroundColor
                            .withAlpha(120),
                        Theme.of(context).scaffoldBackgroundColor
                            .withAlpha(200),
                        Theme.of(context).scaffoldBackgroundColor
                            .withAlpha(240),
                        Theme.of(context).scaffoldBackgroundColor
                            .withAlpha(250),
                        Theme.of(context).scaffoldBackgroundColor
                            .withAlpha(255),
                        Theme.of(context).scaffoldBackgroundColor,
                      ],
                      stops: const [0.0, 0.05, 0.1, 0.2, 0.4, 0.7, 1.0],
                    ),
                  ),
                ),
              ),
            ),
            Container(
              padding: const EdgeInsets.all(16.0),
              width: double.infinity,
              child: Column(
                children: [
                  Text(
                    _stepTitles[_currentStep],
                    style: const TextStyle(
                      fontSize: 20,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  Text(
                    "add_recipe_screen.step_counter".trParams({
                      "0": (_currentStep + 1).toString(),
                      "1": "4",
                    }),
                    style: TextStyle(fontSize: 16, color: Colors.grey.shade700),
                  ),
                ],
              ),
            ).animate().fadeIn(duration: 300.ms),
            if (MediaQuery.of(context).viewInsets.bottom == 0)
              Positioned(
                bottom: 0,
                left: 0,
                right: 0,
                child: IgnorePointer(
                  child: Container(
                    height: 100,
                    decoration: BoxDecoration(
                      gradient: LinearGradient(
                        begin: Alignment.topCenter,
                        end: Alignment.bottomCenter,
                        colors: [
                          Theme.of(context).scaffoldBackgroundColor
                              .withAlpha(0),
                          Theme.of(context).scaffoldBackgroundColor
                              .withAlpha(90),
                          Theme.of(context).scaffoldBackgroundColor
                              .withAlpha(180),
                          Theme.of(context).scaffoldBackgroundColor,
                        ],
                        stops: const [0.0, 0.25, 0.6, 1.0],
                      ),
                    ),
                  ),
                ),
              ),
          ],
        ),
        extendBody: true,
        bottomNavigationBar: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Padding(
              padding: const EdgeInsets.only(
                left: 24,
                right: 24,
                top: 8,
                bottom: 32,
              ),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  _currentStep > 0
                      ? PressableButton(
                              child: TextButton(
                                onPressed: () => _handleNavigation(false),
                                child: Text("add_recipe_screen.previous".tr),
                              ),
                            )
                            .animate()
                            .fadeIn(duration: 200.ms)
                            .slideX(begin: -0.5, curve: Curves.easeOutCubic)
                      : const SizedBox(),
                  _currentStep < 3 && _isCurrentStepValid
                      ? PressableButton(
                              child: FilledButton(
                                onPressed: () => _handleNavigation(true),
                                child: Text("add_recipe_screen.next".tr),
                              ),
                            )
                            .animate()
                            .fadeIn(duration: 200.ms)
                            .slideX(begin: 0.5, curve: Curves.easeOutCubic)
                      : _currentStep == 3 && _isCurrentStepValid
                      ? PressableButton(
                              child: FilledButton.icon(
                                icon: const Icon(
                                  CupertinoIcons.checkmark_alt,
                                  size: 20,
                                ),
                                onPressed: _handleSaveRecipe,
                                label: Text("add_recipe_screen.save".tr),
                              ),
                            )
                            .animate()
                            .fadeIn(duration: 200.ms)
                            .slideX(begin: 0.5, curve: Curves.easeOutCubic)
                      : const SizedBox(),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _wrapInScrollView(Widget child) {
    return SingleChildScrollView(
      padding: EdgeInsets.only(
        top: 80,
        bottom: (MediaQuery.of(context).viewInsets.bottom == 0) ? 80 : 16,
      ),
      child: child.animate().fadeIn(duration: 400.ms, delay: 100.ms),
    );
  }
}
