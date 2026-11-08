package com.francids.recipes.data

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import java.io.File
import java.util.UUID

/**
 * Small, dependency-free persistence layer. Recipes and favorites are stored
 * as JSON, while images are copied into the app's private files directory.
 */
class LocalStore(private val context: Context) {
    private val preferences =
        context.getSharedPreferences("recipes_settings", Context.MODE_PRIVATE)
    private val recipesFile = File(context.filesDir, "recipes.json")
    private val favoritesFile = File(context.filesDir, "favorites.json")
    private val imagesDirectory = File(context.filesDir, "recipe-images").apply { mkdirs() }
    private val draftDirectory = File(context.cacheDir, "recipe-drafts").apply { mkdirs() }

    var token: String?
        get() = preferences.getString(KEY_TOKEN, null)
        set(value) {
            preferences.edit().apply {
                if (value.isNullOrBlank()) remove(KEY_TOKEN) else putString(KEY_TOKEN, value)
            }.apply()
        }

    var autoSync: Boolean
        get() = preferences.getBoolean(KEY_AUTO_SYNC, false)
        set(value) = preferences.edit().putBoolean(KEY_AUTO_SYNC, value).apply()

    var language: String?
        get() = preferences.getString(KEY_LANGUAGE, null)
        set(value) {
            preferences.edit().apply {
                if (value.isNullOrBlank()) remove(KEY_LANGUAGE) else putString(KEY_LANGUAGE, value)
            }.apply()
        }

    var darkTheme: Boolean
        get() = preferences.getBoolean(KEY_DARK_THEME, false)
        set(value) = preferences.edit().putBoolean(KEY_DARK_THEME, value).apply()

    var followSystemTheme: Boolean
        get() = preferences.getBoolean(KEY_FOLLOW_SYSTEM, true)
        set(value) = preferences.edit().putBoolean(KEY_FOLLOW_SYSTEM, value).apply()

    var viewOption: ViewOption
        get() = runCatching {
            ViewOption.valueOf(
                preferences.getString(KEY_VIEW_OPTION, ViewOption.LIST.name)
                    ?: ViewOption.LIST.name,
            )
        }.getOrDefault(ViewOption.LIST)
        set(value) = preferences.edit().putString(KEY_VIEW_OPTION, value.name).apply()

    @Synchronized
    fun loadRecipes(): MutableList<Recipe> {
        if (!recipesFile.exists()) return mutableListOf()
        return runCatching {
            val array = JSONArray(recipesFile.readText())
            buildList {
                for (index in 0 until array.length()) {
                    array.optJSONObject(index)?.let { add(Recipe.fromJson(it)) }
                }
            }.toMutableList()
        }.getOrDefault(mutableListOf())
    }

    @Synchronized
    fun saveRecipes(recipes: List<Recipe>) {
        val array = JSONArray()
        recipes.forEach { array.put(it.toJson()) }
        val temporary = File(recipesFile.parentFile, "${recipesFile.name}.tmp")
        temporary.writeText(array.toString())
        if (!temporary.renameTo(recipesFile)) {
            recipesFile.writeText(array.toString())
            temporary.delete()
        }
    }

    @Synchronized
    fun loadFavorites(): Set<String> {
        if (!favoritesFile.exists()) return emptySet()
        return runCatching {
            val array = JSONArray(favoritesFile.readText())
            buildSet {
                for (index in 0 until array.length()) {
                    array.optString(index).takeIf { it.isNotBlank() }?.let(::add)
                }
            }
        }.getOrDefault(emptySet())
    }

    @Synchronized
    fun saveFavorites(favorites: Set<String>) {
        favoritesFile.writeText(JSONArray(favorites.toList()).toString())
    }

    fun copyDraftImage(uri: Uri): String? {
        return runCatching {
            val extension = extensionFor(context.contentResolver.getType(uri))
            val destination = File(draftDirectory, "${UUID.randomUUID()}$extension")
            context.contentResolver.openInputStream(uri)?.use { input ->
                destination.outputStream().use { output -> input.copyTo(output) }
            } ?: return null
            destination.absolutePath
        }.getOrNull()
    }

    fun persistRecipeImage(sourcePath: String): String? {
        if (sourcePath.isBlank()) return null
        val source = File(sourcePath)
        if (!source.exists()) return null
        if (source.parentFile == imagesDirectory) return source.absolutePath
        return runCatching {
            val destination = File(
                imagesDirectory,
                "${UUID.randomUUID()}_${source.name}",
            )
            source.copyTo(destination, overwrite = true)
            destination.absolutePath
        }.getOrNull()
    }

    fun writeRecipeImage(bytes: ByteArray, extension: String = ".jpg"): String? {
        return runCatching {
            val destination = File(imagesDirectory, "${UUID.randomUUID()}$extension")
            destination.writeBytes(bytes)
            destination.absolutePath
        }.getOrNull()
    }

    fun deleteImage(path: String?) {
        if (path.isNullOrBlank() || path.startsWith("http")) return
        runCatching { File(path).takeIf { it.exists() }?.delete() }
    }

    fun deleteDraftImage(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching {
            val file = File(path)
            if (file.parentFile == draftDirectory) file.delete()
        }
    }

    fun clearToken() {
        token = null
    }

    private fun extensionFor(mimeType: String?): String = when (mimeType) {
        "image/png" -> ".png"
        "image/webp" -> ".webp"
        "image/gif" -> ".gif"
        else -> ".jpg"
    }

    private companion object {
        const val KEY_TOKEN = "api_session_token"
        const val KEY_AUTO_SYNC = "autoSync"
        const val KEY_LANGUAGE = "language_key"
        const val KEY_DARK_THEME = "dark_theme"
        const val KEY_FOLLOW_SYSTEM = "follow_system_theme"
        const val KEY_VIEW_OPTION = "view_option_key"
    }
}
