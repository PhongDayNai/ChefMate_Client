package com.watb.chefmate.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.watb.chefmate.data.CookingStep
import com.watb.chefmate.data.IngredientItem
import com.watb.chefmate.data.Recipe

@Entity(tableName = "Recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val recipeId: Int = 0,
    val recipeName: String,
    val image: String,
    val userName: String,
    val isPublic: Boolean,
    val likeQuantity: Int,
    val cookingTime: String,
    val ration: Int,
    val viewCount: Int,
    val ingredientNames: String,
    val ingredientWeights: String,
    val ingredientUnits: String,
    val cookingSteps: String,
    val createdAt: String
)

fun RecipeEntity.toRecipe(): Recipe {
    val ingredientNames = ingredientNames.split(";;;")
    val ingredientWeights = ingredientWeights.split(";;;").map { it.toInt() }
    val ingredientUnits = ingredientUnits.split(";;;")
    val ingredients = ingredientNames.indices.map { index ->
        IngredientItem(
            ingredientName = ingredientNames[index],
            weight = ingredientWeights[index],
            unit = ingredientUnits[index]
        )
    }

    val cookingStepContents = cookingSteps.split(";;;")
    val cookingSteps = cookingStepContents.indices.map { index ->
        CookingStep(
            indexStep = index + 1,
            content = cookingStepContents[index]
        )
    }

    return Recipe(
        recipeId = recipeId,
        image = image,
        recipeName = recipeName,
        userName = userName,
        likeQuantity = likeQuantity,
        viewCount = viewCount,
        ingredients = ingredients,
        cookingSteps = cookingSteps,
        cookingTime = cookingTime,
        ration = ration,
        comments = emptyList(),
        createdAt = createdAt
    )
}