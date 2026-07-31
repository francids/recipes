import "package:flutter/material.dart";
import "package:flutter/cupertino.dart";
import "package:flutter/services.dart";
import "package:google_fonts/google_fonts.dart";
import "package:the_recipes/custom_predictive_back_page_transitions_builder.dart";

class TheRecipeAppTheme {
  static ThemeData theme = getTheme(false);
  static ThemeData darkTheme = getTheme(true);

  static ThemeData getTheme(bool isDark) {
    return ThemeData(
      textTheme: _getTextTheme(isDark),
      dialogTheme: _dialogTheme(isDark),
      cardTheme: cardTheme(isDark),
      dividerTheme: _dividerTheme(isDark),
      colorScheme: isDark ? _darkColorScheme : _colorScheme,
      appBarTheme: _getAppBarTheme(isDark),
      floatingActionButtonTheme: _floatingActionButtonThemeData(isDark),
      textButtonTheme: _textButtonTheme(isDark),
      outlinedButtonTheme: _outlinedButtonTheme,
      filledButtonTheme: _filledButtonTheme,
      bottomNavigationBarTheme: _bottomNavigationBarTheme(isDark),
      chipTheme: _chipTheme(isDark),
      brightness: isDark ? Brightness.dark : Brightness.light,
      scaffoldBackgroundColor: isDark ? const Color(0xFF121212) : Colors.white,
      splashColor: Colors.deepOrange.withAlpha(8),
      highlightColor: Colors.deepOrange.withAlpha(8),
      useMaterial3: true,
      popupMenuTheme: _popupMenuTheme(isDark),
      snackBarTheme: _snackBarTheme(isDark),
      pageTransitionsTheme: const PageTransitionsTheme(
        builders: {
          TargetPlatform.android: CustomPredictiveBackPageTransitionsBuilder(),
          TargetPlatform.iOS: CupertinoPageTransitionsBuilder(),
        },
      ),
    );
  }

