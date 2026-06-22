package com.example.videobrowser.video

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoEnhancementToolsContractTest {
    @Test
    fun `video enhancement behavior is owned by shared enhancement module`() {
        val enhancementScript = projectFile("src/main/assets/scripts/video_enhancement_tools.js").readText()
        val commonScript = projectFile("src/main/assets/scripts/common.js").readText()
        val scriptLoader = projectFile("src/main/java/com/example/videobrowser/inject/ScriptLoader.kt").readText()
        val commonAssetList = scriptLoader.substringAfter("val COMMON_SCRIPT_ASSETS = listOf(")

        assertTrue(enhancementScript.contains("window.VideoBrowserVideoEnhancementTools = tools"))
        assertTrue(enhancementScript.contains("tools.installPlaybackSpeedHooks = tools.installPlaybackSpeedHooks || function (video, state, options)"))
        assertTrue(enhancementScript.contains("tools.currentFullscreenPlaybackSpeed = tools.currentFullscreenPlaybackSpeed || function (state)"))
        assertTrue(enhancementScript.contains("tools.isFullscreenPlaybackTarget = tools.isFullscreenPlaybackTarget || function (video, state, options)"))
        assertTrue(enhancementScript.contains("tools.desiredSpeed = tools.desiredSpeed || function (video, state, options)"))
        assertTrue(enhancementScript.contains("tools.applySpeed = tools.applySpeed || function (video, state, options)"))
        assertTrue(enhancementScript.contains("tools.preferBestQuality = tools.preferBestQuality || function (video, state, options)"))
        assertTrue(enhancementScript.contains("tools.setPlaybackSpeed = tools.setPlaybackSpeed || function (speed, state, options)"))
        assertTrue(enhancementScript.contains("const enhancerState = window.VideoBrowserEnhancerState;"))
        assertTrue(enhancementScript.contains("const hookedVideos = enhancerState.ensureWeakSet(targetState, 'speedHookedVideos');"))
        assertTrue(enhancementScript.contains("enhancerState.ensureWeakMap(targetState, 'bestQualityAttempts');"))
        assertTrue(enhancementScript.contains("const siteVideoCapabilityBroker = window.VideoBrowserSiteVideoCapabilityBroker;"))
        assertTrue(enhancementScript.contains("siteVideoCapabilityBroker.hasFromOptions(config, video, 'setPlaybackSpeed')"))
        assertTrue(enhancementScript.contains("siteVideoCapabilityBroker.invokeFromOptions(config, video, 'setPlaybackSpeed', [speed])"))
        assertTrue(enhancementScript.contains("const callbackTools = window.VideoBrowserCallbackTools;"))
        assertTrue(enhancementScript.contains("callbackTools.call(config, 'stopDirectionalPlayback');"))
        assertTrue(enhancementScript.contains("[targetState.fullscreenPlaybackSpeed]"))
        assertTrue(enhancementScript.contains("forEachVideo(config, function (targetVideo)"))
        assertTrue(enhancementScript.contains("siteVideoCapabilityBroker.hasFromOptions(config, video, 'preferBestQuality')"))
        assertTrue(enhancementScript.contains("siteVideoCapabilityBroker.invokeFromOptions(config, video, 'preferBestQuality', [])"))
        assertTrue(commonScript.contains("const videoEnhancementTools = window.VideoBrowserVideoEnhancementTools"))
        assertTrue(commonScript.contains("return videoEnhancementTools.installPlaybackSpeedHooks(video, state, {"))
        assertTrue(commonScript.contains("return videoEnhancementTools.desiredSpeed(video, state, {"))
        assertTrue(commonScript.contains("return videoEnhancementTools.currentFullscreenPlaybackSpeed(state);"))
        assertTrue(commonScript.contains("return videoEnhancementTools.isFullscreenPlaybackTarget(video, state, {"))
        assertTrue(commonScript.contains("return videoEnhancementTools.applySpeed(video, state, {"))
        assertTrue(commonScript.contains("return videoEnhancementTools.preferBestQuality(video, state, {"))
        assertTrue(commonScript.contains("videoEnhancementTools.setPlaybackSpeed(speed, state, {"))
        assertTrue(scriptLoader.contains("VIDEO_ENHANCEMENT_TOOLS_SCRIPT_ASSET"))
        assertTrue(
            commonAssetList.indexOf("VIDEO_ENHANCEMENT_TOOLS_SCRIPT_ASSET") <
                commonAssetList.indexOf("COMMON_SCRIPT_ASSET")
        )
        assertFalse(commonScript.contains("state.speedHookedVideos.add(video);"))
        assertFalse(commonScript.contains("video.playbackRate = speed;"))
        assertFalse(commonScript.contains("state.bestQualityAttempts.set(video, { at: now"))
        assertFalse(commonScript.contains("const normalizedSpeed = Number(speed || 1);"))
        assertFalse(commonScript.contains("invokeSiteVideoCapability(video, 'setPlaybackSpeed', [state.fullscreenPlaybackSpeed])"))
        assertFalse(commonScript.contains("videoQueryTools.forEach(applyVideoSpeed);"))
        assertFalse(enhancementScript.contains("targetState.speedHookedVideos = new WeakSet();"))
        assertFalse(enhancementScript.contains("targetState.bestQualityAttempts = new WeakMap();"))
        assertFalse(enhancementScript.contains("function invokeSiteVideoCapability(video, action, args, options)"))
        assertFalse(enhancementScript.contains("function hasSiteVideoCapability(video, action, options)"))
        assertFalse(enhancementScript.contains("function call(callbacks, name)"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
