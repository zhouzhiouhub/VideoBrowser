package com.example.videobrowser.browser.search

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchSuggestionClientTest {
    @Test
    fun parseOpenSearchSuggestions_readsArraySecondItem() {
        val suggestions = SearchSuggestionClient.parseSuggestions(
            """["同",["同花顺","同程旅行"]]"""
        )

        assertEquals(listOf("同花顺", "同程旅行"), suggestions)
    }

    @Test
    fun parseSo360Suggestions_readsResultWordFields() {
        val suggestions = SearchSuggestionClient.parseSuggestions(
            """{"errorcode":0,"result":[{"word":"同花顺"},{"word":"同城"}]}"""
        )

        assertEquals(listOf("同花顺", "同城"), suggestions)
    }

    @Test
    fun parseSuggestions_ignoresMalformedPayload() {
        assertEquals(emptyList<String>(), SearchSuggestionClient.parseSuggestions("not json"))
    }
}
