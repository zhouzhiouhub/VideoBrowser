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
        val localDocumentOperations = projectFile(
            "src/main/java/com/example/videobrowser/localfiles/LocalDocumentOperationController.kt"
        ).readText()
        val downloadEnqueueController = projectFile(
            "src/main/java/com/example/videobrowser/download/DownloadEnqueueController.kt"
        ).readText()
        val browserNavigationController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserNavigationController.kt"
        ).readText()
        val searchProviderDialogs = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/SearchProviderDialogController.kt"
        ).readText()
        val elementPickerController = projectFile(
            "src/main/java/com/example/videobrowser/element/ElementPickerController.kt"
        ).readText()

        assertTrue(confirmationDialog.contains("object ConfirmationDialog"))
        assertTrue(confirmationDialog.contains("fun create("))
        assertTrue(confirmationDialog.contains("AlertDialog.Builder(activity)"))
        assertTrue(confirmationDialog.contains("messageRes: Int"))
        assertTrue(confirmationDialog.contains("message: String"))
        assertTrue(confirmationDialog.contains(".setPositiveButton(positiveButtonRes) { _, _ -> onConfirmed() }"))
        assertEquals(1, Regex("AlertDialog\\.Builder\\(activity\\)").findAll(confirmationDialog).count())

        listOf(
            dataManagementDialogs,
            downloadsDialogs,
            savedPagesDialogs,
            ruleSubscriptionPage,
            localDocumentOperations,
            downloadEnqueueController,
            browserNavigationController,
            searchProviderDialogs
        ).plus(simpleConfirmationPages).forEach { source ->
            assertTrue(source.contains("ConfirmationDialog.show("))
            assertFalse(source.contains("private fun showConfirmationDialog("))
            assertFalse(source.contains(".setPositiveButton(positiveButtonRes) { _, _ -> onConfirmed() }"))
        }
        assertTrue(elementPickerController.contains("ConfirmationDialog.create("))
        assertFalse(elementPickerController.contains("AlertDialog.Builder(activity)"))
        assertFalse(elementPickerController.contains(".setPositiveButton(R.string.action_block_element)"))

        assertEquals(0, Regex("AlertDialog\\.Builder\\(activity\\)").findAll(dataManagementDialogs).count())
        assertEquals(0, Regex("AlertDialog\\.Builder\\(activity\\)").findAll(downloadsDialogs).count())
        assertEquals(0, Regex("AlertDialog\\.Builder\\(activity\\)").findAll(savedPagesDialogs).count())
        assertEquals(0, Regex("AlertDialog\\.Builder\\(activity\\)").findAll(ruleSubscriptionPage).count())
        assertEquals(0, Regex("AlertDialog\\.Builder\\(activity\\)").findAll(localDocumentOperations).count())
        assertEquals(0, Regex("AlertDialog\\.Builder\\(activity\\)").findAll(downloadEnqueueController).count())
        assertEquals(0, Regex("AlertDialog\\.Builder\\(activity\\)").findAll(browserNavigationController).count())
        assertEquals(0, Regex("AlertDialog\\.Builder\\(activity\\)").findAll(searchProviderDialogs).count())
        assertEquals(0, Regex("AlertDialog\\.Builder\\(activity\\)").findAll(elementPickerController).count())
        simpleConfirmationPages.forEach { source ->
            assertEquals(0, Regex("AlertDialog\\.Builder\\(activity\\)").findAll(source).count())
            assertFalse(source.contains("import androidx.appcompat.app.AlertDialog"))
        }
        assertEquals(1, Regex("record\\.title\\.ifBlank \\{ record\\.fileName \\}").findAll(downloadsDialogs).count())
        assertEquals(
            1,
            Regex("R\\.string\\.toast_local_file_operation_failed").findAll(localDocumentOperations).count()
        )
        assertEquals(2, Regex("ConfirmationDialog\\.show\\(").findAll(downloadEnqueueController).count())
        assertEquals(1, Regex("ConfirmationDialog\\.show\\(").findAll(browserNavigationController).count())
        assertEquals(2, Regex("ConfirmationDialog\\.show\\(").findAll(searchProviderDialogs).count())
        assertFalse(downloadsDialogs.contains(".setMessage(R.string.dialog_clear_download_records_message)"))
        assertFalse(savedPagesDialogs.contains(".setMessage(R.string.dialog_clear_saved_pages_message)"))
    }
}
