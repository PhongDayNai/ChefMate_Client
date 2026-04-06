package com.watb.chefmate.ui.appflow

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.watb.chefmate.R
import com.watb.chefmate.api.ApiClient
import com.watb.chefmate.data.ChatMessageKind
import com.watb.chefmate.data.ChatRole
import com.watb.chefmate.data.ChatUiMessage
import com.watb.chefmate.data.DietNote
import com.watb.chefmate.data.DietNoteType
import com.watb.chefmate.data.DietNoteUpsertRequest
import com.watb.chefmate.data.MealCompletionAction
import com.watb.chefmate.data.MealCompletionType
import com.watb.chefmate.data.MealRecipeState
import com.watb.chefmate.data.MealRecipeStatus
import com.watb.chefmate.data.MealSessionUiState
import com.watb.chefmate.data.PendingResolveAction
import com.watb.chefmate.data.PromptStatus
import com.watb.chefmate.data.Recommendation
import com.watb.chefmate.data.Recipe
import com.watb.chefmate.data.ResolveAction
import com.watb.chefmate.ui.theme.CustomTextField
import com.watb.chefmate.ui.theme.Header
import com.watb.chefmate.viewmodel.AppFlowViewModel
import com.watb.chefmate.viewmodel.UserViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BepesChatScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    appFlowViewModel: AppFlowViewModel,
    recipeId: Int,
    onOpenRecipe: (Recipe) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isLoggedIn by userViewModel.isLoggedIn.collectAsState()
    val user by userViewModel.user.collectAsState()
    val chatState by appFlowViewModel.chatState.collectAsState()
    val homeState by appFlowViewModel.homeState.collectAsState()

    val listState = rememberLazyListState()
    var messageInput by remember { mutableStateOf("") }
    var previousFirstKey by remember { mutableStateOf<String?>(null) }
    var previousLastKey by remember { mutableStateOf<String?>(null) }
    var showRecipePicker by remember { mutableStateOf(false) }
    var showDietNotesSheet by remember { mutableStateOf(false) }
    var editingDietNote by remember { mutableStateOf<DietNote?>(null) }
    var showDietNoteEditor by remember { mutableStateOf(false) }
    var showCompleteDialog by remember { mutableStateOf(false) }
    var showRecipeBrowserSheet by remember { mutableStateOf(false) }
    var selectedRecipePreview by remember { mutableStateOf<Recipe?>(null) }
    var recipeBrowserItems by remember { mutableStateOf<List<Recipe>>(emptyList()) }
    var selectedRecipeForConfirm by remember { mutableStateOf<Recommendation?>(null) }
    var isResolvingRecipe by remember { mutableStateOf(false) }
    var completionType by remember { mutableStateOf(MealCompletionType.COMPLETED) }
    var markRemainingStatus by remember { mutableStateOf<String?>(MealRecipeStatus.DONE) }
    var completionNote by remember { mutableStateOf("") }

    val allRecommendations = remember(
        homeState.recommendations,
        homeState.readyToCook,
        homeState.almostReady,
        homeState.trendingSuggestions
    ) {
        val merged = homeState.recommendations +
            homeState.readyToCook +
            homeState.almostReady +
            homeState.trendingSuggestions
        merged
            .filter { it.recipeId > 0 }
            .distinctBy { it.recipeId }
    }

    val currentActiveRecipeId = chatState.mealSession.activeRecipeId ?: chatState.currentSession?.activeRecipeId
    val mealRecipes = chatState.mealItems.ifEmpty { chatState.currentSession?.recipes.orEmpty() }
    val activeMealRecipe = remember(currentActiveRecipeId, mealRecipes) {
        mealRecipes.firstOrNull { it.recipeId == currentActiveRecipeId }
    }
    val selectedRecommendation = remember(currentActiveRecipeId, allRecommendations) {
        allRecommendations.firstOrNull { it.recipeId == currentActiveRecipeId }
    }
    val selectedRecipeName = activeMealRecipe?.recipeName ?: selectedRecommendation?.recipeName
    val hasSelectedRecipe = selectedRecommendation != null || (currentActiveRecipeId != null && currentActiveRecipeId > 0)
    var showContextActions by remember(hasSelectedRecipe) { mutableStateOf(!hasSelectedRecipe) }

    val readyToCook = remember(homeState.readyToCook) {
        homeState.readyToCook.filter { it.recipeId > 0 }.distinctBy { it.recipeId }
    }
    val almostReady = remember(homeState.almostReady) {
        homeState.almostReady.filter { it.recipeId > 0 }.distinctBy { it.recipeId }
    }
    val unavailable = remember(allRecommendations, readyToCook, almostReady) {
        val includeIds = (readyToCook + almostReady).map { it.recipeId }.toSet()
        allRecommendations.filter { it.recipeId !in includeIds }
    }

    val activeDietNotes = remember(homeState.dietNotes) {
        homeState.dietNotes.filter { it.isActive }
    }

    LaunchedEffect(isLoggedIn, user?.userId, recipeId, chatState.historyMode) {
        if (isLoggedIn && user != null) {
            if (!chatState.historyMode) {
                appFlowViewModel.bootstrapUnifiedTimeline(
                    userId = user!!.userId,
                    activeRecipeId = recipeId.takeIf { it > 0 }
                )
            }
        }
    }

    LaunchedEffect(chatState.timeline, chatState.sending) {
        if (chatState.timeline.isEmpty()) {
            previousFirstKey = null
            previousLastKey = null
            return@LaunchedEffect
        }

        val currentFirst = chatState.timeline.first()
        val currentLast = chatState.timeline.last()
        val currentFirstKey = currentFirst.messageId?.toString() ?: currentFirst.localId
        val currentLastKey = currentLast.messageId?.toString() ?: currentLast.localId

        val prependingOlderMessages = previousFirstKey != null &&
            currentFirstKey != previousFirstKey &&
            currentLastKey == previousLastKey

        val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
        val isNearBottom = lastVisibleIndex == -1 || lastVisibleIndex >= chatState.timeline.lastIndex - 1
        val shouldScrollToBottom = !prependingOlderMessages && (isNearBottom || chatState.sending || previousLastKey == null)

        if (shouldScrollToBottom) {
            listState.animateScrollToItem(chatState.timeline.lastIndex)
        }

        previousFirstKey = currentFirstKey
        previousLastKey = currentLastKey
    }

    LaunchedEffect(
        isLoggedIn,
        user?.userId
    ) {
        if (!isLoggedIn || user == null) return@LaunchedEffect

        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .map { (index, offset) -> index == 0 && offset <= 8 }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                val latestState = appFlowViewModel.chatState.value
                if (latestState.hasMore && !latestState.loadingTimeline && latestState.nextBeforeMessageId != null) {
                    appFlowViewModel.loadMoreTimeline(user!!.userId)
                }
            }
    }

    val composerLockedByPrompt = chatState.pendingMealPolicyPrompt?.status in listOf(
        PromptStatus.PENDING,
        PromptStatus.LOADING
    )
    val sessionClosed = chatState.mealSession.uiClosed || chatState.currentSession?.uiClosed == true
    val hasDraftSession = chatState.currentSessionId != null && currentActiveRecipeId != null && currentActiveRecipeId > 0
    val canCompleteSession = chatState.currentSessionId != null &&
        chatState.timeline.any { message ->
            message.chatSessionId == chatState.currentSessionId &&
                message.role == ChatRole.USER &&
                message.kind == ChatMessageKind.TEXT &&
                !message.isPending &&
                !message.isFailed
        }
    val composerEnabled = isLoggedIn &&
        user != null &&
        !chatState.historyMode &&
        hasDraftSession &&
        !sessionClosed &&
        !composerLockedByPrompt
    val composerPlaceholder = when {
        chatState.historyMode -> "Phiên lịch sử chỉ để xem lại"
        !hasDraftSession -> "Chọn món xong mới có thể chat"
        sessionClosed -> "Phiên nấu đã hoàn tất"
        composerLockedByPrompt -> "Hãy xử lý prompt hiện tại trước"
        else -> "Nhắn Bepes..."
    }

    chatState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            appFlowViewModel.clearChatError()
        }
    }

    val openCompleteDialog = {
        if (chatState.historyMode) {
            Toast.makeText(context, "Phiên lịch sử chỉ xem lại, không thể cập nhật trực tiếp", Toast.LENGTH_SHORT).show()
        } else if (!canCompleteSession) {
            Toast.makeText(context, "Chưa có hội thoại trong phiên này, chưa thể hoàn thành", Toast.LENGTH_SHORT).show()
        } else if (chatState.currentSessionId != null) {
            completionType = MealCompletionType.COMPLETED
            markRemainingStatus = MealRecipeStatus.DONE
            completionNote = ""
            showCompleteDialog = true
        } else {
            Toast.makeText(context, "Chưa có cuộc trò chuyện để hoàn thành", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Header(
            text = "Bepes",
            leadingIcon = {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = "Quay lại",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            trailingIcon = {
                AnimatedVisibility(
                    visible = !showContextActions,
                    enter = fadeIn() + scaleIn(initialScale = 0.8f),
                    exit = fadeOut() + scaleOut(targetScale = 0.8f)
                ) {
                    IconButton(
                        onClick = openCompleteDialog,
                        enabled = canCompleteSession,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_tick_square),
                            contentDescription = "Hoàn thành",
                            tint = if (canCompleteSession) Color.White else Color.White.copy(alpha = 0.45f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        )

        if (!isLoggedIn || user == null) {
            NeedLoginCard(
                title = "Bạn cần đăng nhập để trò chuyện cùng Bepes",
                onSignIn = { navController.navigate("signIn") }
            )
        } else {
            SessionContextSection(
                selectedRecipeName = selectedRecipeName,
                mealRecipes = mealRecipes,
                activeDietNotes = activeDietNotes,
                onOpenPicker = {
                    appFlowViewModel.refreshRecommendations(user!!.userId)
                    showRecipePicker = true
                },
                onOpenNotes = {
                    appFlowViewModel.refreshDietNotes(user!!.userId)
                    showDietNotesSheet = true
                },
                onComplete = openCompleteDialog,
                onOpenRecipe = {
                    if (mealRecipes.isEmpty()) {
                        Toast.makeText(context, "Hãy chọn món trước khi mở công thức", Toast.LENGTH_SHORT).show()
                    } else {
                        coroutineScope.launch {
                            isResolvingRecipe = true
                            val response = ApiClient.getAllRecipes()
                            isResolvingRecipe = false
                            val items = response?.data
                                ?.filter { recipe -> mealRecipes.any { it.recipeId == recipe.recipeId } }
                                .orEmpty()

                            if (items.isNotEmpty()) {
                                recipeBrowserItems = items.sortedBy { recipe ->
                                    mealRecipes.indexOfFirst { it.recipeId == recipe.recipeId }.takeIf { it >= 0 } ?: Int.MAX_VALUE
                                }
                                selectedRecipePreview = null
                                showRecipeBrowserSheet = true
                            } else {
                                Toast.makeText(context, "Không tìm thấy công thức phù hợp", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                isResolvingRecipe = isResolvingRecipe,
                canCompleteSession = canCompleteSession,
                showActionButtons = showContextActions && !chatState.historyMode,
                onToggleActionButtons = { showContextActions = !showContextActions },
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )

            val sendCurrentMessage = send@{
                if (!composerEnabled) {
                    val error = when {
                        chatState.historyMode -> "Phiên lịch sử chỉ xem lại, hãy mở phiên nấu mới để tiếp tục"
                        !hasDraftSession -> "Chọn món xong mới có thể chat"
                        sessionClosed -> "Phiên này đã hoàn tất"
                        composerLockedByPrompt -> "Hãy xử lý prompt hiện tại trước"
                        else -> "Chưa thể gửi tin nhắn lúc này"
                    }
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    return@send
                }
                val message = messageInput.trim()
                if (message.isNotEmpty() && !chatState.sending) {
                    appFlowViewModel.sendMessage(user!!.userId, message)
                    messageInput = ""
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
            ) {
                if (chatState.loadingTimeline && chatState.timeline.isEmpty()) {
                    item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 10.dp)
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFFF97316),
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }

                if (!chatState.loadingTimeline && chatState.timeline.isEmpty()) {
                    item(key = "empty_state") {
                        EmptyTimelineCard(
                            text = if (sessionClosed) {
                                "Phiên nấu đã hoàn tất. Bạn có thể xem lại lịch sử ở đây."
                            } else if (!hasDraftSession) {
                                "Chọn món để tạo phiên nấu mới, sau đó mới có thể trò chuyện với Bepes."
                            } else {
                                "Chưa có tin nhắn nào. Nhấn gửi để bắt đầu trò chuyện với Bepes."
                            }
                        )
                    }
                }

                itemsIndexed(chatState.timeline, key = { _, item -> item.messageId ?: item.localId }) { index, message ->
                    val previous = chatState.timeline.getOrNull(index - 1)
                    val showSessionDivider = index > 0 && (
                        message.isSessionStart ||
                            (
                                previous != null &&
                                    previous.chatSessionId != null &&
                                    message.chatSessionId != null &&
                                    previous.chatSessionId != message.chatSessionId
                                )
                        )

                    if (showSessionDivider) {
                        SessionDivider(text = sessionDividerText(message.createdAt))
                    }

                    ChatBubble(
                        message = message,
                        isActionRunning = chatState.sending || chatState.mealSyncing,
                        onRetry = {
                            appFlowViewModel.retryMessage(user!!.userId, message.localId)
                        },
                        onPromptAction = { action ->
                            when (action.id) {
                                MealCompletionAction.ADD_MORE_RECIPES -> {
                                    appFlowViewModel.resolveMealPolicyPrompt(user!!.userId, action.id)
                                    appFlowViewModel.refreshRecommendations(user!!.userId)
                                    showRecipePicker = true
                                }
                                MealCompletionAction.COMPLETE_SESSION -> {
                                    openCompleteDialog()
                                }
                                else -> {
                                    appFlowViewModel.resolveMealPolicyPrompt(
                                        userId = user!!.userId,
                                        action = action.id
                                    )
                                }
                            }
                        }
                    )
                }

                if (chatState.sending && !chatState.timeline.any { it.kind == ChatMessageKind.PROMPT && it.promptStatus == PromptStatus.LOADING }) {
                    item(key = "typing_indicator") {
                        TypingIndicatorBubble()
                    }
                }

                item { Spacer(modifier = Modifier.height(10.dp)) }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
            ) {
                CustomTextField(
                    value = messageInput,
                    onValueChange = {
                        if (composerEnabled) {
                            messageInput = it
                        }
                    },
                    placeholder = composerPlaceholder,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { sendCurrentMessage() }),
                    trailingIcon = {
                        IconButton(
                            onClick = { sendCurrentMessage() }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_send),
                                contentDescription = "Gửi",
                                tint = if (composerEnabled && !chatState.sending) Color(0xFFF97316) else Color(0xFF9CA3AF)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showRecipePicker && isLoggedIn && user != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showRecipePicker = false },
            sheetState = sheetState,
            containerColor = Color(0xFFFFFBF5)
        ) {
            RecipePickerSheet(
                isLoading = homeState.isLoading || homeState.isRefreshingRecommendations,
                currentMealRecipes = mealRecipes,
                activeRecipeId = currentActiveRecipeId,
                readyToCook = readyToCook,
                almostReady = almostReady,
                unavailable = unavailable,
                onMoveRecipe = { recipeId, direction ->
                    appFlowViewModel.moveMealRecipe(user!!.userId, recipeId, direction)
                },
                onRemoveRecipe = { recipeId ->
                    appFlowViewModel.removeRecipeFromCurrentSession(user!!.userId, recipeId)
                },
                onSetPrimaryRecipe = { recipeId ->
                    appFlowViewModel.setMealPrimaryRecipe(user!!.userId, recipeId)
                },
                onUpdateRecipeStatus = { recipeId, status ->
                    appFlowViewModel.updateMealRecipeStatus(
                        userId = user!!.userId,
                        recipeId = recipeId,
                        status = status
                    )
                },
                onSelectRecipe = { selected ->
                    selectedRecipeForConfirm = selected
                },
                onRefresh = { appFlowViewModel.refreshRecommendations(user!!.userId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }

    if (selectedRecipeForConfirm != null && user != null) {
        val selected = selectedRecipeForConfirm!!
        val missingText = if (selected.missing.isEmpty()) {
            "Không thiếu nguyên liệu."
        } else {
            selected.missing.joinToString { item ->
                item.ingredientName
            }
        }
        AlertDialog(
            onDismissRequest = { selectedRecipeForConfirm = null },
            title = {
                Text(
                    text = "Chọn món này?",
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                )
            },
            text = {
                Column {
                    Text(
                        text = selected.recipeName,
                        color = Color(0xFF111827),
                        fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                    )
                    Text(
                        text = "Độ sẵn sàng: ${selected.completionRate ?: 0}%",
                        color = Color(0xFF6B7280),
                        fontSize = 13.sp,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Text(
                        text = "Nguyên liệu còn thiếu: $missingText",
                        color = Color(0xFF92400E),
                        fontSize = 13.sp,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        appFlowViewModel.selectRecipeForCurrentSession(user!!.userId, selected.recipeId)
                        selectedRecipeForConfirm = null
                        showRecipePicker = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316))
                ) {
                    Text(
                        text = "Xác nhận",
                        color = Color.White,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = { selectedRecipeForConfirm = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B7280))
                ) {
                    Text(
                        text = "Hủy",
                        color = Color.White,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                    )
                }
            }
        )
    }

    if (showDietNotesSheet && isLoggedIn && user != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showDietNotesSheet = false },
            sheetState = sheetState,
            containerColor = Color(0xFFFFFBF5)
        ) {
            ChatDietNotesSheet(
                notes = homeState.dietNotes,
                onRefresh = { appFlowViewModel.refreshDietNotes(user!!.userId) },
                onAdd = {
                    editingDietNote = null
                    showDietNoteEditor = true
                },
                onEdit = { note ->
                    editingDietNote = note
                    showDietNoteEditor = true
                },
                onToggle = { note ->
                    appFlowViewModel.upsertDietNote(
                        DietNoteUpsertRequest(
                            noteId = note.noteId,
                            userId = note.userId,
                            noteType = note.noteType,
                            label = note.label,
                            keywords = note.keywords,
                            instruction = note.instruction,
                            isActive = !note.isActive,
                            startAt = note.startAt,
                            endAt = note.endAt
                        )
                    )
                },
                onDelete = { note ->
                    note.noteId?.let { noteId ->
                        appFlowViewModel.deleteDietNote(user!!.userId, noteId)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }

    if (showDietNoteEditor && user != null) {
        ChatDietNoteEditorDialog(
            initial = editingDietNote,
            userId = user!!.userId,
            onDismiss = { showDietNoteEditor = false },
            onSave = { request ->
                appFlowViewModel.upsertDietNote(request)
                showDietNoteEditor = false
            }
        )
    }

    if (showCompleteDialog && user != null) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = {
                Text(
                    text = "Hoàn tất phiên nấu",
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Chọn cách đóng phiên và cách xử lý các món còn lại.",
                        fontFamily = FontFamily(Font(resId = R.font.roboto_regular))
                    )

                    Text(
                        text = "Kiểu hoàn tất",
                        color = Color(0xFF374151),
                        fontSize = 13.sp,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ContextActionChip(
                            text = "Completed",
                            onClick = { completionType = MealCompletionType.COMPLETED },
                            containerColor = if (completionType == MealCompletionType.COMPLETED) Color(0xFFD1FAE5) else Color(0xFFF3F4F6),
                            textColor = if (completionType == MealCompletionType.COMPLETED) Color(0xFF166534) else Color(0xFF6B7280),
                            modifier = Modifier.weight(1f)
                        )
                        ContextActionChip(
                            text = "Abandoned",
                            onClick = { completionType = MealCompletionType.ABANDONED },
                            containerColor = if (completionType == MealCompletionType.ABANDONED) Color(0xFFFEE2E2) else Color(0xFFF3F4F6),
                            textColor = if (completionType == MealCompletionType.ABANDONED) Color(0xFFB91C1C) else Color(0xFF6B7280),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text(
                        text = "Xử lý món còn lại",
                        color = Color(0xFF374151),
                        fontSize = 13.sp,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "done" to "Đánh dấu tất cả là xong",
                            "skipped" to "Đánh dấu tất cả là bỏ qua",
                            null to "Giữ nguyên trạng thái hiện tại"
                        ).forEach { (value, label) ->
                            ContextActionChip(
                                text = label,
                                onClick = { markRemainingStatus = value },
                                containerColor = if (markRemainingStatus == value) Color(0xFFFFEDD5) else Color(0xFFF3F4F6),
                                textColor = if (markRemainingStatus == value) Color(0xFFF97316) else Color(0xFF6B7280),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Text(
                        text = "Ghi chú",
                        color = Color(0xFF374151),
                        fontSize = 13.sp,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                    )
                    CustomTextField(
                        value = completionNote,
                        onValueChange = { completionNote = it },
                        placeholder = "Ví dụ: Đã nấu xong, dọn bếp xong",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCompleteDialog = false
                        appFlowViewModel.completeCurrentSession(
                            userId = user!!.userId,
                            completionType = completionType,
                            markRemainingStatus = markRemainingStatus,
                            note = completionNote.trim().ifBlank { null }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316))
                ) {
                    Text(
                        text = "Hoàn thành",
                        color = Color.White,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = { showCompleteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B7280))
                ) {
                    Text(
                        text = "Hủy",
                        color = Color.White,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                    )
                }
            }
        )
    }

    if (showRecipeBrowserSheet && user != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = {
                showRecipeBrowserSheet = false
                selectedRecipePreview = null
            },
            sheetState = sheetState,
            containerColor = Color(0xFFFFFBF5)
        ) {
            RecipeBrowserSheet(
                recipes = recipeBrowserItems,
                selectedRecipe = selectedRecipePreview,
                onSelectRecipe = { recipe ->
                    selectedRecipePreview = recipe
                },
                onBackToList = { selectedRecipePreview = null },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }

    val pendingPrimarySwitch = chatState.pendingPrimaryRecipeSwitch
    if (pendingPrimarySwitch != null && user != null) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(
                    text = "Chọn món tiếp theo",
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                )
            },
            text = {
                Column {
                    Text(
                        text = "Bepes cần xác nhận món focus tiếp theo sau khi bạn hoàn thành món hiện tại.",
                        fontFamily = FontFamily(Font(resId = R.font.roboto_regular))
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        pendingPrimarySwitch.nextPrimaryCandidates.forEach { candidateId ->
                            val label = allRecommendations.firstOrNull { it.recipeId == candidateId }?.recipeName
                                ?: "Món #$candidateId"
                            Button(
                                onClick = {
                                    appFlowViewModel.updateMealRecipeStatus(
                                        userId = user!!.userId,
                                        recipeId = pendingPrimarySwitch.recipeId,
                                        status = MealRecipeStatus.DONE,
                                        confirmSwitchPrimary = true,
                                        nextPrimaryRecipeId = candidateId
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = label,
                                    color = Color.White,
                                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }

    val pending = chatState.pendingPreviousRecipe
    if (pending != null && user != null) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(
                    text = "Bạn đã nấu xong món trước đó chưa?",
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                )
            },
            text = {
                Column {
                    Text(
                        text = buildString {
                            append("Bepes thấy bạn còn món đang nấu")
                            if (!pending.recipeName.isNullOrBlank()) {
                                append(": ${pending.recipeName}")
                            }
                            append(". Bạn muốn xử lý thế nào?")
                        },
                        fontFamily = FontFamily(Font(resId = R.font.roboto_regular))
                    )
                    if (!pending.pendingUserMessage.isNullOrBlank()) {
                        Text(
                            text = "Tin nhắn chờ: ${pending.pendingUserMessage}",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280),
                            fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        pending.actions.forEach { action ->
                            val (containerColor, textColor) = when (action.id) {
                                ResolveAction.COMPLETE_AND_DEDUCT -> Color(0xFFF97316) to Color.White
                                ResolveAction.SKIP_DEDUCTION -> Color(0xFF6B7280) to Color.White
                                else -> Color(0xFFE0F2FE) to Color(0xFF0369A1)
                            }
                            val buttonLabel = when (action.id) {
                                ResolveAction.COMPLETE_AND_DEDUCT -> "Đã nấu xong, trừ nguyên liệu"
                                ResolveAction.SKIP_DEDUCTION -> "Đã nấu xong, không trừ"
                                ResolveAction.CONTINUE_CURRENT_SESSION -> "Chưa nấu xong, tiếp tục trò chuyện"
                                else -> action.label
                            }
                            Button(
                                onClick = {
                                    appFlowViewModel.resolvePendingPreviousRecipe(user!!.userId, action.id)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = containerColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = buttonLabel,
                                    color = textColor,
                                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
}

@Composable
private fun TypingIndicatorBubble() {
    val infinite = rememberInfiniteTransition(label = "bepes_typing")
    val dot1 = infinite.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 900
                1f at 150
                0.25f at 450
                0.25f at 900
            }
        ),
        label = "dot1"
    )
    val dot2 = infinite.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 900
                0.25f at 0
                1f at 300
                0.25f at 600
                0.25f at 900
            }
        ),
        label = "dot2"
    )
    val dot3 = infinite.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 900
                0.25f at 0
                0.25f at 300
                1f at 450
                0.25f at 900
            }
        ),
        label = "dot3"
    )

    Row(
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEDD5)),
            modifier = Modifier.fillMaxWidth(0.56f)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
                Text(
                    text = "Bepes",
                    color = Color(0xFFF97316),
                    fontSize = 12.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    TypingDot(alpha = dot1.value)
                    TypingDot(alpha = dot2.value)
                    TypingDot(alpha = dot3.value)
                }
            }
        }
    }
}

@Composable
private fun TypingDot(alpha: Float) {
    Box(
        modifier = Modifier
            .size(7.dp)
            .background(Color(0xFFF97316).copy(alpha = alpha.coerceIn(0f, 1f)), CircleShape)
    )
}

@Composable
private fun SessionContextSection(
    selectedRecipeName: String?,
    mealRecipes: List<MealRecipeState>,
    activeDietNotes: List<DietNote>,
    onOpenPicker: () -> Unit,
    onOpenNotes: () -> Unit,
    onOpenRecipe: () -> Unit,
    onComplete: () -> Unit,
    isResolvingRecipe: Boolean,
    canCompleteSession: Boolean,
    showActionButtons: Boolean,
    onToggleActionButtons: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .animateContentSize()
        ) {
            val recipeText = when {
                !selectedRecipeName.isNullOrBlank() -> selectedRecipeName
                else -> "Chưa chọn món"
            }

            val noteSummary = if (activeDietNotes.isEmpty()) "Không có" else "Có"

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Món đang chọn: $recipeText",
                        color = Color(0xFF1F2937),
                        fontSize = 13.sp,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_regular))
                    )

                    Text(
                        text = "Ghi chú ăn uống: $noteSummary",
                        color = Color(0xFF374151),
                        fontSize = 12.sp,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    if (mealRecipes.isNotEmpty()) {
                        Card(
                            shape = RoundedCornerShape(999.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                text = "${mealRecipes.size} món",
                                color = Color(0xFF374151),
                                fontSize = 11.sp,
                                fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                ContextActionsToggleButton(
                    expanded = showActionButtons,
                    onClick = onToggleActionButtons
                )
            }

            AnimatedVisibility(
                visible = showActionButtons,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
            ) {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        ContextActionChip(
                            text = if (mealRecipes.isEmpty()) "Chọn món" else "Món ăn",
                            onClick = onOpenPicker,
                            modifier = Modifier.weight(1f)
                        )
                        ContextActionChip(
                            text = "Ghi chú",
                            onClick = onOpenNotes,
                            containerColor = Color(0xFFE0F2FE),
                            textColor = Color(0xFF0369A1),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                    ) {
                        ContextActionChip(
                            text = if (isResolvingRecipe) "Đang mở..." else "Xem công thức",
                            onClick = onOpenRecipe,
                            enabled = !isResolvingRecipe,
                            containerColor = Color(0xFFEDE9FE),
                            textColor = Color(0xFF6D28D9),
                            modifier = Modifier.weight(1f)
                        )
                        ContextActionChip(
                            text = "Hoàn thành",
                            onClick = onComplete,
                            enabled = canCompleteSession,
                            containerColor = Color(0xFFD1FAE5),
                            textColor = Color(0xFF166534),
                            borderColor = Color(0xFF86EFAC),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContextActionsToggleButton(
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "contextToggleRotation"
    )

    Card(
        onClick = onClick,
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color(0xFFFDBA74)),
        modifier = modifier.size(32.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_expand),
                contentDescription = if (expanded) "Ẩn thao tác" else "Hiện thao tác",
                tint = Color(0xFFF97316),
                modifier = Modifier
                    .size(14.dp)
                    .rotate(rotation)
            )
        }
    }
}

@Composable
private fun ContextActionChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = Color(0xFFFFEDD5),
    textColor: Color = Color(0xFFF97316),
    borderColor: Color = Color.Transparent
) {
    Card(
        onClick = {
            if (enabled) {
                onClick()
            }
        },
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) containerColor else Color(0xFFF3F4F6)
        ),
        border = if (enabled) BorderStroke(1.dp, borderColor) else null,
        modifier = modifier
    ) {
        Text(
            text = text,
            color = if (enabled) textColor else Color(0xFF9CA3AF),
            fontSize = 11.sp,
            fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 7.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MiniActionButton(
    text: String,
    containerColor: Color = Color(0xFFF3F4F6),
    textColor: Color = Color(0xFF4B5563),
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun EmptyTimelineCard(text: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFF6B7280),
            fontSize = 14.sp,
            fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp)
        )
    }
}

@Composable
private fun RecipePickerSheet(
    isLoading: Boolean,
    currentMealRecipes: List<MealRecipeState>,
    activeRecipeId: Int?,
    readyToCook: List<Recommendation>,
    almostReady: List<Recommendation>,
    unavailable: List<Recommendation>,
    onRefresh: () -> Unit,
    onMoveRecipe: (Int, Int) -> Unit,
    onRemoveRecipe: (Int) -> Unit,
    onSetPrimaryRecipe: (Int) -> Unit,
    onUpdateRecipeStatus: (Int, String) -> Unit,
    onSelectRecipe: (Recommendation) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var expandReady by remember(readyToCook.isNotEmpty(), almostReady.isNotEmpty(), unavailable.isNotEmpty()) {
        mutableStateOf(readyToCook.isNotEmpty() || almostReady.isNotEmpty())
    }
    var expandAlmost by remember(readyToCook.isNotEmpty(), almostReady.isNotEmpty(), unavailable.isNotEmpty()) {
        mutableStateOf(readyToCook.isNotEmpty() || almostReady.isNotEmpty())
    }
    var expandUnavailable by remember(readyToCook.isNotEmpty(), almostReady.isNotEmpty(), unavailable.isNotEmpty()) {
        mutableStateOf(readyToCook.isEmpty() && almostReady.isEmpty() && unavailable.isNotEmpty())
    }

    Column(
        modifier = modifier
            .padding(bottom = 18.dp)
            .verticalScroll(scrollState)
            .animateContentSize()
    ) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Chọn món cho phiên chat",
                            color = Color(0xFF111827),
                            fontSize = 18.sp,
                            fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                        )
                        Text(
                            text = "Sắp xếp thứ tự auto next, chọn món ưu tiên hiện tại và thêm món từ gợi ý tủ lạnh.",
                            color = Color(0xFF6B7280),
                            fontSize = 11.sp,
                            fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Text(
                        text = "Làm mới",
                        color = Color(0xFFF97316),
                        fontSize = 13.sp,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                        modifier = Modifier.padding(start = 12.dp).clickable(onClick = onRefresh)
                    )
                }

                if (currentMealRecipes.isNotEmpty()) {
                    HorizontalDivider(
                        color = Color(0xFFF3E8D6),
                        modifier = Modifier.padding(top = 14.dp, bottom = 12.dp)
                    )

                    Text(
                        text = "Danh sách món đã chọn",
                        color = Color(0xFF111827),
                        fontSize = 15.sp,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                    )
                    Text(
                        text = "Thứ tự ở đây là thứ tự ưu tiên auto next.",
                        color = Color(0xFF8B7355),
                        fontSize = 11.sp,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    currentMealRecipes.sortedBy { it.sortOrder }.forEachIndexed { index, recipe ->
                        val isPrimary = recipe.recipeId == activeRecipeId || recipe.isPrimary
                        SelectedMealRecipeEditorCard(
                            index = index,
                            recipe = recipe,
                            isPrimary = isPrimary,
                            onMoveUp = { onMoveRecipe(recipe.recipeId, -1) },
                            onMoveDown = { onMoveRecipe(recipe.recipeId, 1) },
                            onRemove = { onRemoveRecipe(recipe.recipeId) },
                            onSetPrimary = { onSetPrimaryRecipe(recipe.recipeId) },
                            onUpdateStatus = { status -> onUpdateRecipeStatus(recipe.recipeId, status) },
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
                }
            }
        }

        if (isLoading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
            ) {
                CircularProgressIndicator(color = Color(0xFFF97316))
            }
            return
        }

        AddMoreDivider(modifier = Modifier.padding(top = 14.dp))

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                Card(
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "+",
                            color = Color(0xFFF59E0B),
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Chọn món để bắt đầu nấu",
                        color = Color(0xFF111827),
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                    )
                    Text(
                        text = "Ưu tiên các món hợp với nguyên liệu hiện có, nhưng bạn vẫn có thể chọn món từ gợi ý để bắt đầu lên kế hoạch nấu tiếp.",
                        color = Color(0xFF6B7280),
                        fontSize = 11.sp,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        RecipeGroupSection(
            title = "Có thể nấu ngay",
            titleColor = Color(0xFF15803D),
            subtitle = "Đủ nguyên liệu để bắt đầu ngay lúc này.",
            expanded = expandReady,
            onToggleExpanded = { expandReady = !expandReady },
            items = readyToCook,
            emptyText = "Chưa có món sẵn sàng",
            onSelectRecipe = onSelectRecipe
        )

        RecipeGroupSection(
            title = "Thiếu một chút",
            titleColor = Color(0xFFB45309),
            subtitle = "Thiếu ít nguyên liệu, có thể cân nhắc thay thế hoặc mua nhanh.",
            expanded = expandAlmost,
            onToggleExpanded = { expandAlmost = !expandAlmost },
            items = almostReady,
            emptyText = "Chưa có món gần hoàn thiện",
            onSelectRecipe = onSelectRecipe
        )

        RecipeGroupSection(
            title = "Món đề xuất",
            titleColor = Color(0xFF6B7280),
            subtitle = "Các món đáng tham khảo thêm cho bữa hiện tại.",
            expanded = expandUnavailable,
            onToggleExpanded = { expandUnavailable = !expandUnavailable },
            items = unavailable,
            emptyText = "Chưa có dữ liệu ở nhóm này",
            onSelectRecipe = onSelectRecipe
        )
    }
}

@Composable
private fun SelectedMealRecipeEditorCard(
    index: Int,
    recipe: MealRecipeState,
    isPrimary: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
    onSetPrimary: () -> Unit,
    onUpdateStatus: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColors = recipeStatusColors(recipe.status)
    var statusExpanded by remember(recipe.recipeId, recipe.status) { mutableStateOf(false) }
    val hintText = when {
        isPrimary -> "Bepes sẽ bám theo món này trước"
        recipe.status.equals(MealRecipeStatus.DONE, ignoreCase = true) -> "Món này đã hoàn tất"
        recipe.status.equals(MealRecipeStatus.SKIPPED, ignoreCase = true) -> "Món này đã được bỏ qua"
        else -> "Có thể chuyển món này lên làm focus"
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDFC)),
        border = BorderStroke(1.dp, if (isPrimary) Color(0xFFF6B26B) else Color(0xFFE6DED2)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Card(
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F5F0)),
                    modifier = Modifier.size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            painter = painterResource(R.drawable.ic_options),
                            contentDescription = null,
                            tint = Color(0xFFC4B5A5),
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Card(
                    shape = RoundedCornerShape(999.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2A44))
                ) {
                    Text(
                        text = "#${index + 1}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = recipe.recipeName ?: "Món #${recipe.recipeId}",
                    color = Color(0xFF111827),
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                    maxLines = 2,
                    modifier = Modifier.weight(1f)
                )
                StatusCapsule(
                    text = if (isPrimary) "Đang ưu tiên" else "Đã chọn",
                    containerColor = if (isPrimary) Color(0xFFD1FAE5) else Color(0xFFF3F4F6),
                    textColor = if (isPrimary) Color(0xFF166534) else Color(0xFF6B7280)
                )
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFCF6)),
                border = BorderStroke(1.dp, Color(0xFFF3D49A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Card(
                        onClick = { statusExpanded = !statusExpanded },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = statusColors.background),
                        border = BorderStroke(1.dp, statusColors.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = recipeStatusLabel(recipe.status),
                                color = statusColors.text,
                                fontSize = 13.sp,
                                fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                painter = painterResource(R.drawable.ic_expand),
                                contentDescription = null,
                                tint = statusColors.text,
                                modifier = Modifier
                                    .size(14.dp)
                                    .rotate(if (statusExpanded) 180f else 0f)
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = statusExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Text(
                                text = "TRẠNG THÁI MÓN",
                                color = Color(0xFFB07A37),
                                fontSize = 10.sp,
                                fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                            )

                            mealRecipeStatuses().forEach { status ->
                                val optionColors = recipeStatusColors(status)
                                val isSelected = recipe.status.equals(status, ignoreCase = true)
                                Card(
                                    onClick = {
                                        statusExpanded = false
                                        if (!isSelected) {
                                            onUpdateStatus(status)
                                        }
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) optionColors.background else Color.White
                                    ),
                                    border = if (isSelected) BorderStroke(1.dp, optionColors.border) else null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = recipeStatusLabel(status),
                                            color = if (isSelected) optionColors.text else Color(0xFF374151),
                                            fontSize = 13.sp,
                                            fontFamily = FontFamily(
                                                Font(
                                                    resId = if (isSelected) R.font.roboto_bold else R.font.roboto_regular
                                                )
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (isSelected) {
                                            Text(
                                                text = "✓",
                                                color = optionColors.text,
                                                fontSize = 14.sp,
                                                fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            val detailLine = listOfNotNull(
                recipe.cookingTime?.takeIf { it.isNotBlank() },
                recipe.ration?.let { "$it phần" }
            ).joinToString(" • ")
            if (detailLine.isNotBlank()) {
                Text(
                    text = detailLine,
                    color = Color(0xFF6B7280),
                    fontSize = 11.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPrimary) Color(0xFFFFF6E8) else Color(0xFFF9F5F0)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Text(
                    text = if (isPrimary) "Đang ưu tiên" else "Chọn focus",
                    color = if (isPrimary) Color(0xFFE58B28) else Color(0xFF0F6CBD),
                    fontSize = 11.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                StatusCapsule(
                    text = hintText,
                    containerColor = if (isPrimary) Color(0xFFFFF3E0) else Color(0xFFF9F5F0),
                    textColor = if (isPrimary) Color(0xFFB45309) else Color(0xFF8B7355),
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(8.dp))
                RecipeFooterIconButton(
                    iconRes = R.drawable.ic_expand,
                    contentDescription = "Đưa món lên",
                    rotation = 180f,
                    onClick = onMoveUp
                )
                Spacer(modifier = Modifier.width(6.dp))
                RecipeFooterIconButton(
                    iconRes = R.drawable.ic_expand,
                    contentDescription = "Đưa món xuống",
                    rotation = 0f,
                    onClick = onMoveDown
                )
                Spacer(modifier = Modifier.width(6.dp))
                RecipeFooterIconButton(
                    iconRes = R.drawable.ic_cancel,
                    contentDescription = "Xóa món",
                    onClick = onRemove,
                    containerColor = Color(0xFFFFF1F2),
                    iconTint = Color(0xFFEF4444)
                )
            }

            if (!isPrimary) {
                ContextActionChip(
                    text = "Bepes sẽ bám theo món này trước",
                    onClick = onSetPrimary,
                    containerColor = Color(0xFFE0F2FE),
                    textColor = Color(0xFF0369A1),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
        }
    }
}

private fun recipeStatusLabel(status: String): String = when (status.lowercase()) {
    MealRecipeStatus.COOKING -> "Đang nấu"
    MealRecipeStatus.DONE -> "Đã xong"
    MealRecipeStatus.SKIPPED -> "Đã bỏ qua"
    MealRecipeStatus.PENDING -> "Chờ nấu"
    else -> "Trạng thái: $status"
}

private fun mealRecipeStatuses(): List<String> = listOf(
    MealRecipeStatus.PENDING,
    MealRecipeStatus.COOKING,
    MealRecipeStatus.DONE,
    MealRecipeStatus.SKIPPED
)

private data class StatusVisuals(
    val text: Color,
    val border: Color,
    val background: Color
)

private fun recipeStatusColors(status: String): StatusVisuals = when (status.lowercase()) {
    MealRecipeStatus.COOKING -> StatusVisuals(
        text = Color(0xFFD97706),
        border = Color(0xFFFCD34D),
        background = Color(0xFFFFF7ED)
    )
    MealRecipeStatus.DONE -> StatusVisuals(
        text = Color(0xFF15803D),
        border = Color(0xFF86EFAC),
        background = Color(0xFFF0FDF4)
    )
    MealRecipeStatus.SKIPPED -> StatusVisuals(
        text = Color(0xFFB91C1C),
        border = Color(0xFFFCA5A5),
        background = Color(0xFFFEF2F2)
    )
    else -> StatusVisuals(
        text = Color(0xFFB45309),
        border = Color(0xFFFCD34D),
        background = Color(0xFFFFFBEB)
    )
}

@Composable
private fun RecipeGroupSection(
    title: String,
    titleColor: Color,
    subtitle: String,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    items: List<Recommendation>,
    emptyText: String,
    onSelectRecipe: (Recommendation) -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "${title}_rotation"
    )

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .animateContentSize()
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpanded)
            ) {
                Text(
                    text = title,
                    color = Color(0xFF111827),
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                    modifier = Modifier.weight(1f)
                )
                StatusCapsule(
                    text = "${items.size} món",
                    containerColor = Color(0xFFF9F5F0),
                    textColor = titleColor
                )
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    painter = painterResource(R.drawable.ic_expand),
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier
                        .size(14.dp)
                        .rotate(rotation)
                )
            }

            Text(
                text = subtitle,
                color = Color(0xFF6B7280),
                fontSize = 11.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                modifier = Modifier.padding(top = 6.dp)
            )

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    if (items.isEmpty()) {
                        Text(
                            text = emptyText,
                            color = Color(0xFF6B7280),
                            fontSize = 12.sp,
                            fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    } else {
                        items.take(8).forEach { recommendation ->
                            PickerItem(
                                recommendation = recommendation,
                                onSelect = { onSelectRecipe(recommendation) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddMoreDivider(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        HorizontalDivider(
            color = Color(0xFFE7DED1),
            modifier = Modifier.weight(1f)
        )
        Card(
            shape = RoundedCornerShape(999.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFF3D1A5)),
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text(
                text = "  THÊM MÓN  ",
                color = Color(0xFFD79A42),
                fontSize = 11.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
        HorizontalDivider(
            color = Color(0xFFE7DED1),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatusCapsule(
    text: String,
    containerColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 10.sp,
            fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun RecipeFooterIconButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    rotation: Float = 0f,
    containerColor: Color = Color.White,
    iconTint: Color = Color(0xFF9CA3AF)
) {
    Card(
        onClick = onClick,
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = modifier.size(38.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier
                    .size(16.dp)
                    .rotate(rotation)
            )
        }
    }
}

@Composable
private fun RecipeFooterButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.White,
    textColor: Color = Color(0xFF9CA3AF)
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun SheetRoundIconButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFFFFFFFF),
    contentColor: Color = Color(0xFF9CA3AF)
) {
    Card(
        onClick = onClick,
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = modifier.size(34.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = label,
                color = contentColor,
                fontSize = 11.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
            )
        }
    }
}

@Composable
private fun RecipeBrowserSheet(
    recipes: List<Recipe>,
    selectedRecipe: Recipe?,
    onSelectRecipe: (Recipe) -> Unit,
    onBackToList: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .padding(bottom = 18.dp)
            .verticalScroll(scrollState)
            .animateContentSize()
    ) {
        if (selectedRecipe == null) {
            Text(
                text = "Công thức trong phiên",
                color = Color(0xFF111827),
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
            )
            Text(
                text = "Chọn một món để xem chi tiết ngay trong sheet này.",
                color = Color(0xFF6B7280),
                fontSize = 13.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                modifier = Modifier.padding(top = 6.dp)
            )

            recipes.forEach { recipe ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clickable { onSelectRecipe(recipe) }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = recipe.recipeName,
                            color = Color(0xFF111827),
                            fontSize = 15.sp,
                            fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                        )
                        Text(
                            text = "Thời gian: ${recipe.cookingTime} • Khẩu phần: ${recipe.ration}",
                            color = Color(0xFF6B7280),
                            fontSize = 12.sp,
                            fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            return
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Quay lại",
                color = Color(0xFFF97316),
                fontSize = 13.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                modifier = Modifier.clickable(onClick = onBackToList)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Chi tiết công thức",
                color = Color(0xFF111827),
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
            )
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = selectedRecipe.recipeName,
                    color = Color(0xFF111827),
                    fontSize = 20.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                )
                Text(
                    text = "Thời gian: ${selectedRecipe.cookingTime} • Khẩu phần: ${selectedRecipe.ration}",
                    color = Color(0xFF6B7280),
                    fontSize = 13.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                    modifier = Modifier.padding(top = 6.dp)
                )

                Text(
                    text = "Nguyên liệu",
                    color = Color(0xFFF97316),
                    fontSize = 15.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                    modifier = Modifier.padding(top = 14.dp)
                )
                selectedRecipe.ingredients.forEach { ingredient ->
                    Text(
                        text = "• ${ingredient.ingredientName}: ${ingredient.weight} ${ingredient.unit}",
                        color = Color(0xFF374151),
                        fontSize = 13.sp,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Text(
                    text = "Các bước",
                    color = Color(0xFFF97316),
                    fontSize = 15.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                    modifier = Modifier.padding(top = 16.dp)
                )
                selectedRecipe.cookingSteps.sortedBy { it.indexStep ?: Int.MAX_VALUE }.forEachIndexed { index, step ->
                    Text(
                        text = "${index + 1}. ${step.stepContent}",
                        color = Color(0xFF374151),
                        fontSize = 13.sp,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PickerItem(
    recommendation: Recommendation,
    onSelect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = recommendation.recipeName,
                color = Color(0xFF111827),
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
            )
            Text(
                text = "Độ hoàn thiện: ${recommendation.completionRate ?: 0}%",
                color = Color(0xFF6B7280),
                fontSize = 12.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                modifier = Modifier.padding(top = 2.dp)
            )
            if (recommendation.missing.isNotEmpty()) {
                val missingText = recommendation.missing.joinToString { item -> item.ingredientName }
                Text(
                    text = "Thiếu: $missingText",
                    color = Color(0xFF92400E),
                    fontSize = 12.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            ) {
                ContextActionChip(
                    text = "Chọn món",
                    onClick = onSelect
                )
            }
        }
    }
}

@Composable
private fun ChatDietNotesSheet(
    notes: List<DietNote>,
    onRefresh: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (DietNote) -> Unit,
    onToggle: (DietNote) -> Unit,
    onDelete: (DietNote) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeCount = notes.count { it.isActive }

    Column(modifier = modifier.padding(bottom = 18.dp)) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Ghi chú ăn uống",
                color = Color(0xFF111827),
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Làm mới",
                    color = Color(0xFFF97316),
                    fontSize = 13.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                    modifier = Modifier.clickable(onClick = onRefresh)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Thêm",
                    color = Color(0xFF0369A1),
                    fontSize = 13.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                    modifier = Modifier.clickable(onClick = onAdd)
                )
            }
        }

        Text(
            text = "Đang bật: $activeCount/${notes.size}",
            color = Color(0xFF6B7280),
            fontSize = 12.sp,
            fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
            modifier = Modifier.padding(top = 6.dp)
        )

        if (notes.isEmpty()) {
            ChatEmptyStateCard(text = "Chưa có ghi chú nào. Hãy thêm ghi chú để Bepes hiểu bạn hơn.")
            return
        }

        notes.forEach { note ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = note.label,
                            color = Color(0xFF111827),
                            fontSize = 14.sp,
                            fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = note.isActive,
                            onCheckedChange = { onToggle(note) }
                        )
                    }
                    Text(
                        text = "Loại: ${noteTypeLabel(note.noteType)}",
                        color = Color(0xFF6B7280),
                        fontSize = 12.sp,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_regular))
                    )
                    if (note.keywords.isNotEmpty()) {
                        Text(
                            text = "Từ khóa: ${note.keywords.joinToString(", ")}",
                            color = Color(0xFF374151),
                            fontSize = 12.sp,
                            fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    if (!note.instruction.isNullOrBlank()) {
                        Text(
                            text = "Ghi chú: ${note.instruction}",
                            color = Color(0xFF374151),
                            fontSize = 12.sp,
                            fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        NoteActionTextButton(text = "Sửa", onClick = { onEdit(note) })
                        Spacer(modifier = Modifier.width(10.dp))
                        NoteActionTextButton(text = "Xóa", onClick = { onDelete(note) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatDietNoteEditorDialog(
    initial: DietNote?,
    userId: Int,
    onDismiss: () -> Unit,
    onSave: (DietNoteUpsertRequest) -> Unit
) {
    var label by remember(initial) { mutableStateOf(initial?.label.orEmpty()) }
    var noteType by remember(initial) { mutableStateOf(initial?.noteType ?: DietNoteType.ALLERGY) }
    var keywordsInput by remember(initial) { mutableStateOf(initial?.keywords?.joinToString(", ").orEmpty()) }
    var instruction by remember(initial) { mutableStateOf(initial?.instruction.orEmpty()) }
    var isActive by remember(initial) { mutableStateOf(initial?.isActive ?: true) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp)
            ) {
                Text(
                    text = if (initial == null) "Thêm ghi chú" else "Cập nhật ghi chú",
                    color = Color(0xFF111827),
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                )

                Text(
                    text = "Loại ghi chú",
                    color = Color(0xFF6B7280),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 10.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    DietNoteType.all.forEach { type ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (noteType == type) Color(0xFFFFEDD5) else Color(0xFFF3F4F6)
                            ),
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .clickable { noteType = type }
                        ) {
                            Text(
                                text = noteTypeLabel(type),
                                color = if (noteType == type) Color(0xFFF97316) else Color(0xFF374151),
                                fontSize = 12.sp,
                                fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                CustomTextField(
                    value = label,
                    onValueChange = { label = it },
                    placeholder = "Tiêu đề",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )

                CustomTextField(
                    value = keywordsInput,
                    onValueChange = { keywordsInput = it },
                    placeholder = "Từ khóa, ngăn cách bằng dấu phẩy",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                )

                CustomTextField(
                    value = instruction,
                    onValueChange = { instruction = it },
                    placeholder = "Hướng dẫn thêm",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                    Text(
                        text = "Đang bật ghi chú này",
                        color = Color(0xFF374151),
                        fontSize = 13.sp,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    NoteActionTextButton(text = "Hủy", onClick = onDismiss)
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val normalizedLabel = label.trim()
                            if (normalizedLabel.isEmpty()) return@Button

                            val keywords = keywordsInput
                                .split(",")
                                .map { it.trim() }
                                .filter { it.isNotBlank() }

                            onSave(
                                DietNoteUpsertRequest(
                                    noteId = initial?.noteId,
                                    userId = userId,
                                    noteType = noteType,
                                    label = normalizedLabel,
                                    keywords = keywords,
                                    instruction = instruction.trim().ifBlank { null },
                                    isActive = isActive,
                                    startAt = initial?.startAt,
                                    endAt = initial?.endAt
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316))
                    ) {
                        Text(
                            text = "Lưu",
                            color = Color.White,
                            fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteActionTextButton(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        color = Color(0xFFF97316),
        fontSize = 13.sp,
        fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
        modifier = Modifier.clickable(onClick = onClick)
    )
}

private fun noteTypeLabel(type: String): String {
    return when (type) {
        DietNoteType.ALLERGY -> "Dị ứng"
        DietNoteType.RESTRICTION -> "Hạn chế"
        DietNoteType.PREFERENCE -> "Sở thích"
        DietNoteType.HEALTH_NOTE -> "Sức khỏe"
        else -> type
    }
}

@Composable
private fun ChatEmptyStateCard(text: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFF6B7280),
            fontSize = 12.sp,
            fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun SessionDivider(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 8.dp)
    ) {
        HorizontalDivider(
            thickness = 1.dp,
            color = Color(0xFFE5E7EB),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = text,
            color = Color(0xFF9CA3AF),
            fontSize = 11.sp,
            fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        HorizontalDivider(
            thickness = 1.dp,
            color = Color(0xFFE5E7EB),
            modifier = Modifier.weight(1f)
        )
    }
}

private fun sessionDividerText(createdAt: String?): String {
    val normalized = createdAt?.replace("T", " ")?.trim().orEmpty()
    if (normalized.isBlank()) return "Cuộc trò chuyện mới"
    val shortTime = if (normalized.length > 16) normalized.take(16) else normalized
    return "Cuộc trò chuyện mới • $shortTime"
}

@Composable
private fun ChatBubble(
    message: ChatUiMessage,
    isActionRunning: Boolean,
    onRetry: () -> Unit,
    onPromptAction: (PendingResolveAction) -> Unit
) {
    if (message.kind == ChatMessageKind.PROMPT) {
        InlinePromptBubble(
            message = message,
            isActionRunning = isActionRunning,
            onPromptAction = onPromptAction
        )
        return
    }

    val isUser = message.role == ChatRole.USER
    val bubbleColor = if (isUser) Color(0xFFF97316) else Color(0xFFFFEDD5)
    val textColor = if (isUser) Color.White else Color(0xFF1F2937)
    val retryCountdown by produceState(initialValue = 0L, key1 = message.retryAvailableAt) {
        val target = message.retryAvailableAt
        if (target == null) return@produceState
        while (true) {
            val remaining = (target - System.currentTimeMillis()).coerceAtLeast(0L)
            value = remaining
            if (remaining <= 0L) break
            delay(1000L)
        }
    }

    Row(
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isFailed) Color(0xFFFEF2F2) else bubbleColor
            ),
            modifier = Modifier.fillMaxWidth(0.82f)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
                if (!isUser) {
                    Text(
                        text = "Bepes",
                        color = Color(0xFFF97316),
                        fontSize = 12.sp,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                    )
                }
                MarkdownText(
                    markdown = message.text,
                    color = if (message.isFailed) Color(0xFF7F1D1D) else textColor,
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                    modifier = Modifier.padding(top = if (isUser) 0.dp else 3.dp)
                )
                if (message.isFailed) {
                    Text(
                        text = message.errorText ?: "Tin nhắn gửi thất bại.",
                        color = Color(0xFFB91C1C),
                        fontSize = 12.sp,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    if (message.retryable) {
                        if (retryCountdown > 0L) {
                            Text(
                                text = "Gui lai sau ${retryCountdown / 1000}s",
                                color = Color(0xFF92400E),
                                fontSize = 12.sp,
                                fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        } else {
                            ContextActionChip(
                                text = "Gui lai",
                                onClick = onRetry,
                                containerColor = Color(0xFFF97316),
                                textColor = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InlinePromptBubble(
    message: ChatUiMessage,
    isActionRunning: Boolean,
    onPromptAction: (PendingResolveAction) -> Unit
) {
    val status = message.promptStatus ?: PromptStatus.PENDING
    Row(
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
            border = BorderStroke(1.dp, Color(0xFFFDBA74)),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
                Text(
                    text = "Bepes can ban xac nhan",
                    color = Color(0xFFF97316),
                    fontSize = 12.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                )
                MarkdownText(
                    markdown = message.text,
                    color = Color(0xFF1F2937),
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                    modifier = Modifier.padding(top = 4.dp)
                )
                if (!message.recipeName.isNullOrBlank()) {
                    Text(
                        text = "Mon: ${message.recipeName}",
                        color = Color(0xFF6B7280),
                        fontSize = 12.sp,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
                when (status) {
                    PromptStatus.RESOLVED -> {
                        Text(
                            text = "Da chon: ${message.selectedActionId.orEmpty()}",
                            color = Color(0xFF166534),
                            fontSize = 12.sp,
                            fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    PromptStatus.ERROR -> {
                        Text(
                            text = message.errorText ?: "Khong the xu ly prompt.",
                            color = Color(0xFFB91C1C),
                            fontSize = 12.sp,
                            fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    else -> Unit
                }

                if (status != PromptStatus.RESOLVED) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        message.promptActions.forEach { action ->
                            Button(
                                onClick = { onPromptAction(action) },
                                enabled = status != PromptStatus.LOADING && !isActionRunning,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (status == PromptStatus.LOADING) Color(0xFF9CA3AF) else Color(0xFFF97316)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = promptActionLabel(action.id, action.label),
                                    color = Color.White,
                                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun promptActionLabel(actionId: String, fallback: String): String {
    return when (actionId) {
        MealCompletionAction.MARK_DONE -> "Da xong mon"
        MealCompletionAction.MARK_SKIPPED -> "Bo qua mon"
        MealCompletionAction.CONTINUE_CURRENT -> "Chua xong, tiep tuc"
        MealCompletionAction.COMPLETE_SESSION -> "Dong phien"
        MealCompletionAction.KEEP_SESSION_OPEN -> "Giu phien mo"
        MealCompletionAction.ADD_MORE_RECIPES -> "Chon them mon"
        else -> fallback.ifBlank { actionId }
    }
}

@Composable
private fun MarkdownText(
    markdown: String,
    color: Color,
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier
) {
    val annotated = remember(markdown) { markdownToAnnotatedString(markdown) }
    Text(
        text = annotated,
        color = color,
        fontSize = fontSize,
        fontFamily = fontFamily,
        modifier = modifier
    )
}

private fun markdownToAnnotatedString(raw: String): AnnotatedString {
    val lines = raw.replace("\r\n", "\n").split('\n')
    return buildAnnotatedString {
        lines.forEachIndexed { index, line ->
            appendMarkdownLine(line)
            if (index < lines.lastIndex) append('\n')
        }
    }
}

private fun AnnotatedString.Builder.appendMarkdownLine(rawLine: String) {
    var line = rawLine.trimEnd()
    line = line.replace(Regex("^\\s{0,3}#{1,6}\\s+"), "")
    line = line.replace(Regex("^\\s*[-*]\\s+"), "• ")
    line = line.replace(Regex("^\\s*>\\s?"), "")

    val boldRegex = Regex("(\\*\\*|__)(.+?)(\\1)")
    var cursor = 0
    boldRegex.findAll(line).forEach { match ->
        val start = match.range.first
        val end = match.range.last + 1

        if (start > cursor) {
            append(stripLooseMarkdown(line.substring(cursor, start)))
        }

        val content = match.groupValues.getOrNull(2).orEmpty()
        if (content.isNotBlank()) {
            val styleStart = length
            append(content)
            addStyle(
                style = SpanStyle(fontWeight = FontWeight.SemiBold),
                start = styleStart,
                end = length
            )
        }
        cursor = end
    }

    if (cursor < line.length) {
        append(stripLooseMarkdown(line.substring(cursor)))
    }
}

private fun stripLooseMarkdown(input: String): String {
    return input
        .replace(Regex("\\*(\\S(?:.*?\\S)?)\\*"), "$1")
        .replace(Regex("_(\\S(?:.*?\\S)?)_"), "$1")
        .replace("**", "")
        .replace("__", "")
        .replace(Regex("(^|\\s)#(?=\\S)"), "$1")
}

@Composable
private fun NeedLoginCard(
    title: String,
    onSignIn: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = title,
                    color = Color(0xFF1F2937),
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Button(
                    onClick = onSignIn,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316))
                ) {
                    Text(
                        text = "Đăng nhập",
                        color = Color.White,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                    )
                }
            }
        }
    }
}
