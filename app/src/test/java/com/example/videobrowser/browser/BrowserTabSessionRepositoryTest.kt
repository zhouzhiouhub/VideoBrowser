package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Browser Tab Session Repository Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import com.example.videobrowser.storage.PreferenceStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class BrowserTabSessionRepositoryTest {
    /**
     * 测试函数 `saveAndRestoreRoundTrip`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `save And Restore Round Trip` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun saveAndRestoreRoundTrip() {
        val store = InMemoryPreferenceStore()
        val repository = BrowserTabSessionRepository(store)

        repository.save(
            tabs = listOf(
                BrowserTab(id = 1L, url = "https://a.example.com", title = "A"),
                BrowserTab(id = 2L, url = "https://b.example.com", title = "B")
            ),
            activeTabId = 2L
        )

        val restored = repository.restore()

        assertEquals(2L, restored?.activeTabId)
        assertEquals(2, restored?.tabs?.size)
        assertEquals("https://b.example.com", restored?.tabs?.last()?.url)
    }

    /**
     * 测试函数 `saveIgnoresTabsWithoutUrls`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `save Ignores Tabs Without Urls` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun saveIgnoresTabsWithoutUrls() {
        val store = InMemoryPreferenceStore()
        val repository = BrowserTabSessionRepository(store)

        repository.save(
            tabs = listOf(BrowserTab(id = 1L), BrowserTab(id = 2L, url = "https://example.com")),
            activeTabId = 1L
        )

        val restored = repository.restore()

        assertEquals(2L, restored?.activeTabId)
        assertEquals(1, restored?.tabs?.size)
        assertEquals("https://example.com", restored?.tabs?.single()?.url)
    }

    /**
     * 测试函数 `saveIgnoresTabsWithNonWebUrls`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `save Ignores Tabs With Non Web Urls` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun saveIgnoresTabsWithNonWebUrls() {
        val store = InMemoryPreferenceStore()
        val repository = BrowserTabSessionRepository(store)

        repository.save(
            tabs = listOf(
                BrowserTab(id = 1L, url = "javascript:alert(1)", title = "Script"),
                BrowserTab(id = 2L, url = "file:///sdcard/page.html", title = "File"),
                BrowserTab(id = 3L, url = "about:blank", title = "About"),
                BrowserTab(id = 4L, url = "https:/missing-host", title = "Broken"),
                BrowserTab(id = 5L, url = " https://example.com/page ", title = " Example ")
            ),
            activeTabId = 1L
        )

        val restored = repository.restore()

        assertEquals(5L, restored?.activeTabId)
        assertEquals(listOf("https://example.com/page"), restored?.tabs?.map { tab -> tab.url })
        assertEquals("Example", restored?.tabs?.single()?.title)
    }

    /**
     * 测试函数 `restoreDropsStoredNonWebUrls`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `restore Drops Stored Non Web Urls` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun restoreDropsStoredNonWebUrls() {
        val store = InMemoryPreferenceStore()
        val repository = BrowserTabSessionRepository(store)
        store.putString(
            BrowserTabSessionRepository.KEY_STANDARD_TAB_SESSION,
            listOf(
                "1",
                "1\t100\tjavascript%3Aalert%281%29\tScript",
                "2\t100\thttps%3A%2F%2Fexample.com\tExample"
            ).joinToString(separator = "\n")
        )

        val restored = repository.restore()

        assertEquals(2L, restored?.activeTabId)
        assertEquals(listOf("https://example.com"), restored?.tabs?.map { tab -> tab.url })
    }

    /**
     * 测试函数 `saveClearsEmptySession`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `save Clears Empty Session` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun saveClearsEmptySession() {
        val store = InMemoryPreferenceStore()
        val repository = BrowserTabSessionRepository(store)
        store.putString(BrowserTabSessionRepository.KEY_STANDARD_TAB_SESSION, "old")

        repository.save(tabs = listOf(BrowserTab(id = 1L)), activeTabId = 1L)

        assertFalse(store.contains(BrowserTabSessionRepository.KEY_STANDARD_TAB_SESSION))
        assertNull(repository.restore())
    }

    /**
     * 测试函数 `clearRemovesSavedSession`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `clear Removes Saved Session` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun clearRemovesSavedSession() {
        val store = InMemoryPreferenceStore()
        val repository = BrowserTabSessionRepository(store)
        repository.save(listOf(BrowserTab(id = 1L, url = "https://example.com")), activeTabId = 1L)

        repository.clear()

        assertFalse(store.contains(BrowserTabSessionRepository.KEY_STANDARD_TAB_SESSION))
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
