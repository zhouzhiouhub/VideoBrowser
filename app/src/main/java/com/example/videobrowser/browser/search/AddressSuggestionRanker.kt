package com.example.videobrowser.browser.search

import com.example.videobrowser.storage.SavedPage
import com.example.videobrowser.utils.UrlUtils
import java.util.Locale

object AddressSuggestionRanker {
    fun build(
        input: String,
        history: List<SavedPage>,
        remoteKeywords: List<String>,
        includePrivateSources: Boolean,
        limit: Int = DEFAULT_LIMIT
    ): List<AddressSuggestion> {
        val keyword = input.trim()
        if (keyword.isEmpty() || limit <= 0) {
            return emptyList()
        }

        val fallback = AddressSuggestion.Fallback(keyword)
        if (!includePrivateSources || limit == 1) {
            return listOf(fallback)
        }

        val normalizedInput = normalize(keyword)
        val historySuggestions = historySuggestions(history, normalizedInput)
        val remoteSuggestions = remoteSuggestions(remoteKeywords, normalizedInput)
        return (historySuggestions + remoteSuggestions)
            .take(limit - 1) + fallback
    }

    private fun historySuggestions(
        history: List<SavedPage>,
        normalizedInput: String
    ): List<AddressSuggestion.History> {
        val seenUrls = linkedSetOf<String>()
        return history.mapNotNull { page ->
            val displayUrl = UrlUtils.displayUrl(page.url)
            val matches = normalize(page.title).contains(normalizedInput) ||
                normalize(displayUrl).contains(normalizedInput)
            val normalizedUrl = page.url.lowercase(Locale.ROOT)
            if (!matches || !seenUrls.add(normalizedUrl)) {
                null
            } else {
                AddressSuggestion.History(
                    title = page.title,
                    url = page.url,
                    displayUrl = displayUrl
                )
            }
        }
    }

    private fun remoteSuggestions(
        remoteKeywords: List<String>,
        normalizedInput: String
    ): List<AddressSuggestion.Remote> {
        val seenKeywords = linkedSetOf<String>()
        return remoteKeywords.mapNotNull { rawKeyword ->
            val keyword = rawKeyword.trim()
            val normalizedKeyword = normalize(keyword)
            if (
                keyword.isEmpty() ||
                normalizedKeyword == normalizedInput ||
                !seenKeywords.add(normalizedKeyword)
            ) {
                null
            } else {
                AddressSuggestion.Remote(keyword)
            }
        }
    }

    private fun normalize(value: String): String {
        return value.trim().lowercase(Locale.ROOT)
    }

    private const val DEFAULT_LIMIT = 6
}
