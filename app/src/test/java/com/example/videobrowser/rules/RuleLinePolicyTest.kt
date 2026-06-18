package com.example.videobrowser.rules

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleLinePolicyTest {
    @Test
    fun `ignores empty comment and hash only rule lines`() {
        assertTrue(RuleLinePolicy.shouldIgnore("   "))
        assertTrue(RuleLinePolicy.shouldIgnore("! comment"))
        assertTrue(RuleLinePolicy.shouldIgnore("# "))
        assertTrue(RuleLinePolicy.shouldIgnore("#"))
        assertFalse(RuleLinePolicy.shouldIgnore("example.com##.ad"))
    }

    @Test
    fun `accepts simple selectors and rejects unsafe selectors`() {
        assertTrue(RuleLinePolicy.isSafeSelector(".ad-banner"))
        assertFalse(RuleLinePolicy.isSafeSelector(""))
        assertFalse(RuleLinePolicy.isSafeSelector(".ad { display:none }"))
        assertFalse(RuleLinePolicy.isSafeSelector("a:has(.sponsored)"))
        assertFalse(RuleLinePolicy.isSafeSelector("a[href^='javascript:']"))
    }

    @Test
    fun `detects supported scriptlet rule markers`() {
        assertTrue(RuleLinePolicy.isScriptletRuleLine("example.com##+js(fetch-block-keyword, /ad/)"))
        assertTrue(RuleLinePolicy.isScriptletRuleLine("example.com#%#//scriptlet('fetch-block-keyword', '/ad/')"))
        assertFalse(RuleLinePolicy.isScriptletRuleLine("example.com##.ad"))
    }
}
