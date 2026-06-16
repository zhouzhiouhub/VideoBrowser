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
    /**
     * 测试函数 `normalizeSubscriptionUrl_acceptsOnlyHttpUrlsWithHost`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `normalize Subscription Url accepts Only Http Urls With Host` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `fetchText_rejectsInvalidUrlsBeforeOpeningConnection`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `fetch Text rejects Invalid Urls Before Opening Connection` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `fetchText_configuresConnectionAndReadsUtf8`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `fetch Text configures Connection And Reads Utf8` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `fetchText_rejectsHttpErrors`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `fetch Text rejects Http Errors` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun fetchText_rejectsHttpErrors() {
        val fetcher = RuleSubscriptionFetcher(
            openConnection = { url -> FakeHttpURLConnection(url, statusCode = 404) }
        )

        assertFailsWithMessage("HTTP 404") {
            fetcher.fetchText("https://example.com/filter.txt")
        }
    }

    /**
     * 测试函数 `fetchText_rejectsContentLengthOverLimit`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `fetch Text rejects Content Length Over Limit` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `fetchText_rejectsUnknownLengthResponseOverLimit`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `fetch Text rejects Unknown Length Response Over Limit` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `subscriptionIdForUrl_usesHostWhenValid`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `subscription Id For Url uses Host When Valid` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun subscriptionIdForUrl_usesHostWhenValid() {
        assertEquals("example.com", RuleSubscriptionFetcher.subscriptionIdForUrl("https://example.com/filter.txt"))
        assertEquals("remote", RuleSubscriptionFetcher.subscriptionIdForUrl("file:///sdcard/filter.txt"))
    }

    /**
     * 测试函数 `assertFailsWithMessage`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `assert Fails With Message` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param message 参数类型为 `String`，表示函数执行 `message` 相关逻辑时需要读取或处理的输入。
     * @param action 参数类型为 `() -> Unit`，表示函数执行 `action` 相关逻辑时需要读取或处理的输入。
     */
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

        /**
         * 测试函数 `disconnect`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `disconnect` 这条行为是否成立。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         */
        override fun disconnect() {
            disconnected = true
        }

        /**
         * 测试函数 `usingProxy`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `using Proxy` 这条行为是否成立。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        override fun usingProxy(): Boolean {
            return false
        }

        /**
         * 测试函数 `connect`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `connect` 这条行为是否成立。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         */
        override fun connect() {
            connected = true
        }

        /**
         * 测试函数 `getResponseCode`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `get Response Code` 这条行为是否成立。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        override fun getResponseCode(): Int {
            return statusCode
        }

        /**
         * 测试函数 `getInputStream`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `get Input Stream` 这条行为是否成立。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        override fun getInputStream(): InputStream {
            inputStreamOpened = true
            return ByteArrayInputStream(body)
        }

        /**
         * 测试函数 `getContentLengthLong`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `get Content Length Long` 这条行为是否成立。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        override fun getContentLengthLong(): Long {
            return contentLength
        }
    }
}
