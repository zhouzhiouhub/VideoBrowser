package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 HttpNavigationSafetyPolicy 可以拆开理解为“Http Navigation Safety Policy”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：封装 WebView 页面加载、标签页、导航安全、页面工具、权限回调或浏览器控件状态。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
import java.net.URI
import java.util.Locale

object HttpNavigationSafetyPolicy {
    /**
     * 函数 `requiresInsecureNavigationConfirmation`：封装 `requires Insecure Navigation Confirmation` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param pageUrl 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param targetUrl 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun requiresInsecureNavigationConfirmation(pageUrl: String?, targetUrl: String): Boolean {
        return schemeOf(pageUrl) == "https" && isHttpNetworkUrl(targetUrl)
    }

    /**
     * 函数 `isHttpNetworkUrl`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun isHttpNetworkUrl(url: String): Boolean {
        val uri = uriOf(url) ?: return false
        return uri.scheme?.lowercase(Locale.ROOT) == "http" && !uri.host.isNullOrBlank()
    }

    /**
     * 函数 `schemeOf`：封装 `scheme Of` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun schemeOf(url: String?): String? {
        return uriOf(url)
            ?.scheme
            ?.lowercase(Locale.ROOT)
    }

    /**
     * 函数 `uriOf`：封装 `uri Of` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun uriOf(url: String?): URI? {
        return runCatching { URI(url?.trim().orEmpty()) }.getOrNull()
    }
}
