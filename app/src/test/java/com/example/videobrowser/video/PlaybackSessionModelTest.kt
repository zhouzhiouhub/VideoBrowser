package com.example.videobrowser.video

/**
 * 测试阅读提示：
 * 这个测试文件验证“Playback Session Model Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaybackSessionModelTest {
    /**
     * 测试函数 `sessionStateNormalizesPlaybackValuesAgainstQueueSize`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `session State Normalizes Playback Values Against Queue Size` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun sessionStateNormalizesPlaybackValuesAgainstQueueSize() {
        val state = PlaybackSessionState(
            positionMs = -500L,
            durationMs = -1L,
            speed = Float.POSITIVE_INFINITY,
            repeatMode = PlaybackRepeatMode.ALL,
            currentIndex = 8,
            playWhenReady = false,
            zoomMode = VideoZoomMode.CROP
        )

        val normalized = state.normalized(itemCount = 3)

        assertEquals(0L, normalized.positionMs)
        assertNull(normalized.durationMs)
        assertEquals(1f, normalized.speed)
        assertEquals(PlaybackRepeatMode.ALL, normalized.repeatMode)
        assertEquals(2, normalized.currentIndex)
        assertEquals(false, normalized.playWhenReady)
        assertEquals(VideoZoomMode.CROP, normalized.zoomMode)
    }

    /**
     * 测试函数 `sessionStateCanBeCreatedFromQueueAndPlayerSnapshot`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `session State Can Be Created From Queue And Player Snapshot` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun sessionStateCanBeCreatedFromQueueAndPlayerSnapshot() {
        val queue = PlaybackQueue(
            items = listOf(
                PlayableMediaItem(uri = "one.mp4", source = PlayableMediaSource.LOCAL_DOCUMENT),
                PlayableMediaItem(uri = "two.mp4", source = PlayableMediaSource.LOCAL_DOCUMENT)
            ),
            currentIndex = 1,
            repeatMode = PlaybackRepeatMode.ONE
        )

        val state = PlaybackSessionState.fromQueue(
            queue = queue,
            positionMs = 42_000L,
            durationMs = 90_000L,
            speed = 1.5f,
            playWhenReady = true,
            zoomMode = VideoZoomMode.STRETCH
        )

        assertEquals(42_000L, state.positionMs)
        assertEquals(90_000L, state.durationMs)
        assertEquals(1.5f, state.speed)
        assertEquals(PlaybackRepeatMode.ONE, state.repeatMode)
        assertEquals(1, state.currentIndex)
        assertEquals(true, state.playWhenReady)
        assertEquals(VideoZoomMode.STRETCH, state.zoomMode)
    }
}
