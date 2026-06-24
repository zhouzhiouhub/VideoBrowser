package com.example.videobrowser.settings

/**
 * 测试阅读提示：
 * 这个测试文件验证“Browser Default Settings Resetter Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import com.example.videobrowser.browser.BrowserTabSessionRepository
import com.example.videobrowser.rules.RuleFileLoader
import com.example.videobrowser.storage.SavedPageRepository
import com.example.videobrowser.storage.SavedPageRepository.SavedPageCollection
import com.example.videobrowser.testutil.InMemoryPreferenceStore
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class BrowserDefaultSettingsResetterTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    /**
     * 测试函数 `restoreDefaults_clearsSettingsAndCachedRules`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `restore Defaults clears Settings And Cached Rules` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun restoreDefaults_clearsSettingsAndCachedRules() {
        val store = InMemoryPreferenceStore()
        val settingsManager = SettingsManager(store)
        val savedPageRepository = SavedPageRepository(store)
        val browserTabSessionRepository = BrowserTabSessionRepository(store)
        val filesDir = temporaryFolder.newFolder()
        val rulesDirectory = filesDir.resolve("rules").apply { mkdirs() }
        rulesDirectory.resolve(RuleFileLoader.REQUEST_RULES_CACHE_FILE).writeText("/blocked-url/")

        settingsManager.setAdBlockEnabled(false)
        settingsManager.addUserElementHideSelectorForSite("video.example.com", ".blocked")
        settingsManager.addCustomShortcut("Docs", "https://docs.example.com/")
        settingsManager.addCustomSearchEngine("Docs", "https://docs.example.com/search?q=")
        store.putString(SavedPageCollection.BOOKMARKS.key, "bookmarks")
        store.putString(SavedPageCollection.HISTORY.key, "history")
        store.putString(BrowserTabSessionRepository.KEY_STANDARD_TAB_SESSION, "tabs")

        val resetter = BrowserDefaultSettingsResetter(
            settingsManager = settingsManager,
            savedPageRepository = savedPageRepository,
            browserTabSessionRepository = browserTabSessionRepository,
            filesDir = filesDir
        )

        assertTrue(resetter.restoreDefaults())

        assertTrue(settingsManager.isAdBlockEnabled())
        assertTrue(settingsManager.userElementHideRules().isEmpty())
        assertTrue(settingsManager.customShortcuts().isEmpty())
        assertTrue(settingsManager.customSearchEngines().isEmpty())
        assertFalse(store.contains(SavedPageCollection.BOOKMARKS.key))
        assertFalse(store.contains(SavedPageCollection.HISTORY.key))
        assertFalse(store.contains(BrowserTabSessionRepository.KEY_STANDARD_TAB_SESSION))
        assertFalse(rulesDirectory.exists())
    }

}
