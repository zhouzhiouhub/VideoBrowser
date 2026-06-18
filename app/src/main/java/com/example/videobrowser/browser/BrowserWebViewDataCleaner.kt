package com.example.videobrowser.browser

import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewDatabase

internal object BrowserWebViewDataCleaner {
    fun clear(targetWebView: WebView, clearSharedStores: Boolean) {
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
}
