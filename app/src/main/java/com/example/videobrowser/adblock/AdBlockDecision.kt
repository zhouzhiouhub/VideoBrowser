package com.example.videobrowser.adblock

/**
 * 初学者阅读提示：
 * 这个文件属于“广告请求拦截模块”。
 * 文件名 AdBlockDecision 可以拆开理解为“Ad Block Decision”，表示它只负责页面净化链路中的一个小职责。
 * 主要职责：把 WebView 的网络请求交给规则系统判断，并在需要拦截时返回安全的本地响应。
 * 阅读顺序：先看数据类/策略类表达什么规则，再看控制器如何把规则接到 WebView 请求或页面脚本上。
 */
import com.example.videobrowser.rules.RuleMatchResult
import com.example.videobrowser.rules.Rule

data class AdBlockDecision(
    val shouldBlock: Boolean,
    val reason: AdBlockDecisionReason,
    val ruleMatchResult: RuleMatchResult = RuleMatchResult.NoMatch,
    val ruleCandidates: List<RuleMatchResult> = if (ruleMatchResult.matched) {
        listOf(ruleMatchResult)
    } else {
        emptyList()
    },
    val overrideReason: AdBlockOverrideReason? = null
) {
    val candidateRules: List<Rule>
        get() = ruleCandidates.mapNotNull { result -> result.rule }

    val shouldLog: Boolean
        get() = reason == AdBlockDecisionReason.RULE_BLOCKED ||
            reason == AdBlockDecisionReason.FORCE_RULE_BLOCKED ||
            reason == AdBlockDecisionReason.RULE_ALLOWED ||
            reason == AdBlockDecisionReason.USER_WHITELISTED ||
            reason == AdBlockDecisionReason.SITE_AD_BLOCK_DISABLED

    companion object {
        fun allow(
            reason: AdBlockDecisionReason,
            ruleCandidates: List<RuleMatchResult> = emptyList(),
            overrideReason: AdBlockOverrideReason? = null
        ): AdBlockDecision {
            return AdBlockDecision(
                shouldBlock = false,
                reason = reason,
                ruleMatchResult = ruleCandidates.firstOrNull() ?: RuleMatchResult.NoMatch,
                ruleCandidates = ruleCandidates,
                overrideReason = overrideReason
            )
        }

        fun allowByRule(
            result: RuleMatchResult,
            ruleCandidates: List<RuleMatchResult> = listOf(result),
            overrideReason: AdBlockOverrideReason? = null
        ): AdBlockDecision {
            return AdBlockDecision(
                shouldBlock = false,
                reason = AdBlockDecisionReason.RULE_ALLOWED,
                ruleMatchResult = result,
                ruleCandidates = ruleCandidates,
                overrideReason = overrideReason
            )
        }

        fun blockByRule(
            result: RuleMatchResult,
            reason: AdBlockDecisionReason = AdBlockDecisionReason.RULE_BLOCKED,
            ruleCandidates: List<RuleMatchResult> = listOf(result),
            overrideReason: AdBlockOverrideReason? = null
        ): AdBlockDecision {
            return AdBlockDecision(
                shouldBlock = true,
                reason = reason,
                ruleMatchResult = result,
                ruleCandidates = ruleCandidates,
                overrideReason = overrideReason
            )
        }
    }
}

enum class AdBlockDecisionReason {
    DISABLED,
    MAIN_FRAME,
    NON_HTTP_SCHEME,
    USER_WHITELISTED,
    SITE_AD_BLOCK_DISABLED,
    RULE_ALLOWED,
    FORCE_RULE_BLOCKED,
    RULE_BLOCKED,
    NO_MATCH
}

enum class AdBlockOverrideReason {
    USER_WHITELIST,
    SITE_AD_BLOCK_DISABLED,
    EXPLICIT_ALLOW_RULE,
    FORCE_BLOCK_RULE
}
