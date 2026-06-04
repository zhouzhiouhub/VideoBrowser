package com.example.videobrowser.rules

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class RuleEngineFactoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun clearRuleCache_removesCachedRuleFilesOnly() {
        val filesDir = temporaryFolder.newFolder()
        val rulesDirectory = filesDir.resolve("rules").apply { mkdirs() }
        rulesDirectory.resolve(RuleFileLoader.REQUEST_RULES_CACHE_FILE).writeText("/blocked-url/")
        rulesDirectory.resolve(RuleFileLoader.CSS_RULES_CACHE_FILE).writeText("##.ad")
        rulesDirectory.resolve(RuleFileLoader.DOM_RULES_CACHE_FILE).writeText("remove:.popup")
        val unrelatedFile = filesDir.resolve("bookmarks.json").apply { writeText("[]") }

        assertTrue(RuleEngineFactory.clearRuleCache(filesDir))

        assertFalse(rulesDirectory.exists())
        assertTrue(unrelatedFile.exists())
    }
}
