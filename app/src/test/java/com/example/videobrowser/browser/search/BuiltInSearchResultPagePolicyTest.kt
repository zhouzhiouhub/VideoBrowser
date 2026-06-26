package com.example.videobrowser.browser.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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
            defaultPolicy.searchQueryFromUrl("https://so.douyin.com/s?keyword=你好")
        )
        assertEquals(
            "你好",
            defaultPolicy.searchQueryFromUrl("https://www.baidu.com/s?wd=%E4%BD%A0%E5%A5%BD")
        )
        assertEquals(
            "hello",
            defaultPolicy.searchQueryFromUrl("https://www.google.com/search?q=hello")
        )
        assertEquals(
            "你好",
            defaultPolicy.searchQueryFromUrl("https://duckduckgo.com/?q=%E4%BD%A0%E5%A5%BD")
        )
        assertEquals(
            "视频",
            defaultPolicy.searchQueryFromUrl("https://search.bilibili.com/all?keyword=%E8%A7%86%E9%A2%91")
        )
        assertEquals(
            "爱情测试免费测试",
            defaultPolicy.searchQueryFromUrl(
                "https://m.baidu.com/from=844b/ssid=0/s?word=%E7%88%B1%E6%83%85%E6%B5%8B%E8%AF%95%E5%85%8D%E8%B4%B9%E6%B5%8B%E8%AF%95&rq=%E6%B5%8B%E8%AF%95"
            )
        )
    }

    @Test
    fun searchPageHideCssForUrl_returnsRulesOnlyForConfiguredSearchResults() {
        val defaultPolicy = BuiltInSearchResultPagePolicy(SearchProviders.defaults)

        assertTrue(
            defaultPolicy.searchPageHideCssForUrl("https://quark.sm.cn/s?q=%E4%BD%A0%E5%A5%BD")
                .contains("form[action*=\"/s\"]")
        )
        assertTrue(
            defaultPolicy.searchPageHideCssForUrl("https://so.douyin.com/s?keyword=你好")
                .contains("[role=\"search\"]")
        )
        assertTrue(
            defaultPolicy.searchPageHideCssForUrl(
                "https://m.baidu.com/from=844b/ssid=0/s?word=%E7%88%B1%E6%83%85"
            ).contains("#index-form")
        )
        assertTrue(defaultPolicy.searchPageHideCssForUrl("https://example.com/").isEmpty())
    }

    @Test
    fun isSearchResultResourceUrl_matchesOnlySearchProviderResources() {
        val defaultPolicy = BuiltInSearchResultPagePolicy(SearchProviders.defaults)

        assertTrue(
            defaultPolicy.isSearchResultResourceUrl(
                pageUrl = "https://m.baidu.com/s?word=%E4%BD%A0%E5%A5%BD",
                resourceUrl = "https://m.baidu.com/static/app.js"
            )
        )
        assertTrue(
            defaultPolicy.isSearchResultResourceUrl(
                pageUrl = "https://www.baidu.com/s?wd=%E4%BD%A0%E5%A5%BD",
                resourceUrl = "https://sp0.baidu.com/9_Q4simg2RQJ8t7jm9iCKT-xh_/tpl/resource.js"
            )
        )
        assertTrue(
            defaultPolicy.isSearchResultResourceUrl(
                pageUrl = "https://m.baidu.com/from=844b/ssid=0/s?word=%E7%88%B1%E6%83%85",
                resourceUrl = "https://m.baidu.com/static/app.js"
            )
        )
        assertFalse(
            defaultPolicy.isSearchResultResourceUrl(
                pageUrl = "https://m.baidu.com/s?word=%E4%BD%A0%E5%A5%BD",
                resourceUrl = "https://doubleclick.net/ad.js"
            )
        )
        assertFalse(
            defaultPolicy.isSearchResultResourceUrl(
                pageUrl = "https://example.com/search?q=test",
                resourceUrl = "https://example.com/app.js"
            )
        )
    }

    @Test
    fun retiredUnstableProvidersAreNotRecognizedAsBuiltInSearchResults() {
        val defaultPolicy = BuiltInSearchResultPagePolicy(SearchProviders.defaults)

        assertNull(defaultPolicy.searchQueryFromUrl("https://m.so.com/s?q=%E4%BD%A0%E5%A5%BD"))
        assertNull(
            defaultPolicy.searchQueryFromUrl(
                "https://m.zhihu.com/search?type=content&q=%E9%97%AE%E7%AD%94"
            )
        )
        assertNull(
            defaultPolicy.searchQueryFromUrl(
                "https://www.zhihu.com/search?type=content&q=%E9%97%AE%E7%AD%94"
            )
        )
        assertNull(defaultPolicy.searchQueryFromUrl("https://s.weibo.com/weibo?q=%E7%83%AD%E7%82%B9"))
        assertTrue(defaultPolicy.searchPageHideCssForUrl("https://m.so.com/s?q=test").isEmpty())
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
