package com.example.videobrowser.browser

import android.webkit.WebView

internal object BrowserPageLifecycleScriptController {
    fun suspendCurrentPage(webView: WebView) {
        if (webView.url.isNullOrBlank()) {
            return
        }
        webView.evaluateJavascript(PAGE_SUSPEND_SCRIPT, null)
    }

    fun disposeCurrentPage(webView: WebView) {
        if (webView.url.isNullOrBlank()) {
            return
        }
        webView.evaluateJavascript(PAGE_DISPOSE_SCRIPT, null)
    }

    private const val PAGE_SUSPEND_SCRIPT =
        "if(window.VideoBrowserEnhancer&&typeof window.VideoBrowserEnhancer.suspend==='function'){" +
            "window.VideoBrowserEnhancer.suspend({pauseVideos:true});" +
            "}"
    private const val PAGE_DISPOSE_SCRIPT =
        "if(window.VideoBrowserEnhancer&&typeof window.VideoBrowserEnhancer.dispose==='function'){" +
            "window.VideoBrowserEnhancer.dispose({pauseVideos:true});" +
            "}"
}
