package com.example.videobrowser.functioncenter

internal object SearchSummaryFormatter {
    fun current(query: String?, fallback: String): String {
        return query
            ?.takeIf { it.isNotBlank() }
            ?: fallback
    }
}
