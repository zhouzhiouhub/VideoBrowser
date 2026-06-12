package com.example.videobrowser.storage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SavedPageRepositoryTest {
    @Test
    fun addBookmarkPersistsCreatedAndUpdatedTime() {
        val store = InMemoryPreferenceStore()
        val repository = SavedPageRepository(store, currentTimeMillis = { 1_000L })

        repository.addBookmark(SavedPage(title = "Example", url = "https://example.com"))

        val bookmark = repository.bookmarks().single()
        assertEquals("Example", bookmark.title)
        assertEquals("https://example.com", bookmark.url)
        assertEquals(1_000L, bookmark.createdAtMillis)
        assertEquals(1_000L, bookmark.updatedAtMillis)
    }

    @Test
    fun addHistoryMovesDuplicateUrlToFrontAndKeepsCreatedTime() {
        var now = 1_000L
        val repository = SavedPageRepository(InMemoryPreferenceStore()) { now }

        repository.addHistory(SavedPage(title = "First", url = "https://example.com"))
        now = 2_000L
        repository.addHistory(SavedPage(title = "Second", url = "https://other.example.com"))
        now = 3_000L
        repository.addHistory(SavedPage(title = "First Again", url = "https://example.com"))

        val history = repository.history()
        assertEquals(listOf("https://example.com", "https://other.example.com"), history.map { it.url })
        assertEquals("First Again", history.first().title)
        assertEquals(1_000L, history.first().createdAtMillis)
        assertEquals(3_000L, history.first().updatedAtMillis)
    }

    @Test
    fun loadLegacyJsonPagesKeepsExistingDataReadable() {
        val store = InMemoryPreferenceStore()
        store.putString(
            SavedPageRepository.SavedPageCollection.HISTORY.key,
            """[{"title":"Old \"Title\"","url":"https:\/\/example.com\/old"}]"""
        )
        val repository = SavedPageRepository(store, currentTimeMillis = { 5_000L })

        val page = repository.history().single()

        assertEquals("Old \"Title\"", page.title)
        assertEquals("https://example.com/old", page.url)
        assertEquals(5_000L, page.createdAtMillis)
        assertEquals(5_000L, page.updatedAtMillis)
    }

    @Test
    fun clearAllRemovesBookmarksAndHistory() {
        val store = InMemoryPreferenceStore()
        val repository = SavedPageRepository(store, currentTimeMillis = { 1_000L })
        repository.addBookmark(SavedPage(title = "Bookmark", url = "https://bookmark.example.com"))
        repository.addHistory(SavedPage(title = "History", url = "https://history.example.com"))

        repository.clearAll()

        assertTrue(repository.bookmarks().isEmpty())
        assertTrue(repository.history().isEmpty())
        assertFalse(store.contains(SavedPageRepository.SavedPageCollection.BOOKMARKS.key))
        assertFalse(store.contains(SavedPageRepository.SavedPageCollection.HISTORY.key))
    }

    @Test
    fun historyKeepsMostRecentThousandEntries() {
        var now = 0L
        val repository = SavedPageRepository(InMemoryPreferenceStore()) { ++now }

        repeat(1_005) { index ->
            repository.addHistory(
                SavedPage(
                    title = "Page $index",
                    url = "https://example.com/$index"
                )
            )
        }

        val history = repository.history()
        assertEquals(1_000, history.size)
        assertEquals("https://example.com/1004", history.first().url)
        assertEquals("https://example.com/5", history.last().url)
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
