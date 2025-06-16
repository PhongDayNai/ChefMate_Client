package com.watb.chefmate.ui.main

import android.annotation.SuppressLint
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import com.watb.chefmate.database.AppDatabase
import com.watb.chefmate.repository.RecipeRepository
import com.watb.chefmate.ui.recipe.AddRecipeScreen
import com.watb.chefmate.ui.recipe.RecipeListScreen
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
    val recipeViewModel = RecipeViewModel(repository)
    return navController.createGraph("mainAct") {
        composable("mainAct") {
            MainAct(navController, recipeViewModel)
        }
        composable("addRecipe") {
            AddRecipeScreen(navController, recipeId = -1, recipeViewModel)
        }
        composable("listRecipe") {
            RecipeListScreen(navController, recipeViewModel)
        }
    }
}

@Preview
@Composable
fun MainScreenPreview() {
    MainScreen()
}