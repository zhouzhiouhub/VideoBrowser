package com.example.videobrowser.utils

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchQueryTermsContractTest {
    @Test
    fun `search filters share query term parsing`() {
        val downloadSearch = projectFile(
            "src/main/java/com/example/videobrowser/download/DownloadRecordSearch.kt"
        ).readText()
        val siteDataSearch = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserSiteDataOriginSearch.kt"
        ).readText()
        val savedPageSearch = projectFile(
            "src/main/java/com/example/videobrowser/storage/SavedPageSearch.kt"
        ).readText()

        listOf(downloadSearch, siteDataSearch, savedPageSearch).forEach { source ->
            assertTrue(source.contains("SearchQueryTerms.parse(query)"))
            assertTrue(source.contains("SearchQueryTerms.containsAll("))
            assertFalse(source.contains("split(Regex(\"\\\\s+\"))"))
        }
    }

}
