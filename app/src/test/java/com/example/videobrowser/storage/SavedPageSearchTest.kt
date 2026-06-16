package com.example.videobrowser.storage

/**
 * 测试阅读提示：
 * 这个测试文件验证“Saved Page Search Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Test

class SavedPageSearchTest {
    @Test
    fun filterReturnsAllPagesForBlankQuery() {
        val pages = samplePages()

        assertEquals(pages, SavedPageSearch.filter(pages, " "))
    }

    @Test
    fun filterMatchesTitleAndUrlCaseInsensitively() {
        val results = SavedPageSearch.filter(samplePages(), "video EXAMPLE")

        assertEquals(listOf("https://video.example.com/watch"), results.map { it.url })
    }

    @Test
    fun filterRequiresEverySearchTerm() {
        val results = SavedPageSearch.filter(samplePages(), "docs api")

        assertEquals(listOf("https://docs.example.com/api"), results.map { it.url })
    }

    @Test
    fun filterMatchesBookmarkFolder() {
        val results = SavedPageSearch.filter(samplePages(), "work")

        assertEquals(listOf("https://docs.example.com/api"), results.map { it.url })
    }

    private fun samplePages(): List<SavedPage> {
        return listOf(
            SavedPage(title = "Video Home", url = "https://video.example.com/watch"),
            SavedPage(title = "Documentation", url = "https://docs.example.com/api", folder = "Work"),
            SavedPage(title = "News", url = "https://news.example.com")
        )
    }
}
