package com.example.videobrowser.site

import java.io.File
import org.junit.Assert.assertFalse
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
    fun commonVideoEnhancementRoutesControlsThroughSiteCapabilities() {
        val script = projectFile("src/main/assets/scripts/common.js").readText()
        val enhanceVideosBody = functionBody(script, "function enhanceVideos()")

        assertTrue(enhanceVideosBody.contains("enableVideoControls(video);"))
        assertFalse(enhanceVideosBody.contains("enableNativeVideoControls(video);"))
    }

    @Test
    fun commonWakeControlsRoutesControlVisibilityThroughSiteCapabilities() {
        val script = projectFile("src/main/assets/scripts/common.js").readText()
        val wakeControlsBody = functionBody(script, "function wakeVideoControls(video)")

        assertTrue(wakeControlsBody.contains("enableVideoControls(target);"))
        assertFalse(wakeControlsBody.contains("enableNativeVideoControls(target);"))
    }

    @Test
    fun commonScriptReportsVideoControlDiagnosticsToNativeLog() {
        val script = projectFile("src/main/assets/scripts/common.js").readText()

        assertTrue(script.contains("function logVideoDiagnostic(event, details)"))
        assertTrue(script.contains("bridge.logVideoEvent(message);"))
        assertTrue(script.contains("logVideoDiagnostic('enable-controls-site'"))
        assertTrue(script.contains("logVideoDiagnostic('enable-controls-native'"))
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

    @Test
    fun platformSiteAdaptersHandleControlEnablementWithoutForcingNativeControls() {
        listOf(
            "youtube" to "src/main/assets/scripts/youtube.js",
            "bilibili" to "src/main/assets/scripts/bilibili.js",
            "iqiyi" to "src/main/assets/scripts/iqiyi.js",
            "tencent" to "src/main/assets/scripts/tencent.js",
            "youku" to "src/main/assets/scripts/youku.js"
        ).forEach { (adapterId, path) ->
            val script = projectFile(path).readText()

            assertTrue(script.contains("adapters.$adapterId.videoCapabilities"))
            assertTrue(script.contains("enableControls: function (video)"))
            assertTrue(script.contains("function removeNativeVideoControls(video)"))
            assertTrue(script.contains("query('video').forEach(removeNativeVideoControls);"))
            assertTrue(script.contains("logVideoDiagnostic('remove-native-controls'"))
            assertTrue(script.contains("video.controls = false"))
            assertTrue(script.contains("video.removeAttribute('controls')"))
            assertFalse(script.contains("video.controls = true"))
            assertFalse(script.contains("setAttribute('controls', 'controls')"))
        }
    }

    private fun functionBody(script: String, signature: String): String {
        val start = script.indexOf(signature)
        assertTrue("Missing $signature", start >= 0)
        val braceStart = script.indexOf('{', start)
        assertTrue("Missing body for $signature", braceStart >= 0)

        var depth = 0
        for (index in braceStart until script.length) {
            when (script[index]) {
                '{' -> depth += 1
                '}' -> {
                    depth -= 1
                    if (depth == 0) {
                        return script.substring(braceStart + 1, index)
                    }
                }
            }
        }
        error("Unterminated function body for $signature")
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
