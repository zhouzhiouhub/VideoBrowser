package com.example.videobrowser.video

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayableMediaItemMedia3ConverterContractTest {
    @Test
    fun playerActivityDelegatesMedia3ConversionToConverter() {
        val playerActivity = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()
        val playerInitializer = projectFile(
            "src/main/java/com/example/videobrowser/video/NativePlayerInitializer.kt"
        ).readText()
        val converter = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayableMediaItemMedia3Converter.kt"
        ).readText()

        assertTrue(playerActivity.contains("NativePlayerInitializer("))
        assertTrue(playerInitializer.contains("PlayableMediaItemMedia3Converter::toMediaItem"))
        assertFalse(playerActivity.contains("private fun toMediaItem("))
        assertFalse(playerActivity.contains("private fun toSubtitleConfiguration("))
        assertFalse(playerActivity.contains("private fun normalizedMimeType("))
        assertTrue(converter.contains("fun toMediaItem(item: PlayableMediaItem): MediaItem"))
        assertTrue(converter.contains("private fun toSubtitleConfiguration("))
        assertTrue(converter.contains("private fun normalizedMimeType("))
    }

}
