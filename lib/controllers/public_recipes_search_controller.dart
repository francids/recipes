import "package:flutter_riverpod/flutter_riverpod.dart";
import "package:the_recipes/controllers/auth_controller.dart";
import "package:the_recipes/models/recipe.dart";
import "package:the_recipes/services/sync_service.dart";

class PublicRecipesSearchState {
  final List<Recipe> recipes;
  final bool isLoading;
  final String? error;

  PublicRecipesSearchState({
    this.recipes = const [],
    this.isLoading = false,
    this.error,
  });

  PublicRecipesSearchState copyWith({
    List<Recipe>? recipes,
    bool? isLoading,
    String? error,
  }) {
    return PublicRecipesSearchState(
      recipes: recipes ?? this.recipes,
      isLoading: isLoading ?? this.isLoading,
      error: error,
    );
  }
}

class PublicRecipesSearchController extends Notifier<PublicRecipesSearchState> {
  @override
  PublicRecipesSearchState build() {
    Future.microtask(() => searchRecipes(""));
    return PublicRecipesSearchState();
  }

  Future<void> searchRecipes(String query) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final authState = ref.read(authControllerProvider);
      final syncService = SyncService(authState);
      final recipes = await syncService.searchPublicRecipes(query);
      state = state.copyWith(recipes: recipes, isLoading: false);
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }
}

final publicRecipesSearchControllerProvider =
    NotifierProvider<PublicRecipesSearchController, PublicRecipesSearchState>(
  () => PublicRecipesSearchController(),
);
