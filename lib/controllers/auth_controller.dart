import "dart:async";

import "package:flutter/foundation.dart";
import "package:flutter_easyloading/flutter_easyloading.dart";
import "package:flutter_riverpod/flutter_riverpod.dart";
import "package:hive_ce_flutter/hive_flutter.dart";
import "package:recipes/api/api_client.dart";
import "package:recipes/hive_boxes.dart";
import "package:recipes/messages.dart";
import "package:recipes/models/app_user.dart";
import "package:recipes/services/sync_service.dart";
import "package:url_launcher/url_launcher.dart";

class AuthState {
  final AppUser? user;
  final String? userProfileImageUrl;
  final bool autoSyncEnabled;

  AuthState({
    this.user,
    this.userProfileImageUrl,
    this.autoSyncEnabled = false,
  });

  bool get isLoggedIn => user != null;

  AuthState copyWith({
    AppUser? user,
    String? userProfileImageUrl,
    bool? autoSyncEnabled,
    bool clearUser = false,
    bool clearProfileImage = false,
  }) {
    return AuthState(
      user: clearUser ? null : (user ?? this.user),
      userProfileImageUrl: clearProfileImage
          ? null
          : (userProfileImageUrl ?? this.userProfileImageUrl),
      autoSyncEnabled: autoSyncEnabled ?? this.autoSyncEnabled,
    );
  }
}

class AuthController extends Notifier<AuthState> {
  static const autoSyncKey = "autoSync";
  static const sessionTokenKey = "api_session_token";

  final _api = ApiClient.instance;

  @override
  AuthState build() {
    _initializeAuth();
    return AuthState(autoSyncEnabled: _getAutoSyncSetting());
  }

  Future<void> _initializeAuth() async {
    try {
      final token = _readSetting(sessionTokenKey) as String?;
      if (token == null || token.isEmpty) return;

      _api.setToken(token);
      final user = AppUser.fromMap(await _api.me());
      state = state.copyWith(
        user: user,
        userProfileImageUrl: user.picture,
        clearProfileImage: user.picture == null,
      );
    } catch (e) {
      debugPrint("No authenticated user: $e");
      _api.setToken(null);
      await _writeSetting(sessionTokenKey, null);
    }
  }

  bool _getAutoSyncSetting() {
    if (!Hive.isBoxOpen(settingsBox)) {
      Hive.openBox(settingsBox);
    }
    final box = Hive.box(settingsBox);
    return box.get(autoSyncKey, defaultValue: false) as bool;
  }

  dynamic _readSetting(String key) {
    if (!Hive.isBoxOpen(settingsBox)) {
      Hive.openBox(settingsBox);
    }
    return Hive.box(settingsBox).get(key);
  }

  Future<void> _writeSetting(String key, dynamic value) async {
    if (!Hive.isBoxOpen(settingsBox)) {
      await Hive.openBox(settingsBox);
    }
    final box = Hive.box(settingsBox);
    if (value == null) {
      await box.delete(key);
    } else {
      await box.put(key, value);
    }
  }

  Future<void> setAutoSyncEnabled(bool value) async {
    if (!Hive.isBoxOpen(settingsBox)) {
      await Hive.openBox(settingsBox);
    }
    final box = Hive.box(settingsBox);
    await box.put(autoSyncKey, value);
    state = state.copyWith(autoSyncEnabled: value);
  }

  Future<void> signInWithGoogle() async {
    try {
      unawaited(EasyLoading.show(status: "auth_controller.signing_in".tr));
      final launched = await launchUrl(
        Uri.parse(_api.googleSignInUrl),
        mode: LaunchMode.externalApplication,
      );
      if (!launched) {
        unawaited(EasyLoading.dismiss());
        unawaited(EasyLoading.showError("auth_controller.sign_in_error".tr));
      }
    } catch (e) {
      unawaited(EasyLoading.dismiss());
      debugPrint("General error: $e");
      unawaited(EasyLoading.showError("auth_controller.sign_in_error".tr));
    }
  }

  /// Called by the deep-link handler after the Google OAuth redirect.
  Future<void> completeSignIn({String? token, String? error}) async {
    unawaited(EasyLoading.dismiss());
    if (error != null || token == null || token.isEmpty) {
      unawaited(EasyLoading.showError("auth_controller.sign_in_error".tr));
      return;
    }

    try {
      _api.setToken(token);
      final user = AppUser.fromMap(await _api.me());
      await _writeSetting(sessionTokenKey, token);
      state = state.copyWith(
        user: user,
        userProfileImageUrl: user.picture,
        clearProfileImage: user.picture == null,
      );
    } catch (e) {
      debugPrint("Error completing sign in: $e");
      _api.setToken(null);
      await _writeSetting(sessionTokenKey, null);
      unawaited(EasyLoading.showError("auth_controller.sign_in_error".tr));
    }
  }

  Future<void> signOut() async {
    try {
      if (state.autoSyncEnabled) {
        await SyncService.clearLocalRecipesOnSignOut();
      }
      await setAutoSyncEnabled(false);

      try {
        await _api.logout();
      } catch (e) {
        debugPrint("Error closing remote session: $e");
      }
      _api.setToken(null);
      await _writeSetting(sessionTokenKey, null);

      state = state.copyWith(clearUser: true, clearProfileImage: true);
    } catch (e) {
      unawaited(
        EasyLoading.showError(
          "auth_controller.sign_out_error".trParams({"0": e.toString()}),
        ),
      );
    }
  }

  Future<void> deleteAccount() async {
    try {
      unawaited(
        EasyLoading.show(status: "auth_controller.deleting_account".tr),
      );

      if (state.user != null) {
        await SyncService.clearLocalRecipesOnSignOut();
        await _api.deleteAccount();
        await setAutoSyncEnabled(false);
        _api.setToken(null);
        await _writeSetting(sessionTokenKey, null);

        state = state.copyWith(clearUser: true, clearProfileImage: true);
        unawaited(EasyLoading.dismiss());
      }
    } catch (e) {
      unawaited(EasyLoading.dismiss());
      unawaited(
        EasyLoading.showError(
          "auth_controller.delete_account_error".trParams({"0": e.toString()}),
        ),
      );
    }
  }
}

final authControllerProvider = NotifierProvider<AuthController, AuthState>(() {
  return AuthController();
});
