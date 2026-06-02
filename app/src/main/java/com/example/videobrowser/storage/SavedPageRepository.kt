package com.example.videobrowser.storage

import org.json.JSONArray
import org.json.JSONObject

data class SavedPage(
    val title: String,
    val url: String
)

class SavedPageRepository(
    private val preferenceStore: PreferenceStore
) {
    fun addBookmark(page: SavedPage) {
        addSavedPage(KEY_BOOKMARKS, page, BOOKMARK_LIMIT)
    }

    fun removeBookmark(url: String) {
        removeSavedPage(KEY_BOOKMARKS, url)
    }

    fun isBookmarked(url: String): Boolean {
        return isSavedPage(KEY_BOOKMARKS, url)
    }

    fun addHistory(page: SavedPage) {
        addSavedPage(KEY_HISTORY, page, HISTORY_LIMIT)
    }

    fun bookmarks(): List<SavedPage> {
        return loadSavedPages(KEY_BOOKMARKS)
    }

    fun history(): List<SavedPage> {
        return loadSavedPages(KEY_HISTORY)
    }

    fun clearHistory() {
        preferenceStore.remove(KEY_HISTORY)
    }

    fun clear(collection: SavedPageCollection) {
        preferenceStore.remove(collection.key)
    }

    fun pages(collection: SavedPageCollection): List<SavedPage> {
        return loadSavedPages(collection.key)
    }

    private fun addSavedPage(key: String, page: SavedPage, limit: Int) {
        val pages = loadSavedPages(key)
            .filterNot { it.url.equals(page.url, ignoreCase = true) }
            .toMutableList()
        pages.add(0, page)
        saveSavedPages(key, pages.take(limit))
    }

    private fun removeSavedPage(key: String, url: String) {
        val pages = loadSavedPages(key)
            .filterNot { it.url.equals(url, ignoreCase = true) }
        saveSavedPages(key, pages)
    }

    private fun isSavedPage(key: String, url: String): Boolean {
        return loadSavedPages(key).any { it.url.equals(url, ignoreCase = true) }
    }

    private fun loadSavedPages(key: String): List<SavedPage> {
        val rawValue = preferenceStore.getString(key, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(rawValue)
            List(array.length()) { index ->
                val item = array.getJSONObject(index)
                SavedPage(
                    title = item.optString(JSON_TITLE),
                    url = item.optString(JSON_URL)
                )
            }.filter { it.url.isNotBlank() }
        }.getOrDefault(emptyList())
    }

    private fun saveSavedPages(key: String, pages: List<SavedPage>) {
        val array = JSONArray()
        pages.forEach { page ->
            array.put(
                JSONObject()
                    .put(JSON_TITLE, page.title)
                    .put(JSON_URL, page.url)
            )
        }
        preferenceStore.putString(key, array.toString())
    }

    enum class SavedPageCollection(val key: String) {
        BOOKMARKS(KEY_BOOKMARKS),
        HISTORY(KEY_HISTORY)
    }

    private companion object {
        private const val KEY_BOOKMARKS = "bookmarks"
        private const val KEY_HISTORY = "history"
        private const val JSON_TITLE = "title"
        private const val JSON_URL = "url"
        private const val BOOKMARK_LIMIT = 100
        private const val HISTORY_LIMIT = 80
    }
}
