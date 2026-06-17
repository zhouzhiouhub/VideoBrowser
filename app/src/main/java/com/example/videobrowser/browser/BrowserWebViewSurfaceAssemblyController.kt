package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器 WebView surface 装配模块”。
 * 文件名 BrowserWebViewSurfaceAssemblyController 可以拆开理解为“Browser WebView Surface Assembly Controller”，
 * 表示它只负责把 WebView 交互控制器和标准 WebView 宿主控制器组合成一个浏览器内容 surface。
 * 阅读顺序：先看 BrowserWebViewSurfaceComponents 知道返回哪些对象，再看 create() 中为什么先创建交互组件、再创建 WebView 宿主。
 */
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.MainActivityViews

/**
 * 浏览器 WebView surface 组件集合。
 *
 * @param webViewInteraction 参数类型为 `BrowserWebViewInteractionComponents`，表示 WebView 长按菜单和 active WebView 状态同步组件。
 * @param browserStandardWebViewHostController 参数类型为 `BrowserStandardWebViewHostController`，表示标准模式 WebView 宿主控制器。
 */
data class BrowserWebViewSurfaceComponents(
    val webViewInteraction: BrowserWebViewInteractionComponents,
    val browserStandardWebViewHostController: BrowserStandardWebViewHostController
)

/**
 * 浏览器 WebView surface 装配控制器。
 *
 * WebView surface 由两部分组成：交互组件负责长按菜单和 active WebView 状态同步，
 * 标准 WebView 宿主负责创建、展示和销毁标准标签页 WebView。本类把二者的初始化顺序收拢起来，
 * 让 MainActivity 只保存最终 surface 组件。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示创建长按菜单、新 WebView 和系统分享面板的宿主 Activity。
 * @param views 参数类型为 `MainActivityViews`，表示读取 WebView 容器和初始 WebView 的视图绑定集合。
 * @param standardTabStore 参数类型为 `BrowserTabStore`，表示标准模式标签页数据源。
 * @param setPrivateBrowsingActive 参数类型为 `(Boolean) -> Unit`，表示 active WebView 切换时写入无痕模式状态的回调。
 * @param openUrlInNewTab 参数类型为 `(String) -> Unit`，表示长按菜单选择新标签打开链接时调用的回调。
 * @param downloadUrl 参数类型为 `(String, String?) -> Unit`，表示长按菜单选择下载时调用的回调，第一个参数是下载 URL，第二个参数是当前 User-Agent。
 * @param isShareableUrl 参数类型为 `(String) -> Boolean`，表示判断链接或图片 URL 是否可用于菜单动作的函数。
 * @param attachBrowserControlsScrollIfReady 参数类型为 `(WebView) -> Unit`，表示把工具栏滚动隐藏逻辑绑定到 active WebView 的回调。
 * @param syncCurrentChromeClientIfReady 参数类型为 `() -> Unit`，表示 ChromeClient 已创建时同步当前 ChromeClient 的回调。
 * @param updatePrivateBrowsingUi 参数类型为 `() -> Unit`，表示刷新无痕浏览 UI 的回调。
 * @param syncSearchProviderVisibility 参数类型为 `() -> Unit`，表示刷新首页搜索入口可见性的回调。
 * @param applyBrowsingModeTheme 参数类型为 `() -> Unit`，表示重新应用普通/无痕浏览主题的回调。
 * @param areBrowserSessionsInitialized 参数类型为 `() -> Boolean`，表示判断标准和无痕会话控制器是否完成初始化的函数。
 * @param currentSessionController 参数类型为 `() -> BrowserSessionController`，表示读取当前浏览模式会话控制器的函数。
 */
class BrowserWebViewSurfaceAssemblyController(
    private val activity: AppCompatActivity,
    private val views: MainActivityViews,
    private val standardTabStore: BrowserTabStore,
    private val setPrivateBrowsingActive: (Boolean) -> Unit,
    private val openUrlInNewTab: (String) -> Unit,
    private val downloadUrl: (String, String?) -> Unit,
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
     * 创建浏览器 WebView surface 组件集合。
     *
     * @return 返回 `BrowserWebViewSurfaceComponents`，其中标准 WebView 宿主已经执行 setup()，可直接供导航、会话和客户端装配使用。
     */
    fun create(): BrowserWebViewSurfaceComponents {
        lateinit var browserStandardWebViewHostController: BrowserStandardWebViewHostController
        val webViewInteraction = BrowserWebViewInteractionAssemblyController(
            activity = activity,
            setPrivateBrowsingActive = setPrivateBrowsingActive,
            openUrlInNewTab = openUrlInNewTab,
            downloadUrl = downloadUrl,
            currentUserAgent = {
                browserStandardWebViewHostController.currentBrowserManager().userAgentString()
            },
            isShareableUrl = isShareableUrl,
            attachBrowserControlsScrollIfReady = attachBrowserControlsScrollIfReady,
            syncCurrentChromeClientIfReady = syncCurrentChromeClientIfReady,
            updatePrivateBrowsingUi = updatePrivateBrowsingUi,
            syncSearchProviderVisibility = syncSearchProviderVisibility,
            applyBrowsingModeTheme = applyBrowsingModeTheme,
            areBrowserSessionsInitialized = areBrowserSessionsInitialized,
            currentSessionController = currentSessionController
        ).create()
        browserStandardWebViewHostController = BrowserStandardWebViewHostAssemblyController(
            activity = activity,
            views = views,
            standardTabStore = standardTabStore,
            configureLinkContextMenu = webViewInteraction.linkContextMenuController::configure,
            handleActiveWebViewChanged =
                webViewInteraction.browserActiveWebViewController::handleActiveWebViewChanged
        ).create()
        browserStandardWebViewHostController.setup()
        return BrowserWebViewSurfaceComponents(
            webViewInteraction = webViewInteraction,
            browserStandardWebViewHostController = browserStandardWebViewHostController
        )
    }
}
