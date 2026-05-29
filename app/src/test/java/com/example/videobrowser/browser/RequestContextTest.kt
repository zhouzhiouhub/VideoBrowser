package com.example.videobrowser.browser

import org.junit.Assert.assertEquals
import org.junit.Test

class RequestContextTest {
    @Test
    fun requestContext_normalizesRequestAndPageHosts() {
        val context = RequestContext(
            requestUrl = "https://CDN.Example.com/assets/player.js",
            pageUrl = "https://Video.Example.com/watch",
            method = "GET",
            requestHeaders = mapOf("Sec-Fetch-Dest" to "script")
        )

        assertEquals("cdn.example.com", context.requestHost)
        assertEquals("video.example.com", context.pageHost)
        assertEquals("https", context.requestScheme)
        assertEquals("GET", context.method)
        assertEquals(ResourceType.SCRIPT, context.resourceType)
    }
}
