package com.example.videobrowser.storage

import com.example.videobrowser.utils.TextWhitespaceNormalizer
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.Locale

internal class SavedPageCodec(
    private val currentTimeMillis: () -> Long
) {
    fun parse(rawValue: String): List<SavedPage> {
        return when {
            rawValue.startsWith(FORMAT_HEADER_PREFIX) -> loadVersionedPages(rawValue)
            rawValue.trimStart().startsWith("[") -> loadLegacyJsonPages(rawValue)
            else -> emptyList()
        }
    }

    fun render(pages: List<SavedPage>): String {
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
                    .append('\t')
                    .append(encode(page.folder))
                    .append('\n')
            }
        }
    }

    fun normalizePageForSave(page: SavedPage, existingPage: SavedPage?): SavedPage {
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
            updatedAtMillis = updatedAt,
            folder = normalizeBookmarkFolder(page.folder).orEmpty()
        )
    }

    fun normalizeImportedBookmark(page: SavedPage): SavedPage? {
        val url = normalizeSavedWebUrl(page.url) ?: return null
        val timestamp = currentTimeMillis()
        return page.copy(
            title = page.title.trim().ifBlank { url },
            url = url,
            createdAtMillis = page.createdAtMillis.takeIf { it > 0L } ?: timestamp,
            updatedAtMillis = page.updatedAtMillis.takeIf { it > 0L } ?: timestamp,
            folder = normalizeBookmarkFolder(page.folder).orEmpty()
        )
    }

    fun bookmarkUrlKey(url: String): String {
        return url.trim().lowercase(Locale.ROOT)
    }

    fun normalizeBookmarkFolder(folder: String): String? {
        val normalized = TextWhitespaceNormalizer.collapse(folder)
        if (normalized.isEmpty()) {
            return null
        }
        if (normalized.length > MAX_BOOKMARK_FOLDER_LENGTH) {
            return null
        }
        if (normalized.any { char -> char == '\t' || char == '\n' || char == '\r' }) {
            return null
        }
        return normalized
    }

    fun normalizeSavedWebUrl(url: String): String? {
        val normalizedUrl = url.trim().takeIf { it.isNotBlank() } ?: return null
        val uri = runCatching { URI(normalizedUrl) }.getOrNull() ?: return null
        val scheme = uri.scheme?.lowercase(Locale.ROOT) ?: return null
        if (scheme != "http" && scheme != "https") {
            return null
        }
        if (uri.host.isNullOrBlank()) {
            return null
        }
        return normalizedUrl
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
        if (parts.size != 4 && parts.size != 5) {
            return null
        }
        val createdAt = parts[0].toLongOrNull() ?: 0L
        val updatedAt = parts[1].toLongOrNull() ?: createdAt
        val title = decode(parts[2]) ?: return null
        val url = decode(parts[3])?.takeIf { it.isNotBlank() } ?: return null
        val normalizedUrl = normalizeSavedWebUrl(url) ?: return null
        val folder = parts.getOrNull(4)?.let(::decode)?.let(::normalizeBookmarkFolder).orEmpty()
        return SavedPage(
            title = title,
            url = normalizedUrl,
            createdAtMillis = createdAt,
            updatedAtMillis = updatedAt,
            folder = folder
        )
    }

    private fun loadLegacyJsonPages(rawValue: String): List<SavedPage> {
        val timestamp = currentTimeMillis()
        return LEGACY_OBJECT_REGEX.findAll(rawValue)
            .mapNotNull { match ->
                val objectText = match.value
                val url = legacyJsonStringValue(objectText, JSON_URL)
                    ?.let(::normalizeSavedWebUrl)
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

    private companion object {
        private const val FORMAT_HEADER = "VideoBrowserSavedPages\t3"
        private const val FORMAT_HEADER_PREFIX = "VideoBrowserSavedPages\t"
        private const val JSON_TITLE = "title"
        private const val JSON_URL = "url"
        private const val CHARSET_NAME = "UTF-8"
        private const val MAX_BOOKMARK_FOLDER_LENGTH = 60
        private val LEGACY_OBJECT_REGEX = Regex("\\{[^{}]*\\}")
    }
}
