package com.watb.chefmate.ui.home

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import coil.compose.rememberAsyncImagePainter
import com.watb.chefmate.R
import com.watb.chefmate.data.CommentItem
import com.watb.chefmate.data.Recipe
import com.watb.chefmate.helper.UsuallyHelper
import com.watb.chefmate.ui.theme.Header
import com.watb.chefmate.ui.theme.SearchTextField

@Composable
fun HomeScreen(navController: NavController, recipes: List<Recipe> = emptyList()) {
    val scrollState = rememberScrollState()
    val showPopular = remember { derivedStateOf { scrollState.value == 0 } }
    val animateShowPopular by animateDpAsState(
        targetValue = if (showPopular.value) 230.dp else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "Show popular"
    )

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
            text = "Phổ biến",
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
                .height(animateShowPopular)
        ) {
            for (index in 0..2) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth(0.85f)
                ) {
                    TopSearchItem(
                        onClick = {},
                        text = "Công thức đồ uống",
                        image = "https://umbercoffee.vn/wp-content/uploads/2024/06/matcha-latte-umber-coffee-tea-ho-chi-minh-city-700000.jpg",
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .weight(1f)
                    )
                    TopSearchItem(
                        onClick = {},
                        text = "Công thức Salad",
                        image = "https://umbercoffee.vn/wp-content/uploads/2024/06/matcha-latte-umber-coffee-tea-ho-chi-minh-city-700000.jpg",
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .weight(1f)
                    )
                }
            }
        }
        Text(
            text = "Top tìm kiếm",
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
                .fillMaxSize()
                .verticalScroll(state = scrollState)
        ) {
            recipes.forEach { recipe ->
                RecipeItem({}, recipe, Modifier.fillMaxWidth(0.9f))
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun TopSearchItem(onClick: () -> Unit, text: String, image: String, modifier: Modifier = Modifier) {
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
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                fontWeight = FontWeight(400),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .fillMaxWidth(0.6f)
            )
            Image(
                painter = rememberAsyncImagePainter(image),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}

@Composable
fun RecipeItem(
    onClick: () -> Unit,
    recipe: Recipe,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFFFF)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        modifier = modifier
            .padding(top = 16.dp)
            .fillMaxWidth(0.9f)
    ) {
        Column {
            Image(
                painter = rememberAsyncImagePainter(recipe.image),
                contentDescription = recipe.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(
                    text = recipe.name,
                    color = Color(0xFF000000),
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                    fontWeight = FontWeight(700),
                )
                Text(
                    text = recipe.author,
                    color = Color(0xFFF97316),
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                    fontWeight = FontWeight(400),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .background(color = Color(0xFFFFEDD5), RoundedCornerShape(4.dp))
                        .padding(4.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_like),
                        contentDescription = "Like",
                        tint = Color(0xFFF97316),
                        modifier = Modifier
                            .size(16.dp)
                    )
                    Text(
                        text = UsuallyHelper.parseNumber(recipe.likesQuantity),
                        color = Color(0xFF6B7280),
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                        fontWeight = FontWeight(400),
                        modifier = Modifier
                            .padding(start = 2.dp)
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_view),
                        contentDescription = "Like",
                        tint = Color(0xFFF97316),
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .size(16.dp)
                    )
                    Text(
                        text = UsuallyHelper.parseNumber(recipe.userViews),
                        color = Color(0xFF6B7280),
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                        fontWeight = FontWeight(400),
                        modifier = Modifier
                            .padding(start = 2.dp)
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_comment),
                        contentDescription = "Like",
                        tint = Color(0xFFF97316),
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .size(16.dp)
                    )
                    Text(
                        text = UsuallyHelper.parseNumber(recipe.comments.size),
                        color = Color(0xFF6B7280),
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                        fontWeight = FontWeight(400),
                        modifier = Modifier
                            .padding(start = 2.dp)
                    )
                }
            }
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

    val recipes = listOf(
        Recipe(
            image = "https://umbercoffee.vn/wp-content/uploads/2024/06/matcha-latte-umber-coffee-tea-ho-chi-minh-city-700000.jpg",
            name = "Matcha Latte",
            author = "Admin",
            likesQuantity = 100,
            userViews = 1151,
            ingredients = listOf("Matcha", "Cream", "Milk"),
            cookingSteps = listOf("Step 1", "Step 2", "Step 3"),
            cookingTime = "30 phút",
            comments = listOf(
                CommentItem(
                    author = "User 1",
                    time = "1 giờ trước",
                    content = "Nội dung bình luận 1"
                ),
                CommentItem(
                    author = "User 2",
                    time = "2 giờ trước",
                    content = "Nội dung bình luận 2"
                )
            ),
        ),
        Recipe(
            image = "https://umbercoffee.vn/wp-content/uploads/2024/06/matcha-latte-umber-coffee-tea-ho-chi-minh-city-700000.jpg",
            name = "Matcha Latte",
            author = "Admin",
            likesQuantity = 100,
            userViews = 1151,
            ingredients = listOf("Matcha", "Cream", "Milk"),
            cookingSteps = listOf("Step 1", "Step 2", "Step 3"),
            cookingTime = "30 phút",
            comments = listOf(
                CommentItem(
                    author = "User 1",
                    time = "1 giờ trước",
                    content = "Nội dung bình luận 1"
                ),
                CommentItem(
                    author = "User 2",
                    time = "2 giờ trước",
                    content = "Nội dung bình luận 2"
                )
            ),
        ),
    )
    HomeScreen(navController = NavController(LocalContext.current), recipes = recipes)
}