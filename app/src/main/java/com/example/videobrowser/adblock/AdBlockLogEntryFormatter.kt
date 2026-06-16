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
