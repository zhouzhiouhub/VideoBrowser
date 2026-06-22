package com.example.videobrowser.video

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaybackSeekBoundsTest {
    @Test
    fun `clamp position keeps seek inside known duration`() {
        assertEquals(0L, PlaybackSeekBounds.clampPosition(-500L, 10_000L))
        assertEquals(4_000L, PlaybackSeekBounds.clampPosition(4_000L, 10_000L))
        assertEquals(10_000L, PlaybackSeekBounds.clampPosition(12_000L, 10_000L))
    }

    @Test
    fun `clamp position only clips below zero without known duration`() {
        assertEquals(0L, PlaybackSeekBounds.clampPosition(-500L, null))
        assertEquals(12_000L, PlaybackSeekBounds.clampPosition(12_000L, null))
        assertEquals(12_000L, PlaybackSeekBounds.clampPosition(12_000L, 0L))
    }

    @Test
    fun `offset position clamps around current position`() {
        assertEquals(7_000L, PlaybackSeekBounds.offsetPosition(5_000L, 2_000L, 10_000L))
        assertEquals(10_000L, PlaybackSeekBounds.offsetPosition(9_000L, 5_000L, 10_000L))
        assertEquals(0L, PlaybackSeekBounds.offsetPosition(1_000L, -5_000L, 10_000L))
    }

    @Test
    fun `offset position returns null without current position`() {
        assertNull(PlaybackSeekBounds.offsetPosition(null, 2_000L, 10_000L))
    }
}
