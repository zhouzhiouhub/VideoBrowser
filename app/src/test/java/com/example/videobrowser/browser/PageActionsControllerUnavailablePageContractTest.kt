package com.example.videobrowser.browser

import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PageActionsControllerUnavailablePageContractTest {
    @Test
    fun unavailableCurrentPageToastIsSharedByPageActions() {
        val source = projectFile(
            "src/main/java/com/example/videobrowser/browser/PageActionsController.kt"
        ).readText()

        assertTrue(source.contains("private fun currentShareableUrlOrShowUnavailable(): String?"))
        assertTrue(source.contains("private fun currentSavedPageOrShowUnavailable(): SavedPage?"))
        assertTrue(source.contains("private fun showNoPageUrlToast()"))
        assertTrue(source.contains("val url = currentShareableUrlOrShowUnavailable() ?: return"))
        assertTrue(source.contains("val page = currentSavedPageOrShowUnavailable() ?: return"))
        assertEquals(1, Regex("R\\.string\\.toast_no_page_url").findAll(source).count())
        assertEquals(1, Regex("currentShareableUrl\\(\\) \\?: run").findAll(source).count())
        assertEquals(1, Regex("currentSavedPage\\(\\) \\?: run").findAll(source).count())
    }
}
