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
    /**
     * 测试函数 `commonScriptDelegatesVideoActionsToSiteCapabilitiesBeforeGenericVideoFallback`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `common Script Delegates Video Actions To Site Capabilities Before Generic Video Fallback` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `commonVideoEnhancementRoutesControlsThroughSiteCapabilities`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `common Video Enhancement Routes Controls Through Site Capabilities` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun commonVideoEnhancementRoutesControlsThroughSiteCapabilities() {
        val script = projectFile("src/main/assets/scripts/common.js").readText()
        val enhanceVideosBody = functionBody(script, "function enhanceVideos()")

        assertTrue(enhanceVideosBody.contains("enableVideoControls(video);"))
        assertFalse(enhanceVideosBody.contains("enableNativeVideoControls(video);"))
    }

    /**
     * 测试函数 `commonVideoEnhancementRequestsBestQualityThroughSiteCapabilitiesWithoutCustomUi`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `common Video Enhancement Requests Best Quality Through Site Capabilities Without Custom Ui` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `commonWakeControlsRoutesControlVisibilityThroughSiteCapabilities`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `common Wake Controls Routes Control Visibility Through Site Capabilities` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun commonWakeControlsRoutesControlVisibilityThroughSiteCapabilities() {
        val script = projectFile("src/main/assets/scripts/common.js").readText()
        val wakeControlsBody = functionBody(script, "function wakeVideoControls(video)")

        assertTrue(wakeControlsBody.contains("enableVideoControls(target);"))
        assertFalse(wakeControlsBody.contains("enableNativeVideoControls(target);"))
    }

    /**
     * 测试函数 `commonScriptReportsVideoControlDiagnosticsToNativeLog`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `common Script Reports Video Control Diagnostics To Native Log` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun commonScriptReportsVideoControlDiagnosticsToNativeLog() {
        val script = projectFile("src/main/assets/scripts/common.js").readText()

        assertTrue(script.contains("function logVideoDiagnostic(event, details)"))
        assertTrue(script.contains("bridge.logVideoEvent(message);"))
        assertTrue(script.contains("logVideoDiagnostic('enable-controls-site'"))
        assertTrue(script.contains("logVideoDiagnostic('enable-controls-custom-player'"))
        assertTrue(script.contains("logVideoDiagnostic('enable-controls-native'"))
    }

    /**
     * 测试函数 `commonScriptKeepsSelectedFullscreenSpeedWhenPageFeaturesReapply`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `common Script Keeps Selected Fullscreen Speed When Page Features Reapply` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun commonScriptKeepsSelectedFullscreenSpeedWhenPageFeaturesReapply() {
        val script = projectFile("src/main/assets/scripts/common.js").readText()
        val applyBody = functionBody(script, "apply: function (config)")

        assertFalse(applyBody.contains("state.fullscreenPlaybackSpeed = 1;"))
    }

    /**
     * 测试函数 `commonScriptReappliesSitePlaybackSpeedBeforeGenericFallback`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `common Script Reapplies Site Playback Speed Before Generic Fallback` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `commonScriptAvoidsNativeControlsWhenUnknownSiteAlreadyHasCustomPlayerControls`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `common Script Avoids Native Controls When Unknown Site Already Has Custom Player Controls` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `bilibiliAdapterExposesPlatformVideoCapabilitiesForCommonBroker`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `bilibili Adapter Exposes Platform Video Capabilities For Common Broker` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `unsupportedSiteAdaptersDoNotFakeBestQualityCapability`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `unsupported Site Adapters Do Not Fake Best Quality Capability` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `platformSiteAdaptersHandleControlEnablementWithoutForcingNativeControls`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `platform Site Adapters Handle Control Enablement Without Forcing Native Controls` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun platformSiteAdaptersHandleControlEnablementWithoutForcingNativeControls() {
        val helperScript = projectFile("src/main/assets/scripts/site_adapter_helpers.js").readText()

        assertTrue(helperScript.contains("tools.removeNativeVideoControls = function (video, adapterId)"))
        assertTrue(helperScript.contains("video.controls = false"))
        assertTrue(helperScript.contains("video.removeAttribute('controls')"))
        assertTrue(helperScript.contains("tools.logVideoDiagnostic(adapterId, 'remove-native-controls'"))

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
            assertTrue(script.contains("siteTools.removeNativeVideoControls(video, '$adapterId')"))
            assertTrue(script.contains("query('video').forEach(removeNativeVideoControls);"))
            assertFalse(script.contains("video.controls = true"))
            assertFalse(script.contains("setAttribute('controls', 'controls')"))
        }
    }

    /**
     * 测试函数 `functionBody`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `function Body` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param script 参数类型为 `String`，表示函数执行 `script` 相关逻辑时需要读取或处理的输入。
     * @param signature 参数类型为 `String`，表示函数执行 `signature` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 测试函数 `projectFile`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `project File` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param path 参数类型为 `String`，表示函数执行 `path` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
