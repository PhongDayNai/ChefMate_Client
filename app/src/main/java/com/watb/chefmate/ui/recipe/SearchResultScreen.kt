package com.watb.chefmate.ui.recipe

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.watb.chefmate.R
import com.watb.chefmate.data.Recipe
import com.watb.chefmate.data.SearchType
import com.watb.chefmate.ui.theme.Header
import com.watb.chefmate.ui.theme.RecipeItem
import com.watb.chefmate.ui.theme.CustomTextField
import com.watb.chefmate.viewmodel.RecipeViewModel
import com.watb.chefmate.viewmodel.UserViewModel

@SuppressLint("MemberExtensionConflict")
@Composable
fun SearchResultScreen(
    navController: NavController,
    searchTypeValue: String,
    search: String,
    onRecipeClick: (Recipe) -> Unit,
    userViewModel: UserViewModel,
    recipeViewModel: RecipeViewModel
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val isLoggedIn by userViewModel.isLoggedIn.collectAsState()
    val user by userViewModel.user.collectAsState()
    val isLoading by recipeViewModel.isLoading.collectAsState()
    val searchResultRecipes by recipeViewModel.searchResult.collectAsState()

    var searchType by remember { mutableStateOf(searchTypeValue) }
    var searchValue by remember { mutableStateOf(search) }

    val scrollState = rememberScrollState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFFFFFFF))
            .padding(bottom = 12.dp)
    ) {
        Header(
            "Nấu ngon",
            leadingIcon = {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = "back",
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
                    if (searchType == SearchType.NAME.value) {
                        recipeViewModel.searchRecipe(searchValue, userId = if (isLoggedIn) user?.userId else null)
                    } else {
                        recipeViewModel.searchRecipeByTag(searchValue, userId = if (isLoggedIn) user?.userId else null)
                    }
                    keyboardController?.hide()
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
        if (!isLoading) {
            if (searchResultRecipes.isEmpty()) {
                Text(
                    text = "Không có kết quả nào.\nVui lòng đợi trong giây lát...",
                    color = Color(0xFF000000),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 84.dp, bottom = 42.dp)
                        .fillMaxWidth(0.9f)
                )
                Button(
                    onClick = {
                        if (searchType == SearchType.NAME.value) {
                            recipeViewModel.searchRecipe(searchValue, userId = if (isLoggedIn) user?.userId else null)
                        } else {
                            recipeViewModel.searchRecipeByTag(searchValue, userId = if (isLoggedIn) user?.userId else null)
                        }
                        keyboardController?.hide()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800)
                    ),
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(0.9f)
                ) {
                    Text(
                        text = "Thử lại",
                        color = Color(0xFFFFFFFF),
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxSize()
                        .verticalScroll(state = scrollState)
                ) {
                    searchResultRecipes.forEach { recipe ->
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
        } else {
            Text(
                text = "Vui lòng đợi trong giây lát...",
                color = Color(0xFF000000),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 84.dp, bottom = 42.dp)
                    .fillMaxWidth(0.9f)
            )
            CircularProgressIndicator()
        }
    }
}