package com.watb.chefmate.api

import com.google.gson.JsonElement
import com.watb.chefmate.data.AuthResponse
import com.watb.chefmate.data.AuthSessionPayload
import com.watb.chefmate.data.UserData

fun parseAuthResponse(result: RawApiResult): AuthResponse {
    return AuthResponse(
        success = result.success,
        data = parseAuthSessionPayload(result.data),
        message = result.message
    )
}

fun parseAuthSessionPayload(element: JsonElement?): AuthSessionPayload? {
    val obj = element.asObjectOrNull() ?: return null
    val accessToken = obj.stringOrNull("accessToken", "access_token", "token", "jwt")
    val refreshToken = obj.stringOrNull("refreshToken", "refresh_token")

    val userElement = when {
        obj.get("user") != null -> obj.get("user")
        obj.get("userData") != null -> obj.get("userData")
        obj.get("profile") != null -> obj.get("profile")
        obj.intOrNull("userId", "id") != null -> obj
        else -> null
    }

    val user = parseUserData(userElement)
    if (accessToken.isNullOrBlank() || refreshToken.isNullOrBlank() || user == null) {
        return null
    }

    return AuthSessionPayload(
        accessToken = accessToken,
        refreshToken = refreshToken,
        user = user
    )
}

fun parseUserData(element: JsonElement?): UserData? {
    val obj = element.asObjectOrNull() ?: return null
    val userId = obj.intOrNull("userId", "id") ?: return null
    val fullName = obj.stringOrNull("fullName", "fullname", "name") ?: return null
    val phone = obj.stringOrNull("phone", "phoneNumber") ?: ""
    val email = obj.stringOrNull("email") ?: ""
    val passwordHash = obj.stringOrNull("passwordHash", "password_hash")
    val followCount = obj.intOrNull("followCount", "followers", "follow_count") ?: 0
    val recipeCount = obj.intOrNull("recipeCount", "recipe_count") ?: 0
    val createdAt = obj.stringOrNull("createdAt", "created_at") ?: ""

    return UserData(
        userId = userId,
        fullName = fullName,
        phone = phone,
        email = email,
        passwordHash = passwordHash,
        followCount = followCount,
        recipeCount = recipeCount,
        createdAt = createdAt
    )
}
