package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“标准 WebView 宿主装配模块”。
 * 文件名 BrowserStandardWebViewHostAssemblyController 可以拆开理解为“Browser Standard WebView Host Assembly Controller”，
 * 表示它只负责创建普通浏览模式下的 WebView 宿主控制器。
 * 阅读顺序：先看 create() 中如何把布局里的初始 WebView、标签页数据源和 active WebView 回调交给宿主控制器。
 */
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.MainActivityViews

/**
 * 标准 WebView 宿主装配控制器。
 *
 * BrowserStandardWebViewHostController 负责标准标签页 WebView 的创建、显示、隐藏、销毁和 active WebView 通知；
 * 本类只把 MainActivity 已经准备好的视图、标签页数据源和回调集中接入。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示创建新标准标签页 WebView 时使用的宿主 Activity。
 * @param views 参数类型为 `MainActivityViews`，表示读取 WebView 容器和布局初始 WebView 的视图绑定集合。
 * @param standardTabStore 参数类型为 `BrowserTabStore`，表示标准模式标签页数据源。
 * @param configureLinkContextMenu 参数类型为 `(WebView) -> Unit`，表示为指定 WebView 配置长按链接/图片菜单的回调。
 * @param handleActiveWebViewChanged 参数类型为 `(WebView, BrowserMode) -> Unit`，表示 active WebView 变化后同步浏览模式、滚动、ChromeClient 和页面状态的回调。
 */
class BrowserStandardWebViewHostAssemblyController(
    private val activity: AppCompatActivity,
    private val views: MainActivityViews,
    private val standardTabStore: BrowserTabStore,
    private val configureLinkContextMenu: (WebView) -> Unit,
    private val handleActiveWebViewChanged: (WebView, BrowserMode) -> Unit
) {
    /**
     * 创建标准 WebView 宿主控制器。
     *
     * @return 返回 `BrowserStandardWebViewHostController`，调用方随后执行 setup() 完成 BrowserManager、标签页 WebView 注册表和会话协调器初始化。
     */
    fun create(): BrowserStandardWebViewHostController {
        return BrowserStandardWebViewHostController(
            activity = activity,
            webViewContainer = views.webViewContainer,
            standardTabStore = standardTabStore,
            initialStandardWebView = views.webView,
            configureLinkContextMenu = configureLinkContextMenu,
            handleActiveWebViewChanged = handleActiveWebViewChanged
        )
    }
}
