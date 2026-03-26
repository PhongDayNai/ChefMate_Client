package com.watb.chefmate.api

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.watb.chefmate.data.ApiNetworkResult
import com.watb.chefmate.data.ChatSendMessageRequest
import com.watb.chefmate.data.ChatSessionActiveRecipeRequest
import com.watb.chefmate.data.ChatSessionCreateRequest
import com.watb.chefmate.data.ChatSessionTitleRequest
import com.watb.chefmate.data.DietNote
import com.watb.chefmate.data.DietNoteDeleteRequest
import com.watb.chefmate.data.DietNoteUpsertRequest
import com.watb.chefmate.data.PantryDeleteRequest
import com.watb.chefmate.data.PantryItem
import com.watb.chefmate.data.PantryUpsertRequest
import com.watb.chefmate.data.RecommendationPayload
import com.watb.chefmate.data.RecommendationRequest
import com.watb.chefmate.data.RecommendationType
import com.watb.chefmate.data.ResolvePreviousSessionRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.util.concurrent.TimeUnit

object AppFlowApiClient {
    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    data class RawApiResult(
        val httpStatus: Int,
        val success: Boolean,
        val data: JsonElement? = null,
        val message: String? = null,
        val code: String? = null,
        val rawBody: String? = null
    )

    private suspend fun executeRaw(request: Request): RawApiResult = withContext(Dispatchers.IO) {
        try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()
                parseRawResponse(
                    statusCode = response.code,
                    body = responseBody,
                    fallbackMessage = response.message
                )
            }
        } catch (e: Exception) {
            RawApiResult(
                httpStatus = -1,
                success = false,
                message = e.message ?: "Network error"
            )
        }
    }

    private fun parseRawResponse(statusCode: Int, body: String?, fallbackMessage: String?): RawApiResult {
        if (body.isNullOrBlank()) {
            return RawApiResult(
                httpStatus = statusCode,
                success = statusCode in 200..299,
                message = fallbackMessage,
                rawBody = body
            )
        }

        return try {
            val root = JsonParser.parseString(body)
            if (!root.isJsonObject) {
                RawApiResult(
                    httpStatus = statusCode,
                    success = statusCode in 200..299,
                    data = root,
                    message = fallbackMessage,
                    rawBody = body
                )
            } else {
                val jsonObject = root.asJsonObject
                RawApiResult(
                    httpStatus = statusCode,
                    success = jsonObject.booleanOrNull("success") ?: (statusCode in 200..299),
                    data = jsonObject.get("data"),
                    message = jsonObject.stringOrNull("message") ?: fallbackMessage,
                    code = jsonObject.stringOrNull("code"),
                    rawBody = body
                )
            }
        } catch (_: Exception) {
            RawApiResult(
                httpStatus = statusCode,
                success = statusCode in 200..299,
                message = fallbackMessage,
                rawBody = body
            )
        }
    }

    private inline fun <reified T> parseData(data: JsonElement?): T? {
        if (data == null || data.isJsonNull) return null
        return runCatching {
            gson.fromJson<T>(data, object : TypeToken<T>() {}.type)
        }.getOrNull()
    }

    private fun <T> toTypedResult(raw: RawApiResult, data: T?): ApiNetworkResult<T> {
        return ApiNetworkResult(
            httpStatus = raw.httpStatus,
            success = raw.success,
            data = data,
            message = raw.message,
            code = raw.code
        )
    }

    private fun buildUrl(path: String, query: Map<String, String?> = emptyMap()): String {
        val candidate = "${ApiConstant.MAIN_URL}$path"
        val parsed = candidate.toHttpUrlOrNull() ?: return candidate
        val builder = parsed.newBuilder()
        query.forEach { (key, value) ->
            if (!value.isNullOrBlank()) {
                builder.addQueryParameter(key, value)
            }
        }
        return builder.build().toString()
    }

    suspend fun getDietNotes(userId: Int): ApiNetworkResult<List<DietNote>> {
        val request = Request.Builder()
            .url(buildUrl("/api/user-diet-notes", mapOf("userId" to userId.toString())))
            .get()
            .build()

        val raw = executeRaw(request)
        return toTypedResult(raw, parseData(raw.data) ?: emptyList())
    }

    suspend fun upsertDietNote(requestBody: DietNoteUpsertRequest): ApiNetworkResult<List<DietNote>> {
        val request = Request.Builder()
            .url(buildUrl("/api/user-diet-notes/upsert"))
            .post(gson.toJson(requestBody).toRequestBody(jsonMediaType))
            .build()

        val raw = executeRaw(request)
        return toTypedResult(raw, parseData(raw.data) ?: emptyList())
    }

    suspend fun deleteDietNote(requestBody: DietNoteDeleteRequest): ApiNetworkResult<List<DietNote>> {
        val request = Request.Builder()
            .url(buildUrl("/api/user-diet-notes/delete"))
            .delete(gson.toJson(requestBody).toRequestBody(jsonMediaType))
            .build()

        val raw = executeRaw(request)
        return toTypedResult(raw, parseData(raw.data) ?: emptyList())
    }

    suspend fun getPantryItems(userId: Int): ApiNetworkResult<List<PantryItem>> {
        val request = Request.Builder()
            .url(buildUrl("/api/pantry", mapOf("userId" to userId.toString())))
            .get()
            .build()

        val raw = executeRaw(request)
        return toTypedResult(raw, parseData(raw.data) ?: emptyList())
    }

    suspend fun upsertPantryItem(requestBody: PantryUpsertRequest): ApiNetworkResult<List<PantryItem>> {
        val request = Request.Builder()
            .url(buildUrl("/api/pantry/upsert"))
            .post(gson.toJson(requestBody).toRequestBody(jsonMediaType))
            .build()

        val raw = executeRaw(request)
        return toTypedResult(raw, parseData(raw.data) ?: emptyList())
    }

    suspend fun deletePantryItem(requestBody: PantryDeleteRequest): ApiNetworkResult<List<PantryItem>> {
        val request = Request.Builder()
            .url(buildUrl("/api/pantry/delete"))
            .delete(gson.toJson(requestBody).toRequestBody(jsonMediaType))
            .build()

        val raw = executeRaw(request)
        return toTypedResult(raw, parseData(raw.data) ?: emptyList())
    }

    suspend fun getRecommendations(userId: Int, limit: Int = 10): ApiNetworkResult<RecommendationPayload> {
        val requestBody = RecommendationRequest(userId = userId, limit = limit)
        val request = Request.Builder()
            .url(buildUrl("/api/ai-chat/recommendations-from-pantry"))
            .post(gson.toJson(requestBody).toRequestBody(jsonMediaType))
            .build()

        val raw = executeRaw(request)
        val payload = parseData<RecommendationPayload>(raw.data)
        val normalized = payload?.normalizeRecommendations() ?: RecommendationPayload(recommendationLimit = limit)
        return toTypedResult(raw, normalized)
    }

    suspend fun createSession(requestBody: ChatSessionCreateRequest): RawApiResult {
        val request = Request.Builder()
            .url(buildUrl("/api/ai-chat/sessions"))
            .post(gson.toJson(requestBody).toRequestBody(jsonMediaType))
            .build()

        return executeRaw(request)
    }

    suspend fun getSessions(userId: Int, page: Int = 1, limit: Int = 50): RawApiResult {
        val request = Request.Builder()
            .url(
                buildUrl(
                    "/api/ai-chat/sessions",
                    mapOf(
                        "userId" to userId.toString(),
                        "page" to page.toString(),
                        "limit" to limit.toString()
                    )
                )
            )
            .get()
            .build()

        return executeRaw(request)
    }

    suspend fun getSessionDetail(sessionId: Int, userId: Int): RawApiResult {
        val request = Request.Builder()
            .url(buildUrl("/api/ai-chat/sessions/$sessionId", mapOf("userId" to userId.toString())))
            .get()
            .build()

        return executeRaw(request)
    }

    suspend fun updateSessionTitle(requestBody: ChatSessionTitleRequest): RawApiResult {
        val request = Request.Builder()
            .url(buildUrl("/api/ai-chat/sessions/title"))
            .patch(gson.toJson(requestBody).toRequestBody(jsonMediaType))
            .build()

        return executeRaw(request)
    }

    suspend fun updateActiveRecipe(requestBody: ChatSessionActiveRecipeRequest): RawApiResult {
        val request = Request.Builder()
            .url(buildUrl("/api/ai-chat/sessions/active-recipe"))
            .patch(gson.toJson(requestBody).toRequestBody(jsonMediaType))
            .build()

        return executeRaw(request)
    }

    suspend fun sendMessage(requestBody: ChatSendMessageRequest): RawApiResult {
        val request = Request.Builder()
            .url(buildUrl("/api/ai-chat/messages"))
            .post(gson.toJson(requestBody).toRequestBody(jsonMediaType))
            .build()

        return executeRaw(request)
    }

    suspend fun getTimeline(userId: Int, limit: Int = 30, beforeMessageId: Int? = null): RawApiResult {
        val request = Request.Builder()
            .url(
                buildUrl(
                    "/api/ai-chat/messages",
                    mapOf(
                        "userId" to userId.toString(),
                        "limit" to limit.toString(),
                        "beforeMessageId" to beforeMessageId?.toString()
                    )
                )
            )
            .get()
            .build()

        return executeRaw(request)
    }

    suspend fun resolvePreviousSession(requestBody: ResolvePreviousSessionRequest): RawApiResult {
        val request = Request.Builder()
            .url(buildUrl("/api/ai-chat/sessions/resolve-previous"))
            .post(gson.toJson(requestBody).toRequestBody(jsonMediaType))
            .build()

        return executeRaw(request)
    }

    suspend fun deleteSession(sessionId: Int, userId: Int): RawApiResult {
        val request = Request.Builder()
            .url(buildUrl("/api/ai-chat/sessions/$sessionId", mapOf("userId" to userId.toString())))
            .delete()
            .build()

        return executeRaw(request)
    }

    private fun RecommendationPayload.normalizeRecommendations(): RecommendationPayload {
        val ready = if (readyToCook.isNotEmpty()) {
            readyToCook
        } else {
            recommendations.filter { it.recommendationType == RecommendationType.READY_TO_COOK }
        }

        val almost = if (almostReady.isNotEmpty()) {
            almostReady
        } else {
            recommendations.filter { it.recommendationType == RecommendationType.ALMOST_READY }
        }

        return copy(
            readyToCook = ready,
            almostReady = almost
        )
    }

    private fun JsonObject.stringOrNull(key: String): String? {
        val value = get(key) ?: return null
        if (value.isJsonNull) return null
        return runCatching { value.asString }.getOrNull()
    }

    private fun JsonObject.booleanOrNull(key: String): Boolean? {
        val value = get(key) ?: return null
        if (value.isJsonNull) return null
        return runCatching { value.asBoolean }.getOrNull()
    }
}
