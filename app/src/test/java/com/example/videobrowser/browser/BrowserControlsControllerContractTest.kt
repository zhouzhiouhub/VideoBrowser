package com.example.videobrowser.browser

import com.example.videobrowser.testutil.projectFile

/**
 * 测试阅读提示：
 * 这个测试文件验证“Browser Controls Controller Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserControlsControllerContractTest {
    /**
     * 测试函数 `refreshButtonStopsLoadingWhilePageIsLoading`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `refresh Button Stops Loading While Page Is Loading` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun refreshButtonStopsLoadingWhilePageIsLoading() {
        val controller = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserControlsController.kt"
        ).readText()
        val browserManager = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserManager.kt"
        ).readText()
        val navigationController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserWebViewNavigationController.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val stopIcon = projectFile("src/main/res/drawable/ic_stop_24.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(browserManager.contains("fun stopLoading()"))
        assertTrue(browserManager.contains("webViewNavigationController.stopLoading()"))
        assertTrue(navigationController.contains("webView().stopLoading()"))
        assertTrue(controller.contains("private var isPageLoading = false"))
        assertTrue(controller.contains("if (isPageLoading)"))
        assertTrue(controller.contains("browserManager().stopLoading()"))
        assertTrue(controller.contains("browserManager().reload()"))
        assertTrue(controller.contains("this.isPageLoading = isPageLoading"))
        assertTrue(controller.contains("refreshButton.visibility = if (visibility.showRefresh) View.VISIBLE else View.GONE"))
        assertTrue(controller.contains("private fun updateRefreshButton()"))
        assertTrue(controller.contains("R.string.action_stop_loading"))
        assertTrue(controller.contains("R.drawable.ic_stop_24"))
        assertTrue(controller.contains("R.id.refreshButton"))
        assertTrue(controller.contains("bottomBar.findViewById<View>(id).visibility == View.VISIBLE"))
        assertTrue(strings.contains("action_stop_loading"))
        assertTrue(stopIcon.contains("M6,6H18V18H6Z"))
        assertTrue(readme.contains("加载中可停止加载"))
    }

    /**
     * 测试函数 `backButtonUsesUnifiedActivityBackNavigation`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `back Button Uses Unified Activity Back Navigation` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun backButtonUsesUnifiedActivityBackNavigation() {
        val controller = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserControlsController.kt"
        ).readText()
        val backNavigationController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserBackNavigationController.kt"
        ).readText()
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val startupController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserStartupController.kt"
        ).readText()
        val startupFeatureAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserStartupFeatureAssemblyController.kt"
        ).readText()
        val runtimeFeatureAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserRuntimeFeatureAssemblyController.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(controller.contains("private val onBack: () -> Unit"))
        assertTrue(controller.contains("backButton.setOnClickListener { onBack() }"))
        assertTrue(controller.contains("backButton.isEnabled = visibility.showBack"))
        assertTrue(runtimeFeatureAssembly.contains("requireStartupFeatures().browserBackNavigationController.handleBrowserBack()"))
        assertTrue(mainActivity.contains("BrowserActivityFeatureAssemblyController"))
        assertTrue(startupFeatureAssembly.contains("BrowserStartupControllerAssembly"))
        assertTrue(startupController.contains("browserBackNavigationController.setupBackNavigation()"))
        assertTrue(runtimeFeatureAssembly.contains("browserBackNavigationController.handleBrowserBack()"))
        assertTrue(backNavigationController.contains("override fun handleOnBackPressed()"))
        assertTrue(!mainActivity.contains("private fun handleBrowserBack()"))
        assertTrue(backNavigationController.contains("browserManager().goBack()"))
        assertTrue(backNavigationController.contains("private fun confirmExitOnSecondBack()"))
        assertTrue(backNavigationController.contains("BACK_EXIT_CONFIRM_WINDOW_MS"))
        assertTrue(backNavigationController.contains("SystemClock.elapsedRealtime()"))
        assertTrue(backNavigationController.contains("R.string.toast_press_back_again_to_exit"))
        assertTrue(strings.contains("toast_press_back_again_to_exit"))
    }

}
