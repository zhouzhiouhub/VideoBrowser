package com.example.videobrowser.site

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneratedAdCleanupContractTest {
    @Test
    fun `generated ad cleanup is owned by shared generated cleanup module`() {
        val generatedScript = projectFile("src/main/assets/scripts/generated_ad_cleanup.js").readText()
        val overlayScript = projectFile("src/main/assets/scripts/generic_ad_overlay_cleanup.js").readText()
        val coordinatorScript = projectFile("src/main/assets/scripts/page_cleanup_coordinator.js").readText()
        val runtimeScript = projectFile("src/main/assets/scripts/enhancer_runtime.js").readText()
        val commonScript = projectFile("src/main/assets/scripts/common.js").readText()
        val geometryScript = projectFile("src/main/assets/scripts/geometry.js").readText()
        val domToolsScript = projectFile("src/main/assets/scripts/dom_tools.js").readText()
        val scriptLoader = projectFile("src/main/java/com/example/videobrowser/inject/ScriptLoader.kt").readText()
        val commonAssetList = scriptLoader.substringAfter("val COMMON_SCRIPT_ASSETS = listOf(")

        assertTrue(generatedScript.contains("window.VideoBrowserGeneratedAdCleanup = cleanup"))
        assertTrue(generatedScript.contains("cleanup.run = cleanup.run || function (state, options)"))
        assertTrue(generatedScript.contains("cleanup.removeScaffolds = cleanup.removeScaffolds || function ()"))
        assertTrue(generatedScript.contains("function isGeneratedImageSlice(element, style, rect, viewportWidth, viewportHeight)"))
        assertTrue(generatedScript.contains("function isGeneratedClickGridCell(element, style, rect, viewportWidth, viewportHeight)"))
        assertTrue(generatedScript.contains("function isGeneratedAdAdjunctControl(element, style, rect)"))
        assertTrue(generatedScript.contains("geometry.visibleRectInViewport(rect, viewportWidth, viewportHeight)"))
        assertTrue(generatedScript.contains("domTools.parseZIndex(style.zIndex)"))
        assertTrue(generatedScript.contains("domTools.elementDescriptor(element)"))
        assertTrue(generatedScript.contains("domActions.hideElement(element, {"))
        assertTrue(coordinatorScript.contains("const generatedAdCleanup = window.VideoBrowserGeneratedAdCleanup || {}"))
        assertTrue(overlayScript.contains("generatedAdCleanup.run(state, {"))
        assertTrue(coordinatorScript.contains("coordinator.runGenerated = coordinator.runGenerated || function (state, options)"))
        assertTrue(coordinatorScript.contains("generatedAdCleanup.run(state, {"))
        assertTrue(runtimeScript.contains("pageCleanupCoordinator.runGenerated(state, {"))
        assertTrue(geometryScript.contains("geometry.visibleRectInViewport = geometry.visibleRectInViewport || function"))
        assertTrue(domToolsScript.contains("domTools.elementDescriptor = domTools.elementDescriptor || function"))
        assertTrue(domToolsScript.contains("domTools.parseZIndex = domTools.parseZIndex || function"))
        assertTrue(scriptLoader.contains("GENERATED_AD_CLEANUP_SCRIPT_ASSET"))
        assertTrue(
            commonAssetList.indexOf("GENERATED_AD_CLEANUP_SCRIPT_ASSET") <
                commonAssetList.indexOf("COMMON_SCRIPT_ASSET")
        )
        assertFalse(commonScript.contains("function runGeneratedAdScaffoldCleanup"))
        assertFalse(commonScript.contains("function removeGeneratedAdScaffolds"))
        assertFalse(commonScript.contains("function isGeneratedImageSlice"))
        assertFalse(commonScript.contains("function isGeneratedClickGridCell"))
        assertFalse(commonScript.contains("function isGeneratedAdAdjunctControl"))
        assertFalse(commonScript.contains("function elementDescriptor(element)"))
        assertFalse(commonScript.contains("function parseZIndex(value)"))
        assertFalse(commonScript.contains("const generatedAdCleanup = window.VideoBrowserGeneratedAdCleanup"))
        assertFalse(commonScript.contains("pageCleanupCoordinator.runGenerated(state, {"))
        assertFalse(commonScript.contains("generatedAdCleanup.run(state, { now: now, force: false });"))
        assertFalse(generatedScript.contains("function hideElement(element, reason)"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
