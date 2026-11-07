import "package:flutter/services.dart";
import "package:flutter_riverpod/flutter_riverpod.dart";
import "package:hive_ce_flutter/adapters.dart";
import "package:material_ui/material_ui.dart";
import "package:recipes/app.dart";
import "package:recipes/easy_loading_config.dart";
import "package:recipes/hive/hive_adapters.dart";
import "package:recipes/hive_boxes.dart";
import "package:recipes/messages.dart";
import "package:recipes/models/recipe.dart";

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  await Messages.init();

  await Hive.initFlutter();
  Hive.registerAdapter(RecipeAdapter());
  await Hive.openBox<Recipe>(recipesBox);
  await Hive.openBox<String>(favoritesBox);
  await Hive.openBox(settingsBox);

  await SystemChrome.setEnabledSystemUIMode(
    SystemUiMode.edgeToEdge,
    overlays: [SystemUiOverlay.top, SystemUiOverlay.bottom],
  );

  configureEasyLoading();

  runApp(const ProviderScope(child: RecipesApp()));
}
