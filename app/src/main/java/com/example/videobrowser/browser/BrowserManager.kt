package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 BrowserManager 可以拆开理解为“Browser Manager”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：封装 WebView 页面加载、标签页、导航安全、页面工具、权限回调或浏览器控件状态。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
import android.os.Build
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.WebStorage
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewDatabase
import android.webkit.WebViewClient
import java.util.Collections
import java.util.WeakHashMap

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

    private val configuredWebViews = Collections.newSetFromMap(WeakHashMap<WebView, Boolean>())
    private val javascriptInterfaces = mutableListOf<JavascriptInterfaceBinding>()
    private var chromeClient: WebChromeClient? = null
    private var browserClient: WebViewClient? = null
    private var downloadListener: DownloadListener? = null
    private var findResultListener: ((Int, Int, Boolean) -> Unit)? = null
    private var privateBrowsingEnabled = false
    private var thirdPartyCookiesEnabled = true
    private var mixedContentBlocked = true
    private var textZoomPercent = 100

    val activeWebView: WebView
        get() = webView

    /**
     * 对当前 WebView 做一次基础配置。
     *
     * configuredWebViews 用 WeakHashMap 记录已经配置过的 WebView，避免同一个 WebView
     * 在标签页切换时被重复设置；WeakHashMap 不会阻止旧 WebView 被回收。
     */
    fun setup() {
        if (!configuredWebViews.add(webView)) {
            return
        }

        applyCookiePolicy(webView)
        applyFindResultListener(webView)

        webView.settings.apply {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = false
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            cacheMode = WebSettings.LOAD_DEFAULT
            loadWithOverviewMode = false
            useWideViewPort = false
            loadsImagesAutomatically = true
            blockNetworkImage = false
            textZoom = textZoomPercent
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            setSupportMultipleWindows(true)
            setGeolocationEnabled(true)
            allowFileAccess = false
            allowContentAccess = true
            @Suppress("DEPRECATION")
            allowFileAccessFromFileURLs = false
            @Suppress("DEPRECATION")
            allowUniversalAccessFromFileURLs = false
            applyMixedContentMode(webView)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                safeBrowsingEnabled = true
            }
            @Suppress("DEPRECATION")
            databaseEnabled = true
            @Suppress("DEPRECATION")
            saveFormData = false
        }
    }

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

    fun setChromeClient(client: WebChromeClient?) {
        chromeClient = client
        webView.webChromeClient = client
    }

    fun setBrowserClient(client: WebViewClient) {
        browserClient = client
        webView.webViewClient = client
    }

    fun setDownloadListener(listener: DownloadListener?) {
        downloadListener = listener
        webView.setDownloadListener(listener)
    }

    fun setFindResultListener(listener: ((Int, Int, Boolean) -> Unit)?) {
        findResultListener = listener
        configuredWebViews.forEach(::applyFindResultListener)
    }

    fun addJavascriptInterface(interfaceObject: Any, name: String) {
        javascriptInterfaces.removeAll { it.name == name }
        javascriptInterfaces.add(JavascriptInterfaceBinding(interfaceObject, name))
        webView.addJavascriptInterface(interfaceObject, name)
    }

    fun load(url: String) {
        suspendCurrentPage()
        webView.loadUrl(url)
    }

    fun loadErrorPage(error: BrowserPageError) {
        disposeCurrentPage()
        webView.loadDataWithBaseURL(
            "about:blank",
            BrowserErrorPage.render(error),
            "text/html",
            "UTF-8",
            null
        )
    }

    fun goBack(): Boolean {
        if (!webView.canGoBack()) {
            return false
        }
        suspendCurrentPage()
        webView.goBack()
        return true
    }

    fun goForward(): Boolean {
        if (!webView.canGoForward()) {
            return false
        }
        suspendCurrentPage()
        webView.goForward()
        return true
    }

    fun reload() {
        suspendCurrentPage()
        webView.reload()
    }

    fun stopLoading() {
        webView.stopLoading()
    }

    fun canGoBack(): Boolean {
        return webView.canGoBack()
    }

    fun canGoForward(): Boolean {
        return webView.canGoForward()
    }

    fun currentUrl(): String? {
        return webView.url
    }

    fun userAgentString(): String? {
        return webView.settings.userAgentString
    }

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

    fun setPrivateBrowsingEnabled(enabled: Boolean) {
        privateBrowsingEnabled = enabled
        configuredWebViews.forEach(::applyCookiePolicy)
        webView.settings.domStorageEnabled = !enabled
        @Suppress("DEPRECATION")
        webView.settings.databaseEnabled = !enabled
        webView.settings.cacheMode = if (enabled) {
            WebSettings.LOAD_NO_CACHE
        } else {
            WebSettings.LOAD_DEFAULT
        }
    }

    fun setThirdPartyCookiesEnabled(enabled: Boolean) {
        thirdPartyCookiesEnabled = enabled
        configuredWebViews.forEach(::applyCookiePolicy)
    }

    fun setMixedContentBlocked(blocked: Boolean) {
        mixedContentBlocked = blocked
        configuredWebViews.forEach(::applyMixedContentMode)
    }

    fun setTextZoomPercent(percent: Int) {
        textZoomPercent = percent
        configuredWebViews.forEach(::applyTextZoom)
    }

    fun evaluateJavascript(script: String) {
        webView.evaluateJavascript(script, null)
    }

    fun findAllAsync(query: String) {
        webView.findAllAsync(query)
    }

    fun findNext(forward: Boolean = true) {
        webView.findNext(forward)
    }

    fun clearFindMatches() {
        webView.clearMatches()
    }

    fun clearBrowsingData(clearSharedStores: Boolean = true) {
        // clearSharedStores 为 true 时会清理 Cookie/WebStorage 等 WebView 全局数据。
        // 无痕退出时可按需要只清当前 WebView，避免影响普通模式的登录状态。
        clearBrowsingData(webView, clearSharedStores)
    }

    fun clearCache() {
        webView.clearCache(true)
    }

    fun destroyWebView(targetWebView: WebView, clearSharedStores: Boolean = true) {
        targetWebView.webChromeClient = null
        if (targetWebView === webView) {
            disposeCurrentPage()
        }
        targetWebView.stopLoading()
        targetWebView.loadUrl("about:blank")
        clearBrowsingData(targetWebView, clearSharedStores)
        targetWebView.removeAllViews()
        configuredWebViews.remove(targetWebView)
        targetWebView.destroy()
    }

    private fun clearBrowsingData(targetWebView: WebView, clearSharedStores: Boolean) {
        targetWebView.clearCache(true)
        targetWebView.clearHistory()
        targetWebView.clearFormData()
        targetWebView.clearSslPreferences()
        if (clearSharedStores) {
            WebStorage.getInstance().deleteAllData()
            WebViewDatabase.getInstance(targetWebView.context).apply {
                clearHttpAuthUsernamePassword()
            }
            CookieManager.getInstance().apply {
                removeAllCookies(null)
                flush()
            }
        }
    }

    private fun applyCookiePolicy(targetWebView: WebView) {
        CookieManager.getInstance().apply {
            setAcceptCookie(!privateBrowsingEnabled)
            setAcceptThirdPartyCookies(
                targetWebView,
                !privateBrowsingEnabled && thirdPartyCookiesEnabled
            )
        }
    }

    private fun applyMixedContentMode(targetWebView: WebView) {
        targetWebView.settings.mixedContentMode = if (mixedContentBlocked) {
            WebSettings.MIXED_CONTENT_NEVER_ALLOW
        } else {
            WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }
    }

    private fun applyTextZoom(targetWebView: WebView) {
        targetWebView.settings.textZoom = textZoomPercent
    }

    private fun applyFindResultListener(targetWebView: WebView) {
        targetWebView.setFindListener(
            findResultListener?.let { listener ->
                WebView.FindListener { activeMatchOrdinal, numberOfMatches, isDoneCounting ->
                    listener(activeMatchOrdinal, numberOfMatches, isDoneCounting)
                }
            }
        )
    }

    fun clearTransientBrowsingData() {
        disposeCurrentPage()
        webView.stopLoading()
        webView.loadUrl("about:blank")
        clearBrowsingData(clearSharedStores = false)
    }

    fun onPause() {
        // MVP keeps WebView active so media playback is not forcibly paused.
    }

    fun onResume() {
        // Reserved for future WebView resume policy.
    }

    fun destroy() {
        destroyWebView(webView)
    }

    private fun suspendCurrentPage() {
        if (webView.url.isNullOrBlank()) {
            return
        }
        webView.evaluateJavascript(PAGE_SUSPEND_SCRIPT, null)
    }

    private fun disposeCurrentPage() {
        if (webView.url.isNullOrBlank()) {
            return
        }
        webView.evaluateJavascript(PAGE_DISPOSE_SCRIPT, null)
    }

    private companion object {
        private const val PAGE_SUSPEND_SCRIPT =
            "if(window.VideoBrowserEnhancer&&typeof window.VideoBrowserEnhancer.suspend==='function'){" +
                "window.VideoBrowserEnhancer.suspend({pauseVideos:true});" +
                "}"
        private const val PAGE_DISPOSE_SCRIPT =
            "if(window.VideoBrowserEnhancer&&typeof window.VideoBrowserEnhancer.dispose==='function'){" +
                "window.VideoBrowserEnhancer.dispose({pauseVideos:true});" +
                "}"
    }
}
