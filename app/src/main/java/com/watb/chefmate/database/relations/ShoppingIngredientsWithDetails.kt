package com.watb.chefmate.database.relations

import androidx.room.Embedded
import com.watb.chefmate.database.entities.ShoppingIngredients

data class ShoppingIngredientsWithDetails(
    @Embedded val shoppingIngredients: ShoppingIngredients, //
    val ingredientName: String
)