package com.example.videobrowser.site

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoCapabilityDelegationContractTest {
    @Test
    fun commonScriptDelegatesVideoActionsToSiteCapabilitiesBeforeGenericVideoFallback() {
        val script = projectFile("src/main/assets/scripts/common.js").readText()

        assertTrue(script.contains("function invokeSiteVideoCapability(video, action, args)"))
        assertTrue(script.contains("function hasSiteVideoCapability(video, action)"))
        assertTrue(script.contains("invokeSiteVideoCapability(video, 'enableControls', [])"))
        assertTrue(script.contains("invokeSiteVideoCapability(video, 'togglePlayPause', [])"))
        assertTrue(script.contains("invokeSiteVideoCapability(video, 'seekBy', [offsetSeconds])"))
        assertTrue(script.contains("invokeSiteVideoCapability(video, 'seekTo', [targetSeconds])"))
        assertTrue(script.contains("invokeSiteVideoCapability(video, 'setPlaybackSpeed', [state.fullscreenPlaybackSpeed])"))
        assertTrue(script.contains("if (hasSiteVideoCapability(video, 'setPlaybackSpeed')) return;"))
    }

    @Test
    fun bilibiliAdapterExposesPlatformVideoCapabilitiesForCommonBroker() {
        val script = projectFile("src/main/assets/scripts/bilibili.js").readText()

        assertTrue(script.contains("adapters.bilibili.videoCapabilities"))
        assertTrue(script.contains("supports: function (video)"))
        assertTrue(script.contains("enableControls: function (video)"))
        assertTrue(script.contains("togglePlayPause: function (video)"))
        assertTrue(script.contains("seekBy: function (video, offsetSeconds)"))
        assertTrue(script.contains("seekTo: function (video, targetSeconds)"))
        assertTrue(script.contains("setPlaybackSpeed: function (video, speed)"))
        assertTrue(script.contains("findBilibiliPlayerApi()"))
        assertTrue(script.contains("'setPlaybackRate'"))
        assertTrue(script.contains("'seek'"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
