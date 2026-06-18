package com.example.videobrowser.utils

import java.io.File
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
            projectFile("src/main/java/com/example/videobrowser/browser/HttpNavigationSafetyPolicy.kt")
        ).map { file -> file.readText() }

        sources.forEach { source ->
            assertTrue(source.contains("WebSchemePolicy."))
            assertFalse(source.contains("private fun isHttpScheme"))
            assertFalse(source.contains("scheme.equals(\"http\", ignoreCase = true)"))
            assertFalse(source.contains("scheme != \"http\" && scheme != \"https\""))
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
