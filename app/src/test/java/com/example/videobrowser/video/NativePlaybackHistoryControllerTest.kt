package com.example.videobrowser.video

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NativePlaybackHistoryControllerTest {
    @Test
    fun `restore starts from beginning when setting is enabled`() {
        val controller = NativePlaybackHistoryController(
            alwaysStartVideosFromBeginning = { true },
            progressFor = { error("progress should not be read") },
            resumePositionFor = { error("resume position should not be read") },
            saveProgress = { _, _ -> },
            privateBrowsing = { false },
            speedNormalizer = { speed -> speed }
        )

        assertEquals(NativePlaybackHistoryRestore(positionMs = 0L), controller.restore("media"))
    }

    @Test
    fun `restore returns resume position and normalized speed`() {
        val controller = NativePlaybackHistoryController(
            alwaysStartVideosFromBeginning = { false },
            progressFor = {
                PlaybackProgress(
                    mediaIdentity = it,
                    positionMs = 4_000L,
                    durationMs = 20_000L,
                    speed = 1.75f,
                    updatedAtMillis = 1L
                )
            },
            resumePositionFor = { 4_000L },
            saveProgress = { _, _ -> },
            privateBrowsing = { false },
            speedNormalizer = { speed -> speed.coerceAtMost(1.5f) }
        )

        assertEquals(
            NativePlaybackHistoryRestore(positionMs = 4_000L, playbackSpeed = 1.5f),
            controller.restore("media")
        )
    }

    @Test
    fun `save writes native media progress with private browsing flag`() {
        val saved = mutableListOf<Pair<PlaybackProgress, Boolean>>()
        val controller = NativePlaybackHistoryController(
            alwaysStartVideosFromBeginning = { false },
            progressFor = { null },
            resumePositionFor = { null },
            saveProgress = { progress, privateBrowsing -> saved += progress to privateBrowsing },
            privateBrowsing = { true },
            currentTimeMillis = { 123L },
            speedNormalizer = { speed -> speed }
        )

        controller.save(
            NativePlaybackHistorySnapshot(
                mediaIdentity = " media ",
                positionMs = -10L,
                durationMs = -20L,
                speed = 1.25f,
                title = "Title"
            )
        )

        assertEquals(1, saved.size)
        assertEquals(
            PlaybackProgress(
                mediaIdentity = "media",
                positionMs = 0L,
                durationMs = 0L,
                speed = 1.25f,
                updatedAtMillis = 123L,
                title = "Title",
                source = PlaybackHistorySource.NATIVE_MEDIA
            ),
            saved.single().first
        )
        assertTrue(saved.single().second)
    }

    @Test
    fun `save ignores blank media identity`() {
        val saved = mutableListOf<PlaybackProgress>()
        val controller = NativePlaybackHistoryController(
            alwaysStartVideosFromBeginning = { false },
            progressFor = { null },
            resumePositionFor = { null },
            saveProgress = { progress, _ -> saved += progress },
            privateBrowsing = { false },
            speedNormalizer = { speed -> speed }
        )

        controller.save(
            NativePlaybackHistorySnapshot(
                mediaIdentity = "   ",
                positionMs = 1L,
                durationMs = 2L,
                speed = 1f,
                title = null
            )
        )

        assertTrue(saved.isEmpty())
    }
}
