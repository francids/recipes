import "package:cupertino_ui/cupertino_ui.dart";
import "package:flutter_animate/flutter_animate.dart";
import "package:lottie/lottie.dart";
import "package:material_ui/material_ui.dart";
import "package:the_recipes/messages.dart";
import "package:the_recipes/views/widgets/pressable_button.dart";

class UIHelpers {
  static void showErrorSnackbar(String message, BuildContext context) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Row(
          children: [
            Icon(
                  CupertinoIcons.exclamationmark_circle,
                  color: Theme.of(context).colorScheme.error,
                )
                .animate()
                .fadeIn(duration: 300.ms)
                .scaleXY(begin: 0.5, curve: Curves.easeOutBack),
            const SizedBox(width: 8),
            Expanded(
              child: Text(message)
                  .animate()
                  .fadeIn(delay: 100.ms, duration: 300.ms)
                  .slideX(begin: 0.1, curve: Curves.easeOutCubic),
            ),
          ],
        ),
        behavior: SnackBarBehavior.floating,
      ),
    );
  }

  static void showSuccessSnackbar(String message, BuildContext context) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Row(
          children: [
            Icon(CupertinoIcons.check_mark_circled, color: Colors.green)
                .animate()
                .fadeIn(duration: 300.ms)
                .scaleXY(begin: 0.5, curve: Curves.easeOutBack),
            const SizedBox(width: 8),
            Expanded(
              child: Text(message)
                  .animate()
                  .fadeIn(delay: 100.ms, duration: 300.ms)
                  .slideX(begin: 0.1, curve: Curves.easeOutCubic),
            ),
          ],
        ),
        behavior: SnackBarBehavior.floating,
        duration: const Duration(seconds: 3),
      ),
    );
  }

  static void showConfirmationDialog({
    required BuildContext context,
    required String title,
    required String message,
    required String lottieAsset,
    required VoidCallback confirmAction,
  }) {
    showDialog(
      context: context,
      barrierDismissible: true,
      useRootNavigator: true,
      builder: (BuildContext context) {
        return Dialog(
              child: Container(
                width: MediaQuery.of(context).size.width * 0.8,
                padding: const EdgeInsets.all(16),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    SizedBox(
                          height: 120,
                          width: 120,
                          child: Lottie.asset(lottieAsset, fit: BoxFit.contain),
                        )
                        .animate()
                        .fadeIn(duration: 300.ms)
                        .scaleXY(begin: 0.8, curve: Curves.easeOutBack),
                    const SizedBox(height: 16),
                    Text(
                          title,
                          style: Theme.of(context).textTheme.displayMedium!,
                          textAlign: TextAlign.center,
                        )
                        .animate()
                        .fadeIn(delay: 100.ms, duration: 300.ms)
                        .slideY(begin: 0.1, curve: Curves.easeOutCubic),
                    const SizedBox(height: 16),
                    Text(
                          message,
                          style: Theme.of(context).textTheme.bodyMedium!,
                          textAlign: TextAlign.center,
                        )
                        .animate()
                        .fadeIn(delay: 200.ms, duration: 300.ms)
                        .slideY(begin: 0.1, curve: Curves.easeOutCubic),
                    const SizedBox(height: 24),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      children: [
                        Expanded(
                              child: PressableButton(
                                child: TextButton(
                                  onPressed: () => Navigator.of(context).pop(),
                                  child: Text("ui_helpers.cancel".tr),
                                ),
                              ),
                            )
                            .animate()
                            .fadeIn(delay: 300.ms, duration: 250.ms)
                            .slideX(begin: -0.1, curve: Curves.easeOutCubic),
                        const SizedBox(width: 8),
                        Expanded(
                              child: PressableButton(
                                child: FilledButton(
                                  onPressed: () {
                                    Navigator.of(context).pop();
                                    confirmAction();
                                  },
                                  child: Text("ui_helpers.confirm".tr),
                                ),
                              ),
                            )
                            .animate()
                            .fadeIn(delay: 350.ms, duration: 250.ms)
                            .slideX(begin: -0.1, curve: Curves.easeOutCubic),
                      ],
                    ),
                  ],
                ),
              ),
            )
            .animate()
            .fadeIn(duration: 200.ms)
            .scaleXY(begin: 0.9, curve: Curves.easeOutBack);
      },
    );
  }

  static void showLoadingDialog(
    BuildContext context,
    String title,
    String message, {
    String? lottieAsset,
  }) {
    showDialog(
      context: context,
      barrierDismissible: false,
      useRootNavigator: true,
      builder: (BuildContext context) {
        return PopScope(
          canPop: false,
          child:
              Dialog(
                    child: Container(
                      width: MediaQuery.of(context).size.width * 0.8,
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          SizedBox(
                                height: 120,
                                width: 120,
                                child: Lottie.asset(
                                  lottieAsset ?? "assets/lottie/loading.json",
                                  fit: BoxFit.contain,
                                ),
                              )
                              .animate()
                              .fadeIn(duration: 300.ms)
                              .scaleXY(begin: 0.8, curve: Curves.easeOutBack),
                          const SizedBox(height: 16),
                          Text(
                                title,
                                style: Theme.of(context)
                                    .textTheme
                                    .displayMedium!,
                                textAlign: TextAlign.center,
                              )
                              .animate()
                              .fadeIn(delay: 100.ms, duration: 300.ms)
                              .slideY(begin: 0.1, curve: Curves.easeOutCubic),
                          const SizedBox(height: 16),
                          Text(
                                message,
                                style: Theme.of(context).textTheme.bodyMedium!,
                                textAlign: TextAlign.center,
                              )
                              .animate()
                              .fadeIn(delay: 200.ms, duration: 300.ms)
                              .slideY(begin: 0.1, curve: Curves.easeOutCubic),
                          const SizedBox(height: 24),
                        ],
                      ),
                    ),
                  )
                  .animate()
                  .fadeIn(duration: 200.ms)
                  .scaleXY(begin: 0.9, curve: Curves.easeOutBack),
        );
      },
    );
  }
}
