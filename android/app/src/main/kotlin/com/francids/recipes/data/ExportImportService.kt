package com.francids.recipes.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class ImportResult(
    val importedCount: Int,
    val skippedCount: Int,
    val errors: List<String>,
)

class ExportImportService(private val store: LocalStore) {

    suspend fun buildExportZip(recipes: List<Recipe>): ByteArray = withContext(Dispatchers.IO) {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            val recipesJson = JSONArray()
            val addedImages = mutableSetOf<String>()

            recipes.forEach { recipe ->
                var imageFileName = ""
                val imagePath = recipe.image
                if (!imagePath.isNullOrBlank() && !imagePath.startsWith("http")) {
                    val imageFile = File(imagePath)
                    if (imageFile.exists()) {
                        val originalName = imageFile.name
                        imageFileName = "${recipe.id}_$originalName"
                        if (addedImages.add(imageFileName)) {
                            zip.putNextEntry(ZipEntry("images/$imageFileName"))
                            imageFile.inputStream().use { it.copyTo(zip) }
                            zip.closeEntry()
                        }
                    }
                }

                recipesJson.put(JSONObject().apply {
                    put("id", recipe.id)
                    put("title", recipe.title)
                    put("description", recipe.description)
                    put("ingredients", JSONArray(recipe.ingredients))
                    put("directions", JSONArray(recipe.directions))
                    put("preparationTime", recipe.preparationTime)
                    put("image", imageFileName)
                    put("exportDate", isoNow())
                })
            }

            val exportJson = JSONObject().apply {
                put("recipes", recipesJson)
                put("exportDate", isoNow())
                put("totalRecipes", recipes.size)
                put("appVersion", "2.0.0")
                put("format", "compressed_with_images")
            }

            zip.putNextEntry(ZipEntry("recipes.json"))
            zip.write(exportJson.toString(2).toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }
        output.toByteArray()
    }

    suspend fun importZip(
        bytes: ByteArray,
        existingRecipes: List<Recipe>,
        ownerId: String,
    ): ImportResult = withContext(Dispatchers.IO) {
        val entries = mutableMapOf<String, ByteArray>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    entries[entry.name] = zip.readBytes()
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }

        val recipesBytes = entries["recipes.json"]
            ?: throw IllegalArgumentException("Invalid file format")
        val root = JSONObject(recipesBytes.toString(Charsets.UTF_8))
        val recipesData = root.optJSONArray("recipes")
            ?: throw IllegalArgumentException("Invalid file format")

        val workingRecipes = existingRecipes.toMutableList()
        val titles = existingRecipes.map { it.title.lowercase() }.toMutableSet()
        var imported = 0
        var skipped = 0
        val errors = mutableListOf<String>()

        for (index in 0 until recipesData.length()) {
            val item = recipesData.optJSONObject(index)
            if (item == null) {
                errors += "Invalid recipe format"
                continue
            }

            val title = item.optString("title")
            if (titles.contains(title.lowercase())) {
                skipped++
                continue
            }

            val ingredients = stringList(item, "ingredients")
            val directions = stringList(item, "directions")
            if (title.isBlank() || ingredients.isEmpty() || directions.isEmpty()) {
                errors += "Recipe '$title' is incomplete and was skipped"
                continue
            }

            val newId = UUID.randomUUID().toString()
            var imagePath = ""
            val originalImageName = item.optString("image")
            if (originalImageName.isNotBlank()) {
                val imageBytes = entries["images/$originalImageName"]
                if (imageBytes != null) {
                    val extension = originalImageName.substringAfterLast('.', "")
                        .let { if (it.isBlank()) ".jpg" else ".$it" }
                    imagePath = store.writeRecipeImage(imageBytes, extension) ?: ""
                }
            }

            val recipe = Recipe(
                id = newId,
                title = title,
                description = item.optString("description"),
                image = imagePath.ifBlank { null },
                ingredients = ingredients,
                directions = directions,
                preparationTime = item.optInt("preparationTime", 0),
                ownerId = ownerId,
                isPublic = false,
                cloudId = null,
            )

            workingRecipes += recipe
            titles += title.lowercase()
            imported++
        }

        if (imported > 0) store.saveRecipes(workingRecipes)
        ImportResult(imported, skipped, errors)
    }

    private fun stringList(json: JSONObject, key: String): List<String> {
        val array = json.optJSONArray(key) ?: return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                array.optString(index).takeIf { it.isNotBlank() }?.let(::add)
            }
        }
    }

    private fun isoNow(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(Date())
    }
}
