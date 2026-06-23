package com.example.videobrowser.site

import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SiteHostUsageContractTest {
    @Test
    fun urlHostCallersShareSiteHostParser() {
        val sources = listOf(
            projectFile("src/main/java/com/example/videobrowser/functioncenter/AdBlockLogPage.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/PagePrintController.kt"),
            projectFile(
                "src/main/java/com/example/videobrowser/browser/ChromeJavaScriptDialogController.kt"
            ),
            projectFile("src/main/java/com/example/videobrowser/browser/PageActionsController.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/SiteSecurityController.kt")
        ).map { file -> file.readText() }

        sources.forEach { source ->
            assertTrue(source.contains("SiteHost.fromUrl("))
            assertFalse(source.contains("Uri.parse(pageUrl).host"))
            assertFalse(source.contains("Uri.parse(url).host"))
            assertFalse(source.contains("Uri.parse(entry.url).host"))
        }
    }
}
