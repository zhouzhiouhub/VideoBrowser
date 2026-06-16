package com.example.videobrowser.adblock

/**
 * 测试阅读提示：
 * 这个测试文件验证“Ad Block Logger Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import com.example.videobrowser.rules.Rule
import com.example.videobrowser.rules.RuleAction
import com.example.videobrowser.rules.RuleMatchResult
import org.junit.Assert.assertEquals
import org.junit.Test

class AdBlockLoggerTest {
    /**
     * 测试函数 `log_keepsNewestEntriesWithinLimit`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `log keeps Newest Entries Within Limit` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun log_keepsNewestEntriesWithinLimit() {
        var now = 1000L
        val logger = AdBlockLogger(
            maxEntries = 2,
            clock = { now++ }
        )
        val decision = AdBlockDecision.blockByRule(
            RuleMatchResult.block(
                Rule.blockUrlContains(
                    pattern = "/pagead/",
                    id = "test:block",
                    source = "test-source"
                )
            )
        )

        logger.log(AdBlockLogAction.BLOCK, "https://a.test/pagead/1.js", "a.test", decision)
        logger.log(AdBlockLogAction.BLOCK, "https://b.test/pagead/2.js", "b.test", decision)
        logger.log(AdBlockLogAction.BLOCK, "https://c.test/pagead/3.js", "c.test", decision)

        val entries = logger.entries()
        assertEquals(2, entries.size)
        assertEquals("c.test", entries[0].host)
        assertEquals("b.test", entries[1].host)
        assertEquals("test:block", entries[0].ruleId)
        assertEquals("test-source", entries[0].ruleSource)
    }

    /**
     * 测试函数 `clear_removesEntries`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `clear removes Entries` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun clear_removesEntries() {
        val logger = AdBlockLogger()
        logger.log(
            AdBlockLogEntry(
                timestampMillis = 1L,
                action = AdBlockLogAction.ALLOW,
                url = "https://example.com/",
                host = "example.com",
                reason = AdBlockDecisionReason.USER_WHITELISTED,
                ruleId = null,
                ruleSource = null,
                rulePattern = null
            )
        )

        logger.clear()

        assertEquals(emptyList<AdBlockLogEntry>(), logger.entries())
    }

    /**
     * 测试函数 `log_recordsCandidateRulesAndOverrideReason`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `log records Candidate Rules And Override Reason` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun log_recordsCandidateRulesAndOverrideReason() {
        val logger = AdBlockLogger(clock = { 2000L })
        val blockResult = RuleMatchResult.block(
            Rule.blockDomainContains(
                domain = "doubleclick.net",
                id = "test:block",
                source = "test-source"
            )
        )
        val allowResult = RuleMatchResult.allow(
            Rule.allowUrlContains(
                pattern = "/pagead/allowed.js",
                id = "test:allow",
                source = "user-rule"
            )
        )
        val decision = AdBlockDecision.allowByRule(
            result = allowResult,
            ruleCandidates = listOf(blockResult, allowResult),
            overrideReason = AdBlockOverrideReason.EXPLICIT_ALLOW_RULE
        )

        logger.log(
            AdBlockLogAction.ALLOW,
            "https://stats.g.doubleclick.net/pagead/allowed.js",
            "stats.g.doubleclick.net",
            decision,
            pageHost = "video.test"
        )

        val entry = logger.entries().single()
        assertEquals("video.test", entry.pageHost)
        assertEquals(AdBlockDecisionReason.RULE_ALLOWED, entry.reason)
        assertEquals(AdBlockOverrideReason.EXPLICIT_ALLOW_RULE, entry.overrideReason)
        assertEquals(2, entry.ruleCandidates.size)
        assertEquals("test:block", entry.ruleCandidates[0].ruleId)
        assertEquals(RuleAction.BLOCK, entry.ruleCandidates[0].action)
        assertEquals("test-source", entry.ruleCandidates[0].ruleSource)
        assertEquals("test:allow", entry.ruleCandidates[1].ruleId)
        assertEquals(RuleAction.ALLOW, entry.ruleCandidates[1].action)
        assertEquals("user-rule", entry.ruleCandidates[1].ruleSource)
    }
}
