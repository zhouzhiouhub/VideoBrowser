package com.example.videobrowser.browser

import com.example.videobrowser.testutil.projectFile

/**
 * 测试阅读提示：
 * 这个测试文件验证“Browser Tab Web View Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserTabWebViewWiringContractTest {
    /**
     * 测试函数 `mainActivityUsesTabWebViewRegistryForStandardTabs`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `main Activity Uses Tab Web View Registry For Standard Tabs` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun mainActivityUsesTabWebViewRegistryForStandardTabs() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val standardWebViewHostController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserStandardWebViewHostController.kt"
        ).readText()
        val webViewSurfaceAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserWebViewSurfaceAssemblyController.kt"
        ).readText()
        val startupFeatureAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserStartupFeatureAssemblyController.kt"
        ).readText()
        val runtimeFeatureAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserRuntimeFeatureAssemblyController.kt"
        ).readText()
        val tabActionsController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserTabActionsController.kt"
        ).readText()

        assertTrue(mainActivity.contains("private lateinit var browserFeatures: BrowserActivityFeatureComponents"))
        assertTrue(webViewSurfaceAssembly.contains("browserStandardWebViewHostController.setup()"))
        assertTrue(standardWebViewHostController.contains("BrowserTabWebViewRegistry<WebView>"))
        assertTrue(standardWebViewHostController.contains("fun createStandardTabWebView(): WebView"))
        assertTrue(standardWebViewHostController.contains("fun showStandardTabWebView(tabWebView: WebView, detachCurrent: Boolean)"))
        assertTrue(standardWebViewHostController.contains("fun hideStandardTabWebView(tabWebView: WebView)"))
        assertTrue(standardWebViewHostController.contains("fun destroyStandardTabWebView(tabWebView: WebView)"))
        assertTrue(mainActivity.contains("private lateinit var browserFeatures: BrowserActivityFeatureComponents"))
        assertTrue(startupFeatureAssembly.contains("browserTabActionsController = browserSessions.browserTabActionsController"))
        assertTrue(tabActionsController.contains("standardTabWebViews.openTab"))
        assertTrue(tabActionsController.contains("private fun openStandardTab(url: String? = null, title: String = \"\")"))
        assertTrue(tabActionsController.contains("standardTabWebViews.switchTo"))
        assertTrue(tabActionsController.contains("standardTabWebViews.closeTab"))
        assertTrue(tabActionsController.contains("standardTabWebViews.closeOtherTabs"))
        assertTrue(runtimeFeatureAssembly.contains("createStandardTabWebView"))
    }

    /**
     * 测试函数 `activeWebViewChangesAreDelegatedToController`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `active Web View Changes Are Delegated To Controller` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun activeWebViewChangesAreDelegatedToController() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val webViewInteractionAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserWebViewInteractionAssemblyController.kt"
        ).readText()
        val webViewSurfaceAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserWebViewSurfaceAssemblyController.kt"
        ).readText()
        val activeWebViewController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserActiveWebViewController.kt"
        ).readText()
        val standardWebViewHostController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserStandardWebViewHostController.kt"
        ).readText()

        assertTrue(mainActivity.contains("private lateinit var browserFeatures: BrowserActivityFeatureComponents"))
        assertTrue(webViewInteractionAssembly.contains("val browserActiveWebViewController = BrowserActiveWebViewController("))
        assertTrue(webViewSurfaceAssembly.contains("webViewInteraction.browserActiveWebViewController::handleActiveWebViewChanged"))
        assertTrue(standardWebViewHostController.contains("onActiveWebViewChanged = handleActiveWebViewChanged"))
        assertTrue(standardWebViewHostController.contains("handleActiveWebViewChanged(tabWebView, BrowserMode.STANDARD)"))
        assertTrue(activeWebViewController.contains("fun handleActiveWebViewChanged(activeWebView: WebView, mode: BrowserMode)"))
        assertTrue(activeWebViewController.contains("setPrivateBrowsingActive(mode == BrowserMode.PRIVATE)"))
        assertTrue(activeWebViewController.contains("attachBrowserControlsScrollIfReady(activeWebView)"))
        assertTrue(activeWebViewController.contains("currentSessionController().renderCurrentState()"))
    }

    /**
     * 测试函数 `switchTabDoesNotReloadExistingTabUrl`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `switch Tab Does Not Reload Existing Tab Url` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun switchTabDoesNotReloadExistingTabUrl() {
        val tabActionsController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserTabActionsController.kt"
        )
            .readText()
        val switchTabBody = tabActionsController.substringAfter("fun switchTab(tabId: Long)")
            .substringBefore("fun closeTab")

        assertFalse(switchTabBody.contains("loadUrl"))
        assertTrue(switchTabBody.contains("showActiveTab"))
    }

    /**
     * 测试函数 `reopenClosedTabCreatesStandardWebViewForRestoredTab`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `reopen Closed Tab Creates Standard Web View For Restored Tab` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun reopenClosedTabCreatesStandardWebViewForRestoredTab() {
        val tabActionsController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserTabActionsController.kt"
        )
            .readText()
        val reopenClosedTabBody = tabActionsController.substringAfter("fun reopenClosedTab()")
            .substringBefore("fun switchTab")

        assertTrue(reopenClosedTabBody.contains("standardTabStore.reopenClosedTab()"))
        assertTrue(reopenClosedTabBody.contains("standardTabWebViews.activate(reopenedTab.id)"))
        assertTrue(reopenClosedTabBody.contains("saveStandardTabSession()"))
        assertTrue(reopenClosedTabBody.contains("loadTabUrlOrHome(reopenedTab)"))
    }

    /**
     * 测试函数 `duplicateTabCreatesIndependentStandardWebView`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `duplicate Tab Creates Independent Standard Web View` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun duplicateTabCreatesIndependentStandardWebView() {
        val tabActionsController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserTabActionsController.kt"
        )
            .readText()
        val duplicateTabBody = tabActionsController.substringAfter("fun duplicateTab(tabId: Long)")
            .substringBefore("fun openUrlInNewTab")

        assertTrue(duplicateTabBody.contains("openStandardTab("))
        assertTrue(duplicateTabBody.contains("url = sourceTab.url"))
        assertTrue(duplicateTabBody.contains("title = sourceTab.title"))
        assertTrue(duplicateTabBody.contains("loadTabUrlOrHome(sourceTab)"))
    }

    /**
     * 测试函数 `openUrlInNewTabCreatesIndependentStandardWebView`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `open Url In New Tab Creates Independent Standard Web View` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun openUrlInNewTabCreatesIndependentStandardWebView() {
        val tabActionsController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserTabActionsController.kt"
        )
            .readText()
        val openUrlInNewTabBody = tabActionsController.substringAfter("fun openUrlInNewTab(url: String)")
            .substringBefore("private fun openStandardTab")

        assertTrue(openUrlInNewTabBody.contains("openStandardTab(url = url)"))
        assertTrue(openUrlInNewTabBody.contains("loadUrl(url)"))
    }

    @Test
    fun standardTabCreationIsOwnedBySharedHelper() {
        val tabActionsController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserTabActionsController.kt"
        ).readText()
        val openNewTabBody = tabActionsController.substringAfter("fun openNewTab()")
            .substringBefore("fun canReopenClosedTab")
        val helperBody = tabActionsController.substringAfter("private fun openStandardTab")
            .substringBefore("private fun showActiveTab")

        assertTrue(openNewTabBody.contains("openStandardTab()"))
        assertTrue(helperBody.contains("standardTabWebViews.openTab("))
        assertTrue(helperBody.contains("view = createStandardTabWebView()"))
        assertTrue(helperBody.contains("hideStandardTabWebView(result.previousView)"))
        assertTrue(helperBody.contains("showStandardTabWebView(result.activeView)"))
        assertTrue(helperBody.contains("saveStandardTabSession()"))
        assertFalse(openNewTabBody.contains("standardTabWebViews.openTab("))
    }

    @Test
    fun tabUrlLoadingIsOwnedBySharedHelper() {
        val tabActionsController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserTabActionsController.kt"
        ).readText()
        val helperBody = tabActionsController.substringAfter("private fun loadTabUrlOrHome(tab: BrowserTab)")
            .substringBefore("private fun showActiveTab")
        val showActiveTabBody = tabActionsController.substringAfter("private fun showActiveTab(tab: BrowserTab)")
            .substringBefore("private fun currentTabStore")

        assertTrue(helperBody.contains("tab.url?.let(loadUrl) ?: openHomePage()"))
        assertTrue(showActiveTabBody.contains("loadTabUrlOrHome(tab)"))
        assertFalse(showActiveTabBody.contains("tab.url?.let(loadUrl) ?: openHomePage()"))
    }

}
