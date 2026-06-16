package com.example.videobrowser.site

/**
 * 测试阅读提示：
 * 这个测试文件验证“Video Capability Delegation Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
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
        assertTrue(script.contains("invokeSiteVideoCapability(video, 'preferBestQuality', [])"))
        assertTrue(script.contains("invokeSiteVideoCapability(video, 'setPlaybackSpeed', [speed])"))
        assertFalse(script.contains("if (hasSiteVideoCapability(video, 'setPlaybackSpeed')) return;"))
    }

    @Test
    fun commonVideoEnhancementRoutesControlsThroughSiteCapabilities() {
        val script = projectFile("src/main/assets/scripts/common.js").readText()
        val enhanceVideosBody = functionBody(script, "function enhanceVideos()")

        assertTrue(enhanceVideosBody.contains("enableVideoControls(video);"))
        assertFalse(enhanceVideosBody.contains("enableNativeVideoControls(video);"))
    }

    @Test
    fun commonVideoEnhancementRequestsBestQualityThroughSiteCapabilitiesWithoutCustomUi() {
        val script = projectFile("src/main/assets/scripts/common.js").readText()
        val enhanceVideosBody = functionBody(script, "function enhanceVideos()")

        assertTrue(script.contains("function preferBestVideoQuality(video)"))
        assertTrue(enhanceVideosBody.contains("preferBestVideoQuality(video);"))
        assertFalse(script.contains("qualitySelector"))
        assertFalse(script.contains("quality-select"))
        assertFalse(script.contains("清晰度选择"))
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
        assertTrue(script.contains("logVideoDiagnostic('enable-controls-custom-player'"))
        assertTrue(script.contains("logVideoDiagnostic('enable-controls-native'"))
    }

    @Test
    fun commonScriptKeepsSelectedFullscreenSpeedWhenPageFeaturesReapply() {
        val script = projectFile("src/main/assets/scripts/common.js").readText()
        val applyBody = functionBody(script, "apply: function (config)")

        assertFalse(applyBody.contains("state.fullscreenPlaybackSpeed = 1;"))
    }

    @Test
    fun commonScriptReappliesSitePlaybackSpeedBeforeGenericFallback() {
        val script = projectFile("src/main/assets/scripts/common.js").readText()
        val applyVideoSpeedBody = functionBody(script, "function applyVideoSpeed(video)")

        assertTrue(applyVideoSpeedBody.contains("const speed = desiredVideoSpeed(video);"))
        assertTrue(applyVideoSpeedBody.contains("invokeSiteVideoCapability(video, 'setPlaybackSpeed', [speed])"))
        assertTrue(
            applyVideoSpeedBody.indexOf("invokeSiteVideoCapability(video, 'setPlaybackSpeed', [speed])") <
                applyVideoSpeedBody.indexOf("video.playbackRate = speed;")
        )
    }

    @Test
    fun commonScriptAvoidsNativeControlsWhenUnknownSiteAlreadyHasCustomPlayerControls() {
        val script = projectFile("src/main/assets/scripts/common.js").readText()
        val enableVideoControlsBody = functionBody(script, "function enableVideoControls(video)")

        assertTrue(script.contains("function hasLikelyCustomPlayerControls(video)"))
        assertTrue(script.contains("function removeNativeVideoControls(video, reason)"))
        assertTrue(script.contains("function expandedRect(rect, amount)"))
        assertTrue(script.contains("function rectsOverlap(first, second)"))
        assertTrue(script.contains("'.xgplayer-controls'"))
        assertTrue(script.contains("'.dplayer-controller'"))
        assertTrue(script.contains("'.art-controls'"))
        assertTrue(script.contains("'.vjs-control-bar'"))
        assertTrue(script.contains("'[class*=\"player-control\"]'"))
        assertTrue(enableVideoControlsBody.contains("hasLikelyCustomPlayerControls(video)"))
        assertTrue(enableVideoControlsBody.contains("removeNativeVideoControls(video, 'custom-player')"))
        assertTrue(enableVideoControlsBody.contains("logVideoDiagnostic('enable-controls-custom-player'"))
        assertTrue(enableVideoControlsBody.indexOf("hasLikelyCustomPlayerControls(video)") <
            enableVideoControlsBody.indexOf("enableNativeVideoControls(video)"))
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
        assertTrue(script.contains("preferBestQuality: function (video)"))
        assertTrue(script.contains("findBilibiliPlayerApi()"))
        assertTrue(script.contains("'setPlaybackRate'"))
        assertTrue(script.contains("'seek'"))
    }

    @Test
    fun unsupportedSiteAdaptersDoNotFakeBestQualityCapability() {
        listOf(
            "youtube" to "src/main/assets/scripts/youtube.js",
            "iqiyi" to "src/main/assets/scripts/iqiyi.js",
            "tencent" to "src/main/assets/scripts/tencent.js",
            "youku" to "src/main/assets/scripts/youku.js"
        ).forEach { (_, path) ->
            val script = projectFile(path).readText()

            assertFalse(script.contains("preferBestQuality"))
        }
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
