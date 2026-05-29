package com.example.videobrowser.adblock

import com.example.videobrowser.rules.RuleMatchResult

data class AdBlockDecision(
    val shouldBlock: Boolean,
    val reason: AdBlockDecisionReason,
    val ruleMatchResult: RuleMatchResult = RuleMatchResult.NoMatch
) {
    val shouldLog: Boolean
        get() = reason == AdBlockDecisionReason.RULE_BLOCKED ||
            reason == AdBlockDecisionReason.RULE_ALLOWED ||
            reason == AdBlockDecisionReason.USER_WHITELISTED

    companion object {
        fun allow(reason: AdBlockDecisionReason): AdBlockDecision {
            return AdBlockDecision(
                shouldBlock = false,
                reason = reason
            )
        }

        fun allowByRule(result: RuleMatchResult): AdBlockDecision {
            return AdBlockDecision(
                shouldBlock = false,
                reason = AdBlockDecisionReason.RULE_ALLOWED,
                ruleMatchResult = result
            )
        }

        fun blockByRule(result: RuleMatchResult): AdBlockDecision {
            return AdBlockDecision(
                shouldBlock = true,
                reason = AdBlockDecisionReason.RULE_BLOCKED,
                ruleMatchResult = result
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
    RULE_BLOCKED,
    NO_MATCH
}
