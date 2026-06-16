package com.example.videobrowser.adblock

/**
 * 测试阅读提示：
 * 这个测试文件验证“Built In Ad Block Rules Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
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
