package com.example.videobrowser.browser

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebPermissionPendingRequestStoreContractTest {
    @Test
    fun webPermissionControllerDelegatesPendingRequestStateToStore() {
        val webPermissionController = projectFile(
            "src/main/java/com/example/videobrowser/browser/WebPermissionRequestController.kt"
        ).readText()
        val pendingRequestStore = projectFile(
            "src/main/java/com/example/videobrowser/browser/WebPermissionPendingRequestStore.kt"
        ).readText()

        assertTrue(webPermissionController.contains("WebPermissionPendingRequestStore()"))
        assertTrue(webPermissionController.contains("pendingRequestStore.replaceWith(request)"))
        assertTrue(webPermissionController.contains("takePendingRequest = pendingRequestStore::take"))
        assertTrue(webPermissionController.contains("pendingRequestStore.clearIfPending(request)"))
        assertTrue(webPermissionController.contains("private fun cancelAllPendingPermissionFlows()"))
        assertTrue(webPermissionController.contains("cancelAllPendingPermissionFlows()"))
        assertTrue(webPermissionController.contains("pendingRequestStore.cancelPending()"))
        assertTrue(webPermissionController.contains("webPermissionPromptController.cancelPendingPrompt()"))
        val cancelAllFlow = webPermissionController.substringAfter(
            "private fun cancelAllPendingPermissionFlows()"
        ).substringBefore("\n    }")
        assertEquals(
            1,
            Regex("pendingRequestStore\\.cancelPending\\(\\)").findAll(cancelAllFlow).count()
        )
        assertEquals(
            1,
            Regex("webPermissionPromptController\\.cancelPendingPrompt\\(\\)").findAll(cancelAllFlow).count()
        )
        assertFalse(webPermissionController.contains("private var pendingWebPermissionRequest"))

        assertTrue(pendingRequestStore.contains("private var pendingWebPermissionRequest: PermissionRequest? = null"))
        assertTrue(pendingRequestStore.contains("fun replaceWith(request: PermissionRequest)"))
        assertTrue(pendingRequestStore.contains("pendingWebPermissionRequest?.deny()"))
        assertTrue(pendingRequestStore.contains("fun take(): PermissionRequest?"))
        assertTrue(pendingRequestStore.contains("fun clearIfPending(request: PermissionRequest)"))
        assertTrue(pendingRequestStore.contains("fun cancelPending()"))
    }

}
