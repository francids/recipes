import "dart:async";
import "dart:convert";

import "package:http/http.dart" as http;
import "package:recipes/api/api_config.dart";

class ApiException implements Exception {
  final String message;
  final int statusCode;

  ApiException(this.message, this.statusCode);

  @override
  String toString() => message;
}

class ApiClient {
  ApiClient._();

  static final ApiClient instance = ApiClient._();

  final http.Client _http = http.Client();
  String? _token;

  String get baseUrl => ApiConfig.baseUrl;
  bool get isAuthenticated => _token != null && _token!.isNotEmpty;

  void setToken(String? token) {
    _token = token;
  }

  String get googleSignInUrl {
    final returnTo = Uri.encodeComponent(ApiConfig.googleReturnUrl);
    return "$baseUrl/auth/google?return_to=$returnTo";
  }

  Map<String, String> _headers({bool json = false}) {
    return {
      if (json) "Content-Type": "application/json",
      if (_token != null && _token!.isNotEmpty)
        "Authorization": "Bearer $_token",
    };
  }

  Future<Map<String, dynamic>> _request(
    String method,
    String path, {
    Object? body,
    bool authenticated = true,
  }) async {
    final request = http.Request(method, Uri.parse("$baseUrl$path"));
    request.headers.addAll(_headers(json: body != null));
    if (body != null) {
      request.body = jsonEncode(body);
    }

    final streamed = await request.send().timeout(const Duration(seconds: 60));
    final response = await http.Response.fromStream(streamed);

    if (response.statusCode >= 200 && response.statusCode < 300) {
      if (response.body.trim().isEmpty) return const {};
      final decoded = jsonDecode(response.body);
      return decoded is Map<String, dynamic> ? decoded : {"data": decoded};
    }

    String message = "Request failed (${response.statusCode})";
    try {
      final decoded = jsonDecode(response.body);
      if (decoded is Map && decoded["error"] != null) {
        message = decoded["error"].toString();
      }
    } catch (_) {
      if (response.body.trim().isNotEmpty) {
        message = response.body;
      }
    }
    throw ApiException(message, response.statusCode);
  }

  Future<Map<String, dynamic>> me() {
    return _request("GET", "/auth/me");
  }

  Future<void> logout() async {
    try {
      await _request("POST", "/auth/logout");
    } on ApiException {
      // The local session is discarded even if the server already expired it.
    }
  }

  Future<void> deleteAccount() async {
    await _request("DELETE", "/account");
  }

  Future<List<Map<String, dynamic>>> listRecipes() async {
    final response = await _request("GET", "/recipes");
    return _list(response);
  }

  Future<Map<String, dynamic>> getRecipe(String id) {
    return _request("GET", "/recipes/${Uri.encodeComponent(id)}");
  }

  Future<Map<String, dynamic>> createRecipe(Map<String, dynamic> recipe) {
    return _request("POST", "/recipes", body: recipe);
  }

  Future<Map<String, dynamic>> updateRecipe(
    String id,
    Map<String, dynamic> changes,
  ) {
    return _request(
      "PATCH",
      "/recipes/${Uri.encodeComponent(id)}",
      body: changes,
    );
  }

  Future<void> deleteRecipe(String id) async {
    await _request("DELETE", "/recipes/${Uri.encodeComponent(id)}");
  }

  Future<List<Map<String, dynamic>>> searchPublicRecipes(String query) async {
    final encoded = Uri.encodeQueryComponent(query.trim());
    final response = await _request(
      "GET",
      "/recipes/public?query=$encoded",
      authenticated: false,
    );
    return _list(response);
  }

  Future<Map<String, dynamic>> getPublicRecipe(String id) {
    return _request(
      "GET",
      "/recipes/public/${Uri.encodeComponent(id)}",
      authenticated: false,
    );
  }

  Future<Map<String, dynamic>> generateRecipe({
    required String imageBase64,
    required String language,
  }) {
    return _request(
      "POST",
      "/ai/recipes",
      body: {"image": imageBase64, "language": language},
    );
  }

  Future<List<int>> download(String url) async {
    final headers = <String, String>{};
    if (url.startsWith(baseUrl) && _token != null && _token!.isNotEmpty) {
      headers["Authorization"] = "Bearer $_token";
    }
    final response = await _http
        .get(Uri.parse(url), headers: headers)
        .timeout(const Duration(seconds: 60));
    if (response.statusCode < 200 || response.statusCode >= 300) {
      throw ApiException(
        "Failed to download image (${response.statusCode})",
        response.statusCode,
      );
    }
    return response.bodyBytes;
  }

  List<Map<String, dynamic>> _list(Map<String, dynamic> response) {
    final values = response["recipes"];
    if (values is! List) return const [];
    return values
        .whereType<Map>()
        .map((item) => Map<String, dynamic>.from(item))
        .toList();
  }
}
