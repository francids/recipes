import "package:flutter_easyloading/flutter_easyloading.dart";
import "package:material_ui/material_ui.dart";

void configureEasyLoading() {
  EasyLoading.instance
    ..displayDuration = const Duration(milliseconds: 1000)
    ..indicatorType = EasyLoadingIndicatorType.fadingCircle
    ..loadingStyle = EasyLoadingStyle.custom
    ..indicatorSize = 45.0
    ..radius = 10.0
    ..progressColor = Colors.deepOrange
    ..backgroundColor = Colors.white
    ..indicatorColor = Colors.deepOrange
    ..textColor = Colors.black87
    ..maskColor = Colors.black.withAlpha(128)
    ..userInteractions = true
    ..dismissOnTap = true
    ..boxShadow = [
      BoxShadow(
        color: Colors.black.withAlpha(25),
        blurRadius: 10,
        offset: const Offset(0, 4),
      ),
    ]
    ..fontSize = 14.0
    ..textStyle = const TextStyle(
      fontWeight: FontWeight.w500,
      fontSize: 14.0,
      color: Colors.black87,
    )
    ..contentPadding = const EdgeInsets.symmetric(
      vertical: 15.0,
      horizontal: 20.0,
    );
}
