package com.example.astroxplore.core.network

object NasaAdsQueryBuilder {

    enum class RefereedFilter {
        ALL, REFEREED_ONLY, PREPRINTS_ONLY
    }

    /**
     * Translates user preference tags into a unified Solr query
     */
    fun buildPreferenceQuery(
        tags: List<String>,
        refereedFilter: RefereedFilter = RefereedFilter.ALL
    ): String {
        if (tags.isEmpty()) {
            return "keyword:astro-ph AND property:eprint"
        }

        val catClauses = mutableListOf<String>()
        val topicClauses = mutableListOf<String>()

        for (tag in tags) {
            val cleanTag = if (tag.startsWith("#")) tag.substring(1) else tag

            if (cleanTag.startsWith("astro-ph")) {
                catClauses.add("keyword:\"$cleanTag\"")
            } else {
                topicClauses.add("(title:\"$cleanTag\" OR abs:\"$cleanTag\" OR keyword:\"$cleanTag\")")
            }
        }

        val baseQuery = when {
            catClauses.isNotEmpty() && topicClauses.isNotEmpty() -> {
                "((${catClauses.joinToString(" OR ")}) OR (${topicClauses.joinToString(" OR ")}))"
            }
            catClauses.isNotEmpty() -> "(${catClauses.joinToString(" OR ")})"
            topicClauses.isNotEmpty() -> "(${topicClauses.joinToString(" OR ")})"
            else -> "*:*"
        }

        return when (refereedFilter) {
            RefereedFilter.REFEREED_ONLY -> "$baseQuery AND property:refereed"
            RefereedFilter.PREPRINTS_ONLY -> "$baseQuery AND property:eprint"
            RefereedFilter.ALL -> baseQuery
        }
    }

    /**
     * Processes raw search input into an optimal ADS query
     */
    fun buildQuickSearchQuery(rawInput: String): String {
        val input = rawInput.trim()
        if (input.isEmpty()) return "*:*"

        // 1. Bibcode Pattern: 2026ApJ...900...12V
        val bibcodeRegex = Regex("^\\d{4}[A-Za-z0-9.&]{15}$")
        if (bibcodeRegex.matches(input)) {
            return "bibcode:\"$input\""
        }

        // 2. arXiv ID Pattern: 2308.12345 or arXiv:2308.12345
        val arxivRegex = Regex("^(arXiv:)?\\d{4}\\.\\d{4,5}(v\\d+)?$")
        if (arxivRegex.matches(input)) {
            val arxivId = if (input.startsWith("arXiv:")) input else "arXiv:$input"
            return "identifier:\"$arxivId\""
        }

        // 3. Author Pattern: "Ghez, A" or "author:Hawking"
        if (input.contains(",") || input.lowercase().startsWith("author:")) {
            val author = if (input.lowercase().startsWith("author:")) {
                input.substring(7).trim()
            } else {
                input
            }
            return "author:\"$author\""
        }

        // 4. Fallback: Title and Abstract
        return "(title:\"$input\" OR abs:\"$input\" OR author:\"$input\")"
    }
}
