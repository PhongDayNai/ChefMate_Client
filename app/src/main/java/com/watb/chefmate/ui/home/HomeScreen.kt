package com.watb.chefmate.ui.home

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.watb.chefmate.R
import com.watb.chefmate.data.CommentItem
import com.watb.chefmate.data.Recipe
import com.watb.chefmate.ui.theme.Header
import com.watb.chefmate.ui.theme.RecipeItem
import com.watb.chefmate.ui.theme.SearchTextField
import java.util.Date

@Composable
fun HomeScreen(
    onRecipeClick: (Recipe) -> Unit,
    navController: NavController,
    recipes: List<Recipe> = emptyList()
) {
    val scrollState = rememberScrollState()
    val showPopular = remember { derivedStateOf { scrollState.value == 0 } }

    var searchValue by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .safeDrawingPadding()
            .fillMaxSize()
            .background(color = Color(0xFFFFFFFF))
            .padding(bottom = 42.dp)
    ) {
        Header("Nấu ngon") {
            IconButton(
                onClick = {}
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
        SearchTextField(
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
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(0.9f)
        )
        Text(
            text = "Thể loại",
            color = Color(0xFF000000),
            fontSize = 18.sp,
            fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth(0.9f)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {
            val imageLeft = listOf(R.drawable.img_drinks_recipes, R.drawable.img_sauce_recipes, R.drawable.img_soup_recipes)
            val recipesTypeLeft = listOf("Công thức đồ uống", "Công thức nước chấm", "Công thức món súp")
            val imageRight = listOf(R.drawable.img_salad_recipes, R.drawable.img_main_course_recipes, R.drawable.img_vegetarian_recipes)
            val recipesTypeRight = listOf("Công thức salad", "Công thức món chính", "Công thức món chay")

            if (showPopular.value) {
                imageLeft.forEachIndexed { index, image ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth(0.85f)
                    ) {
                        TopSearchItem(
                            onClick = {},
                            text = recipesTypeLeft[index],
                            image = image,
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .weight(1f)
                        )
                        TopSearchItem(
                            onClick = {},
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
            text = "Top tìm kiếm",
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
//                        navController.navigate("recipeView")
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
fun TopSearchItem(onClick: () -> Unit, text: String, @DrawableRes image: Int, modifier: Modifier = Modifier) {
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

@Preview
@Composable
fun HomeScreenPreview() {
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        modifier = Modifier
//            .fillMaxSize()
//            .background(color = Color(0xFFFFFFFF))
//    ) {
//
//        TopSearchItem({}, "Công thức đồ uống", "https://umbercoffee.vn/wp-content/uploads/2024/06/matcha-latte-umber-coffee-tea-ho-chi-minh-city-700000.jpg")
//    }
}