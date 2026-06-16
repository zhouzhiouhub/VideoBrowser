package com.example.videobrowser.video

/**
 * 测试阅读提示：
 * 这个测试文件验证“Media Routing Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaRoutingWiringContractTest {
    @Test
    fun mainActivityRoutesAddressBarAndWebViewOverridesThroughMediaRoutingController() {
        val source = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt").readText()

        assertTrue(source.contains("MediaRoutingController.route("))
        assertTrue(source.contains("MediaRouteSource.ADDRESS_BAR"))
        assertTrue(source.contains("MediaRouteSource.WEBVIEW_OVERRIDE"))
        assertFalse(source.contains("MediaUrlUtils.isPlayableMediaUri(Uri.parse(cleanedUrl))"))
        assertFalse(source.contains("openMedia && MediaUrlUtils.isPlayableMediaUri(uri)"))
    }

    @Test
    fun downloadControllerRoutesDownloadListenerThroughMediaRoutingController() {
        val source = projectFile(
            "src/main/java/com/example/videobrowser/download/DownloadController.kt"
        ).readText()

        assertTrue(source.contains("MediaRoutingController.route("))
        assertTrue(source.contains("MediaRouteSource.DOWNLOAD"))
        assertFalse(source.contains("MediaUrlUtils.isPlayableMediaUri"))
    }

    @Test
    fun pageActionsControllerRoutesLocalDocumentsThroughMediaRoutingController() {
        val source = projectFile(
            "src/main/java/com/example/videobrowser/browser/PageActionsController.kt"
        ).readText()

        assertTrue(source.contains("MediaRoutingController.route("))
        assertTrue(source.contains("MediaRouteSource.LOCAL_DOCUMENT"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
