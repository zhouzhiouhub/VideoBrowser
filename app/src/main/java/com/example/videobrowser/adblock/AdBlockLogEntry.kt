package com.example.videobrowser.adblock

data class AdBlockLogEntry(
    val timestampMillis: Long,
    val action: AdBlockLogAction,
    val url: String,
    val host: String?,
    val reason: AdBlockDecisionReason,
    val ruleId: String?,
    val ruleSource: String?,
    val rulePattern: String?
)

enum class AdBlockLogAction {
    BLOCK,
    ALLOW
}
