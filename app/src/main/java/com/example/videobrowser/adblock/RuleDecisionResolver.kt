package com.example.videobrowser.adblock

import com.example.videobrowser.browser.RequestContext
import com.example.videobrowser.rules.RequestRuleMatchSummary

/**
 * 集中处理请求拦截最终优先级，方便日志解释“命中了什么规则”和“为什么被覆盖”。
 */
class RuleDecisionResolver {
    fun resolve(input: Input): AdBlockDecision {
        val candidates = input.ruleSummary.ruleCandidates
        if (!input.enabled) {
            return AdBlockDecision.allow(AdBlockDecisionReason.DISABLED)
        }
        if (input.context.isForMainFrame) {
            return AdBlockDecision.allow(AdBlockDecisionReason.MAIN_FRAME)
        }
        if (!isHttpScheme(input.context.requestScheme)) {
            return AdBlockDecision.allow(AdBlockDecisionReason.NON_HTTP_SCHEME)
        }
        if (input.userWhitelisted) {
            return AdBlockDecision.allow(
                reason = AdBlockDecisionReason.USER_WHITELISTED,
                ruleCandidates = candidates,
                overrideReason = AdBlockOverrideReason.USER_WHITELIST
            )
        }
        if (input.siteAdBlockDisabled) {
            return AdBlockDecision.allow(
                reason = AdBlockDecisionReason.SITE_AD_BLOCK_DISABLED,
                ruleCandidates = candidates,
                overrideReason = AdBlockOverrideReason.SITE_AD_BLOCK_DISABLED
            )
        }

        if (input.ruleSummary.allowMatch.matched) {
            return AdBlockDecision.allowByRule(
                result = input.ruleSummary.allowMatch,
                ruleCandidates = candidates,
                overrideReason = AdBlockOverrideReason.EXPLICIT_ALLOW_RULE
                    .takeIf { input.ruleSummary.hasBlockCandidate() }
            )
        }
        if (input.ruleSummary.forceBlockMatch.matched) {
            return AdBlockDecision.blockByRule(
                result = input.ruleSummary.forceBlockMatch,
                reason = AdBlockDecisionReason.FORCE_RULE_BLOCKED,
                ruleCandidates = candidates,
                overrideReason = AdBlockOverrideReason.FORCE_BLOCK_RULE
                    .takeIf { input.ruleSummary.blockMatch.matched }
            )
        }
        if (input.ruleSummary.blockMatch.matched) {
            return AdBlockDecision.blockByRule(
                result = input.ruleSummary.blockMatch,
                ruleCandidates = candidates
            )
        }
        return AdBlockDecision.allow(AdBlockDecisionReason.NO_MATCH)
    }

    private fun RequestRuleMatchSummary.hasBlockCandidate(): Boolean {
        return forceBlockMatch.matched || blockMatch.matched
    }

    private fun isHttpScheme(scheme: String?): Boolean {
        return scheme.equals("http", ignoreCase = true) ||
            scheme.equals("https", ignoreCase = true)
    }

    data class Input(
        val enabled: Boolean,
        val userWhitelisted: Boolean,
        val siteAdBlockDisabled: Boolean,
        val context: RequestContext,
        val ruleSummary: RequestRuleMatchSummary
    )
}
