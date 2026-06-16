package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Browser Manager Web Settings Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
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
        val readme = projectFile("README.md").readText()

        assertTrue(browserManager.contains("setSupportZoom(true)"))
        assertTrue(browserManager.contains("builtInZoomControls = true"))
        assertTrue(browserManager.contains("displayZoomControls = false"))
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
        val readme = projectFile("README.md").readText()

        assertTrue(browserManager.contains("import android.os.Build"))
        assertTrue(browserManager.contains("Build.VERSION.SDK_INT >= Build.VERSION_CODES.O"))
        assertTrue(browserManager.contains("safeBrowsingEnabled = true"))
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
        val readme = projectFile("README.md").readText()

        assertTrue(browserManager.contains("private var mixedContentBlocked = true"))
        assertTrue(browserManager.contains("fun setMixedContentBlocked(blocked: Boolean)"))
        assertTrue(browserManager.contains("private fun applyMixedContentMode(targetWebView: WebView)"))
        assertTrue(browserManager.contains("WebSettings.MIXED_CONTENT_NEVER_ALLOW"))
        assertTrue(browserManager.contains("WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE"))
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
        val readme = projectFile("README.md").readText()

        assertTrue(browserManager.contains("allowFileAccess = false"))
        assertTrue(browserManager.contains("allowFileAccessFromFileURLs = false"))
        assertTrue(browserManager.contains("allowUniversalAccessFromFileURLs = false"))
        assertTrue(readme.contains("默认禁止 `file://` 页面跨文件或跨来源访问本地资源"))
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
        return listOfNotNull(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path"),
            workingDirectory.parentFile?.let { parent -> File(parent, path) }
        ).first { it.exists() }
    }
}
