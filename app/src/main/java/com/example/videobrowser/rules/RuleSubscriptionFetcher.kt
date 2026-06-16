package com.example.videobrowser.rules

/**
 * 初学者阅读提示：
 * 这个文件属于“规则引擎模块”。
 * 文件名 RuleSubscriptionFetcher 可以拆开理解为“Rule Subscription Fetcher”，表示它只负责页面净化链路中的一个小职责。
 * 主要职责：读取、解析、索引和匹配广告拦截规则、元素隐藏规则、参数清理规则和安全脚本规则。
 * 阅读顺序：先看数据类/策略类表达什么规则，再看控制器如何把规则接到 WebView 请求或页面脚本上。
 */
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.util.Locale

class RuleSubscriptionFetcher(
    private val networkTimeoutMs: Int = DEFAULT_NETWORK_TIMEOUT_MS,
    private val maxBytes: Int = DEFAULT_MAX_SUBSCRIPTION_BYTES,
    private val openConnection: (URL) -> HttpURLConnection = { url ->
        url.openConnection() as HttpURLConnection
    }
) {
    init {
        require(networkTimeoutMs > 0) { "Network timeout must be positive." }
        require(maxBytes > 0) { "Maximum subscription size must be positive." }
    }

    /**
     * 函数 `fetchText`：封装 `fetch Text` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun fetchText(url: String): String {
        val normalizedUrl = normalizeSubscriptionUrl(url)
            ?: error("Rule subscription URL must be HTTP/HTTPS with a host.")
        val connection = openConnection(URL(normalizedUrl)).apply {
            connectTimeout = networkTimeoutMs
            readTimeout = networkTimeoutMs
            instanceFollowRedirects = true
            requestMethod = "GET"
        }
        return try {
            val statusCode = connection.responseCode
            if (normalizeSubscriptionUrl(connection.url.toString()) == null) {
                error("Rule subscription redirect must stay on HTTP/HTTPS with a host.")
            }
            if (statusCode !in 200..299) {
                error("HTTP $statusCode")
            }
            val contentLength = connection.contentLengthLong
            if (contentLength > maxBytes) {
                error("Rule subscription exceeds $maxBytes bytes.")
            }
            connection.inputStream.use { input -> readTextWithByteLimit(input, maxBytes) }
        } finally {
            connection.disconnect()
        }
    }

    /**
     * 函数 `readTextWithByteLimit`：封装 `read Text With Byte Limit` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param input 参数类型为 `InputStream`，表示函数执行 `input` 相关逻辑时需要读取或处理的输入。
     * @param maxBytes 参数类型为 `Int`，表示函数执行 `maxBytes` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun readTextWithByteLimit(input: InputStream, maxBytes: Int): String {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(BUFFER_SIZE)
        var totalBytes = 0
        while (true) {
            val read = input.read(buffer)
            if (read == -1) {
                break
            }
            if (totalBytes + read > maxBytes) {
                error("Rule subscription exceeds $maxBytes bytes.")
            }
            output.write(buffer, 0, read)
            totalBytes += read
        }
        return output.toByteArray().toString(Charsets.UTF_8)
    }

    companion object {
        const val DEFAULT_NETWORK_TIMEOUT_MS = 15_000
        const val DEFAULT_MAX_SUBSCRIPTION_BYTES = 2_000_000

        private const val BUFFER_SIZE = 8 * 1024

        /**
         * 函数 `normalizeSubscriptionUrl`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        fun normalizeSubscriptionUrl(url: String): String? {
            val normalizedUrl = url.trim().takeIf { it.isNotBlank() } ?: return null
            val uri = runCatching { URI(normalizedUrl) }.getOrNull() ?: return null
            val scheme = uri.scheme?.lowercase(Locale.ROOT) ?: return null
            if (scheme != "http" && scheme != "https") {
                return null
            }
            if (uri.host.isNullOrBlank()) {
                return null
            }
            if (!uri.userInfo.isNullOrBlank()) {
                return null
            }
            return normalizedUrl
        }

        /**
         * 函数 `subscriptionIdForUrl`：封装 `subscription Id For Url` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        fun subscriptionIdForUrl(url: String): String {
            val normalizedUrl = normalizeSubscriptionUrl(url) ?: return "remote"
            return runCatching { URI(normalizedUrl).host }
                .getOrNull()
                ?.takeIf { host -> host.isNotBlank() }
                ?: "remote"
        }
    }
}
