package com.example.videobrowser.browser.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SearchEngineUrlToolsTest {
    private val soConfig = SearchEngineConfig(
        name = "360搜索",
        displayUrl = "https://m.so.com",
        searchTemplate = "https://m.so.com/s?q={keyword}",
        queryParam = "q",
        domains = listOf("m.so.com", "so.com"),
        resultPathRules = listOf("/s"),
        hideCss = listOf("form[action*=\"/s\"]"),
        hidePageSearchBox = true
    )

    @Test
    fun buildSearchUrl_replacesKeywordPlaceholderWithUtf8EncodedQuery() {
        assertEquals(
            "https://m.so.com/s?q=%E4%BD%A0%E5%A5%BD",
            SearchEngineUrlTools.buildSearchUrl(soConfig, "你好")
        )
        assertEquals(
            "https://m.so.com/s?q=hello+world",
            SearchEngineUrlTools.buildSearchUrl(soConfig, " hello   world ")
        )
    }

    @Test
    fun normalizeTemplate_acceptsCommonAdvancedPlaceholders() {
        assertEquals(
            "https://example.com/search?q={keyword}",
            SearchEngineUrlTools.normalizeTemplate("https://example.com/search?q=%s")
        )
        assertEquals(
            "https://example.com/search?q={keyword}",
            SearchEngineUrlTools.normalizeTemplate("https://example.com/search?q={searchTerms}")
        )
        assertNull(SearchEngineUrlTools.normalizeTemplate("https://example.com"))
    }

    @Test
    fun queryFromUrl_readsConfiguredQueryParameterFromMatchingSearchResultUrl() {
        assertEquals(
            "你好",
            SearchEngineUrlTools.queryFromUrl(
                soConfig,
                "https://m.so.com/s?q=%E4%BD%A0%E5%A5%BD&src=browser"
            )
        )
        assertEquals(
            "你好",
            SearchEngineUrlTools.queryFromUrl(soConfig, "https://www.so.com/s?q=你好")
        )
    }

    @Test
    fun queryFromUrl_rejectsOtherHostsPathsAndBlankQueries() {
        assertNull(
            SearchEngineUrlTools.queryFromUrl(
                soConfig,
                "https://example.com/s?q=%E4%BD%A0%E5%A5%BD"
            )
        )
        assertNull(SearchEngineUrlTools.queryFromUrl(soConfig, "https://m.so.com/other?q=你好"))
        assertNull(SearchEngineUrlTools.queryFromUrl(soConfig, "https://m.so.com/s?q="))
    }

    @Test
    fun queryFromUrl_usesConfiguredResultPathsAndCommonQueryParameterFallbacks() {
        val config = soConfig.copy(
            queryParam = "q",
            resultPathRules = listOf("/s", "search")
        )

        assertEquals(
            "你好",
            SearchEngineUrlTools.queryFromUrl(
                config,
                "https://m.so.com/search?keyword=%E4%BD%A0%E5%A5%BD"
            )
        )
        assertNull(SearchEngineUrlTools.queryFromUrl(config, "https://m.so.com/other?keyword=你好"))
    }

    @Test
    fun queryFromUrl_ignoresDisabledSearchEngineConfigs() {
        assertNull(
            SearchEngineUrlTools.queryFromUrl(
                soConfig.copy(enabled = false),
                "https://m.so.com/s?q=%E4%BD%A0%E5%A5%BD"
            )
        )
    }

    @Test
    fun queryParamFromTemplate_returnsPlaceholderParameterName() {
        assertEquals(
            "keyword",
            SearchEngineUrlTools.queryParamFromTemplate(
                "https://so.douyin.com/s?keyword={keyword}"
            )
        )
    }
}
