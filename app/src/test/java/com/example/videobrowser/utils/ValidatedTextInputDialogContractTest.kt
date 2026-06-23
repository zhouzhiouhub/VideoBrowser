package com.example.videobrowser.utils

import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatedTextInputDialogContractTest {
    @Test
    fun validatedTextInputsSharePositiveButtonValidationShell() {
        val validatedDialog = projectFile(
            "src/main/java/com/example/videobrowser/utils/ValidatedTextInputDialog.kt"
        ).readText()
        val browserSettingsDialogs = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserSettingsDialogController.kt"
        ).readText()
        val savedPagesDialogs = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPagesDialogController.kt"
        ).readText()
        val localDocumentOperations = projectFile(
            "src/main/java/com/example/videobrowser/localfiles/LocalDocumentOperationController.kt"
        ).readText()
        val ruleSubscriptionPage = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/RuleSubscriptionPage.kt"
        ).readText()

        assertTrue(validatedDialog.contains("internal object ValidatedTextInputDialog"))
        assertTrue(validatedDialog.contains("inputType: Int"))
        assertTrue(validatedDialog.contains("singleLine: Boolean = true"))
        assertTrue(validatedDialog.contains("minLines: Int? = null"))
        assertTrue(validatedDialog.contains("maxLines: Int? = null"))
        assertTrue(validatedDialog.contains("imeOptions: Int? = null"))
        assertTrue(validatedDialog.contains("selectAllOnFocus: Boolean = false"))
        assertTrue(validatedDialog.contains("valueTransform: (String) -> String = { value -> value }"))
        assertTrue(validatedDialog.contains("saveValue: (String) -> Boolean"))
        assertTrue(validatedDialog.contains("onSaved: (String) -> Unit"))
        assertTrue(validatedDialog.contains("dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener"))
        assertTrue(validatedDialog.contains("Toast.makeText(activity, invalidToastRes, Toast.LENGTH_SHORT).show()"))
        assertTrue(validatedDialog.contains("if (successToastRes != null)"))
        assertEquals(1, Regex("AlertDialog\\.Builder\\(activity\\)").findAll(validatedDialog).count())

        assertEquals(1, Regex("ValidatedTextInputDialog\\.show\\(").findAll(browserSettingsDialogs).count())
        assertEquals(2, Regex("ValidatedTextInputDialog\\.show\\(").findAll(savedPagesDialogs).count())
        assertEquals(1, Regex("ValidatedTextInputDialog\\.show\\(").findAll(localDocumentOperations).count())
        assertEquals(2, Regex("ValidatedTextInputDialog\\.show\\(").findAll(ruleSubscriptionPage).count())
        listOf(browserSettingsDialogs, savedPagesDialogs, localDocumentOperations, ruleSubscriptionPage).forEach { source ->
            assertFalse(source.contains("dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener"))
            assertFalse(source.contains("setOnShowListener"))
            assertFalse(source.contains("AlertDialog.Builder(activity)"))
        }
        assertFalse(localDocumentOperations.contains("EditText(activity)"))
        assertFalse(ruleSubscriptionPage.contains("EditText(activity)"))
    }
}
