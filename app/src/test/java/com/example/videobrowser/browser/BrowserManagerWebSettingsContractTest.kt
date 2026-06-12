package com.example.videobrowser.browser

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserManagerWebSettingsContractTest {
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

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOfNotNull(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path"),
            workingDirectory.parentFile?.let { parent -> File(parent, path) }
        ).first { it.exists() }
    }
}
