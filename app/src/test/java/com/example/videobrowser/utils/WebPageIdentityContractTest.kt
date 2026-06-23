package com.example.videobrowser.utils

import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebPageIdentityContractTest {
    @Test
    fun samePageCallersShareWebPageIdentity() {
        val identity = projectFile(
            "src/main/java/com/example/videobrowser/utils/WebPageIdentity.kt"
        ).readText()
        val historyPolicy = projectFile(
            "src/main/java/com/example/videobrowser/browser/HistoryRecordPolicy.kt"
        ).readText()
        val searchHomeMatcher = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/SearchProviderHomeMatcher.kt"
        ).readText()

        assertTrue(identity.contains("data class WebPageIdentity"))
        assertTrue(identity.contains("SafeUriParser.parse(value)"))
        assertTrue(identity.contains("HostNameNormalizer.normalize(uri.host)"))
        assertTrue(identity.contains("WebSchemePolicy.isHttpOrHttpsScheme(scheme)"))
        assertTrue(identity.contains("private fun normalizedPort(scheme: String, port: Int): Int"))

        listOf(historyPolicy, searchHomeMatcher).forEach { source ->
            assertTrue(source.contains("WebPageIdentity.from("))
            assertFalse(source.contains("data class WebUrl"))
            assertFalse(source.contains("android.net.Uri"))
            assertFalse(source.contains("Uri.parse("))
            assertFalse(source.contains("private fun normalizedPath("))
            assertFalse(source.contains("private fun normalizedPort("))
        }
    }
}
