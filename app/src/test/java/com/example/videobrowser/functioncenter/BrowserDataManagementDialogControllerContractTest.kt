package com.example.videobrowser.functioncenter

import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserDataManagementDialogControllerContractTest {
    @Test
    fun confirmationDialogsShareBuilderShell() {
        val source = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserDataManagementDialogController.kt"
        ).readText()

        assertTrue(source.contains("private fun showConfirmationDialog("))
        assertTrue(source.contains("messageRes: Int"))
        assertTrue(source.contains("message: String"))
        assertTrue(source.contains(".setPositiveButton(positiveButtonRes) { _, _ -> onConfirmed() }"))
        assertEquals(3, Regex("AlertDialog\\.Builder\\(activity\\)").findAll(source).count())
        assertEquals(1, Regex("\\.setMessage\\(message\\)").findAll(source).count())
    }
}
