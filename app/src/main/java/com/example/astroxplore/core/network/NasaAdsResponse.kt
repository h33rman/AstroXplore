package com.example.astroxplore.core.network

import com.example.astroxplore.features.feed.model.PaperModel
import kotlinx.serialization.Serializable

@Serializable
data class NasaAdsResponse(
    val response: NasaAdsData
)

@Serializable
data class NasaAdsData(
    val docs: List<PaperModel>,
    val numFound: Int,
    val start: Int
)
