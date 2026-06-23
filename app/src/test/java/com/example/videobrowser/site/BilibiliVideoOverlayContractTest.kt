package com.example.videobrowser.site

import com.example.videobrowser.testutil.projectFile

/**
 * 测试阅读提示：
 * 这个测试文件验证“Bilibili Video Overlay Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
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
        val helperScript = projectFile("src/main/assets/scripts/site_adapter_helpers.js").readText()
        val overlayScript = projectFile("src/main/assets/scripts/bilibili_overlay_cleanup.js").readText()

        assertTrue(helperScript.contains("tools.emptyQuery = tools.emptyQuery || function ()"))
        assertTrue(helperScript.contains("tools.noop = tools.noop || function ()"))
        assertTrue(overlayScript.contains("window.VideoBrowserBilibiliOverlayCleanup = tools"))
        assertTrue(overlayScript.contains("var videoQueryTools = window.VideoBrowserVideoQueryTools || {};"))
        assertTrue(overlayScript.contains("tools.hideVideoPlayPauseOverlays = tools.hideVideoPlayPauseOverlays || function (adapterTools)"))
        assertTrue(overlayScript.contains("var adapterDefaults = window.VideoBrowserSiteAdapterTools || {};"))
        assertTrue(overlayScript.contains("query: typeof tools.query === 'function' ? tools.query : adapterDefaults.emptyQuery"))
        assertTrue(overlayScript.contains("hideElement: typeof tools.hideElement === 'function' ? tools.hideElement : adapterDefaults.noop"))
        assertTrue(overlayScript.contains("isActiveVideo: typeof videoQueryTools.isActive === 'function' ? videoQueryTools.isActive : adapterDefaults.noop"))
        assertTrue(overlayScript.contains("function isLikelyCenterPlaybackOverlay(element, video, helpers)"))
        assertTrue(script.contains("var overlayCleanup = window.VideoBrowserBilibiliOverlayCleanup || {};"))
        assertTrue(script.contains("overlayCleanup.hideVideoPlayPauseOverlays(adapterTools);"))
        assertTrue(script.contains("hideVideoPlayPauseOverlays();"))
        assertFalse(script.contains("enableVideoControls();\n      hideVideoPlayPauseOverlays();"))
        assertTrue(overlayScript.contains(".mplayer-play-icon"))
        assertTrue(overlayScript.contains(".bpx-player-state-wrap"))
        assertTrue(overlayScript.contains("bilibili-video-play-overlay"))
        assertFalse(overlayScript.contains("function emptyQuery()"))
        assertFalse(overlayScript.contains("function noop()"))
        assertFalse(script.contains("var playbackOverlaySelectors = ["))
        assertFalse(script.contains("function matchingVideoForOverlay(element, videos)"))
    }

    @Test
    fun bilibiliAdapterOwnsBrowserChoicePromptCleanup() {
        val commonScript = projectFile("src/main/assets/scripts/common.js").readText()
        val helperScript = projectFile("src/main/assets/scripts/site_adapter_helpers.js").readText()
        val bilibiliScript = projectFile("src/main/assets/scripts/bilibili.js").readText()
        val browserChoiceScript = projectFile("src/main/assets/scripts/bilibili_browser_choice_cleanup.js").readText()

        assertTrue(helperScript.contains("tools.normalizeText = function (value)"))
        assertTrue(helperScript.contains("tools.emptyQuery = tools.emptyQuery || function ()"))
        assertTrue(helperScript.contains("tools.noop = tools.noop || function ()"))
        assertTrue(helperScript.contains("normalizeText: tools.normalizeText"))
        assertTrue(browserChoiceScript.contains("window.VideoBrowserBilibiliBrowserChoiceCleanup = tools"))
        assertTrue(browserChoiceScript.contains("tools.dismissPrompts = tools.dismissPrompts || function (adapterTools)"))
        assertTrue(browserChoiceScript.contains("var adapterDefaults = window.VideoBrowserSiteAdapterTools || {};"))
        assertTrue(browserChoiceScript.contains("query: typeof tools.query === 'function' ? tools.query : adapterDefaults.emptyQuery"))
        assertTrue(browserChoiceScript.contains("hideElement: typeof tools.hideElement === 'function' ? tools.hideElement : adapterDefaults.noop"))
        assertTrue(browserChoiceScript.contains("normalizeText: typeof tools.normalizeText === 'function' ? tools.normalizeText : adapterDefaults.normalizeText"))
        assertTrue(browserChoiceScript.contains("safeRect: typeof tools.safeRect === 'function' ? tools.safeRect : adapterDefaults.nullResult"))
        assertTrue(bilibiliScript.contains("var browserChoiceCleanup = window.VideoBrowserBilibiliBrowserChoiceCleanup || {};"))
        assertTrue(bilibiliScript.contains("function dismissBrowserChoicePrompts()"))
        assertTrue(bilibiliScript.contains("browserChoiceCleanup.dismissPrompts(adapterTools);"))
        assertTrue(bilibiliScript.contains("dismissBrowserChoicePrompts();"))
        assertTrue(browserChoiceScript.contains("function findBrowserChoicePromptRoot(element, helpers)"))
        assertTrue(browserChoiceScript.contains("var rect = helpers.safeRect(current);"))
        assertTrue(browserChoiceScript.contains("bilibili-browser-choice"))
        assertFalse(browserChoiceScript.contains("function normalizeText(value)"))
        assertFalse(browserChoiceScript.contains("function emptyQuery()"))
        assertFalse(browserChoiceScript.contains("function noop()"))
        assertFalse(browserChoiceScript.contains("getBoundingClientRect()"))
        assertFalse(bilibiliScript.contains("var normalizeText = adapterTools.normalizeText;"))
        assertFalse(bilibiliScript.contains("function findBrowserChoicePromptRoot(element)"))
        assertFalse(bilibiliScript.contains("function hideBrowserChoiceBackdrops(promptRoot)"))
        assertFalse(commonScript.contains("dismissBilibiliBrowserChoicePrompts"))
        assertFalse(commonScript.contains("findBilibiliPromptRoot"))
        assertFalse(commonScript.contains("hideBilibiliPromptBackdrops"))
    }

}
