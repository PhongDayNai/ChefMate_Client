package com.watb.chefmate.database

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.watb.chefmate.database.dao.*
import com.watb.chefmate.database.entities.*
import androidx.room.Room
import com.watb.chefmate.database.converter.BitmapConverter
import com.watb.chefmate.database.converter.DateConverter

@Database(
    entities = [
        Recipes::class, // Đã đổi tên
        Ingredients::class, // Đã đổi tên
        RecipesIngredients::class, // Đã đổi tên
        Steps::class, // Đã đổi tên
        ShoppingTimes::class, // Thêm ShoppingTimes
        ShoppingRecipes::class, // Đã đổi tên
        ShoppingIngredients::class // Đã đổi tên
    ],
    version = 2, // Tăng version khi có thay đổi cấu trúc DB (thêm createAt, ShoppingTimes)
    exportSchema = false
)
@TypeConverters(DateConverter::class, BitmapConverter::class) // Đăng ký TypeConverter
abstract class AppDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun shoppingDao(): ShoppingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "recipe_app_database" // Tên file database
                )
                    .fallbackToDestructiveMigration() // Xóa DB khi nâng cấp version nếu không có migration path
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}