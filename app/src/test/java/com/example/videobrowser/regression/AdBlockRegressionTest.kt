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
    /**
     * 测试函数 `requestDecisionRegression_keepsWhitelistSiteDisableAllowAndNoopBoundaries`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `request Decision Regression keeps Whitelist Site Disable Allow And Noop Boundaries` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `evaluate`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `evaluate` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param ruleEngine 参数类型为 `RuleEngine`，表示函数执行 `ruleEngine` 相关逻辑时需要读取或处理的输入。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param host 参数类型为 `String`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @param isForMainFrame 参数类型为 `Boolean`，表示函数执行 `isForMainFrame` 相关逻辑时需要读取或处理的输入。
     * @param siteAdBlockDisabled 参数类型为 `Boolean`，表示函数执行 `siteAdBlockDisabled` 相关逻辑时需要读取或处理的输入。
     * @param userWhitelisted 参数类型为 `Boolean`，表示一组待处理数据，函数会遍历、过滤或转换这些内容。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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
