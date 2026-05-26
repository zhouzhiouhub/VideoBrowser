package com.example.videobrowser.browser

import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebChromeClient.CustomViewCallback
import android.webkit.WebView

class ChromeClient(
    private val progressChanged: (Int) -> Unit = {},
    private val titleReceived: (String) -> Unit = {},
    private val showCustomView: (View?, CustomViewCallback?) -> Unit = { _, callback ->
        callback?.onCustomViewHidden()
    },
    private val hideCustomView: () -> Unit = {}
) : WebChromeClient() {

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        progressChanged(newProgress)
    }

    override fun onReceivedTitle(view: WebView?, title: String?) {
        titleReceived(title.orEmpty())
    }

    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
        showCustomView(view, callback)
    }

    override fun onHideCustomView() {
        hideCustomView()
    }
}
