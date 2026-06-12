package com.example.videobrowser.browser

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class ExternalProtocolWiringContractTest {
    @Test
    fun mainActivityRoutesBlockedExternalSchemesToExternalNavigator() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(mainActivity.contains("private fun openExternalProtocolNavigation"))
        assertTrue(mainActivity.contains("ExternalProtocolPolicy.shouldOpenExternally(uri.scheme)"))
        assertTrue(mainActivity.contains("showExternalProtocolConfirmation(uri)"))
        assertTrue(mainActivity.contains("private fun openConfirmedExternalProtocol"))
        assertTrue(mainActivity.contains("externalNavigator.openExternalProtocolUrl(uri.toString())"))
        assertTrue(mainActivity.contains("R.string.title_external_protocol_request"))
        assertTrue(mainActivity.contains("R.string.dialog_external_protocol_request_message"))
        assertTrue(mainActivity.contains("R.string.action_open_external_app"))
        assertTrue(mainActivity.contains("MediaRouteAction.BLOCK ->"))
        assertTrue(mainActivity.contains("openExternalProtocolNavigation(view, uri)"))
        assertTrue(strings.contains("title_external_protocol_request"))
        assertTrue(strings.contains("dialog_external_protocol_request_message"))
        assertTrue(strings.contains("action_open_external_app"))
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
        assertTrue(navigator.contains("Intent.CATEGORY_BROWSABLE"))
        assertTrue(navigator.contains("parsedIntent.component = null"))
        assertTrue(navigator.contains("parsedIntent.setSelector(null)"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
