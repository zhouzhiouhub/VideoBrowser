package com.example.videobrowser.adblock

import com.example.videobrowser.rules.RuleMatchResult
import com.example.videobrowser.rules.Rule

data class AdBlockDecision(
    val shouldBlock: Boolean,
    val reason: AdBlockDecisionReason,
    val ruleMatchResult: RuleMatchResult = RuleMatchResult.NoMatch,
    val ruleCandidates: List<RuleMatchResult> = if (ruleMatchResult.matched) {
        listOf(ruleMatchResult)
    } else {
        emptyList()
    },
    val overrideReason: AdBlockOverrideReason? = null
) {
    val candidateRules: List<Rule>
        get() = ruleCandidates.mapNotNull { result -> result.rule }

    val shouldLog: Boolean
        get() = reason == AdBlockDecisionReason.RULE_BLOCKED ||
            reason == AdBlockDecisionReason.FORCE_RULE_BLOCKED ||
            reason == AdBlockDecisionReason.RULE_ALLOWED ||
            reason == AdBlockDecisionReason.USER_WHITELISTED ||
            reason == AdBlockDecisionReason.SITE_AD_BLOCK_DISABLED

    companion object {
        fun allow(
            reason: AdBlockDecisionReason,
            ruleCandidates: List<RuleMatchResult> = emptyList(),
            overrideReason: AdBlockOverrideReason? = null
        ): AdBlockDecision {
            return AdBlockDecision(
                shouldBlock = false,
                reason = reason,
                ruleMatchResult = ruleCandidates.firstOrNull() ?: RuleMatchResult.NoMatch,
                ruleCandidates = ruleCandidates,
                overrideReason = overrideReason
            )
        }

        fun allowByRule(
            result: RuleMatchResult,
            ruleCandidates: List<RuleMatchResult> = listOf(result),
            overrideReason: AdBlockOverrideReason? = null
        ): AdBlockDecision {
            return AdBlockDecision(
                shouldBlock = false,
                reason = AdBlockDecisionReason.RULE_ALLOWED,
                ruleMatchResult = result,
                ruleCandidates = ruleCandidates,
                overrideReason = overrideReason
            )
        }

        fun blockByRule(
            result: RuleMatchResult,
            reason: AdBlockDecisionReason = AdBlockDecisionReason.RULE_BLOCKED,
            ruleCandidates: List<RuleMatchResult> = listOf(result),
            overrideReason: AdBlockOverrideReason? = null
        ): AdBlockDecision {
            return AdBlockDecision(
                shouldBlock = true,
                reason = reason,
                ruleMatchResult = result,
                ruleCandidates = ruleCandidates,
                overrideReason = overrideReason
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
    FORCE_RULE_BLOCKED,
    RULE_BLOCKED,
    NO_MATCH
}

enum class AdBlockOverrideReason {
    USER_WHITELIST,
    SITE_AD_BLOCK_DISABLED,
    EXPLICIT_ALLOW_RULE,
    FORCE_BLOCK_RULE
}
