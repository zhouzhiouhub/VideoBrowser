package com.example.videobrowser.adblock

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
