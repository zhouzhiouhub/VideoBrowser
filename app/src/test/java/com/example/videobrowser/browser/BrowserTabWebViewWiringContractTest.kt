package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Browser Tab Web View Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
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
        val tabActionsController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserTabActionsController.kt"
        ).readText()

        assertTrue(mainActivity.contains("private lateinit var browserSurface: BrowserWebViewSurfaceComponents"))
        assertTrue(webViewSurfaceAssembly.contains("browserStandardWebViewHostController.setup()"))
        assertTrue(standardWebViewHostController.contains("BrowserTabWebViewRegistry<WebView>"))
        assertTrue(standardWebViewHostController.contains("fun createStandardTabWebView(): WebView"))
        assertTrue(standardWebViewHostController.contains("fun showStandardTabWebView(tabWebView: WebView, detachCurrent: Boolean)"))
        assertTrue(standardWebViewHostController.contains("fun hideStandardTabWebView(tabWebView: WebView)"))
        assertTrue(standardWebViewHostController.contains("fun destroyStandardTabWebView(tabWebView: WebView)"))
        assertTrue(mainActivity.contains("private lateinit var browserSessions: BrowserSessionComponents"))
        assertTrue(mainActivity.contains("browserTabActionsController = browserSessions.browserTabActionsController"))
        assertTrue(tabActionsController.contains("standardTabWebViews.openTab"))
        assertTrue(tabActionsController.contains("standardTabWebViews.switchTo"))
        assertTrue(tabActionsController.contains("standardTabWebViews.closeTab"))
        assertTrue(tabActionsController.contains("standardTabWebViews.closeOtherTabs"))
        assertTrue(tabActionsController.contains("standardTabWebViews.openTab("))
        assertTrue(mainActivity.contains("createStandardTabWebView"))
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

        assertTrue(mainActivity.contains("private lateinit var browserSurface: BrowserWebViewSurfaceComponents"))
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
        assertTrue(reopenClosedTabBody.contains("reopenedTab.url?.let(loadUrl) ?: openHomePage()"))
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

        assertTrue(duplicateTabBody.contains("standardTabWebViews.openTab("))
        assertTrue(duplicateTabBody.contains("view = createStandardTabWebView()"))
        assertTrue(duplicateTabBody.contains("url = sourceTab.url"))
        assertTrue(duplicateTabBody.contains("title = sourceTab.title"))
        assertTrue(duplicateTabBody.contains("sourceTab.url?.let(loadUrl) ?: openHomePage()"))
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
            .substringBefore("private fun showActiveTab")

        assertTrue(openUrlInNewTabBody.contains("standardTabWebViews.openTab("))
        assertTrue(openUrlInNewTabBody.contains("view = createStandardTabWebView()"))
        assertTrue(openUrlInNewTabBody.contains("url = url"))
        assertTrue(openUrlInNewTabBody.contains("loadUrl(url)"))
    }

    /**
     * 测试函数 `projectFile`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `project File` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param path 参数类型为 `String`，表示函数执行 `path` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
