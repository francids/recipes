import "package:hive_ce/hive.dart";
import "package:the_recipes/models/recipe.dart";

part "hive_adapters.g.dart";

@GenerateAdapters([AdapterSpec<Recipe>()])
class HiveAdapters {}
