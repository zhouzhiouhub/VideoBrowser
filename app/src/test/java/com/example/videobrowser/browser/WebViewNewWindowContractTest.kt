package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Web View New Window Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class WebViewNewWindowContractTest {
    @Test
    fun browserManagerAllowsUserGesturePopupWindowsOnly() {
        val browserManager = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserManager.kt"
        ).readText()

        assertTrue(browserManager.contains("javaScriptCanOpenWindowsAutomatically = false"))
        assertTrue(browserManager.contains("setSupportMultipleWindows(true)"))
    }

    @Test
    fun chromeClientForwardsCreateWindowRequests() {
        val chromeClient = projectFile(
            "src/main/java/com/example/videobrowser/browser/ChromeClient.kt"
        ).readText()

        assertTrue(chromeClient.contains("import android.os.Message"))
        assertTrue(chromeClient.contains("newWindowRequested: (WebView?, Boolean, Boolean, Message?) -> Boolean"))
        assertTrue(chromeClient.contains("windowClosed: (WebView?) -> Unit"))
        assertTrue(chromeClient.contains("override fun onCreateWindow"))
        assertTrue(chromeClient.contains("return newWindowRequested(view, isDialog, isUserGesture, resultMsg)"))
        assertTrue(chromeClient.contains("override fun onCloseWindow"))
        assertTrue(chromeClient.contains("windowClosed(window)"))
    }

    @Test
    fun mainActivityRoutesNewWindowsIntoStandardTabsAndClosesRequestedPopupTabs() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val readme = projectFile("README.md").readText()

        assertTrue(mainActivity.contains("import android.os.Message"))
        assertTrue(mainActivity.contains("newWindowRequested = ::handleCreateWebWindow"))
        assertTrue(mainActivity.contains("windowClosed = ::handleCloseWebWindow"))
        assertTrue(mainActivity.contains("private fun handleCreateWebWindow"))
        assertTrue(mainActivity.contains("if (privateBrowsingActive || !isUserGesture)"))
        assertTrue(mainActivity.contains("val tab = standardTabStore.openTab()"))
        assertTrue(mainActivity.contains("val tabWebView = standardTabWebViews.activate(tab.id)"))
        assertTrue(mainActivity.contains("as? WebView.WebViewTransport"))
        assertTrue(mainActivity.contains("transport.webView = tabWebView"))
        assertTrue(mainActivity.contains("resultMsg?.sendToTarget()"))
        assertTrue(mainActivity.contains("private fun handleCloseWebWindow(window: WebView?)"))
        assertTrue(mainActivity.contains("standardTabWebViews.tabIdFor(window)"))
        assertTrue(mainActivity.contains("closeTab(tabId)"))
        assertTrue(readme.contains("网页弹窗标签可响应关闭请求"))
        assertTrue(readme.contains("非用户手势触发的新窗口请求会被拒绝"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOfNotNull(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path"),
            workingDirectory.parentFile?.let { parent -> File(parent, path) }
        ).first { it.exists() }
    }
}
