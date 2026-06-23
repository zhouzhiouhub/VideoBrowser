package com.example.videobrowser.utils

import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfirmationDialogContractTest {
    @Test
    fun confirmationDialogsShareOneBuilderImplementation() {
        val confirmationDialog = projectFile(
            "src/main/java/com/example/videobrowser/utils/ConfirmationDialog.kt"
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
        val simpleConfirmationPages = listOf(
            "AdBlockLogPage.kt",
            "PlaybackHistoryPage.kt",
            "RestoreDefaultSettingsPage.kt",
            "SitePermissionsPage.kt",
            "UserManualRulesPage.kt",
            "UserWhitelistPage.kt"
        ).map { fileName ->
            projectFile("src/main/java/com/example/videobrowser/functioncenter/$fileName").readText()
        }
        val ruleSubscriptionPage = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/RuleSubscriptionPage.kt"
        ).readText()

        assertTrue(confirmationDialog.contains("object ConfirmationDialog"))
        assertTrue(confirmationDialog.contains("AlertDialog.Builder(activity)"))
        assertTrue(confirmationDialog.contains("messageRes: Int"))
        assertTrue(confirmationDialog.contains("message: String"))
        assertTrue(confirmationDialog.contains(".setPositiveButton(positiveButtonRes) { _, _ -> onConfirmed() }"))
        assertEquals(1, Regex("AlertDialog\\.Builder\\(activity\\)").findAll(confirmationDialog).count())

        listOf(
            dataManagementDialogs,
            downloadsDialogs,
            savedPagesDialogs,
            ruleSubscriptionPage
        ).plus(simpleConfirmationPages).forEach { source ->
            assertTrue(source.contains("ConfirmationDialog.show("))
            assertFalse(source.contains("private fun showConfirmationDialog("))
            assertFalse(source.contains(".setPositiveButton(positiveButtonRes) { _, _ -> onConfirmed() }"))
        }

        assertEquals(2, Regex("AlertDialog\\.Builder\\(activity\\)").findAll(dataManagementDialogs).count())
        assertEquals(4, Regex("AlertDialog\\.Builder\\(activity\\)").findAll(downloadsDialogs).count())
        assertEquals(4, Regex("AlertDialog\\.Builder\\(activity\\)").findAll(savedPagesDialogs).count())
        assertEquals(2, Regex("AlertDialog\\.Builder\\(activity\\)").findAll(ruleSubscriptionPage).count())
        simpleConfirmationPages.forEach { source ->
            assertEquals(0, Regex("AlertDialog\\.Builder\\(activity\\)").findAll(source).count())
            assertFalse(source.contains("import androidx.appcompat.app.AlertDialog"))
        }
        assertEquals(1, Regex("record\\.title\\.ifBlank \\{ record\\.fileName \\}").findAll(downloadsDialogs).count())
        assertFalse(downloadsDialogs.contains(".setMessage(R.string.dialog_clear_download_records_message)"))
        assertFalse(savedPagesDialogs.contains(".setMessage(R.string.dialog_clear_saved_pages_message)"))
    }
}
