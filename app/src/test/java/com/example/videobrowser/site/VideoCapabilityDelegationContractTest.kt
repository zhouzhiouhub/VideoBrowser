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
        val brokerScript = projectFile("src/main/assets/scripts/site_video_capability_broker.js").readText()
        val playbackScript = projectFile("src/main/assets/scripts/video_playback_tools.js").readText()
        val enhancementScript = projectFile("src/main/assets/scripts/video_enhancement_tools.js").readText()

        assertTrue(brokerScript.contains("broker.invoke = broker.invoke || function (video, action, args)"))
        assertTrue(brokerScript.contains("broker.has = broker.has || function (video, action)"))
        assertTrue(script.contains("const invokeSiteVideoCapability = siteVideoCapabilityBroker.invoke"))
        assertTrue(script.contains("const hasSiteVideoCapability = siteVideoCapabilityBroker.has"))
        assertTrue(script.contains("invokeSiteVideoCapability(video, 'enableControls', [])"))
        assertTrue(script.contains("videoPlaybackTools.togglePlayPause(video, {"))
        assertTrue(playbackScript.contains("invokeSiteVideoCapability(video, 'togglePlayPause', [], options)"))
        assertTrue(playbackScript.contains("invokeSiteVideoCapability(video, 'seekBy', [offsetSeconds], options)"))
        assertTrue(playbackScript.contains("invokeSiteVideoCapability(video, 'seekTo', [targetSeconds], options)"))
        assertTrue(script.contains("invokeSiteVideoCapability(video, 'setPlaybackSpeed', [state.fullscreenPlaybackSpeed])"))
        assertTrue(enhancementScript.contains("invokeSiteVideoCapability(video, 'preferBestQuality', [], config)"))
        assertTrue(enhancementScript.contains("invokeSiteVideoCapability(video, 'setPlaybackSpeed', [speed], config)"))
        assertFalse(enhancementScript.contains("if (hasSiteVideoCapability(video, 'setPlaybackSpeed')) return;"))
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
        val enhancementScript = projectFile("src/main/assets/scripts/video_enhancement_tools.js").readText()
        val enhanceVideosBody = functionBody(script, "function enhanceVideos()")

        assertTrue(script.contains("function preferBestVideoQuality(video)"))
        assertTrue(enhancementScript.contains("tools.preferBestQuality = tools.preferBestQuality || function (video, state, options)"))
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
        val wakeScript = projectFile("src/main/assets/scripts/video_wake_tools.js").readText()
        val wakeControlsBody = functionBody(script, "function wakeVideoControls(video)")

        assertTrue(wakeControlsBody.contains("return videoWakeTools.wake(video, {"))
        assertTrue(wakeControlsBody.contains("enableVideoControls: enableVideoControls"))
        assertTrue(wakeScript.contains("callbacks.enableVideoControls(target);"))
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
        val nativeBridgeScript = projectFile("src/main/assets/scripts/native_bridge.js").readText()

        assertTrue(nativeBridgeScript.contains("window.VideoBrowserNativeBridge = bridgeTools"))
        assertTrue(nativeBridgeScript.contains("bridge.logVideoEvent(message);"))
        assertTrue(nativeBridgeScript.contains("bridgeTools.logPageVideoDiagnostic = bridgeTools.logPageVideoDiagnostic || function (event, details)"))
        assertTrue(nativeBridgeScript.contains("bridgeTools.videoLogDetails = bridgeTools.videoLogDetails || function (video, extra)"))
        assertTrue(script.contains("const logVideoDiagnostic = nativeBridge.logPageVideoDiagnostic"))
        assertTrue(script.contains("const videoLogDetails = nativeBridge.videoLogDetails"))
        assertTrue(script.contains("logVideoDiagnostic('enable-controls-site'"))
        assertTrue(script.contains("logVideoDiagnostic('enable-controls-custom-player'"))
        assertTrue(script.contains("logVideoDiagnostic('enable-controls-native'"))
        assertFalse(script.contains("function logVideoDiagnostic(event, details)"))
        assertFalse(script.contains("function videoLogDetails(video, extra)"))
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
        val enhancementScript = projectFile("src/main/assets/scripts/video_enhancement_tools.js").readText()

        assertTrue(enhancementScript.contains("const speed = tools.desiredSpeed(video, state, config);"))
        assertTrue(enhancementScript.contains("invokeSiteVideoCapability(video, 'setPlaybackSpeed', [speed], config)"))
        assertTrue(
            enhancementScript.indexOf("invokeSiteVideoCapability(video, 'setPlaybackSpeed', [speed], config)") <
                enhancementScript.indexOf("video.playbackRate = speed;")
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
        val geometryScript = projectFile("src/main/assets/scripts/geometry.js").readText()
        val videoControlToolsScript = projectFile("src/main/assets/scripts/video_control_tools.js").readText()
        val customControlDetectorScript = projectFile("src/main/assets/scripts/video_custom_control_detector.js").readText()
        val enableVideoControlsBody = functionBody(script, "function enableVideoControls(video)")

        assertTrue(videoControlToolsScript.contains("window.VideoBrowserVideoControlTools = tools"))
        assertTrue(videoControlToolsScript.contains("tools.enableNativeControls = tools.enableNativeControls || function (video)"))
        assertTrue(videoControlToolsScript.contains("tools.removeNativeControls = tools.removeNativeControls || function (video)"))
        assertTrue(videoControlToolsScript.contains("tools.cleanupLegacyOverlays = tools.cleanupLegacyOverlays || function (state, options)"))
        assertTrue(videoControlToolsScript.contains("document.querySelectorAll('.__videobrowser_video_controls__')"))
        assertTrue(script.contains("const videoControlTools = window.VideoBrowserVideoControlTools"))
        assertTrue(customControlDetectorScript.contains("detector.hasControls = detector.hasControls || function (video)"))
        assertTrue(script.contains("function removeNativeVideoControls(video, reason)"))
        assertTrue(script.contains("videoControlTools.removeNativeControls(video)"))
        assertTrue(script.contains("videoControlTools.enableNativeControls(video)"))
        assertTrue(script.contains("videoControlTools.cleanupLegacyOverlays(state, {"))
        assertFalse(script.contains("document.querySelectorAll('.__videobrowser_video_controls__')"))
        assertFalse(script.contains("try { video.controls = false; }"))
        assertFalse(script.contains("try { video.removeAttribute('controls'); }"))
        assertTrue(geometryScript.contains("geometry.expandedRect = geometry.expandedRect || function (rect, amount)"))
        assertTrue(geometryScript.contains("geometry.rectsOverlap = geometry.rectsOverlap || function (first, second)"))
        assertTrue(customControlDetectorScript.contains("geometry.expandedRect(videoRect, 12)"))
        assertTrue(customControlDetectorScript.contains("geometry.rectsOverlap(rect,"))
        assertTrue(customControlDetectorScript.contains("'.xgplayer-controls'"))
        assertTrue(customControlDetectorScript.contains("'.dplayer-controller'"))
        assertTrue(customControlDetectorScript.contains("'.art-controls'"))
        assertTrue(customControlDetectorScript.contains("'.vjs-control-bar'"))
        assertTrue(customControlDetectorScript.contains("'[class*=\"player-control\"]'"))
        assertTrue(enableVideoControlsBody.contains("customControlDetector.hasControls(video)"))
        assertTrue(enableVideoControlsBody.contains("removeNativeVideoControls(video, 'custom-player')"))
        assertTrue(enableVideoControlsBody.contains("logVideoDiagnostic('enable-controls-custom-player'"))
        assertTrue(enableVideoControlsBody.indexOf("customControlDetector.hasControls(video)") <
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
        val playerApiScript = projectFile("src/main/assets/scripts/bilibili_player_api.js").readText()
        val qualityScript = projectFile("src/main/assets/scripts/bilibili_quality_tools.js").readText()

        assertTrue(script.contains("adapters.bilibili.videoCapabilities"))
        assertTrue(script.contains("supports: function (video)"))
        assertTrue(script.contains("enableControls: function (video)"))
        assertTrue(script.contains("togglePlayPause: function (video)"))
        assertTrue(script.contains("seekBy: function (video, offsetSeconds)"))
        assertTrue(script.contains("seekTo: function (video, targetSeconds)"))
        assertTrue(script.contains("setPlaybackSpeed: function (video, speed)"))
        assertTrue(script.contains("preferBestQuality: function (video)"))
        assertTrue(script.contains("var playerApi = window.VideoBrowserBilibiliPlayerApi || {};"))
        assertTrue(script.contains("var qualityTools = window.VideoBrowserBilibiliQualityTools || {};"))
        assertTrue(script.contains("qualityTools.preferBestQuality(adapterTools, playerApi);"))
        assertTrue(script.contains("return typeof playerApi.hasMethod === 'function' && playerApi.hasMethod(action, video);"))
        assertTrue(playerApiScript.contains("window.VideoBrowserBilibiliPlayerApi = tools"))
        assertTrue(playerApiScript.contains("tools.find = tools.find || function ()"))
        assertTrue(playerApiScript.contains("tools.methodsFor = tools.methodsFor || function (action, video)"))
        assertTrue(playerApiScript.contains("'setPlaybackRate'"))
        assertTrue(playerApiScript.contains("'seek'"))
        assertTrue(playerApiScript.contains("tools.handledValue = tools.handledValue || function (callResult, fallbackValue)"))
        assertTrue(script.contains("playerApi.handledValue(callPlayerMethod("))
        assertTrue(qualityScript.contains("window.VideoBrowserBilibiliQualityTools = tools"))
        assertTrue(qualityScript.contains("tools.preferBestQuality = tools.preferBestQuality || function (adapterTools, playerApi)"))
        assertTrue(qualityScript.contains("function qualityScore(text)"))
        assertTrue(qualityScript.contains("handledValue: typeof api.handledValue === 'function' ? api.handledValue : nullCaller"))
        assertTrue(qualityScript.contains("helpers.logVideoDiagnostic('quality-menu-select'"))
        assertFalse(script.contains("function handledValue(callResult, fallbackValue)"))
        assertFalse(qualityScript.contains("function handledValue(callResult, fallbackValue)"))
        assertFalse(script.contains("function qualityScore(text)"))
        assertFalse(script.contains("function findBilibiliPlayerApi()"))
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
        val videoControlToolsScript = projectFile("src/main/assets/scripts/video_control_tools.js").readText()

        assertTrue(videoControlToolsScript.contains("try { video.controls = false; }"))
        assertTrue(videoControlToolsScript.contains("try { video.removeAttribute('controls'); }"))
        assertTrue(helperScript.contains("var nativeBridge = window.VideoBrowserNativeBridge || {}"))
        assertTrue(helperScript.contains("var videoControlTools = window.VideoBrowserVideoControlTools || {}"))
        assertTrue(helperScript.contains("nativeBridge.logVideoDiagnostic(event, details,"))
        assertTrue(helperScript.contains("tools.videoSource = nativeBridge.videoSource"))
        assertFalse(helperScript.contains("bridge.logVideoEvent(message);"))
        assertTrue(helperScript.contains("tools.removeNativeVideoControls = function (video, adapterId)"))
        assertTrue(helperScript.contains("videoControlTools.removeNativeControls(video)"))
        assertFalse(helperScript.contains("video.controls = false"))
        assertFalse(helperScript.contains("video.removeAttribute('controls')"))
        assertTrue(helperScript.contains("tools.logVideoDiagnostic(adapterId, 'remove-native-controls'"))
        assertTrue(helperScript.contains("tools.scopedAdapterTools = function (adapterId, options)"))
        assertTrue(helperScript.contains("removeNativeVideoControls: function (video)"))
        assertTrue(helperScript.contains("tools.registerBasicAdapter = function (options)"))
        assertTrue(helperScript.contains("adapters[adapterId].videoCapabilities"))
        assertTrue(helperScript.contains("enableControls: function (video)"))
        assertTrue(helperScript.contains("adapterTools.query('video').forEach(adapterTools.removeNativeVideoControls);"))

        listOf(
            "youtube" to "src/main/assets/scripts/youtube.js",
            "iqiyi" to "src/main/assets/scripts/iqiyi.js",
            "tencent" to "src/main/assets/scripts/tencent.js",
            "youku" to "src/main/assets/scripts/youku.js"
        ).forEach { (adapterId, path) ->
            val script = projectFile(path).readText()

            assertTrue(script.contains("registerBasicAdapter({"))
            assertTrue(script.contains("adapterId: '$adapterId'"))
            assertFalse(script.contains("adapters.$adapterId.videoCapabilities"))
            assertFalse(script.contains("function removeNativeVideoControls(video)"))
            assertFalse(script.contains("query('video').forEach(removeNativeVideoControls);"))
            assertFalse(script.contains("video.controls = true"))
            assertFalse(script.contains("setAttribute('controls', 'controls')"))
        }

        val bilibiliScript = projectFile("src/main/assets/scripts/bilibili.js").readText()
        assertTrue(bilibiliScript.contains("adapters.bilibili.videoCapabilities"))
        assertTrue(bilibiliScript.contains("enableControls: function (video)"))
        assertTrue(bilibiliScript.contains("scopedAdapterTools('bilibili'"))
        assertTrue(bilibiliScript.contains("var removeNativeVideoControls = adapterTools.removeNativeVideoControls"))
        assertFalse(bilibiliScript.contains("siteTools.removeNativeVideoControls(video, 'bilibili')"))
        assertTrue(bilibiliScript.contains("query('video').forEach(removeNativeVideoControls);"))
        assertFalse(bilibiliScript.contains("video.controls = true"))
        assertFalse(bilibiliScript.contains("setAttribute('controls', 'controls')"))
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
