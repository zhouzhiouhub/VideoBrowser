package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Browser Client Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserClientContractTest {
    /**
     * 测试函数 `browserClientRoutesMainFrameLoadFailuresToErrorCallback`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `browser Client Routes Main Frame Load Failures To Error Callback` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `browserClientCancelsSslErrorsBeforeShowingErrorPage`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `browser Client Cancels Ssl Errors Before Showing Error Page` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun browserClientCancelsSslErrorsBeforeShowingErrorPage() {
        val browserClient = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserClient.kt"
        ).readText()

        assertTrue(browserClient.contains("override fun onReceivedSslError"))
        assertTrue(browserClient.contains("handler?.cancel()"))
        assertTrue(browserClient.contains("BrowserPageError.Ssl"))
    }

    /**
     * 测试函数 `browserClientBacksToSafetyWhenSafeBrowsingReportsThreats`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `browser Client Backs To Safety When Safe Browsing Reports Threats` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `browserClientRoutesHttpAuthRequestsToActivityPrompt`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `browser Client Routes Http Auth Requests To Activity Prompt` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun browserClientRoutesHttpAuthRequestsToActivityPrompt() {
        val browserClient = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserClient.kt"
        ).readText()
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val runtimeFeatureAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserRuntimeFeatureAssemblyController.kt"
        ).readText()
        val lifecycleController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserActivityLifecycleController.kt"
        ).readText()
        val browserWebClientController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserWebClientController.kt"
        ).readText()
        val httpAuthController = projectFile(
            "src/main/java/com/example/videobrowser/browser/HttpAuthController.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(browserClient.contains("import android.webkit.HttpAuthHandler"))
        assertTrue(browserClient.contains("httpAuthRequested: (WebView?, HttpAuthHandler?, String?, String?) -> Unit"))
        assertTrue(browserClient.contains("handler?.cancel()"))
        assertTrue(browserClient.contains("override fun onReceivedHttpAuthRequest"))
        assertTrue(browserClient.contains("httpAuthRequested(view, handler, host, realm)"))
        assertTrue(mainActivity.contains("private lateinit var browserFeatures: BrowserActivityFeatureComponents"))
        assertTrue(runtimeFeatureAssembly.contains("httpAuthController = pageActions.httpAuthController"))
        assertTrue(httpAuthController.contains("private var pendingHandler: HttpAuthHandler?"))
        assertTrue(httpAuthController.contains("private var pendingDialog: AlertDialog?"))
        assertTrue(browserWebClientController.contains("httpAuthRequested = { _, handler, host, realm ->"))
        assertTrue(browserWebClientController.contains("httpAuthController.handleRequest(handler, host, realm)"))
        assertTrue(mainActivity.contains("browserActivityLifecycleController.handleDestroy()"))
        assertTrue(lifecycleController.contains("browserWebClientController()?.cancelPendingHttpAuthRequest()"))
        assertTrue(browserWebClientController.contains("fun cancelPendingHttpAuthRequest()"))
        assertTrue(httpAuthController.contains("pendingDialog = dialog"))
        assertTrue(httpAuthController.contains("dialog?.setOnDismissListener(null)"))
        assertTrue(httpAuthController.contains("R.string.title_http_auth_request"))
        assertTrue(httpAuthController.contains("R.string.hint_http_auth_username"))
        assertTrue(httpAuthController.contains("R.string.hint_http_auth_password"))
        assertTrue(httpAuthController.contains("authHandler.proceed("))
        assertTrue(httpAuthController.contains("authHandler.cancel()"))
        assertTrue(strings.contains("title_http_auth_request"))
        assertTrue(strings.contains("dialog_http_auth_request_message"))
        assertTrue(strings.contains("hint_http_auth_username"))
        assertTrue(strings.contains("hint_http_auth_password"))
        assertTrue(readme.contains("HTTP Basic Auth"))
        assertTrue(readme.contains("HTTP Basic Auth 只保留一个待处理认证弹窗"))
    }

    /**
     * 测试函数 `browserClientRoutesClientCertificateRequestsToSystemSelector`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `browser Client Routes Client Certificate Requests To System Selector` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun browserClientRoutesClientCertificateRequestsToSystemSelector() {
        val browserClient = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserClient.kt"
        ).readText()
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val runtimeFeatureAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserRuntimeFeatureAssemblyController.kt"
        ).readText()
        val lifecycleController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserActivityLifecycleController.kt"
        ).readText()
        val browserWebClientController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserWebClientController.kt"
        ).readText()
        val clientCertificateController = projectFile(
            "src/main/java/com/example/videobrowser/browser/ClientCertificateController.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(browserClient.contains("import android.webkit.ClientCertRequest"))
        assertTrue(browserClient.contains("clientCertRequested: (WebView?, ClientCertRequest?) -> Unit"))
        assertTrue(browserClient.contains("request?.cancel()"))
        assertTrue(browserClient.contains("override fun onReceivedClientCertRequest"))
        assertTrue(browserClient.contains("clientCertRequested(view, request)"))
        assertTrue(mainActivity.contains("private lateinit var browserFeatures: BrowserActivityFeatureComponents"))
        assertTrue(runtimeFeatureAssembly.contains("clientCertificateController = pageActions.clientCertificateController"))
        assertTrue(
            browserWebClientController.contains(
                "clientCertRequested = { _, request -> clientCertificateController.handleRequest(request) }"
            )
        )
        assertTrue(clientCertificateController.contains("private var pendingRequest: ClientCertRequest?"))
        assertTrue(clientCertificateController.contains("KeyChain.choosePrivateKeyAlias("))
        assertTrue(clientCertificateController.contains("KeyChain.getPrivateKey(appContext, alias)"))
        assertTrue(clientCertificateController.contains("KeyChain.getCertificateChain(appContext, alias)"))
        assertTrue(clientCertificateController.contains("request.proceed(credential.privateKey, credential.certificateChain)"))
        assertTrue(clientCertificateController.contains("request.cancel()"))
        assertTrue(browserWebClientController.contains("fun cancelPendingClientCertRequest()"))
        assertTrue(mainActivity.contains("browserActivityLifecycleController.handleDestroy()"))
        assertTrue(lifecycleController.contains("browserWebClientController()?.cancelPendingClientCertRequest()"))
        assertTrue(clientCertificateController.contains("R.string.toast_client_certificate_unavailable"))
        assertTrue(strings.contains("toast_client_certificate_unavailable"))
        assertTrue(readme.contains("Android 系统证书选择器"))
    }

    /**
     * 测试函数 `browserClientRoutesRenderProcessGoneToActivityRecovery`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `browser Client Routes Render Process Gone To Activity Recovery` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun browserClientRoutesRenderProcessGoneToActivityRecovery() {
        val browserClient = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserClient.kt"
        ).readText()
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val runtimeFeatureAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserRuntimeFeatureAssemblyController.kt"
        ).readText()
        val browserWebClientController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserWebClientController.kt"
        ).readText()
        val renderProcessRecoveryController = projectFile(
            "src/main/java/com/example/videobrowser/browser/RenderProcessRecoveryController.kt"
        ).readText()
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
        assertTrue(mainActivity.contains("private lateinit var browserFeatures: BrowserActivityFeatureComponents"))
        assertTrue(browserWebClientController.contains("renderProcessGone = renderProcessRecoveryController::handleRenderProcessGone"))
        assertTrue(browserWebClientController.contains("fun showBrowserErrorPage(error: BrowserPageError)"))
        assertTrue(runtimeFeatureAssembly.contains("browserClients.browserWebClientController.showBrowserErrorPage(error)"))
        assertTrue(renderProcessRecoveryController.contains("BrowserPageError.RenderProcessGone"))
        assertTrue(renderProcessRecoveryController.contains("standardTabWebViews.replaceView(tabId, replacementWebView)"))
        assertTrue(renderProcessRecoveryController.contains("sessionCoordinator.replacePrivateWebView()"))
        assertTrue(renderProcessRecoveryController.contains("private fun disposeGoneWebView(goneWebView: WebView)"))
        assertTrue(renderProcessRecoveryController.contains("goneWebView.webViewClient = WebViewClient()"))
        assertTrue(sessionCoordinator.contains("fun replacePrivateWebView(): WebView?"))
        assertTrue(sessionCoordinator.contains("detachCurrent = false"))
        assertTrue(tabRegistry.contains("data class ReplaceResult"))
        assertTrue(tabRegistry.contains("fun replaceView(tabId: Long, replacementView: T): ReplaceResult<T>?"))
        assertTrue(errorPage.contains("data class RenderProcessGone"))
        assertTrue(readme.contains("网页渲染进程退出时自动恢复"))
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
}
