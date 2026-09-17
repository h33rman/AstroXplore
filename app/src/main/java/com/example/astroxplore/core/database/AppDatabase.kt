package com.example.astroxplore.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.astroxplore.core.database.dao.FeedPaperDao
import com.example.astroxplore.core.database.dao.GroupDao
import com.example.astroxplore.core.database.dao.KeywordDao
import com.example.astroxplore.core.database.dao.SavedPaperDao
import com.example.astroxplore.core.database.entity.FeedPaperEntity
import com.example.astroxplore.core.database.entity.GroupEntity
import com.example.astroxplore.core.database.entity.KeywordEntity
import com.example.astroxplore.core.database.entity.SavedPaperEntity
import com.example.astroxplore.core.database.entity.UserPreferenceEntity

@Database(
    entities = [
        KeywordEntity::class, 
        UserPreferenceEntity::class, 
        SavedPaperEntity::class,
        FeedPaperEntity::class,
        GroupEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun keywordDao(): KeywordDao
    abstract fun savedPaperDao(): SavedPaperDao
    abstract fun feedPaperDao(): FeedPaperDao
    abstract fun groupDao(): GroupDao
}
