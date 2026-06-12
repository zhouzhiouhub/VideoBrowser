package com.example.videobrowser.storage

import java.net.URLDecoder
import java.net.URLEncoder

data class SavedPage(
    val title: String,
    val url: String,
    val createdAtMillis: Long = 0L,
    val updatedAtMillis: Long = 0L
)

class SavedPageRepository(
    private val preferenceStore: PreferenceStore,
    private val currentTimeMillis: () -> Long = { System.currentTimeMillis() }
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

    fun clearHistoryUpdatedSince(cutoffMillis: Long): Int {
        val history = history()
        val remainingHistory = history.filterNot { page ->
            page.updatedAtMillis >= cutoffMillis
        }
        saveSavedPages(KEY_HISTORY, remainingHistory)
        return history.size - remainingHistory.size
    }

    fun clear(collection: SavedPageCollection) {
        preferenceStore.remove(collection.key)
    }

    fun remove(collection: SavedPageCollection, url: String): Boolean {
        return removeSavedPage(collection.key, url)
    }

    fun updateTitle(collection: SavedPageCollection, url: String, title: String): Boolean {
        val normalizedTitle = title.trim()
        if (normalizedTitle.isEmpty()) {
            return false
        }
        var updated = false
        val pages = loadSavedPages(collection.key).map { page ->
            if (page.url.equals(url, ignoreCase = true)) {
                updated = true
                page.copy(title = normalizedTitle, updatedAtMillis = currentTimeMillis())
            } else {
                page
            }
        }
        if (!updated) {
            return false
        }
        saveSavedPages(collection.key, pages)
        return true
    }

    fun clearAll() {
        SavedPageCollection.values().forEach { collection ->
            preferenceStore.remove(collection.key)
        }
    }

    fun pages(collection: SavedPageCollection): List<SavedPage> {
        return loadSavedPages(collection.key)
    }

    private fun addSavedPage(key: String, page: SavedPage, limit: Int) {
        val existingPages = loadSavedPages(key)
        val existingPage = existingPages.firstOrNull { it.url.equals(page.url, ignoreCase = true) }
        val pages = existingPages
            .filterNot { it.url.equals(page.url, ignoreCase = true) }
            .toMutableList()
        pages.add(0, normalizePageForSave(page, existingPage))
        saveSavedPages(key, pages.take(limit))
    }

    private fun removeSavedPage(key: String, url: String): Boolean {
        val pages = loadSavedPages(key)
            .filterNot { it.url.equals(url, ignoreCase = true) }
        if (pages.size == loadSavedPages(key).size) {
            return false
        }
        saveSavedPages(key, pages)
        return true
    }

    private fun isSavedPage(key: String, url: String): Boolean {
        return loadSavedPages(key).any { it.url.equals(url, ignoreCase = true) }
    }

    private fun loadSavedPages(key: String): List<SavedPage> {
        val rawValue = preferenceStore.getString(key, null) ?: return emptyList()
        return when {
            rawValue.startsWith(FORMAT_HEADER) -> loadVersionedPages(rawValue)
            rawValue.trimStart().startsWith("[") -> loadLegacyJsonPages(rawValue)
            else -> emptyList()
        }
    }

    private fun saveSavedPages(key: String, pages: List<SavedPage>) {
        if (pages.isEmpty()) {
            preferenceStore.remove(key)
            return
        }
        preferenceStore.putString(key, renderPages(pages))
    }

    private fun normalizePageForSave(page: SavedPage, existingPage: SavedPage?): SavedPage {
        val timestamp = currentTimeMillis()
        val createdAt = existingPage?.createdAtMillis
            ?.takeIf { it > 0L }
            ?: page.createdAtMillis.takeIf { it > 0L }
            ?: timestamp
        val updatedAt = page.updatedAtMillis.takeIf { it > 0L } ?: timestamp
        return page.copy(
            title = page.title.trim(),
            url = page.url.trim(),
            createdAtMillis = createdAt,
            updatedAtMillis = updatedAt
        )
    }

    private fun renderPages(pages: List<SavedPage>): String {
        return buildString {
            append(FORMAT_HEADER).append('\n')
            pages.forEach { page ->
                append(page.createdAtMillis.coerceAtLeast(0L))
                    .append('\t')
                    .append(page.updatedAtMillis.coerceAtLeast(0L))
                    .append('\t')
                    .append(encode(page.title))
                    .append('\t')
                    .append(encode(page.url))
                    .append('\n')
            }
        }
    }

    private fun loadVersionedPages(rawValue: String): List<SavedPage> {
        return rawValue
            .lineSequence()
            .drop(1)
            .mapNotNull(::parseVersionedPageLine)
            .filter { page -> page.url.isNotBlank() }
            .toList()
    }

    private fun parseVersionedPageLine(line: String): SavedPage? {
        val parts = line.split('\t')
        if (parts.size != 4) {
            return null
        }
        val createdAt = parts[0].toLongOrNull() ?: 0L
        val updatedAt = parts[1].toLongOrNull() ?: createdAt
        val title = decode(parts[2]) ?: return null
        val url = decode(parts[3])?.takeIf { it.isNotBlank() } ?: return null
        return SavedPage(
            title = title,
            url = url,
            createdAtMillis = createdAt,
            updatedAtMillis = updatedAt
        )
    }

    private fun loadLegacyJsonPages(rawValue: String): List<SavedPage> {
        val timestamp = currentTimeMillis()
        return LEGACY_OBJECT_REGEX.findAll(rawValue)
            .mapNotNull { match ->
                val objectText = match.value
                val url = legacyJsonStringValue(objectText, JSON_URL)?.takeIf { it.isNotBlank() }
                    ?: return@mapNotNull null
                SavedPage(
                    title = legacyJsonStringValue(objectText, JSON_TITLE).orEmpty(),
                    url = url,
                    createdAtMillis = timestamp,
                    updatedAtMillis = timestamp
                )
            }
            .toList()
    }

    private fun legacyJsonStringValue(objectText: String, key: String): String? {
        val match = Regex("\"${Regex.escape(key)}\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"")
            .find(objectText)
            ?: return null
        return unescapeJsonString(match.groupValues[1])
    }

    private fun unescapeJsonString(value: String): String {
        val builder = StringBuilder()
        var index = 0
        while (index < value.length) {
            val char = value[index]
            if (char != '\\' || index == value.lastIndex) {
                builder.append(char)
                index += 1
                continue
            }
            when (val escaped = value[index + 1]) {
                '"', '\\', '/' -> builder.append(escaped)
                'b' -> builder.append('\b')
                'f' -> builder.append('\u000C')
                'n' -> builder.append('\n')
                'r' -> builder.append('\r')
                't' -> builder.append('\t')
                'u' -> {
                    val hex = value.substring(index + 2, (index + 6).coerceAtMost(value.length))
                    val codePoint = hex.takeIf { it.length == 4 }?.toIntOrNull(16)
                    if (codePoint != null) {
                        builder.append(codePoint.toChar())
                        index += 4
                    }
                }
                else -> builder.append(escaped)
            }
            index += 2
        }
        return builder.toString()
    }

    private fun encode(value: String): String {
        return URLEncoder.encode(value, CHARSET_NAME)
    }

    private fun decode(value: String): String? {
        return runCatching { URLDecoder.decode(value, CHARSET_NAME) }.getOrNull()
    }

    enum class SavedPageCollection(val key: String) {
        BOOKMARKS(KEY_BOOKMARKS),
        HISTORY(KEY_HISTORY)
    }

    private companion object {
        private const val KEY_BOOKMARKS = "bookmarks"
        private const val KEY_HISTORY = "history"
        private const val FORMAT_HEADER = "VideoBrowserSavedPages\t2"
        private const val JSON_TITLE = "title"
        private const val JSON_URL = "url"
        private const val CHARSET_NAME = "UTF-8"
        private const val BOOKMARK_LIMIT = 500
        private const val HISTORY_LIMIT = 1000
        private val LEGACY_OBJECT_REGEX = Regex("\\{[^{}]*\\}")
    }
}
