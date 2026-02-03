import "dart:io";

import "package:the_recipes/models/recipe.dart";
import "package:flutter/material.dart";
import "package:the_recipes/views/screens/recipe_screen.dart";
import "package:flutter_animate/flutter_animate.dart";
import "package:the_recipes/views/screens/recipes_page.dart";

class RecipeCard extends StatelessWidget {
  const RecipeCard({
    super.key,
    required this.recipe,
    this.viewOption = ViewOption.list,
    this.onTap,
  });

  final Recipe recipe;
  final ViewOption viewOption;
  final VoidCallback? onTap;

  Widget _buildImage(BuildContext context) {
    final isGridView = viewOption == ViewOption.grid;
    final imageWidth = isGridView ? null : 80.0;
    final imageHeight = isGridView ? null : 80.0;

    if (recipe.image == null || recipe.image!.isEmpty) {
      return Container(
        width: imageWidth,
        height: imageHeight,
        color: Colors.grey[300],
        child: Icon(
          Icons.image_not_supported,
          color: Colors.grey[600],
        ),
      );
    }

    ImageProvider imageProvider;
    if (recipe.image!.startsWith("http://") ||
        recipe.image!.startsWith("https://")) {
      imageProvider = NetworkImage(recipe.image!);
    } else {
      imageProvider = FileImage(File(recipe.image!));
    }

    return Image(
      image: imageProvider,
      width: imageWidth,
      height: imageHeight,
      fit: BoxFit.cover,
      errorBuilder: (context, error, stackTrace) {
        return Container(
          width: imageWidth,
          height: imageHeight,
          color: Colors.grey[300],
          child: Icon(
            Icons.image_not_supported,
            color: Colors.grey[600],
          ),
        );
      },
      loadingBuilder: (context, child, loadingProgress) {
        if (loadingProgress == null) return child;
        return Container(
          width: imageWidth,
          height: imageHeight,
          color: Colors.grey[200],
          child: Center(
            child: CircularProgressIndicator(
              value: loadingProgress.expectedTotalBytes != null
                  ? loadingProgress.cumulativeBytesLoaded /
                      loadingProgress.expectedTotalBytes!
                  : null,
            ),
          ),
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    final isDarkMode = Theme.of(context).brightness == Brightness.dark;
    final isGridView = viewOption == ViewOption.grid;

    Widget cardContent;

    if (isGridView) {
      cardContent = Stack(
        alignment: Alignment.bottomLeft,
        children: [
          Positioned.fill(
            child: Container(
              clipBehavior: Clip.hardEdge,
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(10),
              ),
              child: _buildImage(context),
            ),
          ),
          Positioned(
            bottom: 0,
            left: 0,
            right: 0,
            child: IgnorePointer(
              child: Container(
                height: MediaQuery.of(context).size.height * 0.2,
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    begin: Alignment.topCenter,
                    end: Alignment.bottomCenter,
                    colors: [
                      Colors.black.withValues(alpha: 0.0),
                      Colors.black.withValues(alpha: isDarkMode ? 0.24 : 0.08),
                      Colors.black.withValues(alpha: isDarkMode ? 0.47 : 0.15),
                      Colors.black.withValues(alpha: isDarkMode ? 0.78 : 0.25),
                    ],
                    stops: const [0.0, 0.3, 0.7, 1.0],
                  ),
                ),
              ),
            ),
          ),
          Positioned(
            bottom: 0,
            left: 0,
            right: 0,
            child: Container(
              decoration: BoxDecoration(
                borderRadius: const BorderRadius.only(
                  bottomLeft: Radius.circular(10),
                  bottomRight: Radius.circular(10),
                ),
                gradient: LinearGradient(
                  colors: [
                    Colors.black.withAlpha(179),
                    Colors.black.withAlpha(0),
                  ],
                  begin: Alignment.bottomCenter,
                  end: Alignment.topCenter,
                  stops: const [0.0, 0.6],
                ),
              ),
              padding: const EdgeInsets.all(8.0),
              child: Text(
                recipe.title,
                style: Theme.of(context).textTheme.displayMedium?.copyWith(
                      color: Colors.white,
                      fontSize: 16,
                      fontWeight: FontWeight.bold,
                    ),
                overflow: TextOverflow.ellipsis,
                maxLines: 2,
              ),
            ),
          ),
        ],
      );
    } else {
      cardContent = Row(
        children: [
          Container(
            clipBehavior: Clip.hardEdge,
            decoration: BoxDecoration(
              borderRadius: const BorderRadius.only(
                topLeft: Radius.circular(10),
                bottomLeft: Radius.circular(10),
              ),
              border: Border(
                right: BorderSide(
                  width: 0.5,
                  color: isDarkMode ? Colors.white30 : Colors.black26,
                ),
              ),
              color: Theme.of(context).colorScheme.surfaceContainerLow,
            ),
            child: _buildImage(context),
          ),
          Expanded(
            child: Padding(
              padding: const EdgeInsets.only(
                left: 12,
                right: 8,
                top: 8,
                bottom: 8,
              ),
              child: Column(
                spacing: 8,
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(
                    recipe.title,
                    style: Theme.of(context).textTheme.displayMedium,
                    overflow: TextOverflow.ellipsis,
                    maxLines: 1,
                  ),
                  Text(
                    recipe.description,
                    style: Theme.of(context).textTheme.bodySmall,
                    overflow: TextOverflow.ellipsis,
                    maxLines: 2,
                  ),
                ],
              ),
            ),
          ),
        ],
      );
    }

    return Container(
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(10),
        color: Theme.of(context).colorScheme.surface,
        boxShadow: [
          BoxShadow(
            color: isDarkMode
                ? Colors.black.withAlpha(30)
                : Colors.grey.withAlpha(30),
            blurRadius: 4,
            blurStyle: BlurStyle.outer,
            spreadRadius: 0,
            offset: const Offset(0, 1),
          ),
        ],
      ),
      child: Card(
        margin: const EdgeInsets.all(0),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(10),
          side: BorderSide(
            color: isDarkMode ? Colors.white30 : Colors.black26,
            width: 0.5,
            strokeAlign: BorderSide.strokeAlignInside,
          ),
        ),
        color: Colors.transparent,
        elevation: 0,
        clipBehavior: Clip.antiAlias,
        child: InkWell(
          borderRadius: BorderRadius.circular(10),
          onTap: onTap ??
              () {
                Navigator.of(context).push(
                  MaterialPageRoute(
                    builder: (context) => RecipeScreen(recipe: recipe),
                  ),
                );
              },
          child: cardContent,
        ),
      )
          .animate()
          .fadeIn(duration: 500.ms)
          .slideX(begin: -0.2, duration: 400.ms, curve: Curves.easeOutCubic),
    );
  }
}
