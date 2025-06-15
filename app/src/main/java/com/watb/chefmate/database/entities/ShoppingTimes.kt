package com.watb.chefmate.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "ShoppingTimes")
data class ShoppingTimes(
    @PrimaryKey(autoGenerate = true)
    val stId: Int = 0,
    val creationDate: Date
)

