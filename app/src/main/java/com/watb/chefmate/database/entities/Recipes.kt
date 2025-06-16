package com.watb.chefmate.database.entities

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val recipeId: Int = 0,
    val recipeName: String,
    val image: Bitmap,
    val userName: String,
    val isPublic: Boolean,
    val likeQuantity: Int,
    val cookTime: String,
    val ration: Int,
    val viewCount: Int,
    val createdAt: String
)
