package com.example.videobrowser.adblock

/**
 * 初学者阅读提示：
 * 这个文件属于“广告请求拦截模块”。
 * 文件名 RuleDecisionResolver 可以拆开理解为“Rule Decision Resolver”，表示它只负责页面净化链路中的一个小职责。
 * 主要职责：把 WebView 的网络请求交给规则系统判断，并在需要拦截时返回安全的本地响应。
 * 阅读顺序：先看数据类/策略类表达什么规则，再看控制器如何把规则接到 WebView 请求或页面脚本上。
 */
import com.example.videobrowser.browser.RequestContext
import com.example.videobrowser.rules.RequestRuleMatchSummary
import com.example.videobrowser.utils.WebSchemePolicy

/**
 * 集中处理请求拦截最终优先级，方便日志解释“命中了什么规则”和“为什么被覆盖”。
 */
class RuleDecisionResolver {
    /**
     * 函数 `resolve`：从现有状态、缓存或输入对象中取得目标数据，并把结果交给调用方继续处理。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param input 参数类型为 `Input`，表示函数执行 `input` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun resolve(input: Input): AdBlockDecision {
        val candidates = input.ruleSummary.ruleCandidates
        if (!input.enabled) {
            return AdBlockDecision.allow(AdBlockDecisionReason.DISABLED)
        }
        if (input.context.isForMainFrame) {
            return AdBlockDecision.allow(AdBlockDecisionReason.MAIN_FRAME)
        }
        if (!WebSchemePolicy.isHttpOrHttpsScheme(input.context.requestScheme)) {
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

    /**
     * 函数 `hasBlockCandidate`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun RequestRuleMatchSummary.hasBlockCandidate(): Boolean {
        return forceBlockMatch.matched || blockMatch.matched
    }

    data class Input(
        val enabled: Boolean,
        val userWhitelisted: Boolean,
        val siteAdBlockDisabled: Boolean,
        val context: RequestContext,
        val ruleSummary: RequestRuleMatchSummary
    )
}
