package com.example.videobrowser.rules

import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class RuleFileLoaderTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun loadRequestRules_readsAssetsAndCacheFiles() {
        val cacheDirectory = temporaryFolder.newFolder()
        cacheDirectory.resolve(RuleFileLoader.REQUEST_RULES_CACHE_FILE)
            .writeText("/cache-ad/\n", Charsets.UTF_8)
        val loader = loaderFor(
            cacheDirectory = cacheDirectory,
            assets = mapOf(
                RuleFileLoader.REQUEST_RULES_ASSET to "||asset-ad.example^\n@@||allow.example^\n"
            )
        )

        val result = loader.loadRequestRules()

        assertEquals(3, result.rules.size)
        assertTrue(result.rules.any { rule -> rule.source == "asset:${RuleFileLoader.REQUEST_RULES_ASSET}" })
        assertTrue(result.rules.any { rule -> rule.source == "cache:${RuleFileLoader.REQUEST_RULES_CACHE_FILE}" })
        assertTrue(result.skippedRules.isEmpty())
    }

    @Test
    fun loadRequestRules_usesCacheWhenAssetOpenFails() {
        val cacheDirectory = temporaryFolder.newFolder()
        cacheDirectory.resolve(RuleFileLoader.REQUEST_RULES_CACHE_FILE)
            .writeText("/cache-ad/\n", Charsets.UTF_8)
        val loader = RuleFileLoader(
            openAsset = { error("asset open failed") },
            cacheDirectory = cacheDirectory
        )

        val result = loader.loadRequestRules()

        assertEquals(1, result.rules.size)
        assertEquals("/cache-ad/", result.rules.single().pattern)
        assertEquals("cache:${RuleFileLoader.REQUEST_RULES_CACHE_FILE}", result.rules.single().source)
        assertTrue(result.skippedRules.isEmpty())
    }

    @Test
    fun loadRequestRules_usesCacheRulesAndRecordsSkippedAssetWhenAssetReadFails() {
        val cacheDirectory = temporaryFolder.newFolder()
        cacheDirectory.resolve(RuleFileLoader.REQUEST_RULES_CACHE_FILE)
            .writeText("||cache-ad.example^\n", Charsets.UTF_8)
        val loader = RuleFileLoader(
            openAsset = { path ->
                if (path == RuleFileLoader.REQUEST_RULES_ASSET) {
                    FailingInputStream("asset failure")
                } else {
                    null
                }
            },
            cacheDirectory = cacheDirectory
        )

        val result = loader.loadRequestRules()

        assertEquals(1, result.rules.size)
        assertEquals("cache-ad.example", result.rules.single().pattern)
        assertEquals("cache:${RuleFileLoader.REQUEST_RULES_CACHE_FILE}", result.rules.single().source)
        assertEquals(1, result.skippedRules.size)
        assertEquals("asset:${RuleFileLoader.REQUEST_RULES_ASSET}", result.skippedRules.single().source)
        assertEquals(0, result.skippedRules.single().lineNumber)
        assertEquals("asset failure", result.skippedRules.single().reason)
    }

    @Test
    fun loadRequestRules_skipsUnknownRedirectResources() {
        val loader = loaderFor(
            assets = mapOf(
                RuleFileLoader.REQUEST_RULES_ASSET to """
                    ||ads.example.com^${'$'}redirect=noopjs
                    ||evil.example.com^${'$'}redirect=https://evil.test/noop.js
                    ||unknown.example.com^${'$'}redirect=unknown
                """.trimIndent()
            )
        )

        val result = loader.loadRequestRules()

        assertEquals(1, result.rules.size)
        assertEquals("noopjs", result.rules.single().redirectResourceName)
        assertEquals(2, result.skippedRules.size)
        assertTrue(result.skippedRules.all { skippedRule ->
            skippedRule.reason == "unsupported request rule syntax"
        })
    }

    @Test
    fun loadScriptletRules_readsAssetsAndCacheFilesAndSkipsUnknownScriptlets() {
        val cacheDirectory = temporaryFolder.newFolder()
        cacheDirectory.resolve(RuleFileLoader.SCRIPTLET_RULES_CACHE_FILE)
            .writeText("example.com##+js(fetch-block-keyword, /cache-ad/)\n", Charsets.UTF_8)
        val loader = loaderFor(
            cacheDirectory = cacheDirectory,
            assets = mapOf(
                RuleFileLoader.SCRIPTLET_RULES_ASSET to """
                    example.com##+js(window-open-block-keyword, /popup-ad/)
                    example.com##+js(unknown-scriptlet, value)
                    example.com#%#alert('raw')
                """.trimIndent()
            )
        )

        val result = loader.loadScriptletRules()

        assertEquals(2, result.rules.size)
        assertEquals(
            listOf("window-open-block-keyword", "fetch-block-keyword"),
            result.rules.map { rule -> rule.name }
        )
        assertEquals(2, result.skippedRules.size)
        assertEquals(
            listOf(
                ScriptletRegistry.REASON_UNSUPPORTED_SCRIPTLET,
                ScriptletRegistry.REASON_RAW_SCRIPTLET_JAVASCRIPT
            ),
            result.skippedRules.map { skippedRule -> skippedRule.reason }
        )
    }

    @Test
    fun loadCssRules_parsesGlobalAndDomainScopedSelectors() {
        val loader = loaderFor(
            assets = mapOf(
                RuleFileLoader.CSS_RULES_ASSET to """
                    ##.ad-banner
                    youtube.com###player-ads
                    example.com#@#.ad-banner
                    ~safe.example.com##.sponsored
                    """.trimIndent()
            )
        )

        val result = loader.loadCssRules()

        assertEquals(4, result.rules.size)
        assertEquals(".ad-banner", result.rules[0].selector)
        assertEquals("#player-ads", result.rules[1].selector)
        assertEquals(setOf("youtube.com"), result.rules[1].normalizedDomains)
        assertEquals(ElementRuleType.CSS_UNHIDE, result.rules[2].type)
        assertEquals(setOf("example.com"), result.rules[2].normalizedDomains)
        assertEquals(setOf("safe.example.com"), result.rules[3].normalizedExcludedDomains)
        assertTrue(result.skippedRules.isEmpty())
    }

    @Test
    fun loadCssRules_doesNotTreatScriptletRulesAsSelectors() {
        val loader = loaderFor(
            assets = mapOf(
                RuleFileLoader.CSS_RULES_ASSET to """
                    example.com##+js(window-open-block-keyword, /popup-ad/)
                    example.com#%#//scriptlet('fetch-block-keyword', '/pagead/')
                    ##.ad-banner
                """.trimIndent()
            )
        )

        val result = loader.loadCssRules()

        assertEquals(1, result.rules.size)
        assertEquals(".ad-banner", result.rules.single().selector)
        assertTrue(result.skippedRules.isEmpty())
    }

    @Test
    fun loadDomRules_parsesRemoveSelectorsAndSkipsUnsupportedLines() {
        val loader = loaderFor(
            assets = mapOf(
                RuleFileLoader.DOM_RULES_ASSET to """
                    remove:.popup-ad
                    ##.not-dom
                    remove:div:has(.ad)
                """.trimIndent()
            )
        )

        val result = loader.loadDomRules()

        assertEquals(1, result.rules.size)
        assertEquals(".popup-ad", result.rules.single().selector)
        assertEquals(2, result.skippedRules.size)
    }

    private fun loaderFor(
        cacheDirectory: java.io.File? = null,
        assets: Map<String, String>
    ): RuleFileLoader {
        return RuleFileLoader(
            openAsset = { path ->
                assets[path]?.let { text ->
                    ByteArrayInputStream(text.toByteArray(Charsets.UTF_8))
                }
            },
            cacheDirectory = cacheDirectory
        )
    }

    private class FailingInputStream(
        private val message: String
    ) : InputStream() {
        override fun read(): Int {
            throw IOException(message)
        }
    }
}
