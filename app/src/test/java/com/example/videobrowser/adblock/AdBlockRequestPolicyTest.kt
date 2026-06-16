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

    /**
     * 测试函数 `shouldBlock_allowsMainFrameEvenWhenUrlMatchesBlacklist`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `should Block allows Main Frame Even When Url Matches Blacklist` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `shouldBlock_blocksMatchingSubresourceRequests`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `should Block blocks Matching Subresource Requests` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `shouldBlock_allowsRequestsWhenAdBlockDisabled`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `should Block allows Requests When Ad Block Disabled` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `shouldBlock_allowsRequestsWhenCurrentSiteAdBlockDisabled`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `should Block allows Requests When Current Site Ad Block Disabled` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `evaluate_allowsRequestsWhenUserWhitelistMatches`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `evaluate allows Requests When User Whitelist Matches` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `evaluate_keepsMatchedRuleForBlockedRequests`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `evaluate keeps Matched Rule For Blocked Requests` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `evaluate_allowsExplicitRuleAndKeepsBlockedCandidateForDiagnostics`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `evaluate allows Explicit Rule And Keeps Blocked Candidate For Diagnostics` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `evaluate_appliesThirdPartyScriptRulesWithoutBlockingFirstPartyScripts`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `evaluate applies Third Party Script Rules Without Blocking First Party Scripts` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `evaluate_userWhitelistOverridesSiteDisabledAndRuleMatch`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `evaluate user Whitelist Overrides Site Disabled And Rule Match` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `evaluate_currentSiteDisabledOverridesRuleMatchWhenUserWhitelistDoesNotApply`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `evaluate current Site Disabled Overrides Rule Match When User Whitelist Does Not Apply` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `shouldBlock_allowsNonHttpRequests`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `should Block allows Non Http Requests` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `scriptRequest`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `script Request` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param requestUrl 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param pageUrl 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun scriptRequest(requestUrl: String, pageUrl: String): RequestContext {
        return RequestContext(
            requestUrl = requestUrl,
            pageUrl = pageUrl,
            requestHeaders = mapOf("Sec-Fetch-Dest" to "script")
        )
    }
}
