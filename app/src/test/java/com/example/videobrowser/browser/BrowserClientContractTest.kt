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

    @Test
    fun browserClientRoutesClientCertificateRequestsToSystemSelector() {
        val browserClient = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserClient.kt"
        ).readText()
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(browserClient.contains("import android.webkit.ClientCertRequest"))
        assertTrue(browserClient.contains("clientCertRequested: (WebView?, ClientCertRequest?) -> Unit"))
        assertTrue(browserClient.contains("request?.cancel()"))
        assertTrue(browserClient.contains("override fun onReceivedClientCertRequest"))
        assertTrue(browserClient.contains("clientCertRequested(view, request)"))
        assertTrue(mainActivity.contains("import android.security.KeyChain"))
        assertTrue(mainActivity.contains("import android.webkit.ClientCertRequest"))
        assertTrue(mainActivity.contains("clientCertRequested = ::handleClientCertRequest"))
        assertTrue(mainActivity.contains("private fun handleClientCertRequest("))
        assertTrue(mainActivity.contains("KeyChain.choosePrivateKeyAlias("))
        assertTrue(mainActivity.contains("KeyChain.getPrivateKey(appContext, alias)"))
        assertTrue(mainActivity.contains("KeyChain.getCertificateChain(appContext, alias)"))
        assertTrue(mainActivity.contains("request.proceed(credential.privateKey, credential.certificateChain)"))
        assertTrue(mainActivity.contains("private fun cancelPendingClientCertRequest()"))
        assertTrue(mainActivity.contains("cancelPendingClientCertRequest()"))
        assertTrue(strings.contains("toast_client_certificate_unavailable"))
        assertTrue(readme.contains("Android 系统证书选择器"))
    }

    @Test
    fun browserClientRoutesRenderProcessGoneToActivityRecovery() {
        val browserClient = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserClient.kt"
        ).readText()
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val sessionCoordinator = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserSessionCoordinator.kt"
        ).readText()
        val tabRegistry = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserTabWebViewRegistry.kt"
        ).readText()
        val errorPage = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserErrorPage.kt"
        ).readText()
        val readme = projectFile("README.md").readText()

        assertTrue(browserClient.contains("import android.webkit.RenderProcessGoneDetail"))
        assertTrue(browserClient.contains("renderProcessGone: (WebView?, Boolean) -> Boolean"))
        assertTrue(browserClient.contains("override fun onRenderProcessGone"))
        assertTrue(browserClient.contains("detail?.didCrash() == true"))
        assertTrue(browserClient.contains("return renderProcessGone(view, detail?.didCrash() == true)"))
        assertTrue(mainActivity.contains("renderProcessGone = ::handleRenderProcessGone"))
        assertTrue(mainActivity.contains("private fun handleRenderProcessGone(view: WebView?, didCrash: Boolean): Boolean"))
        assertTrue(mainActivity.contains("BrowserPageError.RenderProcessGone"))
        assertTrue(mainActivity.contains("standardTabWebViews.replaceView(tabId, replacementWebView)"))
        assertTrue(mainActivity.contains("browserSessionCoordinator.replacePrivateWebView()"))
        assertTrue(mainActivity.contains("private fun disposeGoneWebView(goneWebView: WebView)"))
        assertTrue(sessionCoordinator.contains("fun replacePrivateWebView(): WebView?"))
        assertTrue(sessionCoordinator.contains("detachCurrent = false"))
        assertTrue(tabRegistry.contains("data class ReplaceResult"))
        assertTrue(tabRegistry.contains("fun replaceView(tabId: Long, replacementView: T): ReplaceResult<T>?"))
        assertTrue(errorPage.contains("data class RenderProcessGone"))
        assertTrue(readme.contains("网页渲染进程退出时自动恢复"))
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
