package com.example.videobrowser.utils

/**
 * 测试阅读提示：
 * 这个测试文件验证“Media Url Utils Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaUrlUtilsTest {
    /**
     * 测试函数 `isPlayableMediaUri_acceptsHttpVideoExtensionsBeforeQueryAndFragment`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `is Playable Media Uri accepts Http Video Extensions Before Query And Fragment` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun isPlayableMediaUri_acceptsHttpVideoExtensionsBeforeQueryAndFragment() {
        assertTrue(
            MediaUrlUtils.isPlayableMediaUri(
                "https://cdn.example.com/video/movie.MP4?token=1#player"
            )
        )
    }

    /**
     * 测试函数 `isPlayableMediaUri_acceptsStreamingManifestsAndRtspSchemes`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `is Playable Media Uri accepts Streaming Manifests And Rtsp Schemes` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun isPlayableMediaUri_acceptsStreamingManifestsAndRtspSchemes() {
        assertTrue(
            MediaUrlUtils.isPlayableMediaUri(
                "https://live.example.com/channel/master.m3u8"
            )
        )
        assertTrue(
            MediaUrlUtils.isPlayableMediaUri(
                "https://video.example.com/dash/manifest.mpd"
            )
        )
        assertTrue(
            MediaUrlUtils.isPlayableMediaUri(
                "rtsp://camera.example.com/live"
            )
        )
    }

    /**
     * 测试函数 `isPlayableMediaUri_acceptsPlayableMimeTypesWithoutExtension`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `is Playable Media Uri accepts Playable Mime Types Without Extension` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun isPlayableMediaUri_acceptsPlayableMimeTypesWithoutExtension() {
        assertTrue(
            MediaUrlUtils.isPlayableMediaUri(
                "content://media/external/video/media/42",
                "video/mp4; charset=utf-8"
            )
        )
        assertTrue(
            MediaUrlUtils.isPlayableMediaUri(
                "https://stream.example.com/live",
                "application/vnd.apple.mpegurl"
            )
        )
    }

    /**
     * 测试函数 `isPlayableMediaUri_rejectsUnsupportedSchemesAndDocuments`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `is Playable Media Uri rejects Unsupported Schemes And Documents` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun isPlayableMediaUri_rejectsUnsupportedSchemesAndDocuments() {
        assertFalse(
            MediaUrlUtils.isPlayableMediaUri(
                "ftp://cdn.example.com/movie.mp4"
            )
        )
        assertFalse(
            MediaUrlUtils.isPlayableMediaUri(
                "https://example.com/readme.pdf",
                "application/pdf"
            )
        )
    }
}
