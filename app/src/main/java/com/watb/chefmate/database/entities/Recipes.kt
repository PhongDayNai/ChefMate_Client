package com.watb.chefmate.database.entities

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date


@Entity(tableName = "Recipes")
data class Recipes(
    @PrimaryKey(autoGenerate = true)
    val recipeId: Int = 0,
    val recipeName: String,
    val image: Bitmap, // Lưu URL/URI của ảnh thay vì Bitmap
    val userId: Int, // Giả định userId tồn tại, nếu không có bảng Users, cân nhắc bỏ qua FK
    val isPublic: Boolean,
    val likeQuantity: Int,
    val cookTime: String,
    val ration: Int,
    val viewCount: Int,
    val createAt: Date
)

