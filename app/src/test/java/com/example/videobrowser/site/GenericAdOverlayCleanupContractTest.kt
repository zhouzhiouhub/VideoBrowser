package com.example.videobrowser.site

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GenericAdOverlayCleanupContractTest {
    @Test
    fun `generic ad overlay cleanup is owned by shared overlay module`() {
        val signalScript = projectFile("src/main/assets/scripts/generic_ad_overlay_signals.js").readText()
        val detectorScript = projectFile("src/main/assets/scripts/generic_ad_overlay_detector.js").readText()
        val overlayScript = projectFile("src/main/assets/scripts/generic_ad_overlay_cleanup.js").readText()
        val coordinatorScript = projectFile("src/main/assets/scripts/page_cleanup_coordinator.js").readText()
        val commonScript = projectFile("src/main/assets/scripts/common.js").readText()
        val scriptLoader = projectFile("src/main/java/com/example/videobrowser/inject/ScriptLoader.kt").readText()
        val commonAssetList = scriptLoader.substringAfter("val COMMON_SCRIPT_ASSETS = listOf(")

        assertTrue(signalScript.contains("window.VideoBrowserGenericAdOverlaySignals = signals"))
        assertTrue(signalScript.contains("signals.promoTextLike = signals.promoTextLike || function (value)"))
        assertTrue(signalScript.contains("signals.mediaSourceLooksLikeAd = signals.mediaSourceLooksLikeAd || function (element)"))
        assertTrue(signalScript.contains("signals.hasCloseLikeDescendant = signals.hasCloseLikeDescendant || function (element)"))
        assertTrue(signalScript.contains("signals.clearScrollLocks = signals.clearScrollLocks || function ()"))
        assertTrue(detectorScript.contains("window.VideoBrowserGenericAdOverlayDetector = detector"))
        assertTrue(detectorScript.contains("detector.collectCandidates = detector.collectCandidates || function ()"))
        assertTrue(detectorScript.contains("detector.findRoot = detector.findRoot || function (element)"))
        assertTrue(detectorScript.contains("function shouldUseGenericAdOverlayRoot(currentRoot, candidateRoot)"))
        assertTrue(detectorScript.contains("function isLikelyGenericAdOverlay(element)"))
        assertTrue(overlayScript.contains("window.VideoBrowserGenericAdOverlayCleanup = cleanup"))
        assertTrue(overlayScript.contains("const overlaySignals = window.VideoBrowserGenericAdOverlaySignals || {}"))
        assertTrue(overlayScript.contains("const overlayDetector = window.VideoBrowserGenericAdOverlayDetector || {}"))
        assertTrue(overlayScript.contains("cleanup.run = cleanup.run || function (state, options)"))
        assertTrue(overlayScript.contains("overlayDetector.collectCandidates()"))
        assertTrue(overlayScript.contains("overlayDetector.findRoot(candidate)"))
        assertTrue(overlayScript.contains("overlaySignals.clearScrollLocks();"))
        assertTrue(overlayScript.contains("generatedAdCleanup.run(state, {"))
        assertTrue(overlayScript.contains("return domTools.queryAll(selector);"))
        assertTrue(coordinatorScript.contains("const genericAdOverlayCleanup = window.VideoBrowserGenericAdOverlayCleanup || {}"))
        assertTrue(coordinatorScript.contains("genericAdOverlayCleanup.run(state);"))
        assertTrue(commonScript.contains("const pageCleanupCoordinator = window.VideoBrowserPageCleanupCoordinator"))
        assertTrue(commonScript.contains("pageCleanupCoordinator.run(state, {"))
        assertTrue(scriptLoader.contains("GENERIC_AD_OVERLAY_SIGNALS_SCRIPT_ASSET"))
        assertTrue(scriptLoader.contains("GENERIC_AD_OVERLAY_DETECTOR_SCRIPT_ASSET"))
        assertTrue(scriptLoader.contains("GENERIC_AD_OVERLAY_CLEANUP_SCRIPT_ASSET"))
        assertTrue(
            commonAssetList.indexOf("GENERATED_AD_CLEANUP_SCRIPT_ASSET") <
                commonAssetList.indexOf("GENERIC_AD_OVERLAY_SIGNALS_SCRIPT_ASSET")
        )
        assertTrue(
            commonAssetList.indexOf("GENERIC_AD_OVERLAY_SIGNALS_SCRIPT_ASSET") <
                commonAssetList.indexOf("GENERIC_AD_OVERLAY_DETECTOR_SCRIPT_ASSET")
        )
        assertTrue(
            commonAssetList.indexOf("GENERIC_AD_OVERLAY_DETECTOR_SCRIPT_ASSET") <
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
        assertFalse(commonScript.contains("const genericAdOverlayCleanup = window.VideoBrowserGenericAdOverlayCleanup"))
        assertFalse(commonScript.contains("genericAdOverlayCleanup.run(state);"))
        assertFalse(overlayScript.contains("function mediaSourceLooksLikeAd(element)"))
        assertFalse(overlayScript.contains("function hasCloseLikeDescendant(element)"))
        assertFalse(overlayScript.contains("function clearOverlayScrollLocks()"))
        assertFalse(overlayScript.contains("function isScrollLockClass(className)"))
        assertFalse(overlayScript.contains("function collectGenericAdOverlayCandidates()"))
        assertFalse(overlayScript.contains("function findGenericAdOverlayRoot(element)"))
        assertFalse(overlayScript.contains("function isLikelyGenericAdOverlay(element)"))
        assertFalse(overlayScript.contains("function shouldUseGenericAdOverlayRoot(currentRoot, candidateRoot)"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
