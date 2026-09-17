package com.example.astroxplore.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feed_papers")
data class FeedPaperEntity(
    @PrimaryKey
    val bibcode: String,
    val title: String,
    val authors: String, // Comma separated
    val abstractText: String,
    val category: String,
    val dateDisplay: String,
    val citationCount: Int,
    val cachedAt: Long = System.currentTimeMillis()
)
