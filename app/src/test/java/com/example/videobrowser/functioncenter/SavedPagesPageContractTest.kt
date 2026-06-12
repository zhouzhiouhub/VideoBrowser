package com.example.videobrowser.functioncenter

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class SavedPagesPageContractTest {
    @Test
    fun savedPagesPageSupportsSearchFiltering() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPagesPage.kt"
        ).readText()

        assertTrue(page.contains("SavedPageSearch.filter(allPages, query)"))
        assertTrue(page.contains("R.string.action_search_saved_pages"))
        assertTrue(page.contains("private fun showSearchDialog"))
        assertTrue(page.contains("R.string.action_clear_search"))
        assertTrue(page.contains("R.string.dialog_saved_pages_search_empty"))
    }

    @Test
    fun savedPagesPageCanCopyRecordLinks() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPagesPage.kt"
        ).readText()

        assertTrue(page.contains("private fun savedPageActions"))
        assertTrue(page.contains("R.string.action_copy_link"))
        assertTrue(page.contains("copySavedPageUrl(page)"))
        assertTrue(page.contains("ClipData.newPlainText"))
        assertTrue(page.contains("Context.CLIPBOARD_SERVICE"))
        assertTrue(page.contains("R.string.clipboard_page_url"))
        assertTrue(page.contains("R.string.toast_link_copied"))
    }

    @Test
    fun savedPagesPageCanShareRecordLinks() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPagesPage.kt"
        ).readText()

        assertTrue(page.contains("R.string.action_share_page"))
        assertTrue(page.contains("shareSavedPageUrl(page)"))
        assertTrue(page.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(page.contains("type = \"text/plain\""))
        assertTrue(page.contains("putExtra(Intent.EXTRA_TEXT, page.url)"))
        assertTrue(page.contains("Intent.createChooser(intent, activity.getString(R.string.action_share_page))"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
