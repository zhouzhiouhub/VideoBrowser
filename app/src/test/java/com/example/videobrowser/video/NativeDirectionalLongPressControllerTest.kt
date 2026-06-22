package com.example.videobrowser.video

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NativeDirectionalLongPressControllerTest {
    @Test
    fun `forward long press boosts speed and restores previous playback state`() {
        val scheduler = FakePlaybackScanScheduler()
        val target = FakePlaybackTarget(playWhenReady = true)
        val controller = NativeDirectionalLongPressController(
            scheduler = scheduler,
            seekBy = {},
            speedNormalizer = { speed -> speed }
        )

        controller.start(
            direction = 1,
            selectedPlaybackSpeed = 1.25f,
            playbackTarget = target
        )
        val restoredSpeed = controller.stop()

        assertEquals(listOf("speed:2.0", "play", "speed:1.25", "play"), target.events)
        assertEquals(1.25f, restoredSpeed)
        assertEquals(0, scheduler.posted.size)
        assertEquals(1, scheduler.removedCount)
    }

    @Test
    fun `backward long press pauses seeks immediately and schedules repeated scan`() {
        val scheduler = FakePlaybackScanScheduler()
        val seekOffsets = mutableListOf<Long>()
        val target = FakePlaybackTarget(playWhenReady = false)
        val controller = NativeDirectionalLongPressController(
            scheduler = scheduler,
            seekBy = seekOffsets::add,
            speedNormalizer = { speed -> speed }
        )

        controller.start(
            direction = -1,
            selectedPlaybackSpeed = 1.5f,
            playbackTarget = target
        )
        scheduler.runLastPosted()
        val restoredSpeed = controller.stop()

        assertEquals(listOf(-500L, -500L), seekOffsets)
        assertEquals(listOf("pause", "speed:1.5", "pause"), target.events)
        assertEquals(1.5f, restoredSpeed)
        assertEquals(2, scheduler.posted.size)
        assertEquals(1, scheduler.removedCount)
    }

    @Test
    fun `stop is ignored when long press is not active`() {
        val controller = NativeDirectionalLongPressController(
            scheduler = FakePlaybackScanScheduler(),
            seekBy = {},
            speedNormalizer = { speed -> speed }
        )

        assertNull(controller.stop())
    }

    private class FakePlaybackScanScheduler : PlaybackScanScheduler {
        val posted = mutableListOf<ScheduledRunnable>()
        var removedCount = 0

        override fun postDelayed(runnable: Runnable, delayMs: Long) {
            posted += ScheduledRunnable(runnable, delayMs)
        }

        override fun removeCallbacks(runnable: Runnable) {
            removedCount += 1
        }

        fun runLastPosted() {
            posted.last().runnable.run()
        }
    }

    private data class ScheduledRunnable(
        val runnable: Runnable,
        val delayMs: Long
    )

    private class FakePlaybackTarget(
        override val playWhenReady: Boolean
    ) : DirectionalLongPressPlaybackTarget {
        val events = mutableListOf<String>()

        override fun setPlaybackSpeed(speed: Float) {
            events += "speed:$speed"
        }

        override fun play() {
            events += "play"
        }

        override fun pause() {
            events += "pause"
        }
    }
}
