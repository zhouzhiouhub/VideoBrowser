package com.example.videobrowser.rules

/**
 * 测试阅读提示：
 * 这个测试文件验证“Rule Engine Factory Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class RuleEngineFactoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    /**
     * 测试函数 `clearRuleCache_removesCachedRuleFilesOnly`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `clear Rule Cache removes Cached Rule Files Only` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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
