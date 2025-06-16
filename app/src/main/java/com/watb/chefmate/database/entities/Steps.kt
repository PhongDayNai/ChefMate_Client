package com.watb.chefmate.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "Steps",
    primaryKeys = ["stepId"],
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["recipeId"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["recipeId"])]
)
data class StepEntity(
    val stepId: Int = 0,
    val recipeId: Int,
    val index: Int,
    val content: String
)