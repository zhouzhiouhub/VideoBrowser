package com.example.videobrowser.site

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchResultCleanupContractTest {
    @Test
    fun `search result cleanup is owned by shared search cleanup module`() {
        val searchScript = projectFile("src/main/assets/scripts/search_result_cleanup.js").readText()
        val coordinatorScript = projectFile("src/main/assets/scripts/page_cleanup_coordinator.js").readText()
        val commonScript = projectFile("src/main/assets/scripts/common.js").readText()
        val scriptLoader = projectFile("src/main/java/com/example/videobrowser/inject/ScriptLoader.kt").readText()
        val commonAssetList = scriptLoader.substringAfter("val COMMON_SCRIPT_ASSETS = listOf(")

        assertTrue(searchScript.contains("window.VideoBrowserSearchResultCleanup = cleanup"))
        assertTrue(searchScript.contains("cleanup.isResultPage = cleanup.isResultPage || function ()"))
        assertTrue(searchScript.contains("cleanup.removeAds = cleanup.removeAds || function (options)"))
        assertTrue(searchScript.contains("function findSearchAdDisclosureMarkers()"))
        assertTrue(searchScript.contains("function findSearchResultRoot(marker)"))
        assertTrue(searchScript.contains("const geometry = window.VideoBrowserGeometry || {}"))
        assertTrue(searchScript.contains("const rect = geometry.safeRect(current);"))
        assertTrue(searchScript.contains("const rect = geometry.safeRect(element);"))
        assertTrue(searchScript.contains("selectorTools.normalizeText("))
        assertTrue(searchScript.contains("domTools.queryAll('span,i,em,b,a,button,[role=\"button\"],[aria-label],[title],[class*=\"ad\"],[class*=\"adv\"]')"))
        assertTrue(searchScript.contains("domTools.queryAllWithin(current, 'a,img,h1,h2,h3,[role=\"heading\"]')"))
        assertTrue(searchScript.contains("const descriptor = domTools.elementDescriptor(element);"))
        assertTrue(searchScript.contains("const compactText = selectorTools.compactText(text);"))
        assertTrue(searchScript.contains("const compactDescriptor = selectorTools.compactText(descriptor);"))
        assertTrue(searchScript.contains("domActions.hideElement("))
        assertTrue(coordinatorScript.contains("const searchResultCleanup = window.VideoBrowserSearchResultCleanup || {}"))
        assertTrue(coordinatorScript.contains("return isBilibiliHost(options) || searchResultCleanup.isResultPage();"))
        assertTrue(coordinatorScript.contains("searchResultCleanup.removeAds({"))
        assertTrue(scriptLoader.contains("SEARCH_RESULT_CLEANUP_SCRIPT_ASSET"))
        assertTrue(
            commonAssetList.indexOf("SEARCH_RESULT_CLEANUP_SCRIPT_ASSET") <
                commonAssetList.indexOf("COMMON_SCRIPT_ASSET")
        )
        assertFalse(commonScript.contains("function findSearchAdDisclosureMarkers()"))
        assertFalse(commonScript.contains("function findSearchResultRoot(marker)"))
        assertFalse(commonScript.contains("function isSearchAdDisclosure(text, descriptor)"))
        assertFalse(commonScript.contains("const searchResultCleanup = window.VideoBrowserSearchResultCleanup"))
        assertFalse(commonScript.contains("searchResultCleanup.removeAds({"))
        assertFalse(searchScript.contains("document.querySelectorAll(selector)"))
        assertFalse(searchScript.contains("replace(/\\s+/g, ' ').trim()"))
        assertFalse(searchScript.contains("replace(/\\s+/g, '')"))
        assertFalse(searchScript.contains("getBoundingClientRect()"))
        assertFalse(searchScript.contains("querySelectorAll("))
        assertFalse(searchScript.contains("function normalizeText(value)"))
        assertFalse(searchScript.contains("function hideElement(element, reason)"))
        assertFalse(searchScript.contains("function queryAll(selector)"))
    }

}
