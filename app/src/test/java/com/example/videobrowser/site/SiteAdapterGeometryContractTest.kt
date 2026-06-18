package com.example.videobrowser.site

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SiteAdapterGeometryContractTest {
    @Test
    fun `site adapter geometry helpers are shared`() {
        val commonScript = projectFile("src/main/assets/scripts/common.js").readText()
        val helperScript = projectFile("src/main/assets/scripts/site_adapter_helpers.js").readText()
        val bilibiliScript = projectFile("src/main/assets/scripts/bilibili.js").readText()

        assertTrue(commonScript.contains("window.VideoBrowserGeometry = geometry"))
        assertTrue(commonScript.contains("geometry.safeRect = geometry.safeRect || function (element)"))
        assertTrue(commonScript.contains("geometry.expandedRect = geometry.expandedRect || function (rect, amount)"))
        assertTrue(commonScript.contains("geometry.rectsOverlap = geometry.rectsOverlap || function (first, second)"))
        assertTrue(commonScript.contains("geometry.centerDistance = geometry.centerDistance || function (first, second)"))
        assertTrue(helperScript.contains("var geometry = window.VideoBrowserGeometry || {}"))
        assertTrue(helperScript.contains("tools.safeRect = geometry.safeRect"))
        assertTrue(helperScript.contains("tools.expandedRect = geometry.expandedRect"))
        assertTrue(helperScript.contains("safeRect: tools.safeRect"))
        assertTrue(bilibiliScript.contains("var safeRect = adapterTools.safeRect"))
        assertTrue(bilibiliScript.contains("var centerDistance = adapterTools.centerDistance"))
        assertFalse(helperScript.contains("tools.safeRect = function (element)"))
        assertFalse(helperScript.contains("tools.expandedRect = function (rect, amount)"))
        assertFalse(bilibiliScript.contains("function safeRect(element)"))
        assertFalse(bilibiliScript.contains("function expandedRect(rect, amount)"))
        assertFalse(bilibiliScript.contains("function rectsOverlap(first, second)"))
        assertFalse(bilibiliScript.contains("function centerDistance(first, second)"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
