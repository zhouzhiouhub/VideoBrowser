package com.example.videobrowser.rules

/**
 * 测试阅读提示：
 * 这个测试文件验证“Rule Engine Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import com.example.videobrowser.browser.ResourceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleEngineTest {
    /**
     * 测试函数 `scriptletHooksFor_filtersByPageDomainAndMapsSupportedHooks`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `scriptlet Hooks For filters By Page Domain And Maps Supported Hooks` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun scriptletHooksFor_filtersByPageDomainAndMapsSupportedHooks() {
        val engine = RuleEngine(
            rules = emptyList(),
            scriptletRules = listOf(
                ScriptletRule(
                    id = "hook:fetch",
                    name = "fetch-block-keyword",
                    arguments = listOf("/pagead/"),
                    domainScope = DomainScope(includedDomains = setOf("example.com"))
                ),
                ScriptletRule(
                    id = "hook:fetch-duplicate",
                    name = "fetch-block-keyword",
                    arguments = listOf("/pagead/"),
                    domainScope = DomainScope(includedDomains = setOf("example.com"))
                ),
                ScriptletRule(
                    id = "hook:open",
                    name = "window-open-block-keyword",
                    arguments = listOf("/popup-ad/"),
                    domainScope = DomainScope(includedDomains = setOf("example.com"))
                ),
                ScriptletRule(
                    id = "hook:skip",
                    name = "click-skip-buttons",
                    domainScope = DomainScope(includedDomains = setOf("other.com"))
                ),
                ScriptletRule(
                    id = "hook:controls",
                    name = "enable-video-controls",
                    domainScope = DomainScope(includedDomains = setOf("example.com"))
                )
            )
        )

        val pageUrl = "https://www.example.com/watch"

        assertEquals(listOf("/pagead/"), engine.scriptletFetchBlockedKeywordsFor(pageUrl))
        assertEquals(listOf("/popup-ad/"), engine.scriptletWindowOpenBlockedKeywordsFor(pageUrl))
        assertFalse(engine.isScriptletSkipButtonsEnabledFor(pageUrl))
        assertTrue(engine.isScriptletVideoControlsEnabledFor(pageUrl))
        assertTrue(engine.isScriptletSkipButtonsEnabledFor("https://other.com/watch"))
    }

    /**
     * 测试函数 `matchRequest_blocksUrlContainsRule`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `match Request blocks Url Contains Rule` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `matchRequest_blocksDomainRuleWithSubdomains`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `match Request blocks Domain Rule With Subdomains` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `matchRequest_doesNotTreatHostSubstringAsDomainMatch`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `match Request does Not Treat Host Substring As Domain Match` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `matchRequest_allowsWhenNoRuleMatches`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `match Request allows When No Rule Matches` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `matchRequest_prefersWhitelistRuleOverBlockRule`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `match Request prefers Whitelist Rule Over Block Rule` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `matchRequestCandidates_returnsAllMatchedRulesInOriginalRuleOrder`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `match Request Candidates returns All Matched Rules In Original Rule Order` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun matchRequestCandidates_returnsAllMatchedRulesInOriginalRuleOrder() {
        val blockRule = Rule.blockDomainContains("doubleclick.net")
        val allowRule = Rule.allowUrlContains("/pagead/allowed.js")
        val engine = RuleEngine(
            listOf(
                blockRule,
                allowRule
            )
        )

        val results = engine.matchRequestCandidates(
            url = "https://stats.g.doubleclick.net/pagead/allowed.js",
            host = "stats.g.doubleclick.net"
        )

        assertEquals(2, results.size)
        assertTrue(results[0].shouldBlock)
        assertSame(blockRule, results[0].rule)
        assertTrue(results[1].shouldAllow)
        assertSame(allowRule, results[1].rule)
    }

    /**
     * 测试函数 `matchRequest_domainIndexUsesUrlHostWhenHostIsMissing`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `match Request domain Index Uses Url Host When Host Is Missing` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun matchRequest_domainIndexUsesUrlHostWhenHostIsMissing() {
        val allowRule = Rule.allowDomainContains("doubleclick.net")
        val blockRule = Rule.blockDomainContains("doubleclick.net")
        val engine = RuleEngine(
            listOf(
                blockRule,
                allowRule
            )
        )

        val result = engine.matchRequest(
            url = "https://stats.g.doubleclick.net/pagead/allowed.js"
        )

        assertTrue(result.shouldAllow)
        assertSame(allowRule, result.rule)
    }

    /**
     * 测试函数 `matchRequest_domainIndexKeepsFallbackAndDomainRulesInOriginalOrder`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `match Request domain Index Keeps Fallback And Domain Rules In Original Order` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun matchRequest_domainIndexKeepsFallbackAndDomainRulesInOriginalOrder() {
        val urlRule = Rule.blockUrlContains("/pagead/")
        val domainRule = Rule.blockDomainContains("doubleclick.net")
        val engine = RuleEngine(
            listOf(
                urlRule,
                domainRule
            )
        )

        val result = engine.matchRequest(
            url = "https://stats.g.doubleclick.net/pagead/banner.js",
            host = "stats.g.doubleclick.net"
        )

        assertTrue(result.shouldBlock)
        assertSame(urlRule, result.rule)
    }

    /**
     * 测试函数 `fromRequestRuleText_parsesP6SafeSubset`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `from Request Rule Text parses P6 Safe Subset` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun fromRequestRuleText_parsesP6SafeSubset() {
        val domainRule = requireNotNull(Rule.fromRequestRuleText("||doubleclick.net^"))
        val whitelistRule = requireNotNull(Rule.fromRequestRuleText("@@||example.com^"))
        val urlRule = requireNotNull(Rule.fromRequestRuleText("/pagead/"))
        val patternRule = requireNotNull(Rule.fromRequestRuleText("||doubleclick.net^*/ad_status^"))
        val thirdPartyRule = requireNotNull(Rule.fromRequestRuleText("||ads.example.com^\$third-party"))
        val scriptRule = requireNotNull(Rule.fromRequestRuleText("||ads.example.com^\$script"))
        val redirectRule = requireNotNull(Rule.fromRequestRuleText("||ads.example.com^\$redirect=noopjs"))
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
        assertEquals(setOf(ResourceType.SCRIPT), scriptRule.resourceTypes)
        assertEquals("noopjs", redirectRule.redirectResourceName)
        assertEquals(setOf("video.example.com"), domainScopedRule.domainScope.normalizedIncludedDomains)
        assertEquals(setOf("safe.video.example.com"), domainScopedRule.domainScope.normalizedExcludedDomains)
    }

    /**
     * 测试函数 `fromRequestRuleText_rejectsUnsupportedSyntaxForP6`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `from Request Rule Text rejects Unsupported Syntax For P6` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun fromRequestRuleText_rejectsUnsupportedSyntaxForP6() {
        assertNull(Rule.fromRequestRuleText("example.com##.ad"))
        assertNull(Rule.fromRequestRuleText("||example.com^\$redirect=unknown"))
        assertNull(Rule.fromRequestRuleText("||example.com^\$redirect=https://evil.test/noop.js"))
        assertNull(Rule.fromRequestRuleText("||example.com^\$~script"))
        assertNull(Rule.fromRequestRuleText("! comment"))
    }

    /**
     * 测试函数 `matchRequest_supportsWildcardSeparatorAndDomainOptions`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `match Request supports Wildcard Separator And Domain Options` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `matchRequest_supportsThirdPartyOption`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `match Request supports Third Party Option` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `matchRequest_usesResourceTypeOptionsConservatively`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `match Request uses Resource Type Options Conservatively` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun matchRequest_usesResourceTypeOptionsConservatively() {
        val engine = RuleEngine(
            listOf(requireNotNull(Rule.fromRequestRuleText("||ads.cdn.com^\$script")))
        )

        assertTrue(
            engine.matchRequest(
                url = "https://ads.cdn.com/banner.js",
                host = "ads.cdn.com",
                resourceType = ResourceType.SCRIPT
            ).shouldBlock
        )
        assertFalse(
            engine.matchRequest(
                url = "https://ads.cdn.com/banner.js",
                host = "ads.cdn.com",
                resourceType = ResourceType.UNKNOWN
            ).matched
        )
        assertFalse(
            engine.matchRequest(
                url = "https://ads.cdn.com/banner.png",
                host = "ads.cdn.com",
                resourceType = ResourceType.IMAGE
            ).matched
        )
    }

    /**
     * 测试函数 `elementSelectorsFor_filtersByTypeAndPageDomain`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `element Selectors For filters By Type And Page Domain` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `elementSelectorsFor_preservesFallbackAndMixedDomainSemantics`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `element Selectors For preserves Fallback And Mixed Domain Semantics` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun elementSelectorsFor_preservesFallbackAndMixedDomainSemantics() {
        val engine = RuleEngine(
            rules = emptyList(),
            elementRules = listOf(
                ElementRule(
                    id = "css:global",
                    selector = ".ad-banner",
                    type = ElementRuleType.CSS_HIDE
                ),
                ElementRule(
                    id = "css:mixed",
                    selector = ".video-ad",
                    type = ElementRuleType.CSS_HIDE,
                    domains = setOf("example.com"),
                    excludedDomains = setOf("safe.example.com")
                ),
                ElementRule(
                    id = "css:excluded-only",
                    selector = ".sponsored",
                    type = ElementRuleType.CSS_HIDE,
                    excludedDomains = setOf("blocked.example.com")
                )
            )
        )

        assertEquals(
            listOf(".ad-banner", ".video-ad", ".sponsored"),
            engine.cssSelectorsFor("https://www.example.com/")
        )
        assertEquals(
            listOf(".ad-banner", ".sponsored"),
            engine.cssSelectorsFor("https://safe.example.com/")
        )
        assertEquals(
            listOf(".ad-banner", ".video-ad"),
            engine.cssSelectorsFor("https://blocked.example.com/")
        )
        assertEquals(
            listOf(".ad-banner"),
            engine.cssSelectorsFor(null)
        )
    }
}
