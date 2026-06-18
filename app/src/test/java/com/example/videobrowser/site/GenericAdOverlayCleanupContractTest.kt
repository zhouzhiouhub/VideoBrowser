package com.example.videobrowser.site

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GenericAdOverlayCleanupContractTest {
    @Test
    fun `generic ad overlay cleanup is owned by shared overlay module`() {
        val overlayScript = projectFile("src/main/assets/scripts/generic_ad_overlay_cleanup.js").readText()
        val commonScript = projectFile("src/main/assets/scripts/common.js").readText()
        val scriptLoader = projectFile("src/main/java/com/example/videobrowser/inject/ScriptLoader.kt").readText()
        val commonAssetList = scriptLoader.substringAfter("val COMMON_SCRIPT_ASSETS = listOf(")

        assertTrue(overlayScript.contains("window.VideoBrowserGenericAdOverlayCleanup = cleanup"))
        assertTrue(overlayScript.contains("cleanup.run = cleanup.run || function (state, options)"))
        assertTrue(overlayScript.contains("function collectGenericAdOverlayCandidates()"))
        assertTrue(overlayScript.contains("function findGenericAdOverlayRoot(element)"))
        assertTrue(overlayScript.contains("function isLikelyGenericAdOverlay(element)"))
        assertTrue(overlayScript.contains("function hasCloseLikeDescendant(element)"))
        assertTrue(overlayScript.contains("function clearOverlayScrollLocks()"))
        assertTrue(overlayScript.contains("generatedAdCleanup.run(state, {"))
        assertTrue(overlayScript.contains("return domTools.queryAll(selector);"))
        assertTrue(commonScript.contains("const genericAdOverlayCleanup = window.VideoBrowserGenericAdOverlayCleanup"))
        assertTrue(commonScript.contains("genericAdOverlayCleanup.run(state);"))
        assertTrue(scriptLoader.contains("GENERIC_AD_OVERLAY_CLEANUP_SCRIPT_ASSET"))
        assertTrue(
            commonAssetList.indexOf("GENERATED_AD_CLEANUP_SCRIPT_ASSET") <
                commonAssetList.indexOf("GENERIC_AD_OVERLAY_CLEANUP_SCRIPT_ASSET")
        )
        assertTrue(
            commonAssetList.indexOf("GENERIC_AD_OVERLAY_CLEANUP_SCRIPT_ASSET") <
                commonAssetList.indexOf("COMMON_SCRIPT_ASSET")
        )
        assertFalse(commonScript.contains("function collectGenericAdOverlayCandidates()"))
        assertFalse(commonScript.contains("function findGenericAdOverlayRoot(element)"))
        assertFalse(commonScript.contains("function isLikelyGenericAdOverlay(element)"))
        assertFalse(commonScript.contains("function mediaSourceLooksLikeAd(element)"))
        assertFalse(commonScript.contains("function isScrollLockClass(className)"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
