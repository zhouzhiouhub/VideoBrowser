package com.example.videobrowser.video

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoCustomControlDetectorContractTest {
    @Test
    fun `custom player control detection is owned by shared detector module`() {
        val detectorScript = projectFile("src/main/assets/scripts/video_custom_control_detector.js").readText()
        val controlCoordinatorScript = projectFile("src/main/assets/scripts/video_control_coordinator.js").readText()
        val commonScript = projectFile("src/main/assets/scripts/common.js").readText()
        val scriptLoader = projectFile("src/main/java/com/example/videobrowser/inject/ScriptLoader.kt").readText()
        val commonAssetList = scriptLoader.substringAfter("val COMMON_SCRIPT_ASSETS = listOf(")

        assertTrue(detectorScript.contains("window.VideoBrowserVideoCustomControlDetector = detector"))
        assertTrue(detectorScript.contains("detector.hasControls = detector.hasControls || function (video)"))
        assertTrue(detectorScript.contains("detector.rootFor = detector.rootFor || function (video)"))
        assertTrue(detectorScript.contains("'.xgplayer-controls'"))
        assertTrue(detectorScript.contains("'.dplayer-controller'"))
        assertTrue(detectorScript.contains("'.art-controls'"))
        assertTrue(detectorScript.contains("'.vjs-control-bar'"))
        assertTrue(detectorScript.contains("'[class*=\"player-control\"]'"))
        assertTrue(detectorScript.contains("const rect = geometry.safeRect(element);"))
        assertTrue(detectorScript.contains("const videoRect = geometry.safeRect(video);"))
        assertTrue(detectorScript.contains("geometry.rectsOverlap(rect, geometry.expandedRect(videoRect, 12))"))
        assertTrue(detectorScript.contains("selectorTools.queryAllWithin(root, selector).some(function (element)"))
        assertTrue(detectorScript.contains("domTools.elementDescriptor(element).toLowerCase()"))
        assertTrue(detectorScript.contains("selectorTools.normalizeText("))
        assertTrue(controlCoordinatorScript.contains("const customControlDetector = window.VideoBrowserVideoCustomControlDetector || {}"))
        assertTrue(controlCoordinatorScript.contains("customControlDetector.hasControls(video)"))
        assertFalse(commonScript.contains("const customControlDetector = window.VideoBrowserVideoCustomControlDetector"))
        assertFalse(commonScript.contains("customControlDetector.hasControls(video)"))
        assertTrue(scriptLoader.contains("VIDEO_CUSTOM_CONTROL_DETECTOR_SCRIPT_ASSET"))
        assertTrue(scriptLoader.contains("VIDEO_CONTROL_COORDINATOR_SCRIPT_ASSET"))
        assertTrue(
            commonAssetList.indexOf("VIDEO_CUSTOM_CONTROL_DETECTOR_SCRIPT_ASSET") <
                commonAssetList.indexOf("VIDEO_CONTROL_COORDINATOR_SCRIPT_ASSET")
        )
        assertTrue(
            commonAssetList.indexOf("VIDEO_CONTROL_COORDINATOR_SCRIPT_ASSET") <
                commonAssetList.indexOf("COMMON_SCRIPT_ASSET")
        )
        assertFalse(commonScript.contains("const customPlayerControlSelectors = ["))
        assertFalse(commonScript.contains("function hasLikelyCustomPlayerControls(video)"))
        assertFalse(commonScript.contains("function customPlayerRootFor(video)"))
        assertFalse(commonScript.contains("function isLikelyCustomControlElement(element, video)"))
        assertFalse(commonScript.contains("function isLikelyMediaControlElement(element, video)"))
        assertFalse(detectorScript.contains("function normalizeText(value)"))
        assertFalse(detectorScript.contains("function elementDescriptor(element)"))
        assertFalse(detectorScript.contains("function queryAllWithin(root, selector)"))
        assertFalse(detectorScript.contains("getBoundingClientRect"))
    }

}
