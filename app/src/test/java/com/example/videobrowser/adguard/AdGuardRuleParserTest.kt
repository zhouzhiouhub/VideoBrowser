package com.example.videobrowser.adguard

import com.example.videobrowser.browser.ResourceType
import com.example.videobrowser.rules.ElementRuleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdGuardRuleParserTest {
    @Test
    fun parseSubscription_splitsSupportedSafeRuleTypesAndRecordsSkippedLines() {
        val result = AdGuardRuleParser().parseSubscription(
            text = """
                ! comment
                ||ads.example.com^${'$'}third-party,script
                @@||safe.example.com^
                example.com##.ad-banner
                example.com#@#.ad-banner
                example.com##+js(fetch-block-keyword, /pagead/)
                ||tracker.example.com^${'$'}removeparam=utm_source
                ||unsupported.example.com^${'$'}bad-option
            """.trimIndent(),
            source = "subscription:test"
        )

        assertEquals(2, result.requestRules.size)
        assertEquals("ads.example.com", result.requestRules[0].pattern)
        assertEquals(true, result.requestRules[0].thirdParty)
        assertEquals(setOf(ResourceType.SCRIPT), result.requestRules[0].resourceTypes)
        assertEquals("safe.example.com", result.requestRules[1].pattern)

        assertEquals(2, result.elementRules.size)
        assertEquals(ElementRuleType.CSS_HIDE, result.elementRules[0].type)
        assertEquals(ElementRuleType.CSS_UNHIDE, result.elementRules[1].type)

        assertEquals(1, result.scriptletRules.size)
        assertEquals("fetch-block-keyword", result.scriptletRules.single().name)

        assertEquals(1, result.removeParamRules.size)
        assertEquals("utm_source", result.removeParamRules.single().parameterName)
        assertEquals("tracker.example.com", result.removeParamRules.single().pattern)

        assertEquals(1, result.skippedRules.size)
        assertEquals("unsupported rule syntax", result.skippedRules.single().reason)
        assertTrue(result.skippedRules.single().text.contains("bad-option"))
    }
}
