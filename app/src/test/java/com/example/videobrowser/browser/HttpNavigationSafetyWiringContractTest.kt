package com.example.videobrowser.browser

import com.example.videobrowser.testutil.projectFile

/**
 * 测试阅读提示：
 * 这个测试文件验证“Http Navigation Safety Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HttpNavigationSafetyWiringContractTest {
    /**
     * 测试函数 `mainActivityConfirmsHttpsToHttpTopLevelNavigation`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `main Activity Confirms Https To Http Top Level Navigation` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun mainActivityConfirmsHttpsToHttpTopLevelNavigation() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt").readText()
        val coreFeatureAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserCoreFeatureAssemblyController.kt"
        ).readText()
        val navigationController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserNavigationController.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(mainActivity.contains("private lateinit var browserFeatures: BrowserActivityFeatureComponents"))
        assertTrue(coreFeatureAssembly.contains("browserNavigationController = browserNavigation.browserNavigationController"))
        assertTrue(navigationController.contains("HttpNavigationSafetyPolicy.requiresInsecureNavigationConfirmation"))
        assertTrue(navigationController.contains("private fun loadUrlInternal(url: String, allowInsecureNavigation: Boolean)"))
        assertTrue(navigationController.contains("fun loadUrlAfterInsecureNavigationConfirmation(url: String)"))
        assertTrue(navigationController.contains("private fun showInsecureNavigationConfirmation(url: String)"))
        assertTrue(navigationController.contains("view?.stopLoading()"))
        assertTrue(navigationController.contains("ConfirmationDialog.show("))
        assertEquals(1, Regex("ConfirmationDialog\\.show\\(").findAll(navigationController).count())
        assertFalse(navigationController.contains("AlertDialog.Builder(activity)"))
        assertTrue(navigationController.contains("R.string.title_confirm_insecure_navigation"))
        assertTrue(navigationController.contains("R.string.dialog_confirm_insecure_navigation_message"))
        assertTrue(strings.contains("title_confirm_insecure_navigation"))
        assertTrue(strings.contains("dialog_confirm_insecure_navigation_message"))
        assertTrue(readme.contains("HTTPS 页面跳转到 HTTP 明文页面前会先确认"))
    }

}
