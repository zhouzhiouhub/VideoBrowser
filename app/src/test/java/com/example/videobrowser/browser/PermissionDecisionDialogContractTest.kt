package com.example.videobrowser.browser

import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionDecisionDialogContractTest {
    @Test
    fun permissionPromptsShareAllowOnceDenyDialogShell() {
        val permissionDecisionDialog = projectFile(
            "src/main/java/com/example/videobrowser/browser/PermissionDecisionDialog.kt"
        ).readText()
        val webPermissionPrompt = projectFile(
            "src/main/java/com/example/videobrowser/browser/WebPermissionPromptController.kt"
        ).readText()
        val geolocationPermission = projectFile(
            "src/main/java/com/example/videobrowser/browser/GeolocationPermissionController.kt"
        ).readText()

        assertTrue(permissionDecisionDialog.contains("internal object PermissionDecisionDialog"))
        assertTrue(permissionDecisionDialog.contains("AppDialog.builder(activity)"))
        assertTrue(permissionDecisionDialog.contains("R.string.action_allow"))
        assertTrue(permissionDecisionDialog.contains("R.string.action_allow_once"))
        assertTrue(permissionDecisionDialog.contains("R.string.action_deny"))
        assertTrue(permissionDecisionDialog.contains("dialog.setOnCancelListener"))
        assertEquals(1, Regex("AppDialog\\.builder\\(activity\\)").findAll(permissionDecisionDialog).count())

        listOf(webPermissionPrompt, geolocationPermission).forEach { source ->
            assertTrue(source.contains("PermissionDecisionDialog.create("))
            assertTrue(source.contains("rememberDecision = false"))
            assertFalse(source.contains("AlertDialog.Builder(activity)"))
            assertFalse(source.contains("R.string.action_allow_once"))
            assertFalse(source.contains("R.string.action_deny"))
        }
    }
}
