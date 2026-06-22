package com.example.videobrowser.functioncenter

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchSummaryFormatterContractTest {
    @Test
    fun `searchable pages share current search summary formatting`() {
        val sources = listOf(
            projectFile("src/main/java/com/example/videobrowser/functioncenter/DownloadsPage.kt"),
            projectFile("src/main/java/com/example/videobrowser/functioncenter/BrowserSiteDataManagementPage.kt"),
            projectFile("src/main/java/com/example/videobrowser/functioncenter/SavedPagesPage.kt")
        ).map { file -> file.readText() }

        sources.forEach { source ->
            assertTrue(source.contains("SearchSummaryFormatter.current("))
            assertFalse(source.contains("private fun currentSearchSummary"))
            assertFalse(source.contains("fun currentSearchSummary"))
        }
    }

}
