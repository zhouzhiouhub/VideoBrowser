package com.example.videobrowser.browser

import android.webkit.WebView

internal class BrowserWebViewSwitcher(
    private val activeWebView: () -> WebView,
    private val setActiveWebView: (WebView) -> Unit,
    private val bindingController: BrowserWebViewBindingController,
    private val setupWebView: (WebView) -> Unit,
    private val setPrivateBrowsingEnabled: (Boolean, WebView) -> Unit
) {
    fun switchWebView(
        nextWebView: WebView,
        privateBrowsingEnabled: Boolean,
        detachCurrent: Boolean = true
    ) {
        val currentWebView = activeWebView()
        if (currentWebView === nextWebView) {
            setPrivateBrowsingEnabled(privateBrowsingEnabled, currentWebView)
            return
        }

        if (detachCurrent) {
            bindingController.detachFrom(currentWebView)
        }

        setActiveWebView(nextWebView)
        setupWebView(nextWebView)
        setPrivateBrowsingEnabled(privateBrowsingEnabled, nextWebView)
        bindingController.attachToCurrentWebView()
    }
}
