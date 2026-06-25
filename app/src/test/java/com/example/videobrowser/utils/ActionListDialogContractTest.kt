package com.example.videobrowser.utils

import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActionListDialogContractTest {
    @Test
    fun actionListDialogsShareSetItemsShell() {
        val actionListDialog = projectFile(
            "src/main/java/com/example/videobrowser/utils/ActionListDialog.kt"
        ).readText()
        val downloadsDialogs = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/DownloadsPageDialogController.kt"
        ).readText()
        val savedPagesDialogs = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPagesDialogController.kt"
        ).readText()
        val searchProviderDialogs = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/SearchProviderDialogController.kt"
        ).readText()
        val linkContextMenu = projectFile(
            "src/main/java/com/example/videobrowser/browser/LinkContextMenuController.kt"
        ).readText()
        val dataManagementDialogs = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserDataManagementDialogController.kt"
        ).readText()
        val nativeTrackSelectionDialogs = projectFile(
            "src/main/java/com/example/videobrowser/video/NativeTrackSelectionDialogController.kt"
        ).readText()
        val nativePlaybackQueueDialogs = projectFile(
            "src/main/java/com/example/videobrowser/video/NativePlaybackQueueDialogController.kt"
        ).readText()

        assertTrue(actionListDialog.contains("object ActionListDialog"))
        assertTrue(actionListDialog.contains("data class DialogAction("))
        assertTrue(actionListDialog.contains("data class DialogButtonAction("))
        assertTrue(actionListDialog.contains("AppDialog.builder(activity)"))
        assertTrue(actionListDialog.contains(".setItems(actions.map { action -> action.title }.toTypedArray())"))
        assertTrue(actionListDialog.contains("negativeButtonRes: Int? = null"))
        assertTrue(actionListDialog.contains("positiveButton: DialogButtonAction? = null"))
        assertTrue(actionListDialog.contains("neutralButton: DialogButtonAction? = null"))
        assertEquals(1, Regex("AppDialog\\.builder\\(activity\\)").findAll(actionListDialog).count())

        listOf(
            downloadsDialogs,
            searchProviderDialogs,
            linkContextMenu,
            dataManagementDialogs,
            nativeTrackSelectionDialogs,
            nativePlaybackQueueDialogs
        ).forEach { source ->
            assertTrue(source.contains("ActionListDialog.show("))
            assertFalse(source.contains(".setItems(actions.map { action -> action.title }.toTypedArray())"))
        }
        assertEquals(0, Regex("AlertDialog\\.Builder\\(activity\\)").findAll(dataManagementDialogs).count())
        assertEquals(0, Regex("AlertDialog\\.Builder\\(activity\\)").findAll(nativeTrackSelectionDialogs).count())
        assertEquals(0, Regex("AlertDialog\\.Builder\\(activity\\)").findAll(nativePlaybackQueueDialogs).count())
        assertFalse(savedPagesDialogs.contains("ActionListDialog.show("))
        assertFalse(savedPagesDialogs.contains("DialogAction("))
        assertFalse(downloadsDialogs.contains("private data class DownloadRecordAction"))
        assertFalse(savedPagesDialogs.contains("private data class SavedPageAction"))
        assertFalse(linkContextMenu.contains("private data class UrlContextMenuAction"))
    }
}
