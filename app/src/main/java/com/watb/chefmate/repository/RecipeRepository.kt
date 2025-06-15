package com.watb.chefmate.repository

import com.watb.chefmate.database.dao.*
import com.watb.chefmate.database.entities.*
import com.watb.chefmate.database.relations.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

class RecipeRepository(private val recipeDao: RecipeDao) {

    // Thêm phương thức này để lấy tất cả các công thức
    fun getAllRecipes(): Flow<List<Recipes>> {
        return recipeDao.getAllRecipes()
    }

    suspend fun insertRecipe(recipe: Recipes): Long {
        return recipeDao.insertRecipe(recipe)
    }

    suspend fun updateRecipe(recipe: Recipes) {
        recipeDao.updateRecipe(recipe)
    }

    suspend fun insertIngredient(ingredient: Ingredients): Long {
        return recipeDao.insertIngredient(ingredient)
    }

    fun getIngredientByName(name: String): Flow<Ingredients?> {
        return recipeDao.getIngredientByName(name)
    }

    suspend fun insertRecipeIngredient(recipeIngredient: RecipesIngredients) {
        recipeDao.insertRecipeIngredient(recipeIngredient)
    }

    suspend fun deleteRecipeIngredientsForRecipe(recipeId: Int) {
        recipeDao.deleteRecipeIngredientsForRecipe(recipeId)
    }

    suspend fun insertStep(step: Steps) {
        recipeDao.insertStep(step)
    }

    suspend fun deleteStepsForRecipe(recipeId: Int) {
        recipeDao.deleteStepsForRecipe(recipeId)
    }

    fun getRecipeWithIngredientsAndSteps(recipeId: Int): Flow<RecipeWithIngredientsAndSteps?> {
        return recipeDao.getRecipeWithIngredientsAndSteps(recipeId)
    }
}