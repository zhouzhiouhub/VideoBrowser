package com.example.videobrowser.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WebPageIdentityTest {
    @Test
    fun from_normalizesSchemeHostDefaultPortAndPath() {
        assertEquals(
            WebPageIdentity(
                scheme = "https",
                host = "www.example.com",
                port = 443,
                path = "watch"
            ),
            WebPageIdentity.from("HTTPS://WWW.Example.COM/watch/?v=1#top")
        )
    }

    @Test
    fun isSamePageAs_ignoresQueryAndFragmentButKeepsPort() {
        val homePage = WebPageIdentity.from("https://example.com/start")!!

        assertTrue(homePage.isSamePageAs(WebPageIdentity.from("https://example.com/start?from=app")!!))
        assertFalse(homePage.isSamePageAs(WebPageIdentity.from("https://example.com:444/start")!!))
        assertFalse(homePage.isSamePageAs(WebPageIdentity.from("https://example.com/start/article")!!))
    }

    @Test
    fun from_rejectsNonWebUrlsAndMissingHosts() {
        assertNull(WebPageIdentity.from("about:blank"))
        assertNull(WebPageIdentity.from("https:/missing-host"))
        assertNull(WebPageIdentity.from(null))
    }
}
