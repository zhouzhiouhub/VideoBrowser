package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 BrowserSessionCoordinator 可以拆开理解为“Browser Session Coordinator”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：封装 WebView 页面加载、标签页、导航安全、页面工具、权限回调或浏览器控件状态。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
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

    fun replacePrivateWebView(): WebView? {
        if (!isPrivate) {
            return null
        }
        val previousWebView = privateWebView ?: return null
        val replacementWebView = WebView(activity).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            overScrollMode = standardWebView.overScrollMode
            setBackgroundColor(0x00000000)
        }
        privateWebView = replacementWebView
        webViewContainer.addView(replacementWebView)
        browserManager.switchWebView(
            nextWebView = replacementWebView,
            privateBrowsingEnabled = true,
            detachCurrent = false
        )
        onActiveWebViewChanged(replacementWebView, BrowserMode.PRIVATE)
        return previousWebView
    }
}
