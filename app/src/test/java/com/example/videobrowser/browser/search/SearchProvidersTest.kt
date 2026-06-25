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
    fun all_filtersRemovedBuiltInsAndKeepsOneFallbackProvider() {
        val withoutBaidu = SearchProviders.all(
            customSearchEngines = emptyList(),
            removedProviderIds = setOf("baidu")
        )
        assertTrue(withoutBaidu.none { provider -> provider.id == "baidu" })

        val fallback = SearchProviders.all(
            customSearchEngines = emptyList(),
            removedProviderIds = SearchProviders.defaults.map { provider -> provider.id }.toSet()
        )
        assertEquals(listOf(SearchProviders.DEFAULT_PROVIDER_ID), fallback.map { provider -> provider.id })
    }

    @Test
    fun all_mapsCustomSearchEngineTemplatesAndResultRules() {
        val providers = SearchProviders.all(
            listOf(
                CustomSearchEngine(
                    id = "custom_so",
                    name = "My 360",
                    searchUrlPrefix = "https://m.so.com/s?q=",
                    displayUrl = "https://m.so.com",
                    searchTemplate = "https://m.so.com/s?q={keyword}",
                    queryParam = "q",
                    domains = listOf("m.so.com", "so.com"),
                    resultPathRules = listOf("/s"),
                    hideCss = listOf("form[action*=\"/s\"]"),
                    hidePageSearchBox = true
                )
            )
        )
        val provider = providers.last()

        assertEquals("https://m.so.com/s?q=%E4%BD%A0%E5%A5%BD", provider.searchUrlFor("你好"))
        assertEquals("https://m.so.com/s?q={keyword}", provider.searchTemplate)
        assertEquals("q", provider.queryParam)
        assertEquals(listOf("m.so.com", "so.com"), provider.domains)
        assertEquals(listOf("/s"), provider.resultPathRules)
        assertTrue(provider.hidePageSearchBox)
    }

    @Test
    fun all_filtersDisabledCustomSearchEngines() {
        val providers = SearchProviders.all(
            listOf(
                CustomSearchEngine(
                    id = "custom_disabled",
                    name = "Disabled",
                    searchUrlPrefix = "https://disabled.example.com/search?q=",
                    enabled = false
                )
            )
        )

        assertTrue(providers.none { provider -> provider.id == "custom_disabled" })
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
