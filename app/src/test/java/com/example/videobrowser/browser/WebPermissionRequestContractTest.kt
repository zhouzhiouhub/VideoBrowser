package com.example.videobrowser.browser

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

class WebPermissionRequestContractTest {
    @Test
    fun manifestDeclaresRuntimePermissionsForWebCapture() {
        val permissionNames = manifest()
            .elements("uses-permission")
            .map { permission -> permission.androidAttribute("name") }
            .toSet()

        assertTrue("Camera capture pages need CAMERA permission", "android.permission.CAMERA" in permissionNames)
        assertTrue(
            "Microphone capture pages need RECORD_AUDIO permission",
            "android.permission.RECORD_AUDIO" in permissionNames
        )
    }

    @Test
    fun chromeClientForwardsWebPermissionRequests() {
        val chromeClient = projectFile(
            "src/main/java/com/example/videobrowser/browser/ChromeClient.kt"
        ).readText()

        assertTrue(chromeClient.contains("PermissionRequest"))
        assertTrue(chromeClient.contains("permissionRequested"))
        assertTrue(chromeClient.contains("permissionRequestCanceled"))
        assertTrue(chromeClient.contains("override fun onPermissionRequest"))
        assertTrue(chromeClient.contains("override fun onPermissionRequestCanceled"))
    }

    @Test
    fun mainActivityMapsWebCaptureRequestsThroughRuntimePermissions() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()

        assertTrue(mainActivity.contains("ActivityResultContracts.RequestMultiplePermissions()"))
        assertTrue(mainActivity.contains("pendingWebPermissionRequest: PermissionRequest?"))
        assertTrue(mainActivity.contains("PermissionRequest.RESOURCE_VIDEO_CAPTURE"))
        assertTrue(mainActivity.contains("Manifest.permission.CAMERA"))
        assertTrue(mainActivity.contains("PermissionRequest.RESOURCE_AUDIO_CAPTURE"))
        assertTrue(mainActivity.contains("Manifest.permission.RECORD_AUDIO"))
        assertTrue(mainActivity.contains("request.grant(request.resources)"))
        assertTrue(mainActivity.contains("request.deny()"))
        assertTrue(mainActivity.contains("permissionRequested = ::handleWebPermissionRequest"))
        assertTrue(mainActivity.contains("permissionRequestCanceled = ::handleWebPermissionRequestCanceled"))
    }

    private fun manifest(): Element {
        return DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
        }.newDocumentBuilder()
            .parse(projectFile("src/main/AndroidManifest.xml"))
            .documentElement
    }

    private fun Element.elements(tagName: String): List<Element> {
        val nodes = getElementsByTagName(tagName)
        return List(nodes.length) { index -> nodes.item(index) }
            .filterIsInstance<Element>()
    }

    private fun Element.androidAttribute(name: String): String {
        return getAttributeNS(ANDROID_NAMESPACE, name)
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }

    private companion object {
        private const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"
    }
}
