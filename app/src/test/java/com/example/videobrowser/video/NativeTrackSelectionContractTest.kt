package com.example.videobrowser.video

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class NativeTrackSelectionContractTest {
    @Test
    fun fullscreenOverlayProvidesTrackSelectionButton() {
        val source = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoGestureOverlay.kt"
        ).readText()

        assertTrue(source.contains("var onTrackSelectionRequested: (() -> Unit)? = null"))
        assertTrue(source.contains("private val trackButton"))
        assertTrue(source.contains("trackButton.setOnClickListener"))
        assertTrue(source.contains("onTrackSelectionRequested?.invoke()"))
        assertTrue(source.contains("R.string.video_control_tracks"))
    }

    @Test
    fun playerActivityShowsMedia3TrackSelectionDialogsForAudioAndSubtitles() {
        val source = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()

        assertTrue(source.contains("TrackSelectionDialogBuilder"))
        assertTrue(source.contains("showTrackSelectionMenu()"))
        assertTrue(source.contains("showTrackSelectionDialog(C.TRACK_TYPE_AUDIO"))
        assertTrue(source.contains("showTrackSelectionDialog(C.TRACK_TYPE_TEXT"))
        assertTrue(source.contains("onTrackSelectionRequested = ::showTrackSelectionMenu"))
    }

    @Test
    fun trackSelectionStringsAreDefined() {
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(strings.contains("name=\"video_control_tracks\""))
        assertTrue(strings.contains("name=\"video_track_audio\""))
        assertTrue(strings.contains("name=\"video_track_subtitles\""))
        assertTrue(strings.contains("name=\"toast_video_tracks_unavailable\""))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
