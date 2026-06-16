package com.example.videobrowser.rules

/**
 * 测试阅读提示：
 * 这个测试文件验证“Rule Subscription Fetcher Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleSubscriptionFetcherTest {
    @Test
    fun normalizeSubscriptionUrl_acceptsOnlyHttpUrlsWithHost() {
        assertEquals(
            "https://example.com/filter.txt",
            RuleSubscriptionFetcher.normalizeSubscriptionUrl(" https://example.com/filter.txt ")
        )
        assertEquals(
            "http://example.com/filter.txt",
            RuleSubscriptionFetcher.normalizeSubscriptionUrl("http://example.com/filter.txt")
        )

        assertNull(RuleSubscriptionFetcher.normalizeSubscriptionUrl("file:///sdcard/filter.txt"))
        assertNull(RuleSubscriptionFetcher.normalizeSubscriptionUrl("javascript:alert(1)"))
        assertNull(RuleSubscriptionFetcher.normalizeSubscriptionUrl("https:/missing-host"))
        assertNull(RuleSubscriptionFetcher.normalizeSubscriptionUrl("https://user@example.com/filter.txt"))
    }

    @Test
    fun fetchText_rejectsInvalidUrlsBeforeOpeningConnection() {
        var opened = false
        val fetcher = RuleSubscriptionFetcher(
            openConnection = { url ->
                opened = true
                FakeHttpURLConnection(url)
            }
        )

        assertFailsWithMessage("HTTP/HTTPS") {
            fetcher.fetchText("file:///sdcard/filter.txt")
        }

        assertFalse(opened)
    }

    @Test
    fun fetchText_configuresConnectionAndReadsUtf8() {
        lateinit var connection: FakeHttpURLConnection
        val fetcher = RuleSubscriptionFetcher(
            networkTimeoutMs = 1234,
            openConnection = { url ->
                connection = FakeHttpURLConnection(
                    url = url,
                    body = "||ads.example^".toByteArray(Charsets.UTF_8)
                )
                connection
            }
        )

        val text = fetcher.fetchText("https://example.com/filter.txt")

        assertEquals("||ads.example^", text)
        assertEquals(1234, connection.connectTimeout)
        assertEquals(1234, connection.readTimeout)
        assertEquals("GET", connection.requestMethod)
        assertTrue(connection.instanceFollowRedirects)
        assertTrue(connection.disconnected)
    }

    @Test
    fun fetchText_rejectsHttpErrors() {
        val fetcher = RuleSubscriptionFetcher(
            openConnection = { url -> FakeHttpURLConnection(url, statusCode = 404) }
        )

        assertFailsWithMessage("HTTP 404") {
            fetcher.fetchText("https://example.com/filter.txt")
        }
    }

    @Test
    fun fetchText_rejectsContentLengthOverLimit() {
        lateinit var connection: FakeHttpURLConnection
        val fetcher = RuleSubscriptionFetcher(
            maxBytes = 3,
            openConnection = { url ->
                connection = FakeHttpURLConnection(url, contentLength = 4)
                connection
            }
        )

        assertFailsWithMessage("exceeds 3 bytes") {
            fetcher.fetchText("https://example.com/filter.txt")
        }

        assertFalse(connection.inputStreamOpened)
        assertTrue(connection.disconnected)
    }

    @Test
    fun fetchText_rejectsUnknownLengthResponseOverLimit() {
        val fetcher = RuleSubscriptionFetcher(
            maxBytes = 3,
            openConnection = { url ->
                FakeHttpURLConnection(
                    url = url,
                    body = "abcd".toByteArray(Charsets.UTF_8),
                    contentLength = -1
                )
            }
        )

        assertFailsWithMessage("exceeds 3 bytes") {
            fetcher.fetchText("https://example.com/filter.txt")
        }
    }

    @Test
    fun subscriptionIdForUrl_usesHostWhenValid() {
        assertEquals("example.com", RuleSubscriptionFetcher.subscriptionIdForUrl("https://example.com/filter.txt"))
        assertEquals("remote", RuleSubscriptionFetcher.subscriptionIdForUrl("file:///sdcard/filter.txt"))
    }

    private fun assertFailsWithMessage(message: String, action: () -> Unit) {
        val error = runCatching(action).exceptionOrNull()
            ?: error("Expected action to fail.")
        assertTrue(error.message.orEmpty().contains(message))
    }

    private class FakeHttpURLConnection(
        url: URL,
        private val statusCode: Int = 200,
        private val body: ByteArray = ByteArray(0),
        private val contentLength: Long = body.size.toLong()
    ) : HttpURLConnection(url) {
        var disconnected = false
            private set
        var inputStreamOpened = false
            private set

        override fun disconnect() {
            disconnected = true
        }

        override fun usingProxy(): Boolean {
            return false
        }

        override fun connect() {
            connected = true
        }

        override fun getResponseCode(): Int {
            return statusCode
        }

        override fun getInputStream(): InputStream {
            inputStreamOpened = true
            return ByteArrayInputStream(body)
        }

        override fun getContentLengthLong(): Long {
            return contentLength
        }
    }
}
