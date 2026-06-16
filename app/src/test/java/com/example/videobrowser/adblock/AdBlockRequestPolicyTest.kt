package com.example.videobrowser.adblock

/**
 * 测试阅读提示：
 * 这个测试文件验证“Ad Block Request Policy Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import com.example.videobrowser.browser.RequestContext
import com.example.videobrowser.rules.Rule
import com.example.videobrowser.rules.RuleEngine
import org.junit.Assert.assertEquals
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
    fun shouldBlock_allowsRequestsWhenCurrentSiteAdBlockDisabled() {
        assertFalse(
            AdBlockRequestPolicy.shouldBlock(
                enabled = true,
                siteAdBlockDisabled = true,
                url = "https://stats.g.doubleclick.net/pagead/script.js",
                host = "stats.g.doubleclick.net",
                scheme = "https",
                isForMainFrame = false,
                ruleEngine = ruleEngine
            )
        )
    }

    @Test
    fun evaluate_allowsRequestsWhenUserWhitelistMatches() {
        val decision = AdBlockRequestPolicy.evaluate(
            enabled = true,
            userWhitelisted = true,
            url = "https://stats.g.doubleclick.net/pagead/script.js",
            host = "stats.g.doubleclick.net",
            scheme = "https",
            isForMainFrame = false,
            ruleEngine = ruleEngine
        )

        assertFalse(decision.shouldBlock)
        assertEquals(AdBlockDecisionReason.USER_WHITELISTED, decision.reason)
    }

    @Test
    fun evaluate_keepsMatchedRuleForBlockedRequests() {
        val decision = AdBlockRequestPolicy.evaluate(
            enabled = true,
            url = "https://stats.g.doubleclick.net/pagead/script.js",
            host = "stats.g.doubleclick.net",
            scheme = "https",
            isForMainFrame = false,
            ruleEngine = ruleEngine
        )

        assertTrue(decision.shouldBlock)
        assertEquals(AdBlockDecisionReason.RULE_BLOCKED, decision.reason)
        assertTrue(decision.ruleMatchResult.matched)
    }

    @Test
    fun evaluate_allowsExplicitRuleAndKeepsBlockedCandidateForDiagnostics() {
        val blockRule = Rule.blockUrlContains("/pagead/", id = "test:block-pagead")
        val allowRule = Rule.allowUrlContains("/pagead/allowed.js", id = "test:allow-pagead")
        val decision = AdBlockRequestPolicy.evaluate(
            enabled = true,
            context = RequestContext(
                requestUrl = "https://stats.g.doubleclick.net/pagead/allowed.js",
                pageUrl = "https://video.example.com/watch"
            ),
            ruleEngine = RuleEngine(listOf(blockRule, allowRule))
        )

        assertFalse(decision.shouldBlock)
        assertEquals(AdBlockDecisionReason.RULE_ALLOWED, decision.reason)
        assertEquals(AdBlockOverrideReason.EXPLICIT_ALLOW_RULE, decision.overrideReason)
        assertEquals(allowRule, decision.ruleMatchResult.rule)
        assertEquals(listOf(blockRule, allowRule), decision.candidateRules)
    }

    @Test
    fun evaluate_appliesThirdPartyScriptRulesWithoutBlockingFirstPartyScripts() {
        val rule = requireNotNull(
            Rule.fromRequestRuleText("||ads.video.example.com^\$third-party,script")
        )
        val engine = RuleEngine(listOf(rule))
        val thirdPartyDecision = AdBlockRequestPolicy.evaluate(
            enabled = true,
            context = scriptRequest(
                requestUrl = "https://ads.video.example.com/banner.js",
                pageUrl = "https://other.example.org/watch"
            ),
            ruleEngine = engine
        )
        val firstPartyDecision = AdBlockRequestPolicy.evaluate(
            enabled = true,
            context = scriptRequest(
                requestUrl = "https://ads.video.example.com/banner.js",
                pageUrl = "https://www.video.example.com/watch"
            ),
            ruleEngine = engine
        )

        assertTrue(thirdPartyDecision.shouldBlock)
        assertEquals(AdBlockDecisionReason.RULE_BLOCKED, thirdPartyDecision.reason)
        assertFalse(firstPartyDecision.shouldBlock)
        assertEquals(AdBlockDecisionReason.NO_MATCH, firstPartyDecision.reason)
    }

    @Test
    fun evaluate_userWhitelistOverridesSiteDisabledAndRuleMatch() {
        val blockRule = Rule.blockDomainContains("doubleclick.net", id = "test:block-doubleclick")
        val decision = AdBlockRequestPolicy.evaluate(
            enabled = true,
            siteAdBlockDisabled = true,
            userWhitelisted = true,
            context = RequestContext(
                requestUrl = "https://stats.g.doubleclick.net/pagead/script.js",
                pageUrl = "https://video.example.com/watch"
            ),
            ruleEngine = RuleEngine(listOf(blockRule))
        )

        assertFalse(decision.shouldBlock)
        assertEquals(AdBlockDecisionReason.USER_WHITELISTED, decision.reason)
        assertEquals(AdBlockOverrideReason.USER_WHITELIST, decision.overrideReason)
        assertEquals(listOf(blockRule), decision.candidateRules)
    }

    @Test
    fun evaluate_currentSiteDisabledOverridesRuleMatchWhenUserWhitelistDoesNotApply() {
        val blockRule = Rule.blockDomainContains("doubleclick.net", id = "test:block-doubleclick")
        val decision = AdBlockRequestPolicy.evaluate(
            enabled = true,
            siteAdBlockDisabled = true,
            userWhitelisted = false,
            context = RequestContext(
                requestUrl = "https://stats.g.doubleclick.net/pagead/script.js",
                pageUrl = "https://video.example.com/watch"
            ),
            ruleEngine = RuleEngine(listOf(blockRule))
        )

        assertFalse(decision.shouldBlock)
        assertEquals(AdBlockDecisionReason.SITE_AD_BLOCK_DISABLED, decision.reason)
        assertEquals(AdBlockOverrideReason.SITE_AD_BLOCK_DISABLED, decision.overrideReason)
        assertEquals(listOf(blockRule), decision.candidateRules)
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

    private fun scriptRequest(requestUrl: String, pageUrl: String): RequestContext {
        return RequestContext(
            requestUrl = requestUrl,
            pageUrl = pageUrl,
            requestHeaders = mapOf("Sec-Fetch-Dest" to "script")
        )
    }
}
