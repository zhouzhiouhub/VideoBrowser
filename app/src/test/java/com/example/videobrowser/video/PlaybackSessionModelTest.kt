package com.example.videobrowser.video

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaybackSessionModelTest {
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
