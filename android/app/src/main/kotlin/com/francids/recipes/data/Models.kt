package com.francids.recipes.data

import org.json.JSONArray
import org.json.JSONObject

/**
 * A recipe stored locally. [image] is either an absolute local file path or a
 * remote URL, mirroring the data model used by the previous Flutter app.
 */
data class Recipe(
    val id: String,
    val title: String,
    val description: String,
    val image: String? = null,
    val ingredients: List<String> = emptyList(),
    val directions: List<String> = emptyList(),
    val preparationTime: Int = 0,
    val ownerId: String = "",
    val isPublic: Boolean = false,
    val cloudId: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("description", description)
        put("image", image ?: JSONObject.NULL)
        put("ingredients", JSONArray(ingredients))
        put("directions", JSONArray(directions))
        put("preparationTime", preparationTime)
        put("ownerId", ownerId)
        put("isPublic", isPublic)
        put("cloudId", cloudId ?: JSONObject.NULL)
        put("createdAt", createdAt ?: JSONObject.NULL)
        put("updatedAt", updatedAt ?: JSONObject.NULL)
    }

    fun toApiJson(includeImage: String? = null): JSONObject = JSONObject().apply {
        put("title", title)
        put("description", description)
        put("ingredients", JSONArray(ingredients))
        put("directions", JSONArray(directions))
        put("preparationTime", preparationTime)
        put("isPublic", isPublic)
        if (!includeImage.isNullOrBlank()) put("image", includeImage)
    }

    companion object {
        fun fromJson(json: JSONObject): Recipe {
            fun stringList(key: String): List<String> {
                val array = json.optJSONArray(key) ?: return emptyList()
                return buildList {
                    for (index in 0 until array.length()) {
                        array.optString(index).takeIf { it.isNotEmpty() }?.let(::add)
                    }
                }
            }

            return Recipe(
                id = json.optString("id").ifBlank { java.util.UUID.randomUUID().toString() },
                title = json.optString("title"),
                description = json.optString("description"),
                image = json.optString("image").takeIf { it.isNotBlank() && it != "null" },
                ingredients = stringList("ingredients"),
                directions = stringList("directions"),
                preparationTime = json.optInt("preparationTime", 0),
                ownerId = json.optString("ownerId"),
                isPublic = json.optBoolean("isPublic", false),
                cloudId = json.optString("cloudId").takeIf { it.isNotBlank() && it != "null" },
                createdAt = json.optString("createdAt").takeIf { it.isNotBlank() && it != "null" },
                updatedAt = json.optString("updatedAt").takeIf { it.isNotBlank() && it != "null" },
            )
        }

        /** Maps an API response, using its [cloudId] (or id) as the cloud identity. */
        fun fromApi(json: JSONObject): Recipe {
            val cloudId = json.optString("cloudId").ifBlank { json.optString("id") }
            return fromJson(json).copy(cloudId = cloudId.ifBlank { null })
        }
    }
}

data class AppUser(
    val id: String,
    val name: String,
    val email: String,
    val picture: String?,
    val emailVerification: Boolean,
    val registration: String,
    val accessedAt: String,
) {
    companion object {
        fun fromJson(json: JSONObject): AppUser = AppUser(
            id = json.optString("id"),
            name = json.optString("name"),
            email = json.optString("email"),
            picture = json.optString("picture").takeIf { it.isNotBlank() && it != "null" },
            emailVerification = json.optBoolean("emailVerification", false),
            registration = json.optString("registration"),
            accessedAt = json.optString("accessedAt"),
        )
    }
}

data class RecipeDraft(
    val title: String = "",
    val description: String = "",
    val ingredients: List<String> = emptyList(),
    val directions: List<String> = emptyList(),
    val preparationTime: Int = 0,
    val imagePath: String = "",
) {
    val hasData: Boolean
        get() = title.isNotBlank() || description.isNotBlank() || imagePath.isNotBlank() ||
            preparationTime > 0 || ingredients.isNotEmpty() || directions.isNotEmpty()
}

enum class ViewOption { LIST, GRID }

enum class SortOption { NONE, TITLE, DURATION }

data class PublicSearchState(
    val recipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

data class UiMessage(val text: String, val isError: Boolean)

data class ConfirmRequest(
    val title: String,
    val message: String,
    val confirmLabel: String,
    val cancelLabel: String,
    val destructive: Boolean = false,
    val animationAsset: String = "lottie/alert.json",
    val onConfirm: () -> Unit,
)

data class LoadingRequest(
    val title: String,
    val message: String,
    val animationAsset: String = "lottie/loading.json",
)

sealed interface DeepLinkTarget {
    data class LocalRecipe(val recipeId: String) : DeepLinkTarget
    data class SharedRecipe(val recipe: Recipe) : DeepLinkTarget
}
