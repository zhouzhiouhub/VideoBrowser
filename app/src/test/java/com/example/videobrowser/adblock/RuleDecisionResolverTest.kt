package com.example.videobrowser.adblock

import com.example.videobrowser.browser.RequestContext
import com.example.videobrowser.rules.RequestRuleMatchSummary
import com.example.videobrowser.rules.Rule
import com.example.videobrowser.rules.RuleMatchResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleDecisionResolverTest {
    private val resolver = RuleDecisionResolver()

    @Test
    fun resolve_userWhitelistOverridesRuleCandidatesAndKeepsOverrideContext() {
        val blockResult = RuleMatchResult.block(
            Rule.blockUrlContains(
                pattern = "/pagead/",
                id = "test:block"
            )
        )

        val decision = resolver.resolve(
            RuleDecisionResolver.Input(
                enabled = true,
                siteAdBlockDisabled = false,
                userWhitelisted = true,
                context = subresourceContext("https://ads.test/pagead/script.js"),
                ruleSummary = RequestRuleMatchSummary(blockMatch = blockResult)
            )
        )

        assertFalse(decision.shouldBlock)
        assertEquals(AdBlockDecisionReason.USER_WHITELISTED, decision.reason)
        assertEquals(AdBlockOverrideReason.USER_WHITELIST, decision.overrideReason)
        assertEquals(listOf(blockResult), decision.ruleCandidates)
        assertSame(blockResult.rule, decision.ruleMatchResult.rule)
    }

    @Test
    fun resolve_currentSiteDisabledOverridesRuleCandidatesAfterUserWhitelist() {
        val blockResult = RuleMatchResult.block(
            Rule.blockDomainContains(
                domain = "doubleclick.net",
                id = "test:block"
            )
        )

        val decision = resolver.resolve(
            RuleDecisionResolver.Input(
                enabled = true,
                siteAdBlockDisabled = true,
                userWhitelisted = false,
                context = subresourceContext("https://stats.g.doubleclick.net/pagead/script.js"),
                ruleSummary = RequestRuleMatchSummary(blockMatch = blockResult)
            )
        )

        assertFalse(decision.shouldBlock)
        assertEquals(AdBlockDecisionReason.SITE_AD_BLOCK_DISABLED, decision.reason)
        assertEquals(AdBlockOverrideReason.SITE_AD_BLOCK_DISABLED, decision.overrideReason)
        assertEquals(listOf(blockResult), decision.ruleCandidates)
    }

    @Test
    fun resolve_explicitAllowOverridesForceAndNormalBlockCandidates() {
        val allowResult = RuleMatchResult.allow(
            Rule.allowUrlContains(
                pattern = "/pagead/allowed.js",
                id = "test:allow"
            )
        )
        val forceBlockResult = RuleMatchResult.block(
            Rule.blockUrlContains(
                pattern = "/pagead/allowed.js",
                id = "test:force"
            )
        )
        val blockResult = RuleMatchResult.block(
            Rule.blockDomainContains(
                domain = "doubleclick.net",
                id = "test:block"
            )
        )

        val decision = resolver.resolve(
            RuleDecisionResolver.Input(
                enabled = true,
                siteAdBlockDisabled = false,
                userWhitelisted = false,
                context = subresourceContext("https://stats.g.doubleclick.net/pagead/allowed.js"),
                ruleSummary = RequestRuleMatchSummary(
                    allowMatch = allowResult,
                    forceBlockMatch = forceBlockResult,
                    blockMatch = blockResult
                )
            )
        )

        assertFalse(decision.shouldBlock)
        assertEquals(AdBlockDecisionReason.RULE_ALLOWED, decision.reason)
        assertEquals(AdBlockOverrideReason.EXPLICIT_ALLOW_RULE, decision.overrideReason)
        assertEquals(listOf(allowResult, forceBlockResult, blockResult), decision.ruleCandidates)
        assertSame(allowResult.rule, decision.ruleMatchResult.rule)
    }

    @Test
    fun resolve_forceBlockOverridesNormalBlockCandidate() {
        val forceBlockResult = RuleMatchResult.block(
            Rule.blockUrlContains(
                pattern = "/pagead/",
                id = "test:force"
            )
        )
        val blockResult = RuleMatchResult.block(
            Rule.blockDomainContains(
                domain = "doubleclick.net",
                id = "test:block"
            )
        )

        val decision = resolver.resolve(
            RuleDecisionResolver.Input(
                enabled = true,
                siteAdBlockDisabled = false,
                userWhitelisted = false,
                context = subresourceContext("https://stats.g.doubleclick.net/pagead/script.js"),
                ruleSummary = RequestRuleMatchSummary(
                    forceBlockMatch = forceBlockResult,
                    blockMatch = blockResult
                )
            )
        )

        assertTrue(decision.shouldBlock)
        assertEquals(AdBlockDecisionReason.FORCE_RULE_BLOCKED, decision.reason)
        assertEquals(AdBlockOverrideReason.FORCE_BLOCK_RULE, decision.overrideReason)
        assertEquals(listOf(forceBlockResult, blockResult), decision.ruleCandidates)
        assertSame(forceBlockResult.rule, decision.ruleMatchResult.rule)
    }

    @Test
    fun resolve_allowsWhenNoRuleMatches() {
        val decision = resolver.resolve(
            RuleDecisionResolver.Input(
                enabled = true,
                siteAdBlockDisabled = false,
                userWhitelisted = false,
                context = subresourceContext("https://example.test/app.js"),
                ruleSummary = RequestRuleMatchSummary.NoMatch
            )
        )

        assertFalse(decision.shouldBlock)
        assertEquals(AdBlockDecisionReason.NO_MATCH, decision.reason)
        assertEquals(emptyList<RuleMatchResult>(), decision.ruleCandidates)
    }

    private fun subresourceContext(url: String): RequestContext {
        return RequestContext(
            requestUrl = url,
            pageUrl = "https://video.test/watch",
            isForMainFrame = false
        )
    }
}
