package com.example.videobrowser.video

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MediaRoutingControllerTest {
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
