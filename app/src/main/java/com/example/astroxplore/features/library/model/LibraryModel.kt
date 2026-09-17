package com.example.astroxplore.features.library.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SavedPaperModel(
    @SerialName("id") val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("bibcode") val bibcode: String,
    @SerialName("title") val title: String? = null,
    @SerialName("authors") val authors: String? = null,
    @SerialName("abstract") val abstract: String? = null,
    @SerialName("category") val category: String? = null,
    @SerialName("date_display") val dateDisplay: String? = null,
    @SerialName("citation_count") val citationCount: Int = 0
)
