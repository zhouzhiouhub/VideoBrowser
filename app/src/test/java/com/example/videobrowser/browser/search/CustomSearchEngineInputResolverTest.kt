package com.example.videobrowser.browser.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CustomSearchEngineInputResolverTest {
    @Test
    fun resolve_knownDisplayHomesUseBuiltInSearchTemplates() {
        assertEquals(
            "https://quark.sm.cn/s?q={keyword}",
            CustomSearchEngineInputResolver.resolve("https://quark.sm.cn")?.searchTemplate
        )
        assertEquals(
            "https://so.douyin.com/s?keyword={keyword}",
            CustomSearchEngineInputResolver.resolve("https://so.douyin.com/")?.searchTemplate
        )
    }

    @Test
    fun resolve_advancedTemplatesNormalizePlaceholders() {
        val config = CustomSearchEngineInputResolver.resolve(
            "https://example.com/search?q=%s"
        )

        assertEquals("https://example.com", config?.displayUrl)
        assertEquals("https://example.com/search?q={keyword}", config?.searchTemplate)
        assertEquals("q", config?.queryParam)
        assertEquals(listOf("example.com"), config?.domains)
        assertEquals(listOf("/search"), config?.resultPathRules)
    }

    @Test
    fun resolve_unknownHomeUrlsReturnNullForAdvancedMode() {
        assertNull(CustomSearchEngineInputResolver.resolve("https://example.com"))
        assertNull(CustomSearchEngineInputResolver.resolve("ftp://example.com/search?q=%s"))
    }
}
