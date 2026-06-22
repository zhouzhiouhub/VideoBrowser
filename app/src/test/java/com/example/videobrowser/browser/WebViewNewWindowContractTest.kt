package com.example.videobrowser.browser

import com.example.videobrowser.testutil.projectFile

/**
 * 测试阅读提示：
 * 这个测试文件验证“Web View New Window Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertTrue
import org.junit.Test

class WebViewNewWindowContractTest {
    /**
     * 测试函数 `browserManagerAllowsUserGesturePopupWindowsOnly`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `browser Manager Allows User Gesture Popup Windows Only` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun browserManagerAllowsUserGesturePopupWindowsOnly() {
        val browserManager = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserManager.kt"
        ).readText()
        val settingsController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserWebViewSettingsController.kt"
        ).readText()

        assertTrue(browserManager.contains("BrowserWebViewSettingsController()"))
        assertTrue(settingsController.contains("javaScriptCanOpenWindowsAutomatically = false"))
        assertTrue(settingsController.contains("setSupportMultipleWindows(true)"))
    }

    /**
     * 测试函数 `chromeClientForwardsCreateWindowRequests`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `chrome Client Forwards Create Window Requests` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `mainActivityRoutesNewWindowsIntoStandardTabsAndClosesRequestedPopupTabs`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `main Activity Routes New Windows Into Standard Tabs And Closes Requested Popup Tabs` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun mainActivityRoutesNewWindowsIntoStandardTabsAndClosesRequestedPopupTabs() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val startupFeatureAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserStartupFeatureAssemblyController.kt"
        ).readText()
        val chromeClientController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserChromeClientController.kt"
        ).readText()
        val webWindowController = projectFile(
            "src/main/java/com/example/videobrowser/browser/WebWindowController.kt"
        ).readText()
        val readme = projectFile("README.md").readText()

        assertTrue(mainActivity.contains("private lateinit var browserFeatures: BrowserActivityFeatureComponents"))
        assertTrue(startupFeatureAssembly.contains("browserChromeClientController = browserClients.browserChromeClientController"))
        assertTrue(chromeClientController.contains("newWindowRequested = webWindowController::handleCreateWebWindow"))
        assertTrue(chromeClientController.contains("windowClosed = webWindowController::handleCloseWebWindow"))
        assertTrue(chromeClientController.contains("private fun createChromeClient"))
        assertTrue(webWindowController.contains("if (isPrivateBrowsingActive() || !isUserGesture)"))
        assertTrue(webWindowController.contains("val tab = standardTabStore.openTab()"))
        assertTrue(webWindowController.contains("val tabWebView = standardTabWebViews.activate(tab.id)"))
        assertTrue(webWindowController.contains("as? WebView.WebViewTransport"))
        assertTrue(webWindowController.contains("transport.webView = tabWebView"))
        assertTrue(webWindowController.contains("resultMsg.sendToTarget()"))
        assertTrue(webWindowController.contains("standardTabWebViews.tabIdFor(window)"))
        assertTrue(webWindowController.contains("closeTab(tabId)"))
        assertTrue(readme.contains("网页弹窗标签可响应关闭请求"))
        assertTrue(readme.contains("非用户手势触发的新窗口请求会被拒绝"))
    }

}
