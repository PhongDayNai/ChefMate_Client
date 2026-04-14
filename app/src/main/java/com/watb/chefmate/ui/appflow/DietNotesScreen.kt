package com.watb.chefmate.ui.appflow

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.watb.chefmate.R
import com.watb.chefmate.data.DietNote
import com.watb.chefmate.data.DietNoteType
import com.watb.chefmate.data.DietNoteUpsertRequest
import com.watb.chefmate.ui.theme.CustomTextField
import com.watb.chefmate.ui.theme.Header
import com.watb.chefmate.ui.theme.HeaderBackButton
import com.watb.chefmate.viewmodel.AppFlowViewModel
import com.watb.chefmate.viewmodel.UserViewModel

@Composable
fun DietNotesScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    appFlowViewModel: AppFlowViewModel
) {
    val context = LocalContext.current
    val isLoggedIn by userViewModel.isLoggedIn.collectAsState()
    val user by userViewModel.user.collectAsState()
    val homeState by appFlowViewModel.homeState.collectAsState()

    var editingNote by remember { mutableStateOf<DietNote?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    LaunchedEffect(isLoggedIn, user?.userId) {
        val userId = user?.userId
        if (isLoggedIn && userId != null) {
            appFlowViewModel.refreshDietNotes(userId)
        }
    }

    homeState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Header(
            text = stringResource(R.string.diet_notes_title),
            leadingIcon = {
                HeaderBackButton(onClick = { navController.popBackStack() })
            }
        )

        if (!isLoggedIn || user == null) {
            NeedLoginCard(
                title = stringResource(R.string.diet_notes_login_required_title),
                onSignIn = { navController.navigate("signIn") }
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Text(
                    text = stringResource(R.string.diet_notes_list_title),
                    color = Color(0xFF111827),
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                )
                IconButton(
                    onClick = {
                        editingNote = null
                        showEditor = true
                    },
                    modifier = Modifier
                        .background(Color(0xFFF97316), CircleShape)
                        .size(34.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = stringResource(R.string.common_add),
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(homeState.dietNotes, key = { note -> note.noteId ?: note.label }) { note ->
                    DietNoteCard(
                        note = note,
                        onToggleActive = {
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
                        onEdit = {
                            editingNote = note
                            showEditor = true
                        },
                        onDelete = {
                            val noteId = note.noteId
                            if (noteId != null) {
                                appFlowViewModel.deleteDietNote(userId = note.userId, noteId = noteId)
                            }
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(90.dp)) }
            }
        }
    }

    if (showEditor && isLoggedIn && user != null) {
        DietNoteEditorDialog(
            initial = editingNote,
            userId = user!!.userId,
            onDismiss = { showEditor = false },
            onSave = { request ->
                appFlowViewModel.upsertDietNote(request)
                showEditor = false
            }
        )
    }
}

@Composable
private fun DietNoteCard(
    note: DietNote,
    onToggleActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = note.label,
                    color = Color(0xFF111827),
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                )
                Switch(
                    checked = note.isActive,
                    onCheckedChange = { onToggleActive() }
                )
            }

            Text(
                text = stringResource(R.string.diet_notes_type, note.noteType),
                color = Color(0xFF6B7280),
                fontSize = 13.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_regular))
            )

            if (note.keywords.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.diet_notes_keywords, note.keywords.joinToString(", ")),
                    color = Color(0xFF374151),
                    fontSize = 13.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            if (!note.instruction.isNullOrBlank()) {
                Text(
                    text = stringResource(R.string.diet_notes_note, note.instruction ?: ""),
                    color = Color(0xFF374151),
                    fontSize = 13.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                ActionTextButton(text = stringResource(R.string.common_edit), onClick = onEdit)
                Spacer(modifier = Modifier.width(10.dp))
                ActionTextButton(text = stringResource(R.string.common_delete), onClick = onDelete)
            }
        }
    }
}

@Composable
private fun DietNoteEditorDialog(
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
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp)
            ) {
                Text(
                    text = if (initial == null) stringResource(R.string.diet_notes_editor_add_title) else stringResource(R.string.diet_notes_editor_update_title),
                    color = Color(0xFF111827),
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                )

                Text(
                    text = stringResource(R.string.diet_notes_editor_type),
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
                                text = type,
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
                    placeholder = stringResource(R.string.diet_notes_editor_title_placeholder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )

                CustomTextField(
                    value = keywordsInput,
                    onValueChange = { keywordsInput = it },
                    placeholder = stringResource(R.string.diet_notes_editor_keywords_placeholder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                )

                CustomTextField(
                    value = instruction,
                    onValueChange = { instruction = it },
                    placeholder = stringResource(R.string.diet_notes_editor_instruction_placeholder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.diet_notes_editor_active),
                        color = Color(0xFF374151),
                        fontSize = 13.sp,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_regular))
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    ActionTextButton(text = stringResource(R.string.common_cancel), onClick = onDismiss)
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (label.trim().isEmpty()) return@Button
                            val keywords = keywordsInput
                                .split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }

                            onSave(
                                DietNoteUpsertRequest(
                                    noteId = initial?.noteId,
                                    userId = userId,
                                    noteType = noteType,
                                    label = label.trim(),
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
                            text = stringResource(R.string.common_save),
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
                        text = stringResource(R.string.common_sign_in),
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
