package com.example.videobrowser.browser

import android.webkit.WebView

internal object BrowserPageLifecycleScriptController {
    fun suspendCurrentPage(webView: WebView) {
        if (webView.url.isNullOrBlank()) {
            return
        }
        BrowserWebViewScriptEvaluator.evaluate(webView, PAGE_SUSPEND_SCRIPT)
    }

    fun disposeCurrentPage(webView: WebView) {
        if (webView.url.isNullOrBlank()) {
            return
        }
        BrowserWebViewScriptEvaluator.evaluate(webView, PAGE_DISPOSE_SCRIPT)
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
