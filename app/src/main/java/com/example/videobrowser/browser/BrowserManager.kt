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
}
