package com.example.videobrowser.video

/**
 * 测试阅读提示：
 * 这个测试文件验证“Web View Video Protocol Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WebViewVideoProtocolTest {
    /**
     * 测试函数 `timelineNormalizesBridgeNumbers`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `timeline Normalizes Bridge Numbers` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun timelineNormalizesBridgeNumbers() {
        val timeline = WebViewVideoTimeline.fromBridge(
            positionMs = 12_345.8,
            durationMs = 60_000.0
        )
        val invalidTimeline = WebViewVideoTimeline.fromBridge(
            positionMs = Double.NaN,
            durationMs = -1.0
        )

        assertEquals(12_345L, timeline.positionMs)
        assertEquals(60_000L, timeline.durationMs)
        assertNull(invalidTimeline.positionMs)
        assertNull(invalidTimeline.durationMs)
    }

    /**
     * 测试函数 `seekCommandsRenderSafeEnhancerCallsInSeconds`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `seek Commands Render Safe Enhancer Calls In Seconds` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun seekCommandsRenderSafeEnhancerCallsInSeconds() {
        val seekBy = WebViewVideoCommand.SeekBy(offsetMs = 1_500L).toJavascript()
        val seekTo = WebViewVideoCommand.SeekTo(positionMs = 65_250L).toJavascript()

        assertTrue(seekBy.contains("var enhancer=window.VideoBrowserEnhancer;"))
        assertTrue(seekBy.contains("typeof enhancer.seekBy==='function'"))
        assertTrue(seekBy.contains("enhancer.seekBy(1.500);"))
        assertTrue(seekTo.contains("typeof enhancer.seekTo==='function'"))
        assertTrue(seekTo.contains("enhancer.seekTo(65.250);"))
    }

    /**
     * 测试函数 `playbackCommandsKeepFallbackGuardsAndNormalizeArguments`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `playback Commands Keep Fallback Guards And Normalize Arguments` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun playbackCommandsKeepFallbackGuardsAndNormalizeArguments() {
        val speed = WebViewVideoCommand.SetPlaybackSpeed(speed = 1.5f).toJavascript()
        val invalidSpeed = WebViewVideoCommand.SetPlaybackSpeed(speed = Float.NaN).toJavascript()
        val startReverse = WebViewVideoCommand.StartDirectionalPlayback(direction = -7).toJavascript()

        assertTrue(speed.contains("typeof enhancer.setPlaybackSpeed==='function'"))
        assertTrue(speed.contains("enhancer.setPlaybackSpeed(1.50);"))
        assertTrue(invalidSpeed.contains("enhancer.setPlaybackSpeed(1.00);"))
        assertTrue(startReverse.contains("enhancer.startDirectionalPlayback(-1);"))
        assertFalse(startReverse.contains("-7"))
    }

    /**
     * 测试函数 `togglePlayPauseAlsoWakesControlsWhenAvailable`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `toggle Play Pause Also Wakes Controls When Available` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun togglePlayPauseAlsoWakesControlsWhenAvailable() {
        val script = WebViewVideoCommand.TogglePlayPause.toJavascript()

        assertTrue(script.contains("enhancer.togglePlayPause();"))
        assertTrue(script.contains("enhancer.wakeControls();"))
        assertTrue(script.indexOf("enhancer.togglePlayPause();") < script.indexOf("enhancer.wakeControls();"))
    }
}
