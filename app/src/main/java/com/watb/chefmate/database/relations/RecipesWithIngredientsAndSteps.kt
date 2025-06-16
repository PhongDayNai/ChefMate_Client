package com.watb.chefmate.database.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.watb.chefmate.database.entities.*

data class RecipeWithIngredientsAndSteps(
    @Embedded val recipes: RecipeEntity,
    @Relation(
        parentColumn = "recipeId",
        entityColumn = "recipeId"
    )
    val steps: List<StepEntity>,
    @Relation(
        entity = RecipeIngredientEntity::class,
        parentColumn = "recipeId",
        entityColumn = "riId"
    )
    val ingredients: List<IngredientWithRecipeIngredient>
)

data class IngredientWithRecipeIngredient(
    @Embedded val recipesIngredients: RecipeIngredientEntity,
    @Relation(
        parentColumn = "ingredientId",
        entityColumn = "ingredientId"
    )
    val ingredients: IngredientEntity
)