package com.example.videobrowser.video

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoPlaybackToolsContractTest {
    @Test
    fun `video playback operations are owned by shared playback module`() {
        val playbackScript = projectFile("src/main/assets/scripts/video_playback_tools.js").readText()
        val commonScript = projectFile("src/main/assets/scripts/common.js").readText()
        val scriptLoader = projectFile("src/main/java/com/example/videobrowser/inject/ScriptLoader.kt").readText()
        val commonAssetList = scriptLoader.substringAfter("val COMMON_SCRIPT_ASSETS = listOf(")

        assertTrue(playbackScript.contains("window.VideoBrowserVideoPlaybackTools = tools"))
        assertTrue(playbackScript.contains("tools.timeline = tools.timeline || function (video)"))
        assertTrue(playbackScript.contains("tools.reportTimeline = tools.reportTimeline || function (video)"))
        assertTrue(playbackScript.contains("tools.play = tools.play || function (video)"))
        assertTrue(playbackScript.contains("tools.pause = tools.pause || function (video)"))
        assertTrue(playbackScript.contains("tools.pauseAll = tools.pauseAll || function (videoQueryTools)"))
        assertTrue(playbackScript.contains("tools.seekTo = tools.seekTo || function (video, targetSeconds, options)"))
        assertTrue(playbackScript.contains("tools.seekBy = tools.seekBy || function (video, offsetSeconds, options)"))
        assertTrue(playbackScript.contains("tools.togglePlayPause = tools.togglePlayPause || function (video, options)"))
        assertTrue(playbackScript.contains("tools.startDirectional = tools.startDirectional || function (direction, state, options)"))
        assertTrue(playbackScript.contains("tools.stopDirectional = tools.stopDirectional || function (state, options)"))
        assertTrue(playbackScript.contains("const siteVideoCapabilityBroker = window.VideoBrowserSiteVideoCapabilityBroker;"))
        assertTrue(playbackScript.contains("siteVideoCapabilityBroker.invokeFromOptions(options, video, 'seekTo', [targetSeconds])"))
        assertTrue(playbackScript.contains("siteVideoCapabilityBroker.invokeFromOptions(options, video, 'seekBy', [offsetSeconds])"))
        assertTrue(playbackScript.contains("siteVideoCapabilityBroker.invokeFromOptions(options, video, 'togglePlayPause', [])"))
        assertTrue(playbackScript.contains("targetState.directionalPlayback.intervalId = window.setInterval(function ()"))
        assertTrue(commonScript.contains("const videoPlaybackTools = window.VideoBrowserVideoPlaybackTools"))
        assertTrue(commonScript.contains("return videoPlaybackTools.timeline(video);"))
        assertTrue(commonScript.contains("videoPlaybackTools.reportTimeline(target);"))
        assertTrue(commonScript.contains("videoPlaybackTools.seekTo(video, targetSeconds, {"))
        assertTrue(commonScript.contains("videoPlaybackTools.seekBy(video, offsetSeconds, {"))
        assertTrue(commonScript.contains("return videoPlaybackTools.togglePlayPause(video, {"))
        assertTrue(commonScript.contains("return videoPlaybackTools.startDirectional(direction, state, {"))
        assertTrue(commonScript.contains("return videoPlaybackTools.stopDirectional(state, {"))
        assertTrue(commonScript.contains("videoPlaybackTools.pauseAll(videoQueryTools);"))
        assertTrue(playbackScript.contains("tools.pause(video);"))
        assertTrue(playbackScript.contains("tools.play(video);"))
        assertTrue(scriptLoader.contains("VIDEO_PLAYBACK_TOOLS_SCRIPT_ASSET"))
        assertTrue(
            commonAssetList.indexOf("VIDEO_PLAYBACK_TOOLS_SCRIPT_ASSET") <
                commonAssetList.indexOf("COMMON_SCRIPT_ASSET")
        )
        assertFalse(commonScript.contains("if (typeof video.fastSeek === 'function')"))
        assertFalse(commonScript.contains("video.play().catch(function () {})"))
        assertFalse(commonScript.contains("video.pause();"))
        assertFalse(commonScript.contains("state.directionalPlayback.intervalId = window.setInterval(function ()"))
        assertFalse(playbackScript.contains("function invokeSiteVideoCapability(video, action, args, options)"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
