package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器导航模块”。
 * 地址栏加载、WebView 跳转拦截、外部协议、HTTP 降级确认和特殊下载拦截都会经过这里。
 * 主要职责：把一次 URL 请求转换为“打开网页、打开原生播放器、交给外部协议处理、显示确认弹窗或直接拦截”。
 * 阅读顺序：先看 loadUrl，再看 shouldBlockUrl，最后看 showInsecureNavigationConfirmation 和 openExternalProtocolNavigation。
 */
import android.net.Uri
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.rules.RuleEngine
import com.example.videobrowser.utils.UrlUtils
import com.example.videobrowser.utils.WebSchemePolicy
import com.example.videobrowser.video.MediaRouteAction
import com.example.videobrowser.video.MediaRouteDecision
import com.example.videobrowser.video.MediaRouteRequest
import com.example.videobrowser.video.MediaRouteSource
import com.example.videobrowser.video.MediaRoutingController

/**
 * 浏览器 URL 导航控制器。
 *
 * MainActivity 只保留 loadUrl 和 shouldBlockUrl 入口；本类集中处理 URL 清理、媒体路由、安全确认和外部协议。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示当前屏幕宿主，用来显示 Toast 和确认弹窗。
 * @param ruleEngine 参数类型为 `() -> RuleEngine?`，表示读取规则引擎的回调；为空时跳过导航 URL 清理。
 * @param browserManager 参数类型为 `() -> BrowserManager`，表示读取当前浏览器管理器的回调，用来加载 URL、读取 UA。
 * @param sessionController 参数类型为 `() -> BrowserSessionController`，表示读取当前会话控制器的回调，用来读取和更新页面 URL/标题。
 * @param externalNavigator 参数类型为 `BrowserExternalNavigator`，表示外部协议导航器，用来处理 intent、mailto 等非网页 scheme。
 * @param closeFunctionCenter 参数类型为 `() -> Boolean`，表示关闭功能中心面板的回调，避免页面加载时面板遮挡内容。
 * @param openNativePlayer 参数类型为 `(MediaRouteDecision) -> Unit`，表示打开原生播放器的回调。
 * @param isProviderHomeUrl 参数类型为 `(String?) -> Boolean`，表示判断 URL 是否为搜索提供方首页的回调。
 * @param updateAddressBar 参数类型为 `(String?) -> Unit`，表示刷新地址栏展示文本的回调。
 * @param hideKeyboard 参数类型为 `() -> Unit`，表示收起软键盘和地址建议面板的回调。
 * @param showHomeContent 参数类型为 `(Boolean) -> Unit`，表示按首页状态刷新主页/浏览器内容区域的回调。
 */
