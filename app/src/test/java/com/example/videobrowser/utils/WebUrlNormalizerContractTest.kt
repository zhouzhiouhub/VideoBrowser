package com.example.videobrowser.utils

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebUrlNormalizerContractTest {
    @Test
    fun `http url validation callers share web url normalizer`() {
        val normalizer = projectFile(
            "src/main/java/com/example/videobrowser/utils/WebUrlNormalizer.kt"
        ).readText()
        val sources = listOf(
            projectFile("src/main/java/com/example/videobrowser/storage/SavedPageCodec.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/BrowserTabSessionRepository.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/HttpNavigationSafetyPolicy.kt"),
            projectFile("src/main/java/com/example/videobrowser/download/DownloadSafetyPolicy.kt"),
            projectFile("src/main/java/com/example/videobrowser/settings/SettingsHttpUrlValidator.kt"),
            projectFile("src/main/java/com/example/videobrowser/rules/RuleSubscriptionFetcher.kt")
        ).map { file -> file.readText() }

        assertTrue(normalizer.contains("fun normalizeHttpOrHttpsUrl(url: String?): String?"))
        assertTrue(normalizer.contains("fun normalizeHttpUrl(url: String?): String?"))
        assertTrue(normalizer.contains("fun isHttpOrHttpsUrl(url: String?): Boolean"))
        assertTrue(normalizer.contains("fun isHttpUrl(url: String?): Boolean"))

        sources.forEach { source ->
            assertTrue(
                source.contains("WebUrlNormalizer.normalizeHttpOrHttpsUrl") ||
                    source.contains("WebUrlNormalizer.isHttpOrHttpsUrl") ||
                    source.contains("WebUrlNormalizer.isHttpUrl")
            )
            assertFalse(source.contains("scheme != \"http\" && scheme != \"https\""))
            assertFalse(
                source.contains(
                    "WebSchemePolicy.isHttpOrHttpsScheme(uri.scheme) && !uri.host.isNullOrBlank()"
                )
            )
            assertFalse(
                source.contains(
                    "WebSchemePolicy.isHttpScheme(uri.scheme) && !uri.host.isNullOrBlank()"
                )
            )
        }
    }

}
