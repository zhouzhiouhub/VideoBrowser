package com.example.videobrowser.video

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoFullscreenToolsContractTest {
    @Test
    fun `video fullscreen behavior is owned by shared fullscreen module`() {
        val fullscreenScript = projectFile("src/main/assets/scripts/video_fullscreen_tools.js").readText()
        val commonScript = projectFile("src/main/assets/scripts/common.js").readText()
        val scriptLoader = projectFile("src/main/java/com/example/videobrowser/inject/ScriptLoader.kt").readText()
        val commonAssetList = scriptLoader.substringAfter("val COMMON_SCRIPT_ASSETS = listOf(")

        assertTrue(fullscreenScript.contains("window.VideoBrowserVideoFullscreenTools = tools"))
        assertTrue(fullscreenScript.contains("tools.activeVideo = tools.activeVideo || function (state, videoQueryTools)"))
        assertTrue(fullscreenScript.contains("tools.request = tools.request || function (video, options)"))
        assertTrue(fullscreenScript.contains("tools.exit = tools.exit || function (state, options)"))
        assertTrue(fullscreenScript.contains("tools.isVideoFullscreen = tools.isVideoFullscreen || function (video)"))
        assertTrue(fullscreenScript.contains("tools.installVideoHooks = tools.installVideoHooks || function (video, state, options)"))
        assertTrue(fullscreenScript.contains("tools.syncDocumentState = tools.syncDocumentState || function (state, options)"))
        assertTrue(fullscreenScript.contains("target.requestFullscreen || target.webkitRequestFullscreen"))
        assertTrue(fullscreenScript.contains("video.addEventListener('webkitbeginfullscreen', function ()"))
        assertTrue(fullscreenScript.contains("video.addEventListener('webkitendfullscreen', function ()"))
        assertTrue(fullscreenScript.contains("document.exitFullscreen()"))
        assertTrue(commonScript.contains("const videoFullscreenTools = window.VideoBrowserVideoFullscreenTools"))
        assertTrue(commonScript.contains("return videoFullscreenTools.installVideoHooks(video, state, {"))
        assertTrue(commonScript.contains("return videoFullscreenTools.activeVideo(state, videoQueryTools);"))
        assertTrue(commonScript.contains("videoFullscreenTools.request(video, {"))
        assertTrue(commonScript.contains("videoFullscreenTools.exit(state, {"))
        assertTrue(commonScript.contains("return videoFullscreenTools.isVideoFullscreen(video);"))
        assertTrue(commonScript.contains("videoFullscreenTools.syncDocumentState(state, {"))
        assertTrue(scriptLoader.contains("VIDEO_FULLSCREEN_TOOLS_SCRIPT_ASSET"))
        assertTrue(
            commonAssetList.indexOf("VIDEO_FULLSCREEN_TOOLS_SCRIPT_ASSET") <
                commonAssetList.indexOf("COMMON_SCRIPT_ASSET")
        )
        assertFalse(commonScript.contains("target.requestFullscreen || target.webkitRequestFullscreen"))
        assertFalse(commonScript.contains("document.exitFullscreen()"))
        assertFalse(commonScript.contains("document.webkitExitFullscreen()"))
        assertFalse(commonScript.contains("state.fullscreenHookedVideos.add(video);"))
        assertFalse(commonScript.contains("video.addEventListener('webkitbeginfullscreen'"))
        assertFalse(commonScript.contains("const timelineReporter = function ()"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
