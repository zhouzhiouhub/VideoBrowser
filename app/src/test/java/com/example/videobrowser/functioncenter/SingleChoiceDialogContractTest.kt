package com.example.videobrowser.functioncenter

import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SingleChoiceDialogContractTest {
    @Test
    fun singleChoiceDialogsShareBuilderShell() {
        val singleChoiceDialog = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SingleChoiceDialog.kt"
        ).readText()
        val browserSettingsDialogs = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserSettingsDialogController.kt"
        ).readText()
        val currentSitePermissionSection = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/CurrentSitePermissionSection.kt"
        ).readText()
        val downloadsDialogs = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/DownloadsPageDialogController.kt"
        ).readText()

        assertTrue(singleChoiceDialog.contains("internal object SingleChoiceDialog"))
        assertTrue(singleChoiceDialog.contains("fun show("))
        assertTrue(singleChoiceDialog.contains("titleRes: Int"))
        assertTrue(singleChoiceDialog.contains("title: String"))
        assertTrue(singleChoiceDialog.contains("setSingleChoiceItems(labels.toTypedArray(), checkedIndex)"))
        assertEquals(1, Regex("AppDialog\\.builder\\(activity\\)").findAll(singleChoiceDialog).count())

        assertEquals(2, Regex("SingleChoiceDialog\\.show\\(").findAll(browserSettingsDialogs).count())
        assertEquals(1, Regex("SingleChoiceDialog\\.show\\(").findAll(currentSitePermissionSection).count())
        assertEquals(1, Regex("SingleChoiceDialog\\.show\\(").findAll(downloadsDialogs).count())
        listOf(browserSettingsDialogs, currentSitePermissionSection, downloadsDialogs).forEach { source ->
            assertFalse(source.contains("setSingleChoiceItems("))
        }
    }
}