class BrowserNavigationController(
    private val activity: AppCompatActivity,
    private val ruleEngine: () -> RuleEngine?,
    private val browserManager: () -> BrowserManager,
    private val sessionController: () -> BrowserSessionController,
    private val externalNavigator: BrowserExternalNavigator,
    private val closeFunctionCenter: () -> Boolean,
    private val openNativePlayer: (MediaRouteDecision) -> Unit,
    private val isProviderHomeUrl: (String?) -> Boolean,
    private val updateAddressBar: (String?) -> Unit,
    private val hideKeyboard: () -> Unit,
    private val showHomeContent: (Boolean) -> Unit
) {
    /**
     * 函数 `loadUrl`：加载一个普通 URL，并在 HTTPS 到 HTTP 降级时先弹出确认。
     *
     * @param url 参数类型为 `String`，表示用户或业务代码请求打开的页面地址。
     */
    fun loadUrl(url: String) {
        loadUrlInternal(url, allowInsecureNavigation = false)
    }

    /**
     * 函数 `loadUrlAfterInsecureNavigationConfirmation`：在用户确认后继续加载不安全跳转目标。
     *
     * @param url 参数类型为 `String`，表示已经通过用户确认的 HTTP 页面地址。
     */
    fun loadUrlAfterInsecureNavigationConfirmation(url: String) {
        loadUrlInternal(url, allowInsecureNavigation = true)
    }

    /**
     * 函数 `shouldBlockUrl`：处理 WebView shouldOverrideUrlLoading 回调中的 URL。
     *
     * 初学者阅读提示：返回 true 表示本应用已经接管或拦截该 URL，WebView 不应继续默认加载。
     *
     * @param view 参数类型为 `WebView?`，表示发起跳转的 WebView，用来停止当前加载。
     * @param uri 参数类型为 `Uri`，表示 WebView 准备打开的目标地址。
     * @param openMedia 参数类型为 `Boolean`，表示是否允许媒体路由逻辑把视频地址转给原生播放器。
     * @return 返回 `Boolean`，true 表示拦截，false 表示允许 WebView 继续加载。
     */
    fun shouldBlockUrl(view: WebView?, uri: Uri, openMedia: Boolean = true): Boolean {
        if (openMedia && routeWebViewMediaNavigation(view, uri)) {
            return true
        }

        if (isUnavailableUcDownloadUrl(uri)) {
            view?.stopLoading()
            Toast.makeText(
                activity,
                R.string.toast_uc_download_unavailable,
                Toast.LENGTH_SHORT
            ).show()
            return true
        }

        if (!WebSchemePolicy.isWebViewLoadableScheme(uri.scheme)) {
            if (openExternalProtocolNavigation(view, uri)) {
                return true
            }
            return true
        }

        if (openMedia &&
            HttpNavigationSafetyPolicy.requiresInsecureNavigationConfirmation(
                sessionController().currentPageUrl,
                uri.toString()
            )
        ) {
            view?.stopLoading()
            showInsecureNavigationConfirmation(uri.toString())
            return true
        }

        if (openMedia && loadCleanedWebViewUrlIfNeeded(view, uri)) {
            return true
        }

        return false
    }

    /**
     * 函数 `loadUrlInternal`：执行地址栏或内部代码发起的顶层页面加载。
     *
     * @param url 参数类型为 `String`，表示待加载的原始 URL。
     * @param allowInsecureNavigation 参数类型为 `Boolean`，表示是否跳过 HTTPS 到 HTTP 的确认检查。
     */
    private fun loadUrlInternal(url: String, allowInsecureNavigation: Boolean) {
        val cleanedUrl = cleanNavigationUrl(url)
        closeFunctionCenter()
        val mediaDecision = MediaRoutingController.route(
            MediaRouteRequest(
                source = MediaRouteSource.ADDRESS_BAR,
                url = cleanedUrl,
                currentPageUrl = sessionController().currentPageUrl,
                currentPageTitle = sessionController().currentPageTitle,
                userAgent = browserManager().userAgentString()
            )
        )
        when (mediaDecision.action) {
            MediaRouteAction.OPEN_NATIVE_PLAYER -> {
                openNativePlayer(mediaDecision)
                return
            }

            MediaRouteAction.BLOCK -> return
            else -> Unit
        }

        if (!allowInsecureNavigation &&
            HttpNavigationSafetyPolicy.requiresInsecureNavigationConfirmation(
                sessionController().currentPageUrl,
                cleanedUrl
            )
        ) {
            showInsecureNavigationConfirmation(cleanedUrl)
            return
        }

        sessionController().currentPageUrl = cleanedUrl
        val isProviderHome = isProviderHomeUrl(cleanedUrl)
        updateAddressBar(cleanedUrl)
        hideKeyboard()
        showHomeContent(isProviderHome)
        browserManager().load(cleanedUrl)
    }

    /**
     * 函数 `routeWebViewMediaNavigation`：尝试把 WebView 跳转目标交给媒体路由处理。
     *
     * @param view 参数类型为 `WebView?`，表示发起跳转的 WebView，用于必要时停止加载。
     * @param uri 参数类型为 `Uri`，表示 WebView 准备打开的目标地址。
     * @return 返回 `Boolean`，true 表示媒体路由已经接管或拦截该跳转。
     */
    private fun routeWebViewMediaNavigation(view: WebView?, uri: Uri): Boolean {
        val mediaDecision = MediaRoutingController.route(
            MediaRouteRequest(
                source = MediaRouteSource.WEBVIEW_OVERRIDE,
                url = uri.toString(),
                currentPageUrl = sessionController().currentPageUrl,
                currentPageTitle = sessionController().currentPageTitle,
                userAgent = browserManager().userAgentString()
            )
        )
        return when (mediaDecision.action) {
            MediaRouteAction.OPEN_NATIVE_PLAYER -> {
                view?.stopLoading()
                openNativePlayer(mediaDecision)
                true
            }

            MediaRouteAction.BLOCK -> {
                if (openExternalProtocolNavigation(view, uri)) {
                    true
                } else {
                    true
                }
            }

            else -> false
        }
    }

    /**
     * 函数 `loadCleanedWebViewUrlIfNeeded`：如果规则引擎清理了 URL，就停止旧跳转并加载清理后的地址。
     *
     * @param view 参数类型为 `WebView?`，表示发起跳转的 WebView，用于停止原始 URL 加载。
     * @param uri 参数类型为 `Uri`，表示原始跳转地址。
     * @return 返回 `Boolean`，true 表示已经改为加载清理后的地址。
     */
    private fun loadCleanedWebViewUrlIfNeeded(view: WebView?, uri: Uri): Boolean {
        val engine = ruleEngine() ?: return false
        val originalUrl = uri.toString()
        val cleanedUrl = engine.cleanNavigationUrl(
            url = originalUrl,
            pageUrl = sessionController().currentPageUrl
        )
        if (cleanedUrl == originalUrl) {
            return false
        }
        view?.stopLoading()
        loadUrl(cleanedUrl)
        return true
    }

    /**
     * 函数 `showInsecureNavigationConfirmation`：在 HTTPS 页面跳转到 HTTP 页面前显示确认弹窗。
     *
     * @param url 参数类型为 `String`，表示需要用户确认后才能继续打开的 HTTP 地址。
     */
    private fun showInsecureNavigationConfirmation(url: String) {
        AlertDialog.Builder(activity)
            .setTitle(R.string.title_confirm_insecure_navigation)
            .setMessage(
                activity.getString(
                    R.string.dialog_confirm_insecure_navigation_message,
                    UrlUtils.displayUrl(url)
                )
            )
            .setPositiveButton(R.string.action_open_page) { _, _ ->
                loadUrlAfterInsecureNavigationConfirmation(url)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    /**
     * 函数 `openExternalProtocolNavigation`：把外部协议交给 BrowserExternalNavigator 处理。
     *
     * @param view 参数类型为 `WebView?`，表示发起跳转的 WebView，用于停止当前加载。
     * @param uri 参数类型为 `Uri`，表示 mailto、intent 或其他自定义协议地址。
     * @return 返回 `Boolean`，true 表示该 scheme 应该被外部协议流程处理。
     */
    private fun openExternalProtocolNavigation(view: WebView?, uri: Uri): Boolean {
        if (!ExternalProtocolPolicy.shouldOpenExternally(uri.scheme)) {
            return false
        }
        view?.stopLoading()
        externalNavigator.openExternalProtocolUrl(uri.toString()) { fallbackUrl ->
            loadUrl(fallbackUrl)
        }
        return true
    }

    /**
     * 函数 `isUnavailableUcDownloadUrl`：识别已知不可用的 UC 浏览器下载地址。
     *
     * @param uri 参数类型为 `Uri`，表示 WebView 准备打开的目标地址。
     * @return 返回 `Boolean`，true 表示该地址应被拦截并提示不可下载。
     */
    private fun isUnavailableUcDownloadUrl(uri: Uri): Boolean {
        val host = uri.host?.lowercase().orEmpty()
        val path = uri.path.orEmpty()
        return (host == "down2.uc.cn" && path == "/ucbrowser/v2/down.php") ||
            (host == "umcdn-oss.oss-cn-beijing.aliyuncs.com" &&
                path.contains("/gongyp/shenmainuc8/") &&
                path.endsWith(".apk", ignoreCase = true))
    }

    /**
     * 函数 `cleanNavigationUrl`：使用规则引擎清理顶层导航 URL。
     *
     * @param url 参数类型为 `String`，表示待清理的导航地址。
     * @return 返回清理后的 URL；没有规则引擎或没有命中规则时返回原地址。
     */
    private fun cleanNavigationUrl(url: String): String {
        return ruleEngine()?.cleanNavigationUrl(url, sessionController().currentPageUrl) ?: url
    }
}
