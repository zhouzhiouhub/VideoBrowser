package com.example.videobrowser.rules

data class CompiledRuleSet(
    val requestCapabilities: List<RuleCapability.Request> = emptyList(),
    val cssHideCapabilities: List<RuleCapability.CssHide> = emptyList(),
    val cssUnhideCapabilities: List<RuleCapability.CssUnhide> = emptyList(),
    val domRemoveCapabilities: List<RuleCapability.DomRemove> = emptyList(),
    val safeHookCapabilities: List<RuleCapability.SafeHook> = emptyList(),
    val noopResponseCapabilities: List<RuleCapability.NoopResponse> = emptyList(),
    val removeParamCapabilities: List<RuleCapability.RemoveParam> = emptyList(),
    val skippedRules: List<SkippedRule> = emptyList(),
    val requestRuleIndex: RequestRuleIndex = RequestRuleIndex.from(requestCapabilities),
    val cssHideRuleIndex: ElementRuleIndex<RuleCapability.CssHide> =
        ElementRuleIndex.from(cssHideCapabilities),
    val cssUnhideRuleIndex: ElementRuleIndex<RuleCapability.CssUnhide> =
        ElementRuleIndex.from(cssUnhideCapabilities),
    val domRemoveRuleIndex: ElementRuleIndex<RuleCapability.DomRemove> =
        ElementRuleIndex.from(domRemoveCapabilities)
) {
    /**
     * 函数 `allCapabilities`：封装 `all Capabilities` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun allCapabilities(): List<RuleCapability> {
        return requestCapabilities +
            elementHideCapabilities() +
            domRemoveCapabilities +
            safeHookCapabilities +
            noopResponseCapabilities +
            removeParamCapabilities
    }

    /**
     * 函数 `elementHideCapabilities`：封装 `element Hide Capabilities` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun elementHideCapabilities(): List<RuleCapability.ElementHide> {
        return cssHideCapabilities + cssUnhideCapabilities
    }

    /**
     * 函数 `requestRules`：处理 `request Rules` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun requestRules(): List<Rule> {
        return requestCapabilities.map { capability -> capability.rule }
    }

    /**
     * 函数 `requestCandidatesFor`：处理 `request Candidates For` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param action 参数类型为 `RuleAction`，表示函数执行 `action` 相关逻辑时需要读取或处理的输入。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `cssHideCandidatesFor`：封装 `css Hide Candidates For` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param pageHost 参数类型为 `String?`，表示函数执行 `pageHost` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun cssHideCandidatesFor(pageHost: String?): List<RuleCapability.CssHide> {
        return cssHideRuleIndex.candidatesFor(pageHost)
    }

    /**
     * 函数 `cssUnhideCandidatesFor`：封装 `css Unhide Candidates For` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param pageHost 参数类型为 `String?`，表示函数执行 `pageHost` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun cssUnhideCandidatesFor(pageHost: String?): List<RuleCapability.CssUnhide> {
        return cssUnhideRuleIndex.candidatesFor(pageHost)
    }

    /**
     * 函数 `domRemoveCandidatesFor`：封装 `dom Remove Candidates For` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param pageHost 参数类型为 `String?`，表示函数执行 `pageHost` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun domRemoveCandidatesFor(pageHost: String?): List<RuleCapability.DomRemove> {
        return domRemoveRuleIndex.candidatesFor(pageHost)
    }

    /**
     * 函数 `elementRules`：封装 `element Rules` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun elementRules(): List<ElementRule> {
        return cssHideRules() + cssUnhideRules() + domRemoveRules()
    }

    /**
     * 函数 `cssHideRules`：封装 `css Hide Rules` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun cssHideRules(): List<ElementRule> {
        return cssHideCapabilities.map { capability -> capability.rule }
    }

    /**
     * 函数 `cssUnhideRules`：封装 `css Unhide Rules` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun cssUnhideRules(): List<ElementRule> {
        return cssUnhideCapabilities.map { capability -> capability.rule }
    }

    /**
     * 函数 `domRemoveRules`：封装 `dom Remove Rules` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun domRemoveRules(): List<ElementRule> {
        return domRemoveCapabilities.map { capability -> capability.rule }
    }

    /**
     * 函数 `removeParamRules`：封装 `remove Param Rules` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun removeParamRules(): List<RemoveParamRule> {
        return removeParamCapabilities.map { capability -> capability.rule }
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
        val arguments: List<String> = emptyList(),
        val domainScope: DomainScope = DomainScope.Empty
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

    data class RemoveParam(val rule: RemoveParamRule) : RuleCapability() {
        override val id: String = rule.id
        override val source: String = rule.source
        override val kind: RuleCapabilityKind = RuleCapabilityKind.REMOVE_PARAM
    }
}

enum class RuleCapabilityKind {
    REQUEST,
    ELEMENT_HIDE,
    DOM_REMOVE,
    SAFE_HOOK,
    NOOP_RESPONSE,
    REMOVE_PARAM
}

enum class ElementHideEffect {
    HIDE,
    UNHIDE
}
