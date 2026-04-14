package com.watb.chefmate.ui.home

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.watb.chefmate.R
import com.watb.chefmate.data.Recipe
import com.watb.chefmate.database.AppDatabase
import com.watb.chefmate.repository.RecipeRepository
import com.watb.chefmate.ui.theme.Header
import com.watb.chefmate.ui.theme.RecipeItem
import com.watb.chefmate.viewmodel.AppFlowViewModel
import com.watb.chefmate.viewmodel.RecipeViewModel
import com.watb.chefmate.viewmodel.UserViewModel

@SuppressLint("MemberExtensionConflict")
@Composable
fun HomeScreen(
    onRecipeClick: (Recipe) -> Unit,
    navController: NavController,
    userViewModel: UserViewModel,
    recipeViewModel: RecipeViewModel,
    appFlowViewModel: AppFlowViewModel
) {
    val scrollState = rememberScrollState()
    val isLoggedIn by userViewModel.isLoggedIn.collectAsState()
    val user by userViewModel.user.collectAsState()
    val recipes by recipeViewModel.topTrending.collectAsState()
    val topTrendingHasMore by recipeViewModel.topTrendingHasMore.collectAsState()
    val topTrendingLoading by recipeViewModel.topTrendingLoading.collectAsState()

    LaunchedEffect(isLoggedIn, user?.userId) {
        if (isLoggedIn && user != null) {
            appFlowViewModel.prefetchChatHistorySources(user!!.userId)
        }
    }

    LaunchedEffect(
        scrollState.value,
        scrollState.maxValue,
        recipes.size,
        topTrendingHasMore,
        topTrendingLoading,
        isLoggedIn,
        user?.userId
    ) {
        if (recipes.isEmpty()) return@LaunchedEffect
        if (!topTrendingHasMore || topTrendingLoading) return@LaunchedEffect

        val thresholdPx = 180
        val nearBottom = scrollState.value >= (scrollState.maxValue - thresholdPx)
        if (nearBottom) {
            recipeViewModel.loadMoreTopTrending(userId = if (isLoggedIn) user?.userId else null)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFFFFFFF))
    ) {
        Header(
            text = stringResource(R.string.home_title),
            trailingIcon = {
                IconButton(
                    onClick = { navController.navigate("homeSearch") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = stringResource(R.string.home_search_open_content_description),
                        tint = Color(0xFFFFFFFF),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        )

        BepesEntrySection(
            onOpenChat = {
                if (isLoggedIn && user != null) {
                    navController.navigate("bepesChat?recipeId=-1")
                } else {
                    navController.navigate("signIn")
                }
            },
            onOpenPantrySuggestions = {
                if (isLoggedIn && user != null) {
                    navController.navigate("homeSearch")
                } else {
                    navController.navigate("signIn")
                }
            },
            modifier = Modifier.padding(top = 14.dp)
        )

//        Temporarily hide "Tags phổ biến". Keep this block for quick restore later.
//        Text(
//            text = "Tags phổ biến",
//            color = Color(0xFF000000),
//            fontSize = 18.sp,
//            fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
//            modifier = Modifier
//                .padding(top = 16.dp)
//                .fillMaxWidth(0.9f)
//        )
//        AnimatedVisibility(
//            visible = showPopular.value,
//            enter = fadeIn() + slideInVertically() + expandVertically(),
//            exit = fadeOut() + shrinkVertically(),
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Column(
//                horizontalAlignment = Alignment.CenterHorizontally,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .animateContentSize()
//            ) {
//                val imageLeft = listOf(R.drawable.img_drinks_recipes, R.drawable.img_sauce_recipes, R.drawable.img_soup_recipes)
//                val recipesTypeLeft = listOf("Công thức\nđồ uống", "Công thức\nnước chấm", "Công thức\nmón súp")
//                val imageRight = listOf(R.drawable.img_salad_recipes, R.drawable.img_main_course_recipes, R.drawable.img_vegetarian_recipes)
//                val recipesTypeRight = listOf("Công thức\nsalad", "Công thức\nmón chính", "Công thức\nmón chay")
//
//                imageLeft.forEachIndexed { index, image ->
//                    Row(
//                        horizontalArrangement = Arrangement.SpaceBetween,
//                        modifier = Modifier
//                            .padding(top = 12.dp)
//                            .fillMaxWidth(0.85f)
//                    ) {
//                        SearchTypeItem(
//                            onClick = {
//                                val value = recipesTypeLeft[index].replace("Công thức\n", "")
//                                navController.navigate("searchRecipe/tag/$value")
//                                recipeViewModel.searchRecipeByTag(value, userId = if (isLoggedIn) user?.userId else null)
//                            },
//                            text = recipesTypeLeft[index],
//                            image = image,
//                            modifier = Modifier
//                                .padding(end = 16.dp)
//                                .weight(1f)
//                        )
//                        SearchTypeItem(
//                            onClick = {
//                                val value = recipesTypeRight[index].replace("Công thức\n", "")
//                                navController.navigate("searchRecipe/tag/$value")
//                                recipeViewModel.searchRecipeByTag(value, userId = if (isLoggedIn) user?.userId else null)
//                            },
//                            text = recipesTypeRight[index],
//                            image = imageRight[index],
//                            modifier = Modifier
//                                .padding(start = 16.dp)
//                                .weight(1f)
//                        )
//                    }
//                }
//                Spacer(modifier = Modifier.height(20.dp))
//            }
//        }
        Text(
            text = stringResource(R.string.home_top_trending),
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
                    onClick = { selectedRecipe -> onRecipeClick(selectedRecipe) },
                    recipe = recipe,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            }
            if (topTrendingLoading && recipes.isNotEmpty()) {
                CircularProgressIndicator(
                    color = Color(0xFFF97316),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(22.dp),
                    strokeWidth = 2.5.dp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun BepesEntrySection(
    onOpenChat: () -> Unit,
    onOpenPantrySuggestions: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFCF8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = stringResource(R.string.home_bepes_title),
                color = Color(0xFF9A3412),
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
            )
            Text(
                text = stringResource(R.string.home_bepes_description),
                color = Color(0xFF7C2D12),
                fontSize = 13.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                modifier = Modifier.padding(top = 6.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                HomeActionChip(
                    text = stringResource(R.string.home_chat_with_bepes),
                    onClick = onOpenChat,
                    modifier = Modifier.weight(1f)
                )
                HomeActionChip(
                    text = stringResource(R.string.home_pantry_suggestions),
                    onClick = onOpenPantrySuggestions,
                    containerColor = Color(0xFFE0F2FE),
                    textColor = Color(0xFF0369A1),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}


@Composable
private fun EmptyRecommendationCard(text: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFF6B7280),
            fontSize = 12.sp,
            fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun HomeActionChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFFFFEDD5),
    textColor: Color = Color(0xFFF97316)
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier
            .background(Color.Transparent)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 7.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SearchTypeItem(onClick: () -> Unit, text: String, @DrawableRes image: Int, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier.height(60.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                color = Color.Black,
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
                modifier = Modifier.fillMaxSize()
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
    val userViewModel: UserViewModel = viewModel()
    val recipeRepository = RecipeRepository(appDatabase.recipeDao(), appDatabase.ingredientDao(), appDatabase.tagDao())
    val appFlowViewModel: AppFlowViewModel = viewModel()

    HomeScreen(
        onRecipeClick = {},
        navController = NavController(LocalContext.current),
        userViewModel = userViewModel,
        recipeViewModel = RecipeViewModel(recipeRepository),
        appFlowViewModel = appFlowViewModel
    )
}
