package com.example.videobrowser.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchQueryTermsTest {
    @Test
    fun `parse trims lowercases and splits whitespace runs`() {
        assertEquals(
            listOf("video", "example", "manual"),
            SearchQueryTerms.parse("  VIDEO\tExample\nmanual  ")
        )
        assertEquals(emptyList<String>(), SearchQueryTerms.parse("   "))
        assertEquals(emptyList<String>(), SearchQueryTerms.parse(null))
    }

    @Test
    fun `contains all matches normalized text`() {
        val terms = SearchQueryTerms.parse("video example")

        assertTrue(SearchQueryTerms.containsAll("Example Video Guide", terms))
        assertFalse(SearchQueryTerms.containsAll("Example Audio Guide", terms))
    }
}
