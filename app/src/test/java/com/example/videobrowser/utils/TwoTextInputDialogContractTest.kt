package com.example.videobrowser.utils

import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TwoTextInputDialogContractTest {
    @Test
    fun twoTextInputsShareFormAndPositiveButtonShell() {
        val twoTextInputDialog = projectFile(
            "src/main/java/com/example/videobrowser/utils/TwoTextInputDialog.kt"
        ).readText()
        val searchProviderDialogs = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/SearchProviderDialogController.kt"
        ).readText()
        val httpAuthController = projectFile(
            "src/main/java/com/example/videobrowser/browser/HttpAuthController.kt"
        ).readText()

        assertTrue(twoTextInputDialog.contains("object TwoTextInputDialog"))
        assertTrue(twoTextInputDialog.contains("data class TextInputDialogField("))
        assertTrue(twoTextInputDialog.contains("data class TwoTextInputValues("))
        assertTrue(twoTextInputDialog.contains("AppDialog.builder(activity)"))
        assertTrue(twoTextInputDialog.contains("dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener"))
        assertEquals(1, Regex("AppDialog\\.builder\\(activity\\)").findAll(twoTextInputDialog).count())

        assertTrue(searchProviderDialogs.contains("TwoTextInputDialog.show("))
        assertTrue(httpAuthController.contains("TwoTextInputDialog.create("))
        listOf(searchProviderDialogs, httpAuthController).forEach { source ->
            assertFalse(source.contains("EditText(activity)"))
            assertFalse(source.contains("LinearLayout(activity)"))
            assertFalse(source.contains("AlertDialog.Builder(activity)"))
            assertFalse(source.contains("dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener"))
        }
        assertFalse(httpAuthController.contains("private fun createCredentialForm("))
    }
}
