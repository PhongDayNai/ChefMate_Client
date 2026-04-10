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
    val tags: String,
    val createdAt: String
)

fun RecipeEntity.toRecipe(): Recipe {
    val ingredientNamesList = ingredientNames
        .split(";;;")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
    val ingredientWeightsList = ingredientWeights
        .split(";;;")
        .mapNotNull { it.trim().toIntOrNull() }
    val ingredientUnitsList = ingredientUnits
        .split(";;;")
        .map { it.trim() }

    val ingredientCount = minOf(
        ingredientNamesList.size,
        ingredientWeightsList.size,
        ingredientUnitsList.size
    )
    val ingredients = List(ingredientCount) { index ->
        IngredientItem(
            ingredientName = ingredientNamesList[index],
            weight = ingredientWeightsList[index],
            unit = ingredientUnitsList[index]
        )
    }

    val cookingSteps = cookingSteps
        .split(";;;")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .mapIndexed { index, stepContent ->
            CookingStep(
                indexStep = index + 1,
                stepContent = stepContent
            )
        }

    val tags = tags
        .split(";;;")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .mapIndexed { index, tag ->
            TagEntity(index, tag)
        }

    return Recipe(
        recipeId = recipeId,
        image = image,
        recipeName = recipeName,
        userId = 0,
        userName = userName,
        likeQuantity = likeQuantity,
        viewCount = viewCount,
        ingredients = ingredients,
        cookingSteps = cookingSteps,
        cookingTime = cookingTime,
        ration = ration,
        comments = emptyList(),
        createdAt = createdAt,
        tags = tags
    )
}

@Entity(tableName = "Ingredients")
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true) val ingredientId: Int = 0,
    val ingredientName: String
)

@Entity(tableName = "Tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val tagId: Int = 0,
    val tagName: String
)
