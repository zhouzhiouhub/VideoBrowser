package com.example.videobrowser.utils

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SafeUriParserContractTest {
    @Test
    fun `url display search and network policies share safe uri parsing`() {
        val sources = listOf(
            projectFile("src/main/java/com/example/videobrowser/utils/SearchUrlQueryParser.kt"),
            projectFile("src/main/java/com/example/videobrowser/utils/UrlDisplayFormatter.kt"),
            projectFile("src/main/java/com/example/videobrowser/download/DownloadSafetyPolicy.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/HttpNavigationSafetyPolicy.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/ExternalProtocolPolicy.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/BrowserUrlStateController.kt"),
            projectFile("src/main/java/com/example/videobrowser/settings/SettingsHttpUrlValidator.kt"),
            projectFile("src/main/java/com/example/videobrowser/rules/RuleSubscriptionFetcher.kt"),
            projectFile("src/main/java/com/example/videobrowser/rules/RuleNavigationUrlCleaner.kt"),
            projectFile("src/main/java/com/example/videobrowser/utils/WebPageIdentity.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/search/HomeQuickLinkBuilder.kt"),
            projectFile("src/main/java/com/example/videobrowser/functioncenter/BrowserTabDisplayText.kt"),
            projectFile("src/main/java/com/example/videobrowser/functioncenter/PlaybackHistoryDisplayText.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/RequestContext.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/ResourceTypeResolver.kt"),
            projectFile("src/main/java/com/example/videobrowser/utils/MediaUrlUtils.kt"),
            projectFile("src/main/java/com/example/videobrowser/utils/HostNameNormalizer.kt")
        ).map { file -> file.readText() }

        sources.forEach { source ->
            assertTrue(source.contains("SafeUriParser."))
            assertFalse(source.contains("private fun parseUri"))
            assertFalse(source.contains("private fun uriOf"))
            assertFalse(source.contains("runCatching { URI("))
        }
    }

}
