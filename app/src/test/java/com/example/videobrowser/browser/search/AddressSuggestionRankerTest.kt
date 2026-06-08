package com.example.videobrowser.browser.search

import com.example.videobrowser.storage.SavedPage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddressSuggestionRankerTest {
    @Test
    fun build_historyMatchesTitleAndUrlBeforeRemoteSuggestionsAndFallback() {
        val suggestions = AddressSuggestionRanker.build(
            input = "同",
            history = listOf(
                SavedPage(
                    title = "同花顺官网",
                    url = "https://stock.example.com/tonghuashun"
                ),
                SavedPage(
                    title = "新闻",
                    url = "https://example.com/%E5%90%8C%E5%9F%8E"
                )
            ),
            remoteKeywords = listOf("同花顺", "同程旅行"),
            includePrivateSources = true
        )

        assertEquals(5, suggestions.size)
        assertTrue(suggestions[0] is AddressSuggestion.History)
        assertTrue(suggestions[1] is AddressSuggestion.History)
        assertTrue(suggestions[2] is AddressSuggestion.Remote)
        assertTrue(suggestions[3] is AddressSuggestion.Remote)
        assertTrue(suggestions[4] is AddressSuggestion.Fallback)

        assertEquals("同花顺官网", (suggestions[0] as AddressSuggestion.History).title)
        assertEquals("https://example.com/同城", (suggestions[1] as AddressSuggestion.History).displayUrl)
        assertEquals("同花顺", (suggestions[2] as AddressSuggestion.Remote).keyword)
        assertEquals("同程旅行", (suggestions[3] as AddressSuggestion.Remote).keyword)
        assertEquals("同", (suggestions[4] as AddressSuggestion.Fallback).keyword)
    }

    @Test
    fun build_privateModeOnlyShowsFallback() {
        val suggestions = AddressSuggestionRanker.build(
            input = "同",
            history = listOf(SavedPage(title = "同花顺官网", url = "https://example.com")),
            remoteKeywords = listOf("同花顺", "同程旅行"),
            includePrivateSources = false
        )

        assertEquals(listOf(AddressSuggestion.Fallback("同")), suggestions)
    }

    @Test
    fun build_removesDuplicateRemoteKeywords() {
        val suggestions = AddressSuggestionRanker.build(
            input = "he",
            history = emptyList(),
            remoteKeywords = listOf("Hello", " hello ", "HELLO", "help"),
            includePrivateSources = true
        )

        assertEquals(
            listOf(
                AddressSuggestion.Remote("Hello"),
                AddressSuggestion.Remote("help"),
                AddressSuggestion.Fallback("he")
            ),
            suggestions
        )
    }
}
