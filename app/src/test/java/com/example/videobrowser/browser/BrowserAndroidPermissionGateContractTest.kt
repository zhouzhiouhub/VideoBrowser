package com.example.videobrowser.browser

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserAndroidPermissionGateContractTest {
    @Test
    fun browserPermissionControllersShareAndroidRuntimePermissionGate() {
        val gate = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserAndroidPermissionGate.kt"
        ).readText()
        val webController = projectFile(
            "src/main/java/com/example/videobrowser/browser/WebPermissionRequestController.kt"
        ).readText()
        val geolocationController = projectFile(
            "src/main/java/com/example/videobrowser/browser/GeolocationPermissionController.kt"
        ).readText()

        assertTrue(gate.contains("internal class BrowserAndroidPermissionGate<T>"))
        assertTrue(gate.contains("fun continueOrRequest(request: T)"))
        assertTrue(gate.contains("fun handleResult(grants: Map<String, Boolean>)"))
        assertTrue(gate.contains("internal enum class BrowserAndroidPermissionResultPolicy"))
        assertTrue(gate.contains("ALL_REQUIRED -> permissions.all"))
        assertTrue(gate.contains("ANY_REQUIRED -> permissions.any"))
        assertTrue(gate.contains("requestAndroidPermissions("))
        assertTrue(gate.contains("replacePendingRequest(request)"))
        assertTrue(gate.contains("takePendingRequest() ?: return"))

        assertTrue(webController.contains("BrowserAndroidPermissionGate("))
        assertTrue(webController.contains("resultPolicy = BrowserAndroidPermissionResultPolicy.ALL_REQUIRED"))
        assertTrue(webController.contains("androidPermissionGate.continueOrRequest(request)"))
        assertTrue(webController.contains("androidPermissionGate.handleResult(grants)"))

        assertTrue(geolocationController.contains("BrowserAndroidPermissionGate("))
        assertTrue(geolocationController.contains("resultPolicy = BrowserAndroidPermissionResultPolicy.ANY_REQUIRED"))
        assertTrue(geolocationController.contains("androidPermissionGate.continueOrRequest(GeolocationPermissionPrompt(origin, callback))"))
        assertTrue(geolocationController.contains("androidPermissionGate.handleResult(grants)"))

        assertFalse(webController.contains("filterNot(hasAndroidPermission)"))
        assertFalse(webController.contains("requestAndroidPermissions(missingPermissions)"))
        assertFalse(geolocationController.contains("permissions.any(hasAndroidPermission)"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
