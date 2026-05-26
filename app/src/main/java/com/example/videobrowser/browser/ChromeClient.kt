package com.example.videobrowser.browser

import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebChromeClient.CustomViewCallback
import android.webkit.WebView
import android.widget.FrameLayout

class ChromeClient(
    private val fullscreenContainer: FrameLayout,
    private val decorView: View,
    private val progressChanged: (Int) -> Unit = {},
    private val titleReceived: (String) -> Unit = {}
) : WebChromeClient() {
    private var customView: View? = null
    private var customViewCallback: CustomViewCallback? = null
    private var previousSystemUiVisibility = 0

    fun isShowingCustomView(): Boolean {
        return customView != null
    }

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        progressChanged(newProgress)
    }

    override fun onReceivedTitle(view: WebView?, title: String?) {
        titleReceived(title.orEmpty())
    }

    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
        if (view == null) {
            callback?.onCustomViewHidden()
            return
        }
        if (customView != null) {
            callback?.onCustomViewHidden()
            return
        }

        customView = view
        customViewCallback = callback
        previousSystemUiVisibility = decorView.systemUiVisibility
        fullscreenContainer.addView(
            view,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        fullscreenContainer.visibility = View.VISIBLE
        decorView.systemUiVisibility =
            previousSystemUiVisibility or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }

    override fun onHideCustomView() {
        hideCustomView()
    }

    fun hideCustomView() {
        val view = customView ?: return
        val callback = customViewCallback
        customView = null
        customViewCallback = null

        fullscreenContainer.removeView(view)
        fullscreenContainer.visibility = View.GONE
        decorView.systemUiVisibility = previousSystemUiVisibility
        previousSystemUiVisibility = 0
        callback?.onCustomViewHidden()
    }
}
