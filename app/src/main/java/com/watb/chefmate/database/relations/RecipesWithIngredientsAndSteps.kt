package com.watb.chefmate.database.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.watb.chefmate.database.entities.*

data class RecipeWithIngredientsAndSteps(
    @Embedded val recipes: Recipes,
    @Relation(
        parentColumn = "recipeId",
        entityColumn = "recipeId"
    )
    val steps: List<Steps>,
    @Relation(
        entity = RecipesIngredients::class,
        parentColumn = "recipeId",
        entityColumn = "riId"
    )
    val ingredients: List<IngredientWithRecipeIngredient>
)

data class IngredientWithRecipeIngredient(
    @Embedded val recipesIngredients: RecipesIngredients,
    @Relation(
        parentColumn = "ingredientId",
        entityColumn = "ingredientId"
    )
    val ingredients: Ingredients
)