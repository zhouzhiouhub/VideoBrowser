package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“WebView 渲染进程恢复模块”。
 * Android WebView 的渲染进程崩溃或被系统回收时，会回调到这里。
 * 主要职责：替换出问题的 WebView、销毁旧实例、保存标签页会话，并显示一页可恢复的错误页。
 * 阅读顺序：先看 handleRenderProcessGone，再看 handlePrivateRenderProcessGone 和 handleStandardRenderProcessGone。
 */
import android.view.ViewGroup
import android.webkit.WebView

/**
 * WebView 渲染进程退出恢复控制器。
 *
 * MainActivity 只负责把 BrowserClient 的 onRenderProcessGone 回调委托给本类；本类负责恢复流程的分支和 WebView 清理。
 *
 * @param webViewContainer 参数类型为 `ViewGroup`，表示承载 WebView 的父容器，用来从界面树中移除崩溃的 WebView。
 * @param sessionCoordinator 参数类型为 `BrowserSessionCoordinator`，表示标准/无痕 WebView 的切换协调器。
 * @param standardTabWebViews 参数类型为 `BrowserTabWebViewRegistry<WebView>`，表示标准模式标签页和 WebView 的映射表。
 * @param currentPageUrl 参数类型为 `() -> String?`，表示读取当前页面 URL 的回调，用来生成错误页说明。
 * @param isPrivateBrowsingActive 参数类型为 `() -> Boolean`，表示当前是否处于无痕模式的回调。
 * @param createStandardTabWebView 参数类型为 `() -> WebView`，表示为标准标签页创建新 WebView 的回调。
 * @param showStandardTabWebView 参数类型为 `(WebView, Boolean) -> Unit`，表示把标准标签页 WebView 切到前台的回调。
 * @param saveStandardTabSession 参数类型为 `() -> Unit`，表示持久化标准标签页会话的回调。
 * @param showBrowserErrorPage 参数类型为 `(BrowserPageError) -> Unit`，表示把恢复失败信息显示成浏览器错误页的回调。
 */
class RenderProcessRecoveryController(
    private val webViewContainer: ViewGroup,
    private val sessionCoordinator: BrowserSessionCoordinator,
    private val standardTabWebViews: BrowserTabWebViewRegistry<WebView>,
    private val currentPageUrl: () -> String?,
    private val isPrivateBrowsingActive: () -> Boolean,
    private val createStandardTabWebView: () -> WebView,
    private val showStandardTabWebView: (WebView, Boolean) -> Unit,
    private val saveStandardTabSession: () -> Unit,
    private val showBrowserErrorPage: (BrowserPageError) -> Unit
) {
    /**
     * 函数 `handleRenderProcessGone`：恢复渲染进程退出后的 WebView 状态。
     *
     * 初学者阅读提示：返回 true 表示应用已经处理该事件，WebView 不需要再继续默认处理。
     *
     * @param view 参数类型为 `WebView?`，表示渲染进程已经退出的 WebView；为空时视为已处理。
     * @param didCrash 参数类型为 `Boolean`，表示渲染进程是否因为崩溃退出，用于错误页文案。
     * @return 返回 `Boolean`，固定为 true，表示该事件已经被应用接管。
     */
    fun handleRenderProcessGone(view: WebView?, didCrash: Boolean): Boolean {
        val goneWebView = view ?: return true
        val pageUrl = currentPageUrl() ?: goneWebView.url

        if (isPrivateBrowsingActive() && sessionCoordinator.activeWebView === goneWebView) {
            handlePrivateRenderProcessGone(
                goneWebView = goneWebView,
                pageUrl = pageUrl,
                didCrash = didCrash
            )
            return true
        }

        if (handleStandardRenderProcessGone(goneWebView, pageUrl, didCrash)) {
            return true
        }

        disposeGoneWebView(goneWebView)
        return true
    }

    /**
     * 函数 `handlePrivateRenderProcessGone`：恢复无痕模式中退出的 WebView。
     *
     * 初学者阅读提示：无痕模式只有一个临时 WebView，所以直接让 sessionCoordinator 替换当前无痕 WebView。
     *
     * @param goneWebView 参数类型为 `WebView`，表示已经退出的无痕 WebView。
     * @param pageUrl 参数类型为 `String?`，表示退出时页面 URL，用来生成错误页。
     * @param didCrash 参数类型为 `Boolean`，表示渲染进程是否因为崩溃退出。
     */
    private fun handlePrivateRenderProcessGone(
        goneWebView: WebView,
        pageUrl: String?,
        didCrash: Boolean
    ) {
        val previousWebView = sessionCoordinator.replacePrivateWebView()
        if (previousWebView != null) {
            disposeGoneWebView(previousWebView)
            showRenderProcessErrorPage(pageUrl, didCrash)
        } else {
            disposeGoneWebView(goneWebView)
        }
    }

    /**
     * 函数 `handleStandardRenderProcessGone`：恢复标准模式中退出的标签页 WebView。
     *
     * 初学者阅读提示：标准模式可能有多个标签页，所以要先根据崩溃 WebView 找到对应 tabId，再替换该标签页的 WebView。
     *
     * @param goneWebView 参数类型为 `WebView`，表示已经退出的标准标签页 WebView。
     * @param pageUrl 参数类型为 `String?`，表示退出时页面 URL，用来生成错误页。
     * @param didCrash 参数类型为 `Boolean`，表示渲染进程是否因为崩溃退出。
     * @return 返回 `Boolean`，表示该 WebView 是否属于标准标签页并已被恢复。
     */
    private fun handleStandardRenderProcessGone(
        goneWebView: WebView,
        pageUrl: String?,
        didCrash: Boolean
    ): Boolean {
        val tabId = standardTabWebViews.tabIdFor(goneWebView) ?: return false
        val replacementWebView = createStandardTabWebView()
        val result = standardTabWebViews.replaceView(tabId, replacementWebView)
        if (result != null && result.replacedActiveView && !isPrivateBrowsingActive()) {
            showStandardTabWebView(replacementWebView, false)
            showRenderProcessErrorPage(pageUrl, didCrash)
        }
        disposeGoneWebView(goneWebView)
        saveStandardTabSession()
        return true
    }

    /**
     * 函数 `showRenderProcessErrorPage`：显示渲染进程退出对应的浏览器错误页。
     *
     * @param pageUrl 参数类型为 `String?`，表示退出时页面 URL，可为空。
     * @param didCrash 参数类型为 `Boolean`，表示渲染进程是否因为崩溃退出。
     */
    private fun showRenderProcessErrorPage(pageUrl: String?, didCrash: Boolean) {
        showBrowserErrorPage(
            BrowserPageError.RenderProcessGone(
                url = pageUrl,
                didCrash = didCrash
            )
        )
    }

    /**
     * 函数 `disposeGoneWebView`：把崩溃的 WebView 从界面树中移除并销毁。
     *
     * 初学者阅读提示：先清空 WebChromeClient、WebViewClient 和下载监听，避免旧 WebView 销毁后继续持有回调对象。
     *
     * @param goneWebView 参数类型为 `WebView`，表示需要清理的崩溃 WebView。
     */
    private fun disposeGoneWebView(goneWebView: WebView) {
        if (goneWebView.parent == webViewContainer) {
            webViewContainer.removeView(goneWebView)
        } else {
            (goneWebView.parent as? ViewGroup)?.removeView(goneWebView)
        }
        BrowserWebViewCallbackCleaner.detachCallbacks(goneWebView)
        goneWebView.removeAllViews()
        goneWebView.destroy()
    }
}
