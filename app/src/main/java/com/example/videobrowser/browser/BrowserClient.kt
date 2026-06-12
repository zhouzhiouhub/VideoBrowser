package com.example.videobrowser.browser

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
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        pageStarted(url)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        pageFinished(url)
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val webRequest = request ?: return null
        return requestIntercepted(BrowserRequest.from(webRequest))
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun shouldInterceptRequest(view: WebView?, url: String?): WebResourceResponse? {
        // 旧版重载只暴露 URL，无法可靠判断是否为主文档请求。
        val request = BrowserRequest.from(url) ?: return null
        return requestIntercepted(request)
    }

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        val webRequest = request ?: return false
        return urlLoadingRequested(view, webRequest.url, webRequest.isForMainFrame)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        val uri = url?.let(Uri::parse) ?: return false
        return urlLoadingRequested(view, uri, true)
    }

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

    override fun onReceivedHttpAuthRequest(
        view: WebView?,
        handler: HttpAuthHandler?,
        host: String?,
        realm: String?
    ) {
        httpAuthRequested(view, handler, host, realm)
    }

    override fun onReceivedClientCertRequest(
        view: WebView?,
        request: ClientCertRequest?
    ) {
        clientCertRequested(view, request)
    }

    @TargetApi(Build.VERSION_CODES.O)
    override fun onRenderProcessGone(
        view: WebView?,
        detail: RenderProcessGoneDetail?
    ): Boolean {
        return renderProcessGone(view, detail?.didCrash() == true)
    }

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
