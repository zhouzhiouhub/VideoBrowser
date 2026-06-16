package com.example.videobrowser.site

/**
 * 测试阅读提示：
 * 这个测试文件验证“Bilibili Video Overlay Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BilibiliVideoOverlayContractTest {
    @Test
    fun bilibiliAdapterHidesOwnCenterPlaybackOverlayAfterEnablingNativeControls() {
        val script = projectFile("src/main/assets/scripts/bilibili.js").readText()

        assertTrue(script.contains("function hideVideoPlayPauseOverlays()"))
        assertTrue(script.contains("function isLikelyCenterPlaybackOverlay(element, video)"))
        assertTrue(script.contains("hideVideoPlayPauseOverlays();"))
        assertFalse(script.contains("enableVideoControls();\n      hideVideoPlayPauseOverlays();"))
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
