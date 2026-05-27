package com.example.videobrowser.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UrlUtilsTest {
    private val searchUrlPrefix = "https://m.baidu.com/s?ie=utf-8&word="

    @Test
    fun resolveAddressInput_keepsHttpAndHttpsUrls() {
        assertEquals(
            "http://example.com/path",
            UrlUtils.resolveAddressInput("http://example.com/path", searchUrlPrefix)
        )
        assertEquals(
            "https://example.com/path",
            UrlUtils.resolveAddressInput("  https://example.com/path  ", searchUrlPrefix)
        )
    }

    @Test
    fun resolveAddressInput_convertsDomainToHttpsUrl() {
        assertEquals(
            "https://example.com",
            UrlUtils.resolveAddressInput("example.com", searchUrlPrefix)
        )
        assertEquals(
            "https://example.com/video?q=1",
            UrlUtils.resolveAddressInput("example.com/video?q=1", searchUrlPrefix)
        )
    }

    @Test
    fun resolveAddressInput_convertsTextToSearchUrl() {
        assertEquals(
            "https://m.baidu.com/s?ie=utf-8&word=hello+world",
            UrlUtils.resolveAddressInput("hello world", searchUrlPrefix)
        )
    }

    @Test
    fun resolveAddressInput_encodesChineseSearchTextAsUtf8() {
        assertEquals(
            "https://m.baidu.com/s?ie=utf-8&word=%E5%A4%9A%E5%B0%91%E9%92%B1",
            UrlUtils.resolveAddressInput("多少钱", searchUrlPrefix)
        )
    }

    @Test
    fun resolveAddressInput_returnsNullForBlankInput() {
        assertNull(UrlUtils.resolveAddressInput("", searchUrlPrefix))
        assertNull(UrlUtils.resolveAddressInput("   ", searchUrlPrefix))
    }
}
