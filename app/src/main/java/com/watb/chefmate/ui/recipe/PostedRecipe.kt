package com.watb.chefmate.ui.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.watb.chefmate.R
import com.watb.chefmate.data.Recipe
import com.watb.chefmate.ui.theme.CustomTextField
import com.watb.chefmate.ui.theme.Header
import com.watb.chefmate.ui.theme.RecipeItem
import com.watb.chefmate.viewmodel.RecipeViewModel

@Composable
fun PostedRecipeList(
    navController: NavController,
    onRecipeClick: (Recipe) -> Unit,
    recipeViewModel: RecipeViewModel
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val recipes by recipeViewModel.personalRecipes.collectAsState()
    var searchValue by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFFFFFFF))
            .padding(bottom = 42.dp)
    ) {
        Header(
            text = "Công thức đã đăng",
            leadingIcon = {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = "Notification",
                        tint = Color(0xFFFFFFFF),
                        modifier = Modifier
                            .size(24.dp)
                    )
                }
            },
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
                    keyboardController?.hide()
                }
            ),
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(0.9f)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState())
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
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}