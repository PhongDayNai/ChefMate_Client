package com.watb.chefmate.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.watb.chefmate.database.entities.IngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredients: IngredientEntity): Long // Trả về ingredientId

    @Update
    suspend fun updateIngredient(ingredients: IngredientEntity)

    @Delete
    suspend fun deleteIngredient(ingredients: IngredientEntity)

    @Query("SELECT * FROM Ingredients ORDER BY ingredientName ASC")
    fun getAllIngredients(): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM Ingredients WHERE ingredientId = :ingredientId")
    suspend fun getIngredientById(ingredientId: Int): IngredientEntity?

    @Query("SELECT * FROM Ingredients WHERE ingredientName = :name LIMIT 1")
    suspend fun getIngredientByName(name: String): IngredientEntity?
}
