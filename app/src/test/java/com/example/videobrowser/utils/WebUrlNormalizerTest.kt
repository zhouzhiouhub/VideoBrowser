package com.example.videobrowser.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WebUrlNormalizerTest {
    @Test
    fun `normalizes http and https urls with hosts`() {
        assertEquals(
            "https://example.com/page",
            WebUrlNormalizer.normalizeHttpOrHttpsUrl(" https://example.com/page ")
        )
        assertEquals(
            "http://example.com/page",
            WebUrlNormalizer.normalizeHttpOrHttpsUrl("http://example.com/page")
        )
    }

    @Test
    fun `rejects non web urls and missing hosts`() {
        assertNull(WebUrlNormalizer.normalizeHttpOrHttpsUrl("file:///sdcard/page.html"))
        assertNull(WebUrlNormalizer.normalizeHttpOrHttpsUrl("javascript:alert(1)"))
        assertNull(WebUrlNormalizer.normalizeHttpOrHttpsUrl("https:/missing-host"))
        assertNull(WebUrlNormalizer.normalizeHttpOrHttpsUrl(" "))
    }
}
