package com.example.videobrowser.browser.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BuiltInSearchResultPagePolicyTest {
    private val policy = BuiltInSearchResultPagePolicy(
        listOf(
            SearchProvider(
                id = "baidu",
                name = "百度",
                badge = "百",
                homeUrl = "https://m.baidu.com/",
                searchUrlPrefix = "https://m.baidu.com/s?ie=utf-8&word=",
                accentColor = 0
            ),
            SearchProvider(
                id = "sogou",
                name = "搜狗",
                badge = "搜",
                homeUrl = "https://m.sogou.com/",
                searchUrlPrefix = "https://www.sogou.com/web?query=",
                addressBarSearchUrlPrefixes = listOf(
                    "https://www.sogou.com/web?query=",
                    "https://m.sogou.com/web/searchList.jsp?s_from=pcsearch&keyword="
                ),
                accentColor = 0
            ),
            SearchProvider(
                id = "so",
                name = "360搜索",
                badge = "360",
                homeUrl = "https://m.so.com/",
                searchUrlPrefix = "https://www.so.com/s?q=",
                accentColor = 0
            )
        )
    )

    @Test
    fun searchQueryFromUrl_matchesBuiltInProviderPrefixes() {
        assertEquals(
            "你好1983",
            policy.searchQueryFromUrl(
                "https://m.sogou.com/web/searchList.jsp?s_from=pcsearch&keyword=%E4%BD%A0%E5%A5%BD1983"
            )
        )
        assertEquals(
            "你好",
            policy.searchQueryFromUrl("https://m.baidu.com/s?ie=utf-8&word=%E4%BD%A0%E5%A5%BD")
        )
    }

    @Test
    fun searchQueryFromUrl_matchesConfiguredSearchResultDomainsAndParams() {
        val defaultPolicy = BuiltInSearchResultPagePolicy(SearchProviders.defaults)

        assertEquals(
            "你好",
            defaultPolicy.searchQueryFromUrl("https://m.so.com/s?q=%E4%BD%A0%E5%A5%BD")
        )
        assertEquals(
            "你好",
            defaultPolicy.searchQueryFromUrl("https://so.douyin.com/s?keyword=你好")
        )
    }

    @Test
    fun isBuiltInSearchResultUrl_rejectsProviderHomesAndNormalPages() {
        assertTrue(
            policy.isBuiltInSearchResultUrl(
                "https://www.so.com/s?q=%E4%BD%A0%E5%A5%BD"
            )
        )
        assertFalse(policy.isBuiltInSearchResultUrl("https://m.baidu.com/"))
        assertFalse(policy.isBuiltInSearchResultUrl("https://example.com/search?q=%E4%BD%A0%E5%A5%BD"))
        assertFalse(policy.isBuiltInSearchResultUrl(null))
    }

    @Test
    fun searchQueryFromUrl_readsLatestProvidersFromSupplier() {
        var providers = emptyList<SearchProvider>()
        val dynamicPolicy = BuiltInSearchResultPagePolicy { providers }

        assertFalse(dynamicPolicy.isBuiltInSearchResultUrl("https://custom.example.com/search?q=test"))

        providers = listOf(
            SearchProvider(
                id = "custom_example",
                name = "Custom",
                badge = "C",
                homeUrl = "https://custom.example.com/search?q=",
                searchUrlPrefix = "https://custom.example.com/search?q=",
                accentColor = 0
            )
        )

        assertEquals(
            "test",
            dynamicPolicy.searchQueryFromUrl("https://custom.example.com/search?q=test")
        )
    }
}
