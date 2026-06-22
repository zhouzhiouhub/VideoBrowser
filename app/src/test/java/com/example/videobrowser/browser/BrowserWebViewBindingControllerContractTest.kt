package com.example.videobrowser.browser

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserWebViewBindingControllerContractTest {
    @Test
    fun browserManagerDelegatesWebViewBindingStateToController() {
        val browserManager = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserManager.kt"
        ).readText()
        val bindingController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserWebViewBindingController.kt"
        ).readText()
        val callbackCleaner = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserWebViewCallbackCleaner.kt"
        ).readText()
        val webViewSwitcher = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserWebViewSwitcher.kt"
        ).readText()

        assertTrue(browserManager.contains("BrowserWebViewBindingController("))
        assertTrue(browserManager.contains("BrowserWebViewSwitcher("))
        assertTrue(webViewSwitcher.contains("bindingController.detachFrom(currentWebView)"))
        assertTrue(webViewSwitcher.contains("bindingController.attachToCurrentWebView()"))
        assertTrue(browserManager.contains("webViewBindingController.setChromeClient(client)"))
        assertTrue(browserManager.contains("webViewBindingController.setBrowserClient(client)"))
        assertTrue(browserManager.contains("webViewBindingController.setDownloadListener(listener)"))
        assertTrue(browserManager.contains("webViewBindingController.addJavascriptInterface(interfaceObject, name)"))
        assertFalse(browserManager.contains("private val javascriptInterfaces"))
        assertFalse(browserManager.contains("private var chromeClient"))
        assertFalse(browserManager.contains("private var browserClient"))
        assertFalse(browserManager.contains("private var downloadListener"))

        assertTrue(bindingController.contains("private val javascriptInterfaces"))
        assertTrue(bindingController.contains("private var chromeClient"))
        assertTrue(bindingController.contains("private var browserClient"))
        assertTrue(bindingController.contains("private var downloadListener"))
        assertTrue(bindingController.contains("BrowserWebViewCallbackCleaner.detachCallbacks(targetWebView)"))
        assertFalse(bindingController.contains("targetWebView.webChromeClient = null"))
        assertFalse(bindingController.contains("targetWebView.webViewClient = WebViewClient()"))
        assertFalse(bindingController.contains("targetWebView.setDownloadListener(null)"))
        assertTrue(bindingController.contains("targetWebView.addJavascriptInterface(binding.interfaceObject, binding.name)"))

        assertTrue(callbackCleaner.contains("internal object BrowserWebViewCallbackCleaner"))
        assertTrue(callbackCleaner.contains("targetWebView.webChromeClient = null"))
        assertTrue(callbackCleaner.contains("targetWebView.webViewClient = WebViewClient()"))
        assertTrue(callbackCleaner.contains("targetWebView.setDownloadListener(null)"))
    }

}
