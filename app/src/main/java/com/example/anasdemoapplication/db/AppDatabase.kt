package com.example.anasdemoapplication.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [UserHoldingEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userHoldingDao(): UserHoldingDao
}