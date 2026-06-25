package com.example.videobrowser.browser.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuickLinkLogoResolverTest {
    @Test
    fun logoUrlsFor_usesShortcutOriginAndCommonIconNames() {
        val urls = QuickLinkLogoResolver.logoUrlsFor(" https://Github.com/openai/codex?tab=readme ")

        assertEquals(
            listOf(
                "https://github.com/apple-touch-icon.png",
                "https://github.com/apple-touch-icon-precomposed.png",
                "https://github.com/favicon.png",
                "https://github.com/favicon.ico"
            ),
            urls
        )
    }

    @Test
    fun logoUrlsFromHtml_resolvesIconLinksAgainstPageUrl() {
        val urls = QuickLinkLogoResolver.logoUrlsFromHtml(
            pageUrl = "https://example.com/docs/page",
            html = """
                <html>
                    <head>
                        <link rel="stylesheet" href="/app.css">
                        <link rel="shortcut icon" href="/favicon-32.png">
                        <link href='icons/touch.png' rel='apple-touch-icon'>
                        <link rel=icon href=https://cdn.example.com/icon.svg>
                    </head>
                </html>
            """.trimIndent()
        )

        assertEquals(
            listOf(
                "https://example.com/favicon-32.png",
                "https://example.com/docs/icons/touch.png",
                "https://cdn.example.com/icon.svg"
            ),
            urls
        )
    }

    @Test
    fun fallbackBadgeText_usesNameBeforeGenericPlus() {
        assertEquals("Gi", QuickLinkLogoResolver.fallbackBadgeText(" Github "))
        assertEquals("+", QuickLinkLogoResolver.fallbackBadgeText(" "))
    }

    @Test
    fun logoUrlsFor_rejectsNonWebUrls() {
        assertTrue(QuickLinkLogoResolver.logoUrlsFor("javascript:alert(1)").isEmpty())
        assertEquals(null, QuickLinkLogoResolver.cacheKey("file:///tmp/index.html"))
    }
}
