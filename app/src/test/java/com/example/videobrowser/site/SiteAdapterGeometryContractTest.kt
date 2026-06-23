package com.example.videobrowser.site

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SiteAdapterGeometryContractTest {
    @Test
    fun `site adapter geometry helpers are shared`() {
        val geometryScript = projectFile("src/main/assets/scripts/geometry.js").readText()
        val commonScript = projectFile("src/main/assets/scripts/common.js").readText()
        val customControlDetectorScript = projectFile("src/main/assets/scripts/video_custom_control_detector.js").readText()
        val helperScript = projectFile("src/main/assets/scripts/site_adapter_helpers.js").readText()
        val bilibiliScript = projectFile("src/main/assets/scripts/bilibili.js").readText()
        val bilibiliOverlayScript = projectFile("src/main/assets/scripts/bilibili_overlay_cleanup.js").readText()

        assertTrue(geometryScript.contains("window.VideoBrowserGeometry = geometry"))
        assertTrue(geometryScript.contains("geometry.safeRect = geometry.safeRect || function (element)"))
        assertTrue(geometryScript.contains("geometry.expandedRect = geometry.expandedRect || function (rect, amount)"))
        assertTrue(geometryScript.contains("geometry.rectsOverlap = geometry.rectsOverlap || function (first, second)"))
        assertTrue(geometryScript.contains("geometry.centerDistance = geometry.centerDistance || function (first, second)"))
        assertTrue(customControlDetectorScript.contains("const geometry = window.VideoBrowserGeometry || {}"))
        assertTrue(customControlDetectorScript.contains("geometry.rectsOverlap(rect, geometry.expandedRect(videoRect, 12))"))
        assertFalse(commonScript.contains("geometry.safeRect = geometry.safeRect || function (element)"))
        assertFalse(commonScript.contains("geometry.expandedRect = geometry.expandedRect || function (rect, amount)"))
        assertFalse(commonScript.contains("geometry.rectsOverlap = geometry.rectsOverlap || function (first, second)"))
        assertFalse(commonScript.contains("geometry.centerDistance = geometry.centerDistance || function (first, second)"))
        assertTrue(helperScript.contains("var geometry = window.VideoBrowserGeometry || {}"))
        assertTrue(helperScript.contains("tools.safeRect = geometry.safeRect"))
        assertTrue(helperScript.contains("tools.expandedRect = geometry.expandedRect"))
        assertTrue(helperScript.contains("tools.queryWithin = function (root, selector)"))
        assertTrue(helperScript.contains("tools.visibleElement = function (element)"))
        assertTrue(helperScript.contains("queryWithin: tools.queryWithin"))
        assertTrue(helperScript.contains("safeRect: tools.safeRect"))
        assertTrue(helperScript.contains("visibleElement: tools.visibleElement"))
        assertTrue(bilibiliOverlayScript.contains("safeRect: tools.safeRect"))
        assertTrue(bilibiliOverlayScript.contains("centerDistance: tools.centerDistance"))
        assertFalse(helperScript.contains("tools.safeRect = function (element)"))
        assertFalse(helperScript.contains("tools.expandedRect = function (rect, amount)"))
        assertFalse(bilibiliScript.contains("var safeRect = adapterTools.safeRect"))
        assertFalse(bilibiliScript.contains("var centerDistance = adapterTools.centerDistance"))
        assertFalse(bilibiliScript.contains("function safeRect(element)"))
        assertFalse(bilibiliScript.contains("function expandedRect(rect, amount)"))
        assertFalse(bilibiliScript.contains("function rectsOverlap(first, second)"))
        assertFalse(bilibiliScript.contains("function centerDistance(first, second)"))
    }

}
