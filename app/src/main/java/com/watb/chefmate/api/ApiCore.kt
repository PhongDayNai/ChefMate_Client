package com.watb.chefmate.api

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.watb.chefmate.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

enum class AuthMode {
    PUBLIC,
    BEARER,
    OPTIONAL_BEARER,
    CHAT_DUAL
}

data class RawApiResult(
    val httpStatus: Int,
    val success: Boolean,
    val data: JsonElement? = null,
    val message: String? = null,
    val code: String? = null,
    val rawBody: String? = null
)

object ApiRequestExecutor {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun executeRaw(
        authMode: AuthMode = AuthMode.PUBLIC,
        requestFactory: () -> Request
    ): RawApiResult = withContext(Dispatchers.IO) {
        val initialToken = SessionRepository.getAccessToken()
        if ((authMode == AuthMode.BEARER || authMode == AuthMode.CHAT_DUAL) && initialToken.isNullOrBlank()) {
            return@withContext RawApiResult(
                httpStatus = 401,
                success = false,
                message = "Authentication required"
            )
        }

        var response = executeOnce(prepareRequest(requestFactory(), authMode, initialToken))
        if (shouldRefresh(response, authMode)) {
            val refreshed = SessionRepository.refreshSession()
            response = if (refreshed) {
                executeOnce(prepareRequest(requestFactory(), authMode, SessionRepository.getAccessToken()))
            } else {
                response.copy(message = response.message ?: "Session expired")
            }
        }
        response
    }

    private fun prepareRequest(request: Request, authMode: AuthMode, accessToken: String?): Request {
        val builder = request.newBuilder()
            .removeHeader("Authorization")
            .removeHeader("x-api-key")

        when (authMode) {
            AuthMode.PUBLIC -> Unit
            AuthMode.BEARER -> {
                if (!accessToken.isNullOrBlank()) {
                    builder.addHeader("Authorization", "Bearer $accessToken")
                }
            }
            AuthMode.OPTIONAL_BEARER -> {
                if (!accessToken.isNullOrBlank()) {
                    builder.addHeader("Authorization", "Bearer $accessToken")
                }
            }
            AuthMode.CHAT_DUAL -> {
                if (!accessToken.isNullOrBlank()) {
                    builder.addHeader("Authorization", "Bearer $accessToken")
                }
                builder.addHeader("x-api-key", BuildConfig.CHAT_API_KEY)
            }
        }

        return builder.build()
    }

    private fun executeOnce(request: Request): RawApiResult {
        return try {
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

    private suspend fun shouldRefresh(result: RawApiResult, authMode: AuthMode): Boolean {
        if (result.httpStatus != 401) return false
        if (authMode == AuthMode.PUBLIC) return false
        if (SessionRepository.getRefreshToken().isNullOrBlank()) return false

        val normalizedMessage = result.message?.lowercase().orEmpty()
        val normalizedCode = result.code?.lowercase().orEmpty()
        if ("api_key" in normalizedCode || "x-api-key" in normalizedMessage || "api key" in normalizedMessage) {
            return false
        }

        return true
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
}

fun JsonObject.stringOrNull(vararg names: String): String? {
    names.forEach { name ->
        val element = get(name)
        if (element != null && element.isJsonPrimitive) {
            return element.asString
        }
    }
    return null
}

fun JsonObject.booleanOrNull(vararg names: String): Boolean? {
    names.forEach { name ->
        val element = get(name)
        if (element != null && element.isJsonPrimitive) {
            return runCatching { element.asBoolean }.getOrNull()
        }
    }
    return null
}

fun JsonObject.intOrNull(vararg names: String): Int? {
    names.forEach { name ->
        val element = get(name)
        if (element != null && element.isJsonPrimitive) {
            return runCatching { element.asInt }.getOrNull()
        }
    }
    return null
}

fun JsonElement?.asObjectOrNull(): JsonObject? {
    return if (this != null && isJsonObject) asJsonObject else null
}
