package com.example.videobrowser.browser.search

/**
 * 测试阅读提示：
 * 这个测试文件验证“Search Suggestion Client Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchSuggestionClientTest {
    /**
     * 测试函数 `parseOpenSearchSuggestions_readsArraySecondItem`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `parse Open Search Suggestions reads Array Second Item` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun parseOpenSearchSuggestions_readsArraySecondItem() {
        val suggestions = SearchSuggestionClient.parseSuggestions(
            """["同",["同花顺","同程旅行"]]"""
        )

        assertEquals(listOf("同花顺", "同程旅行"), suggestions)
    }

    @Test
    fun retired360SuggestionEndpointIsNotReferenced() {
        val source = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/SearchSuggestionClient.kt"
        ).readText()

        assertFalse(source.contains("sug.so.360.cn"))
        assertFalse(source.contains("\"so\" ->"))
    }

    /**
     * 测试函数 `parseSuggestions_ignoresMalformedPayload`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `parse Suggestions ignores Malformed Payload` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun parseSuggestions_ignoresMalformedPayload() {
        assertEquals(emptyList<String>(), SearchSuggestionClient.parseSuggestions("not json"))
    }

    /**
     * 测试函数 `disposeShutsDownExecutor`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `dispose Shuts Down Executor` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun disposeShutsDownExecutor() {
        val executor = java.util.concurrent.Executors.newSingleThreadExecutor()
        val client = SearchSuggestionClient(executor)

        client.dispose()

        assertTrue(executor.isShutdown)
    }
}
