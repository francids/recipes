package com.francids.recipes.data

import com.francids.recipes.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class ApiException(val statusCode: Int, message: String) : Exception(message)

class ApiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    var token: String? = null

    val baseUrl: String = BuildConfig.API_BASE_URL.trimEnd('/')

    val googleSignInUrl: String
        get() {
            val returnTo = URLEncoder.encode("recipes://auth", Charsets.UTF_8.name())
            return "$baseUrl/auth/google?return_to=$returnTo"
        }

    suspend fun me(): AppUser = withContext(Dispatchers.IO) {
        AppUser.fromJson(request("GET", "/auth/me"))
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        request("POST", "/auth/logout")
        Unit
    }

    suspend fun deleteAccount() = withContext(Dispatchers.IO) {
        request("DELETE", "/account")
        Unit
    }

    suspend fun listRecipes(): List<Recipe> = withContext(Dispatchers.IO) {
        val response = request("GET", "/recipes")
        val array = response.optJSONArray("recipes") ?: return@withContext emptyList()
        buildList {
            for (index in 0 until array.length()) {
                array.optJSONObject(index)?.let { add(Recipe.fromApi(it)) }
            }
        }
    }

    suspend fun createRecipe(payload: JSONObject): JSONObject = withContext(Dispatchers.IO) {
        request("POST", "/recipes", payload)
    }

    suspend fun updateRecipe(id: String, payload: JSONObject): JSONObject =
        withContext(Dispatchers.IO) {
            request("PATCH", "/recipes/${encodePath(id)}", payload)
        }

    suspend fun deleteRecipe(id: String) = withContext(Dispatchers.IO) {
        request("DELETE", "/recipes/${encodePath(id)}")
        Unit
    }

    suspend fun searchPublicRecipes(query: String): List<Recipe> = withContext(Dispatchers.IO) {
        val encoded = URLEncoder.encode(query.trim(), Charsets.UTF_8.name())
            .replace("+", "%20")
        val response = request("GET", "/recipes/public?query=$encoded", authenticated = false)
        val array = response.optJSONArray("recipes") ?: return@withContext emptyList()
        buildList {
            for (index in 0 until array.length()) {
                array.optJSONObject(index)?.let { add(Recipe.fromApi(it)) }
            }
        }
    }

    suspend fun getPublicRecipe(id: String): Recipe = withContext(Dispatchers.IO) {
        Recipe.fromApi(request("GET", "/recipes/public/${encodePath(id)}", authenticated = false))
            .copy(cloudId = id)
    }

    suspend fun generateRecipe(imageBase64: String, language: String): JSONObject =
        withContext(Dispatchers.IO) {
            request(
                "POST",
                "/ai/recipes",
                JSONObject().apply {
                    put("image", imageBase64)
                    put("language", language)
                },
            )
        }

    suspend fun download(url: String): ByteArray = withContext(Dispatchers.IO) {
        val builder = Request.Builder().url(url).get()
        if (url.startsWith(baseUrl) && !token.isNullOrBlank()) {
            builder.header("Authorization", "Bearer $token")
        }
        client.newCall(builder.build()).execute().use { response ->
            if (!response.isSuccessful) {
                throw ApiException(
                    response.code,
                    "Failed to download image (${response.code})",
                )
            }
            response.body?.bytes() ?: ByteArray(0)
        }
    }

    private fun request(
        method: String,
        path: String,
        body: JSONObject? = null,
        authenticated: Boolean = true,
    ): JSONObject {
        val builder = Request.Builder().url("$baseUrl$path")
        if (body != null) {
            builder.header("Content-Type", "application/json")
            builder.method(method, body.toString().toRequestBody(jsonMediaType))
        } else {
            builder.method(method, if (method == "POST" || method == "PATCH") {
                "{}".toRequestBody(jsonMediaType)
            } else {
                null
            })
        }

        if (authenticated && !token.isNullOrBlank()) {
            builder.header("Authorization", "Bearer $token")
        }

        client.newCall(builder.build()).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (response.isSuccessful) {
                if (responseBody.isBlank()) return JSONObject()
                return runCatching { JSONObject(responseBody) }.getOrElse {
                    JSONObject().put("data", responseBody)
                }
            }

            val message = runCatching {
                JSONObject(responseBody).optString("error")
            }.getOrNull()?.takeIf { it.isNotBlank() }
                ?: responseBody.takeIf { it.isNotBlank() }
                ?: "Request failed (${response.code})"
            throw ApiException(response.code, message)
        }
    }

    private fun encodePath(value: String): String =
        URLEncoder.encode(value, Charsets.UTF_8.name()).replace("+", "%20")
}
