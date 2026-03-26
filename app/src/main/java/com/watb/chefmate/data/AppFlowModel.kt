package com.watb.chefmate.data

import com.google.gson.annotations.SerializedName

data class ApiEnvelope<T>(
    val success: Boolean = false,
    val data: T? = null,
    val message: String? = null,
    val code: String? = null
)

data class ApiNetworkResult<T>(
    val httpStatus: Int,
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val code: String? = null
)

data class DietNote(
    val noteId: Int? = null,
    val userId: Int = 0,
    val noteType: String = "",
    val label: String = "",
    val keywords: List<String> = emptyList(),
    val instruction: String? = null,
    val isActive: Boolean = true,
    val startAt: String? = null,
    val endAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class DietNoteUpsertRequest(
    val noteId: Int? = null,
    val userId: Int,
    val noteType: String,
    val label: String,
    val keywords: List<String> = emptyList(),
    val instruction: String? = null,
    val isActive: Boolean = true,
    val startAt: String? = null,
    val endAt: String? = null
)

data class DietNoteDeleteRequest(
    val userId: Int,
    val noteId: Int
)

data class PantryItem(
    val pantryItemId: Int? = null,
    val userId: Int = 0,
    val ingredientId: Int? = null,
    val ingredientName: String = "",
    val quantity: Double = 0.0,
    val unit: String = "",
    val expiresAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class PantryUpsertRequest(
    val pantryItemId: Int? = null,
    val userId: Int,
    val ingredientName: String,
    val quantity: Double,
    val unit: String,
    val expiresAt: String? = null
)

data class PantryDeleteRequest(
    val userId: Int,
    val pantryItemId: Int
)

data class RecommendationRequest(
    val userId: Int,
    val limit: Int = 10
)

data class MissingIngredient(
    val ingredientName: String,
    val need: Double? = null,
    val have: Double? = null,
    val unit: String? = null
)

data class Recommendation(
    val index: Int? = null,
    val recommendationType: String = "",
    val recipeId: Int = 0,
    val recipeName: String = "",
    val image: String? = null,
    val cookingTime: String? = null,
    val ration: Int? = null,
    val completionRate: Int? = null,
    val missing: List<MissingIngredient> = emptyList()
)

data class RecommendationPayload(
    val recommendationLimit: Int = 10,
    val recommendations: List<Recommendation> = emptyList(),
    val readyToCook: List<Recommendation> = emptyList(),
    val almostReady: List<Recommendation> = emptyList()
)

data class ChatSession(
    val chatSessionId: Int? = null,
    val userId: Int? = null,
    val title: String? = null,
    val activeRecipeId: Int? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class ChatMessage(
    val messageId: Int? = null,
    @SerializedName("chatMessageId") val chatMessageId: Int? = null,
    val chatSessionId: Int? = null,
    val sessionTitle: String? = null,
    val activeRecipeId: Int? = null,
    val isSessionStart: Boolean? = null,
    val role: String? = null,
    val content: String? = null,
    val message: String? = null,
    val text: String? = null,
    val createdAt: String? = null,
    val senderName: String? = null
)

data class ChatSessionDetail(
    val session: ChatSession? = null,
    val messages: List<ChatMessage> = emptyList()
)

data class ChatSessionCreateRequest(
    val userId: Int,
    val firstMessage: String? = null,
    val title: String? = null,
    val activeRecipeId: Int? = null,
    val model: String? = null
)

data class ChatSessionTitleRequest(
    val userId: Int,
    val chatSessionId: Int,
    val title: String
)

data class ChatSessionActiveRecipeRequest(
    val userId: Int,
    val chatSessionId: Int,
    val recipeId: Int?
)

data class ChatSendMessageRequest(
    val userId: Int,
    val message: String,
    val stream: Boolean = false,
    val chatSessionId: Int? = null
)

data class PendingPreviousRecipePayload(
    val previousSessionId: Int,
    val recipeId: Int? = null,
    val recipeName: String? = null,
    val pendingUserMessage: String? = null,
    val reminderMessage: String? = null,
    val actions: List<PendingResolveAction> = emptyList()
)

data class PendingResolveAction(
    val id: String,
    val label: String
)

data class ResolvePreviousSessionRequest(
    val userId: Int,
    val previousSessionId: Int,
    val action: String,
    val pendingUserMessage: String? = null
)

data class ChatUiMessage(
    val localId: String,
    val messageId: Int? = null,
    val chatSessionId: Int? = null,
    val sessionTitle: String? = null,
    val activeRecipeId: Int? = null,
    val isSessionStart: Boolean = false,
    val role: String,
    val text: String,
    val createdAt: String? = null,
    val isPending: Boolean = false
)

object DietNoteType {
    const val ALLERGY = "allergy"
    const val RESTRICTION = "restriction"
    const val PREFERENCE = "preference"
    const val HEALTH_NOTE = "health_note"

    val all = listOf(ALLERGY, RESTRICTION, PREFERENCE, HEALTH_NOTE)
}

object RecommendationType {
    const val READY_TO_COOK = "ready_to_cook"
    const val ALMOST_READY = "almost_ready"
}

object ChatBusinessCode {
    const val AI_SERVER_BUSY = "AI_SERVER_BUSY"
    const val PENDING_PREVIOUS_RECIPE_COMPLETION = "PENDING_PREVIOUS_RECIPE_COMPLETION"
}

object ResolveAction {
    const val COMPLETE_AND_DEDUCT = "complete_and_deduct"
    const val SKIP_DEDUCTION = "skip_deduction"
    const val CONTINUE_CURRENT_SESSION = "continue_current_session"

    val all = listOf(COMPLETE_AND_DEDUCT, SKIP_DEDUCTION, CONTINUE_CURRENT_SESSION)
}

object ChatRole {
    const val USER = "user"
    const val ASSISTANT = "assistant"
    const val SYSTEM = "system"

    fun normalize(rawRole: String?): String {
        val role = rawRole?.lowercase().orEmpty()
        return when {
            role.contains("assistant") || role.contains("bot") || role.contains("ai") -> ASSISTANT
            role.contains("system") -> SYSTEM
            else -> USER
        }
    }
}

data class JsonMessageEnvelope(
    @SerializedName("session") val session: ChatSession? = null,
    @SerializedName("assistantMessage") val assistantMessage: ChatMessage? = null,
    @SerializedName("messages") val messages: List<ChatMessage>? = null,
    @SerializedName("timeline") val timeline: List<ChatMessage>? = null,
    @SerializedName("pendingUserMessage") val pendingUserMessage: String? = null,
    @SerializedName("carriedPendingUserMessage") val carriedPendingUserMessage: String? = null
)
