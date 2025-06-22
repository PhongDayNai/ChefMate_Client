package com.watb.chefmate.ui.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.watb.chefmate.R
import com.watb.chefmate.data.Recipe
import com.watb.chefmate.database.AppDatabase
import com.watb.chefmate.repository.RecipeRepository
import com.watb.chefmate.ui.theme.Header
import com.watb.chefmate.ui.theme.RecipeItem
import com.watb.chefmate.viewmodel.RecipeViewModel

@Composable
fun RecipeListScreen(
    navController: NavController,
    onRecipeClick: (Recipe) -> Unit,
    viewModel: RecipeViewModel
) {
    val recipes by viewModel.allRecipes.collectAsState(initial = emptyList())

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF))
    ) {
        Header(text = "Kho công thức")
        if (recipes.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Chưa có công thức nào.", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Nhấn nút '+' để thêm công thức mới!", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
            ) {
                items(recipes) { recipe ->
                    RecipeItem(
                        onClick = {
                            onRecipeClick(recipe)
                        },
                        onEdit = {
                            navController.navigate("add_edit_recipe/${recipe.recipeId}")
                        },
                        onDelete = {
                            recipe.recipeId?.let {
                                viewModel.deleteRecipeById(recipe.recipeId)
                            }
                        },
                        recipe = recipe,
                        isStorage = true,
                        modifier = Modifier
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecipeListScreensPreview() {
//    val viewModel: RecipeViewModel = viewModel(
//        factory = RecipeViewModel.Factory(
//            repository = RecipeRepository(AppDatabase.getDatabase(LocalContext.current).recipeDao())
//        )
//    )
//    RecipeListScreen(navController = rememberNavController(), {}, viewModel)
}
