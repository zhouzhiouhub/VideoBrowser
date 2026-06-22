package com.example.videobrowser.video

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoWakeToolsContractTest {
    @Test
    fun `video control wake events are owned by shared wake module`() {
        val wakeScript = projectFile("src/main/assets/scripts/video_wake_tools.js").readText()
        val commonScript = projectFile("src/main/assets/scripts/common.js").readText()
        val scriptLoader = projectFile("src/main/java/com/example/videobrowser/inject/ScriptLoader.kt").readText()
        val commonAssetList = scriptLoader.substringAfter("val COMMON_SCRIPT_ASSETS = listOf(")

        assertTrue(wakeScript.contains("window.VideoBrowserVideoWakeTools = tools"))
        assertTrue(wakeScript.contains("tools.wake = tools.wake || function (video, options)"))
        assertTrue(wakeScript.contains("function dispatchMouseWakeEvent(target, type, clientX, clientY)"))
        assertTrue(wakeScript.contains("function dispatchPointerWakeEvent(target, clientX, clientY)"))
        assertTrue(wakeScript.contains("new MouseEvent(type, {"))
        assertTrue(wakeScript.contains("new PointerEvent('pointermove', {"))
        assertTrue(wakeScript.contains("callbacks.enableVideoControls(target);"))
        assertTrue(commonScript.contains("const videoWakeTools = window.VideoBrowserVideoWakeTools"))
        assertTrue(commonScript.contains("return videoWakeTools.wake(video, {"))
        assertTrue(commonScript.contains("enableVideoControls: enableVideoControls"))
        assertTrue(scriptLoader.contains("VIDEO_WAKE_TOOLS_SCRIPT_ASSET"))
        assertTrue(
            commonAssetList.indexOf("VIDEO_WAKE_TOOLS_SCRIPT_ASSET") <
                commonAssetList.indexOf("COMMON_SCRIPT_ASSET")
        )
        assertFalse(commonScript.contains("function dispatchControlWakeEvent(target, type, clientX, clientY)"))
        assertFalse(commonScript.contains("function dispatchPointerWakeEvent(target, clientX, clientY)"))
        assertFalse(commonScript.contains("new MouseEvent(type, {"))
        assertFalse(commonScript.contains("new PointerEvent('pointermove', {"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
