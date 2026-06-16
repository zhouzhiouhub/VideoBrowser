package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 HistoryRecordPolicy 可以拆开理解为“History Record Policy”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：封装 WebView 页面加载、标签页、导航安全、页面工具、权限回调或浏览器控件状态。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
import java.net.URI
import java.util.Locale

class HistoryRecordPolicy(
    private val homeUrls: () -> List<String>
) {
    /**
     * 函数 `shouldRecord`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun shouldRecord(url: String?): Boolean {
        val currentUrl = WebUrl.from(url) ?: return false
        return homeUrls()
            .mapNotNull(WebUrl::from)
            .none { homeUrl -> homeUrl.isSamePageAs(currentUrl) }
    }

    private data class WebUrl(
        val scheme: String,
        val host: String,
        val port: Int,
        val path: String
    ) {
        /**
         * 函数 `isSamePageAs`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param other 参数类型为 `WebUrl`，表示函数执行 `other` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        fun isSamePageAs(other: WebUrl): Boolean {
            return scheme == other.scheme &&
                host == other.host &&
                port == other.port &&
                path == other.path
        }

        companion object {
            /**
             * 函数 `from`：封装 `from` 这一段业务步骤，让调用方不用关心内部实现细节。
             *
             * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
             * @param value 参数类型为 `String?`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
             * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
             */
            fun from(value: String?): WebUrl? {
                val uri = try {
                    URI(value?.trim().orEmpty())
                } catch (_: IllegalArgumentException) {
                    return null
                }
                val scheme = uri.scheme?.lowercase(Locale.US) ?: return null
                if (scheme != "http" && scheme != "https") {
                    return null
                }
                val host = uri.host
                    ?.trimEnd('.')
                    ?.lowercase(Locale.US)
                    ?.takeIf { it.isNotBlank() }
                    ?: return null
                return WebUrl(
                    scheme = scheme,
                    host = host,
                    port = normalizedPort(scheme, uri.port),
                    path = uri.rawPath.orEmpty().trim('/')
                )
            }

            /**
             * 函数 `normalizedPort`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
             *
             * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
             * @param scheme 参数类型为 `String`，表示函数执行 `scheme` 相关逻辑时需要读取或处理的输入。
             * @param port 参数类型为 `Int`，表示函数执行 `port` 相关逻辑时需要读取或处理的输入。
             * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
             */
            private fun normalizedPort(scheme: String, port: Int): Int {
                if (port >= 0) {
                    return port
                }
                return when (scheme) {
                    "http" -> 80
                    "https" -> 443
                    else -> -1
                }
            }
        }
    }
}
