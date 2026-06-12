package com.example.videobrowser.browser

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserClientContractTest {
    @Test
    fun browserClientRoutesMainFrameLoadFailuresToErrorCallback() {
        val browserClient = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserClient.kt"
        ).readText()

        assertTrue(browserClient.contains("pageLoadFailed: (BrowserPageError) -> Unit"))
        assertTrue(browserClient.contains("override fun onReceivedError"))
        assertTrue(browserClient.contains("request?.isForMainFrame != true"))
        assertTrue(browserClient.contains("BrowserPageError.Network"))
        assertTrue(browserClient.contains("override fun onReceivedHttpError"))
        assertTrue(browserClient.contains("BrowserPageError.Http"))
    }

    @Test
    fun browserClientCancelsSslErrorsBeforeShowingErrorPage() {
        val browserClient = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserClient.kt"
        ).readText()

        assertTrue(browserClient.contains("override fun onReceivedSslError"))
        assertTrue(browserClient.contains("handler?.cancel()"))
        assertTrue(browserClient.contains("BrowserPageError.Ssl"))
    }

    @Test
    fun browserClientBacksToSafetyWhenSafeBrowsingReportsThreats() {
        val browserClient = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserClient.kt"
        ).readText()
        val errorPage = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserErrorPage.kt"
        ).readText()
        val readme = projectFile("README.md").readText()

        assertTrue(browserClient.contains("import android.webkit.SafeBrowsingResponse"))
        assertTrue(browserClient.contains("override fun onSafeBrowsingHit"))
        assertTrue(browserClient.contains("callback?.backToSafety(true)"))
        assertTrue(browserClient.contains("request?.isForMainFrame != true"))
        assertTrue(browserClient.contains("BrowserPageError.SafeBrowsing"))
        assertTrue(browserClient.contains("safeBrowsingThreatDescription(threatType)"))
        assertTrue(errorPage.contains("data class SafeBrowsing"))
        assertTrue(errorPage.contains("error is BrowserPageError.SafeBrowsing"))
        assertTrue(readme.contains("命中风险页面时退回安全页"))
    }

    @Test
    fun browserClientRoutesHttpAuthRequestsToActivityPrompt() {
        val browserClient = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserClient.kt"
        ).readText()
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(browserClient.contains("import android.webkit.HttpAuthHandler"))
        assertTrue(browserClient.contains("httpAuthRequested: (WebView?, HttpAuthHandler?, String?, String?) -> Unit"))
        assertTrue(browserClient.contains("handler?.cancel()"))
        assertTrue(browserClient.contains("override fun onReceivedHttpAuthRequest"))
        assertTrue(browserClient.contains("httpAuthRequested(view, handler, host, realm)"))
        assertTrue(mainActivity.contains("httpAuthRequested = ::handleHttpAuthRequest"))
        assertTrue(mainActivity.contains("private fun handleHttpAuthRequest("))
        assertTrue(mainActivity.contains("R.string.title_http_auth_request"))
        assertTrue(mainActivity.contains("R.string.hint_http_auth_username"))
        assertTrue(mainActivity.contains("R.string.hint_http_auth_password"))
        assertTrue(mainActivity.contains("authHandler.proceed("))
        assertTrue(mainActivity.contains("authHandler.cancel()"))
        assertTrue(strings.contains("title_http_auth_request"))
        assertTrue(strings.contains("dialog_http_auth_request_message"))
        assertTrue(strings.contains("hint_http_auth_username"))
        assertTrue(strings.contains("hint_http_auth_password"))
        assertTrue(readme.contains("HTTP Basic Auth"))
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
