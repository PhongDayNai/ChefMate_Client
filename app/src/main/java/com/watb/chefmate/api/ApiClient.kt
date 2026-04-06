package com.watb.chefmate.api

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.watb.chefmate.data.AllIngredientsResponse
import com.watb.chefmate.data.AllTagsResponse
import com.watb.chefmate.data.AuthResponse
import com.watb.chefmate.data.ChangePasswordRequest
import com.watb.chefmate.data.CommentRequest
import com.watb.chefmate.data.CreateRecipeData
import com.watb.chefmate.data.CreateRecipeResponse
import com.watb.chefmate.data.IncreaseRequest
import com.watb.chefmate.data.IngredientItem
import com.watb.chefmate.data.InteractionData
import com.watb.chefmate.data.InteractionResponse
import com.watb.chefmate.data.LoginRequest
import com.watb.chefmate.data.PaginationMeta
import com.watb.chefmate.data.Recipe
import com.watb.chefmate.data.RecipeListResponse
import com.watb.chefmate.data.RegisterRequest
import com.watb.chefmate.data.SearchRecipeByTagRequest
import com.watb.chefmate.data.SimpleResponse
import com.watb.chefmate.data.TagData
import com.watb.chefmate.data.TrendingV2Data
import com.watb.chefmate.data.UpdateUserInformationRequest
import com.watb.chefmate.data.UserProfileResponse
import com.watb.chefmate.helper.CommonHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object ApiClient {
    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val downloadClient = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    suspend fun register(fullName: String, phone: String, email: String, password: String): AuthResponse {
        val requestBody = gson.toJson(RegisterRequest(fullName, phone, email, password))
            .toRequestBody(jsonMediaType)

        val raw = ApiRequestExecutor.executeRaw(AuthMode.PUBLIC) {
            Request.Builder()
                .url(ApiConstant.REGISTER_URL)
                .post(requestBody)
                .build()
        }
        return parseAuthResponse(raw)
    }

    suspend fun login(identifier: String, password: String): AuthResponse {
        val requestBody = gson.toJson(LoginRequest(identifier, password))
            .toRequestBody(jsonMediaType)

        val raw = ApiRequestExecutor.executeRaw(AuthMode.PUBLIC) {
            Request.Builder()
                .url(ApiConstant.LOGIN_URL)
                .post(requestBody)
                .build()
        }
        return parseAuthResponse(raw)
    }

    suspend fun getTopTrending(
        userId: Int? = null,
        page: Int = 1,
        limit: Int = 20,
        period: String = "all"
    ): RecipeListResponse? {
        val raw = ApiRequestExecutor.executeRaw(AuthMode.OPTIONAL_BEARER) {
            Request.Builder()
                .url(
                    buildUrl(
                        ApiConstant.TOP_TRENDING_URL,
                        mapOf(
                            "page" to page.toString(),
                            "limit" to limit.toString(),
                            "period" to period
                        )
                    )
                )
                .get()
                .build()
        }
        return raw.toRecipeListResponse()
    }

    suspend fun searchRecipe(recipeName: String, userId: Int? = null): RecipeListResponse? {
        val normalizedQuery = recipeName.trim()
        val raw = ApiRequestExecutor.executeRaw(AuthMode.OPTIONAL_BEARER) {
            Request.Builder()
                .url(buildUrl(ApiConstant.SEARCH_URL, mapOf("q" to normalizedQuery)))
                .get()
                .build()
        }
        return raw.toRecipeListResponse()
    }

    suspend fun searchRecipeByTag(tagName: String, userId: Int? = null): RecipeListResponse? {
        val requestBody = gson.toJson(SearchRecipeByTagRequest(tagName = tagName.trim()))
            .toRequestBody(jsonMediaType)

        val raw = ApiRequestExecutor.executeRaw(AuthMode.OPTIONAL_BEARER) {
            Request.Builder()
                .url(ApiConstant.SEARCH_BY_TAG_URL)
                .post(requestBody)
                .build()
        }
        return raw.toRecipeListResponse()
    }

    suspend fun getRecipesByUserId(userId: Int): RecipeListResponse? {
        val raw = ApiRequestExecutor.executeRaw(AuthMode.BEARER) {
            Request.Builder()
                .url(ApiConstant.GET_RECIPES_BY_USER_ID_URL)
                .get()
                .build()
        }
        return raw.toRecipeListResponse()
    }

    suspend fun getAllRecipes(): RecipeListResponse? {
        val raw = ApiRequestExecutor.executeRaw(AuthMode.OPTIONAL_BEARER) {
            Request.Builder()
                .url(ApiConstant.GET_ALL_RECIPES_URL)
                .get()
                .build()
        }
        return raw.toRecipeListResponse()
    }

    suspend fun createRecipe(context: Context, recipe: CreateRecipeData): CreateRecipeResponse? {
        val bitmap = if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(context.contentResolver, recipe.image.toUri())
        } else {
            val source = ImageDecoder.createSource(context.contentResolver, recipe.image.toUri())
            ImageDecoder.decodeBitmap(source)
        }

        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        val imageRequestBody = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, byteArray.size)
        val ingredientsRequestBody = gson.toJson(recipe.ingredients)
        val cookingStepsRequestBody = gson.toJson(recipe.cookingSteps)

        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("recipeName", recipe.recipeName)
            .addFormDataPart("cookingTime", recipe.cookingTime)
            .addFormDataPart("ration", recipe.ration.toString())
            .addFormDataPart("ingredients", ingredientsRequestBody)
            .addFormDataPart("cookingSteps", cookingStepsRequestBody)
            .apply {
                if (recipe.tags.isNotEmpty()) {
                    addFormDataPart("tags", gson.toJson(recipe.tags))
                }
                addFormDataPart(
                    "image",
                    "${CommonHelper.parseName(recipe.recipeName)}.jpg",
                    imageRequestBody
                )
            }
            .build()

        val raw = ApiRequestExecutor.executeRaw(AuthMode.BEARER) {
            Request.Builder()
                .url(ApiConstant.CREATE_RECIPE_URL)
                .post(multipartBody)
                .build()
        }

        return CreateRecipeResponse(
            success = raw.success,
            data = raw.data?.asObjectOrNull()?.intOrNull("recipeId", "id"),
            message = raw.message
        )
    }

    fun downloadAndSaveImage(
        context: Context,
        coroutineScope: CoroutineScope,
        imageUrl: String,
        fileName: String = "Image_${System.currentTimeMillis()}.jpg",
        onResult: (String) -> Unit
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(imageUrl)
                    .build()

                val response = downloadClient.newCall(request).execute()

                if (!response.isSuccessful) {
                    onResult("Error: HTTP ${response.code}")
                    return@launch
                }

                val imageBytes = response.body?.bytes() ?: run {
                    throw IllegalStateException("Empty image data")
                }

                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ChefMate")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    ?: throw IllegalStateException("Failed to create MediaStore URI")

                resolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(imageBytes)
                    outputStream.flush()
                } ?: throw IllegalStateException("Failed to open output stream")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }

                onResult("Success: $uri")
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading or saving image: ${e.message}", e)
                onResult("Error: ${e.message}")
            }
        }
    }

    suspend fun getAllIngredients(): AllIngredientsResponse? {
        val raw = ApiRequestExecutor.executeRaw(AuthMode.PUBLIC) {
            Request.Builder()
                .url(ApiConstant.GET_ALL_INGREDIENTS_URL)
                .get()
                .build()
        }

        return AllIngredientsResponse(
            success = raw.success,
            data = parseList(raw.data),
            message = raw.message
        )
    }

    suspend fun getAllTags(): AllTagsResponse? {
        val raw = ApiRequestExecutor.executeRaw(AuthMode.PUBLIC) {
            Request.Builder()
                .url(ApiConstant.GET_ALL_TAGS_URL)
                .get()
                .build()
        }

        return AllTagsResponse(
            success = raw.success,
            data = parseList(raw.data),
            message = raw.message
        )
    }

    suspend fun likeRecipe(userId: Int, recipeId: Int): InteractionResponse? {
        val requestBody = gson.toJson(mapOf("recipeId" to recipeId))
            .toRequestBody(jsonMediaType)

        val raw = ApiRequestExecutor.executeRaw(AuthMode.BEARER) {
            Request.Builder()
                .url(ApiConstant.LIKE_URL)
                .post(requestBody)
                .build()
        }
        return raw.toInteractionResponse()
    }

    suspend fun commentRecipe(userId: Int = 1, recipeId: Int, content: String): InteractionResponse? {
        val requestBody = gson.toJson(
            CommentRequest(
                userId = userId,
                recipeId = recipeId,
                content = content
            ).let { mapOf("recipeId" to it.recipeId, "content" to it.content) }
        ).toRequestBody(jsonMediaType)

        val raw = ApiRequestExecutor.executeRaw(AuthMode.BEARER) {
            Request.Builder()
                .url(ApiConstant.COMMENT_URL)
                .post(requestBody)
                .build()
        }
        return raw.toInteractionResponse()
    }

    suspend fun increaseViewCount(recipeId: Int): InteractionResponse? {
        val requestBody = gson.toJson(IncreaseRequest(recipeId))
            .toRequestBody(jsonMediaType)

        val raw = ApiRequestExecutor.executeRaw(AuthMode.PUBLIC) {
            Request.Builder()
                .url(ApiConstant.INCREASE_VIEW_COUNT_URL)
                .post(requestBody)
                .build()
        }
        return raw.toInteractionResponse()
    }

    suspend fun updateUserInformation(
        userId: Int,
        fullName: String,
        phoneNumber: String,
        email: String
    ): UserProfileResponse {
        val requestBody = gson.toJson(
            UpdateUserInformationRequest(
                userId = userId,
                fullName = fullName,
                phone = phoneNumber,
                email = email
            ).let {
                mapOf(
                    "fullName" to it.fullName,
                    "phone" to it.phone,
                    "email" to it.email
                )
            }
        ).toRequestBody(jsonMediaType)

        val raw = ApiRequestExecutor.executeRaw(AuthMode.BEARER) {
            Request.Builder()
                .url(ApiConstant.UPDATE_USER_INFORMATION_URL)
                .patch(requestBody)
                .build()
        }

        return UserProfileResponse(
            success = raw.success,
            data = parseUserData(raw.data),
            message = raw.message
        )
    }

    suspend fun changePassword(
        phone: String,
        currentPassword: String,
        newPassword: String
    ): SimpleResponse {
        val requestBody = gson.toJson(ChangePasswordRequest(phone, currentPassword, newPassword))
            .toRequestBody(jsonMediaType)

        val raw = ApiRequestExecutor.executeRaw(AuthMode.PUBLIC) {
            Request.Builder()
                .url(ApiConstant.CHANGE_PASSWORD_URL)
                .post(requestBody)
                .build()
        }

        return SimpleResponse(
            success = raw.success,
            message = raw.message
        )
    }

    private fun buildUrl(baseUrl: String, query: Map<String, String?> = emptyMap()): String {
        val parsed = baseUrl.toHttpUrlOrNull() ?: return baseUrl
        val builder = parsed.newBuilder()
        query.forEach { (key, value) ->
            if (!value.isNullOrBlank()) {
                builder.addQueryParameter(key, value)
            }
        }
        return builder.build().toString()
    }

    private inline fun <reified T> parseList(data: JsonElement?): List<T>? {
        if (data == null || data.isJsonNull) return null
        return runCatching {
            gson.fromJson<List<T>>(data, object : TypeToken<List<T>>() {}.type)
        }.getOrNull()
    }

    private fun RawApiResult.toRecipeListResponse(): RecipeListResponse {
        val listData = when {
            data == null || data.isJsonNull -> emptyList()
            data.isJsonArray -> parseList<Recipe>(data) ?: emptyList()
            data.asObjectOrNull()?.get("items")?.isJsonArray == true -> {
                parseList<Recipe>(data.asJsonObject.get("items")) ?: emptyList()
            }
            data.asObjectOrNull()?.get("recipes")?.isJsonArray == true -> {
                parseList<Recipe>(data.asJsonObject.get("recipes")) ?: emptyList()
            }
            else -> emptyList()
        }

        val nestedObject = data.asObjectOrNull()
        val pagination = nestedObject?.let {
            runCatching { gson.fromJson(it.get("pagination"), PaginationMeta::class.java) }.getOrNull()
        }
        val trendingData = nestedObject?.let {
            runCatching { gson.fromJson(it, TrendingV2Data::class.java) }.getOrNull()
        }

        return RecipeListResponse(
            success = success,
            data = if (listData.isNotEmpty()) listData else trendingData?.items ?: emptyList(),
            message = message,
            pagination = pagination ?: trendingData?.pagination,
            period = nestedObject?.stringOrNull("period") ?: trendingData?.period,
            apiVersion = nestedObject?.stringOrNull("apiVersion") ?: trendingData?.apiVersion
        )
    }

    private fun RawApiResult.toInteractionResponse(): InteractionResponse {
        val interactionData = runCatching {
            gson.fromJson(data, InteractionData::class.java)
        }.getOrNull()

        return InteractionResponse(
            success = success,
            data = interactionData,
            message = message
        )
    }

    private const val TAG = "ApiClient"
}
