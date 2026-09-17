package com.example.astroxplore.core.di

import android.content.Context
import androidx.room.Room
import com.example.astroxplore.core.database.AppDatabase
import com.example.astroxplore.core.database.dao.FeedPaperDao
import com.example.astroxplore.core.database.dao.GroupDao
import com.example.astroxplore.core.database.dao.KeywordDao
import com.example.astroxplore.core.database.dao.SavedPaperDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "astro_xplore_db"
        )
        .fallbackToDestructiveMigration(dropAllTables = true) // For development simplicity
        .build()
    }

    @Provides
    @Singleton
    fun provideKeywordDao(database: AppDatabase): KeywordDao {
        return database.keywordDao()
    }

    @Provides
    @Singleton
    fun provideSavedPaperDao(database: AppDatabase): SavedPaperDao {
        return database.savedPaperDao()
    }

    @Provides
    @Singleton
    fun provideFeedPaperDao(database: AppDatabase): FeedPaperDao {
        return database.feedPaperDao()
    }

    @Provides
    @Singleton
    fun provideGroupDao(database: AppDatabase): GroupDao {
        return database.groupDao()
    }
}
