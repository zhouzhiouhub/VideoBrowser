package com.example.videobrowser.functioncenter

import java.net.URI
import java.util.Locale

object BrowserSiteDataOriginSearch {
    fun filterOriginNames(origins: List<String>, query: String?): List<String> {
        return origins.filter { origin -> matches(origin, query) }
    }

    fun matches(origin: String, query: String?): Boolean {
        val terms = queryTerms(query)
        if (terms.isEmpty()) {
            return true
        }
        val searchableText = listOf(origin, originHost(origin))
            .joinToString(" ")
            .lowercase(Locale.ROOT)
        return terms.all { term -> searchableText.contains(term) }
    }

    private fun queryTerms(query: String?): List<String> {
        return query
            ?.trim()
            ?.lowercase(Locale.ROOT)
            ?.split(Regex("\\s+"))
            ?.filter { term -> term.isNotBlank() }
            ?: emptyList()
    }

    private fun originHost(origin: String): String {
        return runCatching { URI(origin).host }
            .getOrNull()
            .orEmpty()
    }
}
