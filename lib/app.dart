import "package:app_links/app_links.dart";
import "package:flutter_easyloading/flutter_easyloading.dart";
import "package:flutter_riverpod/flutter_riverpod.dart";
import "package:material_ui/material_ui.dart";
import "package:the_recipes/controllers/auth_controller.dart";
import "package:the_recipes/controllers/language_controller.dart";
import "package:the_recipes/controllers/share_recipe_controller.dart";
import "package:the_recipes/controllers/theme_controller.dart";
import "package:the_recipes/messages.dart";
import "package:the_recipes/the_recipe_app_theme.dart";
import "package:the_recipes/views/screens/inicial_screen.dart";
import "package:the_recipes/views/screens/recipe_screen.dart";
import "package:the_recipes/views/screens/shared_recipe_screen.dart";
import "package:the_recipes/views/widgets/ui_helpers.dart";

class TheRecipesApp extends ConsumerStatefulWidget {
  const TheRecipesApp({super.key});

  @override
  ConsumerState<TheRecipesApp> createState() => _TheRecipesAppState();
}

class _TheRecipesAppState extends ConsumerState<TheRecipesApp>
    with WidgetsBindingObserver {
  final _appLinks = AppLinks();

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _initDeepLinks();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  Future<void> _initDeepLinks() async {
    _appLinks.uriLinkStream.listen((uri) {
      _handleDeepLink(uri);
    });

    WidgetsBinding.instance.addPostFrameCallback((_) async {
      await Future.delayed(const Duration(milliseconds: 500));
      final appLink = await _appLinks.getInitialLink();
      if (appLink != null) {
        await _handleDeepLink(appLink);
      }
    });
  }

  Future<void> _handleDeepLink(Uri uri) async {
    final bool isSharingLink =
        (uri.host == "sharing" ||
            uri.pathSegments.contains("recipe") ||
            uri.pathSegments.contains("sharing")) &&
        uri.queryParameters.containsKey("id");

    if (isSharingLink) {
      final recipeCloudId = uri.queryParameters["id"];

      if (recipeCloudId != null) {
        final shareController = ref.read(
          shareRecipeControllerProvider.notifier,
        );
        final localRecipe = shareController.findLocalRecipeByCloudId(
          recipeCloudId,
        );

        if (localRecipe != null) {
          WidgetsBinding.instance.addPostFrameCallback((_) {
            Navigator.of(context).push(
              MaterialPageRoute(
                builder: (context) => RecipeScreen(recipe: localRecipe),
              ),
            );
          });
        } else {
          try {
            final recipeShared = await shareController.getSharedRecipe(
              recipeCloudId,
            );
            WidgetsBinding.instance.addPostFrameCallback((_) {
              Navigator.of(context).push(
                MaterialPageRoute(
                  builder: (context) =>
                      SharedRecipeScreen(recipe: recipeShared),
                ),
              );
            });
          } catch (e) {
            WidgetsBinding.instance.addPostFrameCallback((_) {
              if (mounted) {
                UIHelpers.showErrorSnackbar(
                  "shared_recipe.not_found_error".tr,
                  context,
                );
              }
            });
          }
        }
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final language = ref.watch(languageControllerProvider);
    final themeState = ref.watch(themeControllerProvider);
    ref.watch(authControllerProvider);

    ThemeMode themeMode;
    if (themeState.followSystemTheme) {
      themeMode = ThemeMode.system;
    } else {
      themeMode = themeState.isDarkMode ? ThemeMode.dark : ThemeMode.light;
    }

    return MaterialApp(
      onGenerateTitle: (context) => Messages.appName,
      debugShowCheckedModeBanner: false,
      color: Colors.deepOrange,
      theme: TheRecipeAppTheme.theme,
      darkTheme: TheRecipeAppTheme.darkTheme,
      themeMode: themeMode,
      home: const InicialScreen(),
      builder: (context, child) {
        // EasyLoading still uses the legacy Material library while its
        // ecosystem migration is in progress.
        // ignore: deprecated_member_use
        return MaterialUiCompatibilityBridge(
          child: EasyLoading.init()(context, child),
        );
      },
      locale: Locale(language),
      supportedLocales: const [
        Locale("es"),
        Locale("en"),
        Locale("de"),
        Locale("it"),
        Locale("fr"),
        Locale("pt"),
        Locale("zh"),
        Locale("ja"),
        Locale("ko"),
      ],
      localizationsDelegates: GlobalMaterialLocalizations.delegates,
    );
  }
}
