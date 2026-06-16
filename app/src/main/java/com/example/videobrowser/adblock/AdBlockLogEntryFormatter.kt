package com.example.videobrowser.adblock

/**
 * 初学者阅读提示：
 * 这个文件属于“广告请求拦截模块”。
 * 文件名 AdBlockLogEntryFormatter 可以拆开理解为“Ad Block Log Entry Formatter”，表示它只负责页面净化链路中的一个小职责。
 * 主要职责：把 WebView 的网络请求交给规则系统判断，并在需要拦截时返回安全的本地响应。
 * 阅读顺序：先看数据类/策略类表达什么规则，再看控制器如何把规则接到 WebView 请求或页面脚本上。
 */
import java.util.Locale

object AdBlockLogEntryFormatter {
    /**
     * 函数 `summary`：封装 `summary` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param entry 参数类型为 `AdBlockLogEntry`，表示函数执行 `entry` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun summary(entry: AdBlockLogEntry): String {
        val source = entry.ruleSource ?: entry.reason.name.lowercase(Locale.US)
        val rule = entry.ruleId ?: entry.rulePattern ?: entry.reason.name
        val parts = mutableListOf("$source  $rule")
        entry.overrideReason?.let { reason ->
            parts += "override=${reason.name.lowercase(Locale.US)}"
        }
        if (entry.ruleCandidates.isNotEmpty()) {
            parts += "candidates=${candidateSummary(entry.ruleCandidates)}"
        }
        return parts.joinToString(separator = "  ")
    }

    /**
     * 函数 `recoveryActionFor`：封装 `recovery Action For` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param entry 参数类型为 `AdBlockLogEntry`，表示函数执行 `entry` 相关逻辑时需要读取或处理的输入。
     * @param isUserWhitelisted 参数类型为 `(String) -> Boolean`，表示一组待处理数据，函数会遍历、过滤或转换这些内容。
     * @param isAdBlockDisabledForSite 参数类型为 `(String) -> Boolean`，表示函数执行 `isAdBlockDisabledForSite` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun recoveryActionFor(
        entry: AdBlockLogEntry,
        isUserWhitelisted: (String) -> Boolean,
        isAdBlockDisabledForSite: (String) -> Boolean
    ): AdBlockLogRecoveryAction? {
        val requestHost = entry.host?.takeIf { value -> value.isNotBlank() }
        if (
            entry.action == AdBlockLogAction.BLOCK &&
            requestHost != null &&
            !isUserWhitelisted(requestHost)
        ) {
            return AdBlockLogRecoveryAction(
                type = AdBlockLogRecoveryActionType.ADD_TO_USER_WHITELIST,
                host = requestHost
            )
        }

        val pageHost = entry.pageHost?.takeIf { value -> value.isNotBlank() }
        if (
            entry.action == AdBlockLogAction.ALLOW &&
            entry.reason == AdBlockDecisionReason.SITE_AD_BLOCK_DISABLED &&
            pageHost != null &&
            isAdBlockDisabledForSite(pageHost)
        ) {
            return AdBlockLogRecoveryAction(
                type = AdBlockLogRecoveryActionType.RESTORE_SITE_AD_BLOCK,
                host = pageHost
            )
        }
        return null
    }

    /**
     * 函数 `candidateSummary`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param candidates 参数类型为 `List<AdBlockRuleCandidate>`，表示函数执行 `candidates` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun candidateSummary(candidates: List<AdBlockRuleCandidate>): String {
        return candidates.joinToString(separator = ", ") { candidate ->
            "${candidate.action.name}:${candidate.ruleId}@${candidate.ruleSource}"
        }
    }
}

data class AdBlockLogRecoveryAction(
    val type: AdBlockLogRecoveryActionType,
    val host: String
)

enum class AdBlockLogRecoveryActionType {
    ADD_TO_USER_WHITELIST,
    RESTORE_SITE_AD_BLOCK
}
