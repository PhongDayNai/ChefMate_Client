package com.watb.chefmate.ui.appflow

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.DpOffset
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
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.format.DateTimeFormatter

private enum class PantrySortOption {
    ALL,
    NEWEST,
    OLDEST,
    EXPIRED,
    NOT_EXPIRED
}

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
    var selectedSortOption by remember { mutableStateOf(PantrySortOption.ALL) }

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
            text = stringResource(R.string.pantry_title)
        )

        if (!isLoggedIn || user == null) {
            NeedLoginCard(
                title = stringResource(R.string.pantry_login_required_title),
                onSignIn = { navController.navigate("signIn") }
            )
        } else {
            val visiblePantryItems = remember(homeState.pantryItems, selectedSortOption) {
                buildVisiblePantryItems(
                    items = homeState.pantryItems,
                    sortOption = selectedSortOption
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Text(
                    text = stringResource(R.string.pantry_list_title),
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
                        contentDescription = stringResource(R.string.common_add),
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            PantrySortDropdown(
                selectedOption = selectedSortOption,
                onOptionSelected = { selectedSortOption = it },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(visiblePantryItems, key = { item -> item.pantryItemId ?: item.ingredientName }) { item ->
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
    var showMenu by remember { mutableStateOf(false) }

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
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.ingredientName,
                    color = Color(0xFF111827),
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                    modifier = Modifier.weight(1f)
                )
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Text(
                            text = "⋮",
                            color = Color(0xFF6B7280),
                            fontSize = 18.sp,
                            fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.common_edit)) },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.common_delete)) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }
            Text(
                text = stringResource(R.string.pantry_quantity, item.quantity.toString(), item.unit),
                color = Color(0xFF374151),
                fontSize = 13.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                modifier = Modifier.padding(top = 5.dp)
            )
            if (!item.expiresAt.isNullOrBlank()) {
                Text(
                    text = stringResource(R.string.pantry_expiry, formatPantryExpiryDisplay(item.expiresAt)),
                    color = Color(0xFF6B7280),
                    fontSize = 13.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun PantrySortDropdown(
    selectedOption: PantrySortOption,
    onOptionSelected: (PantrySortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var rowWidth by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.pantry_sort_label),
            color = Color(0xFF6B7280),
            fontSize = 13.sp,
            fontFamily = FontFamily(Font(resId = R.font.roboto_medium))
        )
        Box(modifier = Modifier.padding(top = 6.dp, start = 24.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .onGloballyPositioned { layoutCoordinates ->
                        rowWidth = layoutCoordinates.size.width
                    }
            ) {
                Text(
                    text = sortOptionLabel(selectedOption),
                    color = Color(0xFF111827),
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_medium))
                )
                Text(
                    text = "▼",
                    color = Color(0xFF6B7280),
                    fontSize = 11.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                offset = DpOffset(x = 72.dp, y = 0.dp),
                modifier = Modifier
                    .width(with(density) { rowWidth.toDp() })
                    .background(Color.White)
            ) {
                PantrySortOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = sortOptionLabel(option),
                                fontSize = 13.sp,
                            )
                        },
                        onClick = {
                            expanded = false
                            onOptionSelected(option)
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .background(Color.White)
                    )
                }
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
    val context = LocalContext.current
    var ingredientName by remember(initial) { mutableStateOf(initial?.ingredientName.orEmpty()) }
    var quantityInput by remember(initial) { mutableStateOf(initial?.quantity?.toString().orEmpty()) }
    var unit by remember(initial) { mutableStateOf(initial?.unit.orEmpty()) }
    var expiresAt by remember(initial) {
        mutableStateOf(formatPantryExpiryForStorage(parsePantryDate(initial?.expiresAt)))
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF8)),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Text(
                    text = if (initial == null) stringResource(R.string.pantry_editor_add_title) else stringResource(R.string.pantry_editor_update_title),
                    color = Color(0xFF111827),
                    fontSize = 20.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                )
                Divider(
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.padding(top = 14.dp)
                )

                FormLabel(
                    text = stringResource(R.string.pantry_editor_name_label),
                    modifier = Modifier.padding(top = 16.dp)
                )
                CustomTextField(
                    value = ingredientName,
                    onValueChange = { ingredientName = it },
                    placeholder = stringResource(R.string.pantry_editor_name_placeholder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )

                FormLabel(
                    text = stringResource(R.string.pantry_editor_quantity_label),
                    modifier = Modifier.padding(top = 14.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    CustomTextField(
                        value = quantityInput,
                        onValueChange = { quantityInput = it },
                        placeholder = stringResource(R.string.pantry_editor_quantity_placeholder),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 6.dp)
                    )
                    CustomTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        placeholder = stringResource(R.string.pantry_editor_unit_placeholder),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 6.dp)
                    )
                }

                FormLabel(
                    text = stringResource(R.string.pantry_editor_expiry_label),
                    modifier = Modifier
                        .padding(top = 14.dp)
                )
                ExpiryPickerField(
                    value = formatPantryExpiryDisplay(expiresAt),
                    placeholder = stringResource(R.string.pantry_editor_expiry_placeholder),
                    onClick = {
                        val initialDate = parsePantryDate(expiresAt) ?: LocalDate.now()
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                expiresAt = formatPantryExpiryForStorage(
                                    LocalDate.of(year, month + 1, dayOfMonth)
                                )
                            },
                            initialDate.year,
                            initialDate.monthValue - 1,
                            initialDate.dayOfMonth
                        ).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp, start = 24.dp, end = 24.dp)
                ) {
                    ActionTextButton(text = stringResource(R.string.common_cancel), onClick = onDismiss)
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

@Composable
private fun FormLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = Color(0xFF374151),
        fontSize = 13.sp,
        fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
        modifier = modifier
    )
}

