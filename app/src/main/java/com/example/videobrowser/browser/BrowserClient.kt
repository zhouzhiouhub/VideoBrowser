package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 BrowserClient 可以拆开理解为“Browser Client”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：封装 WebView 页面加载、标签页、导航安全、页面工具、权限回调或浏览器控件状态。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
import android.annotation.TargetApi
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.webkit.ClientCertRequest
import android.webkit.HttpAuthHandler
import android.webkit.RenderProcessGoneDetail
import android.webkit.SafeBrowsingResponse
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebResourceError
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * WebViewClient 薄适配层，负责把 Android 回调转换成应用内浏览事件。
 */
class BrowserClient(
    private val pageStarted: (String?) -> Unit = {},
    private val pageCommitVisible: (String?) -> Unit = {},
    private val pageFinished: (String?) -> Unit = {},
    private val pageLoadFailed: (BrowserPageError) -> Unit = {},
    private val requestIntercepted: (BrowserRequest) -> WebResourceResponse? = { null },
    private val urlLoadingRequested: (WebView?, Uri, Boolean) -> Boolean = { _, _, _ -> false },
    private val clientCertRequested: (WebView?, ClientCertRequest?) -> Unit =
        { _, request -> request?.cancel() },
    private val renderProcessGone: (WebView?, Boolean) -> Boolean = { _, _ -> false },
    private val httpAuthRequested: (WebView?, HttpAuthHandler?, String?, String?) -> Unit =
        { _, handler, _, _ -> handler?.cancel() }
) : WebViewClient() {
    /**
     * 函数 `onPageStarted`：处理 `on Page Started` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param view 参数类型为 `WebView?`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param favicon 参数类型为 `Bitmap?`，表示函数执行 `favicon` 相关逻辑时需要读取或处理的输入。
     */
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        pageStarted(url)
    }

    /**
     * 函数 `onPageCommitVisible`：处理 `on Page Commit Visible` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：WebView 在首屏内容已经提交并可见时回调这里，早于完整页面结束加载。
     * @param view 参数类型为 `WebView?`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param url 参数类型为 `String?`，表示已经可见提交的页面地址，用来同步会话和进度状态。
     */
    override fun onPageCommitVisible(view: WebView?, url: String?) {
        pageCommitVisible(url)
    }

    /**
     * 函数 `onPageFinished`：处理 `on Page Finished` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param view 参数类型为 `WebView?`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     */
    override fun onPageFinished(view: WebView?, url: String?) {
        pageFinished(url)
    }

    /**
     * 函数 `shouldInterceptRequest`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param view 参数类型为 `WebView?`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param request 参数类型为 `WebResourceRequest?`，表示一次请求或响应，函数会检查它的内容并决定如何继续处理。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val webRequest = request ?: return null
        return requestIntercepted(BrowserRequest.from(webRequest))
    }

    /**
     * 函数 `shouldInterceptRequest`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param view 参数类型为 `WebView?`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    @Suppress("OVERRIDE_DEPRECATION")
    override fun shouldInterceptRequest(view: WebView?, url: String?): WebResourceResponse? {
        // 旧版重载只暴露 URL，无法可靠判断是否为主文档请求。
        val request = BrowserRequest.from(url) ?: return null
        return requestIntercepted(request)
    }

    /**
     * 函数 `shouldOverrideUrlLoading`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param view 参数类型为 `WebView?`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param request 参数类型为 `WebResourceRequest?`，表示一次请求或响应，函数会检查它的内容并决定如何继续处理。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        val webRequest = request ?: return false
        return urlLoadingRequested(view, webRequest.url, webRequest.isForMainFrame)
    }

    /**
     * 函数 `shouldOverrideUrlLoading`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param view 参数类型为 `WebView?`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    @Suppress("OVERRIDE_DEPRECATION")
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        val uri = url?.let(Uri::parse) ?: return false
        return urlLoadingRequested(view, uri, true)
    }

    /**
     * 函数 `onReceivedError`：处理 `on Received Error` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param view 参数类型为 `WebView?`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param request 参数类型为 `WebResourceRequest?`，表示一次请求或响应，函数会检查它的内容并决定如何继续处理。
     * @param error 参数类型为 `WebResourceError?`，表示函数执行 `error` 相关逻辑时需要读取或处理的输入。
     */
    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        if (request?.isForMainFrame != true) {
            return
        }
        pageLoadFailed(
            BrowserPageError.Network(
                url = request.url?.toString(),
                code = error?.errorCode ?: 0,
                description = error?.description?.toString().orEmpty()
                    .ifBlank { "网络错误" }
            )
        )
    }

    /**
     * 函数 `onReceivedHttpError`：处理 `on Received Http Error` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param view 参数类型为 `WebView?`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param request 参数类型为 `WebResourceRequest?`，表示一次请求或响应，函数会检查它的内容并决定如何继续处理。
     * @param errorResponse 参数类型为 `WebResourceResponse?`，表示一次请求或响应，函数会检查它的内容并决定如何继续处理。
     */
    override fun onReceivedHttpError(
        view: WebView?,
        request: WebResourceRequest?,
        errorResponse: WebResourceResponse?
    ) {
        if (request?.isForMainFrame != true) {
            return
        }
        pageLoadFailed(
            BrowserPageError.Http(
                url = request.url?.toString(),
                statusCode = errorResponse?.statusCode ?: 0,
                reasonPhrase = errorResponse?.reasonPhrase.orEmpty()
                    .ifBlank { "HTTP 错误" }
            )
        )
    }

    /**
     * 函数 `onReceivedSslError`：处理 `on Received Ssl Error` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param view 参数类型为 `WebView?`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param handler 参数类型为 `SslErrorHandler?`，表示回调对象，异步操作完成后用它把结果通知回调用方。
     * @param error 参数类型为 `SslError?`，表示函数执行 `error` 相关逻辑时需要读取或处理的输入。
     */
    override fun onReceivedSslError(
        view: WebView?,
        handler: SslErrorHandler?,
        error: SslError?
    ) {
        handler?.cancel()
        pageLoadFailed(
            BrowserPageError.Ssl(
                url = error?.url,
                description = error?.toString().orEmpty().ifBlank { "SSL 证书错误" }
            )
        )
    }

    /**
     * 函数 `onSafeBrowsingHit`：处理 `on Safe Browsing Hit` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param view 参数类型为 `WebView?`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param request 参数类型为 `WebResourceRequest?`，表示一次请求或响应，函数会检查它的内容并决定如何继续处理。
     * @param threatType 参数类型为 `Int`，表示函数执行 `threatType` 相关逻辑时需要读取或处理的输入。
     * @param callback 参数类型为 `SafeBrowsingResponse?`，表示回调对象，异步操作完成后用它把结果通知回调用方。
     */
    @TargetApi(Build.VERSION_CODES.O_MR1)
    override fun onSafeBrowsingHit(
        view: WebView?,
        request: WebResourceRequest?,
        threatType: Int,
        callback: SafeBrowsingResponse?
    ) {
        callback?.backToSafety(true)
        if (request?.isForMainFrame != true) {
            return
        }
        pageLoadFailed(
            BrowserPageError.SafeBrowsing(
                url = request.url?.toString(),
                threatType = threatType,
                description = safeBrowsingThreatDescription(threatType)
            )
        )
    }

    /**
     * 函数 `onReceivedHttpAuthRequest`：处理 `on Received Http Auth Request` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param view 参数类型为 `WebView?`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param handler 参数类型为 `HttpAuthHandler?`，表示回调对象，异步操作完成后用它把结果通知回调用方。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @param realm 参数类型为 `String?`，表示函数执行 `realm` 相关逻辑时需要读取或处理的输入。
     */
    override fun onReceivedHttpAuthRequest(
        view: WebView?,
        handler: HttpAuthHandler?,
        host: String?,
        realm: String?
    ) {
        httpAuthRequested(view, handler, host, realm)
    }

    /**
     * 函数 `onReceivedClientCertRequest`：处理 `on Received Client Cert Request` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param view 参数类型为 `WebView?`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param request 参数类型为 `ClientCertRequest?`，表示一次请求或响应，函数会检查它的内容并决定如何继续处理。
     */
    override fun onReceivedClientCertRequest(
        view: WebView?,
        request: ClientCertRequest?
    ) {
        clientCertRequested(view, request)
    }

    /**
     * 函数 `onRenderProcessGone`：处理 `on Render Process Gone` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param view 参数类型为 `WebView?`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param detail 参数类型为 `RenderProcessGoneDetail?`，表示函数执行 `detail` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    @TargetApi(Build.VERSION_CODES.O)
    override fun onRenderProcessGone(
        view: WebView?,
        detail: RenderProcessGoneDetail?
    ): Boolean {
        return renderProcessGone(view, detail?.didCrash() == true)
    }

    /**
     * 函数 `safeBrowsingThreatDescription`：封装 `safe Browsing Threat Description` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param threatType 参数类型为 `Int`，表示函数执行 `threatType` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun safeBrowsingThreatDescription(threatType: Int): String {
        return when (threatType) {
            SAFE_BROWSING_THREAT_MALWARE -> "Safe Browsing 已阻止恶意软件风险。"
            SAFE_BROWSING_THREAT_PHISHING -> "Safe Browsing 已阻止钓鱼网站风险。"
            SAFE_BROWSING_THREAT_UNWANTED_SOFTWARE -> "Safe Browsing 已阻止有害软件风险。"
            SAFE_BROWSING_THREAT_BILLING -> "Safe Browsing 已阻止可疑扣费页面。"
            else -> "Safe Browsing 已阻止此页面。"
        }
    }
}
