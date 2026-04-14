package com.watb.chefmate.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.watb.chefmate.R
import com.watb.chefmate.api.ApiConstant
import com.watb.chefmate.data.Recipe
import com.watb.chefmate.data.SearchType
import com.watb.chefmate.ui.theme.CustomTextField
import com.watb.chefmate.viewmodel.RecipeViewModel
import com.watb.chefmate.viewmodel.UserViewModel

@Composable
fun HomeSearchScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    recipeViewModel: RecipeViewModel,
    recentRecipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val isLoggedIn by userViewModel.isLoggedIn.collectAsState()
    val user by userViewModel.user.collectAsState()
    val topTrending by recipeViewModel.topTrending.collectAsState()
    val topTrendingLoading by recipeViewModel.topTrendingLoading.collectAsState()

    var searchValue by remember { mutableStateOf("") }
    var searchType by remember { mutableStateOf(SearchType.NAME.value) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = stringResource(R.string.common_back_content_description),
                    tint = Color(0xFF111827)
                )
            }
            CustomTextField(
                value = searchValue,
                onValueChange = { searchValue = it },
                placeholder = stringResource(R.string.home_search_hint),
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = stringResource(R.string.home_search_content_description),
                        tint = Color(0xFFF97316),
                        modifier = Modifier.size(22.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (searchValue.isNotBlank()) {
                            navController.navigate("searchRecipe/$searchType/$searchValue")
                            if (searchType == SearchType.NAME.value) {
                                recipeViewModel.searchRecipe(searchValue, userId = if (isLoggedIn) user?.userId else null)
                            } else {
                                recipeViewModel.searchRecipeByTag(searchValue, userId = if (isLoggedIn) user?.userId else null)
                            }
                            keyboardController?.hide()
                        }
                    }
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 6.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(top = 10.dp)
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.search_by_label),
                color = Color(0xFF111827),
                fontSize = 13.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
            )
            SearchChip(
                text = stringResource(R.string.search_by_name),
                selected = searchType == SearchType.NAME.value,
                onClick = { searchType = SearchType.NAME.value },
                modifier = Modifier.padding(start = 8.dp)
            )
            SearchChip(
                text = stringResource(R.string.search_by_tag),
                selected = searchType == SearchType.TAG.value,
                onClick = { searchType = SearchType.TAG.value },
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Text(
            text = stringResource(R.string.home_recently_viewed),
            color = Color(0xFF111827),
            fontSize = 18.sp,
            fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
            modifier = Modifier.padding(top = 18.dp)
        )

        if (recentRecipes.isEmpty()) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Text(
                    text = stringResource(R.string.home_recently_viewed_empty),
                    color = Color(0xFF6B7280),
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                    modifier = Modifier.padding(14.dp)
                )
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 12.dp)
            ) {
                items(recentRecipes, key = { it.recipeId ?: it.recipeName }) { recipe ->
                    RecentRecipeCard(recipe = recipe, onClick = { onRecipeClick(recipe) })
                }
            }
        }

        Text(
            text = stringResource(R.string.home_top_trending),
            color = Color(0xFF111827),
            fontSize = 18.sp,
            fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
            modifier = Modifier.padding(top = 22.dp)
        )

        if (topTrendingLoading && topTrending.isEmpty()) {
            CircularProgressIndicator(
                color = Color(0xFFF97316),
                modifier = Modifier
                    .padding(top = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )
        } else {
            topTrending.forEach { recipe ->
                TrendingRecipeCard(
                    recipe = recipe,
                    onClick = { onRecipeClick(recipe) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SearchChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFFFFEDD5) else Color(0xFFF3F4F6)
        ),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = if (selected) Color(0xFFF97316) else Color(0xFF374151),
            fontSize = 13.sp,
            fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun RecentRecipeCard(
    recipe: Recipe,
    onClick: () -> Unit
) {
    val painter = if (recipe.image.isNotBlank()) {
        if (recipe.image.startsWith("/")) {
            rememberAsyncImagePainter("${ApiConstant.MAIN_URL}${recipe.image}")
        } else {
            rememberAsyncImagePainter(recipe.image)
        }
    } else {
        painterResource(R.drawable.placeholder_image)
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .size(width = 192.dp, height = 220.dp)
            .clickable(onClick = onClick)
    ) {
        Column {
            Image(
                painter = painter,
                contentDescription = recipe.recipeName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = recipe.recipeName,
                    color = Color(0xFF111827),
                    fontSize = 15.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = recipe.userName,
                    color = Color(0xFFF97316),
                    fontSize = 13.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun TrendingRecipeCard(
    recipe: Recipe,
    onClick: () -> Unit
) {
    val painter = if (recipe.image.isNotBlank()) {
        if (recipe.image.startsWith("/")) {
            rememberAsyncImagePainter("${ApiConstant.MAIN_URL}${recipe.image}")
        } else {
            rememberAsyncImagePainter(recipe.image)
        }
    } else {
        painterResource(R.drawable.placeholder_image)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Image(
                painter = painter,
                contentDescription = recipe.recipeName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(92.dp)
                    .background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
            )
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = recipe.recipeName,
                    color = Color(0xFF111827),
                    fontSize = 15.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = recipe.userName,
                    color = Color(0xFF6B7280),
                    fontSize = 13.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                    modifier = Modifier.padding(top = 6.dp)
                )
                Text(
                    text = recipe.cookingTime,
                    color = Color(0xFFF97316),
                    fontSize = 12.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
