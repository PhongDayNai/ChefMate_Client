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
import com.watb.chefmate.data.PantryItem
import com.watb.chefmate.data.PantryUpsertRequest
import com.watb.chefmate.ui.theme.CustomTextField
import com.watb.chefmate.ui.theme.Header
import com.watb.chefmate.viewmodel.AppFlowViewModel
import com.watb.chefmate.viewmodel.UserViewModel

@Composable
fun PantryScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    appFlowViewModel: AppFlowViewModel
) {
    val context = LocalContext.current
    val isLoggedIn by userViewModel.isLoggedIn.collectAsState()
    val user by userViewModel.user.collectAsState()
    val homeState by appFlowViewModel.homeState.collectAsState()

    var editingItem by remember { mutableStateOf<PantryItem?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    LaunchedEffect(isLoggedIn, user?.userId) {
        val userId = user?.userId
        if (isLoggedIn && userId != null) {
            appFlowViewModel.refreshPantry(userId)
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
            text = "Tủ lạnh cá nhân",
            leadingIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = "Quay lại",
                        tint = Color.White
                    )
                }
            }
        )

        if (!isLoggedIn || user == null) {
            NeedLoginCard(
                title = "Bạn cần đăng nhập để quản lý tủ lạnh",
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
                    text = "Nguyên liệu hiện có",
                    color = Color(0xFF111827),
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                )
                IconButton(
                    onClick = {
                        editingItem = null
                        showEditor = true
                    },
                    modifier = Modifier
                        .background(Color(0xFFF97316), CircleShape)
                        .size(34.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = "Thêm",
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
                items(homeState.pantryItems, key = { item -> item.pantryItemId ?: item.ingredientName }) { item ->
                    PantryItemCard(
                        item = item,
                        onEdit = {
                            editingItem = item
                            showEditor = true
                        },
                        onDelete = {
                            val pantryItemId = item.pantryItemId
                            if (pantryItemId != null) {
                                appFlowViewModel.deletePantryItem(userId = item.userId, pantryItemId = pantryItemId)
                            }
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(90.dp)) }
            }
        }
    }

    if (showEditor && isLoggedIn && user != null) {
        PantryEditorDialog(
            initial = editingItem,
            userId = user!!.userId,
            onDismiss = { showEditor = false },
            onSave = { request ->
                appFlowViewModel.upsertPantryItem(request)
                showEditor = false
            }
        )
    }
}

@Composable
private fun PantryItemCard(
    item: PantryItem,
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
            Text(
                text = item.ingredientName,
                color = Color(0xFF111827),
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
            )
            Text(
                text = "Số lượng: ${item.quantity} ${item.unit}",
                color = Color(0xFF374151),
                fontSize = 13.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                modifier = Modifier.padding(top = 5.dp)
            )
            if (!item.expiresAt.isNullOrBlank()) {
                Text(
                    text = "HSD: ${item.expiresAt}",
                    color = Color(0xFF6B7280),
                    fontSize = 13.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                    modifier = Modifier.padding(top = 3.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                ActionTextButton(text = "Sửa", onClick = onEdit)
                Spacer(modifier = Modifier.width(10.dp))
                ActionTextButton(text = "Xóa", onClick = onDelete)
            }
        }
    }
}

@Composable
private fun PantryEditorDialog(
    initial: PantryItem?,
    userId: Int,
    onDismiss: () -> Unit,
    onSave: (PantryUpsertRequest) -> Unit
) {
    var ingredientName by remember(initial) { mutableStateOf(initial?.ingredientName.orEmpty()) }
    var quantityInput by remember(initial) { mutableStateOf(initial?.quantity?.toString().orEmpty()) }
    var unit by remember(initial) { mutableStateOf(initial?.unit.orEmpty()) }
    var expiresAt by remember(initial) { mutableStateOf(initial?.expiresAt.orEmpty()) }

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
                    text = if (initial == null) "Thêm nguyên liệu" else "Cập nhật nguyên liệu",
                    color = Color(0xFF111827),
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                )

                CustomTextField(
                    value = ingredientName,
                    onValueChange = { ingredientName = it },
                    placeholder = "Tên nguyên liệu",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    CustomTextField(
                        value = quantityInput,
                        onValueChange = { quantityInput = it },
                        placeholder = "Số lượng",
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 6.dp)
                    )
                    CustomTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        placeholder = "Đơn vị",
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 6.dp)
                    )
                }

                CustomTextField(
                    value = expiresAt,
                    onValueChange = { expiresAt = it },
                    placeholder = "Hạn sử dụng (YYYY-MM-DD)",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    ActionTextButton(text = "Hủy", onClick = onDismiss)
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val quantity = quantityInput.trim().toDoubleOrNull() ?: return@Button
                            if (quantity < 0.0) return@Button
                            if (ingredientName.trim().isEmpty()) return@Button
                            if (unit.trim().isEmpty()) return@Button

                            onSave(
                                PantryUpsertRequest(
                                    pantryItemId = initial?.pantryItemId,
                                    userId = userId,
                                    ingredientName = ingredientName.trim(),
                                    quantity = quantity,
                                    unit = unit.trim(),
                                    expiresAt = expiresAt.trim().ifBlank { null }
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
