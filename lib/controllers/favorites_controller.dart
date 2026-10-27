import "package:flutter_riverpod/flutter_riverpod.dart";
import "package:hive_ce_flutter/hive_flutter.dart";
import "package:the_recipes/hive_boxes.dart";

class FavoritesState {
  final List<String> favoriteRecipeIds;
  final bool showFavoritesOnly;

  FavoritesState({
    required this.favoriteRecipeIds,
    required this.showFavoritesOnly,
  });

  FavoritesState copyWith({
    List<String>? favoriteRecipeIds,
    bool? showFavoritesOnly,
  }) {
    return FavoritesState(
      favoriteRecipeIds: favoriteRecipeIds ?? this.favoriteRecipeIds,
      showFavoritesOnly: showFavoritesOnly ?? this.showFavoritesOnly,
    );
  }
}

class FavoritesController extends AsyncNotifier<FavoritesState> {
  @override
  Future<FavoritesState> build() async {
    return await _loadFavorites();
  }

  Future<FavoritesState> _loadFavorites() async {
    if (!Hive.isBoxOpen(favoritesBox)) {
      await Hive.openBox<String>(favoritesBox);
    }
    final box = Hive.box<String>(favoritesBox);
    return FavoritesState(
      favoriteRecipeIds: box.values.toList(),
      showFavoritesOnly: false,
    );
  }

  Future<void> toggleFavorite(String recipeId) async {
    final currentState = await future;

    if (!Hive.isBoxOpen(favoritesBox)) {
      await Hive.openBox<String>(favoritesBox);
    }
    final box = Hive.box<String>(favoritesBox);
    final currentFavorites = List<String>.from(currentState.favoriteRecipeIds);

    if (currentFavorites.contains(recipeId)) {
      await box.delete(recipeId);
      currentFavorites.remove(recipeId);
    } else {
      await box.put(recipeId, recipeId);
      currentFavorites.add(recipeId);
    }

    state = AsyncValue.data(
      currentState.copyWith(favoriteRecipeIds: currentFavorites),
    );
  }

  Future<bool> isFavorite(String recipeId) async {
    final currentState = await future;
    return currentState.favoriteRecipeIds.contains(recipeId);
  }

  Future<void> toggleFilter() async {
    final currentState = await future;
    state = AsyncValue.data(
      currentState.copyWith(showFavoritesOnly: !currentState.showFavoritesOnly),
    );
  }

  Future<void> clearFilter() async {
    final currentState = await future;
    state = AsyncValue.data(currentState.copyWith(showFavoritesOnly: false));
  }

  Future<void> setFilter(bool showFavoritesOnly) async {
    final currentState = await future;
    state = AsyncValue.data(
      currentState.copyWith(showFavoritesOnly: showFavoritesOnly),
    );
  }
}

final favoritesControllerProvider =
    AsyncNotifierProvider<FavoritesController, FavoritesState>(() {
      return FavoritesController();
    });
