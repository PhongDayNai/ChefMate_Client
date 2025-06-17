package com.watb.chefmate.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.watb.chefmate.database.entities.IngredientEntity
import com.watb.chefmate.database.entities.RecipeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity): Long

    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    @Query("SELECT * FROM recipes WHERE recipeId = :recipeId")
    fun getRecipeById(recipeId: Int): Flow<RecipeEntity?>

    @Query("SELECT * FROM recipes")
    fun getAllRecipes(): Flow<List<RecipeEntity>>

    @Query("DELETE FROM recipes WHERE recipeId = :recipeId")
    suspend fun deleteRecipeById(recipeId: Int)
}

@Dao
interface IngredientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: IngredientEntity)

    @Query("SELECT * FROM ingredients")
    fun getAllIngredients(): Flow<List<IngredientEntity>?>

    @Query("SELECT * FROM ingredients WHERE ingredientName LIKE '%' + :name + '%'")
    fun getIngredientByName(name: String): Flow<IngredientEntity?>

    @Query("SELECT * FROM ingredients WHERE ingredientId = :ingredientId")
    fun getIngredientById(ingredientId: Int): Flow<IngredientEntity?>

    @Query("DELETE FROM ingredients")
    suspend fun deleteAllIngredients()
}