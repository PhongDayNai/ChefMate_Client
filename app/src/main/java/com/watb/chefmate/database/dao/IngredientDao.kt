package com.watb.chefmate.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.watb.chefmate.database.entities.Ingredients
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredients: Ingredients): Long // Trả về ingredientId

    @Update
    suspend fun updateIngredient(ingredients: Ingredients)

    @Delete
    suspend fun deleteIngredient(ingredients: Ingredients)

    @Query("SELECT * FROM Ingredients ORDER BY ingredientName ASC")
    fun getAllIngredients(): Flow<List<Ingredients>>

    @Query("SELECT * FROM Ingredients WHERE ingredientId = :ingredientId")
    suspend fun getIngredientById(ingredientId: Int): Ingredients?

    @Query("SELECT * FROM Ingredients WHERE ingredientName = :name LIMIT 1")
    suspend fun getIngredientByName(name: String): Ingredients?
}
