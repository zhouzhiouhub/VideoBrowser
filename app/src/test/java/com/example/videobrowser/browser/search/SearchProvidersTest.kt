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

    @Test
    fun defaults_includeSearchEngineConfigsForResultRecognitionAndCleanup() {
        val providersById = SearchProviders.defaults.associateBy { provider -> provider.id }

        assertEquals(
            "https://m.so.com/s?q={keyword}",
            providersById.getValue("so").searchTemplate
        )
        assertEquals("q", providersById.getValue("so").queryParam)
        assertEquals(
            "https://so.douyin.com/s?keyword={keyword}",
            providersById.getValue("douyin").searchTemplate
        )
        assertEquals("keyword", providersById.getValue("douyin").queryParam)
        assertTrue(providersById.getValue("baidu").hidePageSearchBox)
        assertTrue(providersById.getValue("edge").domains.contains("bing.com"))
    }
}
