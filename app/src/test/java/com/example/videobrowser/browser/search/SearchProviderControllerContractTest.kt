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
        assertTrue(controller.contains("private fun loadDefaultSearchProvider(): SearchProvider"))
        assertTrue(controller.contains("SearchProviders.DEFAULT_PROVIDER_ID"))
        assertTrue(controller.contains("fun selectDefaultSearchProvider(providerId: String): Boolean"))
        assertTrue(controller.contains("fun availableProviders(): List<SearchProvider>"))
        assertTrue(controller.contains("val provider = availableProviders().firstOrNull { it.id == providerId }"))
        assertFalse(controller.contains("settingsManager.searchEngineId()"))
        assertFalse(controller.contains("settingsManager.setSearchEngineId(provider.id)"))
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
        assertTrue(searchAssembly.contains("customSearchEngines = settingsManager.customSearchEngines()"))
        assertTrue(searchAssembly.contains("removedProviderIds = settingsManager.removedSearchProviderIds()"))
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

        assertTrue(controller.contains("fun searchQueryFromUrl(url: String): String?"))
        assertTrue(controller.contains("return builtInSearchResultPagePolicy.searchQueryFromUrl(url)"))
        assertTrue(controller.contains("return searchQueryFromUrl(url) ?: UrlUtils.displayUrl(url)"))
        assertTrue(controller.contains("UrlUtils.displayUrl(url)"))
        assertTrue(policy.contains("SearchEngineUrlTools.queryFromUrl(provider.config, normalizedUrl)"))
        assertTrue(policy.contains("provider.addressBarSearchUrlPrefixes.any"))
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
        assertTrue(
            startupAssembly.contains(
                "browserSearch.builtInSearchResultPagePolicy::searchPageHideCssForUrl"
            )
        )
        assertTrue(pageFeatureAssembly.contains("private val isBuiltInSearchResultPage: (String?) -> Boolean"))
        assertTrue(pageFeatureAssembly.contains("private val searchPageHideCssForUrl: (String?) -> List<String>"))
        assertTrue(pageFeatureAssembly.contains("isBuiltInSearchResultPage = isBuiltInSearchResultPage"))
        assertTrue(pageFeatureAssembly.contains("searchPageHideCssForUrl = searchPageHideCssForUrl"))
        assertTrue(coordinator.contains("builtInSearchResultPage = isBuiltInSearchResultPage(pageUrl)"))
        assertTrue(coordinator.contains("searchPageHideCss = if (builtInSearchResultPage)"))
        assertTrue(injector.contains("val builtInSearchResultPage: Boolean = false"))
        assertTrue(injector.contains("val searchPageHideCss: List<String> = emptyList()"))
    }

    @Test
    fun pageFeatureVisibilityHidesBeforePageDrawAndRevealsAfterInjection() {
        val visibilityController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserPageFeatureVisibilityController.kt"
        ).readText()
        val visibilityPolicy = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserPageFeatureVisibilityPolicy.kt"
        ).readText()
        val searchAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/BrowserSearchAssemblyController.kt"
        ).readText()
        val coreAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserCoreFeatureAssemblyController.kt"
        ).readText()
        val webClientController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserWebClientController.kt"
        ).readText()
        val sessionAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserSessionAssemblyController.kt"
        ).readText()
        val sessionController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserSessionController.kt"
        ).readText()

        assertTrue(visibilityController.contains("fun handlePageStarted(url: String?)"))
        assertTrue(visibilityController.contains("setActiveWebViewAlpha(HIDDEN_ALPHA)"))
        assertTrue(visibilityController.contains("fun handlePageFeaturesInjected(url: String?)"))
        assertTrue(visibilityController.contains("setActiveWebViewAlpha(VISIBLE_ALPHA)"))
        assertTrue(visibilityPolicy.contains("settingsManager.isDomAdBlockEnabled()"))
        assertTrue(visibilityPolicy.contains("settingsManager.userElementHideSelectorsForSite(host).isNotEmpty()"))
        assertTrue(visibilityPolicy.contains("isBuiltInSearchResultPage(url)"))
        assertTrue(searchAssembly.contains("val pageFeatureVisibilityController: BrowserPageFeatureVisibilityController"))
        assertTrue(searchAssembly.contains("BrowserPageFeatureVisibilityPolicy("))
        assertTrue(searchAssembly.contains("setActiveWebViewAlpha = { alpha -> activeWebView().alpha = alpha }"))
        assertTrue(coreAssembly.contains("activeWebView = {"))
        assertTrue(webClientController.contains("pageFeatureVisibilityController.handlePageStarted(url)"))
        assertTrue(webClientController.contains("pageFeatureVisibilityController.handlePageFailed(error.url)"))
        assertTrue(sessionAssembly.contains("pageFeatureVisibilityController::handlePageFeaturesInjected"))
        assertTrue(sessionController.contains("injectPageFeatures {"))
        assertTrue(sessionController.contains("val completedPageUrl = currentPageUrl"))
        assertTrue(sessionController.contains("onPageFeaturesInjected(completedPageUrl)"))
    }
}
