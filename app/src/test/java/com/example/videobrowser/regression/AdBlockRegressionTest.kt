package com.example.videobrowser.regression

/**
 * 测试阅读提示：
 * 这个测试文件验证“Ad Block Regression Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import com.example.videobrowser.adblock.AdBlockDecisionReason
import com.example.videobrowser.adblock.AdBlockRequestPolicy
import com.example.videobrowser.adblock.SyntheticResponseRegistry
import com.example.videobrowser.rules.Rule
import com.example.videobrowser.rules.RuleEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdBlockRegressionTest {
    @Test
    fun requestDecisionRegression_keepsWhitelistSiteDisableAllowAndNoopBoundaries() {
        val blockRule = Rule.blockUrlContains("/pagead/")
        val allowRule = Rule.allowUrlContains("/pagead/allowed.js")
        val redirectRule = requireNotNull(
            Rule.fromRequestRuleText(
                text = "||ads.example.com^\$redirect=noopjs",
                id = "redirect:noop"
            )
        )
        val ruleEngine = RuleEngine(listOf(blockRule, allowRule, redirectRule))

        val allowedDecision = evaluate(
            ruleEngine = ruleEngine,
            url = "https://cdn.example.com/pagead/allowed.js",
            host = "cdn.example.com"
        )
        assertFalse(allowedDecision.shouldBlock)
        assertEquals(AdBlockDecisionReason.RULE_ALLOWED, allowedDecision.reason)

        val noopDecision = evaluate(
            ruleEngine = ruleEngine,
            url = "https://ads.example.com/script.js",
            host = "ads.example.com"
        )
        assertTrue(noopDecision.shouldBlock)
        assertEquals("noopjs", noopDecision.ruleMatchResult.rule?.redirectResourceName)
        val noopSpec = SyntheticResponseRegistry()
            .get(noopDecision.ruleMatchResult.rule?.redirectResourceName)
        assertEquals("application/javascript", noopSpec?.mimeType)
        assertEquals("/* noop */\n", noopSpec?.body?.toString(Charsets.UTF_8))

        val mainFrameDecision = evaluate(
            ruleEngine = ruleEngine,
            url = "https://ads.example.com/",
            host = "ads.example.com",
            isForMainFrame = true
        )
        assertFalse(mainFrameDecision.shouldBlock)
        assertEquals(AdBlockDecisionReason.MAIN_FRAME, mainFrameDecision.reason)

        val siteDisabledDecision = evaluate(
            ruleEngine = ruleEngine,
            url = "https://ads.example.com/script.js",
            host = "ads.example.com",
            siteAdBlockDisabled = true
        )
        assertFalse(siteDisabledDecision.shouldBlock)
        assertEquals(AdBlockDecisionReason.SITE_AD_BLOCK_DISABLED, siteDisabledDecision.reason)
        assertTrue(siteDisabledDecision.candidateRules.contains(redirectRule))

        val whitelistedDecision = evaluate(
            ruleEngine = ruleEngine,
            url = "https://ads.example.com/script.js",
            host = "ads.example.com",
            userWhitelisted = true
        )
        assertFalse(whitelistedDecision.shouldBlock)
        assertEquals(AdBlockDecisionReason.USER_WHITELISTED, whitelistedDecision.reason)
        assertTrue(whitelistedDecision.candidateRules.contains(redirectRule))
    }

    private fun evaluate(
        ruleEngine: RuleEngine,
        url: String,
        host: String,
        isForMainFrame: Boolean = false,
        siteAdBlockDisabled: Boolean = false,
        userWhitelisted: Boolean = false
    ) = AdBlockRequestPolicy.evaluate(
        enabled = true,
        siteAdBlockDisabled = siteAdBlockDisabled,
        userWhitelisted = userWhitelisted,
        url = url,
        host = host,
        scheme = "https",
        pageHost = "video.example.com",
        isForMainFrame = isForMainFrame,
        ruleEngine = ruleEngine
    )
}
