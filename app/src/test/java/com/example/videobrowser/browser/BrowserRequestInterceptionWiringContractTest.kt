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

        assertFalse(featureAssembly.contains("activityScaffold.requestInterceptionProvider"))
        assertFalse(scaffoldAssembly.contains("BrowserRequestInterceptionProvider("))
        assertFalse(coreAssembly.contains("requestInterceptionProvider"))

        assertTrue(provider.contains("BrowserRequestInterceptionAssemblyController("))
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
        assertTrue(startupAssembly.contains("adBlockLogger = requestInterceptionProvider.adBlockLogger"))
    }
}
