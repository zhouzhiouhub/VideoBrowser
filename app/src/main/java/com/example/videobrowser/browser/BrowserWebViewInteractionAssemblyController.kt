package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器 WebView 交互装配模块”。
 * 文件名 BrowserWebViewInteractionAssemblyController 可以拆开理解为“Browser WebView Interaction Assembly Controller”，
 * 表示它只负责创建 WebView 长按菜单控制器和 active WebView 状态同步控制器。
 * 阅读顺序：先看 BrowserWebViewInteractionComponents 知道返回哪些对象，再看 create() 中两个控制器如何共享回调。
 */
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity

/**
 * WebView 交互组件集合。
 *
 * @param linkContextMenuController 参数类型为 `LinkContextMenuController`，表示负责 WebView 链接和图片长按菜单的控制器。
 * @param browserActiveWebViewController 参数类型为 `BrowserActiveWebViewController`，表示负责 active WebView 变化后同步浏览器外壳状态的控制器。
 */
data class BrowserWebViewInteractionComponents(
    val linkContextMenuController: LinkContextMenuController,
    val browserActiveWebViewController: BrowserActiveWebViewController
)

/**
 * 浏览器 WebView 交互装配控制器。
 *
 * WebView 长按菜单需要在每个新 WebView 创建和 active WebView 切换时重新绑定；active WebView 切换又需要
 * 同步滚动、ChromeClient、无痕主题和会话 UI。本类把这两类 WebView 交互 wiring 集中起来。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示创建长按菜单和系统分享面板的宿主 Activity。
 * @param setPrivateBrowsingActive 参数类型为 `(Boolean) -> Unit`，表示写入当前是否处于无痕模式的回调。
 * @param openUrlInNewTab 参数类型为 `(String) -> Unit`，表示用户从长按菜单选择新标签打开时调用的回调。
 * @param downloadUrl 参数类型为 `(String, String?) -> Unit`，表示用户从长按菜单选择下载时调用的回调，第一个参数是 URL，第二个参数是当前 User-Agent。
 * @param currentUserAgent 参数类型为 `() -> String?`，表示读取当前 active WebView User-Agent 的函数，当前 WebView 不可用时可返回 null。
 * @param isShareableUrl 参数类型为 `(String) -> Boolean`，表示判断链接或图片 URL 是否可用于菜单动作的函数。
 * @param attachBrowserControlsScrollIfReady 参数类型为 `(WebView) -> Unit`，表示把工具栏滚动隐藏逻辑绑定到 active WebView 的回调。
 * @param syncCurrentChromeClientIfReady 参数类型为 `() -> Unit`，表示在 ChromeClient 已创建时重新绑定当前 ChromeClient 的回调。
 * @param updatePrivateBrowsingUi 参数类型为 `() -> Unit`，表示刷新无痕浏览 UI 状态的回调。
 * @param syncSearchProviderVisibility 参数类型为 `() -> Unit`，表示按当前页面状态刷新搜索入口可见性的回调。
 * @param applyBrowsingModeTheme 参数类型为 `() -> Unit`，表示按普通/无痕模式重新应用主题的回调。
 * @param areBrowserSessionsInitialized 参数类型为 `() -> Boolean`，表示判断标准和无痕会话控制器是否都已经初始化的函数。
 * @param currentSessionController 参数类型为 `() -> BrowserSessionController`，表示返回当前模式会话控制器的函数。
 */
class BrowserWebViewInteractionAssemblyController(
    private val activity: AppCompatActivity,
    private val setPrivateBrowsingActive: (Boolean) -> Unit,
    private val openUrlInNewTab: (String) -> Unit,
    private val downloadUrl: (String, String?) -> Unit,
    private val currentUserAgent: () -> String?,
    private val isShareableUrl: (String) -> Boolean,
    private val attachBrowserControlsScrollIfReady: (WebView) -> Unit,
    private val syncCurrentChromeClientIfReady: () -> Unit,
    private val updatePrivateBrowsingUi: () -> Unit,
    private val syncSearchProviderVisibility: () -> Unit,
    private val applyBrowsingModeTheme: () -> Unit,
    private val areBrowserSessionsInitialized: () -> Boolean,
    private val currentSessionController: () -> BrowserSessionController
) {
    /**
     * 创建 WebView 交互组件集合。
     *
     * @return 返回 `BrowserWebViewInteractionComponents`，调用方把其中对象保存到对应字段后继续创建 WebView 宿主控制器。
     */
    fun create(): BrowserWebViewInteractionComponents {
        val linkContextMenuController = LinkContextMenuController(
            activity = activity,
            openUrlInNewTab = openUrlInNewTab,
            downloadUrl = downloadUrl,
            currentUserAgent = currentUserAgent,
            isShareableUrl = isShareableUrl
        )
        val browserActiveWebViewController = BrowserActiveWebViewController(
            setPrivateBrowsingActive = setPrivateBrowsingActive,
            configureLinkContextMenu = linkContextMenuController::configure,
            attachBrowserControlsScrollIfReady = attachBrowserControlsScrollIfReady,
            syncCurrentChromeClientIfReady = syncCurrentChromeClientIfReady,
            updatePrivateBrowsingUi = updatePrivateBrowsingUi,
            syncSearchProviderVisibility = syncSearchProviderVisibility,
            applyBrowsingModeTheme = applyBrowsingModeTheme,
            areBrowserSessionsInitialized = areBrowserSessionsInitialized,
            currentSessionController = currentSessionController
        )
        return BrowserWebViewInteractionComponents(
            linkContextMenuController = linkContextMenuController,
            browserActiveWebViewController = browserActiveWebViewController
        )
    }
}
