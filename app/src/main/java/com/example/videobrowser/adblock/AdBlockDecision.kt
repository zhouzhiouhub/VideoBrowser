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
        /**
         * 函数 `allow`：封装 `allow` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param reason 参数类型为 `AdBlockDecisionReason`，表示函数执行 `reason` 相关逻辑时需要读取或处理的输入。
         * @param ruleCandidates 参数类型为 `List<RuleMatchResult>`，表示函数执行 `ruleCandidates` 相关逻辑时需要读取或处理的输入。
         * @param overrideReason 参数类型为 `AdBlockOverrideReason?`，表示函数执行 `overrideReason` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
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

        /**
         * 函数 `allowByRule`：封装 `allow By Rule` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param result 参数类型为 `RuleMatchResult`，表示函数执行 `result` 相关逻辑时需要读取或处理的输入。
         * @param ruleCandidates 参数类型为 `List<RuleMatchResult>`，表示函数执行 `ruleCandidates` 相关逻辑时需要读取或处理的输入。
         * @param overrideReason 参数类型为 `AdBlockOverrideReason?`，表示函数执行 `overrideReason` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
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

        /**
         * 函数 `blockByRule`：封装 `block By Rule` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param result 参数类型为 `RuleMatchResult`，表示函数执行 `result` 相关逻辑时需要读取或处理的输入。
         * @param reason 参数类型为 `AdBlockDecisionReason`，表示函数执行 `reason` 相关逻辑时需要读取或处理的输入。
         * @param ruleCandidates 参数类型为 `List<RuleMatchResult>`，表示函数执行 `ruleCandidates` 相关逻辑时需要读取或处理的输入。
         * @param overrideReason 参数类型为 `AdBlockOverrideReason?`，表示函数执行 `overrideReason` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
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
