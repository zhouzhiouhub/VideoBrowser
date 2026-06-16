package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Web Geolocation Permission Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
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
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(mainActivity.contains("pendingGeolocationPermissionPrompt"))
        assertTrue(mainActivity.contains("pendingGeolocationSitePrompt"))
        assertTrue(mainActivity.contains("Manifest.permission.ACCESS_FINE_LOCATION"))
        assertTrue(mainActivity.contains("Manifest.permission.ACCESS_COARSE_LOCATION"))
        assertTrue(mainActivity.contains("SitePermission.LOCATION"))
        assertTrue(mainActivity.contains("SessionSitePermissionStore"))
        assertTrue(mainActivity.contains("private val sessionSitePermissionStore = SessionSitePermissionStore()"))
        assertTrue(mainActivity.contains("settingsManager.sitePermissionDecision(hostName, SitePermission.LOCATION)"))
        assertTrue(mainActivity.contains("settingsManager.setSitePermissionDecision("))
        assertTrue(mainActivity.contains("sessionSitePermissionStore.isAllowed(hostName, SitePermission.LOCATION)"))
        assertTrue(mainActivity.contains("allowGeolocationPermissionForSession(prompt.origin)"))
        assertTrue(mainActivity.contains("SitePermissionDecision.ALLOW -> prompt.callback.invoke(prompt.origin, true, false)"))
        assertTrue(mainActivity.contains("SitePermissionDecision.BLOCK -> denyGeolocationPermissionPrompt"))
        assertTrue(mainActivity.contains("geolocationPermissionRequested = ::handleGeolocationPermissionRequest"))
        assertTrue(mainActivity.contains("geolocationPermissionHidden = ::handleGeolocationPermissionHidden"))
        assertTrue(mainActivity.contains("showGeolocationPermissionPrompt"))
        assertTrue(mainActivity.contains("R.string.title_geolocation_permission_request"))
        assertTrue(mainActivity.contains("R.string.dialog_geolocation_permission_request_message"))
        assertTrue(mainActivity.contains("R.string.action_allow"))
        assertTrue(mainActivity.contains("R.string.action_allow_once"))
        assertTrue(mainActivity.contains("rememberDecision = false"))
        assertTrue(mainActivity.contains("R.string.action_deny"))
        assertTrue(mainActivity.contains("prompt.callback.invoke(prompt.origin, allowed, false)"))
        assertTrue(mainActivity.contains("callback.invoke(origin, false, false)"))
        assertTrue(mainActivity.contains("cancelPendingGeolocationPermissionPrompt()"))
        assertTrue(mainActivity.contains("if (isPrivateBrowsingEnabled())"))
        assertTrue(mainActivity.contains("sessionSitePermissionStore.clear()"))
        assertTrue(strings.contains("title_geolocation_permission_request"))
        assertTrue(strings.contains("dialog_geolocation_permission_request_message"))
        assertTrue(strings.contains("action_allow_once"))
        assertTrue(strings.contains("permission_origin_unknown"))
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
