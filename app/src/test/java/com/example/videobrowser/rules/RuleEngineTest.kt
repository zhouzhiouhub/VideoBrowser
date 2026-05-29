package com.example.videobrowser.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleEngineTest {
    @Test
    fun matchRequest_blocksUrlContainsRule() {
        val rule = Rule.blockUrlContains("/pagead/")
        val engine = RuleEngine(listOf(rule))

        val result = engine.matchRequest(
            url = "https://example.com/static/pagead/banner.js",
            host = "example.com"
        )

        assertTrue(result.matched)
        assertTrue(result.shouldBlock)
        assertEquals(RuleAction.BLOCK, result.action)
        assertSame(rule, result.rule)
    }

    @Test
    fun matchRequest_blocksDomainRuleWithSubdomains() {
        val rule = Rule.blockDomainContains("doubleclick.net")
        val engine = RuleEngine(listOf(rule))

        val result = engine.matchRequest(
            url = "https://stats.g.doubleclick.net/activityi",
            host = "stats.g.doubleclick.net"
        )

        assertTrue(result.shouldBlock)
        assertSame(rule, result.rule)
    }

    @Test
    fun matchRequest_doesNotTreatHostSubstringAsDomainMatch() {
        val engine = RuleEngine(
            listOf(Rule.blockDomainContains("doubleclick.net"))
        )

        val result = engine.matchRequest(
            url = "https://notdoubleclick.net/assets/app.js",
            host = "notdoubleclick.net"
        )

        assertFalse(result.matched)
        assertFalse(result.shouldBlock)
    }

    @Test
    fun matchRequest_allowsWhenNoRuleMatches() {
        val engine = RuleEngine(
            listOf(
                Rule.blockUrlContains("/pagead/"),
                Rule.blockDomainContains("doubleclick.net")
            )
        )

        val result = engine.matchRequest(
            url = "https://example.com/assets/app.js",
            host = "example.com"
        )

        assertFalse(result.matched)
        assertEquals(RuleAction.NONE, result.action)
        assertNull(result.rule)
    }

    @Test
    fun matchRequest_prefersWhitelistRuleOverBlockRule() {
        val allowRule = Rule.allowUrlContains("/pagead/allowed.js")
        val blockRule = Rule.blockDomainContains("doubleclick.net")
        val engine = RuleEngine(
            listOf(
                blockRule,
                allowRule
            )
        )

        val result = engine.matchRequest(
            url = "https://stats.g.doubleclick.net/pagead/allowed.js",
            host = "stats.g.doubleclick.net"
        )

        assertTrue(result.matched)
        assertTrue(result.shouldAllow)
        assertFalse(result.shouldBlock)
        assertEquals(RuleAction.ALLOW, result.action)
        assertSame(allowRule, result.rule)
    }

    @Test
    fun fromRequestRuleText_parsesP6SafeSubset() {
        val domainRule = requireNotNull(Rule.fromRequestRuleText("||doubleclick.net^"))
        val whitelistRule = requireNotNull(Rule.fromRequestRuleText("@@||example.com^"))
        val urlRule = requireNotNull(Rule.fromRequestRuleText("/pagead/"))
        val patternRule = requireNotNull(Rule.fromRequestRuleText("||doubleclick.net^*/ad_status^"))
        val thirdPartyRule = requireNotNull(Rule.fromRequestRuleText("||ads.example.com^\$third-party"))
        val domainScopedRule = requireNotNull(
            Rule.fromRequestRuleText("||ads.example.com^\$domain=video.example.com|~safe.video.example.com")
        )

        assertEquals(RuleType.DOMAIN_CONTAINS, domainRule.type)
        assertEquals(RuleAction.BLOCK, domainRule.action)
        assertEquals("doubleclick.net", domainRule.pattern)

        assertEquals(RuleType.DOMAIN_CONTAINS, whitelistRule.type)
        assertEquals(RuleAction.ALLOW, whitelistRule.action)
        assertEquals("example.com", whitelistRule.pattern)

        assertEquals(RuleType.URL_CONTAINS, urlRule.type)
        assertEquals(RuleAction.BLOCK, urlRule.action)
        assertEquals("/pagead/", urlRule.pattern)

        assertEquals(RuleType.URL_PATTERN, patternRule.type)
        assertEquals(true, thirdPartyRule.thirdParty)
        assertEquals(setOf("video.example.com"), domainScopedRule.domainScope.normalizedIncludedDomains)
        assertEquals(setOf("safe.video.example.com"), domainScopedRule.domainScope.normalizedExcludedDomains)
    }

    @Test
    fun fromRequestRuleText_rejectsUnsupportedSyntaxForP6() {
        assertNull(Rule.fromRequestRuleText("example.com##.ad"))
        assertNull(Rule.fromRequestRuleText("||example.com^\$redirect=noopjs"))
        assertNull(Rule.fromRequestRuleText("! comment"))
    }

    @Test
    fun matchRequest_supportsWildcardSeparatorAndDomainOptions() {
        val engine = RuleEngine(
            listOf(
                requireNotNull(
                    Rule.fromRequestRuleText(
                        "||doubleclick.net^*/ad_status^\$domain=video.example.com|~safe.video.example.com"
                    )
                )
            )
        )

        assertTrue(
            engine.matchRequest(
                url = "https://securepubads.g.doubleclick.net/gampad/ad_status?iu=/1",
                host = "securepubads.g.doubleclick.net",
                pageHost = "www.video.example.com"
            ).shouldBlock
        )
        assertFalse(
            engine.matchRequest(
                url = "https://securepubads.g.doubleclick.net/gampad/ad_status?iu=/1",
                host = "securepubads.g.doubleclick.net",
                pageHost = "safe.video.example.com"
            ).matched
        )
        assertFalse(
            engine.matchRequest(
                url = "https://securepubads.g.doubleclick.net/gampad/ad_status?iu=/1",
                host = "securepubads.g.doubleclick.net",
                pageHost = "example.org"
            ).matched
        )
    }

    @Test
    fun matchRequest_supportsThirdPartyOption() {
        val engine = RuleEngine(
            listOf(requireNotNull(Rule.fromRequestRuleText("||ads.cdn.com^\$third-party")))
        )

        assertTrue(
            engine.matchRequest(
                url = "https://ads.cdn.com/banner.js",
                host = "ads.cdn.com",
                pageHost = "www.video.example.com"
            ).shouldBlock
        )
        assertFalse(
            engine.matchRequest(
                url = "https://ads.cdn.com/banner.js",
                host = "ads.cdn.com",
                pageHost = "news.cdn.com"
            ).matched
        )
    }

    @Test
    fun elementSelectorsFor_filtersByTypeAndPageDomain() {
        val engine = RuleEngine(
            rules = emptyList(),
            elementRules = listOf(
                ElementRule(
                    id = "css:global",
                    selector = ".ad-banner",
                    type = ElementRuleType.CSS_HIDE
                ),
                ElementRule(
                    id = "css:site",
                    selector = "#player-ads",
                    type = ElementRuleType.CSS_HIDE,
                    domains = setOf("youtube.com")
                ),
                ElementRule(
                    id = "css:exception",
                    selector = ".ad-banner",
                    type = ElementRuleType.CSS_UNHIDE,
                    domains = setOf("example.com")
                ),
                ElementRule(
                    id = "css:excluded",
                    selector = ".sponsored",
                    type = ElementRuleType.CSS_HIDE,
                    excludedDomains = setOf("safe.example.com")
                ),
                ElementRule(
                    id = "dom:global",
                    selector = ".popup-ad",
                    type = ElementRuleType.DOM_REMOVE
                )
            )
        )

        assertEquals(
            listOf(".ad-banner", "#player-ads", ".sponsored"),
            engine.cssSelectorsFor("https://m.youtube.com/watch?v=1")
        )
        assertEquals(
            listOf(".sponsored"),
            engine.cssSelectorsFor("https://example.com/")
        )
        assertEquals(
            emptyList<String>(),
            engine.cssSelectorsFor("https://safe.example.com/")
        )
        assertEquals(
            listOf(".popup-ad"),
            engine.domSelectorsFor("https://example.com/")
        )
    }
}
