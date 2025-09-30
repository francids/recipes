import "package:appwrite/appwrite.dart";
import "package:appwrite/models.dart" as models;
import "package:appwrite/enums.dart" as enums;
import "package:flutter/foundation.dart";
import "package:flutter_easyloading/flutter_easyloading.dart";
import "package:flutter_riverpod/flutter_riverpod.dart";
import "package:hive_ce_flutter/hive_flutter.dart";
import "package:the_recipes/hive_boxes.dart";
import "package:the_recipes/messages.dart";
import "package:the_recipes/services/sync_service.dart";
import "package:the_recipes/appwrite_config.dart";
import "package:http/http.dart" as http;
import "dart:convert";

class AuthState {
  final models.User? user;
  final String? userProfileImageUrl;
  final bool autoSyncEnabled;

  AuthState({
    this.user,
    this.userProfileImageUrl,
    this.autoSyncEnabled = false,
  });

  bool get isLoggedIn => user != null;

  AuthState copyWith({
    models.User? user,
    String? userProfileImageUrl,
    bool? autoSyncEnabled,
  }) {
    return AuthState(
      user: user ?? this.user,
      userProfileImageUrl: userProfileImageUrl ?? this.userProfileImageUrl,
      autoSyncEnabled: autoSyncEnabled ?? this.autoSyncEnabled,
    );
  }
}

class AuthController extends Notifier<AuthState> {
  static const autoSyncKey = "autoSync";
  final _account = AppwriteConfig.account;
  final _functions = AppwriteConfig.functions;

  @override
  AuthState build() {
    _checkCurrentUser();
    return AuthState(autoSyncEnabled: _getAutoSyncSetting());
  }

  bool _getAutoSyncSetting() {
    if (!Hive.isBoxOpen(settingsBox)) {
      Hive.openBox(settingsBox);
    }
    final box = Hive.box(settingsBox);
    return box.get(autoSyncKey, defaultValue: false) as bool;
  }

  Future<void> setAutoSyncEnabled(bool value) async {
    if (!Hive.isBoxOpen(settingsBox)) {
      await Hive.openBox(settingsBox);
    }
    final box = Hive.box(settingsBox);
    await box.put(autoSyncKey, value);
    state = state.copyWith(autoSyncEnabled: value);
  }

  Future<void> _checkCurrentUser() async {
    try {
      final user = await _account.get();
      await _loadUserProfileImage(user);
      state = state.copyWith(user: user);
    } catch (e) {
      state = state.copyWith(user: null, userProfileImageUrl: null);
    }
  }

  Future<void> _loadUserProfileImage(models.User user) async {
    try {
      models.Preferences preferences = await _account.getPrefs();
      final Map<String, dynamic> currentPrefs = preferences.data;
      final userProfileImageUrl = currentPrefs["profileImageUrl"] as String?;
      state = state.copyWith(userProfileImageUrl: userProfileImageUrl);
    } catch (e) {
      state = state.copyWith(userProfileImageUrl: null);
      debugPrint("Error loading user profile image: $e");
    }
  }

  Future<void> _storeUserProfileImage(String imageUrl) async {
    try {
      final preferences = await _account.getPrefs();
      final Map<String, dynamic> currentPrefs = preferences.data;
      currentPrefs["profileImageUrl"] = imageUrl;
      await _account.updatePrefs(
          prefs: Map<dynamic, dynamic>.from(currentPrefs));
      state = state.copyWith(userProfileImageUrl: imageUrl);
    } on AppwriteException catch (e) {
      debugPrint(
        "Error storing profile image URL in preferences: ${e.message}",
      );
    } catch (e) {
      debugPrint("Unexpected error storing profile image URL: $e");
    }
  }

  Future<void> signInWithGoogle() async {
    try {
      EasyLoading.show(status: "auth_controller.signing_in".tr);

      await _account.createOAuth2Session(
        provider: enums.OAuthProvider.google,
        scopes: ["profile", "email", "openid"],
      );

      await _checkCurrentUser();

      if (state.user != null) {
        final session = await _account.getSession(sessionId: "current");
        final providerAccessToken = session.providerAccessToken;

        final response = await http.get(
          Uri.parse("https://www.googleapis.com/oauth2/v3/userinfo"),
          headers: {
            "Authorization": "Bearer $providerAccessToken",
          },
        );

        if (response.statusCode == 200) {
          final Map<String, dynamic> userData = jsonDecode(response.body);
          final String? userImageUrl = userData["picture"];
          if (userImageUrl != null) {
            await _storeUserProfileImage(userImageUrl);
          }
        }
      }

      EasyLoading.dismiss();
    } on AppwriteException catch (e) {
      EasyLoading.dismiss();
      print("Appwrite error: ${e.code} - ${e.message}");
      EasyLoading.showError("auth_controller.sign_in_error".tr);
    } catch (e) {
      EasyLoading.dismiss();
      print("General error: $e");
      EasyLoading.showError("auth_controller.sign_in_error".tr);
    }
  }

  Future<void> signOut() async {
    try {
      if (state.autoSyncEnabled) {
        await SyncService.clearLocalRecipesOnSignOut();
      }
      await setAutoSyncEnabled(false);

      await _account.deleteSession(sessionId: "current");
      state = state.copyWith(user: null, userProfileImageUrl: null);
    } catch (e) {
      EasyLoading.showError(
        "auth_controller.sign_out_error".trParams(
          {
            "0": e.toString(),
          },
        ),
      );
    }
  }

  Future<void> deleteAccount() async {
    try {
      EasyLoading.show(status: "auth_controller.deleting_account".tr);

      if (state.user != null) {
        final syncService = SyncService(state);
        await syncService.deleteAllUserRecipesFromCloud();

        await setAutoSyncEnabled(false);

        await _functions.createExecution(
          functionId: "the-recipes-accounts",
          path: "/delete",
        );

        state = state.copyWith(user: null, userProfileImageUrl: null);
        EasyLoading.dismiss();
      }
    } catch (e) {
      EasyLoading.dismiss();
      EasyLoading.showError(
        "auth_controller.delete_account_error".trParams(
          {
            "0": e.toString(),
          },
        ),
      );
    }
  }
}

final authControllerProvider = NotifierProvider<AuthController, AuthState>(() {
  return AuthController();
});
