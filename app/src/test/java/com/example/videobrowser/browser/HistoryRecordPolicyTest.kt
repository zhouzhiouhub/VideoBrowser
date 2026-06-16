package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“History Record Policy Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryRecordPolicyTest {
    @Test
    fun shouldRecord_rejectsProviderHomeUrls() {
        val policy = HistoryRecordPolicy(
            homeUrls = {
                listOf(
                    "https://m.baidu.com/",
                    "https://m.sogou.com/",
                    "https://m.so.com/"
                )
            }
        )

        assertFalse(policy.shouldRecord("https://m.baidu.com/"))
        assertFalse(policy.shouldRecord("https://m.sogou.com/?from=browser"))
        assertFalse(policy.shouldRecord("https://m.so.com"))
    }

    @Test
    fun shouldRecord_rejectsCustomHomeUrl() {
        val policy = HistoryRecordPolicy(
            homeUrls = { listOf("https://portal.example.com/start") }
        )

        assertFalse(policy.shouldRecord("https://portal.example.com/start?source=browser"))
    }

    @Test
    fun shouldRecord_acceptsSearchResultsAndContentPages() {
        val policy = HistoryRecordPolicy(
            homeUrls = {
                listOf(
                    "https://m.baidu.com/",
                    "https://portal.example.com/start"
                )
            }
        )

        assertTrue(policy.shouldRecord("https://m.baidu.com/s?ie=utf-8&word=video"))
        assertTrue(policy.shouldRecord("https://portal.example.com/start/article"))
        assertTrue(policy.shouldRecord("https://video.example.com/watch/1"))
    }
}
