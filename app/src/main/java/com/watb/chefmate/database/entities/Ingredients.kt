package com.watb.chefmate.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Ingredients")
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true)
    val ingredientId: Int = 0,
    val ingredientName: String
)