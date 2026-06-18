package com.example.videobrowser.functioncenter

import com.example.videobrowser.utils.SearchQueryTerms
import com.example.videobrowser.utils.HostNameNormalizer

object BrowserSiteDataOriginSearch {
    fun filterOriginNames(origins: List<String>, query: String?): List<String> {
        return origins.filter { origin -> matches(origin, query) }
    }

    fun matches(origin: String, query: String?): Boolean {
        val terms = SearchQueryTerms.parse(query)
        if (terms.isEmpty()) {
            return true
        }
        val searchableText = listOf(origin, originHost(origin))
            .joinToString(" ")
        return SearchQueryTerms.containsAll(searchableText, terms)
    }

    private fun originHost(origin: String): String {
        return HostNameNormalizer.fromUrl(origin).orEmpty()
    }
}
