package com.example.videobrowser.storage

import com.example.videobrowser.testutil.InMemoryPreferenceStore

/**
 * 测试阅读提示：
 * 这个测试文件验证“Saved Page Repository Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SavedPageRepositoryTest {
    /**
     * 测试函数 `addBookmarkPersistsCreatedAndUpdatedTime`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `add Bookmark Persists Created And Updated Time` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `addBookmarkAndHistoryRejectNonWebUrls`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `add Bookmark And History Reject Non Web Urls` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun addBookmarkAndHistoryRejectNonWebUrls() {
        val repository = SavedPageRepository(InMemoryPreferenceStore())

        repository.addBookmark(SavedPage(title = "Script", url = "javascript:alert(1)"))
        repository.addBookmark(SavedPage(title = "File", url = "file:///sdcard/page.html"))
        repository.addBookmark(SavedPage(title = "About", url = "about:blank"))
        repository.addBookmark(SavedPage(title = "Broken", url = "https:/missing-host"))
        repository.addHistory(SavedPage(title = "Script", url = "javascript:alert(1)"))
        repository.addHistory(SavedPage(title = "File", url = "file:///sdcard/page.html"))
        repository.addHistory(SavedPage(title = "About", url = "about:blank"))
        repository.addHistory(SavedPage(title = "Broken", url = "https:/missing-host"))

        assertTrue(repository.bookmarks().isEmpty())
        assertTrue(repository.history().isEmpty())
    }

    /**
     * 测试函数 `addHistoryMovesDuplicateUrlToFrontAndKeepsCreatedTime`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `add History Moves Duplicate Url To Front And Keeps Created Time` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `loadLegacyJsonPagesKeepsExistingDataReadable`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `load Legacy Json Pages Keeps Existing Data Readable` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun loadLegacyJsonPagesKeepsExistingDataReadable() {
        val store = InMemoryPreferenceStore()
        store.putString(
            SavedPageRepository.SavedPageCollection.HISTORY.key,
            """[
                {"title":"Old \"Title\"","url":"https:\/\/example.com\/old"},
                {"title":"Script","url":"javascript:alert(1)"}
            ]""".trimIndent()
        )
        val repository = SavedPageRepository(store, currentTimeMillis = { 5_000L })

        val page = repository.history().single()

        assertEquals("Old \"Title\"", page.title)
        assertEquals("https://example.com/old", page.url)
        assertEquals(5_000L, page.createdAtMillis)
        assertEquals(5_000L, page.updatedAtMillis)
    }

    /**
     * 测试函数 `clearAllRemovesBookmarksAndHistory`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `clear All Removes Bookmarks And History` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `clearHistoryUpdatedSinceRemovesOnlyMatchingHistory`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `clear History Updated Since Removes Only Matching History` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `exportAndImportBookmarksUseSavedPageFormatAndSkipDuplicates`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `export And Import Bookmarks Use Saved Page Format And Skip Duplicates` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `importBookmarksSkipsNonWebUrls`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `import Bookmarks Skips Non Web Urls` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun importBookmarksSkipsNonWebUrls() {
        val repository = SavedPageRepository(InMemoryPreferenceStore(), currentTimeMillis = { 5_000L })
        val payload = listOf(
            "VideoBrowserSavedPages\t3",
            "1000\t2000\tSafe\thttps%3A%2F%2Fsafe.example.com\t",
            "1000\t2000\tScript\tjavascript%3Aalert%281%29\t",
            "1000\t2000\tFile\tfile%3A%2F%2F%2Fsdcard%2Fpage.html\t",
            "1000\t2000\tBroken\thttps%3A%2Fmissing-host\t"
        ).joinToString(separator = "\n")

        val result = repository.importBookmarks(payload)

        assertEquals(1, result.importedCount)
        assertEquals(listOf("https://safe.example.com"), repository.bookmarks().map { page -> page.url })
    }

    /**
     * 测试函数 `importBookmarksRejectsUnknownFormat`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `import Bookmarks Rejects Unknown Format` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun importBookmarksRejectsUnknownFormat() {
        val repository = SavedPageRepository(InMemoryPreferenceStore())

        val result = repository.importBookmarks("not a bookmark file")

        assertEquals(0, result.importedCount)
        assertEquals(0, result.skippedCount)
        assertTrue(repository.bookmarks().isEmpty())
    }

    /**
     * 测试函数 `bookmarkFoldersCanBeUpdatedAndListed`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `bookmark Folders Can Be Updated And Listed` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `versionedBookmarkImportKeepsFoldersAndReadsOlderRows`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `versioned Bookmark Import Keeps Folders And Reads Older Rows` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `updateTitleChangesOnePageAndRefreshesUpdatedTime`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `update Title Changes One Page And Refreshes Updated Time` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `updateTitleRejectsBlankOrMissingPages`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `update Title Rejects Blank Or Missing Pages` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `historyKeepsMostRecentThousandEntries`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `history Keeps Most Recent Thousand Entries` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

}
