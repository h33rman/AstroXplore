package com.example.astroxplore.core.database.dao

import androidx.room.*
import com.example.astroxplore.core.database.entity.FeedPaperEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedPaperDao {
    @Query("SELECT * FROM feed_papers ORDER BY cachedAt DESC")
    fun getCachedFeed(): Flow<List<FeedPaperEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPapers(papers: List<FeedPaperEntity>)

    @Query("DELETE FROM feed_papers")
    suspend fun clearFeed()

    @Transaction
    suspend fun refreshFeed(papers: List<FeedPaperEntity>) {
        clearFeed()
        insertPapers(papers)
    }
}
