package com.example.videobrowser.rules

import com.example.videobrowser.browser.ResourceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleIndexLargeFilePerformanceTest {
    @Test
    fun largeRuleFile_requestIndexKeepsCandidatesSmallAndMatchesLinearSemantics() {
        val domainTargetKey = ruleKey(777)
        val urlTargetKey = ruleKey(1_888)
        val allowTargetKey = ruleKey(2_222)
        val fallbackTargetPattern = fallbackPattern(0)
        val fallbackTargetRule = Rule.blockUrlContains(
            fallbackTargetPattern,
            id = "block:fallback:$fallbackTargetPattern"
        )
        val fallbackRules = listOf(fallbackTargetRule) +
            (1 until REQUEST_FALLBACK_RULE_COUNT).map { index ->
                val pattern = fallbackPattern(index)
                Rule.blockUrlContains(pattern, id = "block:fallback:$pattern")
            }
        val domainRules = (0 until LARGE_REQUEST_RULE_COUNT).map { index ->
            val key = ruleKey(index)
            Rule.blockDomainContains(
                domain = "ads-$key.example.com",
                id = "block:domain:$key"
            )
        }
        val urlRules = (0 until LARGE_REQUEST_RULE_COUNT).map { index ->
            val key = ruleKey(index)
            Rule.blockUrlContains(
                pattern = "/${key}campaign/banner.js",
                id = "block:url:$key"
            )
        }
        val allowHostBlockRule = Rule.blockDomainContains(
            domain = "ads-allow.example.com",
            id = "block:domain:allow-host"
        )
        val allowRule = Rule.allowUrlContains(
            pattern = "/${allowTargetKey}campaign/safe.js",
            id = "allow:url:$allowTargetKey"
        )
        val requestRules = fallbackRules + domainRules + urlRules + allowHostBlockRule + allowRule

        val compiledTiming = timed {
            RuleCompiler().compile(
                requestRules = requestRules,
                elementRules = emptyList()
            )
        }
        assertTrue(
            "编译 ${requestRules.size} 条请求规则耗时 ${compiledTiming.elapsedMillis}ms，超过回归阈值",
            compiledTiming.elapsedMillis < LARGE_RULE_COMPILE_LIMIT_MS
        )

        val compiled = compiledTiming.value
        val domainRule = domainRules[777]
        val domainHost = "media.ads-$domainTargetKey.example.com"
        val domainUrl = "https://$domainHost/player.js"
        val domainCandidates = compiled.requestCandidatesFor(
            action = RuleAction.BLOCK,
            host = domainHost,
            url = domainUrl
        ).map { capability -> capability.rule }
        assertTrimmedCandidates(
            label = "域名索引",
            candidateCount = domainCandidates.size,
            fullRuleCount = requestRules.count { rule -> rule.action == RuleAction.BLOCK }
        )
        assertSame(domainRule, domainCandidates.single { rule -> rule === domainRule })

        val urlRule = urlRules[1_888]
        val urlHost = "cdn.video.example.com"
        val url = "https://$urlHost/assets/${urlTargetKey}campaign/banner.js"
        val urlCandidates = compiled.requestCandidatesFor(
            action = RuleAction.BLOCK,
            host = urlHost,
            url = url
        ).map { capability -> capability.rule }
        assertTrimmedCandidates(
            label = "URL 关键词索引",
            candidateCount = urlCandidates.size,
            fullRuleCount = requestRules.count { rule -> rule.action == RuleAction.BLOCK }
        )
        assertSame(urlRule, urlCandidates.single { rule -> rule === urlRule })

        val fallbackUrl = "https://static.video.example.com/assets/$fallbackTargetPattern.js"
        val fallbackCandidates = compiled.requestCandidatesFor(
            action = RuleAction.BLOCK,
            host = "static.video.example.com",
            url = fallbackUrl
        ).map { capability -> capability.rule }
        assertEquals(fallbackRules, fallbackCandidates)

        val allowUrl = "https://ads-allow.example.com/assets/${allowTargetKey}campaign/safe.js"
        val allowCandidates = compiled.requestCandidatesFor(
            action = RuleAction.ALLOW,
            host = "ads-allow.example.com",
            url = allowUrl
        ).map { capability -> capability.rule }
        assertEquals(listOf(allowRule), allowCandidates)

        val engine = RuleEngine(requestRules)
        assertIndexedRequestMatchesLinear(
            requestRules = requestRules,
            engine = engine,
            url = domainUrl,
            host = domainHost
        )
        assertIndexedRequestMatchesLinear(
            requestRules = requestRules,
            engine = engine,
            url = url,
            host = urlHost
        )
        assertIndexedRequestMatchesLinear(
            requestRules = requestRules,
            engine = engine,
            url = fallbackUrl,
            host = "static.video.example.com"
        )
        assertIndexedRequestMatchesLinear(
            requestRules = requestRules,
            engine = engine,
            url = allowUrl,
            host = "ads-allow.example.com"
        )

        var matchedCount = 0
        val matchTiming = timed {
            repeat(REQUEST_MATCH_REPEATS) {
                if (engine.matchRequest(domainUrl, host = domainHost).shouldBlock) matchedCount += 1
                if (engine.matchRequest(url, host = urlHost).shouldBlock) matchedCount += 1
                if (engine.matchRequest(fallbackUrl, host = "static.video.example.com").shouldBlock) {
                    matchedCount += 1
                }
                if (engine.matchRequest(allowUrl, host = "ads-allow.example.com").shouldAllow) {
                    matchedCount += 1
                }
            }
        }
        assertEquals(REQUEST_MATCH_REPEATS * 4, matchedCount)
        assertTrue(
            "索引匹配 ${REQUEST_MATCH_REPEATS * 4} 次耗时 ${matchTiming.elapsedMillis}ms，超过回归阈值",
            matchTiming.elapsedMillis < LARGE_RULE_MATCH_LIMIT_MS
        )
    }

    @Test
    fun largeRuleFile_elementIndexKeepsCandidatesSmallAndMatchesLinearSemantics() {
        val targetKey = ruleKey(1_234)
        val globalCssRules = (0 until ELEMENT_FALLBACK_RULE_COUNT).map { index ->
            ElementRule(
                id = "css:global:$index",
                selector = ".global-ad-$index",
                type = ElementRuleType.CSS_HIDE
            )
        }
        val excludedOnlyRule = ElementRule(
            id = "css:excluded-only",
            selector = ".regional-sponsored",
            type = ElementRuleType.CSS_HIDE,
            excludedDomains = setOf("blocked.example.com")
        )
        val scopedCssRules = (0 until LARGE_ELEMENT_RULE_COUNT).map { index ->
            val key = ruleKey(index)
            ElementRule(
                id = "css:site:$key",
                selector = "#player-ads-$key",
                type = ElementRuleType.CSS_HIDE,
                domains = setOf("site-$key.example.com")
            )
        }
        val cssExceptionRule = ElementRule(
            id = "css:unhide:global-0",
            selector = ".global-ad-0",
            type = ElementRuleType.CSS_UNHIDE,
            domains = setOf("site-$targetKey.example.com")
        )
        val globalDomRules = (0 until ELEMENT_DOM_FALLBACK_RULE_COUNT).map { index ->
            ElementRule(
                id = "dom:global:$index",
                selector = ".popup-ad-$index",
                type = ElementRuleType.DOM_REMOVE
            )
        }
        val scopedDomRules = (0 until LARGE_ELEMENT_RULE_COUNT).map { index ->
            val key = ruleKey(index)
            ElementRule(
                id = "dom:site:$key",
                selector = ".overlay-ad-$key",
                type = ElementRuleType.DOM_REMOVE,
                domains = setOf("site-$key.example.com")
            )
        }
        val elementRules = globalCssRules +
            excludedOnlyRule +
            scopedCssRules +
            cssExceptionRule +
            globalDomRules +
            scopedDomRules

        val compiledTiming = timed {
            RuleCompiler().compile(
                requestRules = emptyList(),
                elementRules = elementRules
            )
        }
        assertTrue(
            "编译 ${elementRules.size} 条元素规则耗时 ${compiledTiming.elapsedMillis}ms，超过回归阈值",
            compiledTiming.elapsedMillis < LARGE_RULE_COMPILE_LIMIT_MS
        )

        val compiled = compiledTiming.value
        val pageHost = "player.site-$targetKey.example.com"
        val pageUrl = "https://$pageHost/watch"
        val cssCandidates = compiled.cssHideCandidatesFor(pageHost)
            .map { capability -> capability.rule }
        assertTrimmedCandidates(
            label = "CSS 页面 host 索引",
            candidateCount = cssCandidates.size,
            fullRuleCount = scopedCssRules.size + globalCssRules.size + 1
        )
        assertSame(scopedCssRules[1_234], cssCandidates.single { rule -> rule === scopedCssRules[1_234] })

        val unhideCandidates = compiled.cssUnhideCandidatesFor(pageHost)
            .map { capability -> capability.rule }
        assertEquals(listOf(cssExceptionRule), unhideCandidates)

        val domCandidates = compiled.domRemoveCandidatesFor(pageHost)
            .map { capability -> capability.rule }
        assertTrimmedCandidates(
            label = "DOM 页面 host 索引",
            candidateCount = domCandidates.size,
            fullRuleCount = scopedDomRules.size + globalDomRules.size
        )
        assertSame(scopedDomRules[1_234], domCandidates.single { rule -> rule === scopedDomRules[1_234] })

        val engine = RuleEngine(
            rules = emptyList(),
            elementRules = elementRules
        )
        assertEquals(
            expectedCssSelectors(elementRules, pageUrl),
            engine.cssSelectorsFor(pageUrl)
        )
        assertEquals(
            expectedDomSelectors(elementRules, pageUrl),
            engine.domSelectorsFor(pageUrl)
        )

        var selectorCount = 0
        val selectorTiming = timed {
            repeat(ELEMENT_SELECTOR_REPEATS) {
                selectorCount += engine.cssSelectorsFor(pageUrl).size
                selectorCount += engine.domSelectorsFor(pageUrl).size
            }
        }
        assertTrue(selectorCount > 0)
        assertTrue(
            "元素选择器索引查询 ${ELEMENT_SELECTOR_REPEATS * 2} 次耗时 ${selectorTiming.elapsedMillis}ms，超过回归阈值",
            selectorTiming.elapsedMillis < LARGE_RULE_MATCH_LIMIT_MS
        )
    }

    private fun assertIndexedRequestMatchesLinear(
        requestRules: List<Rule>,
        engine: RuleEngine,
        url: String,
        host: String,
        pageHost: String = "www.video.example.com",
        resourceType: ResourceType = ResourceType.SCRIPT
    ) {
        val expectedRule = firstLinearMatch(
            requestRules = requestRules,
            action = RuleAction.ALLOW,
            url = url,
            host = host,
            pageHost = pageHost,
            resourceType = resourceType
        ) ?: firstLinearMatch(
            requestRules = requestRules,
            action = RuleAction.BLOCK,
            url = url,
            host = host,
            pageHost = pageHost,
            resourceType = resourceType
        )

        val result = engine.matchRequest(
            url = url,
            host = host,
            pageHost = pageHost,
            resourceType = resourceType
        )
        if (expectedRule == null) {
            assertFalse(result.matched)
        } else {
            assertEquals(expectedRule.action, result.action)
            assertSame(expectedRule, result.rule)
        }
    }

    private fun firstLinearMatch(
        requestRules: List<Rule>,
        action: RuleAction,
        url: String,
        host: String,
        pageHost: String,
        resourceType: ResourceType
    ): Rule? {
        val matcher = RuleMatcher()
        return requestRules.firstOrNull { rule ->
            rule.action == action &&
                matcher.matches(
                    rule = rule,
                    url = url,
                    host = host,
                    pageHost = pageHost,
                    resourceType = resourceType
                )
        }
    }

    private fun expectedCssSelectors(
        elementRules: List<ElementRule>,
        pageUrl: String
    ): List<String> {
        val exceptions = elementRules
            .filter { rule -> rule.type == ElementRuleType.CSS_UNHIDE }
            .filter { rule -> rule.matchesPage(pageUrl) }
            .map { rule -> rule.selector }
            .toSet()
        return elementRules
            .filter { rule -> rule.type == ElementRuleType.CSS_HIDE }
            .filter { rule -> rule.matchesPage(pageUrl) }
            .map { rule -> rule.selector }
            .filterNot { selector -> selector in exceptions }
            .distinct()
    }

    private fun expectedDomSelectors(
        elementRules: List<ElementRule>,
        pageUrl: String
    ): List<String> {
        return elementRules
            .filter { rule -> rule.type == ElementRuleType.DOM_REMOVE }
            .filter { rule -> rule.matchesPage(pageUrl) }
            .map { rule -> rule.selector }
            .distinct()
    }

    private fun assertTrimmedCandidates(
        label: String,
        candidateCount: Int,
        fullRuleCount: Int
    ) {
        // G2-07 的性能护栏重点是候选集规模，而不是依赖机器速度的精确耗时。
        assertTrue(
            "$label 候选集 $candidateCount 应明显小于完整规则集 $fullRuleCount",
            candidateCount <= fullRuleCount / 20
        )
    }

    private inline fun <T> timed(block: () -> T): TimedResult<T> {
        val startedAt = System.nanoTime()
        val value = block()
        val elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000L
        return TimedResult(value, elapsedMillis)
    }

    private fun ruleKey(index: Int): String {
        return index.toString(36).padStart(3, '0')
    }

    private fun fallbackPattern(index: Int): String {
        return "q${index.toString(36)}"
    }

    private data class TimedResult<T>(
        val value: T,
        val elapsedMillis: Long
    )

    private companion object {
        const val LARGE_REQUEST_RULE_COUNT = 3_000
        const val LARGE_ELEMENT_RULE_COUNT = 3_000
        const val REQUEST_FALLBACK_RULE_COUNT = 12
        const val ELEMENT_FALLBACK_RULE_COUNT = 12
        const val ELEMENT_DOM_FALLBACK_RULE_COUNT = 4
        const val REQUEST_MATCH_REPEATS = 500
        const val ELEMENT_SELECTOR_REPEATS = 300
        const val LARGE_RULE_COMPILE_LIMIT_MS = 5_000L
        const val LARGE_RULE_MATCH_LIMIT_MS = 3_000L
    }
}
