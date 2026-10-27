import "dart:convert";

import "package:flutter/services.dart";
import "package:intl/intl.dart";

class Messages {
  static late Map<String, Map<String, String>> _keys;

  static String get appName {
    final locale = Intl.getCurrentLocale();
    final langCode = locale.split('_')[0];
    return {
          "es": "Recetas",
          "en": "Recipes",
          "de": "Rezepte",
          "it": "Ricette",
          "fr": "Recettes",
          "pt": "Receitas",
          "zh": "食谱",
          "ja": "レシピ",
          "ko": "레시피",
        }[langCode] ??
        "Recipes";
  }

  static Map<String, Map<String, String>> get keys => _keys;

  static Future<void> init() async {
    _keys = {
      "es": {},
      "en": {},
      "de": {},
      "it": {},
      "fr": {},
      "pt": {},
      "zh": {},
      "ja": {},
      "ko": {},
    };

    for (final lang in _keys.keys) {
      final content = await rootBundle.loadString("assets/messages/$lang.json");
      final translations = _flattenTranslations(json.decode(content));
      _convertParamsFormat(translations);
      _keys[lang] = translations;
    }
  }

  static String translate(String key, [Map<String, String>? params]) {
    final locale = Intl.getCurrentLocale();
    final langCode = locale.split('_')[0];

    final translations = _keys[langCode] ?? _keys["en"] ?? {};
    String translation = translations[key] ?? key;

    if (params != null) {
      params.forEach((paramKey, value) {
        translation = translation.replaceAll("@$paramKey", value);
      });
    }

    return translation;
  }

  static void _convertParamsFormat(Map<String, String> translations) {
    translations.forEach((key, value) {
      if (value.contains("{") && value.contains("}")) {
        var newValue = value;
        final regex = RegExp(r"\{(\d+)\}");
        final matches = regex.allMatches(value);
        for (var match in matches) {
          final paramIndex = match.group(1);
          newValue = newValue.replaceAll("{$paramIndex}", "@$paramIndex");
        }
        translations[key] = newValue;
      }
    });
  }

  static Map<String, String> _flattenTranslations(
    Map<String, dynamic> json, [
    String prefix = "",
  ]) {
    final Map<String, String> result = {};

    json.forEach((key, value) {
      final String newKey = prefix.isEmpty ? key : "$prefix.$key";

      if (value is Map) {
        result.addAll(
          _flattenTranslations(Map<String, dynamic>.from(value), newKey),
        );
      } else if (value is String) {
        result[newKey] = value;
      }
    });

    return result;
  }
}

extension StringTranslationExtension on String {
  String get tr => Messages.translate(this);

  String trParams(Map<String, String> params) =>
      Messages.translate(this, params);
}
