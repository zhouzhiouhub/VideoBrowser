package com.example.videobrowser.video

/**
 * 测试阅读提示：
 * 这个测试文件验证“Media Routing Controller Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MediaRoutingControllerTest {
    /**
     * 测试函数 `routeAddressBarUrl_opensPlayableMediaInNativePlayer`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `route Address Bar Url opens Playable Media In Native Player` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun routeAddressBarUrl_opensPlayableMediaInNativePlayer() {
        val decision = MediaRoutingController.route(
            MediaRouteRequest(
                source = MediaRouteSource.ADDRESS_BAR,
                url = "https://cdn.example.com/movie.mp4",
                currentPageUrl = "https://video.example.com/watch",
                currentPageTitle = "Episode 1",
                userAgent = "Browser UA"
            )
        )

        assertEquals(MediaRouteAction.OPEN_NATIVE_PLAYER, decision.action)
        assertEquals("https://cdn.example.com/movie.mp4", decision.mediaItem?.uri)
        assertEquals("Episode 1", decision.mediaItem?.title)
        assertEquals("Browser UA", decision.mediaItem?.userAgent)
        assertEquals("https://video.example.com/watch", decision.mediaItem?.referer)
        assertEquals(PlayableMediaSource.REMOTE_URL, decision.mediaItem?.source)
    }

    /**
     * 测试函数 `routeAddressBarUrl_keepsRegularWebPagesInWebView`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `route Address Bar Url keeps Regular Web Pages In Web View` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun routeAddressBarUrl_keepsRegularWebPagesInWebView() {
        val decision = MediaRoutingController.route(
            MediaRouteRequest(
                source = MediaRouteSource.ADDRESS_BAR,
                url = "https://example.com/article",
                currentPageUrl = "https://example.com/home"
            )
        )

        assertEquals(MediaRouteAction.LOAD_IN_WEBVIEW, decision.action)
        assertNull(decision.mediaItem)
    }

    /**
     * 测试函数 `routeDownload_opensPlayableMediaAndDownloadsNonMedia`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `route Download opens Playable Media And Downloads Non Media` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun routeDownload_opensPlayableMediaAndDownloadsNonMedia() {
        val mediaDecision = MediaRoutingController.route(
            MediaRouteRequest(
                source = MediaRouteSource.DOWNLOAD,
                url = "https://cdn.example.com/live",
                mimeType = "application/vnd.apple.mpegurl",
                userAgent = "Download UA"
            )
        )

        assertEquals(MediaRouteAction.OPEN_NATIVE_PLAYER, mediaDecision.action)
        assertEquals("Download UA", mediaDecision.mediaItem?.userAgent)
        assertEquals("application/vnd.apple.mpegurl", mediaDecision.mediaItem?.mimeType)

        val documentDecision = MediaRoutingController.route(
            MediaRouteRequest(
                source = MediaRouteSource.DOWNLOAD,
                url = "https://cdn.example.com/archive.zip",
                mimeType = "application/zip"
            )
        )

        assertEquals(MediaRouteAction.DOWNLOAD, documentDecision.action)
        assertNull(documentDecision.mediaItem)
    }

    /**
     * 测试函数 `routeLocalDocument_opensPlayableContentUriAndExternalizesNonMedia`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `route Local Document opens Playable Content Uri And Externalizes Non Media` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun routeLocalDocument_opensPlayableContentUriAndExternalizesNonMedia() {
        val mediaDecision = MediaRoutingController.route(
            MediaRouteRequest(
                source = MediaRouteSource.LOCAL_DOCUMENT,
                url = "content://media/external/video/media/42",
                mimeType = "video/mp4",
                displayName = "Clip.mp4"
            )
        )

        assertEquals(MediaRouteAction.OPEN_NATIVE_PLAYER, mediaDecision.action)
        assertEquals(PlayableMediaSource.LOCAL_DOCUMENT, mediaDecision.mediaItem?.source)
        assertEquals("Clip.mp4", mediaDecision.mediaItem?.title)
        assertNull(mediaDecision.mediaItem?.referer)

        val documentDecision = MediaRoutingController.route(
            MediaRouteRequest(
                source = MediaRouteSource.LOCAL_DOCUMENT,
                url = "content://documents/tree/report",
                mimeType = "application/pdf",
                displayName = "Report.pdf"
            )
        )

        assertEquals(MediaRouteAction.OPEN_EXTERNAL_APP, documentDecision.action)
        assertNull(documentDecision.mediaItem)
    }

    /**
     * 测试函数 `routeWebViewOverride_blocksUnsupportedExternalSchemes`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `route Web View Override blocks Unsupported External Schemes` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun routeWebViewOverride_blocksUnsupportedExternalSchemes() {
        val decision = MediaRoutingController.route(
            MediaRouteRequest(
                source = MediaRouteSource.WEBVIEW_OVERRIDE,
                url = "intent://scan/#Intent;scheme=zxing;package=com.google.zxing.client.android;end"
            )
        )

        assertEquals(MediaRouteAction.BLOCK, decision.action)
        assertNull(decision.mediaItem)
    }
}
