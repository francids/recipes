package com.francids.recipes.data

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeJsonTest {
    @Test
    fun localRecipeRoundTripKeepsCloudIdNull() {
        val original = Recipe(
            id = "local-id",
            title = "Sopa",
            description = "Caliente",
            image = "/tmp/soup.jpg",
            ingredients = listOf("tomate", "sal"),
            directions = listOf("Cocinar"),
            preparationTime = 1200,
            ownerId = "user-1",
            isPublic = false,
            cloudId = null,
        )

        val decoded = Recipe.fromJson(original.toJson())

        assertEquals(original, decoded)
        assertNull(decoded.cloudId)
    }

    @Test
    fun apiResponseUsesCloudIdInsteadOfLocalId() {
        val response = JSONObject(
            """
            {
              "id": "cloud-123",
              "cloudId": "cloud-123",
              "title": "Pasta",
              "description": "Rápida",
              "ingredients": ["pasta"],
              "directions": ["Hervir"],
              "preparationTime": 600,
              "ownerId": "user-1",
              "isPublic": true
            }
            """.trimIndent(),
        )

        val recipe = Recipe.fromApi(response)

        assertEquals("cloud-123", recipe.cloudId)
        assertTrue(recipe.isPublic)
    }

    @Test
    fun apiJsonOnlyIncludesImageWhenProvided() {
        val recipe = Recipe(
            id = "id",
            title = "Tortilla",
            description = "Española",
            ingredients = listOf("huevo"),
            directions = listOf("Freír"),
            preparationTime = 900,
        )

        val withoutImage = recipe.toApiJson()
        assertFalse(withoutImage.has("image"))
        assertEquals(900, withoutImage.getInt("preparationTime"))

        val withImage = recipe.toApiJson("base64-data")
        assertEquals("base64-data", withImage.getString("image"))
    }
}
