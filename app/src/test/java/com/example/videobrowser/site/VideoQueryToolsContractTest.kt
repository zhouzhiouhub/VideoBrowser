package com.example.videobrowser.site

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoQueryToolsContractTest {
    @Test
    fun `video element lookup is owned by shared video query module`() {
        val videoQueryScript = projectFile("src/main/assets/scripts/video_query_tools.js").readText()
        val commonScript = projectFile("src/main/assets/scripts/common.js").readText()
        val fullscreenScript = projectFile("src/main/assets/scripts/video_fullscreen_tools.js").readText()

        assertTrue(videoQueryScript.contains("window.VideoBrowserVideoQueryTools = tools"))
        assertTrue(videoQueryScript.contains("tools.all = tools.all || function ()"))
        assertTrue(videoQueryScript.contains("document.querySelectorAll('video')"))
        assertTrue(videoQueryScript.contains("tools.forEach = tools.forEach || function (callback)"))
        assertTrue(videoQueryScript.contains("tools.some = tools.some || function (predicate)"))
        assertTrue(commonScript.contains("const videoQueryTools = window.VideoBrowserVideoQueryTools"))
        assertTrue(commonScript.contains("videoQueryTools.forEach(function (video)"))
        assertTrue(fullscreenScript.contains("videoQueryTools.all()"))
        assertTrue(commonScript.contains("return videoQueryTools.some(function (video)"))
        assertFalse(commonScript.contains("document.querySelectorAll('video')"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
