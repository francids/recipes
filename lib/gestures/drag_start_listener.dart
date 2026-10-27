import "package:flutter/gestures.dart";
import "package:material_ui/material_ui.dart";

class CustomDragStartListener extends ReorderableDelayedDragStartListener {
  const CustomDragStartListener({
    super.key,
    required super.child,
    required super.index,
    super.enabled,
  });

  @override
  MultiDragGestureRecognizer createRecognizer() {
    return DelayedMultiDragGestureRecognizer(
      delay: const Duration(milliseconds: 100),
      debugOwner: this,
      supportedDevices: const <PointerDeviceKind>{PointerDeviceKind.touch},
    );
  }
}
