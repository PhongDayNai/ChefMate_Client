package com.watb.chefmate.database.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.watb.chefmate.database.entities.Ingredients
import com.watb.chefmate.database.entities.RecipesIngredients

data class IngredientWithQuantity(
    @Embedded val recipesIngredients: RecipesIngredients, //
    @Relation(
        parentColumn = "ingredientId",
        entityColumn = "ingredientId"
    )
    val ingredients: Ingredients
)