package com.example.videobrowser.site

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SiteAdapterRegistryTest {
    @Test
    fun scriptFilesFor_matchesKnownDomainsWithSubdomainBoundary() {
        val registry = SiteAdapterRegistry.default()

        assertEquals(
            listOf("scripts/youtube.js"),
            registry.scriptFilesFor("https://www.youtube.com/watch?v=1")
        )
        assertEquals(
            listOf("scripts/bilibili.js"),
            registry.scriptFilesFor("https://m.bilibili.com/video/BV1")
        )
        assertEquals(
            listOf("scripts/iqiyi.js"),
            registry.scriptFilesFor("https://iqiyi.com/")
        )
        assertEquals(
            listOf("scripts/tencent.js"),
            registry.scriptFilesFor("https://v.qq.com/x/cover/test.html")
        )
        assertEquals(
            listOf("scripts/youku.js"),
            registry.scriptFilesFor("https://www.youku.com/")
        )
    }

    @Test
    fun scriptFilesFor_rejectsLookalikeDomains() {
        val registry = SiteAdapterRegistry.default()

        assertTrue(registry.scriptFilesFor("https://notyoutube.com/").isEmpty())
        assertTrue(registry.scriptFilesFor("https://youtube.com.example.com/").isEmpty())
        assertTrue(registry.scriptFilesFor("https://qq.com/").isEmpty())
        assertTrue(registry.scriptFilesFor("about:blank").isEmpty())
        assertTrue(registry.scriptFilesFor(null).isEmpty())
    }

    @Test
    fun matchingAdapters_acceptsUrlWithoutScheme() {
        val registry = SiteAdapterRegistry.default()

        assertEquals(
            listOf("scripts/youtube.js"),
            registry.scriptFilesFor("m.youtube.com/watch?v=1")
        )
    }
}
