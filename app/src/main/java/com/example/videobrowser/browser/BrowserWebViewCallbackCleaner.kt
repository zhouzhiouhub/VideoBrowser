package com.example.videobrowser.browser

import android.webkit.WebView
import android.webkit.WebViewClient

internal object BrowserWebViewCallbackCleaner {
    fun detachCallbacks(targetWebView: WebView) {
        targetWebView.webChromeClient = null
        targetWebView.webViewClient = WebViewClient()
        targetWebView.setDownloadListener(null)
    }
}
