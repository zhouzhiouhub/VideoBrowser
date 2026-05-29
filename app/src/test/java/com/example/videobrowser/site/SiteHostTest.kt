package com.example.videobrowser.site

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SiteHostTest {
    @Test
    fun fromUrl_extractsNormalizedHostFromHttpUrl() {
        assertEquals(
            "www.example.com",
            SiteHost.fromUrl("https://WWW.Example.COM:443/watch?v=1")
        )
    }

    @Test
    fun normalize_trimsCaseAndTrailingDots() {
        assertEquals("video.example.com", SiteHost.normalize(" Video.Example.COM. "))
    }

    @Test
    fun fromUrl_returnsNullForNonSiteUrls() {
        assertNull(SiteHost.fromUrl("about:blank"))
    }
}
