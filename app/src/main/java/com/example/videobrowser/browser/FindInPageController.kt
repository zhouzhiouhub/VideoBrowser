package com.example.videobrowser.browser

class FindInPageController(
    findAll: (String) -> Unit,
    findNext: (Boolean) -> Unit,
    clearMatches: () -> Unit
) {
    private val findAll = findAll
    private val moveToMatch = findNext
    private val clearMatches = clearMatches

    var currentQuery: String? = null
        private set

    fun search(query: String): Boolean {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isEmpty()) {
            return false
        }
        currentQuery = normalizedQuery
        findAll(normalizedQuery)
        return true
    }

    fun findNext(forward: Boolean = true): Boolean {
        if (currentQuery.isNullOrBlank()) {
            return false
        }
        moveToMatch(forward)
        return true
    }

    fun clear() {
        currentQuery = null
        clearMatches()
    }
}
