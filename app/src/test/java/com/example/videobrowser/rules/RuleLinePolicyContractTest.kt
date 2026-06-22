package com.example.videobrowser.rules

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleLinePolicyContractTest {
    @Test
    fun `rule parsers share line policy helpers`() {
        val sources = listOf(
            projectFile("src/main/java/com/example/videobrowser/adguard/AdGuardRuleParser.kt"),
            projectFile("src/main/java/com/example/videobrowser/rules/RuleLineParser.kt"),
            projectFile("src/main/java/com/example/videobrowser/rules/ScriptletRegistry.kt")
        ).map { file -> file.readText() }

        sources.forEach { source ->
            assertTrue(source.contains("RuleLinePolicy."))
            assertFalse(source.contains("private fun shouldIgnoreRuleLine"))
            assertFalse(source.contains("private fun isSafeSelector"))
            assertFalse(source.contains("private fun isScriptletRuleLine"))
            assertFalse(source.contains("UNSUPPORTED_SELECTOR_TOKENS"))
        }
    }

}
