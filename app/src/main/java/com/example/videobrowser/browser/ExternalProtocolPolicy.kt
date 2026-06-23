package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 ExternalProtocolPolicy 可以拆开理解为“External Protocol Policy”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：封装 WebView 页面加载、标签页、导航安全、页面工具、权限回调或浏览器控件状态。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
import java.util.Locale
import com.example.videobrowser.utils.SafeUriParser
import com.example.videobrowser.utils.Utf8UrlCodec
import com.example.videobrowser.utils.WebUrlNormalizer

object ExternalProtocolPolicy {
    private val blockedSchemes = setOf(
        "about",
        "blob",
        "chrome",
        "content",
        "data",
        "file",
        "http",
        "https",
        "javascript",
        "view-source"
    )

    /**
     * 函数 `shouldOpenExternally`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param scheme 参数类型为 `String?`，表示函数执行 `scheme` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun shouldOpenExternally(scheme: String?): Boolean {
        val normalizedScheme = scheme?.trim()?.lowercase(Locale.ROOT) ?: return false
        if (normalizedScheme.isEmpty()) {
            return false
        }
        return normalizedScheme !in blockedSchemes
    }

    fun shouldOpenUrlExternally(url: String?): Boolean {
        return shouldOpenExternally(SafeUriParser.scheme(url))
    }

    fun isIntentUrl(url: String?): Boolean {
        return SafeUriParser.scheme(url).equals("intent", ignoreCase = true)
    }

    /**
     * 函数 `isWebUrl`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isWebUrl(url: String?): Boolean {
        return WebUrlNormalizer.isHttpOrHttpsUrl(url)
    }

    /**
     * 函数 `fallbackUrlFromIntentUri`：封装 `fallback Url From Intent Uri` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param intentUri 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun fallbackUrlFromIntentUri(intentUri: String): String? {
        if (!intentUri.startsWith("intent:", ignoreCase = true)) {
            return null
        }
        val marker = "S.$BROWSER_FALLBACK_URL="
        val start = intentUri.indexOf(marker)
        if (start < 0) {
            return null
        }
        val valueStart = start + marker.length
        val valueEnd = intentUri.indexOf(';', valueStart).takeIf { it >= 0 } ?: intentUri.length
        val rawValue = intentUri.substring(valueStart, valueEnd)
        val decoded = Utf8UrlCodec.decodeFormComponent(rawValue.replace("+", "%2B"))
        return decoded?.takeIf(::isWebUrl)
    }

    const val BROWSER_FALLBACK_URL = "browser_fallback_url"
}
