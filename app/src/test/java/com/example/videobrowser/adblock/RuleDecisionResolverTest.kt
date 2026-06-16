package com.example.videobrowser.adblock

/**
 * 测试阅读提示：
 * 这个测试文件验证“Rule Decision Resolver Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
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

    /**
     * 测试函数 `resolve_userWhitelistOverridesRuleCandidatesAndKeepsOverrideContext`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `resolve user Whitelist Overrides Rule Candidates And Keeps Override Context` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `resolve_currentSiteDisabledOverridesRuleCandidatesAfterUserWhitelist`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `resolve current Site Disabled Overrides Rule Candidates After User Whitelist` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `resolve_explicitAllowOverridesForceAndNormalBlockCandidates`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `resolve explicit Allow Overrides Force And Normal Block Candidates` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `resolve_forceBlockOverridesNormalBlockCandidate`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `resolve force Block Overrides Normal Block Candidate` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `resolve_allowsWhenNoRuleMatches`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `resolve allows When No Rule Matches` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `subresourceContext`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `subresource Context` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun subresourceContext(url: String): RequestContext {
        return RequestContext(
            requestUrl = url,
            pageUrl = "https://video.test/watch",
            isForMainFrame = false
        )
    }
}
