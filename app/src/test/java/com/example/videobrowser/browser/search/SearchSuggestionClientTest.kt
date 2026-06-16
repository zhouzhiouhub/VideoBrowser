package com.example.videobrowser.browser.search

/**
 * 测试阅读提示：
 * 这个测试文件验证“Search Suggestion Client Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchSuggestionClientTest {
    @Test
    fun parseOpenSearchSuggestions_readsArraySecondItem() {
        val suggestions = SearchSuggestionClient.parseSuggestions(
            """["同",["同花顺","同程旅行"]]"""
        )

        assertEquals(listOf("同花顺", "同程旅行"), suggestions)
    }

    @Test
    fun parseSo360Suggestions_readsResultWordFields() {
        val suggestions = SearchSuggestionClient.parseSuggestions(
            """{"errorcode":0,"result":[{"word":"同花顺"},{"word":"同城"}]}"""
        )

        assertEquals(listOf("同花顺", "同城"), suggestions)
    }

    @Test
    fun parseSuggestions_ignoresMalformedPayload() {
        assertEquals(emptyList<String>(), SearchSuggestionClient.parseSuggestions("not json"))
    }

    @Test
    fun disposeShutsDownExecutor() {
        val executor = java.util.concurrent.Executors.newSingleThreadExecutor()
        val client = SearchSuggestionClient(executor)

        client.dispose()

        assertTrue(executor.isShutdown)
    }
}
