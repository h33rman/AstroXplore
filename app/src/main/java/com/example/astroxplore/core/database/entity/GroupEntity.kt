package com.example.astroxplore.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_clubs")
data class GroupEntity(
    @PrimaryKey
    val id: String,
    val displayId: String,
    val name: String,
    val description: String?,
    val ownerId: String,
    val focusArea: String?,
    val memberCount: Int,
    val createdAt: String?,
    val isSynced: Boolean = false
)
