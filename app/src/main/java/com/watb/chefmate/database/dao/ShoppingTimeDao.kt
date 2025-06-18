package com.watb.chefmate.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.watb.chefmate.database.entities.ShoppingTimeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingTimeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingTime(shoppingTime: ShoppingTimeEntity): Long

    @Query("SELECT * FROM ShoppingTimes ORDER BY createdDate DESC")
    fun getAllShoppingTimes(): Flow<List<ShoppingTimeEntity>>

    @Query("SELECT * FROM ShoppingTimes WHERE stId = :id")
    suspend fun getShoppingTimeById(id: Int): ShoppingTimeEntity

    @Query("UPDATE ShoppingTimes SET ingredientNames = :ingredientNames, ingredientWeights = :ingredientWeights, ingredientUnits = :ingredientUnits, buyingStatuses = :buyingStatuses WHERE stId = :id")
    suspend fun updateShoppingTimeById(id: Int, ingredientNames: String, ingredientWeights: String, ingredientUnits: String, buyingStatuses: String)
}
