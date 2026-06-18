package com.example.videobrowser.utils

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HostNameNormalizerContractTest {
    @Test
    fun `host parsing and domain matching callers share host normalizer`() {
        val sources = listOf(
            projectFile("src/main/java/com/example/videobrowser/site/SiteHost.kt"),
            projectFile("src/main/java/com/example/videobrowser/site/SiteAdapterRegistry.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/RequestContext.kt"),
            projectFile("src/main/java/com/example/videobrowser/rules/RuleMatcher.kt"),
            projectFile("src/main/java/com/example/videobrowser/rules/DomainScope.kt"),
            projectFile("src/main/java/com/example/videobrowser/rules/RuleSubscriptionFetcher.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/HistoryRecordPolicy.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/search/HomeQuickLinkBuilder.kt")
        ).map { file -> file.readText() }

        sources.forEach { source ->
            assertTrue(source.contains("HostNameNormalizer"))
            assertFalse(source.contains("private fun normalizeHost"))
            assertFalse(source.contains("private fun hostFromUrl"))
            assertFalse(source.contains("private fun parseHost"))
            assertFalse(source.contains("host == domain || host.endsWith"))
        }
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
