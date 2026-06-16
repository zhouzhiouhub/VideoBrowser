package com.example.videobrowser.browser.search

/**
 * 初学者阅读提示：
 * 这个文件属于“搜索入口与地址建议模块”。
 * 文件名 SearchSuggestionClient 可以拆开理解为“Search Suggestion Client”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：把地址栏输入、默认搜索引擎、远程搜索建议、收藏和历史候选项整理成用户可以点击的建议列表。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
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
    /**
     * 函数 `fetch`：封装 `fetch` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param provider 参数类型为 `SearchProvider`，表示函数执行 `provider` 相关逻辑时需要读取或处理的输入。
     * @param query 参数类型为 `String`，表示函数执行 `query` 相关逻辑时需要读取或处理的输入。
     * @param onResult 参数类型为 `(List<String>) -> Unit`，表示函数执行 `onResult` 相关逻辑时需要读取或处理的输入。
     */
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

    /**
     * 函数 `dispose`：封装 `dispose` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun dispose() {
        executor.shutdownNow()
    }

    /**
     * 函数 `fetchSuggestions`：封装 `fetch Suggestions` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param provider 参数类型为 `SearchProvider`，表示函数执行 `provider` 相关逻辑时需要读取或处理的输入。
     * @param query 参数类型为 `String`，表示函数执行 `query` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `suggestionEndpoint`：封装 `suggestion Endpoint` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param provider 参数类型为 `SearchProvider`，表示函数执行 `provider` 相关逻辑时需要读取或处理的输入。
     * @param query 参数类型为 `String`，表示函数执行 `query` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun suggestionEndpoint(provider: SearchProvider, query: String): String {
        val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.name())
        return when (provider.id) {
            "edge" -> "https://api.bing.com/osjson.aspx?query=$encodedQuery"
            "so" -> "https://sug.so.360.cn/suggest?word=$encodedQuery&encodein=utf-8&encodeout=utf-8"
            else -> "https://suggestion.baidu.com/su?wd=$encodedQuery&action=opensearch"
        }
    }

    companion object {
        /**
         * 函数 `parseSuggestions`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param payload 参数类型为 `String`，表示函数执行 `payload` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        fun parseSuggestions(payload: String): List<String> {
            val json = payload.trim().withoutJsonpWrapper()
            return parseOpenSearchSuggestions(json)
                .ifEmpty { parseSo360Suggestions(json) }
        }

        /**
         * 函数 `parseOpenSearchSuggestions`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param payload 参数类型为 `String`，表示函数执行 `payload` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
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

        /**
         * 函数 `parseSo360Suggestions`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param payload 参数类型为 `String`，表示函数执行 `payload` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        private fun parseSo360Suggestions(payload: String): List<String> {
            return WORD_FIELD_REGEX.findAll(payload)
                .map { match -> decodeJsonString(match.groupValues[1]).trim() }
                .filter { it.isNotEmpty() }
                .toList()
        }

        /**
         * 函数 `parseQuotedStrings`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param payload 参数类型为 `String`，表示函数执行 `payload` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        private fun parseQuotedStrings(payload: String): List<String> {
            return JSON_STRING_REGEX.findAll(payload)
                .map { match -> decodeJsonString(match.groupValues[1]).trim() }
                .filter { it.isNotEmpty() }
                .toList()
        }

        /**
         * 函数 `matchingBracketIndex`：封装 `matching Bracket Index` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param value 参数类型为 `String`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
         * @param startIndex 参数类型为 `Int`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
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

        /**
         * 函数 `decodeJsonString`：封装 `decode Json String` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param value 参数类型为 `String`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
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

        /**
         * 函数 `responseCharset`：封装 `response Charset` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param contentType 参数类型为 `String?`，表示函数执行 `contentType` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
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

        /**
         * 函数 `withoutJsonpWrapper`：封装 `without Jsonp Wrapper` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
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
