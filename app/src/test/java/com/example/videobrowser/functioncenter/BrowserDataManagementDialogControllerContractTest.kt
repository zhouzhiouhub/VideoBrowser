package com.example.videobrowser.functioncenter

import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserDataManagementDialogControllerContractTest {
    @Test
    fun confirmationDialogsShareBuilderShell() {
        val source = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserDataManagementDialogController.kt"
        ).readText()

        assertTrue(source.contains("ConfirmationDialog.show("))
        assertTrue(source.contains("ActionListDialog.show("))
        assertFalse(source.contains("private fun showConfirmationDialog("))
        assertEquals(8, Regex("ConfirmationDialog\\.show\\(").findAll(source).count())
        assertEquals(0, Regex("AlertDialog\\.Builder\\(activity\\)").findAll(source).count())
    }
}
