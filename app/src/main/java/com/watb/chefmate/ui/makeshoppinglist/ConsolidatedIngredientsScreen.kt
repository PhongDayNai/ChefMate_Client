package com.watb.chefmate.ui.makeshoppinglist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.watb.chefmate.R
import com.watb.chefmate.database.AppDatabase
import com.watb.chefmate.database.entities.ShoppingTimeEntity
import com.watb.chefmate.helper.CommonHelper.parseIngredientName
import com.watb.chefmate.repository.ShoppingTimeRepository
import com.watb.chefmate.ui.recipe.bottomDashedBorder
import com.watb.chefmate.ui.theme.Header
import com.watb.chefmate.viewmodel.ShoppingTimeViewModel

@Composable
fun ConsolidatedIngredientsScreen(
    navController: NavController,
    shoppingTimeId: Int,
    shoppingTimeViewModel: ShoppingTimeViewModel = viewModel(
        factory = ShoppingTimeViewModel.Factory(
            ShoppingTimeRepository(AppDatabase.getDatabase(LocalContext.current).shoppingTimeDao())
        )
    )
) {
    var editIndex by remember { mutableStateOf<Int?>(null) }
    var editName by remember { mutableStateOf("") }
    var editWeight by remember { mutableStateOf("") }
    var editUnit by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    var shoppingTime by remember { mutableStateOf<ShoppingTimeEntity?>(null) }

    // ✅ State cho list status
    var ingredientStatus by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(shoppingTimeId) {
        shoppingTime = shoppingTimeViewModel.getShoppingTimeById(shoppingTimeId)
        ingredientStatus = shoppingTime?.buyingStatuses?.split(";;;") ?: emptyList()
    }

    fun updateStatus(index: Int, newStatus: String) {
        ingredientStatus = ingredientStatus.toMutableList().apply {
            this[index] = newStatus
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF))
            .safeDrawingPadding()
    ) {
        Header(
            text = "Danh sách mua sắm",
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = "back",
                    tint = Color.White
                )
            }
        )

        if (shoppingTime == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Đang tải dữ liệu...", fontSize = 16.sp)
            }
        } else {
            val ingredientNames = shoppingTime!!.ingredientNames.split(";;;")
            val ingredientWeights = shoppingTime!!.ingredientWeights.split(";;;")
            val ingredientUnits = shoppingTime!!.ingredientUnits.split(";;;")

            // ✅ Sắp xếp: normal -> bought -> couldNotBuy
            val orderedIndices = ingredientNames.indices.sortedWith(compareBy(
                { statusOrder(ingredientStatus.getOrNull(it) ?: "") }
            ))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(orderedIndices) { index ->
                    val name = ingredientNames[index]
                    val weight = ingredientWeights.getOrNull(index) ?: ""
                    val unit = ingredientUnits.getOrNull(index) ?: ""
                    val status = ingredientStatus.getOrNull(index) ?: ""

                    IngredientItem(
                        name = name.parseIngredientName(),
                        weight = weight,
                        unit = unit,
                        status = status,
                        onCheckedChange = {
                            val newStatus = if (status == "bought") "waiting" else "bought"
                            updateStatus(index, newStatus)
                        },
                        onCouldNotBuyClick = {
                            val newStatus = if (status == "couldNotBuy") "waiting" else "couldNotBuy"
                            updateStatus(index, newStatus)
                        },
                        onEditClick = {
                            editIndex = index
                            editName = name
                            editWeight = weight
                            editUnit = unit
                        }
                    )
                }
            }
            if (editIndex != null) {
                CustomEditIngredientDialog(
                    name = editName,
                    onNameChange = { editName = it },
                    weight = editWeight,
                    onWeightChange = { editWeight = it },
                    unit = editUnit,
                    onUnitChange = { editUnit = it },
                    onConfirm = {
                        // Lưu chỉnh sửa
                        val idx = editIndex!!
                        val newNames = ingredientNames.toMutableList()
                        val newWeights = ingredientWeights.toMutableList()
                        val newUnits = ingredientUnits.toMutableList()

                        newNames[idx] = editName
                        newWeights[idx] = editWeight
                        newUnits[idx] = editUnit

                        shoppingTime = shoppingTime!!.copy(
                            ingredientNames = newNames.joinToString(";;;"),
                            ingredientWeights = newWeights.joinToString(";;;"),
                            ingredientUnits = newUnits.joinToString(";;;")
                        )

                        editIndex = null
                    },
                    onDismiss = {
                        editIndex = null
                    }
                )
            }

        }
        Row {
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFA1A1A1),
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .padding(30.dp)
            ) {
                Text(
                    text = "Bổ sung"
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF97518),
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .padding(30.dp)
            ) {
                Text(
                    text = "Hoàn thành"
                )
            }
        }
    }
}

fun statusOrder(status: String): Int = when (status) {
    "waiting" -> 0
    "bought" -> 1
    "couldNotBuy" -> 2
    else -> 0
}

@Composable
fun IngredientItem(
    name: String,
    weight: String,
    unit: String,
    status: String,
    onCheckedChange: () -> Unit,
    onCouldNotBuyClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .bottomDashedBorder(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Checkbox(
            checked = status == "bought",
            onCheckedChange = { onCheckedChange() },
            enabled = status != "couldNotBuy",
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFFFFFFFF),
                uncheckedColor = Color(0xFF4E4E4E),
                checkmarkColor = Color(0xFF4E4E4E)
            ),
            modifier = Modifier
                .clip(RoundedCornerShape(5.dp))
        )

        Text(
            text = "$name - $weight $unit",
            fontSize = 16.sp,
            fontFamily = FontFamily(Font(R.font.roboto_medium)),
            fontWeight = FontWeight.Medium,
            color = if (status == "couldNotBuy") Color.Gray else Color.Black,
            textDecoration = if (status == "couldNotBuy") TextDecoration.LineThrough else TextDecoration.None,
        )

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = { onEditClick() }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_editingredirnt),
                contentDescription = "edit ingredient",
                tint = Color(0xFF4E4E4E),
                modifier = Modifier
                    .size(24.dp)
            )
        }

        IconButton(
            onClick = { onCouldNotBuyClick() }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_couldn_tbuy),
                contentDescription = "couldn't buy",
                tint = Color(0xFF4E4E4E),
                modifier = Modifier
                    .size(24.dp)
            )
        }
    }
}

@Composable
fun CustomEditIngredientDialog(
    name: String,
    onNameChange: (String) -> Unit,
    weight: String,
    onWeightChange: (String) -> Unit,
    unit: String,
    onUnitChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                // 👉 Title sát viền & màu nền riêng
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF97518))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Sửa nguyên liệu",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 👉 Nội dung nhập
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = onNameChange,
                        label = { Text("Tên") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = weight,
                        onValueChange = onWeightChange,
                        label = { Text("Khối lượng") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = unit,
                        onValueChange = onUnitChange,
                        label = { Text("Đơn vị") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 👉 Hàng nút confirm / cancel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Hủy")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onConfirm) {
                        Text("Lưu")
                    }
                }
            }
        }
    }
}

