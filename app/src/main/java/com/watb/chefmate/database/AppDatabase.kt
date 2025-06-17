package com.watb.chefmate.database

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.watb.chefmate.database.dao.*
import com.watb.chefmate.database.entities.*
import androidx.room.Room
import com.watb.chefmate.database.converter.DateConverter

@Database(
    entities = [
        RecipeEntity::class,
        ShoppingTimeEntity::class,
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao
    abstract fun shoppingTimeDao(): ShoppingTimeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chefmate_database"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}