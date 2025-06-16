package com.watb.chefmate.repository

import com.watb.chefmate.database.dao.*
import com.watb.chefmate.database.entities.*
import com.watb.chefmate.database.relations.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

//class RecipeRepository(private val recipeDao: RecipeDao) {
//
//    // Thêm phương thức này để lấy tất cả các công thức
//    fun getAllRecipes(): Flow<List<RecipeEntity>> {
//        return recipeDao.getAllRecipes()
//    }
//
//    suspend fun insertRecipe(recipe: RecipeEntity): Long {
//        return recipeDao.insertRecipe(recipe)
//    }
//
//    suspend fun updateRecipe(recipe: RecipeEntity) {
//        recipeDao.updateRecipe(recipe)
//    }
//
//    suspend fun insertIngredient(ingredient: IngredientEntity): Long {
//        return recipeDao.insertIngredient(ingredient)
//    }
//
//    fun getIngredientByName(name: String): Flow<IngredientEntity?> {
//        return recipeDao.getIngredientByName(name)
//    }
//
//    suspend fun insertRecipeIngredient(recipeIngredient: RecipeIngredientEntity) {
//        recipeDao.insertRecipeIngredient(recipeIngredient)
//    }
//
//    suspend fun deleteRecipeIngredientsForRecipe(recipeId: Int) {
//        recipeDao.deleteRecipeIngredientsForRecipe(recipeId)
//    }
//
//    suspend fun insertStep(step: StepEntity) {
//        recipeDao.insertStep(step)
//    }
//
//    suspend fun deleteStepsForRecipe(recipeId: Int) {
//        recipeDao.deleteStepsForRecipe(recipeId)
//    }
//
//    fun getRecipeWithIngredientsAndSteps(recipeId: Int): Flow<RecipeWithIngredientsAndSteps?> {
//        return recipeDao.getRecipeWithIngredientsAndSteps(recipeId)
//    }
//}
class RecipeRepository(private val recipeDao: RecipeDao) {

    fun getAllRecipes(): Flow<List<RecipeEntity>> {
        return recipeDao.getAllRecipes()
    }

    suspend fun insertRecipe(recipe: RecipeEntity): Long {
        return recipeDao.insertRecipe(recipe)
    }

    suspend fun updateRecipe(recipe: RecipeEntity) {
        recipeDao.updateRecipe(recipe)
    }

    fun getRecipeById(recipeId: Int): Flow<RecipeEntity?> {
        return recipeDao.getRecipeById(recipeId)
    }

    suspend fun deleteRecipeById(recipeId: Int) {
        recipeDao.deleteRecipeById(recipeId)
    }

    // Removed all IngredientEntity and StepEntity related repository methods
    // Removed getRecipeWithIngredientsAndSteps
}