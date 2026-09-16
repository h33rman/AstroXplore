package com.example.astroxplore.core.database.dao

import androidx.room.*
import com.example.astroxplore.core.database.entity.KeywordEntity
import com.example.astroxplore.core.database.entity.UserPreferenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KeywordDao {
    @Query("SELECT * FROM keywords")
    fun getAllKeywords(): Flow<List<KeywordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeywords(keywords: List<KeywordEntity>)

    @Query("DELETE FROM keywords")
    suspend fun clearKeywords()

    @Query("SELECT * FROM user_preferences")
    fun getUserPreferences(): Flow<List<UserPreferenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPreferences(preferences: List<UserPreferenceEntity>)

    @Query("DELETE FROM user_preferences")
    suspend fun clearUserPreferences()

    @Transaction
    suspend fun syncUserPreferences(preferences: List<UserPreferenceEntity>) {
        clearUserPreferences()
        insertUserPreferences(preferences)
    }
}
