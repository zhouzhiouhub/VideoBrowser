package com.example.videobrowser.video

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FullscreenVideoGestureMathTest {
    @Test
    fun `brightness drag maps vertical movement and clamps to gesture range`() {
        assertEquals(
            0.9f,
            FullscreenVideoGestureMath.brightnessForDrag(
                initialBrightness = 0.4f,
                deltaY = -50f,
                viewHeight = 100
            ),
            0.0001f
        )
        assertEquals(
            0.02f,
            FullscreenVideoGestureMath.brightnessForDrag(
                initialBrightness = 0.4f,
                deltaY = 100f,
                viewHeight = 100
            ),
            0.0001f
        )
        assertEquals(
            1f,
            FullscreenVideoGestureMath.brightnessForDrag(
                initialBrightness = 0.8f,
                deltaY = -100f,
                viewHeight = 100
            ),
            0.0001f
        )
    }

    @Test
    fun `brightness drag handles invalid height by clamping initial value`() {
        assertEquals(
            0.02f,
            FullscreenVideoGestureMath.brightnessForDrag(
                initialBrightness = -1f,
                deltaY = -100f,
                viewHeight = 0
            ),
            0.0001f
        )
    }

    @Test
    fun `volume drag maps vertical movement and clamps to stream range`() {
        assertEquals(
            10,
            FullscreenVideoGestureMath.volumeForDrag(
                initialVolume = 5,
                deltaY = -50f,
                viewHeight = 100,
                minVolume = 0,
                maxVolume = 10
            )
        )
        assertEquals(
            0,
            FullscreenVideoGestureMath.volumeForDrag(
                initialVolume = 5,
                deltaY = 50f,
                viewHeight = 100,
                minVolume = 0,
                maxVolume = 10
            )
        )
    }

    @Test
    fun `volume drag rejects invalid ranges or height`() {
        assertEquals(
            null,
            FullscreenVideoGestureMath.volumeForDrag(
                initialVolume = 5,
                deltaY = -50f,
                viewHeight = 100,
                minVolume = 10,
                maxVolume = 10
            )
        )
        assertEquals(
            null,
            FullscreenVideoGestureMath.volumeForDrag(
                initialVolume = 5,
                deltaY = -50f,
                viewHeight = 0,
                minVolume = 0,
                maxVolume = 10
            )
        )
    }

    @Test
    fun `volume percent maps stream range and clamps outside values`() {
        assertEquals(0, FullscreenVideoGestureMath.volumePercent(0, 0, 10))
        assertEquals(50, FullscreenVideoGestureMath.volumePercent(5, 0, 10))
        assertEquals(100, FullscreenVideoGestureMath.volumePercent(10, 0, 10))
        assertEquals(0, FullscreenVideoGestureMath.volumePercent(-5, 0, 10))
        assertEquals(100, FullscreenVideoGestureMath.volumePercent(15, 0, 10))
    }

    @Test
    fun `volume percent handles invalid ranges`() {
        assertEquals(0, FullscreenVideoGestureMath.volumePercent(5, 10, 10))
        assertEquals(0, FullscreenVideoGestureMath.volumePercent(5, 12, 10))
    }

    @Test
    fun `normalize speed keeps positive values and falls back for invalid input`() {
        assertEquals(1.25f, FullscreenVideoGestureMath.normalizeSpeed(1.25f))
        assertEquals(1f, FullscreenVideoGestureMath.normalizeSpeed(0f))
        assertEquals(1f, FullscreenVideoGestureMath.normalizeSpeed(-1f))
        assertEquals(1f, FullscreenVideoGestureMath.normalizeSpeed(Float.NaN))
        assertEquals(1f, FullscreenVideoGestureMath.normalizeSpeed(Float.POSITIVE_INFINITY))
    }

    @Test
    fun `normalize speed supports a caller supplied default`() {
        assertEquals(2f, FullscreenVideoGestureMath.normalizeSpeed(Float.NaN, defaultSpeed = 2f))
    }

    @Test
    fun `screen zone uses left center and right bands`() {
        assertEquals(VideoGestureScreenZone.NONE, FullscreenVideoGestureMath.screenZoneFor(-1f, 100))
        assertEquals(VideoGestureScreenZone.NONE, FullscreenVideoGestureMath.screenZoneFor(0f, 0))
        assertEquals(VideoGestureScreenZone.LEFT, FullscreenVideoGestureMath.screenZoneFor(0f, 100))
        assertEquals(VideoGestureScreenZone.LEFT, FullscreenVideoGestureMath.screenZoneFor(29.9f, 100))
        assertEquals(VideoGestureScreenZone.CENTER, FullscreenVideoGestureMath.screenZoneFor(31f, 100))
        assertEquals(VideoGestureScreenZone.CENTER, FullscreenVideoGestureMath.screenZoneFor(69.9f, 100))
        assertEquals(VideoGestureScreenZone.RIGHT, FullscreenVideoGestureMath.screenZoneFor(70f, 100))
    }

    @Test
    fun `side zones are left and right only`() {
        assertTrue(VideoGestureScreenZone.LEFT.isSide())
        assertTrue(VideoGestureScreenZone.RIGHT.isSide())
        assertFalse(VideoGestureScreenZone.CENTER.isSide())
        assertFalse(VideoGestureScreenZone.NONE.isSide())
    }
}
