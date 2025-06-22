package com.watb.chefmate.api

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import com.google.gson.Gson
import com.watb.chefmate.data.AllIngredientsResponse
import com.watb.chefmate.data.AllTagsResponse
import com.watb.chefmate.data.CommentRequest
import com.watb.chefmate.data.CreateRecipeData
import com.watb.chefmate.data.CreateRecipeResponse
import com.watb.chefmate.data.IncreaseRequest
import com.watb.chefmate.data.InteractionResponse
import com.watb.chefmate.data.LikeRequest
import com.watb.chefmate.data.LoginResponse
import com.watb.chefmate.data.RegisterRequest
import com.watb.chefmate.data.RecipeListResponse
import com.watb.chefmate.data.SearchRecipeByTagRequest
import com.watb.chefmate.data.SearchRecipeRequest
import com.watb.chefmate.helper.CommonHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

object ApiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    @SuppressLint("MemberExtensionConflict")
    suspend fun register(fullName: String, phone: String, email: String, password: String): LoginResponse? {
        val registerRequest = RegisterRequest(fullName, phone, email, password)
        val json = gson.toJson(registerRequest)

        val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(ApiConstant.REGISTER_URL)
            .post(requestBody)
            .build()

        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        responseBody?.let {
                            gson.fromJson(it, LoginResponse::class.java)
                        }
                    } else {
                        Log.e("ApiClient", "Error: ${response.code}")
                        null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } catch (e: TimeoutException) {
                e.printStackTrace()
                null
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
                null
            }
        }
    }

    @SuppressLint("MemberExtensionConflict")
    suspend fun getTopTrending(): RecipeListResponse? {
        val request = Request.Builder()
            .url(ApiConstant.TOP_TRENDING_URL)
            .get()
            .build()

        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        Log.d("ApiClient", "Response Body: $responseBody")
                        responseBody?.let {
                            gson.fromJson(it, RecipeListResponse::class.java)
                        }
                    } else {
                        Log.e("ApiClient", "Error: ${response.code}")
                        null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } catch (e: TimeoutException) {
                e.printStackTrace()
                null
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
                null
            }
        }
    }

    @SuppressLint("MemberExtensionConflict")
    suspend fun searchRecipe(recipeName: String, userId: Int? = null): RecipeListResponse? {
        val searchRequest = SearchRecipeRequest(recipeName, userId)
        val json = gson.toJson(searchRequest)

        val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(ApiConstant.SEARCH_URL)
            .post(requestBody)
            .build()

        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        responseBody?.let {
                            gson.fromJson(it, RecipeListResponse::class.java)
                        }
                    } else {
                        Log.e("ApiClient", "Error: ${response.code}")
                        null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } catch (e: TimeoutException) {
                e.printStackTrace()
                null
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
                null
            }
        }
    }

    @SuppressLint("MemberExtensionConflict")
    suspend fun searchRecipeByTag(tagName: String, userId: Int? = null): RecipeListResponse? {
        val searchRequest = SearchRecipeByTagRequest(tagName, userId)
        val json = gson.toJson(searchRequest)

        val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(ApiConstant.SEARCH_BY_TAG_URL)
            .post(requestBody)
            .build()

        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        responseBody?.let {
                            gson.fromJson(it, RecipeListResponse::class.java)
                        }
                    } else {
                        Log.e("ApiClient", "Error: ${response.code}")
                        null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } catch (e: TimeoutException) {
                e.printStackTrace()
                null
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
                null
            }
        }
    }

    @SuppressLint("MemberExtensionConflict")
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
        val cookingSteps = gson.toJson(recipe.cookingSteps)
        Log.d("ApiClient", "Ingredients Request Body: $ingredientsRequestBody")
        Log.d("ApiClient", "Cooking Steps: $cookingSteps")

        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("recipeName", recipe.recipeName)
            .addFormDataPart("cookingTime", recipe.cookingTime)
            .addFormDataPart("ration", "${recipe.ration}")
            .addFormDataPart("ingredients", ingredientsRequestBody)
            .addFormDataPart("cookingSteps", cookingSteps)
            .addFormDataPart("userId", "1")
            .addFormDataPart("image", "${CommonHelper.parseName(recipe.recipeName)}.jpg", imageRequestBody)
            .build()

        val request = Request.Builder()
            .url(ApiConstant.CREATE_RECIPE_URL)
            .post(multipartBody)
            .build()

        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        responseBody?.let {
                            gson.fromJson(it, CreateRecipeResponse::class.java)
                        }
                    } else {
                        Log.e("ApiClient", "Error: ${response.code}")
                        null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } catch (e: TimeoutException) {
                e.printStackTrace()
                null
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
                null
            }
        }
    }

    @SuppressLint("MemberExtensionConflict")
    suspend fun getAllIngredients(): AllIngredientsResponse? {
        val request = Request.Builder()
            .url(ApiConstant.GET_ALL_INGREDIENTS_URL)
            .get()
            .build()

        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        responseBody?.let {
                            gson.fromJson(it, AllIngredientsResponse::class.java)
                            }
                    } else {
                        Log.e("ApiClient", "Error: ${response.code}")
                        null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } catch (e: TimeoutException) {
                e.printStackTrace()
                null
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
                null
            }
        }
    }

    @SuppressLint("MemberExtensionConflict")
    suspend fun getAllTags(): AllTagsResponse? {
        val request = Request.Builder()
            .url(ApiConstant.GET_ALL_TAGS_URL)
            .get()
            .build()

        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        responseBody?.let {
                            gson.fromJson(it, AllTagsResponse::class.java)
                        }
                    } else {
                        Log.e("ApiClient", "Error: ${response.code}")
                        null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } catch (e: TimeoutException) {
                e.printStackTrace()
                null
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
                null
            }
        }
    }



    @SuppressLint("MemberExtensionConflict")
    suspend fun likeRecipe(userId: Int = 1, recipeId: Int): InteractionResponse? {
        val likeRequest = LikeRequest(userId, recipeId)
        val json = gson.toJson(likeRequest)

        val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(ApiConstant.LIKE_URL)
            .post(requestBody)
            .build()

        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        responseBody?.let {
                            gson.fromJson(it, InteractionResponse::class.java)
                        }
                    } else {
                        Log.e("ApiClient", "Error: ${response.code}")
                        null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } catch (e: TimeoutException) {
                e.printStackTrace()
                null
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
                null
            }
        }
    }

    @SuppressLint("MemberExtensionConflict")
    suspend fun commentRecipe(userId: Int = 1, recipeId: Int, content: String): InteractionResponse? {
        val commentRequest = CommentRequest(userId, recipeId, content)
        val json = gson.toJson(commentRequest)

        val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(ApiConstant.COMMENT_URL)
            .post(requestBody)
            .build()

        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        responseBody?.let {
                            gson.fromJson(it, InteractionResponse::class.java)
                        }
                    } else {
                        Log.e("ApiClient", "Error: ${response.code}")
                        null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } catch (e: TimeoutException) {
                e.printStackTrace()
                null
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
                null
            }
        }
    }

    @SuppressLint("MemberExtensionConflict")
    suspend fun increaseViewCount(recipeId: Int): InteractionResponse? {
        val increaseRequest = IncreaseRequest(recipeId)
        val json = gson.toJson(increaseRequest)

        val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(ApiConstant.INCREASE_VIEW_COUNT_URL)
            .post(requestBody)
            .build()

        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        responseBody?.let {
                            gson.fromJson(it, InteractionResponse::class.java)
                            }
                    } else {
                        Log.e("ApiClient", "Error: ${response.code}")
                        null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } catch (e: TimeoutException) {
                e.printStackTrace()
                null
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
                null
            }
        }
    }
}