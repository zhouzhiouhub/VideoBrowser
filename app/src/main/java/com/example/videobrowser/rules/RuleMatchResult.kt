package com.example.videobrowser.rules

/**
 * 初学者阅读提示：
 * 这个文件属于“规则引擎模块”。
 * 文件名 RuleMatchResult 可以拆开理解为“Rule Match Result”，表示它只负责页面净化链路中的一个小职责。
 * 主要职责：读取、解析、索引和匹配广告拦截规则、元素隐藏规则、参数清理规则和安全脚本规则。
 * 阅读顺序：先看数据类/策略类表达什么规则，再看控制器如何把规则接到 WebView 请求或页面脚本上。
 */
data class RuleMatchResult(
    val matched: Boolean,
    val action: RuleAction = RuleAction.NONE,
    val rule: Rule? = null
) {
    init {
        if (!matched) {
            require(action == RuleAction.NONE) { "Unmatched result must use NONE action." }
            require(rule == null) { "Unmatched result must not carry a rule." }
        } else {
            require(action != RuleAction.NONE) { "Matched result must use ALLOW or BLOCK." }
            require(rule != null) { "Matched result must carry a rule." }
        }
    }

    val shouldBlock: Boolean
        get() = matched && action == RuleAction.BLOCK

    val shouldAllow: Boolean
        get() = matched && action == RuleAction.ALLOW

    companion object {
        val NoMatch = RuleMatchResult(matched = false)

        fun block(rule: Rule): RuleMatchResult {
            return RuleMatchResult(
                matched = true,
                action = RuleAction.BLOCK,
                rule = rule
            )
        }

        fun allow(rule: Rule): RuleMatchResult {
            return RuleMatchResult(
                matched = true,
                action = RuleAction.ALLOW,
                rule = rule
            )
        }
    }
}

data class RequestRuleMatchSummary(
    val allowMatch: RuleMatchResult = RuleMatchResult.NoMatch,
    val forceBlockMatch: RuleMatchResult = RuleMatchResult.NoMatch,
    val blockMatch: RuleMatchResult = RuleMatchResult.NoMatch,
    val ruleCandidates: List<RuleMatchResult> = listOf(
        allowMatch,
        forceBlockMatch,
        blockMatch
    ).filter { result -> result.matched }
) {
    init {
        require(!allowMatch.matched || allowMatch.shouldAllow) {
            "Allow match must use ALLOW action."
        }
        require(!forceBlockMatch.matched || forceBlockMatch.shouldBlock) {
            "Force block match must use BLOCK action."
        }
        require(!blockMatch.matched || blockMatch.shouldBlock) {
            "Block match must use BLOCK action."
        }
    }

    companion object {
        val NoMatch = RequestRuleMatchSummary()
    }
}
