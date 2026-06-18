package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器启动和地址栏入口模块”。
 * 用户在地址栏输入内容、点击主页、打开文心入口，或者系统把外部网页链接交给应用时，都会通过这里转换成 loadUrl 调用。
 * 主要职责：解析地址栏文本、拼接搜索 URL、打开配置主页、恢复首次标准标签页 URL，并过滤外部 Intent。
 * 阅读顺序：先看 loadAddressInput，再看 openInitialStandardPage，最后看 handleLaunchIntent。
 */
import android.content.Intent
import com.example.videobrowser.utils.TextWhitespaceNormalizer
import com.example.videobrowser.utils.UrlUtils
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * 浏览器启动和地址栏入口控制器。
 *
 * MainActivity 只保留给按钮、功能中心和生命周期调用的入口函数；本类负责把这些入口转换成目标 URL。
 *
 * @param addressText 参数类型为 `() -> String`，表示读取地址栏当前文本的回调。
 * @param runWithSuggestionsSuppressed 参数类型为 `((() -> Unit) -> Unit)`，表示临时关闭地址建议刷新并执行动作的回调。
 * @param searchUrlPrefix 参数类型为 `() -> String`，表示读取当前搜索引擎搜索 URL 前缀的回调。
 * @param homeUrl 参数类型为 `() -> String`，表示读取当前应该打开的主页 URL 的回调。
 * @param activeStandardTabUrl 参数类型为 `() -> String?`，表示读取标准模式当前标签页恢复 URL 的回调。
 * @param loadUrl 参数类型为 `(String) -> Unit`，表示加载目标 URL 的回调。
 * @param isShareableUrl 参数类型为 `(String) -> Boolean`，表示判断外部 Intent URL 是否是可在浏览器中打开的网页 URL。
 */
class BrowserLaunchController(
    private val addressText: () -> String,
    private val runWithSuggestionsSuppressed: ((() -> Unit) -> Unit),
    private val searchUrlPrefix: () -> String,
    private val homeUrl: () -> String,
    private val activeStandardTabUrl: () -> String?,
    private val loadUrl: (String) -> Unit,
    private val isShareableUrl: (String) -> Boolean
) {
    /**
     * 函数 `loadAddressInput`：解析地址栏内容并加载解析出的 URL。
     *
     * 初学者阅读提示：UrlUtils 会把域名补成 URL，也会把普通文字转换成搜索 URL；空输入会返回 null。
     */
    fun loadAddressInput() {
        val input = addressText().trim()
        runWithSuggestionsSuppressed {
            UrlUtils.resolveAddressInput(input, searchUrlPrefix())
                ?.let { resolvedUrl -> loadUrl(resolvedUrl) }
        }
    }

    /**
     * 函数 `searchAddressKeyword`：把地址建议里的关键词转换成当前搜索引擎 URL。
     *
     * @param keyword 参数类型为 `String`，表示用户选择或输入的搜索关键词。
     */
    fun searchAddressKeyword(keyword: String) {
        val query = TextWhitespaceNormalizer.collapse(keyword)
        if (query.isEmpty()) {
            return
        }
        val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.name())
        loadUrl("${searchUrlPrefix()}$encodedQuery")
    }

    /**
     * 函数 `openHomePage`：打开设置中配置的主页。
     */
    fun openHomePage() {
        loadUrl(homeUrl())
    }

    /**
     * 函数 `openInitialStandardPage`：应用启动时优先恢复标准标签页 URL，否则打开主页。
     */
    fun openInitialStandardPage() {
        val restoredUrl = activeStandardTabUrl()
        if (restoredUrl.isNullOrBlank()) {
            openHomePage()
        } else {
            loadUrl(restoredUrl)
        }
    }

    /**
     * 函数 `openWenxinPage`：打开内置的百度文心页面入口。
     */
    fun openWenxinPage() {
        loadUrl(BAIDU_WENXIN_URL)
    }

    /**
     * 函数 `handleLaunchIntent`：处理系统传入的外部网页链接 Intent。
     *
     * @param intent 参数类型为 `Intent?`，表示 Activity 收到的启动或新 Intent。
     * @return 返回 `Boolean`，true 表示 Intent 中包含可打开 URL 且已经发起加载。
     */
    fun handleLaunchIntent(intent: Intent?): Boolean {
        val launchUrl = externalWebUrlFromIntent(intent) ?: return false
        loadUrl(launchUrl)
        return true
    }

    /**
     * 函数 `externalWebUrlFromIntent`：从 ACTION_VIEW Intent 中提取可打开的网页 URL。
     *
     * @param intent 参数类型为 `Intent?`，表示系统交给应用的 Intent。
     * @return 返回 `String?`，有可打开网页时返回 URL，否则返回 null。
     */
    private fun externalWebUrlFromIntent(intent: Intent?): String? {
        if (intent?.action != Intent.ACTION_VIEW) {
            return null
        }
        return intent.dataString
            ?.trim()
            ?.takeIf { value -> value.isNotEmpty() && isShareableUrl(value) }
    }

    private companion object {
        private const val BAIDU_WENXIN_URL = "https://chat.baidu.com/"
    }
}
