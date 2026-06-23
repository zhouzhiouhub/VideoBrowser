package com.example.videobrowser.functioncenter

import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchQueryDialogContractTest {
    @Test
    fun functionCenterSearchDialogsShareInputShell() {
        val searchDialog = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SearchQueryDialog.kt"
        ).readText()
        val dataManagementDialogs = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserDataManagementDialogController.kt"
        ).readText()
        val downloadsDialogs = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/DownloadsPageDialogController.kt"
        ).readText()
        val savedPagesDialogs = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPagesDialogController.kt"
        ).readText()

        assertTrue(searchDialog.contains("internal object SearchQueryDialog"))
        assertTrue(searchDialog.contains("InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS"))
        assertTrue(searchDialog.contains("currentQuery.orEmpty()"))
        assertTrue(searchDialog.contains("input.text?.toString()?.trim().orEmpty()"))
        assertEquals(1, Regex("AlertDialog\\.Builder\\(activity\\)").findAll(searchDialog).count())

        listOf(dataManagementDialogs, downloadsDialogs, savedPagesDialogs).forEach { source ->
            assertTrue(source.contains("SearchQueryDialog.show("))
        }
        assertEquals(1, Regex("SearchQueryDialog\\.show\\(").findAll(dataManagementDialogs).count())
        assertEquals(1, Regex("SearchQueryDialog\\.show\\(").findAll(downloadsDialogs).count())
        assertEquals(1, Regex("SearchQueryDialog\\.show\\(").findAll(savedPagesDialogs).count())
        assertFalse(dataManagementDialogs.contains("TYPE_TEXT_FLAG_NO_SUGGESTIONS"))
        assertFalse(downloadsDialogs.contains("TYPE_TEXT_FLAG_NO_SUGGESTIONS"))
        assertFalse(savedPagesDialogs.contains("hint = activity.getString(R.string.hint_saved_pages_search)"))
    }
}
