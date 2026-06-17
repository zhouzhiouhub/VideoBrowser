package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器 WebView 状态同步模块”。
 * 文件名 BrowserActiveWebViewController 可以拆开理解为“Browser Active WebView Controller”，
 * 表示它只负责当前活动 WebView 变化后需要同步的外壳状态。
 * 阅读顺序：先看构造参数了解它会通知哪些模块，再看 handleActiveWebViewChanged() 的同步顺序。
 */
import android.webkit.WebView

/**
 * 当前活动 WebView 控制器。
 *
 * BrowserSessionCoordinator 负责决定哪个 WebView 变成 active；本类负责在 active WebView
 * 变化后同步无痕状态、链接菜单、滚动隐藏、ChromeClient、搜索入口、主题和会话渲染。
 *
 * @param setPrivateBrowsingActive 写入当前是否处于无痕模式的函数，参数 true 表示切到无痕 WebView。
 * @param configureLinkContextMenu 为当前 active WebView 绑定长按链接菜单的函数，参数是新的 active WebView。
 * @param attachBrowserControlsScrollIfReady 把浏览器控制栏滚动隐藏逻辑绑定到 active WebView 的函数；尚未初始化时调用方可选择空操作。
 * @param syncCurrentChromeClientIfReady 把当前 ChromeClient 重新绑定到 active WebView 的函数；尚未初始化时调用方可选择空操作。
 * @param updatePrivateBrowsingUi 刷新无痕浏览 UI 状态的函数，例如无痕按钮选中态。
 * @param syncSearchProviderVisibility 按当前页面状态刷新搜索入口可见性的函数。
 * @param applyBrowsingModeTheme 按普通/无痕模式重新应用主题颜色的函数。
 * @param areBrowserSessionsInitialized 判断标准和无痕会话控制器是否都已初始化的函数。
 * @param currentSessionController 返回当前模式会话控制器的函数，用于在可用时重新渲染当前会话状态。
 */
class BrowserActiveWebViewController(
    private val setPrivateBrowsingActive: (Boolean) -> Unit,
    private val configureLinkContextMenu: (WebView) -> Unit,
    private val attachBrowserControlsScrollIfReady: (WebView) -> Unit,
    private val syncCurrentChromeClientIfReady: () -> Unit,
    private val updatePrivateBrowsingUi: () -> Unit,
    private val syncSearchProviderVisibility: () -> Unit,
    private val applyBrowsingModeTheme: () -> Unit,
    private val areBrowserSessionsInitialized: () -> Boolean,
    private val currentSessionController: () -> BrowserSessionController
) {
    /**
     * 同步新的 active WebView 关联的浏览器外壳状态。
     *
     * @param activeWebView 刚切到前台并接收浏览器回调的 WebView。
     * @param mode activeWebView 所属的浏览模式，STANDARD 表示普通浏览，PRIVATE 表示无痕浏览。
     * @return 无返回值；函数会把当前模式、菜单、ChromeClient、搜索入口、主题和会话 UI 同步到最新 WebView。
     */
    fun handleActiveWebViewChanged(activeWebView: WebView, mode: BrowserMode) {
        setPrivateBrowsingActive(mode == BrowserMode.PRIVATE)
        configureLinkContextMenu(activeWebView)
        attachBrowserControlsScrollIfReady(activeWebView)
        syncCurrentChromeClientIfReady()
        updatePrivateBrowsingUi()
        syncSearchProviderVisibility()
        applyBrowsingModeTheme()
        if (areBrowserSessionsInitialized()) {
            currentSessionController().renderCurrentState()
        }
    }
}
