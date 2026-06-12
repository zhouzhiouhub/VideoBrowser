package com.example.videobrowser.browser.search

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SearchSuggestionClient(
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
) {
    fun fetch(
        provider: SearchProvider,
        query: String,
        onResult: (List<String>) -> Unit
    ) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            onResult(emptyList())
            return
        }

        executor.execute {
            val suggestions = runCatching {
                fetchSuggestions(provider, trimmedQuery)
            }.getOrDefault(emptyList())
            onResult(suggestions)
        }
    }

    fun dispose() {
        executor.shutdownNow()
    }

    private fun fetchSuggestions(provider: SearchProvider, query: String): List<String> {
        val endpoint = suggestionEndpoint(provider, query)
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            connectTimeout = NETWORK_TIMEOUT_MS
            readTimeout = NETWORK_TIMEOUT_MS
            requestMethod = "GET"
            setRequestProperty("User-Agent", USER_AGENT)
        }
        return try {
            if (connection.responseCode !in 200..299) {
                emptyList()
            } else {
                val charset = responseCharset(connection.contentType)
                val payload = connection.inputStream.use { stream ->
                    stream.readBytes().toString(charset)
                }
                parseSuggestions(payload)
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun suggestionEndpoint(provider: SearchProvider, query: String): String {
        val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.name())
        return when (provider.id) {
            "edge" -> "https://api.bing.com/osjson.aspx?query=$encodedQuery"
            "so" -> "https://sug.so.360.cn/suggest?word=$encodedQuery&encodein=utf-8&encodeout=utf-8"
            else -> "https://suggestion.baidu.com/su?wd=$encodedQuery&action=opensearch"
        }
    }

    companion object {
        fun parseSuggestions(payload: String): List<String> {
            val json = payload.trim().withoutJsonpWrapper()
            return parseOpenSearchSuggestions(json)
                .ifEmpty { parseSo360Suggestions(json) }
        }

        private fun parseOpenSearchSuggestions(payload: String): List<String> {
            if (!payload.startsWith("[")) {
                return emptyList()
            }
            val secondArrayStart = payload.indexOf('[', startIndex = 1)
            if (secondArrayStart < 0) {
                return emptyList()
            }
            val secondArrayEnd = matchingBracketIndex(payload, secondArrayStart)
            if (secondArrayEnd <= secondArrayStart) {
                return emptyList()
            }
            return parseQuotedStrings(payload.substring(secondArrayStart, secondArrayEnd + 1))
        }

        private fun parseSo360Suggestions(payload: String): List<String> {
            return WORD_FIELD_REGEX.findAll(payload)
                .map { match -> decodeJsonString(match.groupValues[1]).trim() }
                .filter { it.isNotEmpty() }
                .toList()
        }

        private fun parseQuotedStrings(payload: String): List<String> {
            return JSON_STRING_REGEX.findAll(payload)
                .map { match -> decodeJsonString(match.groupValues[1]).trim() }
                .filter { it.isNotEmpty() }
                .toList()
        }

        private fun matchingBracketIndex(value: String, startIndex: Int): Int {
            var depth = 0
            var inString = false
            var escaping = false
            for (index in startIndex until value.length) {
                val char = value[index]
                when {
                    escaping -> escaping = false
                    inString && char == '\\' -> escaping = true
                    inString && char == '"' -> inString = false
                    inString -> Unit
                    char == '"' -> inString = true
                    char == '[' -> depth += 1
                    char == ']' -> {
                        depth -= 1
                        if (depth == 0) {
                            return index
                        }
                    }
                }
            }
            return -1
        }

        private fun decodeJsonString(value: String): String {
            val decoded = StringBuilder(value.length)
            var index = 0
            while (index < value.length) {
                val char = value[index]
                if (char != '\\' || index + 1 >= value.length) {
                    decoded.append(char)
                    index += 1
                    continue
                }

                when (val escaped = value[index + 1]) {
                    '"', '\\', '/' -> {
                        decoded.append(escaped)
                        index += 2
                    }
                    'b' -> {
                        decoded.append('\b')
                        index += 2
                    }
                    'f' -> {
                        decoded.append('\u000C')
                        index += 2
                    }
                    'n' -> {
                        decoded.append('\n')
                        index += 2
                    }
                    'r' -> {
                        decoded.append('\r')
                        index += 2
                    }
                    't' -> {
                        decoded.append('\t')
                        index += 2
                    }
                    'u' -> {
                        val hex = value.substring(index + 2, (index + 6).coerceAtMost(value.length))
                        val decodedChar = hex
                            .takeIf { it.length == 4 }
                            ?.toIntOrNull(16)
                            ?.toChar()
                        if (decodedChar == null) {
                            decoded.append("\\u")
                            index += 2
                        } else {
                            decoded.append(decodedChar)
                            index += 6
                        }
                    }
                    else -> {
                        decoded.append(escaped)
                        index += 2
                    }
                }
            }
            return decoded.toString()
        }

        private fun responseCharset(contentType: String?): Charset {
            val charsetName = contentType
                ?.substringAfter("charset=", missingDelimiterValue = "")
                ?.substringBefore(";")
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
            return charsetName
                ?.let { runCatching { Charset.forName(it) }.getOrNull() }
                ?: StandardCharsets.UTF_8
        }

        private fun String.withoutJsonpWrapper(): String {
            if (startsWith("{") || startsWith("[")) {
                return this
            }
            val start = indexOf('(')
            val end = lastIndexOf(')')
            return if (start >= 0 && end > start) {
                substring(start + 1, end).trim()
            } else {
                this
            }
        }

        private const val NETWORK_TIMEOUT_MS = 1500
        private const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android) AppleWebKit/537.36 Chrome Mobile Safari/537.36"
        private val JSON_STRING_REGEX = Regex("\"((?:\\\\.|[^\"\\\\])*)\"")
        private val WORD_FIELD_REGEX = Regex("\"word\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"")
    }
}
