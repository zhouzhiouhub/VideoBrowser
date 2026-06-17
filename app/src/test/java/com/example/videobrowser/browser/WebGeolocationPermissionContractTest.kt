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
    /**
     * 测试函数 `manifestDeclaresRuntimePermissionsForWebGeolocation`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `manifest Declares Runtime Permissions For Web Geolocation` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `browserManagerEnablesWebViewGeolocation`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `browser Manager Enables Web View Geolocation` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun browserManagerEnablesWebViewGeolocation() {
        val browserManager = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserManager.kt"
        ).readText()

        assertTrue(browserManager.contains("setGeolocationEnabled(true)"))
    }

    /**
     * 测试函数 `chromeClientForwardsWebGeolocationPermissionPrompts`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `chrome Client Forwards Web Geolocation Permission Prompts` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `mainActivityMapsWebGeolocationPromptsThroughRuntimePermissions`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `main Activity Maps Web Geolocation Prompts Through Runtime Permissions` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun mainActivityMapsWebGeolocationPromptsThroughRuntimePermissions() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val geolocationController = projectFile(
            "src/main/java/com/example/videobrowser/browser/GeolocationPermissionController.kt"
        ).readText()
        val privateBrowsingSwitchController = projectFile(
            "src/main/java/com/example/videobrowser/browser/PrivateBrowsingSwitchController.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(mainActivity.contains("geolocationPermissionController.handleAndroidPermissionResult(grants)"))
        assertTrue(mainActivity.contains("private lateinit var geolocationPermissionController: GeolocationPermissionController"))
        assertTrue(geolocationController.contains("pendingPermissionPrompt"))
        assertTrue(geolocationController.contains("pendingSitePrompt"))
        assertTrue(geolocationController.contains("Manifest.permission.ACCESS_FINE_LOCATION"))
        assertTrue(geolocationController.contains("Manifest.permission.ACCESS_COARSE_LOCATION"))
        assertTrue(geolocationController.contains("SitePermission.LOCATION"))
        assertTrue(mainActivity.contains("SessionSitePermissionStore"))
        assertTrue(mainActivity.contains("private val sessionSitePermissionStore = SessionSitePermissionStore()"))
        assertTrue(geolocationController.contains("settingsManager.sitePermissionDecision(hostName, SitePermission.LOCATION)"))
        assertTrue(geolocationController.contains("settingsManager.setSitePermissionDecision("))
        assertTrue(geolocationController.contains("sessionSitePermissionStore.isAllowed(hostName, SitePermission.LOCATION)"))
        assertTrue(geolocationController.contains("allowGeolocationPermissionForSession(prompt.origin)"))
        assertTrue(geolocationController.contains("SitePermissionDecision.ALLOW -> prompt.callback.invoke(prompt.origin, true, false)"))
        assertTrue(geolocationController.contains("SitePermissionDecision.BLOCK -> denyPermissionPrompt"))
        assertTrue(mainActivity.contains("geolocationPermissionRequested = ::handleGeolocationPermissionRequest"))
        assertTrue(mainActivity.contains("geolocationPermissionHidden = ::handleGeolocationPermissionHidden"))
        assertTrue(geolocationController.contains("showPermissionPrompt"))
        assertTrue(geolocationController.contains("R.string.title_geolocation_permission_request"))
        assertTrue(geolocationController.contains("R.string.dialog_geolocation_permission_request_message"))
        assertTrue(geolocationController.contains("R.string.action_allow"))
        assertTrue(geolocationController.contains("R.string.action_allow_once"))
        assertTrue(geolocationController.contains("rememberDecision = false"))
        assertTrue(geolocationController.contains("R.string.action_deny"))
        assertTrue(geolocationController.contains("prompt.callback.invoke(prompt.origin, allowed, false)"))
        assertTrue(geolocationController.contains("callback.invoke(origin, false, false)"))
        assertTrue(mainActivity.contains("cancelPendingGeolocationPermissionPrompt()"))
        assertTrue(geolocationController.contains("if (isPrivateBrowsingEnabled())"))
        assertTrue(privateBrowsingSwitchController.contains("sessionSitePermissionStore.clear()"))
        assertTrue(strings.contains("title_geolocation_permission_request"))
        assertTrue(strings.contains("dialog_geolocation_permission_request_message"))
        assertTrue(strings.contains("action_allow_once"))
        assertTrue(strings.contains("permission_origin_unknown"))
        assertTrue(readme().contains("仅本次允许"))
        assertTrue(readme().contains("不会写入持久记录"))
    }

    /**
     * 测试函数 `readme`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `readme` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun readme(): String {
        return projectFile("README.md").readText()
    }

    /**
     * 测试函数 `manifest`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `manifest` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun manifest(): Element {
        return DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
        }.newDocumentBuilder()
            .parse(projectFile("src/main/AndroidManifest.xml"))
            .documentElement
    }

    /**
     * 测试函数 `elements`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `elements` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param tagName 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun Element.elements(tagName: String): List<Element> {
        val nodes = getElementsByTagName(tagName)
        return List(nodes.length) { index -> nodes.item(index) }
            .filterIsInstance<Element>()
    }

    /**
     * 测试函数 `androidAttribute`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `android Attribute` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param name 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun Element.androidAttribute(name: String): String {
        return getAttributeNS(ANDROID_NAMESPACE, name)
    }

    /**
     * 测试函数 `projectFile`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `project File` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param path 参数类型为 `String`，表示函数执行 `path` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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
