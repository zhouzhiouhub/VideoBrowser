package com.example.videobrowser.browser

import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

class BrowserManager(
    private val webView: WebView
) {
    fun setup() {
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
            setGeolocationEnabled(false)
            allowFileAccess = false
            allowContentAccess = true
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            @Suppress("DEPRECATION")
            databaseEnabled = true
            @Suppress("DEPRECATION")
            saveFormData = false
        }
    }

    fun setChromeClient(client: WebChromeClient?) {
        webView.webChromeClient = client
    }

    fun setBrowserClient(client: WebViewClient) {
        webView.webViewClient = client
    }

    fun setDownloadListener(listener: DownloadListener?) {
        webView.setDownloadListener(listener)
    }

    fun addJavascriptInterface(interfaceObject: Any, name: String) {
        webView.addJavascriptInterface(interfaceObject, name)
    }

    fun load(url: String) {
        suspendCurrentPage()
        webView.loadUrl(url)
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

    fun evaluateJavascript(script: String) {
        webView.evaluateJavascript(script, null)
    }

    fun clearBrowsingData() {
        webView.clearCache(true)
        webView.clearHistory()
        CookieManager.getInstance().removeAllCookies(null)
    }

    fun onPause() {
        // MVP keeps WebView active so media playback is not forcibly paused.
    }

    fun onResume() {
        // Reserved for future WebView resume policy.
    }

    fun destroy() {
        webView.webChromeClient = null
        disposeCurrentPage()
        webView.stopLoading()
        webView.loadUrl("about:blank")
        webView.clearHistory()
        webView.removeAllViews()
        webView.destroy()
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
