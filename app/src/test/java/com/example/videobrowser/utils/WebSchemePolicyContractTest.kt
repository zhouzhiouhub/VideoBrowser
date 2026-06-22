package com.example.videobrowser.utils

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebSchemePolicyContractTest {
    @Test
    fun `request and navigation policies share web scheme checks`() {
        val sources = listOf(
            projectFile("src/main/java/com/example/videobrowser/adblock/AdBlockRequestPolicy.kt"),
            projectFile("src/main/java/com/example/videobrowser/adblock/RuleDecisionResolver.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/SmartNoImageRequestPolicy.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/BrowserNavigationController.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/ExternalProtocolPolicy.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/HttpNavigationSafetyPolicy.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/BrowserUrlStateController.kt"),
            projectFile("src/main/java/com/example/videobrowser/settings/SettingsHttpUrlValidator.kt"),
            projectFile("src/main/java/com/example/videobrowser/video/MediaRoutingController.kt"),
            projectFile("src/main/java/com/example/videobrowser/rules/RuleSubscriptionFetcher.kt"),
            projectFile("src/main/java/com/example/videobrowser/rules/RuleNavigationUrlCleaner.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/HistoryRecordPolicy.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/search/HomeQuickLinkBuilder.kt"),
            projectFile("src/main/java/com/example/videobrowser/utils/MediaUrlUtils.kt")
        ).map { file -> file.readText() }

        sources.forEach { source ->
            assertTrue(source.contains("WebSchemePolicy."))
            assertFalse(source.contains("private fun isHttpScheme"))
            assertFalse(source.contains("scheme.equals(\"http\", ignoreCase = true)"))
            assertFalse(source.contains("scheme != \"http\" && scheme != \"https\""))
        }
    }

}
