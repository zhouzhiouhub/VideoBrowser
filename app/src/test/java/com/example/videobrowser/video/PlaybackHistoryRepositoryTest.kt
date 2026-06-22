package com.example.videobrowser.video

/**
 * 测试阅读提示：
 * 这个测试文件验证“Playback History Repository Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import com.example.videobrowser.testutil.InMemoryPreferenceStore
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

    @Test
    fun webPageProgressPersistsTitleAndSource() {
        val repository = PlaybackHistoryRepository(InMemoryPreferenceStore())

        repository.save(
            PlaybackProgress(
                mediaIdentity = "https://video.example.com/watch/1",
                positionMs = 15_000L,
                durationMs = 90_000L,
                speed = 1f,
                updatedAtMillis = 500L,
                title = " Episode 1 ",
                source = PlaybackHistorySource.WEB_PAGE
            )
        )

        assertEquals(
            PlaybackProgress(
                mediaIdentity = "https://video.example.com/watch/1",
                positionMs = 15_000L,
                durationMs = 90_000L,
                speed = 1f,
                updatedAtMillis = 500L,
                title = "Episode 1",
                source = PlaybackHistorySource.WEB_PAGE
            ),
            repository.progressFor("https://video.example.com/watch/1")
        )
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

}
