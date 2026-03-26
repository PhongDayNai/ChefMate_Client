package com.watb.chefmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.watb.chefmate.api.AppFlowApiClient
import com.watb.chefmate.data.ChatBusinessCode
import com.watb.chefmate.data.ChatMessage
import com.watb.chefmate.data.ChatRole
import com.watb.chefmate.data.ChatSendMessageRequest
import com.watb.chefmate.data.ChatSession
import com.watb.chefmate.data.ChatSessionActiveRecipeRequest
import com.watb.chefmate.data.ChatSessionCreateRequest
import com.watb.chefmate.data.ChatSessionTitleRequest
import com.watb.chefmate.data.ChatUiMessage
import com.watb.chefmate.data.DietNote
import com.watb.chefmate.data.DietNoteDeleteRequest
import com.watb.chefmate.data.DietNoteUpsertRequest
import com.watb.chefmate.data.PantryDeleteRequest
import com.watb.chefmate.data.PantryItem
import com.watb.chefmate.data.PantryUpsertRequest
import com.watb.chefmate.data.PendingPreviousRecipePayload
import com.watb.chefmate.data.PendingResolveAction
import com.watb.chefmate.data.Recommendation
import com.watb.chefmate.data.RecommendationPayload
import com.watb.chefmate.data.ResolveAction
import com.watb.chefmate.data.ResolvePreviousSessionRequest
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

private const val DEFAULT_CHAT_LIMIT = 30
private const val DEFAULT_RECOMMEND_LIMIT = 10
private const val MAX_NO_PROGRESS_ATTEMPTS = 2

data class HomeFlowUiState(
    val isLoading: Boolean = false,
    val isRefreshingRecommendations: Boolean = false,
    val errorMessage: String? = null,
    val dietNotes: List<DietNote> = emptyList(),
    val pantryItems: List<PantryItem> = emptyList(),
    val recommendationLimit: Int = DEFAULT_RECOMMEND_LIMIT,
    val recommendations: List<Recommendation> = emptyList(),
    val readyToCook: List<Recommendation> = emptyList(),
    val almostReady: List<Recommendation> = emptyList()
)

data class ChatUiState(
    val ownerUserId: Int? = null,
    val currentSessionId: Int? = null,
    val currentSession: ChatSession? = null,
    val sessions: List<ChatSession> = emptyList(),
    val timeline: List<ChatUiMessage> = emptyList(),
    val hasMore: Boolean = true,
    val nextBeforeMessageId: Int? = null,
    val lastRequestedBeforeMessageId: Int? = null,
    val noProgressLoadCount: Int = 0,
    val limit: Int = DEFAULT_CHAT_LIMIT,
    val sending: Boolean = false,
    val loadingTimeline: Boolean = false,
    val loadingSessions: Boolean = false,
    val aiBusyRetryCount: Int = 0,
    val pendingPreviousRecipe: PendingPreviousRecipePayload? = null,
    val errorMessage: String? = null
)

class AppFlowViewModel : ViewModel() {
    private val gson = Gson()

    private val _homeState = MutableStateFlow(HomeFlowUiState())
    val homeState: StateFlow<HomeFlowUiState> = _homeState

    private val _chatState = MutableStateFlow(ChatUiState())
    val chatState: StateFlow<ChatUiState> = _chatState

