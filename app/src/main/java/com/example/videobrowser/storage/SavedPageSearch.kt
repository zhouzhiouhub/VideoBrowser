package com.example.videobrowser.storage

import java.util.Locale

object SavedPageSearch {
    fun filter(pages: List<SavedPage>, query: String?): List<SavedPage> {
        val terms = query
            ?.trim()
            ?.lowercase(Locale.ROOT)
            ?.split(Regex("\\s+"))
            ?.filter { term -> term.isNotBlank() }
            ?: emptyList()
        if (terms.isEmpty()) {
            return pages
        }

        return pages.filter { page ->
            val haystack = "${page.title}\n${page.url}\n${page.folder}".lowercase(Locale.ROOT)
            terms.all { term -> haystack.contains(term) }
        }
    }
}
