package com.example.videobrowser.browser.search

sealed class AddressSuggestion {
    data class Bookmark(
        val title: String,
        val url: String,
        val displayUrl: String
    ) : AddressSuggestion()

    data class History(
        val title: String,
        val url: String,
        val displayUrl: String
    ) : AddressSuggestion()

    data class Remote(
        val keyword: String
    ) : AddressSuggestion()

    data class Fallback(
        val keyword: String
    ) : AddressSuggestion()
}
