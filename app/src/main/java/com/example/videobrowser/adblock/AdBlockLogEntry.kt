package com.example.videobrowser.adblock

import com.example.videobrowser.rules.RuleAction

data class AdBlockLogEntry(
    val timestampMillis: Long,
    val action: AdBlockLogAction,
    val url: String,
    val host: String?,
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
