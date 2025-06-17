package com.watb.chefmate.ui.main

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import com.watb.chefmate.database.AppDatabase
import com.watb.chefmate.repository.RecipeRepository
import com.watb.chefmate.ui.recipe.AddRecipeScreen
import com.watb.chefmate.data.CommentItem
import com.watb.chefmate.data.CookingStep
import com.watb.chefmate.data.IngredientItem
import com.watb.chefmate.data.Recipe
import com.watb.chefmate.ui.recipe.RecipeViewScreen
import com.watb.chefmate.ui.recipe.SearchResultScreen
import com.watb.chefmate.ui.theme.ChefMateTheme
import com.watb.chefmate.viewmodel.RecipeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChefMateTheme {
                MainScreen()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    )
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val appDatabase = AppDatabase.getDatabase(context)
    val repository = RecipeRepository(appDatabase.recipeDao())
    val navController = rememberNavController()

    NavHost(navController = navController, graph = navGraph(navController, repository))
}

fun navGraph(
    navController: NavController,
    repository: RecipeRepository
): NavGraph {
    var recipe = Recipe(
        recipeId = -1,
        image = "",
        recipeName = "",
        userName = "Admin",
        likeQuantity = 100,
        viewCount = 1151,
        ingredients = listOf<IngredientItem>(),
        cookingSteps = listOf<CookingStep>(),
        cookingTime = "Unknown",
        comments = listOf<CommentItem>(),
        ration = 0,
        createdAt = "2023-06-15 10:20:00"
    )
    val recipeViewModel = RecipeViewModel(repository)
    return navController.createGraph("splash") {
        composable("splash") {
            SplashScreen(navController)
        }
        composable("mainAct") {
            MainAct(
                navController = navController,
                onRecipeClick = { selectedRecipe, isHistory ->
                    recipe = selectedRecipe
                    if (!isHistory) {
                        navController.navigate("recipeView")
                    } else {
                        navController.navigate("recipeViewHistory")
                    }
                },
                recipeViewModel = recipeViewModel
            )
        }
        composable("recipeView") {
            RecipeViewScreen(navController, recipe)
        }
        composable("recipeViewHistory") {
            RecipeViewScreen(navController, recipe, true)
        }
        composable("addRecipe") {
            AddRecipeScreen(navController, recipeId = -1, recipeViewModel)
        }
        composable("searchRecipe/{searchValue}") { backStackEntry ->
            val searchValue = backStackEntry.arguments?.getString("searchValue")
            if (searchValue != null) {
                SearchResultScreen(
                    navController = navController,
                    search = searchValue,
                    onRecipeClick = { selectedRecipe ->
                        recipe = selectedRecipe
                        navController.navigate("recipeView")
                    },
                    recipeViewModel = recipeViewModel)
            }
        }
    }
}

@Preview
@Composable
fun MainScreenPreview() {
    MainScreen()
}