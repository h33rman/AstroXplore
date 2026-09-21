package com.example.astroxplore.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.astroxplore.features.groups.model.GroupPaperModel

@Entity(tableName = "group_papers")
data class GroupPaperEntity(
    @PrimaryKey
    val id: String, // Supabase UUID
    val groupId: String,
    val bibcode: String,
    val addedBy: String,
    val voteCount: Int,
    val addedAt: String?,
    val isSynced: Boolean = true
) {
    fun toDomainModel() = GroupPaperModel(
        id = id,
        groupId = groupId,
        bibcode = bibcode,
        addedBy = addedBy,
        voteCount = voteCount,
        addedAt = addedAt
    )
}

fun GroupPaperModel.toEntity(isSynced: Boolean = true) = GroupPaperEntity(
    id = id ?: "",
    groupId = groupId,
    bibcode = bibcode,
    addedBy = addedBy,
    voteCount = voteCount,
    addedAt = addedAt,
    isSynced = isSynced
)
