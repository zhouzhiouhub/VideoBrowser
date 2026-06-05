package com.example.videobrowser.site

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class BilibiliVideoOverlayContractTest {
    @Test
    fun bilibiliAdapterHidesOwnCenterPlaybackOverlayAfterEnablingNativeControls() {
        val script = projectFile("src/main/assets/scripts/bilibili.js").readText()

        assertTrue(script.contains("function hideVideoPlayPauseOverlays()"))
        assertTrue(script.contains("function isLikelyCenterPlaybackOverlay(element, video)"))
        assertTrue(script.contains("enableVideoControls();\n      hideVideoPlayPauseOverlays();"))
        assertTrue(script.contains(".mplayer-play-icon"))
        assertTrue(script.contains(".bpx-player-state-wrap"))
        assertTrue(script.contains("bilibili-video-play-overlay"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
