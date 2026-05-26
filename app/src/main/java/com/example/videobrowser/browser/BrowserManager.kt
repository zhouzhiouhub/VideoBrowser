package com.example.videobrowser.browser

import android.webkit.WebSettings
import android.webkit.WebView

class BrowserManager(
    private val webView: WebView
) {
    fun setup() {
        webView.settings.apply {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = false
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            cacheMode = WebSettings.LOAD_DEFAULT
            loadWithOverviewMode = true
            useWideViewPort = true
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

    fun load(url: String) {
        webView.loadUrl(url)
    }

    fun goBack(): Boolean {
        if (!webView.canGoBack()) {
            return false
        }
        webView.goBack()
        return true
    }

    fun goForward(): Boolean {
        if (!webView.canGoForward()) {
            return false
        }
        webView.goForward()
        return true
    }

    fun reload() {
        webView.reload()
    }

    fun canGoBack(): Boolean {
        return webView.canGoBack()
    }

    fun canGoForward(): Boolean {
        return webView.canGoForward()
    }

    fun onPause() {
        // MVP keeps WebView active so media playback is not forcibly paused.
    }

    fun onResume() {
        // Reserved for future WebView resume policy.
    }

    fun destroy() {
        webView.webChromeClient = null
        webView.stopLoading()
        webView.loadUrl("about:blank")
        webView.clearHistory()
        webView.removeAllViews()
        webView.destroy()
    }
}
