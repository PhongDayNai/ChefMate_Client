package com.watb.chefmate.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.watb.chefmate.database.entities.IngredientEntity
import com.watb.chefmate.database.entities.RecipeEntity
import com.watb.chefmate.database.entities.TagEntity
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

    @Query("SELECT * FROM Recipes WHERE recipeId IN (:ids)")
    fun getRecipesByIds(ids: List<Int>): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE recipeName LIKE :name")
    fun getRecipeByName(name: String): Flow<RecipeEntity?>
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

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity)

    @Query("SELECT * FROM tags")
    fun getAllTags(): Flow<List<TagEntity>?>

    @Query("SELECT * FROM tags WHERE tagName LIKE '%' + :name + '%'")
    fun getTagByName(name: String): Flow<TagEntity?>

    @Query("SELECT * FROM tags WHERE tagId = :tagId")
    fun getTagById(tagId: Int): Flow<TagEntity?>

    @Query("DELETE FROM tags")
    suspend fun deleteAllTags()
}