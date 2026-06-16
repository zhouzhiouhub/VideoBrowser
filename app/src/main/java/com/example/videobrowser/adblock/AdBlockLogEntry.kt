package com.example.videobrowser.adblock

/**
 * 初学者阅读提示：
 * 这个文件属于“广告请求拦截模块”。
 * 文件名 AdBlockLogEntry 可以拆开理解为“Ad Block Log Entry”，表示它只负责页面净化链路中的一个小职责。
 * 主要职责：把 WebView 的网络请求交给规则系统判断，并在需要拦截时返回安全的本地响应。
 * 阅读顺序：先看数据类/策略类表达什么规则，再看控制器如何把规则接到 WebView 请求或页面脚本上。
 */
import com.example.videobrowser.rules.RuleAction

data class AdBlockLogEntry(
    val timestampMillis: Long,
    val action: AdBlockLogAction,
    val url: String,
    val host: String?,
    val pageHost: String? = null,
    val reason: AdBlockDecisionReason,
    val ruleId: String?,
    val ruleSource: String?,
    val rulePattern: String?,
    val overrideReason: AdBlockOverrideReason? = null,
    val ruleCandidates: List<AdBlockRuleCandidate> = emptyList()
)

enum class AdBlockLogAction {
    BLOCK,
    ALLOW
}

data class AdBlockRuleCandidate(
    val ruleId: String,
    val action: RuleAction,
    val ruleSource: String,
    val rulePattern: String
)
