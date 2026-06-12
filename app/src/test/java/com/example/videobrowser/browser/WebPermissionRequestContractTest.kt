package com.example.videobrowser.browser

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertFalse
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
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(mainActivity.contains("ActivityResultContracts.RequestMultiplePermissions()"))
        assertTrue(mainActivity.contains("pendingWebPermissionRequest: PermissionRequest?"))
        assertTrue(mainActivity.contains("pendingWebPermissionPromptRequest: PermissionRequest?"))
        assertTrue(mainActivity.contains("PermissionRequest.RESOURCE_VIDEO_CAPTURE"))
        assertTrue(mainActivity.contains("Manifest.permission.CAMERA"))
        assertTrue(mainActivity.contains("PermissionRequest.RESOURCE_AUDIO_CAPTURE"))
        assertTrue(mainActivity.contains("Manifest.permission.RECORD_AUDIO"))
        assertTrue(mainActivity.contains("SitePermission.CAMERA"))
        assertTrue(mainActivity.contains("SitePermission.MICROPHONE"))
        assertTrue(mainActivity.contains("SessionSitePermissionStore"))
        assertTrue(mainActivity.contains("private val sessionSitePermissionStore = SessionSitePermissionStore()"))
        assertTrue(mainActivity.contains("settingsManager.sitePermissionDecision(hostName, permission)"))
        assertTrue(mainActivity.contains("settingsManager.setSitePermissionDecision(hostName, permission, decision)"))
        assertTrue(mainActivity.contains("sessionSitePermissionStore.isAllowed(hostName, permission)"))
        assertTrue(mainActivity.contains("allowWebPermissionForSession(request)"))
        assertTrue(mainActivity.contains("SitePermissionDecision.ALLOW -> grantSupportedWebPermissionResources(request)"))
        assertTrue(mainActivity.contains("SitePermissionDecision.BLOCK -> request.deny()"))
        assertTrue(mainActivity.contains("showWebPermissionPrompt(request)"))
        assertTrue(mainActivity.contains("private fun grantSupportedWebPermissionResources(request: PermissionRequest)"))
        assertTrue(mainActivity.contains("private fun supportedWebPermissionResources(resources: Array<String>): Array<String>?"))
        assertTrue(mainActivity.contains("request.grant(resources)"))
        assertTrue(mainActivity.contains("R.string.title_web_permission_request"))
        assertTrue(mainActivity.contains("R.string.dialog_web_permission_request_message"))
        assertTrue(mainActivity.contains("R.string.action_allow"))
        assertTrue(mainActivity.contains("R.string.action_allow_once"))
        assertTrue(mainActivity.contains("rememberDecision = false"))
        assertTrue(mainActivity.contains("R.string.action_deny"))
        assertFalse(mainActivity.contains("request.grant(request.resources)"))
        assertTrue(mainActivity.contains("request.deny()"))
        assertTrue(mainActivity.contains("permissionRequested = ::handleWebPermissionRequest"))
        assertTrue(mainActivity.contains("permissionRequestCanceled = ::handleWebPermissionRequestCanceled"))
        assertTrue(mainActivity.contains("if (isPrivateBrowsingEnabled())"))
        assertTrue(mainActivity.contains("sessionSitePermissionStore.clear()"))
        assertTrue(mainActivity.contains("return permissions.takeIf { it.isNotEmpty() }"))
        assertTrue(strings.contains("title_web_permission_request"))
        assertTrue(strings.contains("dialog_web_permission_request_message"))
        assertTrue(strings.contains("action_allow_once"))
        assertTrue(strings.contains("web_permission_camera"))
        assertTrue(strings.contains("web_permission_microphone"))
        assertTrue(readme().contains("只会授予相机和麦克风资源，未知网页权限会被拒绝"))
        assertTrue(readme().contains("仅本次允许"))
        assertTrue(readme().contains("不会写入持久记录"))
    }

    private fun readme(): String {
        return projectFile("README.md").readText()
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
        return listOfNotNull(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path"),
            workingDirectory.parentFile?.let { parent -> File(parent, path) }
        ).first { it.exists() }
    }

    private companion object {
        private const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"
    }
}
