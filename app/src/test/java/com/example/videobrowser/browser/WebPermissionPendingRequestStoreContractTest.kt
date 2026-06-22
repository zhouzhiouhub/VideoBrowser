package com.example.videobrowser.browser

import com.example.videobrowser.testutil.projectFile

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
        assertTrue(webPermissionController.contains("pendingRequestStore.cancelPending()"))
        assertFalse(webPermissionController.contains("private var pendingWebPermissionRequest"))

        assertTrue(pendingRequestStore.contains("private var pendingWebPermissionRequest: PermissionRequest? = null"))
        assertTrue(pendingRequestStore.contains("fun replaceWith(request: PermissionRequest)"))
        assertTrue(pendingRequestStore.contains("pendingWebPermissionRequest?.deny()"))
        assertTrue(pendingRequestStore.contains("fun take(): PermissionRequest?"))
        assertTrue(pendingRequestStore.contains("fun clearIfPending(request: PermissionRequest)"))
        assertTrue(pendingRequestStore.contains("fun cancelPending()"))
    }

}
