package com.example.videobrowser.browser

import com.example.videobrowser.testutil.projectFile

/**
 * 测试阅读提示：
 * 这个测试文件验证“External Protocol Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExternalProtocolWiringContractTest {
    /**
     * 测试函数 `mainActivityRoutesExternalSchemesDirectlyToExternalNavigator`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `main Activity Routes External Schemes Directly To External Navigator` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun mainActivityRoutesExternalSchemesDirectlyToExternalNavigator() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val coreFeatureAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserCoreFeatureAssemblyController.kt"
        ).readText()
        val navigationController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserNavigationController.kt"
        ).readText()
        val browserClientAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserClientAssemblyController.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(mainActivity.contains("private lateinit var browserFeatures: BrowserActivityFeatureComponents"))
        assertTrue(coreFeatureAssembly.contains("browserNavigationController = browserNavigation.browserNavigationController"))
        assertTrue(browserClientAssembly.contains("shouldBlockUrl = browserNavigationController::shouldBlockUrl"))
        assertTrue(navigationController.contains("private fun openExternalProtocolNavigation"))
        assertTrue(navigationController.contains("ExternalProtocolPolicy.shouldOpenExternally(uri.scheme)"))
        assertTrue(navigationController.contains("view?.stopLoading()"))
        assertTrue(navigationController.contains("externalNavigator.openExternalProtocolUrl(uri.toString())"))
        assertTrue(navigationController.contains("MediaRouteAction.BLOCK ->"))
        assertTrue(navigationController.contains("openExternalProtocolNavigation(view, uri)"))
        assertFalse(mainActivity.contains("showExternalProtocolConfirmation"))
        assertFalse(mainActivity.contains("openConfirmedExternalProtocol"))
        assertFalse(strings.contains("title_external_protocol_request"))
        assertFalse(strings.contains("dialog_external_protocol_request_message"))
        assertFalse(strings.contains("action_open_external_app"))
    }

    /**
     * 测试函数 `externalNavigatorHandlesIntentUrisWithBrowserFallback`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `external Navigator Handles Intent Uris With Browser Fallback` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun externalNavigatorHandlesIntentUrisWithBrowserFallback() {
        val navigator = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserExternalNavigator.kt"
        ).readText()

        assertTrue(navigator.contains("fun openExternalProtocolUrl"))
        assertTrue(navigator.contains("Intent.parseUri(url, Intent.URI_INTENT_SCHEME)"))
        assertTrue(navigator.contains("ExternalProtocolPolicy.BROWSER_FALLBACK_URL"))
        assertTrue(navigator.contains("loadFallbackUrl(fallbackUrl)"))
        assertTrue(navigator.contains("R.string.toast_external_app_blocked"))
        assertFalse(navigator.contains("Intent.CATEGORY_BROWSABLE"))
        assertFalse(navigator.contains("startExternalIntent"))
        assertFalse(navigator.contains("Intent(Intent.ACTION_VIEW"))
    }

    /**
     * 测试函数 `externalNavigatorDoesNotProvideGenericExternalBrowserLaunch`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `external Navigator Does Not Provide Generic External Browser Launch` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun externalNavigatorDoesNotProvideGenericExternalBrowserLaunch() {
        val navigator = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserExternalNavigator.kt"
        ).readText()
        val readme = projectFile("README.md").readText()

        assertFalse(navigator.contains("fun openExternalUrl"))
        assertFalse(navigator.contains("Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply"))
        assertFalse(navigator.contains("addCategory(Intent.CATEGORY_BROWSABLE)"))
        assertTrue(readme.contains("不启动其他应用"))
    }

}
