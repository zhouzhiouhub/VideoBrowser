package com.example.videobrowser.browser

import android.webkit.WebView

internal object BrowserWebViewScriptEvaluator {
    fun evaluate(targetWebView: WebView, script: String, onComplete: (() -> Unit)? = null) {
        targetWebView.evaluateJavascript(script) {
            onComplete?.invoke()
        }
    }
}
