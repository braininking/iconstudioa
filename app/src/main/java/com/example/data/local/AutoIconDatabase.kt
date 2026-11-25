package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        IconThemeEntity::class,
        ThemedAppEntity::class,
        DetectionEventEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AutoIconDatabase : RoomDatabase() {
    abstract fun iconThemeDao(): IconThemeDao
    abstract fun themedAppDao(): ThemedAppDao
    abstract fun detectionEventDao(): DetectionEventDao

    companion object {
        @Volatile
        private var INSTANCE: AutoIconDatabase? = null

        fun getInstance(context: Context): AutoIconDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AutoIconDatabase::class.java,
                    "autoicon_studio.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
