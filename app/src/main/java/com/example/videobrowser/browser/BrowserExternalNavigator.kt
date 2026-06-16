package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 BrowserExternalNavigator 可以拆开理解为“Browser External Navigator”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：封装 WebView 页面加载、标签页、导航安全、页面工具、权限回调或浏览器控件状态。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
import android.content.Intent
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.video.ExternalSubtitleCandidate
import com.example.videobrowser.video.PlaybackQueue
import com.example.videobrowser.video.PlayerActivity

/**
 * 浏览器跳出当前 WebView 的统一出口。
 *
 * 这里集中处理两类情况：一类是网页给出的特殊协议链接，另一类是把视频地址交给原生播放器。
 * 集中在一个类里可以避免 MainActivity、PageActionsController 等位置重复拼装播放器 Intent。
 */
class BrowserExternalNavigator(
    private val activity: AppCompatActivity,
    private val browserManager: () -> BrowserManager,
    private val currentPageTitle: () -> String,
    private val currentShareableUrl: () -> String?,
    private val isShareableUrl: (String) -> Boolean
) {
    /**
     * 函数 `openExternalProtocolUrl`：启动或加载 `open External Protocol Url` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param loadFallbackUrl 参数类型为 `(String) -> Unit`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun openExternalProtocolUrl(
        url: String,
        loadFallbackUrl: (String) -> Unit = {}
    ): Boolean {
        val uri = Uri.parse(url)
        val scheme = uri.scheme
        if (!ExternalProtocolPolicy.shouldOpenExternally(scheme)) {
            return false
        }

        if (scheme.equals("intent", ignoreCase = true)) {
            return openIntentUri(url, loadFallbackUrl)
        }

        Toast.makeText(activity, R.string.toast_external_app_blocked, Toast.LENGTH_SHORT).show()
        return true
    }

    /**
     * 函数 `openNativePlayer`：启动或加载 `open Native Player` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param mimeType 参数类型为 `String?`，表示函数执行 `mimeType` 相关逻辑时需要读取或处理的输入。
     * @param userAgentOverride 参数类型为 `String?`，表示函数执行 `userAgentOverride` 相关逻辑时需要读取或处理的输入。
     * @param titleOverride 参数类型为 `String?`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param privateBrowsing 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     * @param subtitleCandidates 参数类型为 `List<ExternalSubtitleCandidate>`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param playbackQueue 参数类型为 `PlaybackQueue?`，表示函数执行 `playbackQueue` 相关逻辑时需要读取或处理的输入。
     */
    fun openNativePlayer(
        url: String,
        mimeType: String? = null,
        userAgentOverride: String? = null,
        titleOverride: String? = null,
        privateBrowsing: Boolean = false,
        subtitleCandidates: List<ExternalSubtitleCandidate> = emptyList(),
        playbackQueue: PlaybackQueue? = null
    ) {
        // 打开远程媒体时带上 Cookie 和 Referer，能提高需要登录或防盗链站点的播放成功率。
        val title = titleOverride
            ?.takeIf { it.isNotBlank() }
            ?: currentPageTitle()
                .takeIf { it.isNotBlank() && !it.equals(url, ignoreCase = true) }
            ?: URLUtil.guessFileName(url, null, mimeType)
        val isRemoteMedia = isShareableUrl(url)
        val referer = if (isRemoteMedia) {
            currentShareableUrl()?.takeIf { !it.equals(url, ignoreCase = true) }
        } else {
            null
        }
        val cookie = if (isRemoteMedia) {
            CookieManager.getInstance().getCookie(url)
        } else {
            null
        }
        val intent = PlayerActivity.createIntent(
            context = activity,
            mediaUri = url,
            title = title,
            mimeType = mimeType,
            userAgent = userAgentOverride ?: browserManager().userAgentString(),
            cookie = cookie,
            referer = referer,
            privateBrowsing = privateBrowsing,
            subtitleCandidates = subtitleCandidates,
            playbackQueue = playbackQueue
        )
        activity.startActivity(intent)
    }

    /**
     * 函数 `openIntentUri`：启动或加载 `open Intent Uri` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param loadFallbackUrl 参数类型为 `(String) -> Unit`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun openIntentUri(
        url: String,
        loadFallbackUrl: (String) -> Unit
    ): Boolean {
        val fallbackUrl = browserFallbackUrlFromIntentUri(url)
        if (fallbackUrl != null) {
            loadFallbackUrl(fallbackUrl)
            return true
        }

        Toast.makeText(activity, R.string.toast_external_app_blocked, Toast.LENGTH_SHORT).show()
        return true
    }

    /**
     * 函数 `browserFallbackUrlFromIntentUri`：封装 `browser Fallback Url From Intent Uri` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun browserFallbackUrlFromIntentUri(url: String): String? {
        val parsedFallbackUrl = runCatching {
            Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
        }.getOrNull()
            ?.getStringExtra(ExternalProtocolPolicy.BROWSER_FALLBACK_URL)
            ?.takeIf(ExternalProtocolPolicy::isWebUrl)

        return parsedFallbackUrl ?: ExternalProtocolPolicy.fallbackUrlFromIntentUri(url)
    }
}
