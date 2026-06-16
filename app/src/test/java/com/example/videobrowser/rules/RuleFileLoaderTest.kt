package com.example.videobrowser.rules

/**
 * 测试阅读提示：
 * 这个测试文件验证“Rule File Loader Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
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

    /**
     * 测试函数 `loadRequestRules_readsAssetsAndCacheFiles`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `load Request Rules reads Assets And Cache Files` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `loadRequestRules_usesCacheWhenAssetOpenFails`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `load Request Rules uses Cache When Asset Open Fails` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `loadRequestRules_usesCacheRulesAndRecordsSkippedAssetWhenAssetReadFails`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `load Request Rules uses Cache Rules And Records Skipped Asset When Asset Read Fails` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `loadRequestRules_skipsUnknownRedirectResources`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `load Request Rules skips Unknown Redirect Resources` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `loadScriptletRules_readsAssetsAndCacheFilesAndSkipsUnknownScriptlets`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `load Scriptlet Rules reads Assets And Cache Files And Skips Unknown Scriptlets` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `loadCssRules_parsesGlobalAndDomainScopedSelectors`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `load Css Rules parses Global And Domain Scoped Selectors` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `loadCssRules_doesNotTreatScriptletRulesAsSelectors`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `load Css Rules does Not Treat Scriptlet Rules As Selectors` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `loadDomRules_parsesRemoveSelectorsAndSkipsUnsupportedLines`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `load Dom Rules parses Remove Selectors And Skips Unsupported Lines` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `loadRemoveParamRules_readsAssetsAndCacheFiles`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `load Remove Param Rules reads Assets And Cache Files` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun loadRemoveParamRules_readsAssetsAndCacheFiles() {
        val cacheDirectory = temporaryFolder.newFolder()
        cacheDirectory.resolve(RuleFileLoader.REMOVE_PARAM_RULES_CACHE_FILE)
            .writeText("||cache.example.com^${'$'}removeparam=fbclid\n", Charsets.UTF_8)
        val loader = loaderFor(
            cacheDirectory = cacheDirectory,
            assets = mapOf(
                RuleFileLoader.REMOVE_PARAM_RULES_ASSET to
                    "||asset.example.com^${'$'}removeparam=utm_source\n"
            )
        )

        val result = loader.loadRemoveParamRules()

        assertEquals(2, result.rules.size)
        assertEquals("asset.example.com", result.rules[0].pattern)
        assertEquals("cache.example.com", result.rules[1].pattern)
        assertEquals("utm_source", result.rules[0].parameterName)
        assertEquals("fbclid", result.rules[1].parameterName)
        assertTrue(result.skippedRules.isEmpty())
    }

    /**
     * 测试函数 `loaderFor`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `loader For` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param cacheDirectory 参数类型为 `java.io.File?`，表示函数执行 `cacheDirectory` 相关逻辑时需要读取或处理的输入。
     * @param assets 参数类型为 `Map<String, String>`，表示函数执行 `assets` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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
        /**
         * 测试函数 `read`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `read` 这条行为是否成立。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        override fun read(): Int {
            throw IOException(message)
        }
    }
}
