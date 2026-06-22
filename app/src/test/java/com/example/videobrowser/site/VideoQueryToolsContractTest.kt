package com.example.videobrowser.site

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoQueryToolsContractTest {
    @Test
    fun `video element lookup is owned by shared video query module`() {
        val videoQueryScript = projectFile("src/main/assets/scripts/video_query_tools.js").readText()
        val runtimeScript = projectFile("src/main/assets/scripts/enhancer_runtime.js").readText()
        val commonScript = projectFile("src/main/assets/scripts/common.js").readText()
        val callbacksScript = projectFile("src/main/assets/scripts/video_enhancer_callbacks.js").readText()
        val fullscreenScript = projectFile("src/main/assets/scripts/video_fullscreen_tools.js").readText()

        assertTrue(videoQueryScript.contains("window.VideoBrowserVideoQueryTools = tools"))
        assertTrue(videoQueryScript.contains("const domTools = window.VideoBrowserDomTools || {}"))
        assertTrue(videoQueryScript.contains("tools.all = tools.all || function ()"))
        assertTrue(videoQueryScript.contains("domTools.queryAll('video')"))
        assertTrue(videoQueryScript.contains("tools.forEach = tools.forEach || function (callback)"))
        assertTrue(videoQueryScript.contains("tools.some = tools.some || function (predicate)"))
        assertTrue(videoQueryScript.contains("tools.isActive = tools.isActive || function (video)"))
        assertTrue(videoQueryScript.contains("tools.hasActive = tools.hasActive || function ()"))
        assertTrue(commonScript.contains("const videoQueryTools = window.VideoBrowserVideoQueryTools"))
        assertTrue(callbacksScript.contains("videoQueryTools.forEach(function (video)"))
        assertTrue(fullscreenScript.contains("videoQueryTools.all()"))
        assertTrue(runtimeScript.contains("return videoQueryTools.hasActive();"))
        assertFalse(commonScript.contains("return videoQueryTools.hasActive();"))
        assertFalse(commonScript.contains("return videoQueryTools.some(function (video)"))
        assertFalse(commonScript.contains("document.querySelectorAll('video')"))
        assertFalse(videoQueryScript.contains("document.querySelectorAll('video')"))
    }

}
