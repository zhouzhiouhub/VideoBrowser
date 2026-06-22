package com.example.videobrowser.browser

import android.webkit.DownloadListener
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器 WebView 绑定模块”。
 * 文件名 BrowserWebViewBindingController 可以拆开理解为“Browser WebView Binding Controller”，表示它集中保存并重挂 WebView 外部绑定。
 * 主要职责：管理 ChromeClient、WebViewClient、下载监听器和 JavascriptInterface，确保标签页切换后新 WebView 继续拥有同一组绑定。
 * 阅读顺序：先看 setChromeClient/setBrowserClient/setDownloadListener，再看 addJavascriptInterface 和 attachToCurrentWebView。
 */
internal class BrowserWebViewBindingController(
    private val activeWebView: () -> WebView
) {
    private data class JavascriptInterfaceBinding(
        val interfaceObject: Any,
        val name: String
    )

    private val javascriptInterfaces = mutableListOf<JavascriptInterfaceBinding>()
    private var chromeClient: WebChromeClient? = null
    private var browserClient: WebViewClient? = null
    private var downloadListener: DownloadListener? = null

    fun setChromeClient(client: WebChromeClient?) {
        chromeClient = client
        activeWebView().webChromeClient = client
    }

    fun setBrowserClient(client: WebViewClient) {
        browserClient = client
        activeWebView().webViewClient = client
    }

    fun setDownloadListener(listener: DownloadListener?) {
        downloadListener = listener
        activeWebView().setDownloadListener(listener)
    }

    fun addJavascriptInterface(interfaceObject: Any, name: String) {
        javascriptInterfaces.removeAll { it.name == name }
        javascriptInterfaces.add(JavascriptInterfaceBinding(interfaceObject, name))
        activeWebView().addJavascriptInterface(interfaceObject, name)
    }

    fun detachFrom(targetWebView: WebView) {
        BrowserWebViewCallbackCleaner.detachCallbacks(targetWebView)
    }

    fun attachToCurrentWebView() {
        val targetWebView = activeWebView()
        targetWebView.webChromeClient = chromeClient
        browserClient?.let { targetWebView.webViewClient = it }
        targetWebView.setDownloadListener(downloadListener)
        javascriptInterfaces.forEach { binding ->
            targetWebView.addJavascriptInterface(binding.interfaceObject, binding.name)
        }
    }
}
