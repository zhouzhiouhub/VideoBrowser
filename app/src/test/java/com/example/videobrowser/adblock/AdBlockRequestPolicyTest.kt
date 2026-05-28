package com.example.videobrowser.adblock

import com.example.videobrowser.rules.RuleEngine
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdBlockRequestPolicyTest {
    private val ruleEngine = RuleEngine(BuiltInAdBlockRules.requestRules())

    @Test
    fun shouldBlock_allowsMainFrameEvenWhenUrlMatchesBlacklist() {
        assertFalse(
            AdBlockRequestPolicy.shouldBlock(
                enabled = true,
                url = "https://ads.example.com/pagead/index.html",
                host = "ads.example.com",
                scheme = "https",
                isForMainFrame = true,
                ruleEngine = ruleEngine
            )
        )
    }

    @Test
    fun shouldBlock_blocksMatchingSubresourceRequests() {
        assertTrue(
            AdBlockRequestPolicy.shouldBlock(
                enabled = true,
                url = "https://ads.example.com/pagead/banner.js",
                host = "ads.example.com",
                scheme = "https",
                isForMainFrame = false,
                ruleEngine = ruleEngine
            )
        )
    }

    @Test
    fun shouldBlock_allowsRequestsWhenAdBlockDisabled() {
        assertFalse(
            AdBlockRequestPolicy.shouldBlock(
                enabled = false,
                url = "https://stats.g.doubleclick.net/pagead/script.js",
                host = "stats.g.doubleclick.net",
                scheme = "https",
                isForMainFrame = false,
                ruleEngine = ruleEngine
            )
        )
    }

    @Test
    fun shouldBlock_allowsNonHttpRequests() {
        assertFalse(
            AdBlockRequestPolicy.shouldBlock(
                enabled = true,
                url = "about:blank",
                host = null,
                scheme = "about",
                isForMainFrame = false,
                ruleEngine = ruleEngine
            )
        )
    }
}
