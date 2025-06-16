package com.watb.chefmate.database.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.watb.chefmate.database.entities.IngredientEntity
import com.watb.chefmate.database.entities.RecipeIngredientEntity

data class IngredientWithQuantity(
    @Embedded val recipesIngredients: RecipeIngredientEntity, //
    @Relation(
        parentColumn = "ingredientId",
        entityColumn = "ingredientId"
    )
    val ingredients: IngredientEntity
)