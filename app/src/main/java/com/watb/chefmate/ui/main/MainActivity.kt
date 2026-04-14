package com.watb.chefmate.ui.main

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
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
import com.watb.chefmate.ui.appflow.BepesChatScreen
import com.watb.chefmate.ui.appflow.DietNotesScreen
import com.watb.chefmate.ui.appflow.PantryScreen
import com.watb.chefmate.ui.home.HomeSearchScreen
import com.watb.chefmate.ui.makeshoppinglist.ConsolidatedIngredientsScreen
import com.watb.chefmate.ui.makeshoppinglist.MakeShoppingListScreen
import com.watb.chefmate.ui.makeshoppinglist.ShoppingHistoryScreen
import com.watb.chefmate.ui.network.NetworkStatusWrapper
import com.watb.chefmate.ui.recipe.AddOrEditRecipeScreen
import com.watb.chefmate.ui.recipe.PostedRecipeList
import com.watb.chefmate.ui.recipe.RecipeListScreen
import com.watb.chefmate.ui.recipe.RecipeViewScreen
import com.watb.chefmate.ui.recipe.SearchResultScreen
import com.watb.chefmate.ui.theme.ChefMateTheme
import com.watb.chefmate.viewmodel.AppFlowViewModel
import com.watb.chefmate.viewmodel.RecipeViewModel
import com.watb.chefmate.viewmodel.ShoppingTimeViewModel
import com.watb.chefmate.viewmodel.UserViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemNavigation()
        setContent {
            ChefMateTheme {
                MainScreen(this)
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemNavigation()
        }
    }

    private fun hideSystemNavigation() {
        WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.navigationBars())
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
    val recentRecipes = remember { mutableStateListOf<Recipe>() }
    val userViewModel: UserViewModel = viewModel()
    val appFlowViewModel: AppFlowViewModel = viewModel()
    userViewModel.isLoggedIn(context)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        NetworkStatusWrapper(
            activity = activity,
        ) {
            NavHost(
                navController = navController,
                graph = navGraph(
                    activity = activity,
                    navController = navController,
                    userViewModel = userViewModel,
                    appFlowViewModel = appFlowViewModel,
                    recipeRepository = recipeRepository,
                    shoppingTimeRepository = shoppingTimeRepository,
                    recentRecipes = recentRecipes
                )
            )
        }
    }
}

fun navGraph(
    activity: Activity,
    navController: NavController,
    userViewModel: UserViewModel,
    appFlowViewModel: AppFlowViewModel,
    recipeRepository: RecipeRepository,
    shoppingTimeRepository: ShoppingTimeRepository,
    recentRecipes: MutableList<Recipe>
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
    val openRecipe: (Recipe, Boolean) -> Unit = { selectedRecipe, isHistory ->
        recipe = selectedRecipe
        if (!isHistory) {
            recentRecipes.removeAll { it.recipeId == selectedRecipe.recipeId }
            recentRecipes.add(0, selectedRecipe)
            if (recentRecipes.size > 10) {
                recentRecipes.removeAt(recentRecipes.lastIndex)
            }
            navController.navigate("recipeView")
        } else {
            navController.navigate("recipeViewHistory")
        }
    }
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
                onRecipeClick = openRecipe,
                userViewModel = userViewModel,
                appFlowViewModel = appFlowViewModel,
                recipeViewModel = recipeViewModel,
                shoppingTimeViewModel = shoppingTimeViewModel
            )
        }
        composable(
            route = "homeSearch",
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(280)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(280)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(280)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(280)
                )
            }
        ) {
            HomeSearchScreen(
                navController = navController,
                userViewModel = userViewModel,
                recipeViewModel = recipeViewModel,
                recentRecipes = recentRecipes,
                onRecipeClick = { selectedRecipe -> openRecipe(selectedRecipe, false) }
            )
        }
        composable("dietNotes") {
            DietNotesScreen(
                navController = navController,
                userViewModel = userViewModel,
                appFlowViewModel = appFlowViewModel
            )
        }
        composable("pantry") {
            PantryScreen(
                navController = navController,
                userViewModel = userViewModel,
                appFlowViewModel = appFlowViewModel
            )
        }
        composable(
            route = "bepesChat?recipeId={recipeId}",
            arguments = listOf(
                navArgument("recipeId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getInt("recipeId") ?: -1
            BepesChatScreen(
                navController = navController,
                userViewModel = userViewModel,
                appFlowViewModel = appFlowViewModel,
                recipeId = recipeId,
                onOpenRecipe = { selectedRecipe ->
                    openRecipe(selectedRecipe, false)
                }
            )
        }
        composable("recipeView") {
            RecipeViewScreen(navController, recipe, userViewModel = userViewModel, recipeViewModel = recipeViewModel)
        }
        composable("recipeViewHistory") {
            RecipeViewScreen(navController, recipe, true, userViewModel, recipeViewModel)
        }
        composable("recipeStorage") {
            RecipeListScreen(
                navController = navController,
                onRecipeClick = { selectedRecipe ->
                    openRecipe(selectedRecipe, true)
                },
                viewModel = recipeViewModel
            )
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
                            openRecipe(selectedRecipe, false)
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
            ConsolidatedIngredientsScreen(navController, shoppingTimeId, recipeViewModel = recipeViewModel, shoppingTimeViewModel = shoppingTimeViewModel)
        }
        composable(
            route = "shoppingHistory/{shoppingTimeId}",
            arguments = listOf(navArgument("shoppingTimeId") { type = NavType.IntType })
        ) { backStackEntry ->
            val shoppingTimeId = backStackEntry.arguments?.getInt("shoppingTimeId") ?: 0
            ConsolidatedIngredientsScreen(navController, shoppingTimeId, true, recipeViewModel, shoppingTimeViewModel)
        }
        composable("shoppingHistory") {
            ShoppingHistoryScreen(navController, shoppingTimeViewModel)
        }
        composable("personalRecipes") {
            PostedRecipeList(
                navController = navController,
                onRecipeClick = { selectedRecipe ->
                    openRecipe(selectedRecipe, false)
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
