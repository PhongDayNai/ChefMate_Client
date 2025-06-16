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
        RecipeEntity::class,
        IngredientEntity::class,
        RecipeIngredientEntity::class,
        StepEntity::class,
        ShoppingTimeEntity::class,
        ShoppingRecipeEntity::class,
        ShoppingIngredientEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class, BitmapConverter::class)
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
                    "recipe_app_database"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}