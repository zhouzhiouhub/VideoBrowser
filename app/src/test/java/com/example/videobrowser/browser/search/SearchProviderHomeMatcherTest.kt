package com.example.videobrowser.browser.search

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchProviderHomeMatcherTest {
    private val providers = listOf(
        SearchProvider(
            id = "example",
            name = "Example",
            badge = "E",
            homeUrl = "https://search.example.com/start",
            searchUrlPrefix = "https://search.example.com/search?q=",
            accentColor = 0
        )
    )

    @Test
    fun isProviderHomeUrl_matchesSamePageIgnoringQueryAndCase() {
        assertTrue(
            SearchProviderHomeMatcher.isProviderHomeUrl(
                "HTTPS://SEARCH.Example.COM/start?from=app",
                providers
            )
        )
    }

    @Test
    fun isProviderHomeUrl_rejectsDifferentPortPathAndNonWebUrls() {
        assertFalse(SearchProviderHomeMatcher.isProviderHomeUrl("https://search.example.com:444/start", providers))
        assertFalse(SearchProviderHomeMatcher.isProviderHomeUrl("https://search.example.com/start/news", providers))
        assertFalse(SearchProviderHomeMatcher.isProviderHomeUrl("about:blank", providers))
    }
}
