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
import com.example.videobrowser.storage.PreferenceStore
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
        assertFalse(store.contains(SavedPageCollection.BOOKMARKS.key))
        assertFalse(store.contains(SavedPageCollection.HISTORY.key))
        assertFalse(store.contains(BrowserTabSessionRepository.KEY_STANDARD_TAB_SESSION))
        assertFalse(rulesDirectory.exists())
    }

    private class InMemoryPreferenceStore : PreferenceStore {
        private val values = mutableMapOf<String, Any>()

        /**
         * 测试函数 `contains`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `contains` 这条行为是否成立。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        override fun contains(key: String): Boolean {
            return values.containsKey(key)
        }

        /**
         * 测试函数 `getBoolean`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `get Boolean` 这条行为是否成立。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
         * @param defaultValue 参数类型为 `Boolean`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
            return values[key] as? Boolean ?: defaultValue
        }

        /**
         * 测试函数 `putBoolean`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `put Boolean` 这条行为是否成立。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
         * @param value 参数类型为 `Boolean`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
         */
        override fun putBoolean(key: String, value: Boolean) {
            values[key] = value
        }

        /**
         * 测试函数 `getFloat`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `get Float` 这条行为是否成立。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
         * @param defaultValue 参数类型为 `Float`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        override fun getFloat(key: String, defaultValue: Float): Float {
            return values[key] as? Float ?: defaultValue
        }

        /**
         * 测试函数 `putFloat`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `put Float` 这条行为是否成立。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
         * @param value 参数类型为 `Float`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
         */
        override fun putFloat(key: String, value: Float) {
            values[key] = value
        }

        /**
         * 测试函数 `getString`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `get String` 这条行为是否成立。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
         * @param defaultValue 参数类型为 `String?`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        override fun getString(key: String, defaultValue: String?): String? {
            return values[key] as? String ?: defaultValue
        }

        /**
         * 测试函数 `putString`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `put String` 这条行为是否成立。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
         * @param value 参数类型为 `String`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
         */
        override fun putString(key: String, value: String) {
            values[key] = value
        }

        /**
         * 测试函数 `remove`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `remove` 这条行为是否成立。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
         */
        override fun remove(key: String) {
            values.remove(key)
        }

        /**
         * 测试函数 `remove`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `remove` 这条行为是否成立。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param keys 参数类型为 `Iterable<String>`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
         * @param commit 参数类型为 `Boolean`，表示函数执行 `commit` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        override fun remove(keys: Iterable<String>, commit: Boolean): Boolean {
            keys.forEach { key -> values.remove(key) }
            return true
        }
    }
}
