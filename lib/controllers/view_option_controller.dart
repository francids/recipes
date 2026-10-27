import "package:flutter/foundation.dart";
import "package:flutter_riverpod/flutter_riverpod.dart";
import "package:hive_ce_flutter/adapters.dart";
import "package:the_recipes/hive_boxes.dart";
import "package:the_recipes/views/screens/recipes_page.dart";

class ViewOptionController extends Notifier<ViewOption> {
  static const viewOptionKey = "view_option_key";

  @override
  ViewOption build() {
    loadViewOption();
    return ViewOption.list;
  }

  void loadViewOption() async {
    try {
      if (!Hive.isBoxOpen(settingsBox)) {
        await Hive.openBox(settingsBox);
      }

      final box = Hive.box(settingsBox);
      final viewOptionString = box.get(
        viewOptionKey,
        defaultValue: ViewOption.list.toString(),
      );

      final viewOption = ViewOption.values.firstWhere(
        (e) => e.toString() == viewOptionString,
        orElse: () => ViewOption.list,
      );

      state = viewOption;
    } catch (e) {
      debugPrint("Error loading view option: $e");
    }
  }

  void toggleViewOption() async {
    final newViewOption = state == ViewOption.list
        ? ViewOption.grid
        : ViewOption.list;
    state = newViewOption;
    await _saveViewOption();
  }

  Future<void> _saveViewOption() async {
    try {
      final box = Hive.box(settingsBox);
      await box.put(viewOptionKey, state.toString());
    } catch (e) {
      debugPrint("Error saving view option: $e");
    }
  }
}

final viewOptionControllerProvider =
    NotifierProvider<ViewOptionController, ViewOption>(() {
      return ViewOptionController();
    });
