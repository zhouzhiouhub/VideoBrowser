package com.example.videobrowser.storage

import org.junit.Assert.assertEquals
import org.junit.Test

class SavedPageSearchTest {
    @Test
    fun filterReturnsAllPagesForBlankQuery() {
        val pages = samplePages()

        assertEquals(pages, SavedPageSearch.filter(pages, " "))
    }

    @Test
    fun filterMatchesTitleAndUrlCaseInsensitively() {
        val results = SavedPageSearch.filter(samplePages(), "video EXAMPLE")

        assertEquals(listOf("https://video.example.com/watch"), results.map { it.url })
    }

    @Test
    fun filterRequiresEverySearchTerm() {
        val results = SavedPageSearch.filter(samplePages(), "docs api")

        assertEquals(listOf("https://docs.example.com/api"), results.map { it.url })
    }

    private fun samplePages(): List<SavedPage> {
        return listOf(
            SavedPage(title = "Video Home", url = "https://video.example.com/watch"),
            SavedPage(title = "Documentation", url = "https://docs.example.com/api"),
            SavedPage(title = "News", url = "https://news.example.com")
        )
    }
}
