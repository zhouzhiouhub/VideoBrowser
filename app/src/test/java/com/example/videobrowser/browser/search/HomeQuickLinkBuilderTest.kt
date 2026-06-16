package com.example.videobrowser.browser.search

/**
 * 测试阅读提示：
 * 这个测试文件验证“Home Quick Link Builder Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import com.example.videobrowser.storage.SavedPage
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeQuickLinkBuilderTest {
    /**
     * 测试函数 `fromHistory_keepsRecentDistinctPagesAndExcludesPinnedUrls`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `from History keeps Recent Distinct Pages And Excludes Pinned Urls` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `fromHistory_rejectsNonWebUrlsAndFallsBackToHostTitle`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `from History rejects Non Web Urls And Falls Back To Host Title` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `fromHistory_respectsLimit`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `from History respects Limit` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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
