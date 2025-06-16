package com.watb.chefmate.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "ShoppingIngredients",
    primaryKeys = ["siId"],
    foreignKeys = [
        ForeignKey(
            entity = ShoppingTimeEntity::class,
            parentColumns = ["stId"],
            childColumns = ["stId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = IngredientEntity::class,
            parentColumns = ["ingredientId"],
            childColumns = ["ingredientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["stId"]),
        Index(value = ["ingredientId"])
    ]
)
data class ShoppingIngredientEntity(
    val siId: Int = 0,
    val stId: Int,
    val ingredientId: Int,
    val weight: Double,
    val unit: String,
    val isBought: Boolean
)