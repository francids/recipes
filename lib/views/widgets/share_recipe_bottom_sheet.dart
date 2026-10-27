import "package:cupertino_ui/cupertino_ui.dart";
import "package:flutter_animate/flutter_animate.dart";
import "package:flutter_riverpod/flutter_riverpod.dart";
import "package:material_ui/material_ui.dart";
import "package:share_plus/share_plus.dart";
import "package:the_recipes/controllers/recipe_controller.dart";
import "package:the_recipes/controllers/share_recipe_controller.dart";
import "package:the_recipes/messages.dart";
import "package:the_recipes/views/widgets/qr_bottom_sheet.dart";

class ShareRecipeBottomSheet extends ConsumerWidget {
  const ShareRecipeBottomSheet({super.key, required this.recipeId});

  final String recipeId;

  static void show(BuildContext context, String recipeId) {
    showModalBottomSheet(
      context: context,
      showDragHandle: true,
      backgroundColor: Theme.of(context).colorScheme.surface,
      builder: (context) => ShareRecipeBottomSheet(recipeId: recipeId),
    );
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final shareRecipeController = ref.watch(
      shareRecipeControllerProvider.notifier,
    );
    ref.watch(recipeControllerProvider);

    return Padding(
      padding: EdgeInsets.only(
        top: MediaQuery.of(context).padding.top + 16.0,
        bottom: MediaQuery.of(context).padding.bottom + 8.0,
        left: 16.0,
        right: 16.0,
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Text(
                "recipe_screen.share_recipe".tr,
                style: Theme.of(context).appBarTheme.titleTextStyle,
                textAlign: TextAlign.start,
              )
              .animate()
              .fadeIn(duration: 300.ms)
              .slideX(begin: -0.15, curve: Curves.easeOutCubic),
          const SizedBox(height: 8.0),
          Text(
                shareRecipeController.isPublic(recipeId)
                    ? "recipe_screen.recipe_already_public".tr
                    : "recipe_screen.share_description".tr,
                style: Theme.of(context).textTheme.bodyMedium,
                textAlign: TextAlign.start,
              )
              .animate()
              .fadeIn(delay: 50.ms, duration: 250.ms)
              .slideX(begin: -0.15, curve: Curves.easeOutCubic),
          const SizedBox(height: 16.0),
          Card(
                elevation: 0,
                margin: EdgeInsets.zero,
                clipBehavior: Clip.antiAlias,
                child: Column(
                  children: [
                    SwitchListTile(
                          title: Text(
                            "recipe_screen.make_public".tr,
                            style: Theme.of(context).textTheme.bodyLarge,
                          ),
                          subtitle: Text(
                            shareRecipeController.isPublic(recipeId)
                                ? "recipe_screen.make_private_description".tr
                                : "recipe_screen.make_public_description".tr,
                            style: Theme.of(context).textTheme.bodySmall
                                ?.copyWith(
                                  color: Theme.of(context).colorScheme.outline,
                                ),
                          ),
                          value: shareRecipeController.isPublic(recipeId),
                          onChanged: shareRecipeController.isLoading(recipeId)
                              ? null
                              : (value) async {
                                  if (value) {
                                    await shareRecipeController
                                        .makeRecipePublic(recipeId);
                                  } else {
                                    await shareRecipeController
                                        .makeRecipePrivate(recipeId);
                                  }
                                },
                          secondary: Icon(
                            shareRecipeController.isPublic(recipeId)
                                ? CupertinoIcons.globe
                                : CupertinoIcons.lock,
                            color: Theme.of(context).colorScheme.primary,
                          ),
                        )
                        .animate()
                        .fadeIn(delay: 100.ms, duration: 250.ms)
                        .slideX(begin: -0.15, curve: Curves.easeOutCubic),
                    if (shareRecipeController.isPublic(recipeId)) ...[
                      const Divider(height: 1, indent: 16, endIndent: 16),
                      ListTile(
                            title: Text(
                              "recipe_screen.generate_qr".tr,
                              style: Theme.of(context).textTheme.bodyLarge,
                            ),
                            subtitle: Text(
                              "recipe_screen.generate_qr_description".tr,
                              style: Theme.of(context).textTheme.bodySmall
                                  ?.copyWith(
                                    color: Theme.of(context)
                                        .colorScheme
                                        .outline,
                                  ),
                            ),
                            leading: Icon(
                              CupertinoIcons.qrcode,
                              color: Theme.of(context).colorScheme.primary,
                            ),
                            trailing: Icon(
                              CupertinoIcons.chevron_forward,
                              color: Theme.of(context).colorScheme.outline,
                            ),
                            onTap: () {
                              QrBottomSheet.show(
                                context,
                                shareRecipeController.generateShareUrl(
                                  recipeId,
                                ),
                              );
                            },
                          )
                          .animate()
                          .fadeIn(delay: 150.ms, duration: 250.ms)
                          .slideX(begin: -0.15, curve: Curves.easeOutCubic),
                      const Divider(height: 1, indent: 16, endIndent: 16),
                      ListTile(
                            title: Text(
                              "recipe_screen.share".tr,
                              style: Theme.of(context).textTheme.bodyLarge,
                            ),
                            subtitle: Text(
                              "recipe_screen.share_link_description".tr,
                              style: Theme.of(context).textTheme.bodySmall
                                  ?.copyWith(
                                    color: Theme.of(context)
                                        .colorScheme
                                        .outline,
                                  ),
                            ),
                            leading: Icon(
                              CupertinoIcons.share,
                              color: Theme.of(context).colorScheme.primary,
                            ),
                            trailing: Icon(
                              CupertinoIcons.chevron_forward,
                              color: Theme.of(context).colorScheme.outline,
                            ),
                            onTap: () {
                              SharePlus.instance.share(
                                ShareParams(
                                  uri: Uri.parse(
                                    shareRecipeController.generateShareUrl(
                                      recipeId,
                                    ),
                                  ),
                                ),
                              );
                            },
                          )
                          .animate()
                          .fadeIn(delay: 200.ms, duration: 250.ms)
                          .slideX(begin: -0.15, curve: Curves.easeOutCubic),
                    ],
                  ],
                ),
              )
              .animate()
              .fadeIn(delay: 100.ms, duration: 250.ms)
              .slideX(begin: -0.15, curve: Curves.easeOutCubic),
          const SizedBox(height: 16.0),
          TextButton(
                onPressed: () => Navigator.of(context).pop(),
                child: Text("ui_helpers.cancel".tr),
              )
              .animate()
              .fadeIn(delay: 250.ms, duration: 250.ms)
              .slideX(begin: -0.15, curve: Curves.easeOutCubic),
        ],
      ),
    );
  }
}