    fun refreshHomeContext(userId: Int) {
        viewModelScope.launch {
            _homeState.update { it.copy(isLoading = true, errorMessage = null) }

            val (dietResult, pantryResult, recommendationResult) = coroutineScope {
                val dietDeferred = async { AppFlowApiClient.getDietNotes(userId) }
                val pantryDeferred = async { AppFlowApiClient.getPantryItems(userId) }
                val recommendationDeferred = async {
                    AppFlowApiClient.getRecommendations(userId = userId, limit = _homeState.value.recommendationLimit)
                }
                Triple(dietDeferred.await(), pantryDeferred.await(), recommendationDeferred.await())
            }

            val errorMessage = listOfNotNull(
                dietResult.message.takeIf { !dietResult.success },
                pantryResult.message.takeIf { !pantryResult.success },
                recommendationResult.message.takeIf { !recommendationResult.success }
            ).firstOrNull()

            val recommendationPayload = recommendationResult.data ?: RecommendationPayload()

            _homeState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = errorMessage,
                    dietNotes = dietResult.data ?: emptyList(),
                    pantryItems = pantryResult.data ?: emptyList(),
                    recommendationLimit = recommendationPayload.recommendationLimit,
                    recommendations = recommendationPayload.recommendations,
                    readyToCook = recommendationPayload.readyToCook,
                    almostReady = recommendationPayload.almostReady
                )
            }
        }
    }

    fun refreshRecommendations(userId: Int) {
        viewModelScope.launch {
            _homeState.update { it.copy(isRefreshingRecommendations = true, errorMessage = null) }
            val result = AppFlowApiClient.getRecommendations(userId = userId, limit = _homeState.value.recommendationLimit)
            val payload = result.data ?: RecommendationPayload()
            _homeState.update {
                it.copy(
                    isRefreshingRecommendations = false,
                    errorMessage = result.message.takeIf { !result.success },
                    recommendationLimit = payload.recommendationLimit,
                    recommendations = payload.recommendations,
                    readyToCook = payload.readyToCook,
                    almostReady = payload.almostReady
                )
            }
        }
    }

    fun refreshDietNotes(userId: Int) {
        viewModelScope.launch {
            val result = AppFlowApiClient.getDietNotes(userId)
            _homeState.update {
                it.copy(
                    dietNotes = result.data ?: it.dietNotes,
                    errorMessage = result.message.takeIf { !result.success }
                )
            }
        }
    }

    fun upsertDietNote(request: DietNoteUpsertRequest) {
        viewModelScope.launch {
            val result = AppFlowApiClient.upsertDietNote(request)
            _homeState.update {
                it.copy(
                    dietNotes = result.data ?: it.dietNotes,
                    errorMessage = result.message.takeIf { !result.success }
                )
            }
            if (result.success) {
                refreshRecommendations(request.userId)
            }
        }
    }

    fun deleteDietNote(userId: Int, noteId: Int) {
        viewModelScope.launch {
            val result = AppFlowApiClient.deleteDietNote(DietNoteDeleteRequest(userId, noteId))
            _homeState.update {
                it.copy(
                    dietNotes = result.data ?: it.dietNotes,
                    errorMessage = result.message.takeIf { !result.success }
                )
            }
            if (result.success) {
                refreshRecommendations(userId)
            }
        }
    }

    fun refreshPantry(userId: Int) {
        viewModelScope.launch {
            val result = AppFlowApiClient.getPantryItems(userId)
            _homeState.update {
                it.copy(
                    pantryItems = result.data ?: it.pantryItems,
                    errorMessage = result.message.takeIf { !result.success }
                )
            }
        }
    }

    fun upsertPantryItem(request: PantryUpsertRequest) {
        viewModelScope.launch {
            if (request.quantity < 0.0) {
                _homeState.update { it.copy(errorMessage = "Số lượng không được nhỏ hơn 0") }
                return@launch
            }

            val result = AppFlowApiClient.upsertPantryItem(request)
            _homeState.update {
                it.copy(
                    pantryItems = result.data ?: it.pantryItems,
                    errorMessage = result.message.takeIf { !result.success }
                )
            }
            if (result.success) {
                refreshRecommendations(request.userId)
            }
        }
    }

    fun deletePantryItem(userId: Int, pantryItemId: Int) {
        viewModelScope.launch {
            val result = AppFlowApiClient.deletePantryItem(PantryDeleteRequest(userId, pantryItemId))
            _homeState.update {
                it.copy(
                    pantryItems = result.data ?: it.pantryItems,
                    errorMessage = result.message.takeIf { !result.success }
                )
            }
            if (result.success) {
                refreshRecommendations(userId)
            }
        }
    }

    fun loadSessions(userId: Int) {
        viewModelScope.launch {
            _chatState.update { it.copy(loadingSessions = true, errorMessage = null) }
            val result = AppFlowApiClient.getSessions(userId = userId)
            _chatState.update {
                it.copy(
                    loadingSessions = false,
                    sessions = parseSessions(result.data),
                    errorMessage = result.message.takeIf { !result.success }
                )
            }
        }
    }

    fun prefetchChatHistorySources(userId: Int) {
        val state = _chatState.value
        if (state.loadingTimeline || state.sending) return
        if (state.ownerUserId == userId && state.timeline.isNotEmpty()) return

        viewModelScope.launch {
            fetchUnifiedTimeline(
                userId = userId,
                activeRecipeId = null,
                showLoading = false,
                createSessionIfEmpty = false,
                preserveExistingOnFailure = true
            )
        }
    }

    fun openSession(userId: Int, sessionId: Int) {
        viewModelScope.launch {
            _chatState.update { it.copy(loadingTimeline = true, errorMessage = null) }
            val result = AppFlowApiClient.getSessionDetail(sessionId = sessionId, userId = userId)

            val parsedSession = parsePrimarySession(result.data) ?: ChatSession(chatSessionId = sessionId, userId = userId)
            val sortedTimeline = deduplicateAndSort(parseMessages(result.data))

            _chatState.update {
                it.copy(
                    ownerUserId = userId,
                    loadingTimeline = false,
                    currentSessionId = parsedSession.chatSessionId,
                    currentSession = parsedSession,
                    timeline = sortedTimeline,
                    hasMore = false,
                    nextBeforeMessageId = null,
                    lastRequestedBeforeMessageId = null,
                    noProgressLoadCount = 0,
                    errorMessage = result.message.takeIf { !result.success }
                )
            }
        }
    }

    fun bootstrapUnifiedTimeline(userId: Int, activeRecipeId: Int? = null) {
        val cached = _chatState.value
        if (cached.ownerUserId == userId && cached.timeline.isNotEmpty()) {
            activeRecipeId?.takeIf { it > 0 }?.let { attachActiveRecipe(userId, it) }
            return
        }

        viewModelScope.launch {
            fetchUnifiedTimeline(
                userId = userId,
                activeRecipeId = activeRecipeId,
                showLoading = true,
                createSessionIfEmpty = true,
                preserveExistingOnFailure = true
            )
        }
    }

    private suspend fun fetchUnifiedTimeline(
        userId: Int,
        activeRecipeId: Int?,
        showLoading: Boolean,
        createSessionIfEmpty: Boolean,
        preserveExistingOnFailure: Boolean
    ) {
        val existingState = _chatState.value
        val sameUserState = existingState.takeIf { it.ownerUserId == userId }

        if (showLoading) {
            _chatState.update { it.copy(loadingTimeline = true, errorMessage = null) }
        }

        val timelineResult = AppFlowApiClient.getTimeline(userId = userId, limit = existingState.limit)

        val parsedTimeline = deduplicateAndSort(parseMessages(timelineResult.data))
        val parsedSession = parsePrimarySession(timelineResult.data)
        val inferredSessionId = parsedSession?.chatSessionId
            ?: parsedTimeline.lastOrNull()?.chatSessionId
            ?: sameUserState?.currentSessionId

        val shouldCreateSession = createSessionIfEmpty &&
            timelineResult.success &&
            parsedTimeline.isEmpty() &&
            inferredSessionId == null

        if (shouldCreateSession) {
            createSessionIfNeeded(userId = userId, activeRecipeId = activeRecipeId)
            return
        }

        val shouldApplyFetchedTimeline = timelineResult.success || parsedTimeline.isNotEmpty()

        _chatState.update { current ->
            val resolvedTimeline = when {
                shouldApplyFetchedTimeline -> parsedTimeline
                preserveExistingOnFailure && current.ownerUserId == userId -> current.timeline
                else -> emptyList()
            }

            val resolvedSessionId = if (shouldApplyFetchedTimeline) {
                inferredSessionId
            } else if (current.ownerUserId == userId) {
                current.currentSessionId
            } else {
                null
            }

            val resolvedSession = when {
                parsedSession != null -> parsedSession
                resolvedSessionId != null && current.ownerUserId == userId && current.currentSession?.chatSessionId == resolvedSessionId -> current.currentSession
                else -> null
            }

            val resolvedHasMore = if (shouldApplyFetchedTimeline) {
                parseHasMore(timelineResult.data, resolvedTimeline)
            } else {
                current.hasMore
            }

            val resolvedNextBefore = if (shouldApplyFetchedTimeline) {
                findNextBeforeMessageId(timelineResult.data, resolvedTimeline)
            } else {
                current.nextBeforeMessageId
            }

            current.copy(
                ownerUserId = userId,
                loadingTimeline = false,
                currentSessionId = resolvedSessionId,
                currentSession = resolvedSession,
                timeline = resolvedTimeline,
                hasMore = resolvedHasMore,
                nextBeforeMessageId = resolvedNextBefore,
                lastRequestedBeforeMessageId = null,
                noProgressLoadCount = 0,
                errorMessage = timelineResult.message.takeIf { !timelineResult.success }
            )
        }

        val selectedRecipeId = activeRecipeId?.takeIf { it > 0 }
        if (selectedRecipeId != null) {
            val sessionId = _chatState.value.currentSessionId
            if (sessionId != null) {
                AppFlowApiClient.updateActiveRecipe(
                    ChatSessionActiveRecipeRequest(
                        userId = userId,
                        chatSessionId = sessionId,
                        recipeId = selectedRecipeId
                    )
                )
                _chatState.update {
                    it.copy(
                        currentSession = it.currentSession?.copy(activeRecipeId = selectedRecipeId)
                    )
                }
            }
        }
    }

    fun createSessionIfNeeded(userId: Int, activeRecipeId: Int? = null) {
        viewModelScope.launch {
            val existingSessionId = _chatState.value.currentSessionId
            if (existingSessionId != null) {
                if (activeRecipeId != null && activeRecipeId > 0) {
                    AppFlowApiClient.updateActiveRecipe(
                        ChatSessionActiveRecipeRequest(
                            userId = userId,
                            chatSessionId = existingSessionId,
                            recipeId = activeRecipeId
                        )
                    )
                    _chatState.update {
                        it.copy(
                            currentSession = it.currentSession?.copy(activeRecipeId = activeRecipeId)
                        )
                    }
                }
                return@launch
            }

            val result = AppFlowApiClient.createSession(
                ChatSessionCreateRequest(
                    userId = userId,
                    title = "Bepes",
                    activeRecipeId = activeRecipeId?.takeIf { it > 0 }
                )
            )

            val session = parsePrimarySession(result.data)
            val introMessages = deduplicateAndSort(parseMessages(result.data))
            val sessionId = session?.chatSessionId ?: introMessages.lastOrNull()?.chatSessionId

            _chatState.update {
                val mergedTimeline = if (it.ownerUserId == userId) {
                    deduplicateAndSort(it.timeline + introMessages)
                } else {
                    introMessages
                }

                it.copy(
                    ownerUserId = userId,
                    currentSessionId = sessionId,
                    currentSession = session ?: it.currentSession,
                    timeline = mergedTimeline,
                    hasMore = if (hasPagingInfo(result.data)) parseHasMore(result.data, mergedTimeline) else false,
                    nextBeforeMessageId = if (hasPagingInfo(result.data)) findNextBeforeMessageId(result.data, mergedTimeline) else null,
                    lastRequestedBeforeMessageId = null,
                    noProgressLoadCount = 0,
                    errorMessage = result.message.takeIf { !result.success }
                )
            }
        }
    }

    fun attachActiveRecipe(userId: Int, recipeId: Int?) {
        if (recipeId == null || recipeId <= 0) return

        viewModelScope.launch {
            val sessionId = _chatState.value.currentSessionId
            if (sessionId == null) {
                createSessionIfNeeded(userId = userId, activeRecipeId = recipeId)
                return@launch
            }

            AppFlowApiClient.updateActiveRecipe(
                ChatSessionActiveRecipeRequest(
                    userId = userId,
                    chatSessionId = sessionId,
                    recipeId = recipeId
                )
            )

            _chatState.update {
                it.copy(
                    currentSession = it.currentSession?.copy(activeRecipeId = recipeId)
                )
            }
        }
    }

    fun selectRecipeForCurrentSession(userId: Int, recipeId: Int) {
        if (recipeId <= 0) return

        viewModelScope.launch {
            val sessionId = _chatState.value.currentSessionId
            if (sessionId == null) {
                createSessionIfNeeded(userId = userId, activeRecipeId = recipeId)
                bootstrapUnifiedTimeline(userId = userId, activeRecipeId = recipeId)
                return@launch
            }

            val result = AppFlowApiClient.updateActiveRecipe(
                ChatSessionActiveRecipeRequest(
                    userId = userId,
                    chatSessionId = sessionId,
                    recipeId = recipeId
                )
            )

            _chatState.update {
                it.copy(
                    currentSession = it.currentSession?.copy(activeRecipeId = recipeId),
                    errorMessage = result.message.takeIf { !result.success }
                )
            }
        }
    }

    fun completeCurrentSession(userId: Int) {
        val currentSessionId = _chatState.value.currentSessionId ?: return

        viewModelScope.launch {
            _chatState.update { it.copy(sending = true, errorMessage = null) }

            val result = AppFlowApiClient.resolvePreviousSession(
                ResolvePreviousSessionRequest(
                    userId = userId,
                    previousSessionId = currentSessionId,
                    action = ResolveAction.COMPLETE_AND_DEDUCT,
                    pendingUserMessage = null
                )
            )

            val session = parsePrimarySession(result.data)
            val parsedMessages = deduplicateAndSort(parseMessages(result.data))
            val resolvedSessionId = session?.chatSessionId ?: parsedMessages.lastOrNull()?.chatSessionId ?: currentSessionId
            val hasPaging = hasPagingInfo(result.data)

            _chatState.update {
                val updatedTimeline = if (parsedMessages.isNotEmpty()) parsedMessages else it.timeline
                it.copy(
                    ownerUserId = userId,
                    sending = false,
                    pendingPreviousRecipe = null,
                    currentSessionId = resolvedSessionId,
                    currentSession = session ?: it.currentSession,
                    timeline = updatedTimeline,
                    hasMore = if (hasPaging) parseHasMore(result.data, updatedTimeline) else it.hasMore,
                    nextBeforeMessageId = if (hasPaging) findNextBeforeMessageId(result.data, updatedTimeline) else it.nextBeforeMessageId,
                    lastRequestedBeforeMessageId = if (hasPaging) null else it.lastRequestedBeforeMessageId,
                    noProgressLoadCount = if (hasPaging) 0 else it.noProgressLoadCount,
                    errorMessage = result.message.takeIf { !result.success }
                )
            }

            if (result.success) {
                fetchUnifiedTimeline(
                    userId = userId,
                    activeRecipeId = session?.activeRecipeId,
                    showLoading = false,
                    createSessionIfEmpty = true,
                    preserveExistingOnFailure = true
                )
            }
        }
    }

    fun sendMessage(userId: Int, text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        if (_chatState.value.sending) return

        val currentSessionId = _chatState.value.currentSessionId
        val optimisticLocalId = UUID.randomUUID().toString()
        val optimisticUserMessage = ChatUiMessage(
            localId = optimisticLocalId,
            chatSessionId = currentSessionId,
            role = ChatRole.USER,
            text = trimmed,
            isPending = true
        )

        _chatState.update {
            it.copy(
                sending = true,
                errorMessage = null,
                timeline = deduplicateAndSort(it.timeline + optimisticUserMessage)
            )
        }

        viewModelScope.launch {
            val request = ChatSendMessageRequest(
                userId = userId,
                message = trimmed,
                stream = false
            )

            val retryDelays = listOf(3000L, 5000L, 8000L)
            var retryCount = 0
            var result = AppFlowApiClient.sendMessage(request)

            while (
                result.httpStatus == 503 &&
                result.code == ChatBusinessCode.AI_SERVER_BUSY &&
                retryCount < retryDelays.size
            ) {
                _chatState.update { it.copy(aiBusyRetryCount = retryCount + 1) }
                delay(retryDelays[retryCount])
                retryCount += 1
                result = AppFlowApiClient.sendMessage(request)
            }

            markMessageAsDelivered(optimisticLocalId)

            val pendingPayload = parsePendingPreviousRecipe(result.data)
            if (result.code == ChatBusinessCode.PENDING_PREVIOUS_RECIPE_COMPLETION && pendingPayload != null) {
                _chatState.update {
                    it.copy(
                        sending = false,
                        pendingPreviousRecipe = pendingPayload,
                        errorMessage = null
                    )
                }
                return@launch
            }

            val session = parsePrimarySession(result.data)
            val assistantMessages = parseMessages(result.data)
                .filter { it.role == ChatRole.ASSISTANT || it.role == ChatRole.SYSTEM }
            val hasPaging = hasPagingInfo(result.data)

            _chatState.update {
                val mergedTimeline = if (assistantMessages.isNotEmpty()) {
                    deduplicateAndSort(it.timeline + assistantMessages)
                } else {
                    val fallbackText = if (result.success) {
                        "Bepes đã nhận tin nhắn của bạn."
                    } else {
                        result.message ?: "Bepes hiện chưa thể phản hồi, vui lòng thử lại."
                    }
                    deduplicateAndSort(
                        it.timeline + ChatUiMessage(
                            localId = UUID.randomUUID().toString(),
                            chatSessionId = session?.chatSessionId ?: it.currentSessionId,
                            role = ChatRole.ASSISTANT,
                            text = fallbackText,
                            isPending = false
                        )
                    )
                }

                it.copy(
                    ownerUserId = userId,
                    sending = false,
                    currentSessionId = session?.chatSessionId ?: it.currentSessionId,
                    currentSession = session ?: it.currentSession,
                    timeline = mergedTimeline,
                    hasMore = if (hasPaging) parseHasMore(result.data, mergedTimeline) else it.hasMore,
                    nextBeforeMessageId = if (hasPaging) findNextBeforeMessageId(result.data, mergedTimeline) else it.nextBeforeMessageId,
                    lastRequestedBeforeMessageId = if (hasPaging) null else it.lastRequestedBeforeMessageId,
                    noProgressLoadCount = if (hasPaging) 0 else it.noProgressLoadCount,
                    aiBusyRetryCount = retryCount,
                    errorMessage = result.message.takeIf { !result.success && result.code != ChatBusinessCode.AI_SERVER_BUSY }
                )
            }
        }
    }

    fun resolvePendingPreviousRecipe(userId: Int, action: String) {
        val pending = _chatState.value.pendingPreviousRecipe ?: return
        val normalizedAction = if (action in ResolveAction.all) action else ResolveAction.SKIP_DEDUCTION

        viewModelScope.launch {
            _chatState.update { it.copy(sending = true, errorMessage = null) }

            val result = AppFlowApiClient.resolvePreviousSession(
                ResolvePreviousSessionRequest(
                    userId = userId,
                    previousSessionId = pending.previousSessionId,
                    action = normalizedAction,
                    pendingUserMessage = pending.pendingUserMessage
                )
            )

            val session = parsePrimarySession(result.data)
            val parsedMessages = deduplicateAndSort(parseMessages(result.data))
            val carriedMessage = parseCarriedPendingUserMessage(result.data)
            val resolvedSessionId = session?.chatSessionId ?: parsedMessages.lastOrNull()?.chatSessionId ?: _chatState.value.currentSessionId
            val hasPaging = hasPagingInfo(result.data)

            _chatState.update {
                val fallbackTimeline = when {
                    parsedMessages.isNotEmpty() -> parsedMessages
                    carriedMessage != null -> deduplicateAndSort(
                        it.timeline + ChatUiMessage(
                            localId = UUID.randomUUID().toString(),
                            chatSessionId = resolvedSessionId,
                            role = ChatRole.USER,
                            text = carriedMessage,
                            isPending = false
                        )
                    )
                    else -> it.timeline
                }

                it.copy(
                    ownerUserId = userId,
                    sending = false,
                    pendingPreviousRecipe = null,
                    currentSessionId = resolvedSessionId,
                    currentSession = session ?: it.currentSession,
                    timeline = fallbackTimeline,
                    hasMore = if (hasPaging) parseHasMore(result.data, fallbackTimeline) else it.hasMore,
                    nextBeforeMessageId = if (hasPaging) findNextBeforeMessageId(result.data, fallbackTimeline) else it.nextBeforeMessageId,
                    lastRequestedBeforeMessageId = if (hasPaging) null else it.lastRequestedBeforeMessageId,
                    noProgressLoadCount = if (hasPaging) 0 else it.noProgressLoadCount,
                    errorMessage = result.message.takeIf { !result.success }
                )
            }

            if (result.success) {
                val allowCreateIfEmpty = normalizedAction != ResolveAction.CONTINUE_CURRENT_SESSION
                fetchUnifiedTimeline(
                    userId = userId,
                    activeRecipeId = session?.activeRecipeId,
                    showLoading = false,
                    createSessionIfEmpty = allowCreateIfEmpty,
                    preserveExistingOnFailure = true
                )
            }
        }
    }

    fun clearPendingPreviousRecipe() {
        _chatState.update { it.copy(pendingPreviousRecipe = null) }
    }

    fun loadMoreTimeline(userId: Int) {
        val current = _chatState.value
        if (current.ownerUserId != null && current.ownerUserId != userId) return
        val beforeMessageId = current.nextBeforeMessageId
        if (!current.hasMore || current.loadingTimeline || beforeMessageId == null) return

        if (
            current.lastRequestedBeforeMessageId == beforeMessageId &&
            current.noProgressLoadCount >= MAX_NO_PROGRESS_ATTEMPTS
        ) {
            _chatState.update { it.copy(hasMore = false) }
            return
        }

        viewModelScope.launch {
            _chatState.update {
                it.copy(
                    loadingTimeline = true,
                    errorMessage = null,
                    lastRequestedBeforeMessageId = beforeMessageId
                )
            }

            val result = AppFlowApiClient.getTimeline(
                userId = userId,
                limit = current.limit,
                beforeMessageId = beforeMessageId
            )

            val olderMessages = deduplicateAndSort(parseMessages(result.data))
            if (!result.success && olderMessages.isEmpty()) {
                _chatState.update {
                    it.copy(
                        loadingTimeline = false,
                        errorMessage = result.message.takeIf { !result.success }
                    )
                }
                return@launch
            }

            _chatState.update { latest ->
                val mergedTimeline = deduplicateAndSort(olderMessages + latest.timeline)
                val addedCount = mergedTimeline.size - latest.timeline.size
                val nextBefore = findNextBeforeMessageId(result.data, mergedTimeline)
                val hasMoreFromServer = parseHasMore(result.data, olderMessages)
                val cursorAdvanced = nextBefore != null && nextBefore != beforeMessageId
                val noProgressCount = if (addedCount > 0 || cursorAdvanced) {
                    0
                } else {
                    latest.noProgressLoadCount + 1
                }

                val hasMore = hasMoreFromServer &&
                    nextBefore != null &&
                    noProgressCount < MAX_NO_PROGRESS_ATTEMPTS

                latest.copy(
                    ownerUserId = userId,
                    loadingTimeline = false,
                    timeline = mergedTimeline,
                    nextBeforeMessageId = nextBefore,
                    hasMore = hasMore,
                    noProgressLoadCount = noProgressCount,
                    errorMessage = result.message.takeIf { !result.success }
                )
            }
        }
    }

    fun renameSession(userId: Int, chatSessionId: Int, title: String) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return

        viewModelScope.launch {
            val result = AppFlowApiClient.updateSessionTitle(
                ChatSessionTitleRequest(
                    userId = userId,
                    chatSessionId = chatSessionId,
                    title = trimmed
                )
            )

            _chatState.update {
                it.copy(
                    sessions = it.sessions.map { session ->
                        if (session.chatSessionId == chatSessionId) {
                            session.copy(title = trimmed)
                        } else {
                            session
                        }
                    },
                    currentSession = if (it.currentSession?.chatSessionId == chatSessionId) {
                        it.currentSession.copy(title = trimmed)
                    } else {
                        it.currentSession
                    },
                    errorMessage = result.message.takeIf { !result.success }
                )
            }
        }
    }

    fun deleteSession(userId: Int, chatSessionId: Int) {
        viewModelScope.launch {
            val result = AppFlowApiClient.deleteSession(chatSessionId, userId)

            _chatState.update {
                val removedCurrentSession = it.currentSessionId == chatSessionId
                it.copy(
                    sessions = it.sessions.filter { session -> session.chatSessionId != chatSessionId },
                    currentSessionId = if (removedCurrentSession) null else it.currentSessionId,
                    currentSession = if (removedCurrentSession) null else it.currentSession,
                    timeline = if (removedCurrentSession) emptyList() else it.timeline,
                    nextBeforeMessageId = if (removedCurrentSession) null else it.nextBeforeMessageId,
                    hasMore = if (removedCurrentSession) false else it.hasMore,
                    lastRequestedBeforeMessageId = if (removedCurrentSession) null else it.lastRequestedBeforeMessageId,
                    noProgressLoadCount = if (removedCurrentSession) 0 else it.noProgressLoadCount,
                    errorMessage = result.message.takeIf { !result.success }
                )
            }
        }
    }

    fun clearChatError() {
        _chatState.update { it.copy(errorMessage = null) }
    }

    private fun markMessageAsDelivered(localId: String) {
        _chatState.update {
            it.copy(
                timeline = it.timeline.map { message ->
                    if (message.localId == localId) {
                        message.copy(isPending = false)
                    } else {
                        message
                    }
                }
            )
        }
    }

    private fun parseSessions(data: JsonElement?): List<ChatSession> {
        if (data == null || data.isJsonNull) return emptyList()

        return when {
            data.isJsonArray -> parseSessionList(data)
            data.isJsonObject -> {
                val obj = data.asJsonObject
                when {
                    obj.get("sessions")?.isJsonArray == true -> parseSessionList(obj.get("sessions"))
                    obj.get("items")?.isJsonArray == true -> parseSessionList(obj.get("items"))
                    obj.get("data")?.isJsonArray == true -> parseSessionList(obj.get("data"))
                    else -> listOfNotNull(parseSession(obj))
                }
            }
            else -> emptyList()
        }
    }

    private fun parsePrimarySession(data: JsonElement?): ChatSession? {
        if (data == null || data.isJsonNull || !data.isJsonObject) return null
        val obj = data.asJsonObject

        val latestSessionElement = obj.get("latestSession")
        if (latestSessionElement != null && latestSessionElement.isJsonObject) {
            val latestSession = parseSession(latestSessionElement)
            if (latestSession?.chatSessionId != null) return latestSession
        }

        val nestedSessionElement = obj.get("session")
        if (nestedSessionElement != null && nestedSessionElement.isJsonObject) {
            val session = parseSession(nestedSessionElement)
            if (session?.chatSessionId != null) return session
        }

        val newSessionElement = obj.get("newSession")
        if (newSessionElement != null && newSessionElement.isJsonObject) {
            val newSession = parseSession(newSessionElement)
            if (newSession?.chatSessionId != null) return newSession
        }

        val direct = parseSession(obj)
        if (direct?.chatSessionId != null) return direct

        return null
    }

    private fun parseSession(data: JsonElement?): ChatSession? {
        if (data == null || data.isJsonNull) return null
        return runCatching {
            gson.fromJson(data, ChatSession::class.java)
        }.getOrNull()
    }

    private fun parseSessionList(data: JsonElement?): List<ChatSession> {
        if (data == null || data.isJsonNull) return emptyList()
        return runCatching {
            gson.fromJson<List<ChatSession>>(data, object : TypeToken<List<ChatSession>>() {}.type)
        }.getOrElse { emptyList() }
    }

    private fun parseMessages(data: JsonElement?): List<ChatUiMessage> {
        if (data == null || data.isJsonNull) return emptyList()

        return when {
            data.isJsonArray -> parseChatMessageList(data)
            data.isJsonObject -> {
                val obj = data.asJsonObject
                val fallbackSessionId = parsePrimarySession(data)?.chatSessionId

                when {
                    obj.get("messages")?.isJsonArray == true -> applyFallbackSessionId(
                        parseChatMessageList(obj.get("messages")),
                        fallbackSessionId
                    )
                    obj.get("timeline")?.isJsonArray == true -> applyFallbackSessionId(
                        parseChatMessageList(obj.get("timeline")),
                        fallbackSessionId
                    )
                    obj.get("items")?.isJsonArray == true -> applyFallbackSessionId(
                        parseChatMessageList(obj.get("items")),
                        fallbackSessionId
                    )
                    obj.get("assistantMessage")?.isJsonObject == true -> parseChatMessageList(obj.get("assistantMessage"))
                    obj.get("assistantMessage")?.isJsonPrimitive == true -> listOf(
                        ChatUiMessage(
                            localId = UUID.randomUUID().toString(),
                            chatSessionId = parsePrimarySession(data)?.chatSessionId,
                            role = ChatRole.ASSISTANT,
                            text = obj.get("assistantMessage").asString
                        )
                    )
                    obj.get("carriedPendingUserMessage")?.isJsonPrimitive == true -> listOf(
                        ChatUiMessage(
                            localId = UUID.randomUUID().toString(),
                            chatSessionId = parsePrimarySession(data)?.chatSessionId,
                            role = ChatRole.USER,
                            text = obj.get("carriedPendingUserMessage").asString
                        )
                    )
                    hasMessageShape(obj) -> parseChatMessageList(obj)
                    else -> emptyList()
                }
            }
            else -> emptyList()
        }
    }

    private fun parseChatMessageList(data: JsonElement?): List<ChatUiMessage> {
        if (data == null || data.isJsonNull) return emptyList()

        val messages = if (data.isJsonArray) {
            runCatching {
                gson.fromJson<List<ChatMessage>>(data, object : TypeToken<List<ChatMessage>>() {}.type)
            }.getOrElse { emptyList() }
        } else {
            listOfNotNull(runCatching { gson.fromJson(data, ChatMessage::class.java) }.getOrNull())
        }

        return messages.mapNotNull { message ->
            val text = message.content ?: message.text ?: message.message
            if (text.isNullOrBlank()) {
                null
            } else {
                ChatUiMessage(
                    localId = UUID.randomUUID().toString(),
                    messageId = message.messageId ?: message.chatMessageId,
                    chatSessionId = message.chatSessionId,
                    sessionTitle = message.sessionTitle,
                    activeRecipeId = message.activeRecipeId,
                    isSessionStart = message.isSessionStart == true,
                    role = ChatRole.normalize(message.role),
                    text = text,
                    createdAt = message.createdAt,
                    isPending = false
                )
            }
        }
    }

    private fun parsePendingPreviousRecipe(data: JsonElement?): PendingPreviousRecipePayload? {
        if (data == null || data.isJsonNull || !data.isJsonObject) return null
        val root = data.asJsonObject
        val candidate = when {
            root.get("pendingPreviousRecipe")?.isJsonObject == true -> root.getAsJsonObject("pendingPreviousRecipe")
            root.get("pending")?.isJsonObject == true -> root.getAsJsonObject("pending")
            else -> root
        }

        val previousSessionId = candidate.intOrNull("previousSessionId") ?: return null
        val parsedActions = parsePendingResolveActions(candidate.get("actions"))
        val fallbackActions = listOf(
            PendingResolveAction(
                id = ResolveAction.COMPLETE_AND_DEDUCT,
                label = "Hoàn thành & trừ nguyên liệu"
            ),
            PendingResolveAction(
                id = ResolveAction.SKIP_DEDUCTION,
                label = "Bỏ qua (không trừ)"
            ),
            PendingResolveAction(
                id = ResolveAction.CONTINUE_CURRENT_SESSION,
                label = "Chưa nấu xong, tiếp tục trò chuyện"
            )
        )
        val resolvedActions = ResolveAction.all.mapNotNull { requiredAction ->
            parsedActions.firstOrNull { it.id == requiredAction } ?: fallbackActions.firstOrNull { it.id == requiredAction }
        }

        return PendingPreviousRecipePayload(
            previousSessionId = previousSessionId,
            recipeId = candidate.intOrNull("recipeId"),
            recipeName = candidate.stringOrNull("recipeName"),
            pendingUserMessage = candidate.stringOrNull("pendingUserMessage"),
            reminderMessage = candidate.stringOrNull("reminderMessage"),
            actions = resolvedActions
        )
    }

    private fun parsePendingResolveActions(data: JsonElement?): List<PendingResolveAction> {
        if (data == null || data.isJsonNull || !data.isJsonArray) return emptyList()
        return data.asJsonArray.mapNotNull { element ->
            if (!element.isJsonObject) return@mapNotNull null
            val obj = element.asJsonObject
            val id = obj.stringOrNull("id") ?: return@mapNotNull null
            val label = obj.stringOrNull("label") ?: id
            PendingResolveAction(id = id, label = label)
        }
    }

    private fun parseCarriedPendingUserMessage(data: JsonElement?): String? {
        if (data == null || data.isJsonNull || !data.isJsonObject) return null
        return data.asJsonObject.stringOrNull("carriedPendingUserMessage")
    }

    private fun hasPagingInfo(data: JsonElement?): Boolean {
        if (data == null || data.isJsonNull || !data.isJsonObject) return false
        val obj = data.asJsonObject
        return obj.get("nextBeforeMessageId") != null ||
            obj.get("hasMore") != null ||
            (obj.get("paging")?.isJsonObject == true)
    }

    private fun parseHasMore(data: JsonElement?, parsedMessages: List<ChatUiMessage>): Boolean {
        if (data != null && data.isJsonObject) {
            data.asJsonObject.booleanOrNull("hasMore")?.let { return it }
            val pagingElement = data.asJsonObject.get("paging")
            if (pagingElement != null && pagingElement.isJsonObject) {
                pagingElement.asJsonObject.booleanOrNull("hasMore")?.let { return it }
            }
        }

        return parsedMessages.isNotEmpty()
    }

    private fun findNextBeforeMessageId(data: JsonElement?, fallbackTimeline: List<ChatUiMessage>): Int? {
        var hasMoreHint: Boolean? = null

        if (data != null && data.isJsonObject) {
            val root = data.asJsonObject
            root.intOrNull("nextBeforeMessageId")?.let { return it }
            hasMoreHint = root.booleanOrNull("hasMore")

            val pagingElement = root.get("paging")
            if (pagingElement != null && pagingElement.isJsonObject) {
                val pagingObject = pagingElement.asJsonObject
                pagingObject.intOrNull("nextBeforeMessageId")?.let { return it }
                hasMoreHint = pagingObject.booleanOrNull("hasMore") ?: hasMoreHint
            }
        }

        if (hasMoreHint == false) return null
        return fallbackTimeline.mapNotNull { it.messageId }.minOrNull()
    }

    private fun hasMessageShape(obj: JsonObject): Boolean {
        return obj.get("messageId") != null ||
            obj.get("chatMessageId") != null ||
            obj.get("role") != null ||
            obj.get("content") != null ||
            obj.get("message") != null
    }

    private fun deduplicateAndSort(messages: List<ChatUiMessage>): List<ChatUiMessage> {
        val distinct = linkedMapOf<String, ChatUiMessage>()
        messages.forEach { message ->
            val key = message.messageId?.toString() ?: message.localId
            distinct[key] = message
        }
        return sortMessages(distinct.values.toList())
    }

    private fun applyFallbackSessionId(messages: List<ChatUiMessage>, fallbackSessionId: Int?): List<ChatUiMessage> {
        if (fallbackSessionId == null) return messages
        return messages.map { message ->
            if (message.chatSessionId == null) {
                message.copy(chatSessionId = fallbackSessionId)
            } else {
                message
            }
        }
    }

    private fun sortMessages(messages: List<ChatUiMessage>): List<ChatUiMessage> {
        return messages.sortedWith(
            compareBy<ChatUiMessage> { it.messageId ?: Int.MAX_VALUE }
                .thenBy { it.createdAt ?: "" }
                .thenBy { it.localId }
        )
    }

    private fun JsonObject.stringOrNull(key: String): String? {
        val element = get(key) ?: return null
        if (element.isJsonNull) return null
        return runCatching { element.asString }.getOrNull()
    }

    private fun JsonObject.intOrNull(key: String): Int? {
        val element = get(key) ?: return null
        if (element.isJsonNull) return null
        return runCatching { element.asInt }.getOrNull()
    }

    private fun JsonObject.booleanOrNull(key: String): Boolean? {
        val element = get(key) ?: return null
        if (element.isJsonNull) return null
        return runCatching { element.asBoolean }.getOrNull()
    }
}
