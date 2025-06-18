package com.watb.chefmate.ui.makeshoppinglist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.watb.chefmate.R
import com.watb.chefmate.helper.CommonHelper.parseIngredientName
import com.watb.chefmate.helper.DataStoreHelper
import com.watb.chefmate.ui.recipe.bottomDashedBorder
import com.watb.chefmate.ui.theme.Header
import com.watb.chefmate.ui.theme.SearchTextField
import com.watb.chefmate.viewmodel.ShoppingTimeViewModel
import kotlinx.coroutines.launch

@Composable
fun ConsolidatedIngredientsScreen(
    navController: NavController,
    shoppingTimeId: Int,
    shoppingTimeViewModel: ShoppingTimeViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var showAddIngredient by remember { mutableStateOf(false) }

    var editIndex by remember { mutableStateOf<Int?>(null) }
    var editName by remember { mutableStateOf("") }
    var editWeight by remember { mutableStateOf("") }
    var editUnit by remember { mutableStateOf("") }

    var addName by remember { mutableStateOf("") }
    var addWeight by remember { mutableStateOf("") }
    var addUnit by remember { mutableStateOf("") }

    val ingredientStatus by shoppingTimeViewModel.shoppingIngredientStatuses.collectAsState()
    val ingredientNames by shoppingTimeViewModel.shoppingIngredientNames.collectAsState()
    val ingredientWeights by shoppingTimeViewModel.shoppingIngredientWeights.collectAsState()
    val ingredientUnits by shoppingTimeViewModel.shoppingIngredientUnits.collectAsState()

    LaunchedEffect(shoppingTimeId) {
        shoppingTimeViewModel.getShoppingTimeById(shoppingTimeId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF))
    ) {
        Header(
            text = "Danh sách mua sắm",
            leadingIcon = {
                IconButton(
                    onClick = { navController.navigate("mainAct") }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = "back",
                        tint = Color.White
                    )
                }
            }
        )

        if (ingredientNames.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Đang tải dữ liệu...", fontSize = 16.sp)
            }
        } else {
            val orderedIndices = ingredientNames.indices.sortedWith(compareBy {
                statusOrder(
                    ingredientStatus.getOrNull(it) ?: ""
                )
            })

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

                            val newNames = ingredientNames.toMutableList()
                            val newWeights = ingredientWeights.toMutableList()
                            val newUnits = ingredientUnits.toMutableList()
                            val newStatuses = ingredientStatus.toMutableList()
                            newStatuses[index] = newStatus
                            val newIngredientNamesString = newNames.joinToString(";;;")
                            val newIngredientWeightsString = newWeights.joinToString(";;;")
                            val newIngredientUnitsString = newUnits.joinToString(";;;")
                            val newIngredientStatusesString = newStatuses.joinToString(";;;")
                            shoppingTimeViewModel.updateShoppingTimeById(
                                shoppingTimeId,
                                newIngredientNamesString,
                                newIngredientWeightsString,
                                newIngredientUnitsString,
                                newIngredientStatusesString
                            )
                        },
                        onCouldNotBuyClick = {
                            val newStatus = if (status == "couldNotBuy") "waiting" else "couldNotBuy"

                            val newNames = ingredientNames.toMutableList()
                            val newWeights = ingredientWeights.toMutableList()
                            val newUnits = ingredientUnits.toMutableList()
                            val newStatuses = ingredientStatus.toMutableList()
                            newStatuses[index] = newStatus
                            val newIngredientNamesString = newNames.joinToString(";;;")
                            val newIngredientWeightsString = newWeights.joinToString(";;;")
                            val newIngredientUnitsString = newUnits.joinToString(";;;")
                            val newIngredientStatusesString = newStatuses.joinToString(";;;")
                            shoppingTimeViewModel.updateShoppingTimeById(
                                shoppingTimeId,
                                newIngredientNamesString,
                                newIngredientWeightsString,
                                newIngredientUnitsString,
                                newIngredientStatusesString
                            )
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
                    title = "Chỉnh sửa nguyên liệu",
                    name = editName,
                    onNameChange = { editName = it },
                    weight = editWeight,
                    onWeightChange = { editWeight = it },
                    unit = editUnit,
                    onUnitChange = { editUnit = it },
                    onConfirm = {
                        val idx = editIndex!!
                        val newNames = ingredientNames.toMutableList()
                        val newWeights = ingredientWeights.toMutableList()
                        val newUnits = ingredientUnits.toMutableList()
                        val newStatuses = ingredientStatus.toMutableList()

                        newNames[idx] = editName
                        newWeights[idx] = editWeight
                        newUnits[idx] = editUnit

                        val newIngredientNamesString = newNames.joinToString(";;;")
                        val newIngredientWeightsString = newWeights.joinToString(";;;")
                        val newIngredientUnitsString = newUnits.joinToString(";;;")
                        val newIngredientStatusesString = newStatuses.joinToString(";;;")
                        shoppingTimeViewModel.updateShoppingTimeById(
                            shoppingTimeId,
                            newIngredientNamesString,
                            newIngredientWeightsString,
                            newIngredientUnitsString,
                            newIngredientStatusesString
                        )

                        editIndex = null
                    },
                    onDismiss = {
                        editIndex = null
                    },
                    buttonText = "Cập nhật"
                )
            }
            if (showAddIngredient) {
                CustomEditIngredientDialog(
                    title = "Thêm mới nguyên liệu",
                    name = addName,
                    onNameChange = { addName = it },
                    weight = addWeight,
                    onWeightChange = { addWeight = it },
                    unit = addUnit,
                    onUnitChange = { addUnit = it },
                    onConfirm = {
                        val newNames = ingredientNames.toMutableList()
                        val newWeights = ingredientWeights.toMutableList()
                        val newUnits = ingredientUnits.toMutableList()
                        val newStatuses = ingredientStatus.toMutableList()

                        newNames.add(newNames.size, addName)
                        newWeights.add(newWeights.size, addWeight)
                        newUnits.add(newUnits.size, addUnit)
                        newStatuses.add(newStatuses.size, "waiting")

                        val newIngredientNamesString = newNames.joinToString(";;;")
                        val newIngredientWeightsString = newWeights.joinToString(";;;")
                        val newIngredientUnitsString = newUnits.joinToString(";;;")
                        val newIngredientStatusesString = newStatuses.joinToString(";;;")
                        shoppingTimeViewModel.updateShoppingTimeById(
                            shoppingTimeId,
                            newIngredientNamesString,
                            newIngredientWeightsString,
                            newIngredientUnitsString,
                            newIngredientStatusesString
                        )

                        addName = ""
                        addWeight = ""
                        addUnit = ""

                        showAddIngredient = false
                    },
                    onDismiss = { showAddIngredient = false },
                    buttonText = "Thêm mới"
                )
            }
        }
        Row {
            Button(
                onClick = {
                    showAddIngredient = true
                },
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
                onClick = {
                    coroutineScope.launch {
                        DataStoreHelper.finishShopping(context)
                    }
                    navController.navigate("mainAct") {
                        popUpTo("mainAct") { inclusive = true }
                    }
                },
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
            onClick = { onEditClick() },
            enabled = status == "waiting"
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
            onClick = { onCouldNotBuyClick() },
            enabled = status != "bought"
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
    title: String,
    name: String,
    onNameChange: (String) -> Unit,
    weight: String,
    onWeightChange: (String) -> Unit,
    unit: String,
    onUnitChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    buttonText: String
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF97518))
                        .padding(12.dp)
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(start = 8.dp)
                    )
                    Spacer( modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_cancel),
                            contentDescription = "cancel",
                            tint = Color(0xFFFFFFFF),
                            modifier = Modifier
                                .size(20.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "Tên nguyên liệu",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(top = 6.dp)
                    )
                    SearchTextField(
                        value = name,
                        onValueChange = onNameChange,
                        modifier = Modifier
                            .padding(top = 12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Định lượng",
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .padding(top = 12.dp)
                    ) {
                        SearchTextField(
                            value = weight,
                            onValueChange = onWeightChange,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .weight(1f)
                        )
                        SearchTextField(
                            value = unit,
                            onValueChange = onUnitChange,
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF97518)
                        )
                    ) {
                        Text(text = buttonText)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun CustomEditIngredientDialogPreview() {
    CustomEditIngredientDialog("","", {},"", {},"", {},{}, {}, "")
}

