package com.example.videobrowser.video

import com.example.videobrowser.testutil.projectFile

/**
 * 测试阅读提示：
 * 这个测试文件验证“Media Routing Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaRoutingWiringContractTest {
    /**
     * 测试函数 `mainActivityRoutesAddressBarAndWebViewOverridesThroughMediaRoutingController`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `main Activity Routes Address Bar And Web View Overrides Through Media Routing Controller` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun mainActivityRoutesAddressBarAndWebViewOverridesThroughMediaRoutingController() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val coreFeatureAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserCoreFeatureAssemblyController.kt"
        ).readText()
        val source = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserNavigationController.kt"
        ).readText()
        val browserClientAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserClientAssemblyController.kt"
        ).readText()

        assertTrue(mainActivity.contains("private lateinit var browserFeatures: BrowserActivityFeatureComponents"))
        assertTrue(coreFeatureAssembly.contains("browserNavigationController.loadUrl(url)"))
        assertTrue(browserClientAssembly.contains("shouldBlockUrl = browserNavigationController::shouldBlockUrl"))
        assertTrue(source.contains("MediaRoutingController.route("))
        assertTrue(source.contains("MediaRouteSource.ADDRESS_BAR"))
        assertTrue(source.contains("MediaRouteSource.WEBVIEW_OVERRIDE"))
        assertFalse(source.contains("MediaUrlUtils.isPlayableMediaUri(Uri.parse(cleanedUrl))"))
        assertFalse(source.contains("openMedia && MediaUrlUtils.isPlayableMediaUri(uri)"))
    }

    /**
     * 测试函数 `downloadControllerRoutesDownloadListenerThroughMediaRoutingController`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `download Controller Routes Download Listener Through Media Routing Controller` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun downloadControllerRoutesDownloadListenerThroughMediaRoutingController() {
        val source = projectFile(
            "src/main/java/com/example/videobrowser/download/DownloadController.kt"
        ).readText()

        assertTrue(source.contains("MediaRoutingController.route("))
        assertTrue(source.contains("MediaRouteSource.DOWNLOAD"))
        assertFalse(source.contains("MediaUrlUtils.isPlayableMediaUri"))
    }

    /**
     * 测试函数 `pageActionsControllerRoutesLocalDocumentsThroughMediaRoutingController`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `page Actions Controller Routes Local Documents Through Media Routing Controller` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun pageActionsControllerRoutesLocalDocumentsThroughMediaRoutingController() {
        val source = projectFile(
            "src/main/java/com/example/videobrowser/browser/PageActionsController.kt"
        ).readText()

        assertTrue(source.contains("MediaRoutingController.route("))
        assertTrue(source.contains("MediaRouteSource.LOCAL_DOCUMENT"))
        assertTrue(source.contains("MediaUrlUtils.isPlayableMediaUri(url)"))
        assertFalse(source.contains("MediaUrlUtils.isPlayableMediaUri(Uri.parse(url))"))
    }

}
