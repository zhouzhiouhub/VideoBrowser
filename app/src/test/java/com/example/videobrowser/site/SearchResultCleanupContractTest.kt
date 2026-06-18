package com.example.videobrowser.site

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchResultCleanupContractTest {
    @Test
    fun `search result cleanup is owned by shared search cleanup module`() {
        val searchScript = projectFile("src/main/assets/scripts/search_result_cleanup.js").readText()
        val commonScript = projectFile("src/main/assets/scripts/common.js").readText()
        val scriptLoader = projectFile("src/main/java/com/example/videobrowser/inject/ScriptLoader.kt").readText()
        val commonAssetList = scriptLoader.substringAfter("val COMMON_SCRIPT_ASSETS = listOf(")

        assertTrue(searchScript.contains("window.VideoBrowserSearchResultCleanup = cleanup"))
        assertTrue(searchScript.contains("cleanup.isResultPage = cleanup.isResultPage || function ()"))
        assertTrue(searchScript.contains("cleanup.removeAds = cleanup.removeAds || function (options)"))
        assertTrue(searchScript.contains("function findSearchAdDisclosureMarkers()"))
        assertTrue(searchScript.contains("function findSearchResultRoot(marker)"))
        assertTrue(searchScript.contains("return selectorTools.normalizeText(value);"))
        assertTrue(searchScript.contains("return domTools.queryAll(selector);"))
        assertTrue(searchScript.contains("domActions.hideElement(element, {"))
        assertTrue(commonScript.contains("const searchResultCleanup = window.VideoBrowserSearchResultCleanup"))
        assertTrue(commonScript.contains("return isBilibiliHost() || searchResultCleanup.isResultPage();"))
        assertTrue(commonScript.contains("searchResultCleanup.removeAds({"))
        assertTrue(scriptLoader.contains("SEARCH_RESULT_CLEANUP_SCRIPT_ASSET"))
        assertTrue(
            commonAssetList.indexOf("SEARCH_RESULT_CLEANUP_SCRIPT_ASSET") <
                commonAssetList.indexOf("COMMON_SCRIPT_ASSET")
        )
        assertFalse(commonScript.contains("function findSearchAdDisclosureMarkers()"))
        assertFalse(commonScript.contains("function findSearchResultRoot(marker)"))
        assertFalse(commonScript.contains("function isSearchAdDisclosure(text, descriptor)"))
        assertFalse(searchScript.contains("document.querySelectorAll(selector)"))
        assertFalse(searchScript.contains("replace(/\\s+/g, ' ').trim()"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
