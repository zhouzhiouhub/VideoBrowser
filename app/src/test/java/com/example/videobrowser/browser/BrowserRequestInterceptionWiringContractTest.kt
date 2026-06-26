package com.example.videobrowser.browser

import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserRequestInterceptionWiringContractTest {
    @Test
    fun requestInterceptionProviderIsCreatedAfterCoreFeaturesAreAvailable() {
        val featureAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserActivityFeatureAssemblyController.kt"
        ).readText()
        val scaffoldAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserActivityScaffoldAssemblyController.kt"
        ).readText()
        val coreAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserCoreFeatureAssemblyController.kt"
        ).readText()
        val runtimeAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserRuntimeFeatureAssemblyController.kt"
        ).readText()
        val startupAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserStartupFeatureAssemblyController.kt"
        ).readText()
        val provider = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserRequestInterceptionProvider.kt"
        ).readText()
        val interceptionAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserRequestInterceptionAssemblyController.kt"
        ).readText()
        val webClientController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserWebClientController.kt"
        ).readText()
        val searchPolicy = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/BuiltInSearchResultPagePolicy.kt"
        ).readText()
        val requestFastPathPolicy = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/SearchResultRequestInterceptionPolicy.kt"
        ).readText()

        val coreCreation = featureAssembly.indexOf(
            "val browserCoreFeatures = BrowserCoreFeatureAssemblyController("
        )
        val providerCreation = featureAssembly.indexOf(
            "val requestInterceptionProvider = BrowserRequestInterceptionProvider("
        )

        assertTrue("Core features should be created in the feature assembly", coreCreation >= 0)
        assertTrue(
            "Request interception provider should be created after core features",
            providerCreation > coreCreation
        )
        assertTrue(
            "Core feature creation should complete before the provider is created",
            featureAssembly.substring(coreCreation, providerCreation).contains(").create()")
        )
        assertTrue(
            featureAssembly.contains(
                "browserCoreFeatures.browserShell.browserFeatureStateController"
            )
        )
        assertTrue(featureAssembly.contains("browserCoreFeatures.browserPersistence.settingsManager"))
        assertTrue(featureAssembly.contains("browserCoreFeatures.browserNavigation.ruleEngine"))
        assertTrue(featureAssembly.contains("isSearchResultResourceUrl ="))
        assertTrue(
            featureAssembly.contains(
                "browserCoreFeatures.browserSearch.builtInSearchResultPagePolicy::isSearchResultResourceUrl"
            )
        )

        assertFalse(featureAssembly.contains("activityScaffold.requestInterceptionProvider"))
        assertFalse(scaffoldAssembly.contains("BrowserRequestInterceptionProvider("))
        assertFalse(coreAssembly.contains("requestInterceptionProvider"))

        assertTrue(provider.contains("BrowserRequestInterceptionAssemblyController("))
        assertTrue(provider.contains("isSearchResultResourceUrl = isSearchResultResourceUrl"))
        assertTrue(provider.contains("val searchResultRequestInterceptionPolicy: SearchResultRequestInterceptionPolicy"))
        assertTrue(interceptionAssembly.contains("SearchResultRequestInterceptionPolicy("))
        assertTrue(interceptionAssembly.contains("isSearchResultResourceUrl = isSearchResultResourceUrl"))
        assertTrue(searchPolicy.contains("fun isSearchResultResourceUrl(pageUrl: String?, resourceUrl: String?)"))
        assertTrue(requestFastPathPolicy.contains("fun shouldBypassHeavyInterception(request: BrowserRequest): Boolean"))
        assertTrue(requestFastPathPolicy.contains("if (request.isForMainFrame)"))
        assertTrue(requestFastPathPolicy.contains("return isSearchResultResourceUrl(request.pageUrl, request.url.toString())"))
        assertTrue(
            runtimeAssembly.contains(
                "adBlockRequestInterceptor = requestInterceptionProvider.adBlockRequestInterceptor"
            )
        )
        assertTrue(
            runtimeAssembly.contains(
                "requestInterceptionProvider.smartNoImageRequestInterceptor"
            )
        )
        assertTrue(
            runtimeAssembly.contains(
                "requestInterceptionProvider.searchResultRequestInterceptionPolicy"
            )
        )
        assertTrue(
            webClientController.contains(
                "searchResultRequestInterceptionPolicy.shouldBypassHeavyInterception(request)"
            )
        )
        assertTrue(startupAssembly.contains("adBlockLogger = requestInterceptionProvider.adBlockLogger"))
    }
}
