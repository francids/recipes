import "package:material_ui/material_ui.dart";
import "package:pretty_qr_code/pretty_qr_code.dart";
import "package:screen_brightness/screen_brightness.dart";
import "package:the_recipes/messages.dart";

class QrBottomSheet extends StatefulWidget {
  const QrBottomSheet({super.key, required this.url});

  final String url;

  static void show(BuildContext context, String url) {
    showModalBottomSheet(
      context: context,
      showDragHandle: true,
      backgroundColor: Theme.of(context).colorScheme.surface,
      isScrollControlled: true,
      constraints: BoxConstraints(
        maxHeight: MediaQuery.of(context).size.height * 0.9,
      ),
      builder: (context) => QrBottomSheet(url: url),
    );
  }

  @override
  State<QrBottomSheet> createState() => _QrBottomSheetState();
}

class _QrBottomSheetState extends State<QrBottomSheet> {
  double? _originalBrightness;

  @override
  void initState() {
    super.initState();
    _setBrightnessToMax();
  }

  @override
  void dispose() {
    _restoreOriginalBrightness();
    super.dispose();
  }

  Future<void> _setBrightnessToMax() async {
    try {
      _originalBrightness = await ScreenBrightness.instance.application;
      await ScreenBrightness.instance.setApplicationScreenBrightness(1.0);
    } catch (e) {
      debugPrint("Error setting brightness: $e");
    }
  }

  Future<void> _restoreOriginalBrightness() async {
    try {
      if (_originalBrightness != null) {
        await ScreenBrightness.instance.setApplicationScreenBrightness(
          _originalBrightness!,
        );
      } else {
        await ScreenBrightness.instance.resetApplicationScreenBrightness();
      }
    } catch (e) {
      debugPrint("Error restoring brightness: $e");
    }
  }

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      child: Padding(
        padding: EdgeInsets.only(
          top: 16.0,
          bottom: MediaQuery.of(context).padding.bottom + 16.0,
          left: 16.0,
          right: 16.0,
        ),
        child: Column(
          spacing: 8,
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text(
              "recipe_screen.qr_code".tr,
              style: Theme.of(context).appBarTheme.titleTextStyle,
              textAlign: TextAlign.start,
            ),
            Text(
              "recipe_screen.qr_code_description".tr,
              style: Theme.of(context).textTheme.bodyMedium,
              textAlign: TextAlign.start,
            ),
            const SizedBox(height: 8.0),
            Card(
              elevation: 0,
              margin: EdgeInsets.zero,
              clipBehavior: Clip.antiAlias,
              child: Padding(
                padding: const EdgeInsets.all(20.0),
                child: Center(
                  child: Container(
                    padding: const EdgeInsets.all(12.0),
                    decoration: BoxDecoration(
                      color: Theme.of(context).colorScheme.surface,
                      borderRadius: BorderRadius.circular(8.0),
                      border: Border.all(
                        color: Theme.of(context).colorScheme.outline,
                        width: 1,
                      ),
                    ),
                    child: PrettyQrView.data(
                      data: widget.url,
                      decoration: const PrettyQrDecoration(
                        shape: PrettyQrSmoothSymbol(
                          color: Color(0xFFEA580C),
                          roundFactor: 1,
                        ),
                        image: PrettyQrDecorationImage(
                          image: AssetImage("assets/logo/Icon.webp"),
                          position: PrettyQrDecorationImagePosition.embedded,
                        ),
                        background: Colors.transparent,
                        quietZone: PrettyQrQuietZone.pixels(4),
                      ),
                    ),
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
