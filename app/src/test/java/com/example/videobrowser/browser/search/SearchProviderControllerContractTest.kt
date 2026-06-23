package com.example.videobrowser.browser.search

import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchProviderControllerContractTest {
    @Test
    fun searchProviderControllerOwnsDefaultProviderAndAddressBadgeOnly() {
        val controller = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/SearchProviderController.kt"
        ).readText()

        assertTrue(controller.contains("lateinit var selectedProvider: SearchProvider"))
        assertTrue(controller.contains("private fun loadSavedSearchProvider(): SearchProvider"))
        assertTrue(controller.contains("settingsManager.searchEngineId()"))
        assertTrue(controller.contains("fun selectDefaultSearchProvider(providerId: String): Boolean"))
        assertTrue(controller.contains("settingsManager.setSearchEngineId(provider.id)"))
        assertTrue(controller.contains("private fun updateAddressProviderBadge()"))
        assertTrue(controller.contains("addressProviderBadge.text = selectedProvider.badge"))
        assertTrue(controller.contains("createProviderBadgeBackground("))

        assertTrue(controller.contains("providerList.removeAllViews()"))
        assertTrue(controller.contains("providerScroll.visibility = View.GONE"))
        assertFalse(controller.contains("HomeQuickLinkBuilder.fromHistory("))
        assertFalse(controller.contains("settingsManager.customShortcuts()"))
        assertFalse(controller.contains("private fun addRecentHistoryItem("))
        assertFalse(controller.contains("private fun addCustomShortcutItem("))
        assertFalse(controller.contains("SearchProviderItemFactory("))
        assertFalse(controller.contains("SearchProviderDialogController("))
        assertFalse(controller.contains("openProviderHome"))
        assertFalse(controller.contains("settingsManager.setHomeUrl(provider.homeUrl)"))
    }

    @Test
    fun browserSearchAssemblyDoesNotWireHomeEntryCallbacks() {
        val searchAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/BrowserSearchAssemblyController.kt"
        ).readText()
        val coreAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserCoreFeatureAssemblyController.kt"
        ).readText()

        assertTrue(searchAssembly.contains("val searchProviderController = SearchProviderController("))
        assertTrue(searchAssembly.contains("savedPageRepository = savedPageRepository"))
        assertTrue(searchAssembly.contains("selectedProvider = { searchProviderController.selectedProvider }"))
        assertFalse(searchAssembly.contains("openProviderHome"))
        assertFalse(searchAssembly.contains("openCustomShortcut"))
        assertFalse(searchAssembly.contains("isHomePageVisible: () -> Boolean"))
        assertFalse(coreAssembly.contains("openProviderHome = { browserNavigation.browserLaunchController.openHomePage() }"))
        assertFalse(coreAssembly.contains("openCustomShortcut = { url -> browserNavigation.browserNavigationController.loadUrl(url) }"))
    }

    @Test
    fun addressBarDisplayKeepsSearchParsingWithoutTreatingProviderHomesAsAppHome() {
        val controller = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/SearchProviderController.kt"
        ).readText()

        assertTrue(controller.contains("UrlUtils.searchQueryFromUrl(url, provider.searchUrlPrefix)"))
        assertTrue(controller.contains("UrlUtils.displayUrl(url)"))
        assertFalse(controller.contains("isProviderHomeUrl(url)"))
        assertFalse(controller.contains("return \"\""))
    }
}
