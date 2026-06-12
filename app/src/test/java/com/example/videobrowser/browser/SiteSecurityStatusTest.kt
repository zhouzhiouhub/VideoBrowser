package com.example.videobrowser.browser

import org.junit.Assert.assertEquals
import org.junit.Test

class SiteSecurityStatusTest {
    @Test
    fun fromUrl_marksHttpsSecure() {
        val status = SiteSecurityStatus.fromUrl("https://example.com")

        assertEquals(SiteSecurityStatus.SECURE, status)
        assertEquals("HTTPS", status.protocolDisplayName())
        assertEquals(true, status.isEncryptedConnection())
    }

    @Test
    fun fromUrl_marksHttpNotSecure() {
        val status = SiteSecurityStatus.fromUrl("http://example.com")

        assertEquals(SiteSecurityStatus.NOT_SECURE, status)
        assertEquals("HTTP", status.protocolDisplayName())
        assertEquals(false, status.isEncryptedConnection())
    }

    @Test
    fun fromUrl_ignoresNonWebAndBlankUrls() {
        assertEquals(SiteSecurityStatus.UNKNOWN, SiteSecurityStatus.fromUrl("about:blank"))
        assertEquals(SiteSecurityStatus.UNKNOWN, SiteSecurityStatus.fromUrl("mailto:a@example.com"))
        assertEquals(SiteSecurityStatus.UNKNOWN, SiteSecurityStatus.fromUrl(null))
        assertEquals("未知", SiteSecurityStatus.UNKNOWN.protocolDisplayName())
        assertEquals(false, SiteSecurityStatus.UNKNOWN.isEncryptedConnection())
    }
}
