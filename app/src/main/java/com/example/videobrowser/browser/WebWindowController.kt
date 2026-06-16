package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“网页新窗口模块”。
 * WebView 遇到 window.open 或网页请求关闭窗口时，会通过 ChromeClient 回调到这里。
 * 主要职责：只允许用户手势触发的新窗口，把它们接入标准标签页，并响应网页关闭弹窗标签页的请求。
 * 阅读顺序：先看 handleCreateWebWindow，再看 handleCloseWebWindow。
 */
import android.os.Message
import android.webkit.WebView

/**
 * WebView 新窗口和关闭窗口请求控制器。
 *
 * MainActivity 只负责把 ChromeClient 的窗口回调委托给本类；本类负责把网页弹窗映射到标准标签页。
 *
 * @param isPrivateBrowsingActive 参数类型为 `() -> Boolean`，表示当前是否处于无痕模式的回调；无痕模式会拒绝网页新窗口。
 * @param standardTabStore 参数类型为 `BrowserTabStore`，表示标准模式标签页数据源，用来创建新标签页记录。
 * @param standardTabWebViews 参数类型为 `BrowserTabWebViewRegistry<WebView>`，表示标准标签页和 WebView 的映射表。
 * @param standardSessionController 参数类型为 `BrowserSessionController`，表示标准会话控制器，用来恢复新标签页元数据。
 * @param closeFunctionCenter 参数类型为 `() -> Boolean`，表示关闭功能中心面板的回调，避免弹窗新标签页被面板遮挡。
 * @param saveStandardTabSession 参数类型为 `() -> Unit`，表示持久化标准标签页会话的回调。
 * @param closeTab 参数类型为 `(Long) -> Unit`，表示根据标签页 ID 关闭标准标签页的回调。
 */
class WebWindowController(
    private val isPrivateBrowsingActive: () -> Boolean,
    private val standardTabStore: BrowserTabStore,
    private val standardTabWebViews: BrowserTabWebViewRegistry<WebView>,
    private val standardSessionController: BrowserSessionController,
    private val closeFunctionCenter: () -> Boolean,
    private val saveStandardTabSession: () -> Unit,
    private val closeTab: (Long) -> Unit
) {
    /**
     * 函数 `handleCreateWebWindow`：把用户手势触发的网页新窗口接入标准标签页。
     *
     * 初学者阅读提示：WebView.WebViewTransport 是 Android 让应用提供“新窗口 WebView”的容器。
     *
     * @param view 参数类型为 `WebView?`，表示发起新窗口请求的源 WebView，本函数只保留签名以匹配 ChromeClient 回调。
     * @param isDialog 参数类型为 `Boolean`，表示网页是否请求对话框式窗口，本应用统一接入普通标签页。
     * @param isUserGesture 参数类型为 `Boolean`，表示请求是否来自用户手势；非用户手势的新窗口会被拒绝。
     * @param resultMsg 参数类型为 `Message?`，表示 WebView 等待应用填入新窗口 WebView 的消息对象。
     * @return 返回 `Boolean`，表示是否接受并完成新窗口请求。
     */
    @Suppress("UNUSED_PARAMETER")
    fun handleCreateWebWindow(
        view: WebView?,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message?
    ): Boolean {
        if (isPrivateBrowsingActive() || !isUserGesture) {
            return false
        }
        val transport = resultMsg?.obj as? WebView.WebViewTransport ?: return false
        closeFunctionCenter()
        val tab = standardTabStore.openTab()
        val tabWebView = standardTabWebViews.activate(tab.id)
        standardSessionController.restorePageMetadata(tab.url, tab.title)
        saveStandardTabSession()
        transport.webView = tabWebView
        resultMsg.sendToTarget()
        return true
    }

    /**
     * 函数 `handleCloseWebWindow`：响应网页请求关闭弹窗标签页的回调。
     *
     * 初学者阅读提示：WebView 只能给出要关闭的窗口实例，所以要先反查它对应的标准标签页 ID。
     *
     * @param window 参数类型为 `WebView?`，表示网页请求关闭的弹窗 WebView；为空时直接忽略。
     */
    fun handleCloseWebWindow(window: WebView?) {
        if (isPrivateBrowsingActive() || window == null) {
            return
        }
        val tabId = standardTabWebViews.tabIdFor(window) ?: return
        closeTab(tabId)
    }
}
