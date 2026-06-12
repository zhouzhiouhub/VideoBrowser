package com.example.videobrowser.browser

import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

class BrowserSessionCoordinator(
    private val activity: AppCompatActivity,
    private val webViewContainer: FrameLayout,
    private var standardWebView: WebView,
    private val browserManager: BrowserManager,
    private val onActiveWebViewChanged: (WebView, BrowserMode) -> Unit
) {
    private var privateWebView: WebView? = null

    var mode = BrowserMode.STANDARD
        private set

    val activeWebView: WebView
        get() = privateWebView?.takeIf { mode == BrowserMode.PRIVATE } ?: standardWebView

    val isPrivate: Boolean
        get() = mode == BrowserMode.PRIVATE

    fun setStandardWebView(webView: WebView) {
        standardWebView = webView
    }

    fun enterPrivate(): Boolean {
        if (isPrivate) {
            return true
        }

        return runCatching {
            val temporaryWebView = WebView(activity).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                overScrollMode = standardWebView.overScrollMode
                setBackgroundColor(0x00000000)
            }
            privateWebView = temporaryWebView
            standardWebView.visibility = View.GONE
            webViewContainer.addView(temporaryWebView)
            mode = BrowserMode.PRIVATE
            browserManager.switchWebView(
                nextWebView = temporaryWebView,
                privateBrowsingEnabled = true
            )
            onActiveWebViewChanged(temporaryWebView, BrowserMode.PRIVATE)
        }.isSuccess.also { success ->
            if (!success) {
                privateWebView?.let { webViewContainer.removeView(it) }
                privateWebView = null
                standardWebView.visibility = View.VISIBLE
                mode = BrowserMode.STANDARD
                browserManager.switchWebView(
                    nextWebView = standardWebView,
                    privateBrowsingEnabled = false,
                    detachCurrent = false
                )
                onActiveWebViewChanged(standardWebView, BrowserMode.STANDARD)
            }
        }
    }

    fun exitPrivate() {
        val temporaryWebView = privateWebView ?: run {
            mode = BrowserMode.STANDARD
            browserManager.switchWebView(
                nextWebView = standardWebView,
                privateBrowsingEnabled = false,
                detachCurrent = false
            )
            onActiveWebViewChanged(standardWebView, BrowserMode.STANDARD)
            return
        }

        browserManager.clearTransientBrowsingData()
        webViewContainer.removeView(temporaryWebView)
        browserManager.destroyWebView(temporaryWebView, clearSharedStores = false)
        privateWebView = null
        standardWebView.visibility = View.VISIBLE
        mode = BrowserMode.STANDARD
        browserManager.switchWebView(
            nextWebView = standardWebView,
            privateBrowsingEnabled = false,
            detachCurrent = false
        )
        onActiveWebViewChanged(standardWebView, BrowserMode.STANDARD)
    }

    fun destroyPrivateSession() {
        if (isPrivate) {
            exitPrivate()
            return
        }

        privateWebView?.let { temporaryWebView ->
            webViewContainer.removeView(temporaryWebView)
            browserManager.destroyWebView(temporaryWebView, clearSharedStores = false)
            privateWebView = null
        }
    }
}
