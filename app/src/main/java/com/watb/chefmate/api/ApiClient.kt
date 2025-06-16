package com.watb.chefmate.api

import android.util.Log
import com.google.gson.Gson
import com.watb.chefmate.data.CommentRequest
import com.watb.chefmate.data.IncreaseRequest
import com.watb.chefmate.data.InteractionResponse
import com.watb.chefmate.data.LikeRequest
import com.watb.chefmate.data.LoginResponse
import com.watb.chefmate.data.RegisterRequest
import com.watb.chefmate.data.TopTrendingResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

object ApiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    suspend fun register(phone: String, password: String, fullName: String): LoginResponse? {
        val registerRequest = RegisterRequest(fullName, phone, password)
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
            } catch (e: TimeoutException) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun getTopTrending(): TopTrendingResponse? {
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
                            gson.fromJson(it, TopTrendingResponse::class.java)
                        }
                    } else {
                        Log.e("ApiClient", "Error: ${response.code}")
                        null
                    }
                }
            } catch (e: TimeoutException) {
                e.printStackTrace()
                null
            }
        }
    }

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
            }
        }
    }

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
            }
        }
    }

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
            }
        }
    }
}