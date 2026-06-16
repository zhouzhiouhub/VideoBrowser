package com.example.videobrowser.video

/**
 * 测试阅读提示：
 * 这个测试文件验证“Web View Video Protocol Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class WebViewVideoProtocolWiringContractTest {
    /**
     * 测试函数 `fullscreenControllerUsesTypedWebViewVideoProtocol`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `fullscreen Controller Uses Typed Web View Video Protocol` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun fullscreenControllerUsesTypedWebViewVideoProtocol() {
        val source = File(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoController.kt"
        ).readText()

        assertTrue(source.contains("WebViewVideoTimeline.fromBridge("))
        assertTrue(source.contains("evaluateWebVideoCommand(WebViewVideoCommand.WakeControls)"))
        assertTrue(source.contains("evaluateWebVideoCommand(WebViewVideoCommand.SeekBy("))
        assertTrue(source.contains("evaluateWebVideoCommand(WebViewVideoCommand.SeekTo("))
        assertTrue(source.contains("evaluateWebVideoCommand(WebViewVideoCommand.TogglePlayPause)"))
        assertTrue(source.contains("evaluateWebVideoCommand(WebViewVideoCommand.SetPlaybackSpeed("))
        assertTrue(source.contains("evaluateWebVideoCommand(WebViewVideoCommand.StartDirectionalPlayback("))
        assertTrue(source.contains("evaluateWebVideoCommand(WebViewVideoCommand.StopDirectionalPlayback)"))
        assertTrue(source.contains("evaluateWebVideoCommand(WebViewVideoCommand.ExitFullscreen)"))
        assertFalse(source.contains("typeof window.VideoBrowserEnhancer.seekBy"))
        assertFalse(source.contains("typeof window.VideoBrowserEnhancer.setPlaybackSpeed"))
    }

    /**
     * 测试函数 `mainActivityUsesTypedProtocolForBackPressedFullscreenExit`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `main Activity Uses Typed Protocol For Back Pressed Fullscreen Exit` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun mainActivityUsesTypedProtocolForBackPressedFullscreenExit() {
        val source = File("src/main/java/com/example/videobrowser/MainActivity.kt").readText()

        assertTrue(source.contains("WebViewVideoCommand.ExitFullscreen.toJavascript()"))
        assertFalse(source.contains("EXIT_VIDEO_FULLSCREEN_SCRIPT"))
        assertFalse(source.contains("window.VideoBrowserEnhancer.exitFullscreen()"))
    }
}
