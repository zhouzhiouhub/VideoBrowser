package com.example.videobrowser.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class RemoveParamRuleTest {
    @Test
    fun fromAdGuardRuleText_parsesSafeExactParameterRules() {
        val rule = requireNotNull(
            RemoveParamRule.fromAdGuardRuleText(
                text = "||example.com^${'$'}removeparam=utm_source,domain=video.example.com|~safe.video.example.com",
                id = "remove:param",
                source = "subscription:test"
            )
        )

        assertEquals("remove:param", rule.id)
        assertEquals("example.com", rule.pattern)
        assertEquals(RuleType.DOMAIN_CONTAINS, rule.type)
        assertEquals("utm_source", rule.parameterName)
        assertEquals(setOf("video.example.com"), rule.domainScope.normalizedIncludedDomains)
        assertEquals(setOf("safe.video.example.com"), rule.domainScope.normalizedExcludedDomains)
    }

    @Test
    fun fromAdGuardRuleText_rejectsUnsafeOrUnsupportedRemoveParamRules() {
        assertNull(RemoveParamRule.fromAdGuardRuleText("@@||example.com^${'$'}removeparam=utm_source"))
        assertNull(RemoveParamRule.fromAdGuardRuleText("||example.com^${'$'}removeparam="))
        assertNull(RemoveParamRule.fromAdGuardRuleText("||example.com^${'$'}removeparam=/^utm_/"))
        assertNull(RemoveParamRule.fromAdGuardRuleText("||example.com^${'$'}removeparam=utm source"))
        assertNull(RemoveParamRule.fromAdGuardRuleText("||example.com^${'$'}removeparam=utm_source,bad-option"))
    }

    @Test
    fun cleanNavigationUrl_removesMatchingParametersOnlyForNavigationUrls() {
        val engine = RuleEngine(
            rules = emptyList(),
            removeParamRules = listOf(
                requireNotNull(
                    RemoveParamRule.fromAdGuardRuleText(
                        "||example.com^${'$'}removeparam=utm_source,domain=video.example.com|~safe.video.example.com"
                    )
                )
            )
        )

        val cleaned = engine.cleanNavigationUrl(
            url = "https://www.example.com/watch?utm_source=ad&id=123&utm_source=again#frag",
            pageUrl = "https://m.video.example.com/"
        )

        assertEquals("https://www.example.com/watch?id=123#frag", cleaned)
        assertEquals(
            "https://www.example.com/watch?utm_source=ad&id=123",
            engine.cleanNavigationUrl(
                url = "https://www.example.com/watch?utm_source=ad&id=123",
                pageUrl = "https://safe.video.example.com/"
            )
        )
        assertFalse(
            engine.matchRequest(
                url = "https://www.example.com/watch?utm_source=ad&id=123",
                host = "www.example.com"
            ).matched
        )
    }
}
