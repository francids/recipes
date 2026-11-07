abstract class ApiConfig {
  /// Override at build time with:
  /// `flutter build apk --dart-define=API_BASE_URL=https://...`
  static const String baseUrl = String.fromEnvironment(
    "API_BASE_URL",
    defaultValue: "https://api.recipes.francids.com",
  );

  static const String googleReturnUrl = "recipes://auth";
}
