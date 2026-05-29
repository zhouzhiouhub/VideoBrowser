package com.example.videobrowser.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleCompilerTest {
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
}
