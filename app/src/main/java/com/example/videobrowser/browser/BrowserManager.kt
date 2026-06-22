package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 BrowserManager 可以拆开理解为“Browser Manager”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：封装 WebView 页面加载、标签页、导航安全、页面工具、权限回调或浏览器控件状态。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
import android.webkit.DownloadListener
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * WebView 的安全包装器。
 *
 * Android 的 WebView API 很分散：设置项、Cookie、前进后退、JS 注入、数据清理都在不同对象上。
 * BrowserManager 把这些操作集中起来，MainActivity 只需要和这个类交互，就不用到处直接操作 WebView。
 */
class BrowserManager(
    private var webView: WebView
) {
    private data class JavascriptInterfaceBinding(
        val interfaceObject: Any,
        val name: String
    )

    private val webViewSettings = BrowserWebViewSettingsController()
    private val webViewLifecycle = BrowserWebViewLifecycleController(webViewSettings)
    private val webViewFindController = BrowserWebViewFindController(
        webView = { webView },
        webViewSettings = webViewSettings
    )
    private val webViewNavigationController = BrowserWebViewNavigationController(
        webView = { webView }
    )
    private val javascriptInterfaces = mutableListOf<JavascriptInterfaceBinding>()
    private var chromeClient: WebChromeClient? = null
    private var browserClient: WebViewClient? = null
    private var downloadListener: DownloadListener? = null
    private var privateBrowsingEnabled = false

    val activeWebView: WebView
        get() = webView

    /**
     * 对当前 WebView 做一次基础配置。
     *
     * configuredWebViews 用 WeakHashMap 记录已经配置过的 WebView，避免同一个 WebView
     * 在标签页切换时被重复设置；WeakHashMap 不会阻止旧 WebView 被回收。
     */
    fun setup() {
        webViewSettings.setup(webView)
    }

    /**
     * 函数 `switchWebView`：封装 `switch Web View` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param nextWebView 参数类型为 `WebView`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param privateBrowsingEnabled 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     * @param detachCurrent 参数类型为 `Boolean`，表示函数执行 `detachCurrent` 相关逻辑时需要读取或处理的输入。
     */
    fun switchWebView(
        nextWebView: WebView,
        privateBrowsingEnabled: Boolean = this.privateBrowsingEnabled,
        detachCurrent: Boolean = true
    ) {
        // 标签页切换时，BrowserManager 的“当前 WebView”会换成另一个实例。
        // 这里重新挂回之前保存的 ChromeClient、WebViewClient、下载监听器和原生桥。
        if (webView === nextWebView) {
            setPrivateBrowsingEnabled(privateBrowsingEnabled)
            return
        }
        if (detachCurrent) {
            webView.webChromeClient = null
            webView.webViewClient = WebViewClient()
            webView.setDownloadListener(null)
        }

        webView = nextWebView
        this.privateBrowsingEnabled = privateBrowsingEnabled
        setup()
        setPrivateBrowsingEnabled(privateBrowsingEnabled)
        webView.webChromeClient = chromeClient
        browserClient?.let { webView.webViewClient = it }
        webView.setDownloadListener(downloadListener)
        javascriptInterfaces.forEach { binding ->
            webView.addJavascriptInterface(binding.interfaceObject, binding.name)
        }
    }

    /**
     * 函数 `setChromeClient`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param client 参数类型为 `WebChromeClient?`，表示函数执行 `client` 相关逻辑时需要读取或处理的输入。
     */
    fun setChromeClient(client: WebChromeClient?) {
        chromeClient = client
        webView.webChromeClient = client
    }

    /**
     * 函数 `setBrowserClient`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param client 参数类型为 `WebViewClient`，表示函数执行 `client` 相关逻辑时需要读取或处理的输入。
     */
    fun setBrowserClient(client: WebViewClient) {
        browserClient = client
        webView.webViewClient = client
    }

    /**
     * 函数 `setDownloadListener`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param listener 参数类型为 `DownloadListener?`，表示回调对象，异步操作完成后用它把结果通知回调用方。
     */
    fun setDownloadListener(listener: DownloadListener?) {
        downloadListener = listener
        webView.setDownloadListener(listener)
    }

    /**
     * 函数 `setFindResultListener`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param listener 参数类型为 `((Int, Int, Boolean) -> Unit)?`，表示回调对象，异步操作完成后用它把结果通知回调用方。
     */
    fun setFindResultListener(listener: ((Int, Int, Boolean) -> Unit)?) {
        webViewFindController.setFindResultListener(listener)
    }

    /**
     * 函数 `addJavascriptInterface`：封装 `add Javascript Interface` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param interfaceObject 参数类型为 `Any`，表示函数执行 `interfaceObject` 相关逻辑时需要读取或处理的输入。
     * @param name 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     */
    fun addJavascriptInterface(interfaceObject: Any, name: String) {
        javascriptInterfaces.removeAll { it.name == name }
        javascriptInterfaces.add(JavascriptInterfaceBinding(interfaceObject, name))
        webView.addJavascriptInterface(interfaceObject, name)
    }

    /**
     * 函数 `load`：启动或加载 `load` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     */
    fun load(url: String) {
        webViewNavigationController.load(url)
    }

    /**
     * 函数 `loadErrorPage`：启动或加载 `load Error Page` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param error 参数类型为 `BrowserPageError`，表示函数执行 `error` 相关逻辑时需要读取或处理的输入。
     */
    fun loadErrorPage(error: BrowserPageError) {
        webViewNavigationController.loadErrorPage(error)
    }

    /**
     * 函数 `goBack`：封装 `go Back` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun goBack(): Boolean {
        return webViewNavigationController.goBack()
    }

    /**
     * 函数 `goForward`：封装 `go Forward` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun goForward(): Boolean {
        return webViewNavigationController.goForward()
    }

    /**
     * 函数 `reload`：封装 `reload` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun reload() {
        webViewNavigationController.reload()
    }

    /**
     * 函数 `stopLoading`：封装 `stop Loading` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun stopLoading() {
        webViewNavigationController.stopLoading()
    }

    /**
     * 函数 `canGoBack`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun canGoBack(): Boolean {
        return webViewNavigationController.canGoBack()
    }

    /**
     * 函数 `canGoForward`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun canGoForward(): Boolean {
        return webViewNavigationController.canGoForward()
    }

    /**
     * 函数 `currentUrl`：从现有状态、缓存或输入对象中取得目标数据，并把结果交给调用方继续处理。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun currentUrl(): String? {
        return webViewNavigationController.currentUrl()
    }

    /**
     * 函数 `userAgentString`：封装 `user Agent String` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun userAgentString(): String? {
        return webViewNavigationController.userAgentString()
    }

    /**
     * 函数 `applyDesktopMode`：根据最新状态刷新 `apply Desktop Mode` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param enabled 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     * @param desktopUserAgent 参数类型为 `String`，表示函数执行 `desktopUserAgent` 相关逻辑时需要读取或处理的输入。
     * @param defaultUserAgent 参数类型为 `String?`，表示函数执行 `defaultUserAgent` 相关逻辑时需要读取或处理的输入。
     * @param reload 参数类型为 `Boolean`，表示函数执行 `reload` 相关逻辑时需要读取或处理的输入。
     */
    fun applyDesktopMode(
        enabled: Boolean,
        desktopUserAgent: String,
        defaultUserAgent: String?,
        reload: Boolean
    ) {
        // 桌面模式本质是修改 User-Agent 和 viewport 设置，然后按需重新加载当前页。
        webView.settings.userAgentString = if (enabled) {
            desktopUserAgent
        } else {
            defaultUserAgent
        }
        webView.settings.useWideViewPort = enabled
        webView.settings.loadWithOverviewMode = enabled
        if (reload) {
            reload()
        }
    }

    /**
     * 函数 `setPrivateBrowsingEnabled`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param enabled 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     */
    fun setPrivateBrowsingEnabled(enabled: Boolean) {
        privateBrowsingEnabled = enabled
        webViewSettings.setPrivateBrowsingEnabled(enabled, webView)
    }

    /**
     * 函数 `setThirdPartyCookiesEnabled`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param enabled 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     */
    fun setThirdPartyCookiesEnabled(enabled: Boolean) {
        webViewSettings.setThirdPartyCookiesEnabled(enabled)
    }

    /**
     * 函数 `setMixedContentBlocked`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param blocked 参数类型为 `Boolean`，表示函数执行 `blocked` 相关逻辑时需要读取或处理的输入。
     */
    fun setMixedContentBlocked(blocked: Boolean) {
        webViewSettings.setMixedContentBlocked(blocked)
    }

    /**
     * 函数 `setTextZoomPercent`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param percent 参数类型为 `Int`，表示函数执行 `percent` 相关逻辑时需要读取或处理的输入。
     */
    fun setTextZoomPercent(percent: Int) {
        webViewSettings.setTextZoomPercent(percent)
    }

    /**
     * 函数 `evaluateJavascript`：封装 `evaluate Javascript` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param script 参数类型为 `String`，表示函数执行 `script` 相关逻辑时需要读取或处理的输入。
     */
    fun evaluateJavascript(script: String) {
        webView.evaluateJavascript(script, null)
    }

    /**
     * 函数 `findAllAsync`：从现有状态、缓存或输入对象中取得目标数据，并把结果交给调用方继续处理。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param query 参数类型为 `String`，表示函数执行 `query` 相关逻辑时需要读取或处理的输入。
     */
    fun findAllAsync(query: String) {
        webViewFindController.findAllAsync(query)
    }

    /**
     * 函数 `findNext`：从现有状态、缓存或输入对象中取得目标数据，并把结果交给调用方继续处理。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param forward 参数类型为 `Boolean`，表示函数执行 `forward` 相关逻辑时需要读取或处理的输入。
     */
    fun findNext(forward: Boolean = true) {
        webViewFindController.findNext(forward)
    }

    /**
     * 函数 `clearFindMatches`：封装 `clear Find Matches` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun clearFindMatches() {
        webViewFindController.clearFindMatches()
    }

    /**
     * 函数 `clearBrowsingData`：封装 `clear Browsing Data` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param clearSharedStores 参数类型为 `Boolean`，表示函数执行 `clearSharedStores` 相关逻辑时需要读取或处理的输入。
     */
    fun clearBrowsingData(clearSharedStores: Boolean = true) {
        // clearSharedStores 为 true 时会清理 Cookie/WebStorage 等 WebView 全局数据。
        // 无痕退出时可按需要只清当前 WebView，避免影响普通模式的登录状态。
        webViewLifecycle.clearBrowsingData(webView, clearSharedStores)
    }

    /**
     * 函数 `clearCache`：封装 `clear Cache` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun clearCache() {
        webViewLifecycle.clearCache(webView)
    }

    /**
     * 函数 `destroyWebView`：封装 `destroy Web View` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param targetWebView 参数类型为 `WebView`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param clearSharedStores 参数类型为 `Boolean`，表示函数执行 `clearSharedStores` 相关逻辑时需要读取或处理的输入。
     */
    fun destroyWebView(targetWebView: WebView, clearSharedStores: Boolean = true) {
        webViewLifecycle.destroyWebView(
            targetWebView = targetWebView,
            activeWebView = webView,
            clearSharedStores = clearSharedStores
        )
    }

    /**
     * 函数 `clearTransientBrowsingData`：封装 `clear Transient Browsing Data` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun clearTransientBrowsingData() {
        webViewLifecycle.clearTransientBrowsingData(webView)
    }

    /**
     * 函数 `onPause`：处理 `on Pause` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun onPause() {
        // MVP keeps WebView active so media playback is not forcibly paused.
    }

    /**
     * 函数 `onResume`：处理 `on Resume` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun onResume() {
        // Reserved for future WebView resume policy.
    }

    /**
     * 函数 `destroy`：封装 `destroy` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun destroy() {
        destroyWebView(webView)
    }

}
