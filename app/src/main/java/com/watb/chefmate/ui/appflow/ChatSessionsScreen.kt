package com.watb.chefmate.ui.appflow

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.watb.chefmate.R
import com.watb.chefmate.data.ChatSession
import com.watb.chefmate.ui.theme.CustomTextField
import com.watb.chefmate.ui.theme.Header
import com.watb.chefmate.viewmodel.AppFlowViewModel
import com.watb.chefmate.viewmodel.UserViewModel

@Composable
fun ChatSessionsScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    appFlowViewModel: AppFlowViewModel
) {
    val context = LocalContext.current
    val isLoggedIn by userViewModel.isLoggedIn.collectAsState()
    val user by userViewModel.user.collectAsState()
    val chatState by appFlowViewModel.chatState.collectAsState()

    var editingSession by remember { mutableStateOf<ChatSession?>(null) }
    var newTitle by remember { mutableStateOf("") }

    LaunchedEffect(isLoggedIn, user?.userId) {
        if (isLoggedIn && user != null) {
            appFlowViewModel.loadSessions(user!!.userId)
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
            text = "Lịch sử Bepes",
            leadingIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }
        )

        if (!isLoggedIn || user == null) {
            NeedLoginCard(
                title = "Bạn cần đăng nhập để xem lịch sử trò chuyện",
                onSignIn = { navController.navigate("signIn") }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                items(chatState.sessions, key = { it.chatSessionId ?: it.title.orEmpty() }) { session ->
                    SessionCard(
                        session = session,
                        onOpen = {
                            val sessionId = session.chatSessionId
                            if (sessionId != null) {
                                appFlowViewModel.openSession(user!!.userId, sessionId)
                                navController.navigate("bepesChat?recipeId=-1")
                            }
                        },
                        onRename = {
                            editingSession = session
                            newTitle = session.title.orEmpty()
                        },
                        onDelete = {
                            val sessionId = session.chatSessionId
                            if (sessionId != null) {
                                appFlowViewModel.deleteSession(user!!.userId, sessionId)
                            }
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(90.dp)) }
            }
        }
    }

    if (editingSession != null && user != null) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { editingSession = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Đổi tiêu đề phiên",
                        color = Color(0xFF111827),
                        fontSize = 18.sp,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                    )

                    CustomTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        placeholder = "Tiêu đề phiên",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp)
                    ) {
                        ActionTextButton(text = "Hủy") { editingSession = null }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                val sessionId = editingSession?.chatSessionId
                                if (sessionId != null && newTitle.trim().isNotEmpty()) {
                                    appFlowViewModel.renameSession(
                                        userId = user!!.userId,
                                        chatSessionId = sessionId,
                                        title = newTitle.trim()
                                    )
                                }
                                editingSession = null
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
}

@Composable
private fun SessionCard(
    session: ChatSession,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable(onClick = onOpen)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = session.title?.ifBlank { "Phiên Bepes" } ?: "Phiên Bepes",
                color = Color(0xFF111827),
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
            )
            Text(
                text = "Mã phiên: ${session.chatSessionId ?: "-"}",
                color = Color(0xFF6B7280),
                fontSize = 13.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                modifier = Modifier.padding(top = 4.dp)
            )
            if (!session.updatedAt.isNullOrBlank()) {
                Text(
                    text = "Cập nhật: ${session.updatedAt}",
                    color = Color(0xFF6B7280),
                    fontSize = 13.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                ActionTextButton(text = "Mở", onClick = onOpen)
                Spacer(modifier = Modifier.width(12.dp))
                ActionTextButton(text = "Đổi tên", onClick = onRename)
                Spacer(modifier = Modifier.width(12.dp))
                ActionTextButton(text = "Xóa", onClick = onDelete)
            }
        }
    }
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

@Composable
private fun ActionTextButton(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        color = Color(0xFFF97316),
        fontSize = 13.sp,
        fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
        modifier = Modifier.clickable(onClick = onClick)
    )
}
