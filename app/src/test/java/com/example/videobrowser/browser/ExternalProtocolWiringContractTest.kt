package com.example.videobrowser.browser

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExternalProtocolWiringContractTest {
    @Test
    fun mainActivityRoutesExternalSchemesDirectlyToExternalNavigator() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(mainActivity.contains("private fun openExternalProtocolNavigation"))
        assertTrue(mainActivity.contains("ExternalProtocolPolicy.shouldOpenExternally(uri.scheme)"))
        assertTrue(mainActivity.contains("view?.stopLoading()"))
        assertTrue(mainActivity.contains("externalNavigator.openExternalProtocolUrl(uri.toString())"))
        assertTrue(mainActivity.contains("MediaRouteAction.BLOCK ->"))
        assertTrue(mainActivity.contains("openExternalProtocolNavigation(view, uri)"))
        assertFalse(mainActivity.contains("showExternalProtocolConfirmation"))
        assertFalse(mainActivity.contains("openConfirmedExternalProtocol"))
        assertFalse(strings.contains("title_external_protocol_request"))
        assertFalse(strings.contains("dialog_external_protocol_request_message"))
        assertFalse(strings.contains("action_open_external_app"))
    }

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

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOfNotNull(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path"),
            workingDirectory.parentFile?.let { parent -> File(parent, path) }
        ).first { it.exists() }
    }
}
