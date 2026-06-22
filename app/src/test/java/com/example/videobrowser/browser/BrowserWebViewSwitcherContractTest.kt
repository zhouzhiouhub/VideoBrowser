package com.example.videobrowser.browser

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserWebViewSwitcherContractTest {
    @Test
    fun browserManagerDelegatesWebViewSwitchingToSwitcher() {
        val browserManager = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserManager.kt"
        ).readText()
        val webViewSwitcher = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserWebViewSwitcher.kt"
        ).readText()
        val settingsController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserWebViewSettingsController.kt"
        ).readText()

        assertTrue(browserManager.contains("BrowserWebViewSwitcher("))
        assertTrue(browserManager.contains("webViewSwitcher.switchWebView("))
        assertTrue(browserManager.contains("privateBrowsingEnabled: Boolean = webViewSettings.isPrivateBrowsingEnabled"))
        assertTrue(browserManager.contains("webViewSettings.setPrivateBrowsingEnabled(enabled, webView)"))
        assertFalse(browserManager.contains("private var privateBrowsingEnabled"))
        assertFalse(browserManager.contains("webViewBindingController.detachFrom(webView)"))
        assertFalse(browserManager.contains("webViewBindingController.attachToCurrentWebView()"))

        assertTrue(webViewSwitcher.contains("bindingController.detachFrom(currentWebView)"))
        assertTrue(webViewSwitcher.contains("setActiveWebView(nextWebView)"))
        assertTrue(webViewSwitcher.contains("setupWebView(nextWebView)"))
        assertTrue(webViewSwitcher.contains("setPrivateBrowsingEnabled(privateBrowsingEnabled, nextWebView)"))
        assertTrue(webViewSwitcher.contains("bindingController.attachToCurrentWebView()"))
        assertFalse(webViewSwitcher.contains("private var privateBrowsingEnabled"))

        assertTrue(settingsController.contains("val isPrivateBrowsingEnabled: Boolean"))
        assertTrue(settingsController.contains("private var privateBrowsingEnabled = false"))
    }

}
