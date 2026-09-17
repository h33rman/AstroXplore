package com.example.astroxplore.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_papers")
data class SavedPaperEntity(
    @PrimaryKey
    val bibcode: String,
    val title: String,
    val authors: String, // Stored as comma-separated
    val abstractText: String,
    val category: String,
    val dateDisplay: String,
    val citationCount: Int,
    val savedAt: Long = System.currentTimeMillis()
)
