package com.example.videobrowser.storage

/**
 * 测试阅读提示：
 * 这个测试文件验证“Saved Page Search Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Test

class SavedPageSearchTest {
    /**
     * 测试函数 `filterReturnsAllPagesForBlankQuery`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `filter Returns All Pages For Blank Query` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun filterReturnsAllPagesForBlankQuery() {
        val pages = samplePages()

        assertEquals(pages, SavedPageSearch.filter(pages, " "))
    }

    /**
     * 测试函数 `filterMatchesTitleAndUrlCaseInsensitively`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `filter Matches Title And Url Case Insensitively` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun filterMatchesTitleAndUrlCaseInsensitively() {
        val results = SavedPageSearch.filter(samplePages(), "video EXAMPLE")

        assertEquals(listOf("https://video.example.com/watch"), results.map { it.url })
    }

    /**
     * 测试函数 `filterRequiresEverySearchTerm`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `filter Requires Every Search Term` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun filterRequiresEverySearchTerm() {
        val results = SavedPageSearch.filter(samplePages(), "docs api")

        assertEquals(listOf("https://docs.example.com/api"), results.map { it.url })
    }

    /**
     * 测试函数 `filterMatchesBookmarkFolder`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `filter Matches Bookmark Folder` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun filterMatchesBookmarkFolder() {
        val results = SavedPageSearch.filter(samplePages(), "work")

        assertEquals(listOf("https://docs.example.com/api"), results.map { it.url })
    }

    /**
     * 测试函数 `samplePages`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `sample Pages` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun samplePages(): List<SavedPage> {
        return listOf(
            SavedPage(title = "Video Home", url = "https://video.example.com/watch"),
            SavedPage(title = "Documentation", url = "https://docs.example.com/api", folder = "Work"),
            SavedPage(title = "News", url = "https://news.example.com")
        )
    }
}
