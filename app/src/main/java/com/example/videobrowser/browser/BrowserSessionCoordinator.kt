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

    /**
     * 函数 `setStandardWebView`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param webView 参数类型为 `WebView`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     */
    fun setStandardWebView(webView: WebView) {
        standardWebView = webView
    }

    /**
     * 函数 `enterPrivate`：封装 `enter Private` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `exitPrivate`：封装 `exit Private` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 函数 `destroyPrivateSession`：封装 `destroy Private Session` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 函数 `replacePrivateWebView`：封装 `replace Private Web View` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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
