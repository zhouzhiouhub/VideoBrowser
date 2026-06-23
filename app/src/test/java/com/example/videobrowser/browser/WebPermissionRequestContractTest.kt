package com.example.videobrowser.browser

import com.example.videobrowser.testutil.projectFile

/**
 * 测试阅读提示：
 * 这个测试文件验证“Web Permission Request Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

class WebPermissionRequestContractTest {
    /**
     * 测试函数 `manifestDeclaresRuntimePermissionsForWebCapture`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `manifest Declares Runtime Permissions For Web Capture` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `chromeClientForwardsWebPermissionRequests`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `chrome Client Forwards Web Permission Requests` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `mainActivityMapsWebCaptureRequestsThroughRuntimePermissions`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `main Activity Maps Web Capture Requests Through Runtime Permissions` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun mainActivityMapsWebCaptureRequestsThroughRuntimePermissions() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val runtimeFeatureAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserRuntimeFeatureAssemblyController.kt"
        ).readText()
        val activityScaffoldAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserActivityScaffoldAssemblyController.kt"
        ).readText()
        val activityResultLaunchers = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserActivityResultLaunchers.kt"
        ).readText()
        val lifecycleController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserActivityLifecycleController.kt"
        ).readText()
        val chromeClientController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserChromeClientController.kt"
        ).readText()
        val webPermissionController = projectFile(
            "src/main/java/com/example/videobrowser/browser/WebPermissionRequestController.kt"
        ).readText()
        val webPermissionPromptController = projectFile(
            "src/main/java/com/example/videobrowser/browser/WebPermissionPromptController.kt"
        ).readText()
        val webPermissionResourceMapper = projectFile(
            "src/main/java/com/example/videobrowser/browser/WebPermissionResourceMapper.kt"
        ).readText()
        val webPermissionPendingRequestStore = projectFile(
            "src/main/java/com/example/videobrowser/browser/WebPermissionPendingRequestStore.kt"
        ).readText()
        val androidPermissionGate = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserAndroidPermissionGate.kt"
        ).readText()
        val sitePermissionDecisionController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserSitePermissionDecisionController.kt"
        ).readText()
        val webRequestAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserWebRequestAssemblyController.kt"
        ).readText()
        val privateBrowsingSwitchController = projectFile(
            "src/main/java/com/example/videobrowser/browser/PrivateBrowsingSwitchController.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(activityResultLaunchers.contains("ActivityResultContracts.RequestMultiplePermissions()"))
        assertTrue(activityResultLaunchers.contains("webPermissionRequestController()?.handleAndroidPermissionResult(grants)"))
        assertTrue(mainActivity.contains("private lateinit var browserFeatures: BrowserActivityFeatureComponents"))
        assertTrue(runtimeFeatureAssembly.contains("webPermissionRequestController = webRequests.webPermissionRequestController"))
        assertTrue(webRequestAssembly.contains("requestAndroidPermissions = activityResultLaunchers::requestWebPermissions"))
        assertTrue(webPermissionController.contains("WebPermissionPendingRequestStore()"))
        assertTrue(webPermissionController.contains("pendingRequestStore.replaceWith(request)"))
        assertTrue(webPermissionController.contains("takePendingRequest = pendingRequestStore::take"))
        assertTrue(webPermissionController.contains("pendingRequestStore.cancelPending()"))
        assertFalse(webPermissionController.contains("pendingWebPermissionRequest: PermissionRequest?"))
        assertTrue(webPermissionController.contains("BrowserAndroidPermissionGate("))
        assertTrue(webPermissionController.contains("BrowserAndroidPermissionResultPolicy.ALL_REQUIRED"))
        assertTrue(webPermissionController.contains("androidPermissionGate.continueOrRequest(request)"))
        assertTrue(webPermissionController.contains("androidPermissionGate.handleResult(grants)"))
        assertTrue(androidPermissionGate.contains("fun continueOrRequest(request: T)"))
        assertTrue(androidPermissionGate.contains("fun handleResult(grants: Map<String, Boolean>)"))
        assertTrue(webPermissionPendingRequestStore.contains("pendingWebPermissionRequest: PermissionRequest?"))
        assertTrue(webPermissionPendingRequestStore.contains("pendingWebPermissionRequest?.deny()"))
        assertTrue(webPermissionPromptController.contains("pendingWebPermissionPromptRequest: PermissionRequest?"))
        assertTrue(webPermissionController.contains("WebPermissionResourceMapper.androidPermissionsFor(request.resources)"))
        assertTrue(webPermissionResourceMapper.contains("PermissionRequest.RESOURCE_VIDEO_CAPTURE"))
        assertTrue(webPermissionResourceMapper.contains("Manifest.permission.CAMERA"))
        assertTrue(webPermissionResourceMapper.contains("PermissionRequest.RESOURCE_AUDIO_CAPTURE"))
        assertTrue(webPermissionResourceMapper.contains("Manifest.permission.RECORD_AUDIO"))
        assertTrue(webPermissionResourceMapper.contains("SitePermission.CAMERA"))
        assertTrue(webPermissionResourceMapper.contains("SitePermission.MICROPHONE"))
        assertTrue(webPermissionResourceMapper.contains("private data class WebPermissionResourceDefinition("))
        assertTrue(webPermissionResourceMapper.contains("private val resourceDefinitionsByResource"))
        assertFalse(webPermissionResourceMapper.contains("private fun sitePermissionFor"))
        assertEquals(
            1,
            Regex("PermissionRequest\\.RESOURCE_VIDEO_CAPTURE")
                .findAll(webPermissionResourceMapper)
                .count()
        )
        assertEquals(
            1,
            Regex("PermissionRequest\\.RESOURCE_AUDIO_CAPTURE")
                .findAll(webPermissionResourceMapper)
                .count()
        )
        assertTrue(activityScaffoldAssembly.contains("SessionSitePermissionStore"))
        assertTrue(activityScaffoldAssembly.contains("val sessionSitePermissionStore = SessionSitePermissionStore()"))
        assertTrue(sitePermissionDecisionController.contains("settingsManager.sitePermissionDecision(hostName, permission)"))
        assertTrue(sitePermissionDecisionController.contains("settingsManager.setSitePermissionDecision(hostName, permission, decision)"))
        assertTrue(sitePermissionDecisionController.contains("sessionSitePermissionStore.isAllowed(hostName, permission)"))
        assertTrue(webPermissionController.contains("allowForSession = ::allowWebPermissionForSession"))
        assertTrue(webPermissionPromptController.contains("allowForSession(request)"))
        assertTrue(webPermissionController.contains("SitePermissionDecision.ALLOW -> grantSupportedWebPermissionResources(request)"))
        assertTrue(webPermissionController.contains("SitePermissionDecision.BLOCK -> request.deny()"))
        assertTrue(webPermissionController.contains("SitePermissionDecision.ASK -> webPermissionPromptController.show(request)"))
        assertTrue(webPermissionController.contains("private fun grantSupportedWebPermissionResources(request: PermissionRequest)"))
        assertTrue(webPermissionController.contains("WebPermissionResourceMapper.supportedResources(request.resources)"))
        assertTrue(webPermissionResourceMapper.contains("fun supportedResources(resources: Array<String>): Array<String>?"))
        assertFalse(webPermissionController.contains("private fun supportedWebPermissionResources"))
        assertTrue(webPermissionController.contains("request.grant(resources)"))
        assertTrue(webPermissionPromptController.contains("R.string.title_web_permission_request"))
        assertTrue(webPermissionPromptController.contains("R.string.dialog_web_permission_request_message"))
        assertTrue(webPermissionPromptController.contains("R.string.action_allow"))
        assertTrue(webPermissionPromptController.contains("R.string.action_allow_once"))
        assertTrue(webPermissionPromptController.contains("rememberDecision = false"))
        assertTrue(webPermissionPromptController.contains("R.string.action_deny"))
        assertFalse(webPermissionController.contains("request.grant(request.resources)"))
        assertTrue(webPermissionController.contains("request.deny()"))
        assertTrue(webPermissionPromptController.contains("request.deny()"))
        assertTrue(
            chromeClientController.contains(
                "permissionRequested = webPermissionRequestController::handlePermissionRequest"
            )
        )
        assertTrue(
            chromeClientController.contains(
                "permissionRequestCanceled = webPermissionRequestController::handlePermissionRequestCanceled"
            )
        )
        assertTrue(mainActivity.contains("browserActivityLifecycleController.handleDestroy()"))
        assertTrue(lifecycleController.contains("browserChromeClientController()?.cancelPendingWebPermissionRequest()"))
        assertTrue(sitePermissionDecisionController.contains("if (isPrivateBrowsingEnabled())"))
        assertTrue(privateBrowsingSwitchController.contains("sessionSitePermissionStore.clear()"))
        assertTrue(webPermissionResourceMapper.contains("return androidPermissions.takeIf { it.isNotEmpty() }"))
        assertTrue(strings.contains("title_web_permission_request"))
        assertTrue(strings.contains("dialog_web_permission_request_message"))
        assertTrue(strings.contains("action_allow_once"))
        assertTrue(strings.contains("web_permission_camera"))
        assertTrue(strings.contains("web_permission_microphone"))
        assertTrue(readme().contains("只会授予相机和麦克风资源，未知网页权限会被拒绝"))
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



    private companion object {
        private const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"
    }
}
