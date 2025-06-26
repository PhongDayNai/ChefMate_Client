package com.watb.chefmate.ui.makeshoppinglist

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.watb.chefmate.R
import com.watb.chefmate.data.IngredientItem
import com.watb.chefmate.data.ShoppingStatus
import com.watb.chefmate.database.AppDatabase
import com.watb.chefmate.database.entities.ShoppingTimeEntity
import com.watb.chefmate.helper.CommonHelper
import com.watb.chefmate.helper.DataStoreHelper
import com.watb.chefmate.repository.RecipeRepository
import com.watb.chefmate.repository.ShoppingTimeRepository
import com.watb.chefmate.ui.recipe.bottomDashedBorder
import com.watb.chefmate.ui.theme.Header
import com.watb.chefmate.ui.theme.CustomTextField
import com.watb.chefmate.ui.theme.RecipeSelectedItem
import com.watb.chefmate.viewmodel.RecipeViewModel
import com.watb.chefmate.viewmodel.ShoppingTimeViewModel
import kotlinx.coroutines.launch
import java.util.Date

@SuppressLint("MemberExtensionConflict")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakeShoppingListScreen(
    navController: NavController,
    recipeViewModel: RecipeViewModel,
    shoppingTimeViewModel: ShoppingTimeViewModel
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isShowManually by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val selectedStates = remember { mutableStateMapOf<Int, Boolean>() }

    val allRecipes by recipeViewModel.allRecipes.collectAsState(initial = emptyList())
    val selectedRecipes = allRecipes.filter { selectedStates[it.recipeId] == true }

    var searchQuery by remember { mutableStateOf("") }
    val filteredRecipes = allRecipes.filter {
        it.recipeName.contains(searchQuery, ignoreCase = true)
    }

    val manualIngredients = remember { mutableStateListOf<IngredientItem>() }

    var manualName by remember { mutableStateOf("") }
    var manualWeight by remember { mutableStateOf("") }
    var manualUnit by remember { mutableStateOf("") }


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF))
    ) {
        Header(
            "Lập danh sách mua sắm",
            leadingIcon = {
                IconButton(
                    onClick = { navController.navigate("mainAct")}
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = "back",
                        tint = Color(0xFFFFFFFF)
                    )
                }
            }
        )
        CustomTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it},
            placeholder = "Tìm công thức đã lưu",
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = "search",
                    tint = Color(0xFFFF9800)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_cancel),
                            contentDescription = "Clear search"
                        )
                    }
                }
            },
            modifier = Modifier
                .padding(top = 20.dp, bottom = 20.dp)
                .fillMaxWidth(0.85f)
        )
        Text(
            text = "Thêm nguyên liệu qua công thức",
            fontSize = 16.sp,
            fontWeight = FontWeight(600),
            fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
            modifier = Modifier
                .fillMaxWidth(0.85f)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .safeDrawingPadding()
                .background(Color(0xFFFFFFFF))
        ) {
            if (allRecipes.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Chưa có công thức nào để lựa chọn.", style = MaterialTheme.typography.bodyLarge)
                    Text("Thêm công thức mới từ màn hình chính.", style = MaterialTheme.typography.bodyMedium)
                }
            } else if (searchQuery.isEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxSize(0.5f)
                ) {
                    items(allRecipes.size) { index ->
                        val isSelected = selectedStates[allRecipes[index].recipeId] ?: false
                        Log.d("MakeShoppingList", "Recipe: ${allRecipes[index]}")
                        RecipeSelectedItem(
                            recipe = allRecipes[index],
                            isSelected = isSelected,
                            onToggleSelect = { checked ->
                                allRecipes[index].recipeId?.let {
                                    selectedStates[allRecipes[index].recipeId!!] = checked
                                }
                            }
                        )
                    }
                }
            } else if (searchQuery.isNotEmpty()) {
                if (filteredRecipes.isEmpty()) {
                    Text(
                        text = "Không tìm thấy công thức nào.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        color = Color.Gray,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredRecipes.size) { index ->
                            val recipe = filteredRecipes[index]
                            val isSelected = selectedRecipes.contains(recipe)
                            RecipeSelectedItem(
                                recipe = recipe,
                                isSelected = isSelected,
                                onToggleSelect = { checked ->
                                    allRecipes[index].recipeId?.let {
                                        selectedStates[allRecipes[index].recipeId!!] = checked
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
        Card(
            shape = RoundedCornerShape(15.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFFFFF)
            ),
            border = BorderStroke(1.dp, Color(0xFFD6D6D6)),
            modifier = Modifier
                .padding(top = 10.dp)
                .fillMaxWidth(0.9f)
                .height(160.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                ) {
                    Text(
                        text = "Thêm nguyên liệu thủ công",
                        fontSize = 15.sp,
                        fontWeight = FontWeight(600),
                        fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                        modifier = Modifier
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            isShowManually = !isShowManually
                            coroutineScope.launch {
                                sheetState.show()
                                sheetState.expand()
                            }
                        },
                        modifier = Modifier
                            .size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_addingredient),
                            contentDescription = "add manually",
                            modifier = Modifier
                                .size(20.dp)
                        )
                    }
                }
                LazyColumn {
                    val manualIngredientsList = CommonHelper.consolidateIngredients(manualIngredients = manualIngredients)

                    items(manualIngredientsList.size) { index ->
                        Row(
                            Modifier
                                .padding(vertical = 8.dp)
                                .fillMaxWidth(0.9f)
                                .bottomDashedBorder()
                        ) {
                            Text(
                                text = "${manualIngredientsList[index].ingredientName} ${manualIngredientsList[index].weight} ${manualIngredientsList[index].unit}"
                            )
                        }
                    }

                    if (manualIngredients.size < 3) {
                        val emptyRowTarget = 3 - manualIngredientsList.size
                        items(emptyRowTarget) {
                            Row(
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .height(20.dp)
                                    .fillMaxWidth(0.9f)
                                    .bottomDashedBorder()
                            ) { /*NOTHING*/ }
                        }
                    }
                }
            }
            if (isShowManually) {
                ModalBottomSheet(
                    onDismissRequest = {
                        coroutineScope.launch {
                            sheetState.hide()
                            isShowManually = false
                            manualName = ""
                            manualWeight = ""
                            manualUnit = ""
                        }
                    },
                    modifier = Modifier
                        .imePadding(),
                    sheetState = sheetState
                ) {
                    AddManuallyScreen(
                        name = manualName,
                        onNameChange = { manualName = it },
                        weight = manualWeight,
                        onWeightChange = { manualWeight = it },
                        unit = manualUnit,
                        onUnitChange = { manualUnit = it },
                        onDone = {
                            val newItem = IngredientItem(
                                ingredientName = manualName,
                                weight = manualWeight.toIntOrNull() ?: 0,
                                unit = manualUnit
                            )
                            manualIngredients.add(newItem)

                            manualName = ""
                            manualWeight = ""
                            manualUnit = ""

                            coroutineScope.launch {
                                sheetState.hide()
                                isShowManually = false
                            }
                        }
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .padding(top = 10.dp)
                .fillMaxWidth(0.9f)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    Log.d("MakeShoppingList", "selectedRecipes: $selectedRecipes")
                    val consolidatedIngredients = CommonHelper.consolidateIngredients(recipes = selectedRecipes, manualIngredients = manualIngredients)

                    val ingredientNames = consolidatedIngredients.joinToString(";;;") { it.ingredientName }
                    val ingredientWeights = consolidatedIngredients.joinToString(";;;") { it.weight.toString() }
                    val ingredientUnits = consolidatedIngredients.joinToString(";;;") { it.unit }
                    val buyingStatuses = consolidatedIngredients.joinToString(";;;") { ShoppingStatus.WAITING.value }

                    val shoppingTime = ShoppingTimeEntity(
                        recipeNames = selectedRecipes.joinToString(";;;") { it.recipeName },
                        ingredientNames = ingredientNames,
                        ingredientWeights = ingredientWeights,
                        ingredientUnits = ingredientUnits,
                        buyingStatuses = buyingStatuses,
                        createdDate = CommonHelper.toIso8601UTC(Date())
                    )

                    shoppingTimeViewModel.insertShoppingTime(shoppingTime) { id ->
                        coroutineScope.launch {
                            DataStoreHelper.updateLastShopping(context = context, id.toInt())
                            navController.navigate("consolidated_ingredients_screen/$id")
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF97518)
                ),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text(
                    text = "Hoàn thành",
                    color = Color(0xFFFFFFFF),
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.roboto_bold))
                )
            }
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
fun MakeShoppingListScreenPreview() {
    val navController = rememberNavController()

    val database = AppDatabase.getDatabase(LocalContext.current)

    val recipeRepository = RecipeRepository(database.recipeDao(), database.ingredientDao(), database.tagDao())
    val shoppingTimeRepository = ShoppingTimeRepository(database.shoppingTimeDao())

    val recipeViewModel = RecipeViewModel(recipeRepository)
    val shoppingTimeViewModel = ShoppingTimeViewModel(shoppingTimeRepository)

    MakeShoppingListScreen(navController, recipeViewModel, shoppingTimeViewModel)
}