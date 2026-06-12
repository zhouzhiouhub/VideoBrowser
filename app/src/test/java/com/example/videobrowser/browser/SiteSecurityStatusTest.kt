package com.example.videobrowser.browser

import org.junit.Assert.assertEquals
import org.junit.Test

class SiteSecurityStatusTest {
    @Test
    fun fromUrl_marksHttpsSecure() {
        assertEquals(
            SiteSecurityStatus.SECURE,
            SiteSecurityStatus.fromUrl("https://example.com")
        )
    }

    @Test
    fun fromUrl_marksHttpNotSecure() {
        assertEquals(
            SiteSecurityStatus.NOT_SECURE,
            SiteSecurityStatus.fromUrl("http://example.com")
        )
    }

    @Test
    fun fromUrl_ignoresNonWebAndBlankUrls() {
        assertEquals(SiteSecurityStatus.UNKNOWN, SiteSecurityStatus.fromUrl("about:blank"))
        assertEquals(SiteSecurityStatus.UNKNOWN, SiteSecurityStatus.fromUrl("mailto:a@example.com"))
        assertEquals(SiteSecurityStatus.UNKNOWN, SiteSecurityStatus.fromUrl(null))
    }
}
