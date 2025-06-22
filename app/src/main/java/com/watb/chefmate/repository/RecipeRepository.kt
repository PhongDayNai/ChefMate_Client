package com.watb.chefmate.repository

import com.watb.chefmate.database.dao.*
import com.watb.chefmate.database.entities.*
import kotlinx.coroutines.flow.Flow

class RecipeRepository(
    private val recipeDao: RecipeDao,
    private val ingredientDao: IngredientDao,
    private val tagDao: TagDao
) {
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

    suspend fun getRecipesByIds(ids: List<Int>) = recipeDao.getRecipesByIds(ids)

    fun getRecipeByName(name: String): Flow<RecipeEntity?> {
        return recipeDao.getRecipeByName(name)
    }

    suspend fun deleteRecipeById(recipeId: Int) {
        recipeDao.deleteRecipeById(recipeId)
    }

    suspend fun insertIngredient(ingredient: IngredientEntity) {
        return ingredientDao.insertIngredient(ingredient)
    }

    fun getAllIngredients(): Flow<List<IngredientEntity>?> {
        return ingredientDao.getAllIngredients()
    }

    fun getIngredientByName(name: String): Flow<IngredientEntity?> {
        return ingredientDao.getIngredientByName(name)
    }

    fun getIngredientById(ingredientId: Int): Flow<IngredientEntity?> {
        return ingredientDao.getIngredientById(ingredientId)
    }

    suspend fun deleteAllIngredients() {
        ingredientDao.deleteAllIngredients()
    }

    suspend fun insertTag(tag: TagEntity) {
        return tagDao.insertTag(tag)
    }

    fun getAllTags(): Flow<List<TagEntity>?> {
        return tagDao.getAllTags()
    }

    fun getTagByName(name: String): Flow<TagEntity?> {
        return tagDao.getTagByName(name)
    }

    fun getTagById(tagId: Int): Flow<TagEntity?> {
        return tagDao.getTagById(tagId)
    }

    suspend fun deleteAllTags() {
        tagDao.deleteAllTags()
    }
}