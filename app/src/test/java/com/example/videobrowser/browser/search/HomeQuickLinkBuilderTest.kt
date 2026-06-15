package com.example.videobrowser.browser.search

import com.example.videobrowser.storage.SavedPage
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeQuickLinkBuilderTest {
    @Test
    fun fromHistory_keepsRecentDistinctPagesAndExcludesPinnedUrls() {
        val links = HomeQuickLinkBuilder.fromHistory(
            history = listOf(
                SavedPage(title = "Pinned", url = "https://pinned.example.com"),
                SavedPage(title = "Video", url = "https://video.example.com/watch"),
                SavedPage(title = "Video again", url = "https://video.example.com/watch"),
                SavedPage(title = "Docs", url = "https://docs.example.com"),
                SavedPage(title = "Search Home", url = "https://m.baidu.com/")
            ),
            excludedUrls = listOf("https://pinned.example.com", "https://m.baidu.com/"),
            limit = 4
        )

        assertEquals(
            listOf(
                HomeQuickLink("Video", "https://video.example.com/watch"),
                HomeQuickLink("Docs", "https://docs.example.com")
            ),
            links
        )
    }

    @Test
    fun fromHistory_rejectsNonWebUrlsAndFallsBackToHostTitle() {
        val links = HomeQuickLinkBuilder.fromHistory(
            history = listOf(
                SavedPage(title = "Script", url = "javascript:alert(1)"),
                SavedPage(title = "", url = "https://news.example.com/today")
            ),
            excludedUrls = emptyList()
        )

        assertEquals(
            listOf(HomeQuickLink("news.example.com", "https://news.example.com/today")),
            links
        )
    }

    @Test
    fun fromHistory_respectsLimit() {
        val links = HomeQuickLinkBuilder.fromHistory(
            history = (1..6).map { index ->
                SavedPage(title = "Site $index", url = "https://example.com/$index")
            },
            excludedUrls = emptyList(),
            limit = 3
        )

        assertEquals(3, links.size)
        assertEquals("Site 1", links.first().title)
        assertEquals("Site 3", links.last().title)
    }
}
