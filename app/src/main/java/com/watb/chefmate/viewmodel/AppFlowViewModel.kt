package com.watb.chefmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.watb.chefmate.api.ApiClient
import com.watb.chefmate.api.AppFlowApiClient
import com.watb.chefmate.api.RawApiResult
import com.watb.chefmate.api.asObjectOrNull
import com.watb.chefmate.api.booleanOrNull
import com.watb.chefmate.api.intOrNull
import com.watb.chefmate.api.stringOrNull
import com.watb.chefmate.data.ChatBusinessCode
import com.watb.chefmate.data.ChatMessage
import com.watb.chefmate.data.ChatMessageKind
import com.watb.chefmate.data.ChatRole
import com.watb.chefmate.data.ChatSendMessageRequest
import com.watb.chefmate.data.ChatSession
import com.watb.chefmate.data.ChatSessionTitleRequest
import com.watb.chefmate.data.ChatUiMessage
import com.watb.chefmate.data.DietNote
import com.watb.chefmate.data.DietNoteDeleteRequest
import com.watb.chefmate.data.DietNoteUpsertRequest
import com.watb.chefmate.data.MealCompleteRequest
import com.watb.chefmate.data.MealCompletionAction
import com.watb.chefmate.data.MealCompletionResolveRequest
import com.watb.chefmate.data.MealCompletionType
import com.watb.chefmate.data.MealPrimaryRecipeRequest
import com.watb.chefmate.data.MealRecipeState
import com.watb.chefmate.data.MealRecipeStatus
import com.watb.chefmate.data.MealRecipeStatusRequest
import com.watb.chefmate.data.MealSessionCreateRequest
import com.watb.chefmate.data.MealSessionRecipeInput
import com.watb.chefmate.data.MealSessionRecipesReplaceRequest
import com.watb.chefmate.data.MealSessionUiState
import com.watb.chefmate.data.PantryDeleteRequest
import com.watb.chefmate.data.PantryItem
import com.watb.chefmate.data.PantryUpsertRequest
import com.watb.chefmate.data.PendingMealPolicyPrompt
import com.watb.chefmate.data.PendingPreviousRecipePayload
import com.watb.chefmate.data.PendingPrimaryRecipeSwitchPayload
import com.watb.chefmate.data.PendingResolveAction
import com.watb.chefmate.data.PromptStatus
import com.watb.chefmate.data.Recommendation
import com.watb.chefmate.data.RecommendationPayload
import com.watb.chefmate.data.RecommendationType
import com.watb.chefmate.data.ResolveAction
import com.watb.chefmate.data.ResolvePreviousSessionRequest
import com.watb.chefmate.data.Recipe
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

private const val DEFAULT_CHAT_LIMIT = 32
private const val DEFAULT_RECOMMEND_LIMIT = 12
private const val DEFAULT_RETRY_AFTER_MS = 5000L
private const val MAX_NO_PROGRESS_ATTEMPTS = 2

private data class HomeBootstrapResult(
    val diet: com.watb.chefmate.data.ApiNetworkResult<List<DietNote>>,
    val pantry: com.watb.chefmate.data.ApiNetworkResult<List<PantryItem>>,
    val recommendations: com.watb.chefmate.data.ApiNetworkResult<RecommendationPayload>,
    val trending: com.watb.chefmate.data.RecipeListResponse?
)

data class HomeFlowUiState(
    val isLoading: Boolean = false,
    val isRefreshingRecommendations: Boolean = false,
    val errorMessage: String? = null,
    val dietNotes: List<DietNote> = emptyList(),
    val pantryItems: List<PantryItem> = emptyList(),
    val recommendationLimit: Int = DEFAULT_RECOMMEND_LIMIT,
    val recommendations: List<Recommendation> = emptyList(),
    val readyToCook: List<Recommendation> = emptyList(),
    val almostReady: List<Recommendation> = emptyList(),
    val trendingSuggestions: List<Recommendation> = emptyList()
)

