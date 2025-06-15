package com.watb.chefmate.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.watb.chefmate.database.entities.Ingredients
import com.watb.chefmate.database.entities.Recipes
import com.watb.chefmate.database.entities.RecipesIngredients
import com.watb.chefmate.database.entities.Steps
import com.watb.chefmate.database.relations.RecipeWithIngredientsAndSteps
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    // --- Các thao tác với bảng Recipes ---
    @Query("SELECT * FROM Recipes")
    fun getAllRecipes(): Flow<List<Recipes>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipes): Long

    @Update
    suspend fun updateRecipe(recipe: Recipes)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: Ingredients): Long

    @Query("SELECT * FROM Ingredients WHERE ingredientName = :name LIMIT 1")
    fun getIngredientByName(name: String): Flow<Ingredients?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeIngredient(recipeIngredient: RecipesIngredients)

    @Query("DELETE FROM RecipesIngredients WHERE recipeId = :recipeId")
    suspend fun deleteRecipeIngredientsForRecipe(recipeId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStep(step: Steps)

    @Query("DELETE FROM Steps WHERE recipeId = :recipeId")
    suspend fun deleteStepsForRecipe(recipeId: Int)

    @Transaction
    @Query("SELECT * FROM Recipes WHERE recipeId = :recipeId")
    fun getRecipeWithIngredientsAndSteps(recipeId: Int): Flow<RecipeWithIngredientsAndSteps?>
}