package com.example.videobrowser.browser

import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
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
    private val urlLoadingRequested: (WebView?, Uri, Boolean) -> Boolean = { _, _, _ -> false }
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
}
