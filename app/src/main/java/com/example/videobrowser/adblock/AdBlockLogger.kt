package com.example.videobrowser.adblock

/**
 * 初学者阅读提示：
 * 这个文件属于“广告请求拦截模块”。
 * 文件名 AdBlockLogger 可以拆开理解为“Ad Block Logger”，表示它只负责页面净化链路中的一个小职责。
 * 主要职责：把 WebView 的网络请求交给规则系统判断，并在需要拦截时返回安全的本地响应。
 * 阅读顺序：先看数据类/策略类表达什么规则，再看控制器如何把规则接到 WebView 请求或页面脚本上。
 */
class AdBlockLogger(
    private val maxEntries: Int = DEFAULT_MAX_ENTRIES,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    private val entries = ArrayDeque<AdBlockLogEntry>()

    fun log(
        action: AdBlockLogAction,
        url: String,
        host: String?,
        decision: AdBlockDecision,
        pageHost: String? = null
    ) {
        val rule = decision.ruleMatchResult.rule
        log(
            AdBlockLogEntry(
                timestampMillis = clock(),
                action = action,
                url = url,
                host = host,
                pageHost = pageHost,
                reason = decision.reason,
                ruleId = rule?.id,
                ruleSource = rule?.source,
                rulePattern = rule?.pattern,
                overrideReason = decision.overrideReason,
                ruleCandidates = decision.ruleCandidates.mapNotNull { result ->
                    val candidateRule = result.rule ?: return@mapNotNull null
                    AdBlockRuleCandidate(
                        ruleId = candidateRule.id,
                        action = result.action,
                        ruleSource = candidateRule.source,
                        rulePattern = candidateRule.pattern
                    )
                }
            )
        )
    }

    fun log(entry: AdBlockLogEntry) {
        entries.addFirst(entry)
        while (entries.size > maxEntries) {
            entries.removeLast()
        }
    }

    fun entries(): List<AdBlockLogEntry> {
        return entries.toList()
    }

    fun clear() {
        entries.clear()
    }

    companion object {
        private const val DEFAULT_MAX_ENTRIES = 80
    }
}
