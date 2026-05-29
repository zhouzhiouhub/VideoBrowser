package com.example.videobrowser.rules

/**
 * 把解析后的安全规则子集转换为项目内部能力模型。
 *
 * G2-01/G2-02 只做轻量编译和显式能力分类，不建立索引；后续 G2 任务会在这些能力列表之上增加
 * host、suffix 和关键词索引，避免匹配阶段继续扫描完整规则集。
 */
class RuleCompiler {
    fun compile(
        requestRules: List<Rule>,
        elementRules: List<ElementRule>
    ): CompiledRuleSet {
        val skippedRules = mutableListOf<SkippedRule>()
        val requestCapabilities = requestRules.mapNotNull { rule ->
            compileRequestRule(rule, skippedRules)
        }
        val cssHideCapabilities = mutableListOf<RuleCapability.CssHide>()
        val cssUnhideCapabilities = mutableListOf<RuleCapability.CssUnhide>()
        val domRemoveCapabilities = mutableListOf<RuleCapability.DomRemove>()

        elementRules.forEach { rule ->
            when (val capability = compileElementRule(rule)) {
                is RuleCapability.CssHide -> cssHideCapabilities += capability
                is RuleCapability.CssUnhide -> cssUnhideCapabilities += capability
                is RuleCapability.DomRemove -> domRemoveCapabilities += capability
            }
        }

        return CompiledRuleSet(
            requestCapabilities = requestCapabilities,
            cssHideCapabilities = cssHideCapabilities,
            cssUnhideCapabilities = cssUnhideCapabilities,
            domRemoveCapabilities = domRemoveCapabilities,
            skippedRules = skippedRules
        )
    }

    private fun compileRequestRule(
        rule: Rule,
        skippedRules: MutableList<SkippedRule>
    ): RuleCapability.Request? {
        if (rule.type == RuleType.URL_PATTERN && rule.normalizedPatternRegex == null) {
            skippedRules += skipped(rule, "unsupported url pattern")
            return null
        }
        return RuleCapability.Request(rule)
    }

    private fun compileElementRule(rule: ElementRule): RuleCapability.PageMutation {
        return when (rule.type) {
            ElementRuleType.CSS_HIDE -> RuleCapability.CssHide(rule)
            ElementRuleType.CSS_UNHIDE -> RuleCapability.CssUnhide(rule)
            ElementRuleType.DOM_REMOVE -> RuleCapability.DomRemove(rule)
        }
    }

    private fun skipped(rule: Rule, reason: String): SkippedRule {
        return SkippedRule(
            source = rule.source,
            lineNumber = 0,
            text = "${rule.id}:${rule.pattern}",
            reason = reason
        )
    }
}

