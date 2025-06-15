package com.watb.chefmate.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "RecipesIngredients",
    primaryKeys = ["riId"],
    foreignKeys = [
        ForeignKey(
            entity = Recipes::class,
            parentColumns = ["recipeId"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE // Khi xóa recipe, các ingredient liên quan cũng bị xóa
        ),
        ForeignKey(
            entity = Ingredients::class,
            parentColumns = ["ingredientId"],
            childColumns = ["ingredientId"],
            onDelete = ForeignKey.CASCADE // Khi xóa ingredient, các liên kết cũng bị xóa
        )
    ],
    indices = [
        Index(value = ["recipeId"]), // Tạo index để tối ưu hóa truy vấn
        Index(value = ["ingredientId"])
    ]
)
data class RecipesIngredients(
    val riId: Int = 0, // Khóa chính tự động tăng
    val recipeId: Int,
    val ingredientId: Int,
    val weight: Int,
    val unit: String
)