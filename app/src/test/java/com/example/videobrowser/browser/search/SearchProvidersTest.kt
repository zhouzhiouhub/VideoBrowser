package com.example.videobrowser.browser.search

import com.example.videobrowser.settings.CustomSearchEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchProvidersTest {
    @Test
    fun all_appendsCustomSearchEnginesAfterBuiltIns() {
        val providers = SearchProviders.all(
            listOf(
                CustomSearchEngine(
                    id = "custom_docs",
                    name = "Docs",
                    searchUrlPrefix = "https://docs.example.com/search?q="
                )
            )
        )

        assertTrue(providers.take(SearchProviders.defaults.size) == SearchProviders.defaults)
        assertEquals("custom_docs", providers.last().id)
        assertEquals("Docs", providers.last().name)
        assertEquals("D", providers.last().badge)
        assertEquals(
            "https://docs.example.com/search?q=",
            providers.last().searchUrlPrefix
        )
    }
}