data class CompiledRuleSet(
    val requestCapabilities: List<RuleCapability.Request> = emptyList(),
    val cssHideCapabilities: List<RuleCapability.CssHide> = emptyList(),
    val cssUnhideCapabilities: List<RuleCapability.CssUnhide> = emptyList(),
    val domRemoveCapabilities: List<RuleCapability.DomRemove> = emptyList(),
    val safeHookCapabilities: List<RuleCapability.SafeHook> = emptyList(),
    val noopResponseCapabilities: List<RuleCapability.NoopResponse> = emptyList(),
    val skippedRules: List<SkippedRule> = emptyList(),
    val requestRuleIndex: RequestRuleIndex = RequestRuleIndex.from(requestCapabilities),
    val cssHideRuleIndex: ElementRuleIndex<RuleCapability.CssHide> =
        ElementRuleIndex.from(cssHideCapabilities),
    val cssUnhideRuleIndex: ElementRuleIndex<RuleCapability.CssUnhide> =
        ElementRuleIndex.from(cssUnhideCapabilities),
    val domRemoveRuleIndex: ElementRuleIndex<RuleCapability.DomRemove> =
        ElementRuleIndex.from(domRemoveCapabilities)
) {
    fun allCapabilities(): List<RuleCapability> {
        return requestCapabilities +
            elementHideCapabilities() +
            domRemoveCapabilities +
            safeHookCapabilities +
            noopResponseCapabilities
    }

    fun elementHideCapabilities(): List<RuleCapability.ElementHide> {
        return cssHideCapabilities + cssUnhideCapabilities
    }

    fun requestRules(): List<Rule> {
        return requestCapabilities.map { capability -> capability.rule }
    }

    fun requestCandidatesFor(
        action: RuleAction,
        host: String?,
        url: String? = null
    ): List<RuleCapability.Request> {
        return requestRuleIndex.candidatesFor(
            action = action,
            host = host,
            url = url
        )
    }

    fun cssHideCandidatesFor(pageHost: String?): List<RuleCapability.CssHide> {
        return cssHideRuleIndex.candidatesFor(pageHost)
    }

    fun cssUnhideCandidatesFor(pageHost: String?): List<RuleCapability.CssUnhide> {
        return cssUnhideRuleIndex.candidatesFor(pageHost)
    }

    fun domRemoveCandidatesFor(pageHost: String?): List<RuleCapability.DomRemove> {
        return domRemoveRuleIndex.candidatesFor(pageHost)
    }

    fun elementRules(): List<ElementRule> {
        return cssHideRules() + cssUnhideRules() + domRemoveRules()
    }

    fun cssHideRules(): List<ElementRule> {
        return cssHideCapabilities.map { capability -> capability.rule }
    }

    fun cssUnhideRules(): List<ElementRule> {
        return cssUnhideCapabilities.map { capability -> capability.rule }
    }

    fun domRemoveRules(): List<ElementRule> {
        return domRemoveCapabilities.map { capability -> capability.rule }
    }
}

sealed class RuleCapability {
    abstract val id: String
    abstract val source: String
    abstract val kind: RuleCapabilityKind

    data class Request(val rule: Rule) : RuleCapability() {
        override val id: String = rule.id
        override val source: String = rule.source
        override val kind: RuleCapabilityKind = RuleCapabilityKind.REQUEST
        val action: RuleAction = rule.action
    }

    sealed class PageMutation : RuleCapability() {
        abstract val rule: ElementRule
        override val id: String
            get() = rule.id
        override val source: String
            get() = rule.source
    }

    sealed class ElementHide : PageMutation() {
        abstract val effect: ElementHideEffect
        override val kind: RuleCapabilityKind = RuleCapabilityKind.ELEMENT_HIDE
    }

    data class CssHide(override val rule: ElementRule) : ElementHide() {
        override val effect: ElementHideEffect = ElementHideEffect.HIDE
    }

    data class CssUnhide(override val rule: ElementRule) : ElementHide() {
        override val effect: ElementHideEffect = ElementHideEffect.UNHIDE
    }

    data class DomRemove(override val rule: ElementRule) : PageMutation() {
        override val kind: RuleCapabilityKind = RuleCapabilityKind.DOM_REMOVE
    }

    /**
     * 预留给后续 scriptlet / 安全 Hook 映射。当前阶段不从规则文件生成该能力。
     */
    data class SafeHook(
        override val id: String,
        override val source: String,
        val hookName: String,
        val arguments: List<String> = emptyList()
    ) : RuleCapability() {
        override val kind: RuleCapabilityKind = RuleCapabilityKind.SAFE_HOOK
    }

    /**
     * 预留给后续内置 noop / redirect 响应。当前阶段不接收远程或任意响应内容。
     */
    data class NoopResponse(
        override val id: String,
        override val source: String,
        val resourceName: String
    ) : RuleCapability() {
        override val kind: RuleCapabilityKind = RuleCapabilityKind.NOOP_RESPONSE
    }
}

enum class RuleCapabilityKind {
    REQUEST,
    ELEMENT_HIDE,
    DOM_REMOVE,
    SAFE_HOOK,
    NOOP_RESPONSE
}

enum class ElementHideEffect {
    HIDE,
    UNHIDE
}
