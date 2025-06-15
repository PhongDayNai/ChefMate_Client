package com.watb.chefmate.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "ShoppingRecipes",
    primaryKeys = ["srId"],
    foreignKeys = [
        ForeignKey(
            entity = ShoppingTimes::class,
            parentColumns = ["stId"],
            childColumns = ["stId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Recipes::class,
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
data class ShoppingRecipes(
    val srId: Int = 0,
    val stId: Int,
    val recipeId: Int
)

