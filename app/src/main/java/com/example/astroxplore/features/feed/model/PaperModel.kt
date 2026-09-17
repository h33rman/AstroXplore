package com.example.astroxplore.features.feed.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Serializable
data class PaperModel(
    @SerialName("bibcode") val bibcode: String,
    @SerialName("title") val rawTitles: List<String> = emptyList(),
    @SerialName("author") val authors: List<String> = emptyList(),
    @SerialName("abstract") val abstractText: String = "",
    @SerialName("identifier") val identifiers: List<String> = emptyList(),
    @SerialName("citation_count") val citationCount: Int = 0,
    @SerialName("pubdate") val rawPubDate: String? = null,
    @SerialName("keyword") val keywords: List<String> = emptyList()
) {
    val title: String
        get() = rawTitles.firstOrNull() ?: "Untitled Paper"

    val arxivId: String?
        get() = identifiers.firstOrNull { it.startsWith("arXiv:") }?.removePrefix("arXiv:")

    val pdfUrl: String?
        get() = arxivId?.let { "https://arxiv.org/pdf/$it.pdf" }

    val category: String
        get() = keywords.firstOrNull() ?: "General"

    val dateDisplay: String
        get() = try {
            val dateStr = rawPubDate?.replace("-00", "-01") ?: ""
            val date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US))
        } catch (e: Exception) {
            rawPubDate ?: ""
        }
}
