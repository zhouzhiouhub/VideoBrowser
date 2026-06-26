package com.example.videobrowser.site

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchResultCleanupContractTest {
    @Test
    fun `search result cleanup is owned by shared search cleanup module`() {
        val searchScript = projectFile("src/main/assets/scripts/search_result_cleanup.js").readText()
        val embeddedSearchShellScript = projectFile(
            "src/main/assets/scripts/embedded_search_shell_cleanup.js"
        ).readText()
        val coordinatorScript = projectFile("src/main/assets/scripts/page_cleanup_coordinator.js").readText()
        val commonScript = projectFile("src/main/assets/scripts/common.js").readText()
        val runtimeScript = projectFile("src/main/assets/scripts/enhancer_runtime.js").readText()
        val enhancerApiScript = projectFile("src/main/assets/scripts/enhancer_api.js").readText()
        val scriptLoader = projectFile("src/main/java/com/example/videobrowser/inject/ScriptLoader.kt").readText()
        val commonAssetList = scriptLoader.substringAfter("val COMMON_SCRIPT_ASSETS = listOf(")

        assertTrue(searchScript.contains("window.VideoBrowserSearchResultCleanup = cleanup"))
        assertTrue(searchScript.contains("cleanup.isResultPage = cleanup.isResultPage || function ()"))
        assertTrue(searchScript.contains("cleanup.removeAds = cleanup.removeAds || function (options)"))
        assertTrue(searchScript.contains("function findSearchAdDisclosureMarkers()"))
        assertTrue(searchScript.contains("function findSearchResultRoot(marker)"))
        assertTrue(
            embeddedSearchShellScript.contains(
                "window.VideoBrowserEmbeddedSearchShellCleanup = cleanup"
            )
        )
        assertTrue(embeddedSearchShellScript.contains("cleanup.apply = cleanup.apply || function (options)"))
        assertTrue(embeddedSearchShellScript.contains("state.config.builtInSearchResultPage"))
        assertTrue(embeddedSearchShellScript.contains("moveConfiguredSearchChromeToContentEnd(state.config.searchPageHideCss);"))
        assertTrue(embeddedSearchShellScript.contains("function moveConfiguredSearchChromeToContentEnd(selectors)"))
        assertTrue(embeddedSearchShellScript.contains("function moveEmbeddedSearchChromeToContentEnd()"))
        assertTrue(embeddedSearchShellScript.contains("function moveSearchChromeRootToContentEnd(root, reason)"))
        assertTrue(embeddedSearchShellScript.contains("videobrowser-search-shell-content-end"))
        assertTrue(embeddedSearchShellScript.contains("data-videobrowser-search-shell-bottom"))
        assertTrue(embeddedSearchShellScript.contains("function rootHasSearchControl(root)"))
        assertTrue(embeddedSearchShellScript.contains("function collectEmbeddedSearchChromeCandidates()"))
        assertTrue(embeddedSearchShellScript.contains("function findEmbeddedSearchChromeRoot(element)"))
        assertTrue(embeddedSearchShellScript.contains("function isEmbeddedSearchChromeCandidate(element)"))
        assertTrue(searchScript.contains("function pathMatchesSearchPath(path, searchPath)"))
        assertTrue(searchScript.contains("pathMatchesSearchPath(path, '/s')"))
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
        assertTrue(embeddedSearchShellScript.contains("selectorTools.safeSelectorList(selectors).forEach"))
        assertTrue(embeddedSearchShellScript.contains("configured-embedded-search-shell"))
        assertTrue(coordinatorScript.contains("const searchResultCleanup = window.VideoBrowserSearchResultCleanup || {}"))
        assertTrue(coordinatorScript.contains("return isBilibiliHost(options) || searchResultCleanup.isResultPage();"))
        assertTrue(coordinatorScript.contains("searchResultCleanup.removeAds({"))
        assertTrue(coordinatorScript.contains("coordinator.applyEmbeddedSearchShell = coordinator.applyEmbeddedSearchShell || function (state, options)"))
        assertTrue(coordinatorScript.contains("const embeddedSearchShellCleanup = window.VideoBrowserEmbeddedSearchShellCleanup || {}"))
        assertTrue(coordinatorScript.contains("embeddedSearchShellCleanup.apply({"))
        assertTrue(coordinatorScript.contains("coordinator.runSearchResultFastCleanup = coordinator.runSearchResultFastCleanup || function (state, options)"))
        assertTrue(coordinatorScript.contains("includeGenericSelectors: false"))
        assertTrue(coordinatorScript.contains("includeRuleSelectors: false"))
        assertTrue(runtimeScript.contains("if (state.config.builtInSearchResultPage)"))
        assertTrue(runtimeScript.contains("pageCleanupCoordinator.applyEmbeddedSearchShell(state, {"))
        assertTrue(runtimeScript.contains("pageCleanupCoordinator.runSearchResultFastCleanup(state, {"))
        assertTrue(runtimeScript.contains("} else if (state.config.cleanupEnabled) {"))
        assertTrue(runtimeScript.contains("function shouldDisableObserver()"))
        assertTrue(
            runtimeScript.contains(
                "Boolean(state.config && state.config.builtInSearchResultPage) || isBilibiliHost()"
            )
        )
        assertTrue(enhancerApiScript.contains("if (!state.config.builtInSearchResultPage)"))
        assertTrue(enhancerApiScript.contains("callbacks.cleanupLegacyVideoOverlays();"))
        assertTrue(enhancerApiScript.contains("callbacks.installHooks();"))
        assertTrue(scriptLoader.contains("SEARCH_RESULT_CLEANUP_SCRIPT_ASSET"))
        assertTrue(scriptLoader.contains("EMBEDDED_SEARCH_SHELL_CLEANUP_SCRIPT_ASSET"))
        assertTrue(
            commonAssetList.indexOf("SEARCH_RESULT_CLEANUP_SCRIPT_ASSET") <
                commonAssetList.indexOf("EMBEDDED_SEARCH_SHELL_CLEANUP_SCRIPT_ASSET")
        )
        assertTrue(
            commonAssetList.indexOf("EMBEDDED_SEARCH_SHELL_CLEANUP_SCRIPT_ASSET") <
                commonAssetList.indexOf("PAGE_CLEANUP_COORDINATOR_SCRIPT_ASSET")
        )
        assertFalse(commonScript.contains("function findSearchAdDisclosureMarkers()"))
        assertFalse(commonScript.contains("function findSearchResultRoot(marker)"))
        assertFalse(commonScript.contains("function isSearchAdDisclosure(text, descriptor)"))
        assertFalse(commonScript.contains("const searchResultCleanup = window.VideoBrowserSearchResultCleanup"))
        assertFalse(commonScript.contains("searchResultCleanup.removeAds({"))
        assertFalse(searchScript.contains("function moveEmbeddedSearchChromeToContentEnd()"))
        assertFalse(searchScript.contains("state.config.builtInSearchResultPage"))
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
