package com.francids.recipes.ui

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.francids.recipes.data.ApiClient
import com.francids.recipes.data.ApiException
import com.francids.recipes.data.AppUser
import com.francids.recipes.data.ConfirmRequest
import com.francids.recipes.data.DeepLinkTarget
import com.francids.recipes.data.ExportImportService
import com.francids.recipes.data.ImportResult
import com.francids.recipes.data.LoadingRequest
import com.francids.recipes.data.LocalStore
import com.francids.recipes.data.Messages
import com.francids.recipes.data.PublicSearchState
import com.francids.recipes.data.Recipe
import com.francids.recipes.data.RecipeDraft
import com.francids.recipes.data.UiMessage
import com.francids.recipes.data.ViewOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Locale
import java.util.UUID

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val store = LocalStore(application)
    private val api = ApiClient()
    private val exportImportService = ExportImportService(store)

    var recipes by mutableStateOf<List<Recipe>>(emptyList())
        private set

    var favorites by mutableStateOf<Set<String>>(emptySet())
        private set

    var user by mutableStateOf<AppUser?>(null)
        private set

    var autoSync by mutableStateOf(false)
        private set

    var darkTheme by mutableStateOf(false)
        private set

    var followSystemTheme by mutableStateOf(true)
        private set

    var language by mutableStateOf("en")
        private set

    var viewOption by mutableStateOf(ViewOption.LIST)
        private set

    var message by mutableStateOf<UiMessage?>(null)
        private set

    var loading by mutableStateOf<LoadingRequest?>(null)
        private set

    var confirmation by mutableStateOf<ConfirmRequest?>(null)
        private set

    var draft by mutableStateOf(RecipeDraft())
        private set

    var publicSearch by mutableStateOf(PublicSearchState())
        private set

    var pendingSharedRecipe by mutableStateOf<Recipe?>(null)
        private set

    var deepLinkTarget by mutableStateOf<DeepLinkTarget?>(null)
        private set

    var shareLoadingIds by mutableStateOf<Set<String>>(emptySet())
        private set

    val isLoggedIn: Boolean get() = user != null
    val appName: String get() = Messages.appName(language)
    val isDarkMode: Boolean get() = darkTheme

    init {
        Messages.load(application)
        language = store.language?.takeIf { it in Messages.supportedLanguages }
            ?: defaultLanguage()
        darkTheme = store.darkTheme
        followSystemTheme = store.followSystemTheme
        viewOption = store.viewOption
        autoSync = store.autoSync
        recipes = store.loadRecipes()
        favorites = store.loadFavorites()

        store.token?.let { savedToken ->
            api.token = savedToken
            viewModelScope.launch {
                runCatching { api.me() }
                    .onSuccess { user = it }
                    .onFailure {
                        api.token = null
                        store.clearToken()
                    }
            }
        }
    }

    fun tr(key: String, vararg args: Any?): String =
        Messages.translate(language, key, args.toList())

    fun consumeMessage() {
        message = null
    }

    fun showMessage(text: String, isError: Boolean = false) {
        message = UiMessage(text, isError)
    }

    fun dismissConfirmation() {
        confirmation = null
    }

    fun showConfirmation(request: ConfirmRequest) {
        confirmation = request
    }

    fun consumeDeepLink() {
        deepLinkTarget = null
    }

    fun clearPendingSharedRecipe() {
        pendingSharedRecipe = null
    }

    fun defaultLanguage(): String {
        val device = Locale.getDefault().language
        return if (device in Messages.supportedLanguages) device else "en"
    }

    fun changeLanguage(code: String) {
        if (code !in Messages.supportedLanguages) return
        language = code
        store.language = code
    }

    fun updateDarkTheme(enabled: Boolean) {
        darkTheme = enabled
        store.darkTheme = enabled
    }

    fun updateFollowSystemTheme(enabled: Boolean) {
        followSystemTheme = enabled
        store.followSystemTheme = enabled
    }

    fun toggleViewOption() {
        viewOption = if (viewOption == ViewOption.LIST) ViewOption.GRID else ViewOption.LIST
        store.viewOption = viewOption
    }

    fun refreshRecipes() {
        recipes = store.loadRecipes()
    }

    fun toggleFavorite(recipeId: String) {
        val updated = favorites.toMutableSet()
        if (!updated.add(recipeId)) updated.remove(recipeId)
        favorites = updated
        store.saveFavorites(updated)
    }

    fun isFavorite(recipeId: String): Boolean = favorites.contains(recipeId)

    fun recipeById(id: String): Recipe? = recipes.firstOrNull { it.id == id }

    // ---------------------------------------------------------------------
    // Add recipe
    // ---------------------------------------------------------------------

    fun updateDraftTitle(value: String) {
        draft = draft.copy(title = value)
    }

    fun updateDraftDescription(value: String) {
        draft = draft.copy(description = value)
    }

    fun updateDraftPreparationMinutes(value: Int) {
        draft = draft.copy(preparationTime = value.coerceAtLeast(0) * 60)
    }

    fun updateDraftIngredients(items: List<String>) {
        draft = draft.copy(ingredients = items)
    }

    fun updateDraftDirections(items: List<String>) {
        draft = draft.copy(directions = items)
    }

    fun setDraftImage(uri: Uri) {
        store.deleteDraftImage(draft.imagePath)
        val path = store.copyDraftImage(uri)
        if (path == null) {
            showMessage(tr("add_recipe_screen.error_saving"), true)
            return
        }
        draft = draft.copy(imagePath = path)
    }

    fun resetDraft() {
        store.deleteDraftImage(draft.imagePath)
        draft = RecipeDraft()
    }

    fun saveDraft(onFinished: (Boolean) -> Unit) {
        if (draft.title.isBlank() || draft.description.isBlank() ||
            draft.preparationTime <= 0 || draft.imagePath.isBlank() ||
            draft.ingredients.isEmpty() || draft.ingredients.any { it.isBlank() } ||
            draft.directions.isEmpty() || draft.directions.any { it.isBlank() }
        ) {
            showMessage(tr("add_recipe_screen.error_complete_all"), true)
            onFinished(false)
            return
        }

        viewModelScope.launch {
            loading = LoadingRequest(
                tr("add_recipe_screen.saving_recipe"),
                tr("add_recipe_screen.please_wait"),
            )
            try {
                val draftImagePath = draft.imagePath
                val savedImage = store.persistRecipeImage(draftImagePath) ?: ""
                val recipe = Recipe(
                    id = UUID.randomUUID().toString(),
                    title = draft.title.trim(),
                    description = draft.description.trim(),
                    image = savedImage.ifBlank { null },
                    ingredients = draft.ingredients.map { it.trim() },
                    directions = draft.directions.map { it.trim() },
                    preparationTime = draft.preparationTime,
                    ownerId = user?.id.orEmpty(),
                    isPublic = false,
                    cloudId = null,
                )
                val updated = recipes + recipe
                store.saveRecipes(updated)
                recipes = updated
                store.deleteDraftImage(draftImagePath)
                draft = RecipeDraft()

                if (isLoggedIn && autoSync) {
                    runCatching { syncRecipesToCloud() }
                        .onFailure { showMessage(tr("recipe_controller.sync_error", it.message), true) }
                }
                showMessage(tr("add_recipe_screen.recipe_saved"))
                onFinished(true)
            } catch (error: Exception) {
                showMessage(tr("add_recipe_screen.error_saving"), true)
                onFinished(false)
            } finally {
                loading = null
            }
        }
    }

    fun generateRecipeFromImage(onFinished: (Boolean) -> Unit) {
        val path = draft.imagePath
        if (path.isBlank()) return
        viewModelScope.launch {
            loading = LoadingRequest(
                tr("image_step.analyzing_image_title"),
                tr("image_step.analyzing_image_message"),
            )
            try {
                val image = withContext(Dispatchers.Default) {
                    encodeImage(path, maxDimension = 900, quality = 70)
                }
                val data = api.generateRecipe(image, language)
                draft = draft.copy(
                    title = data.optString("title"),
                    description = data.optString("description"),
                    ingredients = stringList(data, "ingredients"),
                    directions = stringList(data, "directions"),
                    preparationTime = data.optInt("preparationTime", 0),
                )
                showMessage(tr("image_step.recipe_generated_success"))
                onFinished(true)
            } catch (error: ApiException) {
                showMessage(aiErrorMessage(error), true)
                onFinished(false)
            } catch (error: Exception) {
                showMessage(
                    tr("image_step.recipe_generated_error", error.message ?: ""),
                    true,
                )
                onFinished(false)
            } finally {
                loading = null
            }
        }
    }

    // ---------------------------------------------------------------------
    // Recipe actions
    // ---------------------------------------------------------------------

    fun deleteRecipe(recipe: Recipe, onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            loading = LoadingRequest(
                tr("recipe_screen.deleting_recipe"),
                tr("recipe_screen.deleting_recipe_message"),
            )
            try {
                if (isLoggedIn && !recipe.cloudId.isNullOrBlank()) {
                    runCatching { api.deleteRecipe(recipe.cloudId) }
                }
                store.deleteImage(recipe.image)
                val updated = recipes.filterNot { it.id == recipe.id }
                store.saveRecipes(updated)
                recipes = updated
                onFinished()
            } catch (error: Exception) {
                showMessage(tr("recipe_controller.delete_error", error.message), true)
            } finally {
                loading = null
            }
        }
    }

    fun setRecipePublic(recipeId: String, isPublic: Boolean) {
        val recipe = recipeById(recipeId) ?: return
        if (!isLoggedIn) {
            showMessage(tr("auth_controller.sign_in_error"), true)
            return
        }
        if (recipe.cloudId.isNullOrBlank()) {
            showMessage(tr("sync_service.user_must_be_logged_in"), true)
            return
        }

        shareLoadingIds = shareLoadingIds + recipeId
        viewModelScope.launch {
            try {
                api.updateRecipe(recipe.cloudId, JSONObject().put("isPublic", isPublic))
                updateLocalRecipe(recipeId) { it.copy(isPublic = isPublic) }
            } catch (error: Exception) {
                showMessage(error.message ?: tr("ui_helpers.error"), true)
            } finally {
                shareLoadingIds = shareLoadingIds - recipeId
            }
        }
    }

    fun isRecipePublic(recipeId: String): Boolean =
        recipeById(recipeId)?.isPublic == true

    fun isShareLoading(recipeId: String): Boolean = recipeId in shareLoadingIds

    fun canShare(recipeId: String): Boolean =
        !recipeById(recipeId)?.cloudId.isNullOrBlank()

    fun generateShareUrl(recipeId: String): String {
        val cloudId = recipeById(recipeId)?.cloudId ?: return ""
        return "https://recipes.francids.com/sharing?id=$cloudId"
    }

    fun isRecipeAlreadySaved(sharedRecipe: Recipe): Boolean {
        if (!sharedRecipe.cloudId.isNullOrBlank() &&
            findLocalByCloudId(sharedRecipe.cloudId) != null
        ) {
            return true
        }

        val byTitle = findLocalByTitle(sharedRecipe.title) ?: return false
        return byTitle.ingredients.size == sharedRecipe.ingredients.size &&
            byTitle.ingredients.all { ingredient ->
                sharedRecipe.ingredients.any { it.equals(ingredient, ignoreCase = true) }
            }
    }

    fun saveSharedRecipe(sharedRecipe: Recipe, onFinished: (Boolean) -> Unit) {
        viewModelScope.launch {
            loading = LoadingRequest(
                tr("shared_recipe.saving_title"),
                tr("shared_recipe.saving_message"),
            )
            try {
                val newId = UUID.randomUUID().toString()
                var localImage = sharedRecipe.image.orEmpty()
                if (localImage.startsWith("http")) {
                    val bytes = api.download(localImage)
                    localImage = store.writeRecipeImage(bytes) ?: ""
                }
                val recipe = Recipe(
                    id = newId,
                    title = sharedRecipe.title,
                    description = sharedRecipe.description,
                    image = localImage.ifBlank { null },
                    ingredients = sharedRecipe.ingredients,
                    directions = sharedRecipe.directions,
                    preparationTime = sharedRecipe.preparationTime,
                    ownerId = user?.id.orEmpty(),
                    isPublic = false,
                    cloudId = null,
                )
                val updated = recipes + recipe
                store.saveRecipes(updated)
                recipes = updated
                showMessage(tr("shared_recipe.save_success"))
                onFinished(true)
            } catch (error: Exception) {
                showMessage(tr("shared_recipe.save_error", error.message), true)
                onFinished(false)
            } finally {
                loading = null
            }
        }
    }

    // ---------------------------------------------------------------------
    // Authentication
    // ---------------------------------------------------------------------

    fun signInWithGoogle() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(api.googleSignInUrl))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            getApplication<Application>().startActivity(intent)
        } catch (error: Exception) {
            showMessage(tr("auth_controller.sign_in_error"), true)
        }
    }

    fun completeSignIn(token: String?, error: String?) {
        if (!error.isNullOrBlank() || token.isNullOrBlank()) {
            showMessage(tr("auth_controller.sign_in_error"), true)
            return
        }
        viewModelScope.launch {
            try {
                api.token = token
                val authenticated = api.me()
                store.token = token
                user = authenticated
            } catch (signInError: Exception) {
                api.token = null
                store.clearToken()
                showMessage(tr("auth_controller.sign_in_error"), true)
            }
        }
    }

    fun signOut(onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                if (autoSync) clearLocalOwnedRecipes()
                autoSync = false
                store.autoSync = false
                runCatching { api.logout() }
                api.token = null
                store.clearToken()
                user = null
                onFinished()
            } catch (error: Exception) {
                showMessage(tr("auth_controller.sign_out_error", error.message), true)
            }
        }
    }

    fun deleteAccount(onFinished: () -> Unit = {}) {
        if (!isLoggedIn) return
        viewModelScope.launch {
            loading = LoadingRequest(
                tr("auth_controller.deleting_account"),
                tr("profile_info_screen.confirm_delete_message"),
            )
            try {
                clearLocalOwnedRecipes()
                api.deleteAccount()
                autoSync = false
                store.autoSync = false
                api.token = null
                store.clearToken()
                user = null
                onFinished()
            } catch (error: Exception) {
                showMessage(tr("auth_controller.delete_account_error", error.message), true)
            } finally {
                loading = null
            }
        }
    }

    // ---------------------------------------------------------------------
    // Synchronization
    // ---------------------------------------------------------------------

    fun toggleAutoSync(enabled: Boolean) {
        if (enabled) {
            enableAutoSync()
        } else {
            confirmation = ConfirmRequest(
                title = tr("profile_page.disable_sync_confirmation_title"),
                message = tr("profile_page.disable_sync_confirmation_message"),
                confirmLabel = tr("ui_helpers.confirm"),
                cancelLabel = tr("ui_helpers.cancel"),
                destructive = true,
                onConfirm = { disableAutoSync() },
            )
        }
    }

    private fun enableAutoSync() {
        if (!isLoggedIn) {
            showMessage(tr("sync_service.user_must_be_logged_in"), true)
            return
        }
        viewModelScope.launch {
            autoSync = true
            store.autoSync = true
            loading = LoadingRequest(
                tr("profile_page.syncing_recipes"),
                tr("profile_page.syncing_recipes_description"),
            )
            try {
                assignOwnerIdToLocalRecipes()
                fullSyncInternal()
                showMessage(tr("profile_page.sync_success_message"))
            } catch (error: Exception) {
                autoSync = false
                store.autoSync = false
                showMessage(tr("profile_page.sync_error_message", error.message), true)
            } finally {
                loading = null
            }
        }
    }

    private fun disableAutoSync() {
        viewModelScope.launch {
            autoSync = false
            store.autoSync = false
            loading = LoadingRequest(
                tr("profile_page.deleting_cloud_recipes_title"),
                tr("profile_page.deleting_cloud_recipes_description"),
            )
            try {
                deleteAllCloudRecipes()
                showMessage(tr("profile_page.auto_sync_disabled"))
            } catch (error: Exception) {
                autoSync = true
                store.autoSync = true
                showMessage(
                    tr("profile_page.delete_cloud_recipes_error", error.message),
                    true,
                )
            } finally {
                loading = null
            }
        }
    }

    private suspend fun fullSyncInternal() {
        syncRecipesToCloud()
        syncRecipesFromCloud()
    }

    private suspend fun syncRecipesToCloud() {
        val snapshot = recipes.toList()
        var working = recipes
        for (recipe in snapshot) {
            val updated = uploadRecipe(recipe)
            working = working.map { if (it.id == updated.id) updated else it }
            store.saveRecipes(working)
            recipes = working
        }
    }

    private suspend fun uploadRecipe(recipe: Recipe): Recipe {
        val payload = buildRecipePayload(recipe)
        val response = try {
            if (!recipe.cloudId.isNullOrBlank()) {
                api.updateRecipe(recipe.cloudId, payload)
            } else {
                api.createRecipe(payload)
            }
        } catch (error: ApiException) {
            if (error.statusCode == 404 && !recipe.cloudId.isNullOrBlank()) {
                api.createRecipe(payload)
            } else {
                throw error
            }
        }

        val cloudId = response.optString("cloudId")
            .ifBlank { response.optString("id") }
        return recipe.copy(
            ownerId = user?.id ?: recipe.ownerId,
            isPublic = response.optBoolean("isPublic", recipe.isPublic),
            cloudId = cloudId.ifBlank { recipe.cloudId },
        )
    }

    private suspend fun syncRecipesFromCloud() {
        val remoteRecipes = api.listRecipes()
        var working = recipes.toMutableList()

        remoteRecipes.forEach { remote ->
            val cloudId = remote.cloudId ?: return@forEach
            val existingIndex = working.indexOfFirst { it.cloudId == cloudId }
            val existing = working.getOrNull(existingIndex)
            var localImage = existing?.image.orEmpty()

            if (remote.image?.startsWith("http") == true &&
                (localImage.isBlank() || localImage.startsWith("http"))
            ) {
                runCatching {
                    val bytes = api.download(remote.image)
                    localImage = store.writeRecipeImage(bytes) ?: localImage
                }
            }

            val synced = Recipe(
                id = existing?.id ?: UUID.randomUUID().toString(),
                title = remote.title,
                description = remote.description,
                image = localImage.ifBlank { null },
                ingredients = remote.ingredients,
                directions = remote.directions,
                preparationTime = remote.preparationTime,
                ownerId = remote.ownerId,
                isPublic = remote.isPublic,
                cloudId = cloudId,
                createdAt = remote.createdAt,
                updatedAt = remote.updatedAt,
            )

            if (existingIndex >= 0) working[existingIndex] = synced else working += synced
        }

        store.saveRecipes(working)
        recipes = working
    }

    private suspend fun deleteAllCloudRecipes() {
        api.listRecipes().forEach { recipe ->
            recipe.cloudId?.takeIf { it.isNotBlank() }?.let { api.deleteRecipe(it) }
        }
    }

    private suspend fun assignOwnerIdToLocalRecipes() {
        val ownerId = user?.id ?: return
        val updated = recipes.map { recipe ->
            if (recipe.ownerId != ownerId) recipe.copy(ownerId = ownerId) else recipe
        }
        store.saveRecipes(updated)
        recipes = updated
    }

    private suspend fun clearLocalOwnedRecipes() {
        val removed = recipes.filter { it.ownerId.isNotBlank() }
        removed.forEach { store.deleteImage(it.image) }
        val remaining = recipes.filter { it.ownerId.isBlank() }
        store.saveRecipes(remaining)
        recipes = remaining
    }

    private suspend fun buildRecipePayload(recipe: Recipe): JSONObject {
        val imageBase64 = recipe.image
            ?.takeIf { it.isNotBlank() && !it.startsWith("http") }
            ?.let { path ->
                runCatching {
                    withContext(Dispatchers.Default) {
                        encodeImage(path, maxDimension = 1600, quality = 85)
                    }
                }.getOrNull()
            }
        return recipe.toApiJson(imageBase64)
    }

    private suspend fun updateLocalRecipe(id: String, transform: (Recipe) -> Recipe) {
        val updated = recipes.map { if (it.id == id) transform(it) else it }
        store.saveRecipes(updated)
        recipes = updated
    }

    // ---------------------------------------------------------------------
    // Public recipes & sharing links
    // ---------------------------------------------------------------------

    fun searchPublicRecipes(query: String) {
        publicSearch = publicSearch.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val result = api.searchPublicRecipes(query)
                publicSearch = PublicSearchState(recipes = result)
            } catch (error: Exception) {
                publicSearch = PublicSearchState(error = error.message ?: "")
            }
        }
    }

    fun findLocalByCloudId(cloudId: String): Recipe? =
        recipes.firstOrNull { it.cloudId == cloudId }

    fun findLocalByTitle(title: String): Recipe? {
        val exact = recipes.firstOrNull { it.title.equals(title, ignoreCase = true) }
        if (exact != null) return exact
        return recipes.firstOrNull {
            it.title.contains(title, ignoreCase = true) ||
                title.contains(it.title, ignoreCase = true)
        }
    }

    fun openSharedRecipe(recipe: Recipe) {
        pendingSharedRecipe = recipe
    }

    fun handleDeepLink(uri: Uri) {
        val isAuth = uri.host == "auth" || uri.pathSegments.contains("auth")
        if (isAuth) {
            completeSignIn(uri.getQueryParameter("token"), uri.getQueryParameter("error"))
            return
        }

        val isSharing = (uri.host == "sharing" ||
            uri.pathSegments.contains("recipe") ||
            uri.pathSegments.contains("sharing")) &&
            uri.getQueryParameter("id") != null
        if (!isSharing) return

        val cloudId = uri.getQueryParameter("id") ?: return
        val local = findLocalByCloudId(cloudId)
        if (local != null) {
            deepLinkTarget = DeepLinkTarget.LocalRecipe(local.id)
            return
        }

        viewModelScope.launch {
            loading = LoadingRequest(
                tr("public_recipes_search.title"),
                tr("public_recipes_search.search_hint"),
            )
            try {
                val shared = api.getPublicRecipe(cloudId)
                pendingSharedRecipe = shared
                deepLinkTarget = DeepLinkTarget.SharedRecipe(shared)
            } catch (error: Exception) {
                showMessage(tr("shared_recipe.not_found_error"), true)
            } finally {
                loading = null
            }
        }
    }

    // ---------------------------------------------------------------------
    // Import & export
    // ---------------------------------------------------------------------

    fun exportRecipes(uri: Uri) {
        if (recipes.isEmpty()) {
            showMessage(tr("export_service.no_recipes_to_export"), true)
            return
        }
        viewModelScope.launch {
            loading = LoadingRequest(tr("export_service.save_file"), "")
            try {
                val bytes = exportImportService.buildExportZip(recipes)
                withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver
                        .openOutputStream(uri)
                        ?.use { it.write(bytes) }
                        ?: throw IllegalStateException(tr("export_service.save_failed"))
                }
                showMessage(tr("export_service.saved_successfully"))
            } catch (error: Exception) {
                showMessage(tr("export_service.save_failed"), true)
            } finally {
                loading = null
            }
        }
    }

    fun importRecipes(uri: Uri) {
        viewModelScope.launch {
            loading = LoadingRequest(
                tr("import_service.processing_import"),
                tr("import_service.extracting_files"),
            )
            try {
                val bytes = withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver.openInputStream(uri)
                        ?.use { it.readBytes() }
                        ?: throw IllegalStateException(tr("import_service.file_not_found"))
                }
                val before = recipes
                val result: ImportResult = exportImportService.importZip(
                    bytes = bytes,
                    existingRecipes = before,
                    ownerId = user?.id.orEmpty(),
                )
                refreshRecipes()

                if (result.importedCount > 0 && isLoggedIn && autoSync) {
                    assignOwnerIdToLocalRecipes()
                    runCatching { syncRecipesToCloud() }
                }

                showMessage(
                    tr(
                        "profile_page.import_success_message",
                        result.importedCount,
                        result.skippedCount,
                    ),
                )
            } catch (error: Exception) {
                showMessage(tr("profile_page.import_error_message", error.message), true)
            } finally {
                loading = null
            }
        }
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private fun stringList(json: JSONObject, key: String): List<String> {
        val array: JSONArray = json.optJSONArray(key) ?: return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                array.optString(index).takeIf { it.isNotBlank() }?.let(::add)
            }
        }
    }

    private fun aiErrorMessage(error: ApiException): String = when (error.statusCode) {
        401 -> tr("ai_errors.authentication_failed")
        429 -> tr("ai_errors.rate_limit_exceeded")
        400 -> if (error.message.orEmpty().contains("Image too large", ignoreCase = true)) {
            tr("ai_errors.image_too_large")
        } else {
            tr("ai_errors.invalid_request")
        }

        500, 502 -> tr("ai_errors.server_error")
        else -> tr("ai_errors.unknown_error")
    }

    private fun encodeImage(path: String, maxDimension: Int, quality: Int): String {
        val file = File(path)
        require(file.exists()) { "Image file does not exist: $path" }

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        var sampleSize = 1
        while (bounds.outWidth / sampleSize > maxDimension * 2 ||
            bounds.outHeight / sampleSize > maxDimension * 2
        ) {
            sampleSize *= 2
        }

        val decoded = BitmapFactory.decodeFile(
            path,
            BitmapFactory.Options().apply { inSampleSize = sampleSize },
        ) ?: throw IllegalStateException("Could not decode image")

        val largest = maxOf(decoded.width, decoded.height)
        val scaled = if (largest > maxDimension) {
            val ratio = maxDimension.toFloat() / largest
            Bitmap.createScaledBitmap(
                decoded,
                (decoded.width * ratio).toInt().coerceAtLeast(1),
                (decoded.height * ratio).toInt().coerceAtLeast(1),
                true,
            )
        } else {
            decoded
        }

        val output = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, quality, output)
        if (scaled !== decoded) decoded.recycle()
        scaled.recycle()
        return Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
    }
}
