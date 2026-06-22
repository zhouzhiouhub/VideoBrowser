package com.example.videobrowser.browser

import android.webkit.WebView

internal object BrowserWebViewScriptEvaluator {
    fun evaluate(targetWebView: WebView, script: String) {
        targetWebView.evaluateJavascript(script, null)
    }
}
