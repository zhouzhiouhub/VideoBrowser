package com.example.videobrowser.utils

import java.util.Locale

object SearchQueryTerms {
    fun parse(query: String?): List<String> {
        return query
            ?.let(TextWhitespaceNormalizer::collapse)
            ?.lowercase(Locale.ROOT)
            ?.split(" ")
            ?.filter { term -> term.isNotBlank() }
            ?: emptyList()
    }

    fun containsAll(searchableText: String, terms: List<String>): Boolean {
        val normalizedText = searchableText.lowercase(Locale.ROOT)
        return terms.all { term -> normalizedText.contains(term) }
    }
}
