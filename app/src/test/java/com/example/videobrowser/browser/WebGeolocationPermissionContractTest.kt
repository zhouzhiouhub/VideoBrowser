package com.example.videobrowser.browser

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

class WebGeolocationPermissionContractTest {
    @Test
    fun manifestDeclaresRuntimePermissionsForWebGeolocation() {
        val permissionNames = manifest()
            .elements("uses-permission")
            .map { permission -> permission.androidAttribute("name") }
            .toSet()

        assertTrue(
            "Web geolocation needs ACCESS_FINE_LOCATION permission",
            "android.permission.ACCESS_FINE_LOCATION" in permissionNames
        )
        assertTrue(
            "Web geolocation should support approximate location grants",
            "android.permission.ACCESS_COARSE_LOCATION" in permissionNames
        )
    }

    @Test
    fun browserManagerEnablesWebViewGeolocation() {
        val browserManager = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserManager.kt"
        ).readText()

        assertTrue(browserManager.contains("setGeolocationEnabled(true)"))
    }

    @Test
    fun chromeClientForwardsWebGeolocationPermissionPrompts() {
        val chromeClient = projectFile(
            "src/main/java/com/example/videobrowser/browser/ChromeClient.kt"
        ).readText()

        assertTrue(chromeClient.contains("GeolocationPermissions"))
        assertTrue(chromeClient.contains("geolocationPermissionRequested"))
        assertTrue(chromeClient.contains("geolocationPermissionHidden"))
        assertTrue(chromeClient.contains("override fun onGeolocationPermissionsShowPrompt"))
        assertTrue(chromeClient.contains("override fun onGeolocationPermissionsHidePrompt"))
    }

    @Test
    fun mainActivityMapsWebGeolocationPromptsThroughRuntimePermissions() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()

        assertTrue(mainActivity.contains("pendingGeolocationPermissionPrompt"))
        assertTrue(mainActivity.contains("Manifest.permission.ACCESS_FINE_LOCATION"))
        assertTrue(mainActivity.contains("Manifest.permission.ACCESS_COARSE_LOCATION"))
        assertTrue(mainActivity.contains("geolocationPermissionRequested = ::handleGeolocationPermissionRequest"))
        assertTrue(mainActivity.contains("geolocationPermissionHidden = ::handleGeolocationPermissionHidden"))
        assertTrue(mainActivity.contains("callback.invoke(origin, true, false)"))
        assertTrue(mainActivity.contains("callback.invoke(origin, false, false)"))
        assertTrue(mainActivity.contains("cancelPendingGeolocationPermissionPrompt()"))
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
