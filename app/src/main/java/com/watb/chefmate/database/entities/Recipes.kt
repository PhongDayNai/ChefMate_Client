package com.watb.chefmate.database.entities

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.watb.chefmate.data.Recipe

@Entity(tableName = "Recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val recipeId: Int = 0,
    val recipeName: String,
    val image: String,
    val userName: String,
    val isPublic: Boolean,
    val likeQuantity: Int,
    val cookTime: String,
    val ration: Int,
    val viewCount: Int,
    val ingredients: String,
    val cookingSteps: String,
    val createdAt: String
)

fun RecipeEntity.toRecipe(): Recipe {
    return Recipe(
        recipeId = recipeId,
        image = image,
        name = recipeName,
        author = userName,
        likesQuantity = likeQuantity,
        userViews = viewCount,
        ingredients = ingredients.split(";;;"), // hoặc map từ Relation nếu có
        cookingSteps = cookingSteps.split(";;;"), // hoặc map từ Relation nếu có
        cookingTime = cookTime,
        comments = emptyList() // hoặc map từ relation nếu có
    )
}
