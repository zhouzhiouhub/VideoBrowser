package com.example.videobrowser.video

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoWakeToolsContractTest {
    @Test
    fun `video control wake events are owned by shared wake module`() {
        val wakeScript = projectFile("src/main/assets/scripts/video_wake_tools.js").readText()
        val callbacksScript = projectFile("src/main/assets/scripts/video_enhancer_callbacks.js").readText()
        val commonScript = projectFile("src/main/assets/scripts/common.js").readText()
        val scriptLoader = projectFile("src/main/java/com/example/videobrowser/inject/ScriptLoader.kt").readText()
        val commonAssetList = scriptLoader.substringAfter("val COMMON_SCRIPT_ASSETS = listOf(")

        assertTrue(wakeScript.contains("window.VideoBrowserVideoWakeTools = tools"))
        assertTrue(wakeScript.contains("const geometry = window.VideoBrowserGeometry || {}"))
        assertTrue(wakeScript.contains("tools.wake = tools.wake || function (video, options)"))
        assertTrue(wakeScript.contains("function dispatchMouseWakeEvent(target, type, clientX, clientY)"))
        assertTrue(wakeScript.contains("function dispatchPointerWakeEvent(target, clientX, clientY)"))
        assertTrue(wakeScript.contains("const rect = geometry.safeRect(root);"))
        assertTrue(wakeScript.contains("geometry.rectCenterX(rect)"))
        assertTrue(wakeScript.contains("geometry.rectCenterY(rect)"))
        assertTrue(wakeScript.contains("new MouseEvent(type, {"))
        assertTrue(wakeScript.contains("new PointerEvent('pointermove', {"))
        assertTrue(wakeScript.contains("callbacks.enableVideoControls(target);"))
        assertTrue(commonScript.contains("const videoWakeTools = window.VideoBrowserVideoWakeTools"))
        assertTrue(commonScript.contains("wakeVideoControls: videoCallbacks.wakeVideoControls"))
        assertTrue(callbacksScript.contains("return videoWakeTools.wake(video, {"))
        assertTrue(callbacksScript.contains("enableVideoControls: videoControlCoordinator.enableControls"))
        assertTrue(scriptLoader.contains("VIDEO_WAKE_TOOLS_SCRIPT_ASSET"))
        assertTrue(
            commonAssetList.indexOf("VIDEO_WAKE_TOOLS_SCRIPT_ASSET") <
                commonAssetList.indexOf("VIDEO_ENHANCER_CALLBACKS_SCRIPT_ASSET")
        )
        assertTrue(
            commonAssetList.indexOf("VIDEO_ENHANCER_CALLBACKS_SCRIPT_ASSET") <
                commonAssetList.indexOf("COMMON_SCRIPT_ASSET")
        )
        assertFalse(commonScript.contains("function dispatchControlWakeEvent(target, type, clientX, clientY)"))
        assertFalse(commonScript.contains("function dispatchPointerWakeEvent(target, clientX, clientY)"))
        assertFalse(commonScript.contains("new MouseEvent(type, {"))
        assertFalse(commonScript.contains("new PointerEvent('pointermove', {"))
        assertFalse(wakeScript.contains("getBoundingClientRect"))
    }

}
