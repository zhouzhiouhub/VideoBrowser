package com.example.videobrowser.browser

import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient

class BrowserClient(
    private val pageStarted: (String?) -> Unit = {},
    private val pageFinished: (String?) -> Unit = {},
    private val requestIntercepted: (Uri, Boolean) -> WebResourceResponse? = { _, _ -> null },
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
        return requestIntercepted(webRequest.url, webRequest.isForMainFrame)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun shouldInterceptRequest(view: WebView?, url: String?): WebResourceResponse? {
        val uri = url?.let(Uri::parse) ?: return null
        return requestIntercepted(uri, false)
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
}
