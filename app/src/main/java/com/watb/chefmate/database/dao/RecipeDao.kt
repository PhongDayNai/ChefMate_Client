package com.watb.chefmate.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.watb.chefmate.database.entities.IngredientEntity
import com.watb.chefmate.database.entities.RecipeEntity
import com.watb.chefmate.database.entities.RecipeIngredientEntity
import com.watb.chefmate.database.entities.StepEntity
import com.watb.chefmate.database.relations.RecipeWithIngredientsAndSteps
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    // --- Các thao tác với bảng Recipes ---
    @Query("SELECT * FROM Recipes")
    fun getAllRecipes(): Flow<List<RecipeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity): Long

    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: IngredientEntity): Long

    @Query("SELECT * FROM Ingredients WHERE ingredientName = :name LIMIT 1")
    fun getIngredientByName(name: String): Flow<IngredientEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeIngredient(recipeIngredient: RecipeIngredientEntity)

    @Query("DELETE FROM RecipesIngredients WHERE recipeId = :recipeId")
    suspend fun deleteRecipeIngredientsForRecipe(recipeId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStep(step: StepEntity)

    @Query("DELETE FROM Steps WHERE recipeId = :recipeId")
    suspend fun deleteStepsForRecipe(recipeId: Int)

    @Transaction
    @Query("SELECT * FROM Recipes WHERE recipeId = :recipeId")
    fun getRecipeWithIngredientsAndSteps(recipeId: Int): Flow<RecipeWithIngredientsAndSteps?>
}