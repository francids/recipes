import "package:flutter_riverpod/flutter_riverpod.dart";
import "package:hive_ce_flutter/adapters.dart";
import "package:intl/intl.dart";
import "package:material_ui/material_ui.dart";
import "package:the_recipes/hive_boxes.dart";

class LanguageController extends Notifier<String> {
  static const languageKey = "language_key";

  @override
  String build() {
    loadLanguage();
    return _getInitialLanguage();
  }

  String _getInitialLanguage() {
    try {
      if (Hive.isBoxOpen(settingsBox)) {
        final box = Hive.box(settingsBox);
        final deviceLocale = WidgetsBinding.instance.platformDispatcher.locale;
        return box.get(languageKey, defaultValue: deviceLocale.languageCode);
      }
    } catch (e) {
      debugPrint("Error getting initial language: $e");
    }
    return WidgetsBinding.instance.platformDispatcher.locale.languageCode;
  }

  void loadLanguage() async {
    try {
      if (!Hive.isBoxOpen(settingsBox)) {
        await Hive.openBox(settingsBox);
      }

      final box = Hive.box(settingsBox);
      final deviceLocale = WidgetsBinding.instance.platformDispatcher.locale;
      final language = box.get(
        languageKey,
        defaultValue: deviceLocale.languageCode,
      );

      state = language;
      applyLanguage();
    } catch (e) {
      debugPrint("Error loading language: $e");
      state = "en";
      applyLanguage();
    }
  }

  void changeLanguage(Locale language) async {
    state = language.languageCode;

    try {
      final box = Hive.box(settingsBox);
      await box.put(languageKey, language.languageCode);
    } catch (e) {
      debugPrint("Error saving language: $e");
    }

    applyLanguage();
  }

  void applyLanguage() {
    Intl.defaultLocale = state;
  }
}

final languageControllerProvider = NotifierProvider<LanguageController, String>(
  () {
    return LanguageController();
  },
);