data class ChatUiState(
    val ownerUserId: Int? = null,
    val historyMode: Boolean = false,
    val isBootstrapping: Boolean = false,
    val restoreCompleted: Boolean = false,
    val currentSessionId: Int? = null,
    val currentSession: ChatSession? = null,
    val mealSession: MealSessionUiState = MealSessionUiState(),
    val mealItems: List<MealRecipeState> = emptyList(),
    val sessions: List<ChatSession> = emptyList(),
    val timeline: List<ChatUiMessage> = emptyList(),
    val hasMore: Boolean = true,
    val nextBeforeMessageId: Int? = null,
    val lastRequestedBeforeMessageId: Int? = null,
    val noProgressLoadCount: Int = 0,
    val limit: Int = DEFAULT_CHAT_LIMIT,
    val sending: Boolean = false,
    val mealSyncing: Boolean = false,
    val loadingTimeline: Boolean = false,
    val loadingSessions: Boolean = false,
    val pendingPreviousRecipe: PendingPreviousRecipePayload? = null,
    val pendingMealPolicyPrompt: PendingMealPolicyPrompt? = null,
    val pendingPrimaryRecipeSwitch: PendingPrimaryRecipeSwitchPayload? = null,
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

            val (dietResult, pantryResult, recommendationResult, trendingResult) = coroutineScope {
                val dietDeferred = async { AppFlowApiClient.getDietNotes(userId) }
                val pantryDeferred = async { AppFlowApiClient.getPantryItems(userId) }
                val recommendationDeferred = async {
                    AppFlowApiClient.getRecommendations(userId = userId, limit = _homeState.value.recommendationLimit)
                }
                val trendingDeferred = async {
                    ApiClient.getTopTrending(userId = userId, page = 1, limit = 20, period = "all")
                }
                HomeBootstrapResult(
                    diet = dietDeferred.await(),
                    pantry = pantryDeferred.await(),
                    recommendations = recommendationDeferred.await(),
                    trending = trendingDeferred.await()
                )
            }

            val recommendationPayload = recommendationResult.data ?: RecommendationPayload(
                recommendationLimit = _homeState.value.recommendationLimit
            )
            val trendingSuggestions = mapRecipesToRecommendations(trendingResult?.data.orEmpty())
            val fallbackRecommendations = recommendationPayload.recommendations.ifEmpty { trendingSuggestions }

            val errorMessage = listOfNotNull(
                dietResult.message.takeIf { !dietResult.success },
                pantryResult.message.takeIf { !pantryResult.success },
                recommendationResult.message.takeIf { !recommendationResult.success },
                trendingResult?.message.takeIf { trendingResult?.success == false }
            ).firstOrNull()

            _homeState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = errorMessage,
                    dietNotes = dietResult.data ?: emptyList(),
                    pantryItems = pantryResult.data ?: emptyList(),
                    recommendationLimit = recommendationPayload.recommendationLimit,
                    recommendations = fallbackRecommendations,
                    readyToCook = recommendationPayload.readyToCook,
                    almostReady = recommendationPayload.almostReady,
                    trendingSuggestions = trendingSuggestions
                )
            }
        }
    }

    fun refreshRecommendations(userId: Int) {
        viewModelScope.launch {
            _homeState.update { it.copy(isRefreshingRecommendations = true, errorMessage = null) }
            val recommendationResult = AppFlowApiClient.getRecommendations(
                userId = userId,
                limit = _homeState.value.recommendationLimit
            )
            val trendingResult = ApiClient.getTopTrending(userId = userId, page = 1, limit = 20, period = "all")
            val payload = recommendationResult.data ?: RecommendationPayload(
                recommendationLimit = _homeState.value.recommendationLimit
            )
            val trendingSuggestions = mapRecipesToRecommendations(trendingResult?.data.orEmpty())
            _homeState.update {
                it.copy(
                    isRefreshingRecommendations = false,
                    errorMessage = listOfNotNull(
                        recommendationResult.message.takeIf { !recommendationResult.success },
                        trendingResult?.message.takeIf { trendingResult?.success == false }
                    ).firstOrNull(),
                    recommendationLimit = payload.recommendationLimit,
                    recommendations = payload.recommendations.ifEmpty { trendingSuggestions },
                    readyToCook = payload.readyToCook,
                    almostReady = payload.almostReady,
                    trendingSuggestions = trendingSuggestions
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
                    ownerUserId = userId,
                    loadingSessions = false,
                    sessions = parseSessions(result.data),
                    errorMessage = result.message.takeIf { !result.success }
                )
            }
        }
    }

    fun prefetchChatHistorySources(userId: Int) {
        val state = _chatState.value
        if (state.loadingSessions || state.ownerUserId == userId && state.sessions.isNotEmpty()) return
        loadSessions(userId)
    }

    fun openSession(userId: Int, sessionId: Int) {
        viewModelScope.launch {
            _chatState.update {
                it.copy(
                    ownerUserId = userId,
                    historyMode = true,
                    loadingTimeline = true,
                    errorMessage = null,
                    pendingMealPolicyPrompt = null,
                    pendingPrimaryRecipeSwitch = null
                )
            }
            val result = AppFlowApiClient.getSessionDetail(sessionId = sessionId, userId = userId)
            val parsedSession = parsePrimarySession(result.data)
                ?: ChatSession(chatSessionId = sessionId, userId = userId, title = "Phiên Bepes")
            val mealSession = parseMealSession(result.data, parsedSession)
            val mealItems = parseMealItems(result.data, parsedSession)
            val hydratedSession = parsedSession.copy(
                activeRecipeId = mealSession.activeRecipeId ?: parsedSession.activeRecipeId,
                needsSelection = mealSession.needsSelection,
                recipes = mealItems,
                isMealSession = true
            )
            val timeline = deduplicateAndSort(parseMessages(result.data, parsedSession.chatSessionId))

            _chatState.update {
                it.copy(
                    ownerUserId = userId,
                    historyMode = true,
                    restoreCompleted = true,
                    loadingTimeline = false,
                    currentSessionId = hydratedSession.chatSessionId,
                    currentSession = hydratedSession,
                    mealSession = mealSession,
                    mealItems = mealItems,
                    timeline = timeline,
                    hasMore = false,
                    nextBeforeMessageId = null,
                    lastRequestedBeforeMessageId = null,
                    noProgressLoadCount = 0,
                    pendingMealPolicyPrompt = null,
                    pendingPrimaryRecipeSwitch = null,
                    errorMessage = result.message.takeIf { !result.success }
                )
            }
        }
    }

    fun bootstrapUnifiedTimeline(userId: Int, activeRecipeId: Int? = null) {
        val cached = _chatState.value
        if (cached.isBootstrapping) return

        viewModelScope.launch {
            _chatState.update {
                it.copy(
                    ownerUserId = userId,
                    historyMode = false,
                    isBootstrapping = true,
                    restoreCompleted = false,
                    currentSessionId = null,
                    currentSession = null,
                    mealSession = MealSessionUiState(),
                    mealItems = emptyList(),
                    timeline = emptyList(),
                    hasMore = true,
                    nextBeforeMessageId = null,
                    lastRequestedBeforeMessageId = null,
                    noProgressLoadCount = 0,
                    pendingMealPolicyPrompt = null,
                    pendingPrimaryRecipeSwitch = null,
                    errorMessage = null
                )
            }

            refreshHomeContext(userId)

            val sessionsResult = AppFlowApiClient.getSessions(userId = userId)
            val sessions = parseSessions(sessionsResult.data)
            val restoreSessionId = sessions
                .sortedWith(
                    compareByDescending<ChatSession> { it.updatedAt ?: it.createdAt ?: "" }
                        .thenByDescending { it.chatSessionId ?: 0 }
                )
                .firstOrNull()
                ?.chatSessionId

            var restoredSession: ChatSession? = null
            var restoredMealSession = MealSessionUiState()
            var restoredMealItems = emptyList<MealRecipeState>()
            var restoredTimeline = emptyList<ChatUiMessage>()

            if (restoreSessionId != null) {
                val detailResult = AppFlowApiClient.getSessionDetail(sessionId = restoreSessionId, userId = userId)
                restoredSession = parsePrimarySession(detailResult.data) ?: sessions.firstOrNull { it.chatSessionId == restoreSessionId }
                restoredMealSession = parseMealSession(detailResult.data, restoredSession)
                restoredMealItems = parseMealItems(detailResult.data, restoredSession)
                restoredTimeline = deduplicateAndSort(parseMessages(detailResult.data, restoreSessionId))
            }

            val timelineResult = AppFlowApiClient.getTimeline(
                userId = userId,
                limit = DEFAULT_CHAT_LIMIT,
                beforeMessageId = null
            )
            val latestTimeline = deduplicateAndSort(parseMessages(timelineResult.data, restoredSession?.chatSessionId))
            val mergedTimeline = deduplicateAndSort(restoredTimeline + latestTimeline)
            val resolvedSession = restoredSession ?: parsePrimarySession(timelineResult.data)
            val resolvedMealItems = restoredMealItems.ifEmpty { resolvedSession?.recipes.orEmpty() }
            val hydratedResolvedSession = resolvedSession?.copy(
                activeRecipeId = restoredMealSession.activeRecipeId ?: resolvedSession.activeRecipeId,
                needsSelection = restoredMealSession.needsSelection || resolvedSession.needsSelection,
                recipes = resolvedMealItems,
                isMealSession = true
            )

            _chatState.update {
                it.copy(
                    ownerUserId = userId,
                    historyMode = false,
                    isBootstrapping = false,
                    restoreCompleted = true,
                    loadingSessions = false,
                    currentSessionId = hydratedResolvedSession?.chatSessionId ?: restoredMealSession.chatSessionId,
                    currentSession = hydratedResolvedSession,
                    mealSession = if (hydratedResolvedSession != null) {
                        restoredMealSession.copy(
                            chatSessionId = hydratedResolvedSession.chatSessionId,
                            activeRecipeId = hydratedResolvedSession.activeRecipeId ?: restoredMealSession.activeRecipeId,
                            needsSelection = hydratedResolvedSession.needsSelection || restoredMealSession.needsSelection,
                            uiClosed = hydratedResolvedSession.uiClosed || restoredMealSession.uiClosed
                        )
                    } else {
                        restoredMealSession
                    },
                    mealItems = resolvedMealItems,
                    sessions = sessions,
                    timeline = mergedTimeline,
                    hasMore = parseHasMore(timelineResult.data, latestTimeline),
                    nextBeforeMessageId = findNextBeforeMessageId(timelineResult.data, mergedTimeline),
                    lastRequestedBeforeMessageId = null,
                    noProgressLoadCount = 0,
                    pendingPreviousRecipe = if (
                        timelineResult.code == ChatBusinessCode.PENDING_PREVIOUS_RECIPE_COMPLETION
                    ) {
                        parsePendingPreviousRecipe(timelineResult.data)
                    } else {
                        null
                    },
                    pendingMealPolicyPrompt = null,
                    pendingPrimaryRecipeSwitch = null,
                    errorMessage = listOfNotNull(
                        sessionsResult.message.takeIf { !sessionsResult.success },
                        timelineResult.message.takeIf { !timelineResult.success && latestTimeline.isEmpty() }
                    ).firstOrNull()
                )
            }

            activeRecipeId?.takeIf { it > 0 }?.let { attachActiveRecipe(userId, it) }
        }
    }

    fun createSessionIfNeeded(userId: Int, activeRecipeId: Int? = null) {
        val recipeId = activeRecipeId?.takeIf { it > 0 } ?: return
        val currentSessionId = _chatState.value.currentSessionId
        if (currentSessionId != null) return

        viewModelScope.launch {
            _chatState.update { it.copy(mealSyncing = true, errorMessage = null) }
            val result = AppFlowApiClient.createMealSession(
                MealSessionCreateRequest(
                    title = "Bepes",
                    recipeIds = listOf(recipeId)
                )
            )
            applyLiveResult(userId = userId, result = result)
            if (result.success) {
                refreshLatestTimelinePage(userId)
            }
            _chatState.update { it.copy(mealSyncing = false) }
        }
    }

    fun attachActiveRecipe(userId: Int, recipeId: Int?) {
        recipeId?.takeIf { it > 0 }?.let { selectRecipeForCurrentSession(userId, it) }
    }

    fun selectRecipeForCurrentSession(userId: Int, recipeId: Int) {
        if (recipeId <= 0) return

        viewModelScope.launch {
            val state = _chatState.value
            val currentSessionId = state.currentSessionId

            if (currentSessionId == null) {
                createSessionIfNeeded(userId, recipeId)
                return@launch
            }

            val existingItems = state.mealItems.ifEmpty { state.currentSession?.recipes.orEmpty() }
            if (existingItems.none { it.recipeId == recipeId }) {
                val nextItems = (existingItems + MealRecipeState(
                    recipeId = recipeId,
                    status = MealRecipeStatus.PENDING,
                    isPrimary = true
                )).mapIndexed { index, item ->
                    item.copy(
                        sortOrder = index + 1,
                        isPrimary = item.recipeId == recipeId
                    )
                }
                syncMealRecipeList(userId, nextItems) {
                    setMealPrimaryRecipe(userId, recipeId)
                }
            } else {
                setMealPrimaryRecipe(userId, recipeId)
            }
        }
    }

    fun removeRecipeFromCurrentSession(userId: Int, recipeId: Int) {
        if (recipeId <= 0) return
        val state = _chatState.value
        val remaining = state.mealItems.filterNot { it.recipeId == recipeId }
            .mapIndexed { index, item -> item.copy(sortOrder = index + 1) }
        syncMealRecipeList(userId, remaining)
    }

    fun moveMealRecipe(userId: Int, recipeId: Int, direction: Int) {
        val currentItems = _chatState.value.mealItems.toMutableList()
        val index = currentItems.indexOfFirst { it.recipeId == recipeId }
        val nextIndex = index + direction
        if (index < 0 || nextIndex !in currentItems.indices) return
        val item = currentItems.removeAt(index)
        currentItems.add(nextIndex, item)
        syncMealRecipeList(
            userId = userId,
            nextItems = currentItems.mapIndexed { order, value ->
                value.copy(sortOrder = order + 1)
            }
        )
    }

    fun setMealPrimaryRecipe(userId: Int, recipeId: Int) {
        if (recipeId <= 0) return
        val previousState = _chatState.value
        val previousSession = previousState.currentSession
        val previousMealSession = previousState.mealSession
        val previousItems = previousState.mealItems

        _chatState.update {
            it.copy(
                mealSyncing = true,
                currentSession = it.currentSession?.copy(activeRecipeId = recipeId),
                mealSession = it.mealSession.copy(activeRecipeId = recipeId),
                mealItems = it.mealItems.map { item ->
                    item.copy(isPrimary = item.recipeId == recipeId)
                }
            )
        }

        viewModelScope.launch {
            val currentSessionId = previousState.currentSessionId ?: return@launch
            val result = AppFlowApiClient.updateMealPrimaryRecipe(
                MealPrimaryRecipeRequest(
                    chatSessionId = currentSessionId,
                    recipeId = recipeId
                )
            )
            if (!result.success) {
                _chatState.update {
                    it.copy(
                        mealSyncing = false,
                        currentSession = previousSession,
                        mealSession = previousMealSession,
                        mealItems = previousItems,
                        errorMessage = result.message.takeIf { !result.success }
                    )
                }
                return@launch
            }
            applyLiveResult(userId = userId, result = result)
            _chatState.update { it.copy(mealSyncing = false) }
        }
    }

    fun updateMealRecipeStatus(
        userId: Int,
        recipeId: Int,
        status: String,
        confirmSwitchPrimary: Boolean = false,
        nextPrimaryRecipeId: Int? = null
    ) {
        if (status !in MealRecipeStatus.all) return
        val currentState = _chatState.value
        val currentSessionId = currentState.currentSessionId ?: return
        val previousItems = currentState.mealItems
        val previousMealSession = currentState.mealSession
        val previousSession = currentState.currentSession

        _chatState.update {
            it.copy(
                sending = true,
                mealSyncing = true,
                errorMessage = null,
                mealItems = it.mealItems.map { item ->
                    when {
                        item.recipeId == recipeId -> item.copy(status = status)
                        nextPrimaryRecipeId != null -> item.copy(isPrimary = item.recipeId == nextPrimaryRecipeId)
                        else -> item
                    }
                }
            )
        }

        viewModelScope.launch {
            val request = MealRecipeStatusRequest(
                chatSessionId = currentSessionId,
                recipeId = recipeId,
                status = status,
                confirmSwitchPrimary = confirmSwitchPrimary.takeIf { it },
                nextPrimaryRecipeId = nextPrimaryRecipeId,
                confirmFieldName = currentState.pendingPrimaryRecipeSwitch?.confirmField,
                chooseFieldName = currentState.pendingPrimaryRecipeSwitch?.chooseField
            )
            val result = AppFlowApiClient.updateMealRecipeStatus(request)
            if (result.code == ChatBusinessCode.PENDING_PRIMARY_RECIPE_SWITCH_CONFIRMATION || !result.success) {
                _chatState.update {
                    it.copy(
                        currentSession = previousSession,
                        mealSession = previousMealSession,
                        mealItems = previousItems
                    )
                }
            }
            applyLiveResult(userId = userId, result = result)
            _chatState.update { it.copy(sending = false, mealSyncing = false) }
        }
    }

    fun resolveMealPolicyPrompt(
        userId: Int,
        action: String,
        nextPrimaryRecipeId: Int? = null
    ) {
        val pending = _chatState.value.pendingMealPolicyPrompt ?: return
        if (action !in MealCompletionAction.all) return

        if (action == MealCompletionAction.ADD_MORE_RECIPES) {
            markPromptResolved(pending, action)
            return
        }

        updatePromptStatus(
            prompt = pending,
            status = PromptStatus.LOADING,
            selectedActionId = action,
            errorMessage = null
        )

        viewModelScope.launch {
            val result = AppFlowApiClient.resolveMealCompletion(
                MealCompletionResolveRequest(
                    chatSessionId = pending.chatSessionId,
                    action = action,
                    pendingUserMessage = pending.pendingUserMessage,
                    nextPrimaryRecipeId = nextPrimaryRecipeId
                )
            )

            if (!result.success && isStalePromptResult(result)) {
                val detailResult = AppFlowApiClient.getSessionDetail(
                    sessionId = pending.chatSessionId,
                    userId = userId
                )
                val refreshedSession = parsePrimarySession(detailResult.data)
                val refreshedMealSession = parseMealSession(detailResult.data, refreshedSession)
                val refreshedMealItems = parseMealItems(detailResult.data, refreshedSession)
                val refreshedMessages = deduplicateAndSort(parseMessages(detailResult.data, pending.chatSessionId))
                _chatState.update {
                    it.copy(
                        currentSessionId = refreshedSession?.chatSessionId ?: it.currentSessionId,
                        currentSession = refreshedSession ?: it.currentSession,
                        mealSession = refreshedMealSession,
                        mealItems = refreshedMealItems,
                        timeline = if (refreshedMessages.isNotEmpty()) refreshedMessages else it.timeline.filterNot { message ->
                            message.localId == pending.messageLocalId
                        },
                        pendingMealPolicyPrompt = null,
                        errorMessage = "Prompt không còn hợp lệ, đã đồng bộ lại."
                    )
                }
                return@launch
            }

            applyLiveResult(
                userId = userId,
                result = result,
                resolvedPrompt = pending,
                resolvedPromptActionId = action
            )
        }
    }

    fun completeCurrentSession(
        userId: Int,
        completionType: String = MealCompletionType.COMPLETED,
        markRemainingStatus: String? = null,
        note: String? = null
    ) {
        val currentSessionId = _chatState.value.currentSessionId ?: return
        viewModelScope.launch {
            _chatState.update { it.copy(sending = true, mealSyncing = true, errorMessage = null) }
            val request = MealCompleteRequest(
                chatSessionId = currentSessionId,
                completionType = completionType,
                markRemainingStatus = markRemainingStatus,
                note = note
            )
            val result = AppFlowApiClient.completeMealSession(request)
            applyLiveResult(
                userId = userId,
                result = result,
                completionRequest = request
            )
            _chatState.update { it.copy(sending = false, mealSyncing = false) }
        }
    }

    fun sendMessage(userId: Int, text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        val state = _chatState.value
        if (state.sending) return
        if (state.mealSession.uiClosed || state.currentSession?.uiClosed == true) {
            _chatState.update { it.copy(errorMessage = "Phiên nấu này đã hoàn tất, không thể gửi thêm tin nhắn.") }
            return
        }
        if (state.pendingMealPolicyPrompt?.status in listOf(PromptStatus.PENDING, PromptStatus.LOADING)) {
            _chatState.update { it.copy(errorMessage = "Hãy xử lý prompt hiện tại trước khi tiếp tục trò chuyện.") }
            return
        }

        val optimisticLocalId = UUID.randomUUID().toString()
        val optimisticUserMessage = ChatUiMessage(
            localId = optimisticLocalId,
            tempId = optimisticLocalId,
            chatSessionId = state.currentSessionId,
            role = ChatRole.USER,
            text = trimmed,
            createdAt = nowIsoString(),
            isPending = true
        )

        _chatState.update {
            it.copy(
                sending = true,
                errorMessage = null,
                timeline = deduplicateAndSort(it.timeline + optimisticUserMessage),
                pendingMealPolicyPrompt = null,
                pendingPrimaryRecipeSwitch = null
            )
        }

        viewModelScope.launch {
            val request = ChatSendMessageRequest(
                message = buildOutgoingMessage(trimmed),
                stream = false,
                chatSessionId = state.currentSessionId,
                useUnifiedSession = true
            )
            val result = AppFlowApiClient.sendMealMessage(request)
            if (result.httpStatus == 503 && result.code == ChatBusinessCode.AI_SERVER_BUSY) {
                markMessageFailed(
                    localId = optimisticLocalId,
                    errorText = result.message ?: "AI server đang bận, hãy thử lại sau.",
                    retryable = true,
                    retryAfterMs = DEFAULT_RETRY_AFTER_MS
                )
                _chatState.update { it.copy(sending = false) }
                return@launch
            }

            if (!result.success) {
                markMessageFailed(
                    localId = optimisticLocalId,
                    errorText = result.message ?: "Không thể gửi tin nhắn lúc này.",
                    retryable = false,
                    retryAfterMs = null
                )
                _chatState.update { it.copy(sending = false) }
                return@launch
            }

            applyLiveResult(
                userId = userId,
                result = result,
                optimisticLocalId = optimisticLocalId
            )
            _chatState.update { it.copy(sending = false) }
        }
    }

    fun retryMessage(userId: Int, localId: String) {
        val message = _chatState.value.timeline.firstOrNull { it.localId == localId && it.isFailed } ?: return
        val retryAvailableAt = message.retryAvailableAt ?: 0L
        if (retryAvailableAt > System.currentTimeMillis()) return
        sendMessage(userId, message.text)
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

            val parsedSession = parsePrimarySession(result.data)
            val parsedMessages = deduplicateAndSort(parseMessages(result.data, parsedSession?.chatSessionId))
            val mealSession = parseMealSession(result.data, parsedSession)
            val mealItems = parseMealItems(result.data, parsedSession)

            _chatState.update {
                it.copy(
                    ownerUserId = userId,
                    sending = false,
                    historyMode = false,
                    currentSessionId = parsedSession?.chatSessionId ?: it.currentSessionId,
                    currentSession = parsedSession ?: it.currentSession,
                    mealSession = mealSession,
                    mealItems = mealItems,
                    timeline = if (parsedMessages.isNotEmpty()) parsedMessages else it.timeline,
                    pendingPreviousRecipe = null,
                    errorMessage = result.message.takeIf { !result.success }
                )
            }
        }
    }

    fun clearPendingPreviousRecipe() {
        _chatState.update { it.copy(pendingPreviousRecipe = null) }
    }

    fun clearMealDialogs() {
        val prompt = _chatState.value.pendingMealPolicyPrompt
        if (prompt != null) {
            updatePromptStatus(prompt, status = PromptStatus.RESOLVED, selectedActionId = MealCompletionAction.KEEP_SESSION_OPEN)
        } else {
            _chatState.update {
                it.copy(
                    pendingMealPolicyPrompt = null,
                    pendingPrimaryRecipeSwitch = null
                )
            }
        }
    }

    fun loadMoreTimeline(userId: Int) {
        val current = _chatState.value
        if (current.historyMode) return
        if (current.ownerUserId != null && current.ownerUserId != userId) return
        val beforeMessageId = current.nextBeforeMessageId ?: return
        if (!current.hasMore || current.loadingTimeline) return

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

            val olderMessages = deduplicateAndSort(parseMessages(result.data, current.currentSessionId))
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
                val noProgressCount = if (addedCount > 0 || cursorAdvanced) 0 else latest.noProgressLoadCount + 1

                latest.copy(
                    loadingTimeline = false,
                    timeline = mergedTimeline,
                    nextBeforeMessageId = nextBefore,
                    hasMore = hasMoreFromServer && nextBefore != null && noProgressCount < MAX_NO_PROGRESS_ATTEMPTS,
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
            val removedCurrentSession = _chatState.value.currentSessionId == chatSessionId
            _chatState.update {
                it.copy(
                    sessions = it.sessions.filter { session -> session.chatSessionId != chatSessionId },
                    currentSessionId = if (removedCurrentSession) null else it.currentSessionId,
                    currentSession = if (removedCurrentSession) null else it.currentSession,
                    mealSession = if (removedCurrentSession) MealSessionUiState() else it.mealSession,
                    mealItems = if (removedCurrentSession) emptyList() else it.mealItems,
                    timeline = if (removedCurrentSession) emptyList() else it.timeline,
                    nextBeforeMessageId = if (removedCurrentSession) null else it.nextBeforeMessageId,
                    hasMore = if (removedCurrentSession) false else it.hasMore,
                    pendingMealPolicyPrompt = if (removedCurrentSession) null else it.pendingMealPolicyPrompt,
                    pendingPrimaryRecipeSwitch = if (removedCurrentSession) null else it.pendingPrimaryRecipeSwitch,
                    errorMessage = result.message.takeIf { !result.success }
                )
            }
        }
    }

    fun clearChatError() {
        _chatState.update { it.copy(errorMessage = null) }
    }

    private fun syncMealRecipeList(
        userId: Int,
        nextItems: List<MealRecipeState>,
        onSuccess: (() -> Unit)? = null
    ) {
        val previousState = _chatState.value
        val currentSessionId = previousState.currentSessionId

        _chatState.update {
            it.copy(
                mealSyncing = true,
                errorMessage = null,
                mealItems = nextItems,
                currentSession = it.currentSession?.copy(recipes = nextItems)
            )
        }

        viewModelScope.launch {
            val result = if (currentSessionId == null) {
                AppFlowApiClient.createMealSession(
                    MealSessionCreateRequest(
                        title = "Bepes",
                        recipeIds = nextItems.map { item -> item.recipeId }
                    )
                )
            } else {
                AppFlowApiClient.replaceMealRecipes(
                    MealSessionRecipesReplaceRequest(
                        chatSessionId = currentSessionId,
                        recipes = nextItems.mapIndexed { index, item ->
                            MealSessionRecipeInput(
                                recipeId = item.recipeId,
                                sortOrder = index + 1,
                                status = item.status,
                                note = item.note,
                                servingsOverride = item.servingsOverride
                            )
                        }
                    )
                )
            }

            if (!result.success) {
                _chatState.update {
                    it.copy(
                        mealSyncing = false,
                        currentSession = previousState.currentSession,
                        mealSession = previousState.mealSession,
                        mealItems = previousState.mealItems,
                        errorMessage = result.message.takeIf { !result.success }
                    )
                }
                return@launch
            }

            applyLiveResult(userId = userId, result = result)
            if (currentSessionId == null) {
                refreshLatestTimelinePage(userId)
            }
            _chatState.update { it.copy(mealSyncing = false) }
            onSuccess?.invoke()
        }
    }

    private suspend fun refreshLatestTimelinePage(userId: Int) {
        val result = AppFlowApiClient.getTimeline(userId = userId, limit = DEFAULT_CHAT_LIMIT)
        val latestTimeline = deduplicateAndSort(parseMessages(result.data, _chatState.value.currentSessionId))
        _chatState.update {
            it.copy(
                timeline = deduplicateAndSort(it.timeline + latestTimeline),
                hasMore = parseHasMore(result.data, latestTimeline),
                nextBeforeMessageId = findNextBeforeMessageId(result.data, deduplicateAndSort(it.timeline + latestTimeline))
            )
        }
    }

    private fun applyLiveResult(
        userId: Int,
        result: RawApiResult,
        optimisticLocalId: String? = null,
        resolvedPrompt: PendingMealPolicyPrompt? = null,
        resolvedPromptActionId: String? = null,
        completionRequest: MealCompleteRequest? = null
    ) {
        val parsedSession = parsePrimarySession(result.data)
        val parsedMessages = deduplicateAndSort(parseMessages(result.data, parsedSession?.chatSessionId ?: _chatState.value.currentSessionId))
        val parsedMealSession = parseMealSession(result.data, parsedSession)
        val parsedMealItems = parseMealItems(result.data, parsedSession)
        val pendingPrompt = parsePendingMealPolicyPrompt(result.code, result.data, parsedSession)
        val pendingSwitch = if (result.code == ChatBusinessCode.PENDING_PRIMARY_RECIPE_SWITCH_CONFIRMATION) {
            parsePendingPrimarySwitch(result.data)
        } else {
            null
        }
        val pendingPrevious = if (result.code == ChatBusinessCode.PENDING_PREVIOUS_RECIPE_COMPLETION) {
            parsePendingPreviousRecipe(result.data)
        } else {
            null
        }

        _chatState.update { state ->
            var timeline = state.timeline
            if (optimisticLocalId != null) {
                val echoedUserMessage = parsedMessages.any { message ->
                    message.role == ChatRole.USER
                }
                timeline = if (echoedUserMessage) {
                    timeline.filterNot { it.localId == optimisticLocalId }
                } else {
                    timeline.map { message ->
                        if (message.localId == optimisticLocalId) {
                            message.copy(isPending = false, isFailed = false, retryable = false, errorText = null)
                        } else {
                            message
                        }
                    }
                }
            }

            if (resolvedPrompt != null) {
                timeline = timeline.map { message ->
                    if (message.localId == resolvedPrompt.messageLocalId) {
                        message.copy(
                            promptStatus = if (result.success) PromptStatus.RESOLVED else PromptStatus.ERROR,
                            selectedActionId = resolvedPromptActionId,
                            errorText = result.message.takeIf { !result.success }
                        )
                    } else {
                        message
                    }
                }
            }

            if (parsedMessages.isNotEmpty()) {
                timeline = deduplicateAndSort(timeline + parsedMessages)
            }

            val promptForState = pendingPrompt?.let { prompt ->
                val promptMessage = buildPromptMessage(prompt)
                timeline = deduplicateAndSort(timeline.filterNot { it.localId == prompt.messageLocalId } + promptMessage)
                prompt
            }

            var nextSession = parsedSession ?: state.currentSession
            val hasMealPayload = hasSessionMealPayload(result.data)
            var nextMealSession = if (parsedSession != null || hasMealPayload || parsedMealItems.isNotEmpty()) {
                parsedMealSession
            } else {
                state.mealSession
            }
            var nextMealItems = if (hasMealPayload) parsedMealItems else if (parsedMealItems.isNotEmpty()) parsedMealItems else state.mealItems
            if (nextSession != null) {
                nextSession = nextSession.copy(
                    activeRecipeId = nextMealSession.activeRecipeId ?: nextSession.activeRecipeId,
                    needsSelection = nextMealSession.needsSelection || nextSession.needsSelection,
                    recipes = nextMealItems,
                    isMealSession = true
                )
            }

            if (completionRequest != null && result.success) {
                val closedSessionId = nextSession?.chatSessionId ?: state.currentSessionId
                nextSession = null
                nextMealSession = MealSessionUiState()
                nextMealItems = emptyList()
                if (parsedMessages.isEmpty()) {
                    timeline = deduplicateAndSort(
                        timeline + ChatUiMessage(
                            localId = UUID.randomUUID().toString(),
                            chatSessionId = closedSessionId,
                            role = ChatRole.ASSISTANT,
                            text = "Bepes đã đóng phiên nấu này.",
                            createdAt = nowIsoString()
                        )
                    )
                }
            }

            val effectivePrompt = when {
                promptForState != null -> promptForState
                completionRequest != null && result.success -> null
                resolvedPrompt != null && result.success -> null
                resolvedPrompt != null && !result.success -> state.pendingMealPolicyPrompt?.copy(
                    status = PromptStatus.ERROR,
                    selectedActionId = resolvedPromptActionId,
                    errorMessage = result.message
                )
                else -> state.pendingMealPolicyPrompt
            }

            state.copy(
                ownerUserId = userId,
                historyMode = false,
                isBootstrapping = false,
                restoreCompleted = true,
                currentSessionId = nextSession?.chatSessionId ?: nextMealSession.chatSessionId,
                currentSession = nextSession,
                mealSession = nextMealSession.copy(
                    chatSessionId = nextSession?.chatSessionId ?: nextMealSession.chatSessionId
                ),
                mealItems = nextMealItems,
                timeline = timeline,
                pendingPreviousRecipe = pendingPrevious,
                pendingMealPolicyPrompt = effectivePrompt,
                pendingPrimaryRecipeSwitch = pendingSwitch,
                errorMessage = result.message.takeIf { !result.success && result.code != ChatBusinessCode.AI_SERVER_BUSY }
            )
        }
    }

    private fun parseSessions(data: JsonElement?): List<ChatSession> {
        if (data == null || data.isJsonNull) return emptyList()
        return when {
            data.isJsonArray -> parseSessionList(data.asJsonArray)
            data.isJsonObject -> {
                val obj = data.asJsonObject
                when {
                    obj.get("sessions")?.isJsonArray == true -> parseSessionList(obj.get("sessions").asJsonArray)
                    obj.get("items")?.isJsonArray == true -> parseSessionList(obj.get("items").asJsonArray)
                    obj.get("data")?.isJsonArray == true -> parseSessionList(obj.get("data").asJsonArray)
                    else -> listOfNotNull(parseSession(obj))
                }
            }
            else -> emptyList()
        }
    }

    private fun parsePrimarySession(data: JsonElement?): ChatSession? {
        val obj = data.asObjectOrNull() ?: return null
        val prioritized = listOf("session", "latestSession", "newSession", "mealSession", "currentSession")
        prioritized.forEach { key ->
            val candidate = obj.get(key)
            if (candidate != null && candidate.isJsonObject) {
                parseSession(candidate.asJsonObject)?.let { return it }
            }
        }
        return parseSession(obj)
    }

    private fun parseSession(data: JsonElement?): ChatSession? {
        val obj = data.asObjectOrNull() ?: return null
        val sessionId = obj.intOrNull("chatSessionId", "sessionId", "id") ?: return null
        val focus = obj.get("focus").asObjectOrNull()
        return ChatSession(
            chatSessionId = sessionId,
            userId = obj.intOrNull("userId"),
            title = obj.stringOrNull("title"),
            activeRecipeId = obj.intOrNull("activeRecipeId")
                ?: focus?.intOrNull("activeRecipeId", "primaryRecipeId"),
            recipes = emptyList(),
            needsSelection = focus?.booleanOrNull("needsSelection")
                ?: obj.booleanOrNull("needsSelection")
                ?: false,
            isMealSession = obj.booleanOrNull("isMealSession") ?: true,
            uiClosed = obj.booleanOrNull("uiClosed", "isClosed", "closed")
                ?: (obj.stringOrNull("status")?.lowercase() == "closed"),
            createdAt = obj.stringOrNull("createdAt", "created_at"),
            updatedAt = obj.stringOrNull("updatedAt", "updated_at")
        )
    }

    private fun parseSessionList(data: JsonArray): List<ChatSession> {
        return data.mapNotNull { element -> parseSession(element) }
    }

    private fun parseMealSession(data: JsonElement?, fallbackSession: ChatSession?): MealSessionUiState {
        val root = data.asObjectOrNull()
        val focusObj = root?.get("focus").asObjectOrNull()

        return MealSessionUiState(
            chatSessionId = fallbackSession?.chatSessionId,
            activeRecipeId = focusObj?.intOrNull("activeRecipeId", "primaryRecipeId") ?: fallbackSession?.activeRecipeId,
            needsSelection = focusObj?.booleanOrNull("needsSelection") ?: false,
            uiClosed = fallbackSession?.uiClosed ?: false
        )
    }

    private fun parseMealItems(data: JsonElement?, fallbackSession: ChatSession?): List<MealRecipeState> {
        val root = data.asObjectOrNull()
        val activeRecipeId = root?.get("focus")?.asObjectOrNull()?.intOrNull("activeRecipeId")
            ?: fallbackSession?.activeRecipeId
        val mealObj = root?.get("meal").asObjectOrNull()
        return if (mealObj?.get("items")?.isJsonArray == true) {
            parseMealRecipeList(
                data = mealObj.get("items"),
                activeRecipeId = activeRecipeId
            )
        } else {
            emptyList()
        }
    }

    private fun hasSessionMealPayload(data: JsonElement?): Boolean {
        val root = data.asObjectOrNull() ?: return false
        val mealObj = root.get("meal").asObjectOrNull() ?: return false
        return mealObj.get("items")?.isJsonArray == true
    }

    private fun parseMealRecipeList(
        data: JsonElement?,
        activeRecipeId: Int? = null
    ): List<MealRecipeState> {
        if (data == null || data.isJsonNull || !data.isJsonArray) return emptyList()
        return data.asJsonArray.mapIndexedNotNull { index, element ->
            val obj = element.asObjectOrNull() ?: return@mapIndexedNotNull null
            val recipeObj = obj.get("recipe").asObjectOrNull()
            val recipeId = obj.intOrNull("recipeId", "id")
                ?: recipeObj?.intOrNull("recipeId", "id")
                ?: return@mapIndexedNotNull null
            MealRecipeState(
                recipeId = recipeId,
                chatSessionRecipeId = obj.intOrNull("chatSessionRecipeId"),
                recipeName = recipeObj?.stringOrNull("recipeName", "title", "name")
                    ?: obj.stringOrNull("recipeName", "title", "name"),
                image = recipeObj?.stringOrNull("image") ?: obj.stringOrNull("image"),
                cookingTime = recipeObj?.stringOrNull("cookingTime") ?: obj.stringOrNull("cookingTime"),
                ration = recipeObj?.intOrNull("ration") ?: obj.intOrNull("ration"),
                status = obj.stringOrNull("status") ?: MealRecipeStatus.PENDING,
                isPrimary = recipeId == activeRecipeId,
                sortOrder = obj.intOrNull("sortOrder") ?: index + 1,
                note = obj.stringOrNull("note"),
                servingsOverride = obj.intOrNull("servingsOverride"),
                selectedAt = obj.stringOrNull("selectedAt"),
                resolvedAt = obj.stringOrNull("resolvedAt")
            )
        }
    }

    private fun parseMessages(data: JsonElement?, fallbackSessionId: Int? = null): List<ChatUiMessage> {
        if (data == null || data.isJsonNull) return emptyList()
        val messages = mutableListOf<ChatUiMessage>()
        when {
            data.isJsonArray -> messages += parseChatMessageList(data, fallbackSessionId)
            data.isJsonObject -> {
                val obj = data.asJsonObject
                messages += parseChatMessageList(obj.get("messages"), fallbackSessionId)
                messages += parseChatMessageList(obj.get("timeline"), fallbackSessionId)
                messages += parseChatMessageList(obj.get("items"), fallbackSessionId)
                parseSingleMessage(obj.get("assistantMessage"), fallbackSessionId)?.let { messages += it }
            }
        }
        return deduplicateAndSort(messages)
    }

    private fun parseChatMessageList(data: JsonElement?, fallbackSessionId: Int?): List<ChatUiMessage> {
        if (data == null || data.isJsonNull || !data.isJsonArray) return emptyList()
        return runCatching {
            gson.fromJson<List<ChatMessage>>(data, object : TypeToken<List<ChatMessage>>() {}.type)
        }.getOrElse { emptyList() }.mapNotNull { message ->
            val normalizedText = message.content ?: message.message ?: message.text
            if (normalizedText.isNullOrBlank()) {
                null
            } else {
                ChatUiMessage(
                    localId = UUID.randomUUID().toString(),
                    messageId = message.messageId ?: message.chatMessageId,
                    chatSessionId = message.chatSessionId ?: fallbackSessionId,
                    sessionTitle = message.sessionTitle,
                    activeRecipeId = message.activeRecipeId,
                    isSessionStart = message.isSessionStart == true,
                    role = ChatRole.normalize(message.role),
                    text = normalizedText,
                    createdAt = message.createdAt,
                    kind = ChatMessageKind.TEXT
                )
            }
        }
    }

    private fun parseSingleMessage(data: JsonElement?, fallbackSessionId: Int?): ChatUiMessage? {
        if (data == null || data.isJsonNull) return null
        return when {
            data.isJsonObject -> {
                runCatching { gson.fromJson(data, ChatMessage::class.java) }.getOrNull()?.let { message ->
                    val normalizedText = message.content ?: message.message ?: message.text ?: return null
                    ChatUiMessage(
                        localId = UUID.randomUUID().toString(),
                        messageId = message.messageId ?: message.chatMessageId,
                        chatSessionId = message.chatSessionId ?: fallbackSessionId,
                        sessionTitle = message.sessionTitle,
                        activeRecipeId = message.activeRecipeId,
                        role = ChatRole.normalize(message.role),
                        text = normalizedText,
                        createdAt = message.createdAt
                    )
                }
            }
            data.isJsonPrimitive -> ChatUiMessage(
                localId = UUID.randomUUID().toString(),
                chatSessionId = fallbackSessionId,
                role = ChatRole.ASSISTANT,
                text = data.asString,
                createdAt = nowIsoString()
            )
            else -> null
        }
    }

    private fun parsePendingPreviousRecipe(data: JsonElement?): PendingPreviousRecipePayload? {
        val obj = data.asObjectOrNull() ?: return null
        val payloadObj = when {
            obj.get("pendingPreviousRecipe")?.isJsonObject == true -> obj.get("pendingPreviousRecipe").asJsonObject
            else -> obj
        }

        val previousSessionId = payloadObj.intOrNull("previousSessionId") ?: return null
        return PendingPreviousRecipePayload(
            previousSessionId = previousSessionId,
            recipeId = payloadObj.intOrNull("recipeId"),
            recipeName = payloadObj.stringOrNull("recipeName"),
            pendingUserMessage = payloadObj.stringOrNull("pendingUserMessage"),
            reminderMessage = payloadObj.stringOrNull("reminderMessage"),
            actions = parseActions(payloadObj.get("actions"))
        )
    }

    private fun parsePendingMealPolicyPrompt(
        code: String?,
        data: JsonElement?,
        parsedSession: ChatSession?
    ): PendingMealPolicyPrompt? {
        val root = data.asObjectOrNull() ?: return null
        return when (code) {
            ChatBusinessCode.PENDING_MEAL_V2_COMPLETION_CHECK -> {
                val payload = root.get("completionCheck").asObjectOrNull() ?: root
                val sessionId = payload.intOrNull("chatSessionId") ?: parsedSession?.chatSessionId ?: return null
                PendingMealPolicyPrompt(
                    chatSessionId = sessionId,
                    promptCode = code,
                    actions = parseActions(payload.get("actions")).ifEmpty {
                        listOf(
                            PendingResolveAction(MealCompletionAction.MARK_DONE, "Đã xong món"),
                            PendingResolveAction(MealCompletionAction.MARK_SKIPPED, "Bỏ qua món"),
                            PendingResolveAction(MealCompletionAction.CONTINUE_CURRENT, "Chưa xong, tiếp tục")
                        )
                    },
                    pendingUserMessage = payload.stringOrNull("pendingUserMessage"),
                    reminderMessage = payload.stringOrNull("reminderMessage", "message"),
                    recipeId = payload.intOrNull("recipeId"),
                    recipeName = payload.stringOrNull("recipeName"),
                    messageLocalId = "prompt-${sessionId}-${UUID.randomUUID()}",
                    status = PromptStatus.PENDING
                )
            }
            ChatBusinessCode.MEAL_SESSION_READY_TO_COMPLETE -> {
                val payload = root.get("completionReady").asObjectOrNull() ?: root
                val sessionId = payload.intOrNull("chatSessionId") ?: parsedSession?.chatSessionId ?: return null
                val nextActions = parseActions(payload.get("nextActions"))
                PendingMealPolicyPrompt(
                    chatSessionId = sessionId,
                    promptCode = code,
                    actions = if (nextActions.isNotEmpty()) {
                        nextActions + PendingResolveAction(
                            MealCompletionAction.ADD_MORE_RECIPES,
                            "Chọn thêm món"
                        )
                    } else {
                        listOf(
                            PendingResolveAction(MealCompletionAction.COMPLETE_SESSION, "Đóng phiên"),
                            PendingResolveAction(MealCompletionAction.KEEP_SESSION_OPEN, "Giữ phiên mở"),
                            PendingResolveAction(MealCompletionAction.ADD_MORE_RECIPES, "Chọn thêm món")
                        )
                    },
                    pendingUserMessage = payload.stringOrNull("pendingUserMessage"),
                    reminderMessage = payload.stringOrNull("message", "reminderMessage")
                        ?: (parseSingleMessage(root.get("assistantMessage"), sessionId)?.text),
                    recipeId = payload.intOrNull("recipeId"),
                    recipeName = payload.stringOrNull("recipeName"),
                    messageLocalId = "prompt-${sessionId}-${UUID.randomUUID()}",
                    status = PromptStatus.PENDING
                )
            }
            else -> null
        }
    }

    private fun parsePendingPrimarySwitch(data: JsonElement?): PendingPrimaryRecipeSwitchPayload? {
        val obj = data.asObjectOrNull() ?: return null
        val payload = obj.get("switchConfirmation").asObjectOrNull() ?: obj.get("pendingSwitch").asObjectOrNull() ?: obj
        val sessionId = payload.intOrNull("chatSessionId") ?: parsePrimarySession(data)?.chatSessionId ?: return null
        val recipeId = payload.intOrNull("recipeId", "completedRecipeId", "closedRecipeId") ?: return null
        val nextCandidates = parseIntList(payload.get("candidateNextPrimaryRecipeIds")).ifEmpty {
            parseIntList(payload.get("nextPrimaryCandidates"))
        }.ifEmpty {
            parseIntList(payload.get("candidates"))
        }

        return PendingPrimaryRecipeSwitchPayload(
            chatSessionId = sessionId,
            recipeId = recipeId,
            reason = payload.stringOrNull("reason"),
            closedRecipeId = payload.intOrNull("closedRecipeId"),
            closedRecipeStatus = payload.stringOrNull("closedRecipeStatus"),
            currentPrimaryRecipeId = payload.intOrNull("currentPrimaryRecipeId"),
            nextPrimaryCandidates = nextCandidates,
            suggestedNextPrimaryRecipeId = payload.intOrNull("suggestedNextPrimaryRecipeId"),
            confirmField = payload.stringOrNull("confirmField"),
            chooseField = payload.stringOrNull("chooseField")
        )
    }

    private fun parseActions(data: JsonElement?): List<PendingResolveAction> {
        if (data == null || data.isJsonNull) return emptyList()
        return runCatching {
            gson.fromJson<List<PendingResolveAction>>(data, object : TypeToken<List<PendingResolveAction>>() {}.type)
        }.getOrElse { emptyList() }
    }

    private fun parseIntList(data: JsonElement?): List<Int> {
        if (data == null || data.isJsonNull || !data.isJsonArray) return emptyList()
        return data.asJsonArray.mapNotNull { element ->
            runCatching { element.asInt }.getOrNull()
        }
    }

    private fun deduplicateAndSort(messages: List<ChatUiMessage>): List<ChatUiMessage> {
        return messages
            .distinctBy { message -> message.messageId ?: message.tempId ?: message.localId }
            .sortedWith(
                compareBy<ChatUiMessage> { it.createdAt ?: "" }
                    .thenBy { it.messageId ?: Int.MAX_VALUE }
                    .thenBy { it.localId }
            )
    }

    private fun buildPromptMessage(prompt: PendingMealPolicyPrompt): ChatUiMessage {
        return ChatUiMessage(
            localId = prompt.messageLocalId,
            tempId = prompt.messageLocalId,
            chatSessionId = prompt.chatSessionId,
            role = ChatRole.ASSISTANT,
            text = prompt.reminderMessage ?: "Bepes cần bạn xác nhận bước tiếp theo.",
            createdAt = nowIsoString(),
            kind = ChatMessageKind.PROMPT,
            promptCode = prompt.promptCode,
            promptActions = prompt.actions,
            promptStatus = prompt.status,
            selectedActionId = prompt.selectedActionId,
            pendingUserMessage = prompt.pendingUserMessage,
            recipeId = prompt.recipeId,
            recipeName = prompt.recipeName,
            errorText = prompt.errorMessage
        )
    }

    private fun updatePromptStatus(
        prompt: PendingMealPolicyPrompt,
        status: String,
        selectedActionId: String? = prompt.selectedActionId,
        errorMessage: String? = prompt.errorMessage
    ) {
        _chatState.update {
            it.copy(
                pendingMealPolicyPrompt = prompt.copy(
                    status = status,
                    selectedActionId = selectedActionId,
                    errorMessage = errorMessage
                ),
                timeline = it.timeline.map { message ->
                    if (message.localId == prompt.messageLocalId) {
                        message.copy(
                            promptStatus = status,
                            selectedActionId = selectedActionId,
                            errorText = errorMessage
                        )
                    } else {
                        message
                    }
                }
            )
        }
    }

    private fun markPromptResolved(prompt: PendingMealPolicyPrompt, action: String) {
        updatePromptStatus(
            prompt = prompt,
            status = PromptStatus.RESOLVED,
            selectedActionId = action,
            errorMessage = null
        )
        _chatState.update { it.copy(pendingMealPolicyPrompt = null) }
    }

    private fun markMessageFailed(
        localId: String,
        errorText: String,
        retryable: Boolean,
        retryAfterMs: Long?
    ) {
        val retryAvailableAt = retryAfterMs?.let { System.currentTimeMillis() + it }
        _chatState.update {
            it.copy(
                timeline = it.timeline.map { message ->
                    if (message.localId == localId) {
                        message.copy(
                            isPending = false,
                            isFailed = true,
                            retryable = retryable,
                            retryAfterMs = retryAfterMs,
                            retryAvailableAt = retryAvailableAt,
                            errorText = errorText
                        )
                    } else {
                        message
                    }
                },
                errorMessage = errorText.takeIf { retryable.not() }
            )
        }
    }

    private fun buildOutgoingMessage(rawText: String): String {
        val activeRecipeId = _chatState.value.mealSession.activeRecipeId ?: _chatState.value.currentSession?.activeRecipeId
        val recipeName = _chatState.value.mealItems.firstOrNull { it.recipeId == activeRecipeId }?.recipeName
        return if (activeRecipeId != null) {
            buildString {
                append("Ngữ cảnh phiên nấu hiện tại: món đang được ưu tiên hiện tại là \"")
                append(recipeName?.takeIf { it.isNotBlank() } ?: "Món #$activeRecipeId")
                append("\" (recipeId: ")
                append(activeRecipeId)
                append(").\n")
                append("Nếu người dùng không chỉ rõ món khác hoặc không yêu cầu so sánh toàn bộ bữa, hãy mặc định trả lời theo món đang ưu tiên này trước.\n")
                append("Yêu cầu của người dùng: ")
                append(rawText)
            }
        } else {
            rawText
        }
    }

    private fun isStalePromptResult(result: RawApiResult): Boolean {
        if (result.success) return false
        if (result.httpStatus in listOf(409, 410, 422)) return true
        val normalizedMessage = result.message?.lowercase().orEmpty()
        val normalizedCode = result.code?.lowercase().orEmpty()
        return "stale" in normalizedMessage ||
            "prompt" in normalizedMessage && "invalid" in normalizedMessage ||
            "stale" in normalizedCode
    }

    private fun parseHasMore(data: JsonElement?, currentMessages: List<ChatUiMessage>): Boolean {
        if (data == null || data.isJsonNull || !data.isJsonObject) {
            return currentMessages.size >= DEFAULT_CHAT_LIMIT
        }
        val obj = data.asJsonObject
        val paging = obj.get("paging").asObjectOrNull()
        val pagination = obj.get("pagination").asObjectOrNull()
        return paging?.booleanOrNull("hasMore")
            ?: pagination?.booleanOrNull("hasMore")
            ?: (currentMessages.size >= DEFAULT_CHAT_LIMIT)
    }

    private fun findNextBeforeMessageId(data: JsonElement?, currentMessages: List<ChatUiMessage>): Int? {
        if (data == null || data.isJsonNull || !data.isJsonObject) {
            return currentMessages.firstOrNull()?.messageId
        }
        val obj = data.asJsonObject
        val paging = obj.get("paging").asObjectOrNull()
        val pagination = obj.get("pagination").asObjectOrNull()
        return paging?.intOrNull("nextBeforeMessageId")
            ?: pagination?.intOrNull("nextBeforeMessageId")
            ?: currentMessages.firstOrNull()?.messageId
    }

    private fun mapRecipesToRecommendations(recipes: List<Recipe>): List<Recommendation> {
        return recipes.mapNotNull { recipe ->
            val recipeId = recipe.recipeId ?: return@mapNotNull null
            Recommendation(
                recommendationType = RecommendationType.TRENDING,
                recipeId = recipeId,
                recipeName = recipe.recipeName,
                image = recipe.image,
                cookingTime = recipe.cookingTime,
                ration = recipe.ration,
                completionRate = null,
                missing = emptyList()
            )
        }
    }

    private fun nowIsoString(): String = Instant.now().toString()
}
