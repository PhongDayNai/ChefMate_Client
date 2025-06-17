package com.watb.chefmate.ui.main

import android.util.Log
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.watb.chefmate.R
import com.watb.chefmate.api.ApiClient
import com.watb.chefmate.data.CommentItem
import com.watb.chefmate.data.CookingStep
import com.watb.chefmate.data.IngredientItem
import com.watb.chefmate.data.Recipe
import com.watb.chefmate.ui.home.HomeScreen
import com.watb.chefmate.ui.recipe.RecipeListScreen
import com.watb.chefmate.viewmodel.RecipeViewModel
import kotlinx.coroutines.launch

@Composable
fun MainAct(
    navController: NavController,
    onRecipeClick: (Recipe, Boolean) -> Unit,
    recipeViewModel: RecipeViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 3 }
    )

    val recipes by recipeViewModel.topTrending.collectAsState()

    LaunchedEffect(Unit) {
        recipeViewModel.getTopTrending()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    selectedIndex = pagerState.currentPage,
                    navController = navController
                ) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(it)
                    }
                }
            }
        ) { _ ->
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = true,
                modifier = Modifier
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    when (page) {
                        0 -> HomeScreen(
                            onRecipeClick = { selectedRecipe ->
                                onRecipeClick(selectedRecipe, false)
                            },
                            navController = navController,
                            recipes = recipes
                        )
                        1 -> RecipeListScreen(
                            navController = navController,
                            onRecipeClick = { selectedRecipe ->
                                onRecipeClick(selectedRecipe, true)
                            },
                            recipeViewModel
                        )
//                        2 -> ProfileScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavController,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    onTabSelected: (Int) -> Unit,
) {
    var isShowOptions by remember { mutableStateOf(false) }
    val animateOptionsBackground by animateFloatAsState(
        targetValue = if (isShowOptions) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "Options background"
    )

    val items = listOf("Trang chủ", "Kho công thức", "Tài khoản")
    val icons = listOf(R.drawable.ic_home, R.drawable.ic_marker, R.drawable.ic_profile)

    ConstraintLayout(
        modifier
    ) {
        val (bottomBarRef, optionsRef, optionsBackgroundRef) = createRefs()

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFFFFF)
            ),
            shape = RectangleShape,
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                items.forEachIndexed { index, item ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable{ onTabSelected(index) }
                    ) {
                        Icon(
                            painter = painterResource(icons[index]),
                            contentDescription = item,
                            tint = if (selectedIndex == index) Color(0xFFFB923C) else Color(0xFF4B5563),
                            modifier = Modifier
                                .size(27.dp)
                        )
                        Text(
                            text = item,
                            fontSize = 12.sp,
                            fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                            color = if (selectedIndex == index) Color(0xFFFB923C) else Color(0xFF4B5563),
                            modifier = Modifier
                        )
                    }
                }
            }
        }
        Button(
            onClick = { isShowOptions = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF97316)
            ),
            contentPadding = PaddingValues(0.dp),
            elevation = ButtonDefaults.elevatedButtonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp
            ),
            modifier = Modifier
                .size(((1f - animateOptionsBackground) * 48).dp)
                .constrainAs(optionsRef) {
                    bottom.linkTo(bottomBarRef.top, margin = 24.dp)
                    end.linkTo(parent.end, margin = 24.dp)
                }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_options),
                contentDescription = "Options",
                tint = Color(0xFFFFFFFF),
                modifier = Modifier
            )
        }
        ConstraintLayout(
            modifier = Modifier
                .size((animateOptionsBackground * 270).dp, (animateOptionsBackground * 300).dp)
                .background(color = Color(0xFFFB923C), shape = CircleShape)
                .rotate(45f)
                .constrainAs(optionsBackgroundRef) {
                    bottom.linkTo(bottomBarRef.bottom, margin = (-36).dp)
                    end.linkTo(parent.end, margin = (-32).dp)
                }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size((animateOptionsBackground * 300).dp, (animateOptionsBackground * 320).dp)
                    .background(color = Color(0xA0000000), shape = CircleShape)
                    .rotate(135f)
                    .constrainAs(createRef()) {
                        bottom.linkTo(parent.bottom, margin = (-6).dp)
                        start.linkTo(parent.start, margin = (-6).dp)
                    }
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .rotate(180f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable {
                                navController.navigate("addRecipe")
                            }
                    ) {
                        Text(
                            text = "Thêm công thức",
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                            color = Color(0xFFFFFFFF),
                            modifier = Modifier
                                .padding(end = 12.dp)
                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .background(color = Color(0xFFFFFFFF), shape = CircleShape)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_add_recipe),
                                contentDescription = "Add Recipe",
                                tint = Color.Unspecified,
                                modifier = Modifier
                                    .size(28.dp)
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = 24.dp)
                            .clickable {
                                navController.navigate("make_shopping_list_screen")
                            }
                    ) {
                        Text(
                            text = "Lập danh sách\nmua sắm",
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                            color = Color(0xFFFFFFFF),
                            modifier = Modifier
                                .padding(end = 12.dp)
                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .background(color = Color(0xFFFFFFFF), shape = CircleShape)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_shopping),
                                contentDescription = "Create Shopping List",
                                tint = Color.Unspecified,
                                modifier = Modifier
                                    .size(28.dp)
                            )
                        }
                    }
                    Button(
                        onClick = {
                            isShowOptions = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFFFFF)
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier
                            .padding(top = 24.dp)
                            .height(32.dp)
                    ) {
                        Text(
                            text = "Đóng",
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                            color = Color(0xFF474747),
                            modifier = Modifier
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun Preview() {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF))
    ) {
        BottomNavigationBar(
            navController = rememberNavController(),
            selectedIndex = 0,
            onTabSelected = {},
            modifier = Modifier
                .constrainAs(createRef()) {
                    bottom.linkTo(parent.bottom)
                }
        )
    }
}