@Composable
private fun ExpiryPickerField(
    value: String,
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .shadowCardBackground()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_clock_filled),
            contentDescription = null,
            tint = Color(0xFFF97316),
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = value.ifBlank { placeholder },
            color = if (value.isBlank()) Color(0xFFADAEBC) else Color(0xFF111827),
            fontSize = 15.sp,
            fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp)
        )
    }
}

@Composable
private fun sortOptionLabel(option: PantrySortOption): String {
    return when (option) {
        PantrySortOption.ALL -> stringResource(R.string.pantry_sort_all)
        PantrySortOption.NEWEST -> stringResource(R.string.pantry_sort_newest)
        PantrySortOption.OLDEST -> stringResource(R.string.pantry_sort_oldest)
        PantrySortOption.EXPIRED -> stringResource(R.string.pantry_sort_expired)
        PantrySortOption.NOT_EXPIRED -> stringResource(R.string.pantry_sort_not_expired)
    }
}

private fun buildVisiblePantryItems(
    items: List<PantryItem>,
    sortOption: PantrySortOption
): List<PantryItem> {
    val today = LocalDate.now()

    return when (sortOption) {
        PantrySortOption.ALL -> items.sortedWith(
            compareBy<PantryItem> { item ->
                parsePantryDate(item.expiresAt) ?: LocalDate.MAX
            }.thenBy { item ->
                item.ingredientName.lowercase()
            }
        )

        PantrySortOption.NEWEST -> items.sortedWith(
            compareByDescending<PantryItem> { item ->
                parsePantryDate(item.expiresAt)
            }.thenBy { item ->
                item.ingredientName.lowercase()
            }
        )

        PantrySortOption.OLDEST -> items.sortedWith(
            compareBy<PantryItem> { item ->
                parsePantryDate(item.expiresAt) ?: LocalDate.MAX
            }.thenBy { item ->
                item.ingredientName.lowercase()
            }
        )

        PantrySortOption.EXPIRED -> items.filter { item ->
            val date = parsePantryDate(item.expiresAt) ?: return@filter false
            date.isBefore(today)
        }.sortedWith(
            compareBy<PantryItem> { item -> parsePantryDate(item.expiresAt) }
                .thenBy { item -> item.ingredientName.lowercase() }
        )

        PantrySortOption.NOT_EXPIRED -> items.filter { item ->
            val date = parsePantryDate(item.expiresAt)
            date == null || !date.isBefore(today)
        }.sortedWith(
            compareBy<PantryItem> { item ->
                parsePantryDate(item.expiresAt) ?: LocalDate.MAX
            }.thenBy { item ->
                item.ingredientName.lowercase()
            }
        )
    }
}

private fun parsePantryDate(value: String?): LocalDate? {
    if (value.isNullOrBlank()) return null
    val normalized = value.trim()
    val datePatterns = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ofPattern("yyyy-MM-dd")
    )
    datePatterns.forEach { formatter ->
        try {
            return LocalDate.parse(normalized.take(10), formatter)
        } catch (_: DateTimeParseException) {
        }
    }
    return null
}

private fun formatPantryExpiryDisplay(value: String?): String {
    val date = parsePantryDate(value) ?: return value.orEmpty()
    return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
}

private fun formatPantryExpiryForStorage(value: LocalDate?): String {
    if (value == null) return ""
    return value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}

private fun Modifier.shadowCardBackground(): Modifier {
    return this
        .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
        .background(Color.White, RoundedCornerShape(16.dp))
}
