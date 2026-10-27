import "package:material_ui/material_ui.dart";

class PressableButton extends StatefulWidget {
  final Widget child;

  const PressableButton({super.key, required this.child});

  @override
  State<PressableButton> createState() => PressableButtonState();
}

class PressableButtonState extends State<PressableButton> {
  bool _isPressed = false;

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTapDown: (_) => setState(() => _isPressed = true),
      onTapUp: (_) => setState(() => _isPressed = false),
      onTapCancel: () => setState(() => _isPressed = false),
      child: AnimatedScale(
        scale: _isPressed ? 0.975 : 1.0,
        duration: const Duration(milliseconds: 100),
        curve: Curves.easeInOut,
        child: widget.child,
      ),
    );
  }
}
