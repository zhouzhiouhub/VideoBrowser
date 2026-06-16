package com.example.videobrowser.rules

/**
 * 测试阅读提示：
 * 这个测试文件验证“Scriptlet Registry Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScriptletRegistryTest {
    @Test
    fun parseSupportedUboScriptlet_keepsNameArgumentsAndDomainScope() {
        val parsed = ScriptletRegistry.parse(
            text = "example.com,~safe.example.com##+js(window-open-block-keyword, /ad-popup/)",
            id = "asset:rules/scriptlet_rules.txt:1",
            source = "asset:rules/scriptlet_rules.txt"
        )

        assertTrue(parsed is ScriptletParseResult.Rule)
        val rule = (parsed as ScriptletParseResult.Rule).value
        assertEquals("asset:rules/scriptlet_rules.txt:1", rule.id)
        assertEquals("window-open-block-keyword", rule.name)
        assertEquals(listOf("/ad-popup/"), rule.arguments)
        assertTrue(rule.matchesPage("https://www.example.com/watch"))
        assertFalse(rule.matchesPage("https://safe.example.com/watch"))
        assertFalse(rule.matchesPage("https://other.example/watch"))
    }

    @Test
    fun parseSupportedScriptletSyntax_stripsQuotedArguments() {
        val parsed = ScriptletRegistry.parse(
            text = "example.com#%#//scriptlet('fetch-block-keyword', '/pagead/')",
            id = "asset:rules/scriptlet_rules.txt:2",
            source = "asset:rules/scriptlet_rules.txt"
        )

        assertTrue(parsed is ScriptletParseResult.Rule)
        val rule = (parsed as ScriptletParseResult.Rule).value
        assertEquals("fetch-block-keyword", rule.name)
        assertEquals(listOf("/pagead/"), rule.arguments)
        assertTrue(rule.matchesPage("https://example.com/"))
    }

    @Test
    fun parseNoArgumentScriptlets_acceptsLocalToggleHooks() {
        val skip = ScriptletRegistry.parse(
            text = "##+js(click-skip-buttons)",
            id = "asset:rules/scriptlet_rules.txt:3",
            source = "asset:rules/scriptlet_rules.txt"
        )
        val controls = ScriptletRegistry.parse(
            text = "##+js(enable-video-controls)",
            id = "asset:rules/scriptlet_rules.txt:4",
            source = "asset:rules/scriptlet_rules.txt"
        )

        assertEquals("click-skip-buttons", (skip as ScriptletParseResult.Rule).value.name)
        assertEquals(emptyList<String>(), skip.value.arguments)
        assertEquals("enable-video-controls", (controls as ScriptletParseResult.Rule).value.name)
        assertEquals(emptyList<String>(), controls.value.arguments)
    }

    @Test
    fun parseUnknownScriptlet_returnsSkippedReason() {
        val parsed = ScriptletRegistry.parse(
            text = "example.com##+js(run-raw-js, alert(1))",
            id = "asset:rules/scriptlet_rules.txt:5",
            source = "asset:rules/scriptlet_rules.txt"
        )

        assertTrue(parsed is ScriptletParseResult.Skipped)
        assertEquals(
            ScriptletRegistry.REASON_UNSUPPORTED_SCRIPTLET,
            (parsed as ScriptletParseResult.Skipped).reason
        )
    }

    @Test
    fun parseUnsafeArguments_returnsSkippedReason() {
        val parsed = ScriptletRegistry.parse(
            text = "example.com##+js(fetch-block-keyword, javascript:alert(1))",
            id = "asset:rules/scriptlet_rules.txt:6",
            source = "asset:rules/scriptlet_rules.txt"
        )

        assertTrue(parsed is ScriptletParseResult.Skipped)
        assertEquals(
            ScriptletRegistry.REASON_INVALID_ARGUMENTS,
            (parsed as ScriptletParseResult.Skipped).reason
        )
    }

    @Test
    fun parseRawScriptletJavaScript_returnsSkippedReasonWithoutKeepingCode() {
        val parsed = ScriptletRegistry.parse(
            text = "example.com#%#alert('raw')",
            id = "asset:rules/scriptlet_rules.txt:7",
            source = "asset:rules/scriptlet_rules.txt"
        )

        assertTrue(parsed is ScriptletParseResult.Skipped)
        assertEquals(
            ScriptletRegistry.REASON_RAW_SCRIPTLET_JAVASCRIPT,
            (parsed as ScriptletParseResult.Skipped).reason
        )
    }
}
