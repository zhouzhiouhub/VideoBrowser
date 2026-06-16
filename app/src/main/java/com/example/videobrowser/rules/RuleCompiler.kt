package com.example.videobrowser.rules

/**
 * 初学者阅读提示：
 * 这个文件属于“规则引擎模块”。
 * 文件名 RuleCompiler 可以拆开理解为“Rule Compiler”，表示它只负责页面净化链路中的一个小职责。
 * 主要职责：读取、解析、索引和匹配广告拦截规则、元素隐藏规则、参数清理规则和安全脚本规则。
 * 阅读顺序：先看数据类/策略类表达什么规则，再看控制器如何把规则接到 WebView 请求或页面脚本上。
 */
import java.util.Locale

/**
 * 把解析后的安全规则子集转换为项目内部能力模型。
 *
 * G2-01/G2-02 只做轻量编译和显式能力分类，不建立索引；后续 G2 任务会在这些能力列表之上增加
 * host、suffix 和关键词索引，避免匹配阶段继续扫描完整规则集。
 */
class RuleCompiler {
    /**
     * 函数 `compile`：封装 `compile` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param requestRules 参数类型为 `List<Rule>`，表示一次请求或响应，函数会检查它的内容并决定如何继续处理。
     * @param elementRules 参数类型为 `List<ElementRule>`，表示一组待处理数据，函数会遍历、过滤或转换这些内容。
     * @param scriptletRules 参数类型为 `List<ScriptletRule>`，表示一组待处理数据，函数会遍历、过滤或转换这些内容。
     * @param removeParamRules 参数类型为 `List<RemoveParamRule>`，表示一组待处理数据，函数会遍历、过滤或转换这些内容。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun compile(
        requestRules: List<Rule>,
        elementRules: List<ElementRule>,
        scriptletRules: List<ScriptletRule> = emptyList(),
        removeParamRules: List<RemoveParamRule> = emptyList()
    ): CompiledRuleSet {
        // 编译阶段不做真实匹配，只把不同类型的规则分门别类，方便 RuleEngine 快速查询。
        val skippedRules = mutableListOf<SkippedRule>()
        val requestCapabilities = requestRules.mapNotNull { rule ->
            compileRequestRule(rule, skippedRules)
        }
        val noopResponseCapabilities = requestCapabilities.mapNotNull { capability ->
            compileNoopResponseCapability(capability)
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
        val safeHookCapabilities = scriptletRules.mapNotNull { rule ->
            compileScriptletRule(rule, skippedRules)
        }
        val removeParamCapabilities = removeParamRules.map { rule ->
            RuleCapability.RemoveParam(rule)
        }

        return CompiledRuleSet(
            requestCapabilities = requestCapabilities,
            cssHideCapabilities = cssHideCapabilities,
            cssUnhideCapabilities = cssUnhideCapabilities,
            domRemoveCapabilities = domRemoveCapabilities,
            safeHookCapabilities = safeHookCapabilities,
            noopResponseCapabilities = noopResponseCapabilities,
            removeParamCapabilities = removeParamCapabilities,
            skippedRules = skippedRules
        )
    }

    /**
     * 函数 `compileRequestRule`：封装 `compile Request Rule` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param rule 参数类型为 `Rule`，表示函数执行 `rule` 相关逻辑时需要读取或处理的输入。
     * @param skippedRules 参数类型为 `MutableList<SkippedRule>`，表示一组待处理数据，函数会遍历、过滤或转换这些内容。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun compileRequestRule(
        rule: Rule,
        skippedRules: MutableList<SkippedRule>
    ): RuleCapability.Request? {
        // 如果规则类型需要正则但正则没有成功构建，就跳过并记录原因，避免运行时抛异常。
        if (rule.type == RuleType.URL_PATTERN && rule.normalizedPatternRegex == null) {
            skippedRules += skipped(rule, "unsupported url pattern")
            return null
        }
        return RuleCapability.Request(rule)
    }

    /**
     * 函数 `compileNoopResponseCapability`：封装 `compile Noop Response Capability` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param requestCapability 参数类型为 `RuleCapability.Request`，表示一次请求或响应，函数会检查它的内容并决定如何继续处理。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun compileNoopResponseCapability(
        requestCapability: RuleCapability.Request
    ): RuleCapability.NoopResponse? {
        val resourceName = requestCapability.rule.redirectResourceName ?: return null
        return RuleCapability.NoopResponse(
            id = requestCapability.id,
            source = requestCapability.source,
            resourceName = resourceName
        )
    }

    /**
     * 函数 `compileElementRule`：封装 `compile Element Rule` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param rule 参数类型为 `ElementRule`，表示函数执行 `rule` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun compileElementRule(rule: ElementRule): RuleCapability.PageMutation {
        return when (rule.type) {
            ElementRuleType.CSS_HIDE -> RuleCapability.CssHide(rule)
            ElementRuleType.CSS_UNHIDE -> RuleCapability.CssUnhide(rule)
            ElementRuleType.DOM_REMOVE -> RuleCapability.DomRemove(rule)
        }
    }

    /**
     * 函数 `compileScriptletRule`：封装 `compile Scriptlet Rule` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param rule 参数类型为 `ScriptletRule`，表示函数执行 `rule` 相关逻辑时需要读取或处理的输入。
     * @param skippedRules 参数类型为 `MutableList<SkippedRule>`，表示一组待处理数据，函数会遍历、过滤或转换这些内容。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun compileScriptletRule(
        rule: ScriptletRule,
        skippedRules: MutableList<SkippedRule>
    ): RuleCapability.SafeHook? {
        val normalizedName = rule.name.trim().lowercase(Locale.US)
        return when (ScriptletRegistry.validate(normalizedName, rule.arguments)) {
            ScriptletValidation.Valid -> RuleCapability.SafeHook(
                id = rule.id,
                source = rule.source,
                hookName = normalizedName,
                arguments = rule.arguments,
                domainScope = rule.domainScope
            )
            ScriptletValidation.Unsupported -> {
                skippedRules += skipped(rule, ScriptletRegistry.REASON_UNSUPPORTED_SCRIPTLET)
                null
            }
            ScriptletValidation.InvalidArguments -> {
                skippedRules += skipped(rule, ScriptletRegistry.REASON_INVALID_ARGUMENTS)
                null
            }
        }
    }

    /**
     * 函数 `skipped`：封装 `skipped` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param rule 参数类型为 `Rule`，表示函数执行 `rule` 相关逻辑时需要读取或处理的输入。
     * @param reason 参数类型为 `String`，表示函数执行 `reason` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun skipped(rule: Rule, reason: String): SkippedRule {
        return SkippedRule(
            source = rule.source,
            lineNumber = 0,
            text = "${rule.id}:${rule.pattern}",
            reason = reason
        )
    }

    /**
     * 函数 `skipped`：封装 `skipped` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param rule 参数类型为 `ScriptletRule`，表示函数执行 `rule` 相关逻辑时需要读取或处理的输入。
     * @param reason 参数类型为 `String`，表示函数执行 `reason` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun skipped(rule: ScriptletRule, reason: String): SkippedRule {
        return SkippedRule(
            source = rule.source,
            lineNumber = 0,
            text = "${rule.id}:${rule.name}",
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
