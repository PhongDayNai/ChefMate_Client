package com.watb.chefmate.ui.main

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import androidx.navigation.navArgument
import com.watb.chefmate.database.AppDatabase
import com.watb.chefmate.repository.RecipeRepository
import com.watb.chefmate.data.CommentItem
import com.watb.chefmate.data.CookingStep
import com.watb.chefmate.data.IngredientItem
import com.watb.chefmate.data.Recipe
import com.watb.chefmate.database.entities.TagEntity
import com.watb.chefmate.repository.ShoppingTimeRepository
import com.watb.chefmate.ui.account.EditProfileScreen
import com.watb.chefmate.ui.account.SignInScreen
import com.watb.chefmate.ui.account.SignUpScreen
import com.watb.chefmate.ui.makeshoppinglist.ConsolidatedIngredientsScreen
import com.watb.chefmate.ui.makeshoppinglist.MakeShoppingListScreen
import com.watb.chefmate.ui.makeshoppinglist.ShoppingHistoryScreen
import com.watb.chefmate.ui.network.NetworkStatusWrapper
import com.watb.chefmate.ui.recipe.AddOrEditRecipeScreen
import com.watb.chefmate.ui.recipe.PostedRecipeList
import com.watb.chefmate.ui.recipe.RecipeViewScreen
import com.watb.chefmate.ui.recipe.SearchResultScreen
import com.watb.chefmate.ui.theme.ChefMateTheme
import com.watb.chefmate.viewmodel.RecipeViewModel
import com.watb.chefmate.viewmodel.ShoppingTimeViewModel
import com.watb.chefmate.viewmodel.UserViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChefMateTheme {
                MainScreen(this)
            }
        }
    }
}

@Composable
fun MainScreen(activity: Activity) {
    val context = LocalContext.current
    val appDatabase = AppDatabase.getDatabase(context)
    val recipeRepository = RecipeRepository(appDatabase.recipeDao(), appDatabase.ingredientDao(), appDatabase.tagDao())
    val shoppingTimeRepository = ShoppingTimeRepository(appDatabase.shoppingTimeDao())
    val navController = rememberNavController()
    val userViewModel: UserViewModel = viewModel()
    userViewModel.isLoggedIn(context)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .systemBarsPadding()
    ) {
        NetworkStatusWrapper(
            activity = activity,
        ) {
            NavHost(navController = navController, graph = navGraph(activity, navController, userViewModel, recipeRepository, shoppingTimeRepository))
        }
    }
}

fun navGraph(
    activity: Activity,
    navController: NavController,
    userViewModel: UserViewModel,
    recipeRepository: RecipeRepository,
    shoppingTimeRepository: ShoppingTimeRepository
): NavGraph {
    var recipe = Recipe(
        recipeId = -1,
        image = "",
        recipeName = "",
        userId = -1,
        userName = "Admin",
        likeQuantity = 100,
        viewCount = 1151,
        ingredients = listOf<IngredientItem>(),
        cookingSteps = listOf<CookingStep>(),
        cookingTime = "Unknown",
        comments = listOf<CommentItem>(),
        ration = 0,
        createdAt = "2023-06-15 10:20:00",
        tags = listOf<TagEntity>()
    )
    val recipeViewModel = RecipeViewModel(recipeRepository)
    val shoppingTimeViewModel = ShoppingTimeViewModel(shoppingTimeRepository)
    return navController.createGraph("splash") {
        composable("splash") {
            SplashScreen(navController)
        }
        composable("signIn") {
            SignInScreen(navController, userViewModel)
        }
        composable("signUp") {
            SignUpScreen(navController, userViewModel)
        }
        composable("editProfile") {
            EditProfileScreen(navController, userViewModel)
        }
        composable("mainAct") {
            MainAct(
                activity = activity,
                navController = navController,
                onRecipeClick = { selectedRecipe, isHistory ->
                    recipe = selectedRecipe
                    if (!isHistory) {
                        navController.navigate("recipeView")
                    } else {
                        navController.navigate("recipeViewHistory")
                    }
                },
                userViewModel = userViewModel,
                recipeViewModel = recipeViewModel,
                shoppingTimeViewModel = shoppingTimeViewModel
            )
        }
        composable("recipeView") {
            RecipeViewScreen(navController, recipe, userViewModel = userViewModel, recipeViewModel = recipeViewModel)
        }
        composable("recipeViewHistory") {
            RecipeViewScreen(navController, recipe, true, userViewModel, recipeViewModel)
        }
        composable(
            route = "add_edit_recipe/{recipeId}",
            arguments = listOf(navArgument("recipeId") {
                type = NavType.IntType
                defaultValue = -1
            })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getInt("recipeId") ?: 0
            AddOrEditRecipeScreen(navController = navController, recipeId = recipeId, userViewModel, recipeViewModel = recipeViewModel)
        }
        composable("searchRecipe/{searchType}/{searchValue}") { backStackEntry ->
            val searchType = backStackEntry.arguments?.getString("searchType")
            val searchValue = backStackEntry.arguments?.getString("searchValue")
            if (searchValue != null) {
                if (searchType != null) {
                    SearchResultScreen(
                        navController = navController,
                        searchTypeValue = searchType,
                        search = searchValue,
                        onRecipeClick = { selectedRecipe ->
                            recipe = selectedRecipe
                            navController.navigate("recipeView")
                        },
                        userViewModel = userViewModel,
                        recipeViewModel = recipeViewModel
                    )
                }
            }
        }
        composable("make_shopping_list_screen") {
            MakeShoppingListScreen(navController, recipeViewModel, shoppingTimeViewModel)
        }
        composable(
            route = "consolidated_ingredients_screen/{shoppingTimeId}",
            arguments = listOf(navArgument("shoppingTimeId") { type = NavType.IntType })
        ) { backStackEntry ->
            val shoppingTimeId = backStackEntry.arguments?.getInt("shoppingTimeId") ?: 0
            ConsolidatedIngredientsScreen(navController, shoppingTimeId, shoppingTimeViewModel = shoppingTimeViewModel)
        }
        composable(
            route = "shoppingHistory/{shoppingTimeId}",
            arguments = listOf(navArgument("shoppingTimeId") { type = NavType.IntType })
        ) { backStackEntry ->
            val shoppingTimeId = backStackEntry.arguments?.getInt("shoppingTimeId") ?: 0
            ConsolidatedIngredientsScreen(navController, shoppingTimeId, true, shoppingTimeViewModel)
        }
        composable("shoppingHistory") {
            ShoppingHistoryScreen(navController, shoppingTimeViewModel)
        }
        composable("personalRecipes") {
            PostedRecipeList(
                navController = navController,
                onRecipeClick = { selectedRecipe ->
                    recipe = selectedRecipe
                    navController.navigate("recipeView")
                },
                recipeViewModel = recipeViewModel
            )
        }
    }
}

@Preview
@Composable
fun MainScreenPreview() {
//    MainScreen()
}