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
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
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
import com.watb.chefmate.data.ChatRole
import com.watb.chefmate.data.ChatUiMessage
import com.watb.chefmate.data.DietNote
import com.watb.chefmate.data.DietNoteType
import com.watb.chefmate.data.DietNoteUpsertRequest
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
    var selectedRecipeForConfirm by remember { mutableStateOf<Recommendation?>(null) }
    var isResolvingRecipe by remember { mutableStateOf(false) }

    val allRecommendations = remember(homeState.recommendations, homeState.readyToCook, homeState.almostReady) {
        val merged = homeState.recommendations + homeState.readyToCook + homeState.almostReady
        merged
            .filter { it.recipeId > 0 }
            .distinctBy { it.recipeId }
    }

    val currentActiveRecipeId = chatState.currentSession?.activeRecipeId
    val selectedRecommendation = remember(currentActiveRecipeId, allRecommendations) {
        allRecommendations.firstOrNull { it.recipeId == currentActiveRecipeId }
    }

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

    LaunchedEffect(isLoggedIn, user?.userId, recipeId) {
        if (isLoggedIn && user != null) {
            appFlowViewModel.refreshHomeContext(user!!.userId)
            appFlowViewModel.bootstrapUnifiedTimeline(
                userId = user!!.userId,
                activeRecipeId = recipeId.takeIf { it > 0 }
            )
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

    chatState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            appFlowViewModel.clearChatError()
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
            }
        )

        if (!isLoggedIn || user == null) {
            NeedLoginCard(
                title = "Bạn cần đăng nhập để trò chuyện cùng Bepes",
                onSignIn = { navController.navigate("signIn") }
            )
        } else {
            SessionContextSection(
                selectedRecipe = selectedRecommendation,
                activeRecipeId = currentActiveRecipeId,
                activeDietNotes = activeDietNotes,
                onOpenPicker = {
                    appFlowViewModel.refreshRecommendations(user!!.userId)
                    showRecipePicker = true
                },
                onOpenNotes = {
                    appFlowViewModel.refreshDietNotes(user!!.userId)
                    showDietNotesSheet = true
                },
                onComplete = {
                    if (chatState.currentSessionId != null) {
                        showCompleteDialog = true
                    } else {
                        Toast.makeText(context, "Chưa có cuộc trò chuyện để hoàn thành", Toast.LENGTH_SHORT).show()
                    }
                },
                onOpenRecipe = {
                    val selected = selectedRecommendation
                    if (selected == null || selected.recipeName.isBlank()) {
                        Toast.makeText(context, "Hãy chọn món trước khi mở công thức", Toast.LENGTH_SHORT).show()
                    } else {
                        coroutineScope.launch {
                            isResolvingRecipe = true
                            val response = ApiClient.searchRecipe(
                                recipeName = selected.recipeName,
                                userId = user?.userId
                            )
                            val matchedRecipe = response?.data
                                ?.firstOrNull { recipe -> recipe.recipeId == selected.recipeId }
                                ?: response?.data?.firstOrNull()

                            isResolvingRecipe = false
                            if (matchedRecipe != null) {
                                onOpenRecipe(matchedRecipe)
                            } else {
                                Toast.makeText(context, "Không tìm thấy công thức phù hợp", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                isResolvingRecipe = isResolvingRecipe,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )

            val sendCurrentMessage = {
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
                if (chatState.loadingTimeline) {
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

                    ChatBubble(message)
                }

                if (chatState.sending) {
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
                    onValueChange = { messageInput = it },
                    placeholder = "Nhắn Bepes...",
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { sendCurrentMessage() }),
                    trailingIcon = {
                        IconButton(
                            onClick = { sendCurrentMessage() }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_send),
                                contentDescription = "Gửi",
                                tint = if (chatState.sending) Color(0xFF9CA3AF) else Color(0xFFF97316)
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
                readyToCook = readyToCook,
                almostReady = almostReady,
                unavailable = unavailable,
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
                    text = "Bạn đã nấu xong món này chưa?",
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                )
            },
            text = {
                Text(
                    text = "Nếu đã xong, Bepes sẽ ghi nhận hoàn thành món và bắt đầu cuộc trò chuyện mới.",
                    fontFamily = FontFamily(Font(resId = R.font.roboto_regular))
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCompleteDialog = false
                        appFlowViewModel.completeCurrentSession(user!!.userId)
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
    selectedRecipe: Recommendation?,
    activeRecipeId: Int?,
    activeDietNotes: List<DietNote>,
    onOpenPicker: () -> Unit,
    onOpenNotes: () -> Unit,
    onOpenRecipe: () -> Unit,
    onComplete: () -> Unit,
    isResolvingRecipe: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            val recipeText = when {
                selectedRecipe != null -> selectedRecipe.recipeName
                activeRecipeId != null && activeRecipeId > 0 -> "Món đã chọn #$activeRecipeId"
                else -> "Chưa chọn món cho cuộc trò chuyện hiện tại"
            }
            Text(
                text = "Món đang chọn: $recipeText",
                color = Color(0xFF1F2937),
                fontSize = 13.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                modifier = Modifier.padding(top = 4.dp)
            )

            val noteSummary = if (activeDietNotes.isEmpty()) "Không có" else "Có"

            Text(
                text = "Ghi chú ăn uống: $noteSummary",
                color = Color(0xFF374151),
                fontSize = 12.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                modifier = Modifier.padding(top = 4.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                ContextActionChip(
                    text = "Chọn món",
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
                    containerColor = Color(0xFFD1FAE5),
                    textColor = Color(0xFF166534),
                    borderColor = Color(0xFF86EFAC),
                    modifier = Modifier.weight(1f)
                )
            }
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
private fun RecipePickerSheet(
    isLoading: Boolean,
    readyToCook: List<Recommendation>,
    almostReady: List<Recommendation>,
    unavailable: List<Recommendation>,
    onRefresh: () -> Unit,
    onSelectRecipe: (Recommendation) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandReady by remember { mutableStateOf(true) }
    var expandAlmost by remember { mutableStateOf(true) }
    var expandUnavailable by remember { mutableStateOf(true) }

    Column(modifier = modifier.padding(bottom = 18.dp)) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Chọn món cho Bepes",
                color = Color(0xFF111827),
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
            )
            Text(
                text = "Làm mới",
                color = Color(0xFFF97316),
                fontSize = 13.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                modifier = Modifier.clickable(onClick = onRefresh)
            )
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

        RecipeGroupSection(
            title = "Sẵn sàng để nấu",
            titleColor = Color(0xFF15803D),
            expanded = expandReady,
            onToggleExpanded = { expandReady = !expandReady },
            items = readyToCook,
            emptyText = "Chưa có món sẵn sàng",
            onSelectRecipe = onSelectRecipe
        )

        RecipeGroupSection(
            title = "Thiếu một chút",
            titleColor = Color(0xFFB45309),
            expanded = expandAlmost,
            onToggleExpanded = { expandAlmost = !expandAlmost },
            items = almostReady,
            emptyText = "Chưa có món gần hoàn thiện",
            onSelectRecipe = onSelectRecipe
        )

        RecipeGroupSection(
            title = "Không có sẵn đồ",
            titleColor = Color(0xFF6B7280),
            expanded = expandUnavailable,
            onToggleExpanded = { expandUnavailable = !expandUnavailable },
            items = unavailable,
            emptyText = "Chưa có dữ liệu ở nhóm này",
            onSelectRecipe = onSelectRecipe
        )
    }
}

@Composable
private fun RecipeGroupSection(
    title: String,
    titleColor: Color,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    items: List<Recommendation>,
    emptyText: String,
    onSelectRecipe: (Recommendation) -> Unit
) {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpanded)
            ) {
                Text(
                    text = title,
                    color = titleColor,
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    painter = painterResource(R.drawable.ic_expand),
                    contentDescription = null,
                    tint = Color(0xFF6B7280),
                    modifier = Modifier
                        .size(14.dp)
                        .rotate(if (expanded) 180f else 0f)
                )
            }

            if (expanded) {
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
private fun ChatBubble(message: ChatUiMessage) {
    val isUser = message.role == ChatRole.USER
    val bubbleColor = if (isUser) Color(0xFFF97316) else Color(0xFFFFEDD5)
    val textColor = if (isUser) Color.White else Color(0xFF1F2937)

    Row(
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            modifier = Modifier
                .fillMaxWidth(0.82f)
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
                    color = textColor,
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                    modifier = Modifier.padding(top = if (isUser) 0.dp else 3.dp)
                )
            }
        }
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
