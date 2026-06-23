package com.example.videobrowser.browser

import android.webkit.WebView

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器 WebView 生命周期模块”。
 * 文件名 BrowserWebViewLifecycleController 可以拆开理解为“Browser WebView Lifecycle Controller”，表示它集中处理 WebView 临时数据清理和销毁流程。
 * 主要职责：把 WebView 停止加载、回收视图、清理缓存/站点数据、忘记设置状态这些步骤放在一个地方。
 * 阅读顺序：先看 clearTransientBrowsingData，再看 destroyWebView，最后看 clearBrowsingData 和 clearCache。
 */
internal class BrowserWebViewLifecycleController(
    private val webViewSettings: BrowserWebViewSettingsController
) {
    fun clearBrowsingData(targetWebView: WebView, clearSharedStores: Boolean = true) {
        BrowserWebViewDataCleaner.clear(targetWebView, clearSharedStores)
    }

    fun clearCache(targetWebView: WebView) {
        targetWebView.clearCache(true)
    }

    fun destroyWebView(
        targetWebView: WebView,
        activeWebView: WebView,
        clearSharedStores: Boolean = true
    ) {
        BrowserWebViewCallbackCleaner.detachCallbacks(targetWebView)
        if (targetWebView === activeWebView) {
            BrowserPageLifecycleScriptController.disposeCurrentPage(activeWebView)
        }
        clearLoadedPageData(targetWebView, clearSharedStores)
        targetWebView.removeAllViews()
        webViewSettings.forget(targetWebView)
        targetWebView.destroy()
    }

    fun clearTransientBrowsingData(targetWebView: WebView) {
        BrowserPageLifecycleScriptController.disposeCurrentPage(targetWebView)
        clearLoadedPageData(targetWebView, clearSharedStores = false)
    }

    private fun clearLoadedPageData(targetWebView: WebView, clearSharedStores: Boolean) {
        targetWebView.stopLoading()
        targetWebView.loadUrl("about:blank")
        clearBrowsingData(targetWebView, clearSharedStores)
    }
}
