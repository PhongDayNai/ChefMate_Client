package com.watb.chefmate.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "ShoppingRecipes",
    primaryKeys = ["srId"],
    foreignKeys = [
        ForeignKey(
            entity = ShoppingTimeEntity::class,
            parentColumns = ["stId"],
            childColumns = ["stId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["recipeId"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["stId"]),
        Index(value = ["recipeId"])
    ]
)
data class ShoppingRecipeEntity(
    val srId: Int = 0,
    val stId: Int,
    val recipeId: Int
)

