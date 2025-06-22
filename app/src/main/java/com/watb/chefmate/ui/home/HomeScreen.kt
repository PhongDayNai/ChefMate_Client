package com.watb.chefmate.ui.home

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.watb.chefmate.R
import com.watb.chefmate.data.Recipe
import com.watb.chefmate.data.SearchType
import com.watb.chefmate.database.AppDatabase
import com.watb.chefmate.repository.RecipeRepository
import com.watb.chefmate.ui.theme.Header
import com.watb.chefmate.ui.theme.RecipeItem
import com.watb.chefmate.ui.theme.CustomTextField
import com.watb.chefmate.viewmodel.RecipeViewModel

@SuppressLint("MemberExtensionConflict")
@Composable
fun HomeScreen(
    onRecipeClick: (Recipe) -> Unit,
    navController: NavController,
    recipeViewModel: RecipeViewModel
) {
    val scrollState = rememberScrollState()
    val showPopular = remember { derivedStateOf { scrollState.value == 0 } }
    val recipes by recipeViewModel.topTrending.collectAsState()

    var searchType by remember { mutableStateOf(SearchType.NAME.value) }
    var searchValue by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFFFFFFF))
            .padding(bottom = 42.dp)
    ) {
        Header(
            text = "Nấu ngon",
            trailingIcon = {
                IconButton(
                    onClick = {},
                    modifier = Modifier
                        .size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_bell),
                        contentDescription = "Notification",
                        tint = Color(0xFFFFFFFF),
                        modifier = Modifier
                            .size(24.dp)
                    )
                }
            }
        )
        CustomTextField(
            value = searchValue,
            onValueChange = { searchValue = it },
            placeholder = "Tìm kiếm món ăn",
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = "Search",
                    tint = Color(0xFFFF9800).copy(alpha = 0.75f),
                    modifier = Modifier
                        .size(24.dp)
                )
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    navController.navigate("searchRecipe/${searchType}/$searchValue")
                    if (searchType == SearchType.NAME.value) {
                        recipeViewModel.searchRecipe(searchValue, userId = null)
                    } else {
                        recipeViewModel.searchRecipeByTag(searchValue, userId = null)
                    }
                }
            ),
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(0.9f)
        )
        AnimatedVisibility(
            visible = searchValue.isNotEmpty(),
            enter = fadeIn() + slideInVertically() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(0.9f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(36.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Tìm kiếm theo: ",
                    color = Color(0xFF000000),
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                    modifier = Modifier
                )
                Card(
                    onClick = {
                        searchType = SearchType.NAME.value
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFFFFF)
                    ),
                    elevation = CardDefaults.elevatedCardElevation(
                        defaultElevation = if (searchType == SearchType.NAME.value) 4.dp else 0.dp,
                        pressedElevation = 8.dp
                    ),
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "Tên món",
                        color = Color(0xFF000000),
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Card(
                    onClick = {
                        searchType = SearchType.TAG.value
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFFFFF)
                    ),
                    elevation = CardDefaults.elevatedCardElevation(
                        defaultElevation = if (searchType == SearchType.TAG.value) 4.dp else 0.dp,
                        pressedElevation = 8.dp
                    ),
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "Tag",
                        color = Color(0xFF000000),
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
        Text(
            text = "Tags phổ biến",
            color = Color(0xFF000000),
            fontSize = 18.sp,
            fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(0.9f)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {
            val imageLeft = listOf(R.drawable.img_drinks_recipes, R.drawable.img_sauce_recipes, R.drawable.img_soup_recipes)
            val recipesTypeLeft = listOf("Công thức\nđồ uống", "Công thức\nnước chấm", "Công thức\nmón súp")
            val imageRight = listOf(R.drawable.img_salad_recipes, R.drawable.img_main_course_recipes, R.drawable.img_vegetarian_recipes)
            val recipesTypeRight = listOf("Công thức\nsalad", "Công thức\nmón chính", "Công thức\nmón chay")

            if (showPopular.value) {
                imageLeft.forEachIndexed { index, image ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth(0.85f)
                    ) {
                        SearchTypeItem(
                            onClick = {
                                val value = recipesTypeLeft[index].replace("Công thức\n", "")
                                navController.navigate("searchRecipe/tag/$value")
                                recipeViewModel.searchRecipeByTag(value, userId = null)
                            },
                            text = recipesTypeLeft[index],
                            image = image,
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .weight(1f)
                        )
                        SearchTypeItem(
                            onClick = {
                                val value = recipesTypeRight[index].replace("Công thức\n", "")
                                navController.navigate("searchRecipe/tag/$value")
                                recipeViewModel.searchRecipeByTag(value, userId = null)
                            },
                            text = recipesTypeRight[index],
                            image = imageRight[index],
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
        Text(
            text = "Top thịnh hành",
            color = Color(0xFF000000),
            fontSize = 18.sp,
            fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
            modifier = Modifier
                .padding(top = 10.dp)
                .fillMaxWidth(0.9f)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = scrollState)
        ) {
            recipes.forEach { recipe ->
                RecipeItem(
                    onClick = { selectedRecipe ->
                        onRecipeClick(selectedRecipe)
                    },
                    recipe = recipe,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SearchTypeItem(onClick: () -> Unit, text: String, @DrawableRes image: Int, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFFFF)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        modifier = modifier
            .height(60.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Text(
                text = text,
                color = Color(0xFF000000),
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                fontWeight = FontWeight(400),
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .fillMaxWidth(0.6f)
            )
            Image(
                painter = painterResource(image),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
fun HomeScreenPreview() {
    val context = LocalContext.current
    val appDatabase = AppDatabase.getDatabase(context)
    val recipeRepository = RecipeRepository(appDatabase.recipeDao(), appDatabase.ingredientDao(), appDatabase.tagDao())

    HomeScreen(
        onRecipeClick = {},
        navController = NavController(LocalContext.current),
//        recipes = listOf(),
        recipeViewModel = RecipeViewModel(recipeRepository)
    )
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        modifier = Modifier
//            .fillMaxSize()
//            .background(color = Color(0xFFFFFFFF))
//    ) {
//
//        SearchTypeItem({}, "Công thức đồ uống", "https://umbercoffee.vn/wp-content/uploads/2024/06/matcha-latte-umber-coffee-tea-ho-chi-minh-city-700000.jpg")
//    }
}