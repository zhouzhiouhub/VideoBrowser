package com.example.videobrowser.browser

import android.webkit.WebView

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器 WebView 导航模块”。
 * 文件名 BrowserWebViewNavigationController 可以拆开理解为“Browser WebView Navigation Controller”，表示它只负责页面加载、错误页、前进后退和基础页面状态读取。
 * 主要职责：把 load/goBack/goForward/reload/stopLoading/currentUrl/userAgentString 从 BrowserManager 中集中出来。
 * 阅读顺序：先看 load/loadErrorPage，再看 goBack/goForward/reload。
 */
internal class BrowserWebViewNavigationController(
    private val webView: () -> WebView
) {
    fun load(url: String) {
        runWithSuspendedCurrentPage { targetWebView ->
            targetWebView.loadUrl(url)
        }
    }

    fun loadErrorPage(error: BrowserPageError) {
        val targetWebView = webView()
        BrowserPageLifecycleScriptController.disposeCurrentPage(targetWebView)
        targetWebView.loadDataWithBaseURL(
            "about:blank",
            BrowserErrorPage.render(error),
            "text/html",
            "UTF-8",
            null
        )
    }

    fun goBack(): Boolean {
        val targetWebView = webView()
        if (!targetWebView.canGoBack()) {
            return false
        }
        runWithSuspendedCurrentPage(targetWebView) { suspendedWebView ->
            suspendedWebView.goBack()
        }
        return true
    }

    fun goForward(): Boolean {
        val targetWebView = webView()
        if (!targetWebView.canGoForward()) {
            return false
        }
        runWithSuspendedCurrentPage(targetWebView) { suspendedWebView ->
            suspendedWebView.goForward()
        }
        return true
    }

    fun reload() {
        runWithSuspendedCurrentPage { targetWebView ->
            targetWebView.reload()
        }
    }

    fun stopLoading() {
        webView().stopLoading()
    }

    fun canGoBack(): Boolean {
        return webView().canGoBack()
    }

    fun canGoForward(): Boolean {
        return webView().canGoForward()
    }

    fun currentUrl(): String? {
        return webView().url
    }

    fun userAgentString(): String? {
        return webView().settings.userAgentString
    }

    private fun runWithSuspendedCurrentPage(
        targetWebView: WebView = webView(),
        action: (WebView) -> Unit
    ) {
        BrowserPageLifecycleScriptController.suspendCurrentPage(targetWebView)
        action(targetWebView)
    }
}
