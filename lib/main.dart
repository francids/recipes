import "package:flutter/services.dart";
import "package:flutter_riverpod/flutter_riverpod.dart";
import "package:hive_ce_flutter/adapters.dart";
import "package:material_ui/material_ui.dart";
import "package:the_recipes/app.dart";
import "package:the_recipes/appwrite_config.dart";
import "package:the_recipes/easy_loading_config.dart";
import "package:the_recipes/hive/hive_adapters.dart";
import "package:the_recipes/hive_boxes.dart";
import "package:the_recipes/messages.dart";
import "package:the_recipes/models/recipe.dart";

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  await Messages.init();

  await Hive.initFlutter();
  Hive.registerAdapter(RecipeAdapter());
  await Hive.openBox<Recipe>(recipesBox);
  await Hive.openBox<String>(favoritesBox);
  await Hive.openBox(settingsBox);

  AppwriteConfig.client;

  await SystemChrome.setEnabledSystemUIMode(
    SystemUiMode.edgeToEdge,
    overlays: [SystemUiOverlay.top, SystemUiOverlay.bottom],
  );

  configureEasyLoading();

  runApp(const ProviderScope(child: TheRecipesApp()));
}
