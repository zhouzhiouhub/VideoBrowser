package com.example.videobrowser.browser

import com.example.videobrowser.testutil.projectFile

/**
 * 测试阅读提示：
 * 这个测试文件验证“Browser Manager Web Settings Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserManagerWebSettingsContractTest {
    /**
     * 测试函数 `browserManagerEnablesPinchZoomWithoutOverlayControls`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `browser Manager Enables Pinch Zoom Without Overlay Controls` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun browserManagerEnablesPinchZoomWithoutOverlayControls() {
        val browserManager = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserManager.kt"
        ).readText()
        val settingsController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserWebViewSettingsController.kt"
        ).readText()
        val readme = projectFile("README.md").readText()

        assertTrue(browserManager.contains("BrowserWebViewSettingsController()"))
        assertTrue(settingsController.contains("setSupportZoom(true)"))
        assertTrue(settingsController.contains("builtInZoomControls = true"))
        assertTrue(settingsController.contains("displayZoomControls = false"))
        assertTrue(readme.contains("双指缩放网页"))
    }

    /**
     * 测试函数 `browserManagerEnablesSafeBrowsingOnSupportedAndroidVersions`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `browser Manager Enables Safe Browsing On Supported Android Versions` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun browserManagerEnablesSafeBrowsingOnSupportedAndroidVersions() {
        val browserManager = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserManager.kt"
        ).readText()
        val settingsController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserWebViewSettingsController.kt"
        ).readText()
        val readme = projectFile("README.md").readText()

        assertTrue(browserManager.contains("webViewSettings.setup(webView)"))
        assertTrue(settingsController.contains("import android.os.Build"))
        assertTrue(settingsController.contains("Build.VERSION.SDK_INT >= Build.VERSION_CODES.O"))
        assertTrue(settingsController.contains("safeBrowsingEnabled = true"))
        assertTrue(readme.contains("WebView Safe Browsing"))
    }

    /**
     * 测试函数 `browserManagerBlocksMixedContentByDefaultButAllowsCompatibilityModeSetting`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `browser Manager Blocks Mixed Content By Default But Allows Compatibility Mode Setting` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun browserManagerBlocksMixedContentByDefaultButAllowsCompatibilityModeSetting() {
        val browserManager = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserManager.kt"
        ).readText()
        val settingsController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserWebViewSettingsController.kt"
        ).readText()
        val readme = projectFile("README.md").readText()

        assertTrue(settingsController.contains("private var mixedContentBlocked = true"))
        assertTrue(browserManager.contains("fun setMixedContentBlocked(blocked: Boolean)"))
        assertTrue(browserManager.contains("webViewSettings.setMixedContentBlocked(blocked)"))
        assertTrue(settingsController.contains("private fun applyMixedContentMode(targetWebView: WebView)"))
        assertTrue(settingsController.contains("WebSettings.MIXED_CONTENT_NEVER_ALLOW"))
        assertTrue(settingsController.contains("WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE"))
        assertTrue(readme.contains("默认阻止 HTTPS 页面混合内容"))
    }

    /**
     * 测试函数 `browserManagerDisablesFileUrlLocalAccessByDefault`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `browser Manager Disables File Url Local Access By Default` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun browserManagerDisablesFileUrlLocalAccessByDefault() {
        val browserManager = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserManager.kt"
        ).readText()
        val settingsController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserWebViewSettingsController.kt"
        ).readText()
        val readme = projectFile("README.md").readText()

        assertTrue(browserManager.contains("webViewSettings.setup(webView)"))
        assertTrue(settingsController.contains("allowFileAccess = false"))
        assertTrue(settingsController.contains("allowFileAccessFromFileURLs = false"))
        assertTrue(settingsController.contains("allowUniversalAccessFromFileURLs = false"))
        assertTrue(readme.contains("默认禁止 `file://` 页面跨文件或跨来源访问本地资源"))
    }

    @Test
    fun desktopModeWebSettingsAreOwnedBySettingsController() {
        val browserManager = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserManager.kt"
        ).readText()
        val settingsController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserWebViewSettingsController.kt"
        ).readText()

        assertTrue(browserManager.contains("fun applyDesktopMode("))
        assertTrue(browserManager.contains("webViewSettings.applyDesktopMode("))
        assertTrue(settingsController.contains("fun applyDesktopMode("))
        assertTrue(settingsController.contains("webView.settings.userAgentString = if (enabled)"))
        assertTrue(settingsController.contains("webView.settings.useWideViewPort = enabled"))
        assertTrue(settingsController.contains("webView.settings.loadWithOverviewMode = enabled"))
        assertFalse(browserManager.contains("webView.settings.userAgentString = if (enabled)"))
        assertFalse(browserManager.contains("webView.settings.useWideViewPort = enabled"))
        assertFalse(browserManager.contains("webView.settings.loadWithOverviewMode = enabled"))
    }

    @Test
    fun textZoomUsesWebViewTextAutosizingLayout() {
        val settingsController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserWebViewSettingsController.kt"
        ).readText()

        assertTrue(settingsController.contains("layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING"))
        assertTrue(settingsController.contains("private fun applyTextZoom(targetWebView: WebView)"))
        assertTrue(settingsController.contains("textZoom = textZoomPercent"))
    }

}
