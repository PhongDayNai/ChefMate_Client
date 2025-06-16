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
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import com.watb.chefmate.data.CommentItem
import com.watb.chefmate.data.Recipe
import com.watb.chefmate.ui.recipe.RecipeViewScreen
import com.watb.chefmate.ui.theme.ChefMateTheme

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
    val navController = rememberNavController()

    NavHost(navController = navController, graph = navGraph(navController))
}

fun navGraph(
    navController: NavController
): NavGraph {
    var recipe = Recipe(
        image = "",
        name = "",
        author = "Admin",
        likesQuantity = 100,
        viewCount = 1151,
        ingredients = listOf<String>(),
        cookingSteps = listOf<String>(),
        cookingTime = "Unknown",
        comments = listOf<CommentItem>(),
        ration = 0,
        createdAt = "2023-06-15 10:20:00"
    )
    return navController.createGraph("mainAct") {
        composable("mainAct") {
            MainAct(navController) { selectedRecipe ->
                recipe = selectedRecipe
                navController.navigate("recipeView")
            }
        }
        composable("recipeView") {
            RecipeViewScreen(navController, recipe)
        }
    }
}

@Preview
@Composable
fun MainScreenPreview() {
    MainScreen()
}