package com.example.videobrowser.video

/**
 * 测试阅读提示：
 * 这个测试文件验证“Playback History Repository Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import com.example.videobrowser.storage.PreferenceStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackHistoryRepositoryTest {
    /**
     * 测试函数 `progressIsPersistedAndReplacedByMediaIdentity`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `progress Is Persisted And Replaced By Media Identity` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun progressIsPersistedAndReplacedByMediaIdentity() {
        val store = InMemoryPreferenceStore()
        val repository = PlaybackHistoryRepository(store)

        repository.save(
            PlaybackProgress(
                mediaIdentity = "https://cdn.example.com/movie.mp4",
                positionMs = 10_000L,
                durationMs = 60_000L,
                speed = 1.25f,
                updatedAtMillis = 100L
            )
        )
        repository.save(
            PlaybackProgress(
                mediaIdentity = "https://cdn.example.com/movie.mp4",
                positionMs = 20_000L,
                durationMs = 60_000L,
                speed = 1.5f,
                updatedAtMillis = 200L
            )
        )

        val reloaded = PlaybackHistoryRepository(store)

        assertEquals(
            PlaybackProgress(
                mediaIdentity = "https://cdn.example.com/movie.mp4",
                positionMs = 20_000L,
                durationMs = 60_000L,
                speed = 1.5f,
                updatedAtMillis = 200L
            ),
            reloaded.progressFor("https://cdn.example.com/movie.mp4")
        )
        assertEquals(20_000L, reloaded.resumePositionFor("https://cdn.example.com/movie.mp4"))
    }

    /**
     * 测试函数 `resumePositionIsSkippedNearKnownEnd`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `resume Position Is Skipped Near Known End` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun resumePositionIsSkippedNearKnownEnd() {
        val repository = PlaybackHistoryRepository(InMemoryPreferenceStore())
        repository.save(
            PlaybackProgress(
                mediaIdentity = "content://media/video/42",
                positionMs = 96_000L,
                durationMs = 100_000L,
                speed = 1f,
                updatedAtMillis = 300L
            )
        )

        assertNull(repository.resumePositionFor("content://media/video/42"))
    }

    /**
     * 测试函数 `privateBrowsingDoesNotPersistProgress`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `private Browsing Does Not Persist Progress` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun privateBrowsingDoesNotPersistProgress() {
        val repository = PlaybackHistoryRepository(InMemoryPreferenceStore())

        repository.save(
            PlaybackProgress(
                mediaIdentity = "https://private.example.com/clip.mp4",
                positionMs = 12_000L,
                durationMs = 40_000L,
                speed = 1f,
                updatedAtMillis = 400L
            ),
            privateBrowsing = true
        )

        assertNull(repository.progressFor("https://private.example.com/clip.mp4"))
    }

    /**
     * 测试函数 `recordsKeepMostRecentHundredEntries`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `records Keep Most Recent Hundred Entries` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun recordsKeepMostRecentHundredEntries() {
        val repository = PlaybackHistoryRepository(InMemoryPreferenceStore())

        (1..101).forEach { index ->
            repository.save(
                PlaybackProgress(
                    mediaIdentity = "media-$index",
                    positionMs = index * 1_000L,
                    durationMs = 200_000L,
                    speed = 1f,
                    updatedAtMillis = index.toLong()
                )
            )
        }

        val records = repository.records()

        assertEquals(100, records.size)
        assertEquals("media-101", records.first().mediaIdentity)
        assertEquals("media-2", records.last().mediaIdentity)
        assertNull(repository.progressFor("media-1"))
    }

    /**
     * 测试函数 `corruptStorageIsIgnored`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `corrupt Storage Is Ignored` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun corruptStorageIsIgnored() {
        val store = InMemoryPreferenceStore()
        store.putString("playback_history", "{not-json")

        assertTrue(PlaybackHistoryRepository(store).records().isEmpty())
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
