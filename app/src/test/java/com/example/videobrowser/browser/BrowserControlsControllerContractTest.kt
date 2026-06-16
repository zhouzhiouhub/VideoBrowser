package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Browser Controls Controller Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserControlsControllerContractTest {
    @Test
    fun refreshButtonStopsLoadingWhilePageIsLoading() {
        val controller = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserControlsController.kt"
        ).readText()
        val browserManager = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserManager.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val stopIcon = projectFile("src/main/res/drawable/ic_stop_24.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(browserManager.contains("fun stopLoading()"))
        assertTrue(browserManager.contains("webView.stopLoading()"))
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

    @Test
    fun backButtonUsesUnifiedActivityBackNavigation() {
        val controller = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserControlsController.kt"
        ).readText()
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(controller.contains("private val onBack: () -> Unit"))
        assertTrue(controller.contains("backButton.setOnClickListener { onBack() }"))
        assertTrue(controller.contains("backButton.isEnabled = visibility.showBack"))
        assertTrue(mainActivity.contains("onBack = ::handleBrowserBack"))
        assertTrue(mainActivity.contains("override fun handleOnBackPressed()"))
        assertTrue(mainActivity.contains("private fun handleBrowserBack()"))
        assertTrue(mainActivity.contains("currentBrowserManager().goBack()"))
        assertTrue(mainActivity.contains("private fun confirmExitOnSecondBack()"))
        assertTrue(mainActivity.contains("BACK_EXIT_CONFIRM_WINDOW_MS"))
        assertTrue(mainActivity.contains("SystemClock.elapsedRealtime()"))
        assertTrue(mainActivity.contains("R.string.toast_press_back_again_to_exit"))
        assertTrue(strings.contains("toast_press_back_again_to_exit"))
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
