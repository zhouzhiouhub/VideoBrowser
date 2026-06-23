package com.example.videobrowser.browser

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserWebViewNavigationControllerContractTest {
    @Test
    fun browserManagerDelegatesWebViewNavigationToController() {
        val browserManager = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserManager.kt"
        ).readText()
        val navigationController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserWebViewNavigationController.kt"
        ).readText()

        assertTrue(browserManager.contains("BrowserWebViewNavigationController("))
        assertTrue(browserManager.contains("webViewNavigationController.load(url)"))
        assertTrue(browserManager.contains("webViewNavigationController.loadErrorPage(error)"))
        assertTrue(browserManager.contains("return webViewNavigationController.goBack()"))
        assertTrue(browserManager.contains("return webViewNavigationController.goForward()"))
        assertTrue(browserManager.contains("webViewNavigationController.reload()"))
        assertTrue(browserManager.contains("webViewNavigationController.stopLoading()"))
        assertTrue(browserManager.contains("return webViewNavigationController.currentUrl()"))
        assertTrue(browserManager.contains("return webViewNavigationController.userAgentString()"))
        assertFalse(browserManager.contains("BrowserErrorPage.render(error)"))
        assertFalse(browserManager.contains("BrowserPageLifecycleScriptController.suspendCurrentPage(webView)"))

        assertTrue(navigationController.contains("private fun runWithSuspendedCurrentPage("))
        assertTrue(navigationController.contains("BrowserPageLifecycleScriptController.suspendCurrentPage(targetWebView)"))
        assertEquals(
            1,
            Regex("BrowserPageLifecycleScriptController\\.suspendCurrentPage")
                .findAll(navigationController)
                .count()
        )
        assertTrue(navigationController.contains("BrowserPageLifecycleScriptController.disposeCurrentPage"))
        assertTrue(navigationController.contains("BrowserErrorPage.render(error)"))
        assertTrue(navigationController.contains("suspendedWebView.goBack()"))
        assertTrue(navigationController.contains("suspendedWebView.goForward()"))
        assertTrue(navigationController.contains("targetWebView.reload()"))
        assertTrue(navigationController.contains("webView().stopLoading()"))
        assertTrue(navigationController.contains("webView().settings.userAgentString"))
    }

}
