package com.example.videobrowser.adblock

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BuiltInAdBlockRulesTest {
    @Test
    fun matches_blocksKnownAdHosts() {
        assertTrue(
            BuiltInAdBlockRules.matches(
                url = "https://stats.g.doubleclick.net/pagead/viewthroughconversion.js",
                host = "stats.g.doubleclick.net"
            )
        )
        assertTrue(
            BuiltInAdBlockRules.matches(
                url = "https://cdn.adservice.google.com/script.js",
                host = "cdn.adservice.google.com"
            )
        )
    }

    @Test
    fun matches_blocksKnownAdUrlKeywords() {
        assertTrue(
            BuiltInAdBlockRules.matches(
                url = "https://example.com/static/pagead/banner.js",
                host = "example.com"
            )
        )
        assertTrue(
            BuiltInAdBlockRules.matches(
                url = "https://video.example.com/media/preroll/clip.m3u8",
                host = "video.example.com"
            )
        )
    }

    @Test
    fun matches_allowsNormalResources() {
        assertFalse(
            BuiltInAdBlockRules.matches(
                url = "https://example.com/assets/app.js",
                host = "example.com"
            )
        )
        assertFalse(
            BuiltInAdBlockRules.matches(
                url = "https://cdn.example.com/images/poster.jpg",
                host = "cdn.example.com"
            )
        )
    }

    @Test
    fun matches_doesNotTreatHostSubstringAsDomainMatch() {
        assertFalse(
            BuiltInAdBlockRules.matches(
                url = "https://notdoubleclick.net/assets/app.js",
                host = "notdoubleclick.net"
            )
        )
    }
}
