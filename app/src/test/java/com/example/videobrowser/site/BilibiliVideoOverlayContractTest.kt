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
    /**
     * 测试函数 `bilibiliAdapterHidesOwnCenterPlaybackOverlayAfterEnablingNativeControls`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `bilibili Adapter Hides Own Center Playback Overlay After Enabling Native Controls` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun bilibiliAdapterHidesOwnCenterPlaybackOverlayAfterEnablingNativeControls() {
        val script = projectFile("src/main/assets/scripts/bilibili.js").readText()
        val overlayScript = projectFile("src/main/assets/scripts/bilibili_overlay_cleanup.js").readText()

        assertTrue(overlayScript.contains("window.VideoBrowserBilibiliOverlayCleanup = tools"))
        assertTrue(overlayScript.contains("tools.hideVideoPlayPauseOverlays = tools.hideVideoPlayPauseOverlays || function (adapterTools)"))
        assertTrue(overlayScript.contains("function isLikelyCenterPlaybackOverlay(element, video, helpers)"))
        assertTrue(script.contains("var overlayCleanup = window.VideoBrowserBilibiliOverlayCleanup || {};"))
        assertTrue(script.contains("overlayCleanup.hideVideoPlayPauseOverlays(adapterTools);"))
        assertTrue(script.contains("hideVideoPlayPauseOverlays();"))
        assertFalse(script.contains("enableVideoControls();\n      hideVideoPlayPauseOverlays();"))
        assertTrue(overlayScript.contains(".mplayer-play-icon"))
        assertTrue(overlayScript.contains(".bpx-player-state-wrap"))
        assertTrue(overlayScript.contains("bilibili-video-play-overlay"))
        assertFalse(script.contains("var playbackOverlaySelectors = ["))
        assertFalse(script.contains("function matchingVideoForOverlay(element, videos)"))
    }

    @Test
    fun bilibiliAdapterOwnsBrowserChoicePromptCleanup() {
        val commonScript = projectFile("src/main/assets/scripts/common.js").readText()
        val helperScript = projectFile("src/main/assets/scripts/site_adapter_helpers.js").readText()
        val bilibiliScript = projectFile("src/main/assets/scripts/bilibili.js").readText()

        assertTrue(helperScript.contains("tools.normalizeText = function (value)"))
        assertTrue(helperScript.contains("normalizeText: tools.normalizeText"))
        assertTrue(bilibiliScript.contains("var normalizeText = adapterTools.normalizeText;"))
        assertTrue(bilibiliScript.contains("function dismissBrowserChoicePrompts()"))
        assertTrue(bilibiliScript.contains("dismissBrowserChoicePrompts();"))
        assertTrue(bilibiliScript.contains("function findBrowserChoicePromptRoot(element)"))
        assertTrue(bilibiliScript.contains("bilibili-browser-choice"))
        assertFalse(commonScript.contains("dismissBilibiliBrowserChoicePrompts"))
        assertFalse(commonScript.contains("findBilibiliPromptRoot"))
        assertFalse(commonScript.contains("hideBilibiliPromptBackdrops"))
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
