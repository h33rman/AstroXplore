package com.example.astroxplore.core.network

import retrofit2.http.GET
import retrofit2.http.Query

interface NasaAdsApiService {
    
    @GET("search/query")
    suspend fun executeQuery(
        @Query("q") query: String,
        @Query("fl") fields: String = DEFAULT_FIELDS,
        @Query("sort") sort: String = "date desc",
        @Query("rows") rows: Int = 20,
        @Query("start") start: Int = 0
    ): NasaAdsResponse

    companion object {
        const val BASE_URL = "https://api.adsabs.harvard.edu/v1/"
        const val DEFAULT_FIELDS = "bibcode,title,author,abstract,identifier,citation_count,pubdate,keyword"
    }
}
