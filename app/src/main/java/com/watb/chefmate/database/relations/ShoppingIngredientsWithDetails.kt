package com.watb.chefmate.database.relations

import androidx.room.Embedded
import com.watb.chefmate.database.entities.ShoppingIngredientEntity

data class ShoppingIngredientsWithDetails(
    @Embedded val shoppingIngredients: ShoppingIngredientEntity, //
    val ingredientName: String
)