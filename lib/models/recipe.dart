import "package:hive_ce_flutter/hive_flutter.dart";
import "package:uuid/uuid.dart";

class Recipe extends HiveObject {
  String id;
  String title;
  String description;
  String? image;
  List<String> ingredients;
  List<String> directions;
  int preparationTime; // in seconds
  String? ownerId;
  bool? isPublic;
  String? cloudId;

  Recipe({
    required this.id,
    required this.title,
    required this.description,
    this.image,
    required this.ingredients,
    required this.directions,
    this.preparationTime = 0,
    String? ownerId,
    this.isPublic = false,
    this.cloudId,
  }) : ownerId = ownerId ?? "",
       assert(ingredients.isNotEmpty),
       assert(directions.isNotEmpty);

  Recipe.fromMap(Map<String, dynamic> map)
    : id = map["id"]?.toString().isNotEmpty == true
          ? map["id"].toString()
          : const Uuid().v4(),
      title = map["title"] ?? "",
      description = map["description"] ?? "",
      image = map["image"],
      ingredients = List<String>.from(map["ingredients"] ?? []),
      directions = List<String>.from(map["directions"] ?? []),
      preparationTime = map["preparationTime"] ?? 0,
      ownerId = map["ownerId"] ?? "",
      isPublic = map["isPublic"] ?? false,
      cloudId = map["cloudId"] {
    assert(ingredients.isNotEmpty);
    assert(directions.isNotEmpty);
  }

  Map<String, dynamic> toMap() {
    return {
      "id": id,
      "title": title,
      "description": description,
      "image": image,
      "ingredients": ingredients,
      "directions": directions,
      "preparationTime": preparationTime,
      "ownerId": ownerId,
      "isPublic": isPublic,
      "cloudId": cloudId,
    };
  }
}
