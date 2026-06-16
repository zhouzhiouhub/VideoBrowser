package com.example.videobrowser.rules

/**
 * 测试阅读提示：
 * 这个测试文件验证“Rule Subscription Importer Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import com.example.videobrowser.adguard.AdGuardRuleParser
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class RuleSubscriptionImporterTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun importText_writesSupportedRuleBucketsAndSourceMetadata() {
        val cacheDirectory = temporaryFolder.newFolder()
        val importer = RuleSubscriptionImporter(
            cacheDirectory = cacheDirectory,
            parser = AdGuardRuleParser()
        )

        val result = importer.importText(
            subscriptionId = "easylist-cn",
            text = """
                ||ads.example.com^
                example.com##.ad-banner
                example.com##+js(fetch-block-keyword, /pagead/)
                ||tracker.example.com^${'$'}removeparam=utm_source
            """.trimIndent()
        )

        assertTrue(result.updated)
        assertEquals(1, result.requestRuleCount)
        assertEquals(1, result.cssRuleCount)
        assertEquals(1, result.scriptletRuleCount)
        assertEquals(1, result.removeParamRuleCount)
        assertEquals(0, result.skippedRuleCount)

        assertEquals("||ads.example.com^\n", cacheDirectory.resolve(RuleFileLoader.REQUEST_RULES_CACHE_FILE).readText())
        assertEquals("example.com##.ad-banner\n", cacheDirectory.resolve(RuleFileLoader.CSS_RULES_CACHE_FILE).readText())
        assertEquals(
            "example.com##+js(fetch-block-keyword, /pagead/)\n",
            cacheDirectory.resolve(RuleFileLoader.SCRIPTLET_RULES_CACHE_FILE).readText()
        )
        assertEquals(
            "||tracker.example.com^${'$'}removeparam=utm_source\n",
            cacheDirectory.resolve(RuleFileLoader.REMOVE_PARAM_RULES_CACHE_FILE).readText()
        )

        val loader = RuleFileLoader(openAsset = { null }, cacheDirectory = cacheDirectory)
        assertEquals(
            "cache:${RuleFileLoader.REQUEST_RULES_CACHE_FILE}:subscription:easylist-cn",
            loader.loadRequestRules().rules.single().source
        )
        assertEquals(
            "cache:${RuleFileLoader.REMOVE_PARAM_RULES_CACHE_FILE}:subscription:easylist-cn",
            loader.loadRemoveParamRules().rules.single().source
        )
    }

    @Test
    fun update_preservesExistingCacheWhenFetchFails() {
        val cacheDirectory = temporaryFolder.newFolder()
        cacheDirectory.resolve(RuleFileLoader.REQUEST_RULES_CACHE_FILE)
            .writeText("||old.example.com^\n", Charsets.UTF_8)
        val importer = RuleSubscriptionImporter(
            cacheDirectory = cacheDirectory,
            parser = AdGuardRuleParser()
        )

        val result = importer.update(
            subscriptionId = "easylist-cn",
            fetchText = { throw IOException("network down") }
        )

        assertFalse(result.updated)
        assertTrue(result.usedExistingCache)
        assertEquals("network down", result.errorMessage)
        assertEquals(
            "||old.example.com^\n",
            cacheDirectory.resolve(RuleFileLoader.REQUEST_RULES_CACHE_FILE).readText()
        )
    }
}
