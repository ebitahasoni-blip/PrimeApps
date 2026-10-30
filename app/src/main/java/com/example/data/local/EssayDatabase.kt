package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [EssayEntity::class], version = 1, exportSchema = false)
abstract class EssayDatabase : RoomDatabase() {

    abstract fun essayDao(): EssayDao

    companion object {
        @Volatile
        private var INSTANCE: EssayDatabase? = null

        fun getDatabase(context: Context): EssayDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EssayDatabase::class.java,
                    "prime_essay_database.db"
                ).fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
