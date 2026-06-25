package com.example.videobrowser.functioncenter

import com.example.videobrowser.storage.SavedPage
import org.junit.Assert.assertEquals
import org.junit.Test

class SavedPageHistoryCategoryTest {
    @Test
    fun historyCategoriesExposeOnlyImplementedFilters() {
        assertEquals(
            listOf("ALL", "URL", "VIDEO"),
            SavedPageHistoryCategory.values().map { category -> category.name }
        )
    }

    @Test
    fun allCategoryShowsEveryHistoryRecord() {
        val pages = listOf(
            SavedPage(title = "Example", url = "https://example.com"),
            SavedPage(title = "https://url.example.com", url = "https://url.example.com")
        )

        assertEquals(pages, SavedPageHistoryCategory.ALL.filter(pages))
    }

    @Test
    fun urlCategoryShowsUrlOnlyRecords() {
        val urlOnly = SavedPage(
            title = "https://url.example.com",
            url = "https://url.example.com"
        )
        val emptyTitle = SavedPage(title = "", url = "https://empty.example.com")
        val titled = SavedPage(title = "Example", url = "https://example.com")

        assertEquals(
            listOf(urlOnly, emptyTitle),
            SavedPageHistoryCategory.URL.filter(listOf(urlOnly, emptyTitle, titled))
        )
    }

    @Test
    fun videoCategoryShowsPlayableOrVideoNamedRecords() {
        val media = SavedPage(title = "Movie", url = "https://cdn.example.com/movie.mp4")
        val named = SavedPage(title = "Bilibili clip", url = "https://example.com/watch")
        val page = SavedPage(title = "Article", url = "https://example.com/article")

        assertEquals(
            listOf(media, named),
            SavedPageHistoryCategory.VIDEO.filter(listOf(media, named, page))
        )
    }
}
