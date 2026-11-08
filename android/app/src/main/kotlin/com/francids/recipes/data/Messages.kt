package com.francids.recipes.data

import android.content.Context
import org.json.JSONObject

/**
 * Loads the JSON translation files that were already part of the Flutter app.
 * Keeping the same files means every existing translation is preserved.
 */
object Messages {
    val supportedLanguages = listOf("es", "en", "de", "it", "fr", "pt", "zh", "ja", "ko")

    private val translations = mutableMapOf<String, Map<String, String>>()

    private val appNames = mapOf(
        "es" to "Recetas",
        "en" to "Recipes",
        "de" to "Rezepte",
        "it" to "Ricette",
        "fr" to "Recettes",
        "pt" to "Receitas",
        "zh" to "食谱",
        "ja" to "レシピ",
        "ko" to "레시피",
    )

    fun load(context: Context) {
        if (translations.isNotEmpty()) return
        supportedLanguages.forEach { language ->
            runCatching {
                val text = context.assets.open("messages/$language.json")
                    .bufferedReader()
                    .use { it.readText() }
                translations[language] = flatten(JSONObject(text))
            }
        }
        if (!translations.containsKey("en")) {
            translations["en"] = emptyMap()
        }
    }

    fun appName(language: String): String =
        appNames[language.substringBefore('-')] ?: appNames["en"] ?: "Recipes"

    fun translate(language: String, key: String, args: List<Any?> = emptyList()): String {
        val code = language.substringBefore('-')
        var value = translations[code]?.get(key)
            ?: translations["en"]?.get(key)
            ?: key

        args.forEachIndexed { index, arg ->
            value = value.replace("{$index}", arg?.toString() ?: "")
        }
        return value
    }

    private fun flatten(json: JSONObject): Map<String, String> {
        val result = mutableMapOf<String, String>()

        fun visit(prefix: String, value: Any?) {
            when (value) {
                is JSONObject -> {
                    val keys = value.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        visit(if (prefix.isEmpty()) key else "$prefix.$key", value.get(key))
                    }
                }

                is String -> result[prefix] = value
                else -> if (prefix.isNotEmpty() && value != null) result[prefix] = value.toString()
            }
        }

        visit("", json)
        return result
    }
}
