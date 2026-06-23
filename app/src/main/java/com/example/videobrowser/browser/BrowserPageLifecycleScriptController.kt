package com.example.videobrowser.browser

import android.webkit.WebView
import com.example.videobrowser.utils.WebViewEnhancerScript

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

    private val PAGE_SUSPEND_SCRIPT = WebViewEnhancerScript.call(
        "suspend",
        "{pauseVideos:true}"
    )
    private val PAGE_DISPOSE_SCRIPT = WebViewEnhancerScript.call(
        "dispose",
        "{pauseVideos:true}"
    )
}
