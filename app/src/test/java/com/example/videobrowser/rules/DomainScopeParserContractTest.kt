package com.example.videobrowser.rules

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainScopeParserContractTest {
    @Test
    fun `rule parsers share domain scope parsing`() {
        val adGuardParser = projectFile(
            "src/main/java/com/example/videobrowser/adguard/AdGuardRuleParser.kt"
        ).readText()
        val ruleLineParser = projectFile(
            "src/main/java/com/example/videobrowser/rules/RuleLineParser.kt"
        ).readText()
        val scriptletRegistry = projectFile(
            "src/main/java/com/example/videobrowser/rules/ScriptletRegistry.kt"
        ).readText()
        val requestRuleFactory = projectFile(
            "src/main/java/com/example/videobrowser/rules/RequestRuleFactory.kt"
        ).readText()

        assertTrue(adGuardParser.contains("DomainScopeParser.parseCommaSeparated"))
        assertTrue(ruleLineParser.contains("DomainScopeParser.parseCommaSeparated"))
        assertTrue(scriptletRegistry.contains("DomainScopeParser.parseCommaSeparated"))
        assertTrue(requestRuleFactory.contains("DomainScopeParser.parsePipeSeparated"))
        listOf(adGuardParser, ruleLineParser, scriptletRegistry).forEach { source ->
            assertFalse(source.contains("private fun parseDomains"))
            assertFalse(source.contains("private fun isValidDomain"))
        }
        assertFalse(requestRuleFactory.contains("private fun isValidDomainPattern"))
    }

}
