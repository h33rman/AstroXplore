package com.example.astroxplore.features.feed.data

import com.example.astroxplore.core.network.NasaAdsApiService
import com.example.astroxplore.features.feed.model.PaperModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaperRepository @Inject constructor(
    private val apiService: NasaAdsApiService
) {
    suspend fun getPapersByQuery(
        query: String,
        sort: String = "date desc",
        page: Int = 0,
        pageSize: Int = 20
    ): List<PaperModel> {
        val response = apiService.executeQuery(
            query = query,
            sort = sort,
            rows = pageSize,
            start = page * pageSize
        )
        return response.response.docs
    }

    suspend fun getDailyPreprints(
        category: String = "astro-ph",
        page: Int = 0,
        pageSize: Int = 20
    ): List<PaperModel> {
        val query = if (category.contains(" OR ") || category.contains(" AND ")) {
            "keyword:($category) AND property:eprint"
        } else if (category.contains(".")) {
            "keyword:\"$category\" AND property:eprint"
        } else {
            "keyword:$category AND property:eprint"
        }

        return getPapersByQuery(
            query = query,
            sort = "date desc",
            page = page,
            pageSize = pageSize
        )
    }

    suspend fun search(
        query: String,
        categoryFilter: String? = null,
        refereedOnly: Boolean = false,
        page: Int = 0,
        pageSize: Int = 20
    ): List<PaperModel> {
        var finalQuery = query
        if (!categoryFilter.isNullOrEmpty()) {
            finalQuery += " keyword:\"$categoryFilter\""
        }
        if (refereedOnly) {
            finalQuery += " property:refereed"
        }

        return getPapersByQuery(
            query = finalQuery,
            sort = "citation_count desc, pubdate desc",
            page = page,
            pageSize = pageSize
        )
    }
}
