import "package:flutter_riverpod/flutter_riverpod.dart";
import "package:hive_ce_flutter/adapters.dart";
import "package:material_ui/material_ui.dart";
import "package:the_recipes/hive_boxes.dart";

class ThemeState {
  final bool isDarkMode;
  final bool followSystemTheme;

  ThemeState({required this.isDarkMode, required this.followSystemTheme});

  ThemeState copyWith({bool? isDarkMode, bool? followSystemTheme}) {
    return ThemeState(
      isDarkMode: isDarkMode ?? this.isDarkMode,
      followSystemTheme: followSystemTheme ?? this.followSystemTheme,
    );
  }
}

class ThemeController extends Notifier<ThemeState> {
  static const themeKey = "dark_theme";
  static const followSystemThemeKey = "follow_system_theme";

  @override
  ThemeState build() {
    _initializeTheme();
    return _getInitialTheme();
  }

  ThemeState _getInitialTheme() {
    try {
      if (Hive.isBoxOpen(settingsBox)) {
        final box = Hive.box(settingsBox);
        final followSystemTheme = box.get(
          followSystemThemeKey,
          defaultValue: true,
        );
        var isDarkMode = box.get(themeKey, defaultValue: false);

        if (followSystemTheme) {
          final brightness =
              WidgetsBinding.instance.platformDispatcher.platformBrightness;
          isDarkMode = brightness == Brightness.dark;
        }

        return ThemeState(
          isDarkMode: isDarkMode,
          followSystemTheme: followSystemTheme,
        );
      }
    } catch (e) {
      debugPrint("Error getting initial theme: $e");
    }

    final brightness =
        WidgetsBinding.instance.platformDispatcher.platformBrightness;
    return ThemeState(
      isDarkMode: brightness == Brightness.dark,
      followSystemTheme: true,
    );
  }

  void _initializeTheme() async {
    await loadTheme();
  }

  Future<void> loadTheme() async {
    try {
      if (!Hive.isBoxOpen(settingsBox)) {
        await Hive.openBox(settingsBox);
      }

      final box = Hive.box(settingsBox);
      final followSystemTheme = box.get(
        followSystemThemeKey,
        defaultValue: true,
      );
      var isDarkMode = box.get(themeKey, defaultValue: false);

      if (followSystemTheme) {
        final brightness =
            WidgetsBinding.instance.platformDispatcher.platformBrightness;
        isDarkMode = brightness == Brightness.dark;
      }

      state = ThemeState(
        isDarkMode: isDarkMode,
        followSystemTheme: followSystemTheme,
      );
    } catch (e) {
      debugPrint("Error loading theme: $e");
    }
  }

  void setDarkMode(bool isDark) async {
    state = state.copyWith(isDarkMode: isDark);
    await _saveTheme();
  }

  void setFollowSystemTheme(bool follow) async {
    state = state.copyWith(followSystemTheme: follow);

    if (follow) {
      final brightness =
          WidgetsBinding.instance.platformDispatcher.platformBrightness;
      state = state.copyWith(isDarkMode: brightness == Brightness.dark);
    }

    await _saveTheme();
  }

  Future<void> _saveTheme() async {
    try {
      final box = Hive.box(settingsBox);
      await box.put(themeKey, state.isDarkMode);
      await box.put(followSystemThemeKey, state.followSystemTheme);
    } catch (e) {
      debugPrint("Error saving theme: $e");
    }
  }
}

final themeControllerProvider = NotifierProvider<ThemeController, ThemeState>(
  () {
    return ThemeController();
  },
);
