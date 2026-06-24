package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器工具栏装配模块”。
 * 文件名 BrowserControlsAssemblyController 可以拆开理解为“Browser Controls Assembly Controller”，
 * 表示它只负责创建浏览器按钮/地址栏控制器和页面滚动时自动隐藏工具栏的控制器。
 * 阅读顺序：先看 BrowserControlsComponents 知道返回哪些对象，再看 create() 中工具栏控制器和滚动控制器如何互相连接。
 */
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.MainActivityViews
import com.example.videobrowser.storage.SavedPageRepository

/**
 * 浏览器工具栏组件集合。
 *
 * @param browserControlsController 参数类型为 `BrowserControlsController`，表示管理地址栏、导航按钮、收藏按钮和进度条的控制器。
 * @param browserControlsScrollController 参数类型为 `BrowserControlsScrollController`，表示监听页面滚动并自动隐藏或显示工具栏的控制器。
 */
data class BrowserControlsComponents(
    val browserControlsController: BrowserControlsController,
    val browserControlsScrollController: BrowserControlsScrollController
)

/**
 * 浏览器工具栏装配控制器。
 *
 * MainActivityViews 已经收拢了 activity_main.xml 中的控件引用；本类继续把这些控件接入
 * BrowserControlsController 和 BrowserControlsScrollController，避免 MainActivity 展开大量 UI 参数。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示读取字符串资源、设置 tooltip 和访问 Activity 上下文的宿主。
 * @param views 参数类型为 `MainActivityViews`，表示主界面所有常用控件的绑定集合。
 * @param savedPageRepository 参数类型为 `SavedPageRepository`，表示判断当前页面是否已收藏的数据仓库。
 * @param browserStandardWebViewHostController 参数类型为 `BrowserStandardWebViewHostController`，表示提供当前 BrowserManager 和标准 WebView 的宿主控制器。
 * @param browserUrlStateController 参数类型为 `BrowserUrlStateController`，表示提供当前可操作 URL 的控制器。
 * @param browserLaunchController 参数类型为 `BrowserLaunchController`，表示处理地址栏加载和打开文心页面的导航入口。
 * @param pageActionsController 参数类型为 `PageActionsController`，表示执行收藏切换等当前页面动作的控制器。
 * @param browserAddressBarStateController 参数类型为 `BrowserAddressBarStateController`，表示地址栏焦点变化时切换展示文本的控制器。
 * @param browserControlsShellController 参数类型为 `BrowserControlsShellController`，表示协调工具栏显示、地址栏焦点和搜索入口可见性的外壳控制器。
 * @param isHomePageVisible 参数类型为 `() -> Boolean`，表示读取当前首页内容是否可见的回调。
 * @param isVideoFullscreenUiActive 参数类型为 `() -> Boolean`，表示读取当前是否处于视频全屏 UI 的回调。
 * @param onBack 参数类型为 `() -> Unit`，表示点击后退按钮时执行统一后退流程的回调。
 * @param showFunctionCenter 参数类型为 `() -> Unit`，表示点击页面工具按钮时打开功能中心的回调。
 * @param showProfilePage 参数类型为 `() -> Unit`，表示点击个人入口按钮时打开资料页的回调。
 * @param onAddressFocusChanged 参数类型为 `(Boolean) -> Unit`，表示地址栏焦点变化后刷新首页搜索框位置的回调。
 * @param dp 参数类型为 `(Int) -> Int`，表示把 dp 数值转换为像素的回调。
 */
class BrowserControlsAssemblyController(
    private val activity: AppCompatActivity,
    private val views: MainActivityViews,
    private val savedPageRepository: SavedPageRepository,
    private val browserStandardWebViewHostController: BrowserStandardWebViewHostController,
    private val browserUrlStateController: BrowserUrlStateController,
    private val browserLaunchController: BrowserLaunchController,
    private val pageActionsController: PageActionsController,
    private val browserAddressBarStateController: BrowserAddressBarStateController,
    private val browserControlsShellController: BrowserControlsShellController,
    private val isHomePageVisible: () -> Boolean,
    private val isVideoFullscreenUiActive: () -> Boolean,
    private val onBack: () -> Unit,
    private val showFunctionCenter: () -> Unit,
    private val showProfilePage: () -> Unit,
    private val onAddressFocusChanged: (Boolean) -> Unit,
    private val dp: (Int) -> Int
) {
    /**
     * 创建浏览器工具栏组件集合。
     *
     * @return 返回 `BrowserControlsComponents`，调用方把其中对象保存到 MainActivity 字段后供会话、全屏和启动流程继续使用。
     */
    fun create(): BrowserControlsComponents {
        val browserControlsController = BrowserControlsController(
            activity = activity,
            browserManager = {
                browserStandardWebViewHostController.currentBrowserManager()
            },
            topBar = views.topBar,
            bottomBar = views.bottomBar,
            addressInput = views.addressInput,
            pageProgress = views.pageProgress,
            pageToolsButton = views.pageToolsButton,
            wenxinButton = views.wenxinButton,
            profileButton = views.profileButton,
            backButton = views.backButton,
            refreshButton = views.refreshButton,
            bookmarkButton = views.bookmarkButton,
            loadButton = views.loadButton,
            savedPageRepository = savedPageRepository,
            currentActionableUrl = browserUrlStateController::currentActionableUrl,
            isHomePageVisible = isHomePageVisible,
            isVideoFullscreenUiActive = isVideoFullscreenUiActive,
            onLoadAddress = browserLaunchController::loadAddressInput,
            onBack = onBack,
            onOpenWenxin = browserLaunchController::openWenxinPage,
            onShowFunctionCenter = showFunctionCenter,
            onShowProfilePage = showProfilePage,
            onToggleBookmark = pageActionsController::toggleCurrentBookmark,
            onShowControlsRequested = {
                browserControlsShellController.setBrowserControlsHidden(false)
            },
            onAddressFocusChanged = { hasFocus ->
                browserAddressBarStateController.handleAddressFocusChanged(hasFocus)
                browserControlsShellController.handleAddressFocusChanged(hasFocus)
                onAddressFocusChanged(hasFocus)
            },
            onVisibilityChanged = browserControlsShellController::syncSearchProviderVisibility
        )
        val browserControlsScrollController = BrowserControlsScrollController(
            webView = browserStandardWebViewHostController.standardWebView,
            addressInput = views.addressInput,
            dp = dp,
            areControlsHidden = { browserControlsController.areHidden },
            isHomePageVisible = isHomePageVisible,
            isVideoFullscreenUiActive = isVideoFullscreenUiActive,
            applyControlsHidden = browserControlsController::setHidden,
            updatePageProgressVisibility = browserControlsShellController::updatePageProgressVisibility
        )
        return BrowserControlsComponents(
            browserControlsController = browserControlsController,
            browserControlsScrollController = browserControlsScrollController
        )
    }
}
