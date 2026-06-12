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

        repository.addBookmark(
            SavedPage(
                title = "Example",
                url = "https://example.com",
                folder = "视频"
            )
        )

        val bookmark = repository.bookmarks().single()
        assertEquals("Example", bookmark.title)
        assertEquals("https://example.com", bookmark.url)
        assertEquals(1_000L, bookmark.createdAtMillis)
        assertEquals(1_000L, bookmark.updatedAtMillis)
        assertEquals("视频", bookmark.folder)
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
    fun clearHistoryUpdatedSinceRemovesOnlyMatchingHistory() {
        val store = InMemoryPreferenceStore()
        var now = 1_000L
        val repository = SavedPageRepository(store, currentTimeMillis = { now })

        repository.addHistory(SavedPage(title = "Old", url = "https://old.example.com"))
        now = 5_000L
        repository.addHistory(SavedPage(title = "Cutoff", url = "https://cutoff.example.com"))
        now = 9_000L
        repository.addHistory(SavedPage(title = "Recent", url = "https://recent.example.com"))

        assertEquals(2, repository.clearHistoryUpdatedSince(5_000L))

        val remainingHistory = repository.history()
        assertEquals(listOf("https://old.example.com"), remainingHistory.map { page -> page.url })
        assertTrue(store.contains(SavedPageRepository.SavedPageCollection.HISTORY.key))

        assertEquals(1, repository.clearHistoryUpdatedSince(0L))
        assertTrue(repository.history().isEmpty())
        assertFalse(store.contains(SavedPageRepository.SavedPageCollection.HISTORY.key))
    }

    @Test
    fun exportAndImportBookmarksUseSavedPageFormatAndSkipDuplicates() {
        var now = 1_000L
        val source = SavedPageRepository(InMemoryPreferenceStore(), currentTimeMillis = { now })
        source.addBookmark(SavedPage(title = "First", url = "https://first.example.com"))
        now = 2_000L
        source.addBookmark(SavedPage(title = "Second", url = "https://second.example.com"))

        val target = SavedPageRepository(InMemoryPreferenceStore(), currentTimeMillis = { 5_000L })
        target.addBookmark(SavedPage(title = "Existing", url = "https://second.example.com"))

        val result = target.importBookmarks(source.exportBookmarks())

        assertEquals(1, result.importedCount)
        assertEquals(1, result.skippedCount)
        assertEquals(
            listOf("https://second.example.com", "https://first.example.com"),
            target.bookmarks().map { page -> page.url }
        )
        assertTrue(target.exportBookmarks().startsWith("VideoBrowserSavedPages\t3"))
    }

    @Test
    fun importBookmarksRejectsUnknownFormat() {
        val repository = SavedPageRepository(InMemoryPreferenceStore())

        val result = repository.importBookmarks("not a bookmark file")

        assertEquals(0, result.importedCount)
        assertEquals(0, result.skippedCount)
        assertTrue(repository.bookmarks().isEmpty())
    }

    @Test
    fun bookmarkFoldersCanBeUpdatedAndListed() {
        var now = 1_000L
        val repository = SavedPageRepository(InMemoryPreferenceStore()) { now }
        repository.addBookmark(SavedPage(title = "Video", url = "https://video.example.com"))
        repository.addBookmark(SavedPage(title = "Docs", url = "https://docs.example.com", folder = "工作"))

        now = 2_000L
        assertTrue(repository.updateBookmarkFolder("https://video.example.com", " 视频  收藏 "))

        assertEquals(listOf("工作", "视频 收藏"), repository.bookmarkFolders())
        val video = repository.bookmarks().first { page -> page.url == "https://video.example.com" }
        assertEquals("视频 收藏", video.folder)
        assertEquals(2_000L, video.updatedAtMillis)

        assertTrue(repository.updateBookmarkFolder("https://video.example.com", " "))
        assertEquals("", repository.bookmarks().first { page -> page.url == "https://video.example.com" }.folder)
        assertFalse(repository.updateBookmarkFolder("https://missing.example.com", "视频"))
        assertFalse(repository.updateBookmarkFolder("https://docs.example.com", "x".repeat(61)))
    }

    @Test
    fun versionedBookmarkImportKeepsFoldersAndReadsOlderRows() {
        val repository = SavedPageRepository(InMemoryPreferenceStore(), currentTimeMillis = { 5_000L })
        val payload = listOf(
            "VideoBrowserSavedPages\t3",
            "1000\t2000\tVideo\thttps%3A%2F%2Fvideo.example.com\t%E8%A7%86%E9%A2%91",
            "3000\t4000\tOld\thttps%3A%2F%2Fold.example.com"
        ).joinToString(separator = "\n")

        val result = repository.importBookmarks(payload)

        assertEquals(2, result.importedCount)
        assertEquals(
            listOf("视频", ""),
            repository.bookmarks().map { page -> page.folder }
        )
    }

    @Test
    fun updateTitleChangesOnePageAndRefreshesUpdatedTime() {
        var now = 1_000L
        val repository = SavedPageRepository(InMemoryPreferenceStore()) { now }
        repository.addBookmark(SavedPage(title = "Old", url = "https://example.com"))
        now = 2_000L

        assertTrue(
            repository.updateTitle(
                SavedPageRepository.SavedPageCollection.BOOKMARKS,
                url = "https://example.com",
                title = " New title "
            )
        )

        val bookmark = repository.bookmarks().single()
        assertEquals("New title", bookmark.title)
        assertEquals(1_000L, bookmark.createdAtMillis)
        assertEquals(2_000L, bookmark.updatedAtMillis)
    }

    @Test
    fun updateTitleRejectsBlankOrMissingPages() {
        val repository = SavedPageRepository(InMemoryPreferenceStore())
        repository.addBookmark(SavedPage(title = "Old", url = "https://example.com"))

        assertFalse(
            repository.updateTitle(
                SavedPageRepository.SavedPageCollection.BOOKMARKS,
                url = "https://example.com",
                title = " "
            )
        )
        assertFalse(
            repository.updateTitle(
                SavedPageRepository.SavedPageCollection.BOOKMARKS,
                url = "https://missing.example.com",
                title = "New"
            )
        )
        assertEquals("Old", repository.bookmarks().single().title)
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
