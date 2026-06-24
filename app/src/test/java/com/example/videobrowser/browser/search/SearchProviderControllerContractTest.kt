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
        val policy = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/BuiltInSearchResultPagePolicy.kt"
        ).readText()
        val searchAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/BrowserSearchAssemblyController.kt"
        ).readText()

        assertTrue(controller.contains("builtInSearchResultPagePolicy.searchQueryFromUrl(url)"))
        assertTrue(controller.contains("UrlUtils.displayUrl(url)"))
        assertTrue(policy.contains("provider.addressBarSearchUrlPrefixes.forEach"))
        assertTrue(policy.contains("UrlUtils.searchQueryFromUrl(normalizedUrl, searchUrlPrefix)"))
        assertTrue(searchAssembly.contains("val builtInSearchResultPagePolicy = BuiltInSearchResultPagePolicy(providers)"))
        assertTrue(searchAssembly.contains("builtInSearchResultPagePolicy = builtInSearchResultPagePolicy"))
        assertFalse(controller.contains("isProviderHomeUrl(url)"))
        assertFalse(controller.contains("return \"\""))
    }

    @Test
    fun sogouMobileResultUrlIsAnAddressBarSearchPrefix() {
        val providers = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/SearchProvider.kt"
        ).readText()

        assertTrue(providers.contains("val addressBarSearchUrlPrefixes: List<String>"))
        assertTrue(
            providers.contains(
                "\"https://m.sogou.com/web/searchList.jsp?s_from=pcsearch&keyword=\""
            )
        )
    }

    @Test
    fun builtInSearchResultPolicyFeedsPageFeatureInjection() {
        val searchAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/BrowserSearchAssemblyController.kt"
        ).readText()
        val startupAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserStartupFeatureAssemblyController.kt"
        ).readText()
        val pageFeatureAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserPageFeatureAssemblyController.kt"
        ).readText()
        val coordinator = projectFile(
            "src/main/java/com/example/videobrowser/inject/PageFeatureCoordinator.kt"
        ).readText()
        val injector = projectFile("src/main/java/com/example/videobrowser/inject/JsInjector.kt")
            .readText()

        assertTrue(searchAssembly.contains("val builtInSearchResultPagePolicy: BuiltInSearchResultPagePolicy"))
        assertTrue(
            startupAssembly.contains(
                "browserSearch.builtInSearchResultPagePolicy::isBuiltInSearchResultUrl"
            )
        )
        assertTrue(pageFeatureAssembly.contains("private val isBuiltInSearchResultPage: (String?) -> Boolean"))
        assertTrue(pageFeatureAssembly.contains("isBuiltInSearchResultPage = isBuiltInSearchResultPage"))
        assertTrue(coordinator.contains("builtInSearchResultPage = isBuiltInSearchResultPage(pageUrl)"))
        assertTrue(injector.contains("val builtInSearchResultPage: Boolean = false"))
    }
}
