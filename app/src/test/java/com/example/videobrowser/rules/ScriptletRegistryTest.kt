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
    /**
     * 测试函数 `parseSupportedUboScriptlet_keepsNameArgumentsAndDomainScope`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `parse Supported Ubo Scriptlet keeps Name Arguments And Domain Scope` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `parseSupportedScriptletSyntax_stripsQuotedArguments`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `parse Supported Scriptlet Syntax strips Quoted Arguments` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `parseNoArgumentScriptlets_acceptsLocalToggleHooks`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `parse No Argument Scriptlets accepts Local Toggle Hooks` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `parseUnknownScriptlet_returnsSkippedReason`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `parse Unknown Scriptlet returns Skipped Reason` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `parseUnsafeArguments_returnsSkippedReason`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `parse Unsafe Arguments returns Skipped Reason` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `parseRawScriptletJavaScript_returnsSkippedReasonWithoutKeepingCode`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `parse Raw Scriptlet Java Script returns Skipped Reason Without Keeping Code` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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
