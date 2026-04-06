package com.watb.chefmate.api

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.watb.chefmate.data.ApiNetworkResult
import com.watb.chefmate.data.ChatSendMessageRequest
import com.watb.chefmate.data.ChatSessionActiveRecipeRequest
import com.watb.chefmate.data.ChatSessionCreateRequest
import com.watb.chefmate.data.ChatSessionTitleRequest
import com.watb.chefmate.data.DietNote
import com.watb.chefmate.data.DietNoteDeleteRequest
import com.watb.chefmate.data.DietNoteUpsertRequest
import com.watb.chefmate.data.MealCompleteRequest
import com.watb.chefmate.data.MealCompletionResolveRequest
import com.watb.chefmate.data.MealPrimaryRecipeRequest
import com.watb.chefmate.data.MealRecipeStatusRequest
import com.watb.chefmate.data.MealSessionCreateRequest
import com.watb.chefmate.data.MealSessionRecipesReplaceRequest
import com.watb.chefmate.data.PantryDeleteRequest
import com.watb.chefmate.data.PantryItem
import com.watb.chefmate.data.PantryUpsertRequest
import com.watb.chefmate.data.RecommendationPayload
import com.watb.chefmate.data.ResolvePreviousSessionRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object AppFlowApiClient {
    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private const val V1_CHAT_BASE = "/v2/ai-chat-v1"
    private const val V2_CHAT_BASE = "/v2/ai-chat"

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
        val raw = ApiRequestExecutor.executeRaw(AuthMode.BEARER) {
            Request.Builder()
                .url(buildUrl("/v2/user-diet-notes/"))
                .get()
                .build()
        }
        return toTypedResult(raw, parseData(raw.data) ?: emptyList())
    }

    suspend fun upsertDietNote(requestBody: DietNoteUpsertRequest): ApiNetworkResult<List<DietNote>> {
        val body = gson.toJson(
            mapOf(
                "noteId" to requestBody.noteId,
                "noteType" to requestBody.noteType,
                "label" to requestBody.label,
                "keywords" to requestBody.keywords,
                "instruction" to requestBody.instruction,
                "isActive" to requestBody.isActive,
                "startAt" to requestBody.startAt,
                "endAt" to requestBody.endAt
            )
        ).toRequestBody(jsonMediaType)

        val raw = ApiRequestExecutor.executeRaw(AuthMode.BEARER) {
            Request.Builder()
                .url(buildUrl("/v2/user-diet-notes/upsert"))
                .post(body)
                .build()
        }
        return toTypedResult(raw, parseData(raw.data) ?: emptyList())
    }

    suspend fun deleteDietNote(requestBody: DietNoteDeleteRequest): ApiNetworkResult<List<DietNote>> {
        val body = gson.toJson(mapOf("noteId" to requestBody.noteId))
            .toRequestBody(jsonMediaType)

        val raw = ApiRequestExecutor.executeRaw(AuthMode.BEARER) {
            Request.Builder()
                .url(buildUrl("/v2/user-diet-notes/delete"))
                .delete(body)
                .build()
        }
        return toTypedResult(raw, parseData(raw.data) ?: emptyList())
    }

    suspend fun getPantryItems(userId: Int): ApiNetworkResult<List<PantryItem>> {
        val raw = ApiRequestExecutor.executeRaw(AuthMode.BEARER) {
            Request.Builder()
                .url(buildUrl("/v2/pantry/"))
                .get()
                .build()
        }
        return toTypedResult(raw, parseData(raw.data) ?: emptyList())
    }

    suspend fun upsertPantryItem(requestBody: PantryUpsertRequest): ApiNetworkResult<List<PantryItem>> {
        val body = gson.toJson(
            mapOf(
                "pantryItemId" to requestBody.pantryItemId,
                "ingredientName" to requestBody.ingredientName,
                "quantity" to requestBody.quantity,
                "unit" to requestBody.unit,
                "expiresAt" to requestBody.expiresAt
            )
        ).toRequestBody(jsonMediaType)

        val raw = ApiRequestExecutor.executeRaw(AuthMode.BEARER) {
            Request.Builder()
                .url(buildUrl("/v2/pantry/upsert"))
                .post(body)
                .build()
        }
        return toTypedResult(raw, parseData(raw.data) ?: emptyList())
    }

    suspend fun deletePantryItem(requestBody: PantryDeleteRequest): ApiNetworkResult<List<PantryItem>> {
        val body = gson.toJson(mapOf("pantryItemId" to requestBody.pantryItemId))
            .toRequestBody(jsonMediaType)

        val raw = ApiRequestExecutor.executeRaw(AuthMode.BEARER) {
            Request.Builder()
                .url(buildUrl("/v2/pantry/delete"))
                .delete(body)
                .build()
        }
        return toTypedResult(raw, parseData(raw.data) ?: emptyList())
    }

    suspend fun getRecommendations(userId: Int, limit: Int = 10): ApiNetworkResult<RecommendationPayload> {
        val body = gson.toJson(mapOf("limit" to limit))
            .toRequestBody(jsonMediaType)

        val raw = ApiRequestExecutor.executeRaw(AuthMode.CHAT_DUAL) {
            Request.Builder()
                .url(buildUrl("$V1_CHAT_BASE/recommendations-from-pantry"))
                .post(body)
                .build()
        }
        val payload = parseData<RecommendationPayload>(raw.data)
        val normalized = payload?.normalizeRecommendations() ?: RecommendationPayload(recommendationLimit = limit)
        return toTypedResult(raw, normalized)
    }

    suspend fun createSession(requestBody: ChatSessionCreateRequest): RawApiResult {
        val body = gson.toJson(
            mapOf(
                "firstMessage" to requestBody.firstMessage,
                "title" to requestBody.title,
                "activeRecipeId" to requestBody.activeRecipeId,
                "model" to requestBody.model
            )
        ).toRequestBody(jsonMediaType)

        return ApiRequestExecutor.executeRaw(AuthMode.CHAT_DUAL) {
            Request.Builder()
                .url(buildUrl("$V1_CHAT_BASE/sessions"))
                .post(body)
                .build()
        }
    }

    suspend fun getSessions(userId: Int, page: Int = 1, limit: Int = 50): RawApiResult {
        return ApiRequestExecutor.executeRaw(AuthMode.CHAT_DUAL) {
            Request.Builder()
                .url(
                    buildUrl(
                        "$V1_CHAT_BASE/sessions",
                        mapOf(
                            "page" to page.toString(),
                            "limit" to limit.toString()
                        )
                    )
                )
                .get()
                .build()
        }
    }

    suspend fun getSessionDetail(sessionId: Int, userId: Int): RawApiResult {
        return ApiRequestExecutor.executeRaw(AuthMode.CHAT_DUAL) {
            Request.Builder()
                .url(buildUrl("$V1_CHAT_BASE/sessions/$sessionId"))
                .get()
                .build()
        }
    }

    suspend fun updateSessionTitle(requestBody: ChatSessionTitleRequest): RawApiResult {
        val body = gson.toJson(
            mapOf(
                "chatSessionId" to requestBody.chatSessionId,
                "title" to requestBody.title
            )
        ).toRequestBody(jsonMediaType)

        return ApiRequestExecutor.executeRaw(AuthMode.CHAT_DUAL) {
            Request.Builder()
                .url(buildUrl("$V1_CHAT_BASE/sessions/title"))
                .patch(body)
                .build()
        }
    }

    suspend fun updateActiveRecipe(requestBody: ChatSessionActiveRecipeRequest): RawApiResult {
        val body = gson.toJson(
            mapOf(
                "chatSessionId" to requestBody.chatSessionId,
                "recipeId" to requestBody.recipeId
            )
        ).toRequestBody(jsonMediaType)

        return ApiRequestExecutor.executeRaw(AuthMode.CHAT_DUAL) {
            Request.Builder()
                .url(buildUrl("$V1_CHAT_BASE/sessions/active-recipe"))
                .patch(body)
                .build()
        }
    }

    suspend fun sendLegacyMessage(requestBody: ChatSendMessageRequest): RawApiResult {
        val body = gson.toJson(
            mapOf(
                "chatSessionId" to requestBody.chatSessionId,
                "message" to requestBody.message,
                "model" to requestBody.model,
                "stream" to requestBody.stream,
                "useUnifiedSession" to (requestBody.useUnifiedSession ?: true)
            )
        ).toRequestBody(jsonMediaType)

        return ApiRequestExecutor.executeRaw(AuthMode.CHAT_DUAL) {
            Request.Builder()
                .url(buildUrl("$V1_CHAT_BASE/messages"))
                .post(body)
                .build()
        }
    }

    suspend fun getTimeline(userId: Int, limit: Int = 30, beforeMessageId: Int? = null): RawApiResult {
        return ApiRequestExecutor.executeRaw(AuthMode.CHAT_DUAL) {
            Request.Builder()
                .url(
                    buildUrl(
                        "$V1_CHAT_BASE/messages",
                        mapOf(
                            "limit" to limit.toString(),
                            "beforeMessageId" to beforeMessageId?.toString()
                        )
                    )
                )
                .get()
                .build()
        }
    }

    suspend fun resolvePreviousSession(requestBody: ResolvePreviousSessionRequest): RawApiResult {
        val body = gson.toJson(
            mapOf(
                "previousSessionId" to requestBody.previousSessionId,
                "action" to requestBody.action,
                "pendingUserMessage" to requestBody.pendingUserMessage
            )
        ).toRequestBody(jsonMediaType)

        return ApiRequestExecutor.executeRaw(AuthMode.CHAT_DUAL) {
            Request.Builder()
                .url(buildUrl("$V1_CHAT_BASE/sessions/resolve-previous"))
                .post(body)
                .build()
        }
    }

    suspend fun deleteSession(sessionId: Int, userId: Int): RawApiResult {
        return ApiRequestExecutor.executeRaw(AuthMode.CHAT_DUAL) {
            Request.Builder()
                .url(buildUrl("$V1_CHAT_BASE/sessions/$sessionId"))
                .delete()
                .build()
        }
    }

    suspend fun createMealSession(requestBody: MealSessionCreateRequest): RawApiResult {
        val body = gson.toJson(requestBody).toRequestBody(jsonMediaType)
        return ApiRequestExecutor.executeRaw(AuthMode.CHAT_DUAL) {
            Request.Builder()
                .url(buildUrl("$V2_CHAT_BASE/sessions/meal"))
                .post(body)
                .build()
        }
    }

    suspend fun replaceMealRecipes(requestBody: MealSessionRecipesReplaceRequest): RawApiResult {
        val body = gson.toJson(
            mapOf(
                "chatSessionId" to requestBody.chatSessionId,
                "recipes" to requestBody.recipes
            )
        ).toRequestBody(jsonMediaType)
        return ApiRequestExecutor.executeRaw(AuthMode.CHAT_DUAL) {
            Request.Builder()
                .url(buildUrl("$V2_CHAT_BASE/sessions/meal/recipes"))
                .patch(body)
                .build()
        }
    }

    suspend fun updateMealRecipeStatus(requestBody: MealRecipeStatusRequest): RawApiResult {
        val payload = linkedMapOf<String, Any?>(
            "chatSessionId" to requestBody.chatSessionId,
            "recipeId" to requestBody.recipeId,
            "status" to requestBody.status
        )
        requestBody.note?.let { payload["note"] = it }

        val confirmField = requestBody.confirmFieldName.takeUnless { it.isNullOrBlank() } ?: "confirmSwitchPrimary"
        val chooseField = requestBody.chooseFieldName.takeUnless { it.isNullOrBlank() } ?: "nextPrimaryRecipeId"

        requestBody.confirmSwitchPrimary?.let { payload[confirmField] = it }
        requestBody.nextPrimaryRecipeId?.let { payload[chooseField] = it }

        val body = gson.toJson(payload).toRequestBody(jsonMediaType)
        return ApiRequestExecutor.executeRaw(AuthMode.CHAT_DUAL) {
            Request.Builder()
                .url(buildUrl("$V2_CHAT_BASE/sessions/meal/recipes/status"))
                .patch(body)
                .build()
        }
    }

    suspend fun updateMealPrimaryRecipe(requestBody: MealPrimaryRecipeRequest): RawApiResult {
        val body = gson.toJson(requestBody).toRequestBody(jsonMediaType)
        return ApiRequestExecutor.executeRaw(AuthMode.CHAT_DUAL) {
            Request.Builder()
                .url(buildUrl("$V2_CHAT_BASE/sessions/meal/primary-recipe"))
                .patch(body)
                .build()
        }
    }

    suspend fun sendMealMessage(requestBody: ChatSendMessageRequest): RawApiResult {
        val body = gson.toJson(
            mapOf(
                "chatSessionId" to requestBody.chatSessionId,
                "message" to requestBody.message,
                "stream" to requestBody.stream,
                "useUnifiedSession" to requestBody.useUnifiedSession
            )
        ).toRequestBody(jsonMediaType)

        return ApiRequestExecutor.executeRaw(AuthMode.CHAT_DUAL) {
            Request.Builder()
                .url(buildUrl("$V2_CHAT_BASE/messages"))
                .post(body)
                .build()
        }
    }

    suspend fun resolveMealCompletion(requestBody: MealCompletionResolveRequest): RawApiResult {
        val body = gson.toJson(requestBody).toRequestBody(jsonMediaType)
        return ApiRequestExecutor.executeRaw(AuthMode.CHAT_DUAL) {
            Request.Builder()
                .url(buildUrl("$V2_CHAT_BASE/sessions/meal/resolve-completion-check"))
                .post(body)
                .build()
        }
    }

    suspend fun completeMealSession(requestBody: MealCompleteRequest): RawApiResult {
        val body = gson.toJson(
            mapOf(
                "chatSessionId" to requestBody.chatSessionId,
                "completionType" to requestBody.completionType,
                "markRemainingStatus" to requestBody.markRemainingStatus,
                "note" to requestBody.note
            )
        ).toRequestBody(jsonMediaType)
        return ApiRequestExecutor.executeRaw(AuthMode.CHAT_DUAL) {
            Request.Builder()
                .url(buildUrl("$V2_CHAT_BASE/sessions/meal/complete"))
                .patch(body)
                .build()
        }
    }

    private fun RecommendationPayload.normalizeRecommendations(): RecommendationPayload {
        val ready = if (readyToCook.isNotEmpty()) {
            readyToCook
        } else {
            recommendations.filter { it.recommendationType == "ready_to_cook" }
        }

        val almost = if (almostReady.isNotEmpty()) {
            almostReady
        } else {
            recommendations.filter { it.recommendationType == "almost_ready" }
        }

        return copy(
            readyToCook = ready,
            almostReady = almost
        )
    }
}
