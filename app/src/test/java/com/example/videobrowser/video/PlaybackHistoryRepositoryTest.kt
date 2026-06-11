package com.example.videobrowser.video

import com.example.videobrowser.storage.PreferenceStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackHistoryRepositoryTest {
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

    @Test
    fun corruptStorageIsIgnored() {
        val store = InMemoryPreferenceStore()
        store.putString("playback_history", "{not-json")

        assertTrue(PlaybackHistoryRepository(store).records().isEmpty())
    }

    private class InMemoryPreferenceStore : PreferenceStore {
        private val values = mutableMapOf<String, Any>()

        override fun contains(key: String): Boolean {
            return values.containsKey(key)
        }

        override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
            return values[key] as? Boolean ?: defaultValue
        }

        override fun putBoolean(key: String, value: Boolean) {
            values[key] = value
        }

        override fun getFloat(key: String, defaultValue: Float): Float {
            return values[key] as? Float ?: defaultValue
        }

        override fun putFloat(key: String, value: Float) {
            values[key] = value
        }

        override fun getString(key: String, defaultValue: String?): String? {
            return values[key] as? String ?: defaultValue
        }

        override fun putString(key: String, value: String) {
            values[key] = value
        }

        override fun remove(key: String) {
            values.remove(key)
        }

        override fun remove(keys: Iterable<String>, commit: Boolean): Boolean {
            keys.forEach { key -> values.remove(key) }
            return true
        }
    }
}
