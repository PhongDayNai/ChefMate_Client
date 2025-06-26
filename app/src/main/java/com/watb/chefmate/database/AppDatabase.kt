package com.watb.chefmate.database

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import com.watb.chefmate.database.dao.*
import com.watb.chefmate.database.entities.*
import androidx.room.Room

@Database(
    entities = [
        RecipeEntity::class,
        IngredientEntity::class,
        TagEntity::class,
        ShoppingTimeEntity::class,
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun tagDao(): TagDao
    abstract fun shoppingTimeDao(): ShoppingTimeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room
                    .databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "chefmate_database"
                    )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}