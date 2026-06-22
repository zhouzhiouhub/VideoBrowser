package com.example.videobrowser.browser

import android.webkit.WebView

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器页内查找模块”。
 * 文件名 BrowserWebViewFindController 可以拆开理解为“Browser WebView Find Controller”，表示它只负责 WebView 查找接口和查找结果监听。
 * 主要职责：把 findAllAsync/findNext/clearMatches/setFindResultListener 从 BrowserManager 中集中出来。
 * 阅读顺序：先看构造参数，再看四个公开函数如何转发到当前 WebView 或 WebView 设置控制器。
 */
internal class BrowserWebViewFindController(
    private val webView: () -> WebView,
    private val webViewSettings: BrowserWebViewSettingsController
) {
    fun setFindResultListener(listener: ((Int, Int, Boolean) -> Unit)?) {
        webViewSettings.setFindResultListener(listener)
    }

    fun findAllAsync(query: String) {
        webView().findAllAsync(query)
    }

    fun findNext(forward: Boolean = true) {
        webView().findNext(forward)
    }

    fun clearFindMatches() {
        webView().clearMatches()
    }
}
