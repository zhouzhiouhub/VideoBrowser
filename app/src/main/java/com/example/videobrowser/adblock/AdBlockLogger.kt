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

    /**
     * 函数 `log`：封装 `log` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param action 参数类型为 `AdBlockLogAction`，表示函数执行 `action` 相关逻辑时需要读取或处理的输入。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @param decision 参数类型为 `AdBlockDecision`，表示函数执行 `decision` 相关逻辑时需要读取或处理的输入。
     * @param pageHost 参数类型为 `String?`，表示函数执行 `pageHost` 相关逻辑时需要读取或处理的输入。
     */
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

    /**
     * 函数 `log`：封装 `log` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param entry 参数类型为 `AdBlockLogEntry`，表示函数执行 `entry` 相关逻辑时需要读取或处理的输入。
     */
    fun log(entry: AdBlockLogEntry) {
        entries.addFirst(entry)
        while (entries.size > maxEntries) {
            entries.removeLast()
        }
    }

    /**
     * 函数 `entries`：封装 `entries` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun entries(): List<AdBlockLogEntry> {
        return entries.toList()
    }

    /**
     * 函数 `clear`：封装 `clear` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun clear() {
        entries.clear()
    }

    companion object {
        private const val DEFAULT_MAX_ENTRIES = 80
    }
}
