package com.example.videobrowser.rules

/**
 * 测试阅读提示：
 * 这个测试文件验证“Rule Compiler Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleCompilerTest {
    /**
     * 测试函数 `compile_categorizesSupportedRuleCapabilities`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `compile categorizes Supported Rule Capabilities` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun compile_categorizesSupportedRuleCapabilities() {
        val requestRule = Rule.blockUrlContains("/pagead/")
        val allowRule = Rule.allowDomainContains("example.com")
        val cssRule = ElementRule(
            id = "css:hide",
            selector = ".ad-banner",
            type = ElementRuleType.CSS_HIDE
        )
        val cssExceptionRule = ElementRule(
            id = "css:unhide",
            selector = ".ad-banner",
            type = ElementRuleType.CSS_UNHIDE,
            domains = setOf("example.com")
        )
        val domRule = ElementRule(
            id = "dom:remove",
            selector = ".popup-ad",
            type = ElementRuleType.DOM_REMOVE
        )

        val result = RuleCompiler().compile(
            requestRules = listOf(requestRule, allowRule),
            elementRules = listOf(cssRule, cssExceptionRule, domRule)
        )

        assertEquals(listOf(requestRule, allowRule), result.requestRules())
        assertEquals(listOf(cssRule), result.cssHideRules())
        assertEquals(listOf(cssExceptionRule), result.cssUnhideRules())
        assertEquals(listOf(domRule), result.domRemoveRules())
        assertEquals(listOf(cssRule, cssExceptionRule, domRule), result.elementRules())
        assertEquals(
            listOf(RuleCapabilityKind.ELEMENT_HIDE, RuleCapabilityKind.ELEMENT_HIDE),
            result.elementHideCapabilities().map { capability -> capability.kind }
        )
        assertEquals(
            listOf(ElementHideEffect.HIDE, ElementHideEffect.UNHIDE),
            result.elementHideCapabilities().map { capability -> capability.effect }
        )
        assertTrue(result.safeHookCapabilities.isEmpty())
        assertTrue(result.noopResponseCapabilities.isEmpty())
        assertTrue(result.skippedRules.isEmpty())
    }

    /**
     * 测试函数 `compile_preservesOriginalRuleObjectsForDiagnostics`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `compile preserves Original Rule Objects For Diagnostics` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun compile_preservesOriginalRuleObjectsForDiagnostics() {
        val requestRule = Rule.blockDomainContains("doubleclick.net", source = "test-source")
        val elementRule = ElementRule(
            id = "css:site",
            selector = "#player-ads",
            type = ElementRuleType.CSS_HIDE,
            source = "test-source"
        )

        val result = RuleCompiler().compile(
            requestRules = listOf(requestRule),
            elementRules = listOf(elementRule)
        )

        assertSame(requestRule, result.requestCapabilities.single().rule)
        assertSame(elementRule, result.cssHideCapabilities.single().rule)
        assertEquals("test-source", result.requestCapabilities.single().source)
        assertEquals("test-source", result.cssHideCapabilities.single().source)
    }

    /**
     * 测试函数 `compile_createsNoopResponseCapabilityForRedirectRules`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `compile creates Noop Response Capability For Redirect Rules` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun compile_createsNoopResponseCapabilityForRedirectRules() {
        val redirectRule = requireNotNull(
            Rule.fromRequestRuleText(
                text = "||ads.example.com^\$redirect=noopjs",
                id = "test:redirect",
                source = "test-source"
            )
        )

        val result = RuleCompiler().compile(
            requestRules = listOf(redirectRule),
            elementRules = emptyList()
        )

        assertEquals(listOf(redirectRule), result.requestRules())
        assertEquals(1, result.noopResponseCapabilities.size)
        assertEquals("test:redirect", result.noopResponseCapabilities.single().id)
        assertEquals("test-source", result.noopResponseCapabilities.single().source)
        assertEquals("noopjs", result.noopResponseCapabilities.single().resourceName)
        assertTrue(result.skippedRules.isEmpty())
    }

    /**
     * 测试函数 `compile_createsSafeHookCapabilitiesForScriptletRules`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `compile creates Safe Hook Capabilities For Scriptlet Rules` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun compile_createsSafeHookCapabilitiesForScriptletRules() {
        val scriptletRule = ScriptletRule(
            id = "scriptlet:fetch",
            name = "fetch-block-keyword",
            arguments = listOf("/pagead/"),
            source = "test-source",
            domainScope = DomainScope(includedDomains = setOf("example.com"))
        )

        val result = RuleCompiler().compile(
            requestRules = emptyList(),
            elementRules = emptyList(),
            scriptletRules = listOf(scriptletRule)
        )

        assertEquals(1, result.safeHookCapabilities.size)
        val capability = result.safeHookCapabilities.single()
        assertEquals("scriptlet:fetch", capability.id)
        assertEquals("test-source", capability.source)
        assertEquals("fetch-block-keyword", capability.hookName)
        assertEquals(listOf("/pagead/"), capability.arguments)
        assertTrue(capability.domainScope.matches("www.example.com"))
        assertTrue(result.skippedRules.isEmpty())
    }

    /**
     * 测试函数 `compile_skipsInvalidScriptletRules`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `compile skips Invalid Scriptlet Rules` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun compile_skipsInvalidScriptletRules() {
        val scriptletRule = ScriptletRule(
            id = "scriptlet:unknown",
            name = "unknown-scriptlet",
            arguments = listOf("alert(1)"),
            source = "test-source"
        )

        val result = RuleCompiler().compile(
            requestRules = emptyList(),
            elementRules = emptyList(),
            scriptletRules = listOf(scriptletRule)
        )

        assertTrue(result.safeHookCapabilities.isEmpty())
        assertEquals(1, result.skippedRules.size)
        assertEquals(
            ScriptletRegistry.REASON_UNSUPPORTED_SCRIPTLET,
            result.skippedRules.single().reason
        )
    }

    /**
     * 测试函数 `capabilityKinds_distinguishRequestPageHookAndNoopCapabilities`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `capability Kinds distinguish Request Page Hook And Noop Capabilities` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun capabilityKinds_distinguishRequestPageHookAndNoopCapabilities() {
        val requestCapability = RuleCapability.Request(Rule.blockUrlContains("/ad/"))
        val hideCapability = RuleCapability.CssHide(
            ElementRule(
                id = "css:hide",
                selector = ".ad",
                type = ElementRuleType.CSS_HIDE
            )
        )
        val domCapability = RuleCapability.DomRemove(
            ElementRule(
                id = "dom:remove",
                selector = ".popup",
                type = ElementRuleType.DOM_REMOVE
            )
        )
        val hookCapability = RuleCapability.SafeHook(
            id = "hook:fetch",
            source = "test",
            hookName = "fetch-block-keyword",
            arguments = listOf("/ad/")
        )
        val noopCapability = RuleCapability.NoopResponse(
            id = "noop:script",
            source = "test",
            resourceName = "noopjs"
        )

        val result = CompiledRuleSet(
            requestCapabilities = listOf(requestCapability),
            cssHideCapabilities = listOf(hideCapability),
            domRemoveCapabilities = listOf(domCapability),
            safeHookCapabilities = listOf(hookCapability),
            noopResponseCapabilities = listOf(noopCapability)
        )

        assertEquals(
            listOf(
                RuleCapabilityKind.REQUEST,
                RuleCapabilityKind.ELEMENT_HIDE,
                RuleCapabilityKind.DOM_REMOVE,
                RuleCapabilityKind.SAFE_HOOK,
                RuleCapabilityKind.NOOP_RESPONSE
            ),
            result.allCapabilities().map { capability -> capability.kind }
        )
        assertEquals(RuleAction.BLOCK, requestCapability.action)
        assertEquals(ElementHideEffect.HIDE, hideCapability.effect)
    }

    /**
     * 测试函数 `requestRuleIndex_returnsDomainSuffixCandidatesAndFallbackInOriginalOrder`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `request Rule Index returns Domain Suffix Candidates And Fallback In Original Order` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun requestRuleIndex_returnsDomainSuffixCandidatesAndFallbackInOriginalOrder() {
        val firstFallbackRule = Rule.blockUrlContains("/pagead/")
        val matchingDomainRule = Rule.blockDomainContains("doubleclick.net")
        val unrelatedDomainRule = Rule.blockDomainContains("tracker.example")
        val lastFallbackRule = requireNotNull(
            Rule.fromRequestRuleText("||doubleclick.net^*/ad_status^")
        )

        val result = RuleCompiler().compile(
            requestRules = listOf(
                firstFallbackRule,
                matchingDomainRule,
                unrelatedDomainRule,
                lastFallbackRule
            ),
            elementRules = emptyList()
        )

        assertEquals(
            listOf(firstFallbackRule, matchingDomainRule, lastFallbackRule),
            result.requestCandidatesFor(
                action = RuleAction.BLOCK,
                host = "securepubads.g.doubleclick.net"
            ).map { capability -> capability.rule }
        )
    }

    /**
     * 测试函数 `requestRuleIndex_separatesAllowAndBlockDomainCandidates`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `request Rule Index separates Allow And Block Domain Candidates` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun requestRuleIndex_separatesAllowAndBlockDomainCandidates() {
        val allowRule = Rule.allowDomainContains("doubleclick.net")
        val blockRule = Rule.blockDomainContains("doubleclick.net")

        val result = RuleCompiler().compile(
            requestRules = listOf(blockRule, allowRule),
            elementRules = emptyList()
        )

        assertEquals(
            listOf(allowRule),
            result.requestCandidatesFor(
                action = RuleAction.ALLOW,
                host = "stats.g.doubleclick.net"
            ).map { capability -> capability.rule }
        )
        assertEquals(
            listOf(blockRule),
            result.requestCandidatesFor(
                action = RuleAction.BLOCK,
                host = "stats.g.doubleclick.net"
            ).map { capability -> capability.rule }
        )
    }

    /**
     * 测试函数 `requestRuleIndex_returnsUrlContainsKeywordCandidatesAndFallbackInOriginalOrder`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `request Rule Index returns Url Contains Keyword Candidates And Fallback In Original Order` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun requestRuleIndex_returnsUrlContainsKeywordCandidatesAndFallbackInOriginalOrder() {
        val shortFallbackRule = Rule.blockUrlContains("ad")
        val matchingUrlRule = Rule.blockUrlContains("/pagead/")
        val unrelatedUrlRule = Rule.blockUrlContains("/analytics/")
        val patternFallbackRule = requireNotNull(
            Rule.fromRequestRuleText("||cdn.example^*/ad_status^")
        )

        val result = RuleCompiler().compile(
            requestRules = listOf(
                shortFallbackRule,
                matchingUrlRule,
                unrelatedUrlRule,
                patternFallbackRule
            ),
            elementRules = emptyList()
        )

        assertEquals(
            listOf(shortFallbackRule, matchingUrlRule, patternFallbackRule),
            result.requestCandidatesFor(
                action = RuleAction.BLOCK,
                host = "assets.example.com",
                url = "https://assets.example.com/static/pagead/banner.js"
            ).map { capability -> capability.rule }
        )
    }

    /**
     * 测试函数 `requestRuleIndex_findsUrlKeywordInsideLongerUrlToken`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `request Rule Index finds Url Keyword Inside Longer Url Token` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun requestRuleIndex_findsUrlKeywordInsideLongerUrlToken() {
        val rule = Rule.blockUrlContains("foo")

        val result = RuleCompiler().compile(
            requestRules = listOf(rule),
            elementRules = emptyList()
        )

        assertEquals(
            listOf(rule),
            result.requestCandidatesFor(
                action = RuleAction.BLOCK,
                host = "static.example.com",
                url = "https://static.example.com/assets/xfoo.js"
            ).map { capability -> capability.rule }
        )
    }

    /**
     * 测试函数 `requestRuleIndex_keepsFallbackCandidatesWhenHostOrUrlIsMissing`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `request Rule Index keeps Fallback Candidates When Host Or Url Is Missing` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun requestRuleIndex_keepsFallbackCandidatesWhenHostOrUrlIsMissing() {
        val shortFallbackRule = Rule.blockUrlContains("ad")
        val matchingUrlRule = Rule.blockUrlContains("/pagead/")
        val unrelatedUrlRule = Rule.blockUrlContains("/analytics/")
        val matchingDomainRule = Rule.blockDomainContains("doubleclick.net")
        val patternFallbackRule = requireNotNull(
            Rule.fromRequestRuleText("||cdn.example^*/ad_status^")
        )

        val result = RuleCompiler().compile(
            requestRules = listOf(
                shortFallbackRule,
                matchingUrlRule,
                unrelatedUrlRule,
                matchingDomainRule,
                patternFallbackRule
            ),
            elementRules = emptyList()
        )

        assertEquals(
            listOf(shortFallbackRule, matchingUrlRule, patternFallbackRule),
            result.requestCandidatesFor(
                action = RuleAction.BLOCK,
                host = null,
                url = "https://static.example.com/assets/pagead/banner.js"
            ).map { capability -> capability.rule }
        )
        assertEquals(
            listOf(
                shortFallbackRule,
                matchingUrlRule,
                unrelatedUrlRule,
                matchingDomainRule,
                patternFallbackRule
            ),
            result.requestCandidatesFor(
                action = RuleAction.BLOCK,
                host = "stats.g.doubleclick.net",
                url = null
            ).map { capability -> capability.rule }
        )
    }

    /**
     * 测试函数 `requestRuleIndex_separatesAllowAndBlockUrlKeywordCandidates`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `request Rule Index separates Allow And Block Url Keyword Candidates` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun requestRuleIndex_separatesAllowAndBlockUrlKeywordCandidates() {
        val allowRule = Rule.allowUrlContains("/pagead/allowed.js")
        val blockRule = Rule.blockUrlContains("/pagead/")

        val result = RuleCompiler().compile(
            requestRules = listOf(blockRule, allowRule),
            elementRules = emptyList()
        )

        assertEquals(
            listOf(allowRule),
            result.requestCandidatesFor(
                action = RuleAction.ALLOW,
                host = "static.example.com",
                url = "https://static.example.com/pagead/allowed.js"
            ).map { capability -> capability.rule }
        )
        assertEquals(
            listOf(blockRule),
            result.requestCandidatesFor(
                action = RuleAction.BLOCK,
                host = "static.example.com",
                url = "https://static.example.com/pagead/allowed.js"
            ).map { capability -> capability.rule }
        )
    }

    /**
     * 测试函数 `elementRuleIndex_returnsPageHostCandidatesAndFallbackInOriginalOrder`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `element Rule Index returns Page Host Candidates And Fallback In Original Order` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun elementRuleIndex_returnsPageHostCandidatesAndFallbackInOriginalOrder() {
        val globalHideRule = ElementRule(
            id = "css:global",
            selector = ".ad-banner",
            type = ElementRuleType.CSS_HIDE
        )
        val youtubeHideRule = ElementRule(
            id = "css:youtube",
            selector = "#player-ads",
            type = ElementRuleType.CSS_HIDE,
            domains = setOf("youtube.com")
        )
        val vimeoHideRule = ElementRule(
            id = "css:vimeo",
            selector = ".vimeo-sponsored",
            type = ElementRuleType.CSS_HIDE,
            domains = setOf("vimeo.com")
        )
        val excludedOnlyRule = ElementRule(
            id = "css:excluded",
            selector = ".sponsored",
            type = ElementRuleType.CSS_HIDE,
            excludedDomains = setOf("safe.example.com")
        )
        val exampleExceptionRule = ElementRule(
            id = "css:exception",
            selector = ".ad-banner",
            type = ElementRuleType.CSS_UNHIDE,
            domains = setOf("example.com")
        )
        val exampleDomRule = ElementRule(
            id = "dom:example",
            selector = ".popup-ad",
            type = ElementRuleType.DOM_REMOVE,
            domains = setOf("example.com")
        )

        val result = RuleCompiler().compile(
            requestRules = emptyList(),
            elementRules = listOf(
                globalHideRule,
                youtubeHideRule,
                vimeoHideRule,
                excludedOnlyRule,
                exampleExceptionRule,
                exampleDomRule
            )
        )

        assertEquals(
            listOf(globalHideRule, youtubeHideRule, excludedOnlyRule),
            result.cssHideCandidatesFor("m.youtube.com")
                .map { capability -> capability.rule }
        )
        assertEquals(
            listOf(globalHideRule, vimeoHideRule, excludedOnlyRule),
            result.cssHideCandidatesFor("player.vimeo.com")
                .map { capability -> capability.rule }
        )
        assertEquals(
            listOf(globalHideRule, excludedOnlyRule),
            result.cssHideCandidatesFor(null)
                .map { capability -> capability.rule }
        )
        assertEquals(
            listOf(exampleExceptionRule),
            result.cssUnhideCandidatesFor("www.example.com")
                .map { capability -> capability.rule }
        )
        assertEquals(
            listOf(exampleDomRule),
            result.domRemoveCandidatesFor("news.example.com")
                .map { capability -> capability.rule }
        )
    }

    /**
     * 测试函数 `elementRuleIndex_keepsGlobalAndExcludedFallbackWithMixedDomainRules`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `element Rule Index keeps Global And Excluded Fallback With Mixed Domain Rules` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun elementRuleIndex_keepsGlobalAndExcludedFallbackWithMixedDomainRules() {
        val globalHideRule = ElementRule(
            id = "css:global",
            selector = ".ad-banner",
            type = ElementRuleType.CSS_HIDE
        )
        val mixedDomainRule = ElementRule(
            id = "css:mixed",
            selector = ".video-ad",
            type = ElementRuleType.CSS_HIDE,
            domains = setOf("example.com"),
            excludedDomains = setOf("safe.example.com")
        )
        val excludedOnlyRule = ElementRule(
            id = "css:excluded-only",
            selector = ".sponsored",
            type = ElementRuleType.CSS_HIDE,
            excludedDomains = setOf("blocked.example.com")
        )

        val result = RuleCompiler().compile(
            requestRules = emptyList(),
            elementRules = listOf(
                globalHideRule,
                mixedDomainRule,
                excludedOnlyRule
            )
        )

        assertEquals(
            listOf(globalHideRule, mixedDomainRule, excludedOnlyRule),
            result.cssHideCandidatesFor("www.example.com")
                .map { capability -> capability.rule }
        )
        assertEquals(
            listOf(globalHideRule, mixedDomainRule, excludedOnlyRule),
            result.cssHideCandidatesFor("safe.example.com")
                .map { capability -> capability.rule }
        )
        assertEquals(
            listOf(globalHideRule, excludedOnlyRule),
            result.cssHideCandidatesFor(null)
                .map { capability -> capability.rule }
        )
    }
}
