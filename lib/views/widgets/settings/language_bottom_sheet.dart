import "package:flutter_animate/flutter_animate.dart";
import "package:flutter_riverpod/flutter_riverpod.dart";
import "package:material_ui/material_ui.dart";
import "package:the_recipes/controllers/language_controller.dart";
import "package:the_recipes/messages.dart";

class LanguageBottomSheet extends ConsumerWidget {
  const LanguageBottomSheet({super.key});

  static void show(BuildContext context) {
    showModalBottomSheet(
      context: context,
      showDragHandle: true,
      backgroundColor: Theme.of(context).colorScheme.surface,
      isScrollControlled: true,
      constraints: BoxConstraints(
        maxHeight: MediaQuery.of(context).size.height * 0.9,
      ),
      builder: (context) => const LanguageBottomSheet(),
    );
  }

  static const List<Map<String, String>> _languages = [
    {"code": "es", "name": "Español", "flagCode": "ES"},
    {"code": "en", "name": "English", "flagCode": "GB"},
    {"code": "de", "name": "Deutsch", "flagCode": "DE"},
    {"code": "it", "name": "Italiano", "flagCode": "IT"},
    {"code": "fr", "name": "Français", "flagCode": "FR"},
    {"code": "pt", "name": "Português", "flagCode": "PT"},
    {"code": "zh", "name": "中文", "flagCode": "CN"},
    {"code": "ja", "name": "日本語", "flagCode": "JP"},
    {"code": "ko", "name": "한국어", "flagCode": "KR"},
  ];

  String _getFlagEmoji(String countryCode) {
    final int firstLetter = countryCode.codeUnitAt(0) - 0x41 + 0x1F1E6;
    final int secondLetter = countryCode.codeUnitAt(1) - 0x41 + 0x1F1E6;
    return String.fromCharCode(firstLetter) + String.fromCharCode(secondLetter);
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
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
                "settings_screen.language".tr,
                style: Theme.of(context).appBarTheme.titleTextStyle,
                textAlign: TextAlign.start,
              )
              .animate()
              .fadeIn(duration: 300.ms)
              .slideX(begin: -0.15, curve: Curves.easeOutCubic),
          const SizedBox(height: 16.0),
          Flexible(
            child:
                Card(
                      elevation: 0,
                      margin: EdgeInsets.zero,
                      clipBehavior: Clip.antiAlias,
                      child: RadioGroup<String>(
                        groupValue: ref.watch(languageControllerProvider),
                        onChanged: (value) {
                          if (value != null) {
                            ref
                                .read(languageControllerProvider.notifier)
                                .changeLanguage(Locale(value));
                            Navigator.of(context).pop();
                          }
                        },
                        child: Column(
                          mainAxisSize: MainAxisSize.min,
                          children: List.generate(_languages.length, (index) {
                            final language = _languages[index];
                            return Column(
                                  mainAxisSize: MainAxisSize.min,
                                  children: [
                                    RadioListTile<String>(
                                      title: Row(
                                        children: [
                                          Text(
                                            _getFlagEmoji(
                                              language["flagCode"]!,
                                            ),
                                          ),
                                          const SizedBox(width: 8),
                                          Text(
                                            language["name"]!,
                                            style: Theme.of(context)
                                                .textTheme
                                                .bodyLarge,
                                          ),
                                        ],
                                      ),
                                      value: language["code"]!,
                                    ),
                                    if (index < _languages.length - 1)
                                      const Divider(
                                        height: 1,
                                        indent: 16,
                                        endIndent: 16,
                                      ),
                                  ],
                                )
                                .animate()
                                .fadeIn(
                                  delay: (100 + index * 50).ms,
                                  duration: 250.ms,
                                )
                                .slideX(
                                  begin: -0.15,
                                  curve: Curves.easeOutCubic,
                                );
                          }),
                        ),
                      ),
                    )
                    .animate()
                    .fadeIn(delay: 100.ms, duration: 250.ms)
                    .slideX(begin: -0.15, curve: Curves.easeOutCubic),
          ),
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
