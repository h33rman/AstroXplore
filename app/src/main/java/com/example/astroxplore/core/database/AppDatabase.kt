package com.example.astroxplore.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.astroxplore.core.database.dao.KeywordDao
import com.example.astroxplore.core.database.entity.KeywordEntity
import com.example.astroxplore.core.database.entity.UserPreferenceEntity

@Database(
    entities = [KeywordEntity::class, UserPreferenceEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun keywordDao(): KeywordDao
}
