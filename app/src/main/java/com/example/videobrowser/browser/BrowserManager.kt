package com.example.videobrowser.browser

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
    private var privateBrowsingEnabled = false

    val activeWebView: WebView
        get() = webView

    fun setup() {
        if (!configuredWebViews.add(webView)) {
            return
        }

        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }

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
            setSupportMultipleWindows(false)
            setGeolocationEnabled(true)
            allowFileAccess = false
            allowContentAccess = true
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
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
        CookieManager.getInstance().apply {
            setAcceptCookie(!enabled)
            setAcceptThirdPartyCookies(webView, !enabled)
        }
        webView.settings.domStorageEnabled = !enabled
        @Suppress("DEPRECATION")
        webView.settings.databaseEnabled = !enabled
        webView.settings.cacheMode = if (enabled) {
            WebSettings.LOAD_NO_CACHE
        } else {
            WebSettings.LOAD_DEFAULT
        }
    }

    fun evaluateJavascript(script: String) {
        webView.evaluateJavascript(script, null)
    }

    fun clearBrowsingData(clearSharedStores: Boolean = true) {
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
