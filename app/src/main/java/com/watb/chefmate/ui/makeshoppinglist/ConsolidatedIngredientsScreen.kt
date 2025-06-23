package com.watb.chefmate.ui.makeshoppinglist

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.navigation.compose.rememberNavController
import com.watb.chefmate.R
import com.watb.chefmate.data.StatusShopping
import com.watb.chefmate.database.AppDatabase
import com.watb.chefmate.helper.CommonHelper.parseIngredientName
import com.watb.chefmate.helper.DataStoreHelper
import com.watb.chefmate.repository.ShoppingTimeRepository
import com.watb.chefmate.ui.recipe.bottomDashedBorder
import com.watb.chefmate.ui.theme.CustomDialog
import com.watb.chefmate.ui.theme.Header
import com.watb.chefmate.viewmodel.ShoppingTimeViewModel
import kotlinx.coroutines.launch

@Composable
fun ConsolidatedIngredientsScreen(
    navController: NavController,
    shoppingTimeId: Int,
    isHistory: Boolean = false,
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
            text = if (!isHistory) "Danh sách mua sắm" else "Lịch sử mua sắm",
            leadingIcon = {
                IconButton(
                    onClick = {
                        if (!isHistory) {
                            navController.navigate("mainAct") {
                                popUpTo("consolidated_ingredients_screen/$shoppingTimeId") { inclusive = true }
                            }
                        } else {
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier
                        .size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = "back",
                        tint = Color.White,
                        modifier = Modifier
                            .size(20.dp)
                    )
                }
            }
        )

        if (ingredientNames.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Chưa có nguyên liệu nào trong danh sách mua sắm.", fontSize = 16.sp)
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
                        isHistory = isHistory,
                        onCheckedChange = {
                            val newStatus = if (status == StatusShopping.BOUGHT.value) StatusShopping.WAITING.value else StatusShopping.BOUGHT.value

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
                            val newStatus = if (status == StatusShopping.COULD_NOT_BUY.value) StatusShopping.WAITING.value else StatusShopping.COULD_NOT_BUY.value

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
                CustomDialog(
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
                    isConfirm = false,
                    confirmText = "",
                    onDismiss = {
                        editIndex = null
                    },
                    buttonText = "Cập nhật"
                )
            }
            if (showAddIngredient) {
                CustomDialog(
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
                        newStatuses.add(newStatuses.size, StatusShopping.WAITING.value)

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
                    isConfirm = false,
                    confirmText = "",
                    onDismiss = { showAddIngredient = false },
                    buttonText = "Thêm mới"
                )
            }
        }
        Row {
            if (!isHistory) {
                Button(
                    onClick = {
                        showAddIngredient = true
                    },
                    border = _root_ide_package_.androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF000000)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDCC8C8),
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .padding(30.dp)
                ) {
                    Text(
                        text = "Bổ sung",
                        color = Color(0xFF000000),
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.roboto_bold))
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    if (!isHistory) {
                        coroutineScope.launch {
                            DataStoreHelper.finishShopping(context)
                            navController.navigate("mainAct") {
                                popUpTo("consolidated_ingredients_screen/$shoppingTimeId") { inclusive = true }
                            }
                        }
                    } else {
                        navController.popBackStack()
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
                    text = if (!isHistory) "Hoàn thành" else "Trở lại",
                    color = Color(0xFFFFFFFF),
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.roboto_bold))
                )
            }
        }
    }
}

fun statusOrder(status: String): Int = when (status) {
    StatusShopping.WAITING.value -> 0
    StatusShopping.BOUGHT.value -> 1
    StatusShopping.COULD_NOT_BUY.value -> 2
    else -> 0
}

@Composable
fun IngredientItem(
    name: String,
    weight: String,
    unit: String,
    status: String,
    isHistory: Boolean,
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
            checked = status == StatusShopping.BOUGHT.value,
            onCheckedChange = { onCheckedChange() },
            enabled = if (isHistory) false else status != StatusShopping.COULD_NOT_BUY.value,
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
            color = if (status == StatusShopping.COULD_NOT_BUY.value) Color.Gray else Color.Black,
            textDecoration = if (status == StatusShopping.COULD_NOT_BUY.value) TextDecoration.LineThrough else TextDecoration.None,
        )

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = { onEditClick() },
            enabled = if (isHistory) false else status == StatusShopping.WAITING.value
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
            enabled = if (isHistory) false else status != StatusShopping.BOUGHT.value
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

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
fun CustomEditIngredientDialogPreview() {
    val context = LocalContext.current
    val appDatabase = AppDatabase.getDatabase(context)
    val shoppingTimeRepository = ShoppingTimeRepository(appDatabase.shoppingTimeDao())
    val shoppingTimeViewModel = ShoppingTimeViewModel(shoppingTimeRepository)
    val navController = rememberNavController()
    ConsolidatedIngredientsScreen(
        navController = navController,
        shoppingTimeId = 1,
        shoppingTimeViewModel = shoppingTimeViewModel
    )
}

