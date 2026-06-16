package com.example.videobrowser.adblock

/**
 * 测试阅读提示：
 * 这个测试文件验证“Ad Block Log Entry Formatter Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import com.example.videobrowser.rules.RuleAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AdBlockLogEntryFormatterTest {
    /**
     * 测试函数 `summary_includesSourceOverrideReasonAndCandidateRules`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `summary includes Source Override Reason And Candidate Rules` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun summary_includesSourceOverrideReasonAndCandidateRules() {
        val entry = AdBlockLogEntry(
            timestampMillis = 1L,
            action = AdBlockLogAction.ALLOW,
            url = "https://stats.g.doubleclick.net/pagead/allowed.js",
            host = "stats.g.doubleclick.net",
            pageHost = "video.example.com",
            reason = AdBlockDecisionReason.RULE_ALLOWED,
            ruleId = "test:allow",
            ruleSource = "user-rule",
            rulePattern = "/pagead/allowed.js",
            overrideReason = AdBlockOverrideReason.EXPLICIT_ALLOW_RULE,
            ruleCandidates = listOf(
                AdBlockRuleCandidate(
                    ruleId = "test:block",
                    action = RuleAction.BLOCK,
                    ruleSource = "test-source",
                    rulePattern = "doubleclick.net"
                ),
                AdBlockRuleCandidate(
                    ruleId = "test:allow",
                    action = RuleAction.ALLOW,
                    ruleSource = "user-rule",
                    rulePattern = "/pagead/allowed.js"
                )
            )
        )

        assertEquals(
            "user-rule  test:allow  override=explicit_allow_rule  " +
                "candidates=BLOCK:test:block@test-source, ALLOW:test:allow@user-rule",
            AdBlockLogEntryFormatter.summary(entry)
        )
    }

    /**
     * 测试函数 `recoveryActionFor_blockedRequestAddsRequestHostToWhitelist`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `recovery Action For blocked Request Adds Request Host To Whitelist` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun recoveryActionFor_blockedRequestAddsRequestHostToWhitelist() {
        val entry = blockedEntry(host = "ads.example.com")

        val action = AdBlockLogEntryFormatter.recoveryActionFor(
            entry = entry,
            isUserWhitelisted = { false },
            isAdBlockDisabledForSite = { false }
        )

        assertEquals(
            AdBlockLogRecoveryAction(
                type = AdBlockLogRecoveryActionType.ADD_TO_USER_WHITELIST,
                host = "ads.example.com"
            ),
            action
        )
    }

    /**
     * 测试函数 `recoveryActionFor_siteDisabledAllowRestoresPageHostAdBlock`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `recovery Action For site Disabled Allow Restores Page Host Ad Block` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun recoveryActionFor_siteDisabledAllowRestoresPageHostAdBlock() {
        val entry = AdBlockLogEntry(
            timestampMillis = 1L,
            action = AdBlockLogAction.ALLOW,
            url = "https://stats.g.doubleclick.net/pagead/script.js",
            host = "stats.g.doubleclick.net",
            pageHost = "video.example.com",
            reason = AdBlockDecisionReason.SITE_AD_BLOCK_DISABLED,
            ruleId = "test:block",
            ruleSource = "test-source",
            rulePattern = "doubleclick.net",
            overrideReason = AdBlockOverrideReason.SITE_AD_BLOCK_DISABLED
        )

        val action = AdBlockLogEntryFormatter.recoveryActionFor(
            entry = entry,
            isUserWhitelisted = { false },
            isAdBlockDisabledForSite = { host -> host == "video.example.com" }
        )

        assertEquals(
            AdBlockLogRecoveryAction(
                type = AdBlockLogRecoveryActionType.RESTORE_SITE_AD_BLOCK,
                host = "video.example.com"
            ),
            action
        )
    }

    /**
     * 测试函数 `recoveryActionFor_skipsAlreadyRecoveredEntries`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `recovery Action For skips Already Recovered Entries` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun recoveryActionFor_skipsAlreadyRecoveredEntries() {
        assertNull(
            AdBlockLogEntryFormatter.recoveryActionFor(
                entry = blockedEntry(host = "ads.example.com"),
                isUserWhitelisted = { true },
                isAdBlockDisabledForSite = { false }
            )
        )
    }

    /**
     * 测试函数 `blockedEntry`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `blocked Entry` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun blockedEntry(host: String): AdBlockLogEntry {
        return AdBlockLogEntry(
            timestampMillis = 1L,
            action = AdBlockLogAction.BLOCK,
            url = "https://$host/pagead/script.js",
            host = host,
            reason = AdBlockDecisionReason.RULE_BLOCKED,
            ruleId = "test:block",
            ruleSource = "test-source",
            rulePattern = "/pagead/"
        )
    }
}
