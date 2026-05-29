package com.example.videobrowser.browser

import org.junit.Assert.assertEquals
import org.junit.Test

class ResourceTypeResolverTest {
    @Test
    fun resolve_returnsDocumentForMainFrameRequests() {
        val type = ResourceTypeResolver.resolve(
            requestUrl = "https://example.com/watch",
            isForMainFrame = true
        )

        assertEquals(ResourceType.DOCUMENT, type)
    }

    @Test
    fun resolve_infersScriptAndImageFromHeaders() {
        assertEquals(
            ResourceType.SCRIPT,
            ResourceTypeResolver.resolve(
                requestUrl = "https://cdn.example.com/app",
                isForMainFrame = false,
                requestHeaders = mapOf("Sec-Fetch-Dest" to "script")
            )
        )
        assertEquals(
            ResourceType.IMAGE,
            ResourceTypeResolver.resolve(
                requestUrl = "https://cdn.example.com/banner",
                isForMainFrame = false,
                requestHeaders = mapOf("Accept" to "image/avif,image/webp,*/*")
            )
        )
    }

    @Test
    fun resolve_infersMediaFromUrlSuffix() {
        val type = ResourceTypeResolver.resolve(
            requestUrl = "https://video.example.com/live/playlist.m3u8?token=1",
            isForMainFrame = false
        )

        assertEquals(ResourceType.MEDIA, type)
    }

    @Test
    fun resolve_returnsUnknownWhenRequestIsAmbiguous() {
        val type = ResourceTypeResolver.resolve(
            requestUrl = "https://example.com/api/opaque",
            isForMainFrame = false,
            requestHeaders = mapOf("Accept" to "*/*")
        )

        assertEquals(ResourceType.UNKNOWN, type)
    }
}
