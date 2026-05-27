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
        assertEquals(
            "https://m.baidu.com/s?ie=utf-8&word=" +
                "%E6%B7%B1%E5%9C%B3%E8%81%8C%E4%B8%9A%E6%8A%80%E6%9C%AF" +
                "%E5%AD%A6%E9%99%A2%E5%AD%A6%E8%B4%B9%E4%B8%80%E5%B9%B4" +
                "%E5%A4%9A%E5%B0%91%E9%92%B1",
            UrlUtils.resolveAddressInput("深圳职业技术学院学费一年多少钱", searchUrlPrefix)
        )
    }

    @Test
    fun resolveAddressInput_returnsNullForBlankInput() {
        assertNull(UrlUtils.resolveAddressInput("", searchUrlPrefix))
        assertNull(UrlUtils.resolveAddressInput("   ", searchUrlPrefix))
    }
}
