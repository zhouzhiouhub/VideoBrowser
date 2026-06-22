package com.example.videobrowser.browser

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserWebViewLifecycleControllerContractTest {
    @Test
    fun browserManagerDelegatesWebViewCleanupAndDestroyLifecycle() {
        val browserManager = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserManager.kt"
        ).readText()
        val lifecycleController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserWebViewLifecycleController.kt"
        ).readText()
        val callbackCleaner = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserWebViewCallbackCleaner.kt"
        ).readText()

        assertTrue(browserManager.contains("BrowserWebViewLifecycleController(webViewSettings)"))
        assertTrue(browserManager.contains("webViewLifecycle.clearBrowsingData(webView, clearSharedStores)"))
        assertTrue(browserManager.contains("webViewLifecycle.clearCache(webView)"))
        assertTrue(browserManager.contains("webViewLifecycle.destroyWebView("))
        assertTrue(browserManager.contains("webViewLifecycle.clearTransientBrowsingData(webView)"))
        assertFalse(browserManager.contains("BrowserWebViewDataCleaner.clear(targetWebView"))
        assertFalse(browserManager.contains("targetWebView.removeAllViews()"))

        assertTrue(lifecycleController.contains("BrowserWebViewDataCleaner.clear(targetWebView, clearSharedStores)"))
        assertTrue(lifecycleController.contains("targetWebView.clearCache(true)"))
        assertTrue(lifecycleController.contains("BrowserWebViewCallbackCleaner.detachCallbacks(targetWebView)"))
        assertTrue(lifecycleController.contains("BrowserPageLifecycleScriptController.disposeCurrentPage(activeWebView)"))
        assertTrue(lifecycleController.contains("targetWebView.stopLoading()"))
        assertTrue(lifecycleController.contains("targetWebView.loadUrl(\"about:blank\")"))
        assertTrue(lifecycleController.contains("targetWebView.removeAllViews()"))
        assertTrue(lifecycleController.contains("webViewSettings.forget(targetWebView)"))
        assertTrue(lifecycleController.contains("targetWebView.destroy()"))
        assertTrue(lifecycleController.contains("clearBrowsingData(targetWebView, clearSharedStores = false)"))

        assertTrue(callbackCleaner.contains("targetWebView.webChromeClient = null"))
        assertTrue(callbackCleaner.contains("targetWebView.webViewClient = WebViewClient()"))
        assertTrue(callbackCleaner.contains("targetWebView.setDownloadListener(null)"))
    }

}