  static DialogThemeData _dialogTheme(bool isDark) {
    return DialogThemeData(
      elevation: 8,
      backgroundColor: isDark ? _darkColorScheme.surface : _colorScheme.surface,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(16),
      ),
    );
  }

  static CardThemeData cardTheme(bool isDark) {
    return CardThemeData(
      color: isDark ? const Color(0xFF2A2A2A) : const Color(0xFFFAFAFA),
      shadowColor: isDark ? Colors.black26 : Colors.black12,
      surfaceTintColor: isDark ? Colors.black26 : Colors.black12,
      elevation: 2,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(10),
        side: BorderSide(
          color: isDark ? Colors.white30 : Colors.black26,
          width: 0.5,
          strokeAlign: BorderSide.strokeAlignInside,
        ),
      ),
    );
  }

  static DividerThemeData _dividerTheme(bool isDark) => DividerThemeData(
        color: isDark
            ? Colors.white30.withAlpha(24)
            : Colors.black26.withAlpha(24),
        thickness: 0.5,
        space: 0.5,
      );

  static TextButtonThemeData _textButtonTheme(bool isDark) =>
      TextButtonThemeData(
        style: ButtonStyle(
          textStyle: WidgetStatePropertyAll(
            TextStyle(
              fontWeight: FontWeight.w600,
            ),
          ),
          surfaceTintColor: WidgetStatePropertyAll(
            isDark ? Colors.grey.shade800 : Colors.grey.shade200,
          ),
          overlayColor: WidgetStatePropertyAll(
            isDark ? Colors.white10 : Colors.black12,
          ),
          splashFactory: NoSplash.splashFactory,
          backgroundColor: WidgetStatePropertyAll(
            isDark ? Colors.grey.shade800 : Colors.grey.shade200,
          ),
          foregroundColor: WidgetStatePropertyAll(
            isDark ? Colors.white : Colors.black87,
          ),
          shape: WidgetStatePropertyAll(
            RoundedRectangleBorder(
              borderRadius: BorderRadius.all(
                Radius.circular(10),
              ),
            ),
          ),
        ),
      );

  static const OutlinedButtonThemeData _outlinedButtonTheme =
      OutlinedButtonThemeData(
    style: ButtonStyle(
      textStyle: WidgetStatePropertyAll(
        TextStyle(
          fontWeight: FontWeight.w500,
        ),
      ),
      foregroundColor: WidgetStatePropertyAll(Colors.deepOrange),
      side: WidgetStatePropertyAll(
        BorderSide(
          color: Colors.deepOrange,
          width: 1.5,
        ),
      ),
      shape: WidgetStatePropertyAll(
        RoundedRectangleBorder(
          borderRadius: BorderRadius.all(
            Radius.circular(10),
          ),
        ),
      ),
    ),
  );

  static const FilledButtonThemeData _filledButtonTheme =
      const FilledButtonThemeData(
    style: ButtonStyle(
      textStyle: WidgetStatePropertyAll(
        TextStyle(
          fontWeight: FontWeight.w600,
        ),
      ),
      backgroundColor: WidgetStatePropertyAll(Colors.deepOrange),
      foregroundColor: WidgetStatePropertyAll(Colors.white),
      shape: WidgetStatePropertyAll(
        RoundedRectangleBorder(
          borderRadius: BorderRadius.all(
            Radius.circular(10),
          ),
        ),
      ),
    ),
  );

  static AppBarTheme _getAppBarTheme(bool isDark) {
    return AppBarTheme(
      centerTitle: false,
      toolbarHeight: 64,
      titleSpacing: 16,
      systemOverlayStyle: isDark
          ? const SystemUiOverlayStyle(
              statusBarIconBrightness: Brightness.light,
              statusBarBrightness: Brightness.dark,
            )
          : const SystemUiOverlayStyle(
              statusBarIconBrightness: Brightness.dark,
              statusBarBrightness: Brightness.light,
            ),
      backgroundColor: Colors.transparent,
      foregroundColor: isDark ? Colors.white : Colors.black,
      titleTextStyle: _getTextTheme(isDark).displayMedium!,
      shadowColor: Colors.transparent,
      elevation: 0,
      shape: Border(
        bottom: BorderSide(
          color: isDark ? Colors.white30 : Colors.black26,
          width: 0.5,
        ),
      ),
    );
  }

  static FloatingActionButtonThemeData _floatingActionButtonThemeData(
      bool isDark) {
    return FloatingActionButtonThemeData(
      backgroundColor: Colors.deepOrange,
      foregroundColor: Colors.white,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.all(
          Radius.circular(10),
        ),
      ),
    );
  }

  static ColorScheme _colorScheme = ColorScheme.fromSeed(
    seedColor: Colors.deepOrange,
    primary: Colors.deepOrange,
    secondary: Colors.deepOrange,
    surface: Colors.white,
    error: Colors.red,
    brightness: Brightness.light,
  );

  static ColorScheme _darkColorScheme = ColorScheme.fromSeed(
    seedColor: Colors.deepOrange,
    primary: Colors.deepOrange,
    secondary: Colors.deepOrange,
    surface: const Color(0xFF1E1E1E),
    error: Colors.red,
    brightness: Brightness.dark,
  );

  static TextTheme _getTextTheme(bool isDark) {
    return GoogleFonts.montserratTextTheme(
      TextTheme(
        displayLarge: TextStyle(
          color: isDark ? Colors.white : Colors.black,
          fontSize: 28,
          fontWeight: FontWeight.w600,
        ),
        displayMedium: TextStyle(
          color: isDark ? Colors.white : Colors.black,
          fontSize: 20,
          fontWeight: FontWeight.w500,
        ),
        bodyLarge: TextStyle(
          color: isDark ? Colors.white : Colors.black.withAlpha(230),
          fontSize: 16,
          fontWeight: FontWeight.w500,
        ),
        bodyMedium: TextStyle(
          color: isDark
              ? Colors.white.withAlpha(225)
              : Colors.black.withAlpha(225),
          fontSize: 14,
        ),
        bodySmall: TextStyle(
          color: isDark ? Colors.white60 : Colors.black54,
          fontSize: 12,
        ),
        labelLarge: TextStyle(
          color: isDark ? Colors.white : Colors.black87,
          fontSize: 14,
          fontWeight: FontWeight.w500,
        ),
        labelMedium: TextStyle(
          color: isDark ? Colors.white70 : Colors.black87,
          fontSize: 12,
          fontWeight: FontWeight.w400,
        ),
      ),
    );
  }

  static PopupMenuThemeData _popupMenuTheme(bool isDark) {
    return PopupMenuThemeData(
      color: isDark ? const Color(0xFF2A2A2A) : Colors.white,
      textStyle: TextStyle(
        color: isDark ? Colors.white : Colors.black87,
        fontSize: 14,
        fontWeight: FontWeight.w500,
      ),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(10),
        side: BorderSide(
          color: isDark ? Colors.white30 : Colors.black26,
          width: 0.5,
          strokeAlign: BorderSide.strokeAlignInside,
        ),
      ),
      elevation: 2,
    );
  }

  static BottomNavigationBarThemeData _bottomNavigationBarTheme(bool isDark) {
    return BottomNavigationBarThemeData(
      backgroundColor: isDark ? const Color(0xFF1E1E1E) : Colors.white,
      selectedItemColor: Colors.deepOrange,
      unselectedItemColor: isDark ? Colors.white54 : Colors.black54,
      selectedLabelStyle: const TextStyle(
        fontSize: 12,
        fontWeight: FontWeight.w600,
      ),
      unselectedLabelStyle: const TextStyle(
        fontSize: 12,
        fontWeight: FontWeight.w400,
      ),
      type: BottomNavigationBarType.fixed,
      elevation: 0,
      enableFeedback: true,
      mouseCursor: WidgetStateProperty.all(SystemMouseCursors.click),
      selectedIconTheme: const IconThemeData(
        size: 20,
      ),
      unselectedIconTheme: const IconThemeData(
        size: 20,
      ),
      landscapeLayout: BottomNavigationBarLandscapeLayout.centered,
      showSelectedLabels: true,
      showUnselectedLabels: true,
    );
  }

  static SnackBarThemeData _snackBarTheme(bool isDark) {
    return SnackBarThemeData(
      backgroundColor:
          isDark ? const Color(0xFF2A2A2A) : const Color(0xFF323232),
      contentTextStyle: TextStyle(
        color: Colors.white,
        fontSize: 14,
        fontWeight: FontWeight.w500,
      ),
      actionTextColor: Colors.deepOrange,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(10),
      ),
      behavior: SnackBarBehavior.floating,
      elevation: 6,
    );
  }

  static ChipThemeData _chipTheme(bool isDark) {
    return ChipThemeData(
      backgroundColor:
          isDark ? const Color(0xFF2A2A2A) : const Color(0xFFF5F5F5),
      selectedColor: Colors.deepOrange.withAlpha(50),
      disabledColor: isDark ? Colors.white12 : Colors.black12,
      deleteIconColor: isDark ? Colors.white70 : Colors.black54,
      labelStyle: _getTextTheme(isDark).labelLarge!.copyWith(
            fontWeight: FontWeight.w500,
            color: isDark ? Colors.white70 : Colors.black54,
          ),
      secondaryLabelStyle: _getTextTheme(isDark).labelMedium!.copyWith(
            fontWeight: FontWeight.w400,
            color: isDark ? Colors.white70 : Colors.black54,
          ),
      side: BorderSide(
        color: isDark ? Colors.white30 : Colors.black26,
        width: 0.5,
      ),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(10),
      ),
      elevation: 0,
      pressElevation: 2,
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      iconTheme: IconThemeData(
        color: isDark ? Colors.white70 : Colors.black54,
        size: 18,
      ),
    );
  }
}
