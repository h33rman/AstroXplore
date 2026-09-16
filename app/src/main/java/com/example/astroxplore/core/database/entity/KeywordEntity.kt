package com.example.astroxplore.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "keywords")
data class KeywordEntity(
    @PrimaryKey
    val name: String,
    val category: String? = null
)

@Entity(tableName = "user_preferences")
data class UserPreferenceEntity(
    @PrimaryKey
    val keyword: String
)
