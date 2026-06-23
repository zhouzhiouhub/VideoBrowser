package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器外壳基础装配模块”。
 * 文件名 BrowserShellAssemblyController 可以拆开理解为“Browser Shell Assembly Controller”，
 * 表示它只负责创建键盘、功能状态、URL 状态、控制栏外壳、浏览模式主题和外壳 UI 这些基础控制器。
 * 阅读顺序：先看 BrowserShellComponents 知道返回哪些对象，再看 create() 中这些基础控制器如何通过延迟 provider 避免初始化顺序问题。
 */
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.MainActivityViews
import com.example.videobrowser.browser.search.AddressSuggestionController
import com.example.videobrowser.browser.search.SearchProviderController
import com.example.videobrowser.functioncenter.FunctionCenterController
import com.example.videobrowser.inject.PageFeatureCoordinator
import com.example.videobrowser.inject.PageFeatureInjectionController
import com.example.videobrowser.settings.SettingsManager

/**
 * 浏览器外壳基础组件集合。
 *
 * @param browserKeyboardController 参数类型为 `BrowserKeyboardController`，表示地址栏键盘和输入法动作控制器。
 * @param pageFeatureInjectionController 参数类型为 `PageFeatureInjectionController`，表示页面完成加载后触发增强脚本注入的入口控制器。
 * @param functionCenterController 参数类型为 `FunctionCenterController`，表示底部功能中心容器控制器。
 * @param browserFeatureStateController 参数类型为 `BrowserFeatureStateController`，表示读取无痕、桌面模式和页面增强开关状态的控制器。
 * @param browserUrlStateController 参数类型为 `BrowserUrlStateController`，表示读取当前页面 URL、WebView URL 和站点 host 的控制器。
 * @param browserControlsShellController 参数类型为 `BrowserControlsShellController`，表示协调工具栏显示、搜索入口和进度条状态的控制器。
 * @param browsingModeThemeController 参数类型为 `BrowsingModeThemeController`，表示应用普通/无痕主题颜色的控制器。
 * @param browserShellUiController 参数类型为 `BrowserShellUiController`，表示统一刷新浏览器外壳首页、导航、收藏和站点安全入口的控制器。
 */
data class BrowserShellComponents(
    val browserKeyboardController: BrowserKeyboardController,
    val pageFeatureInjectionController: PageFeatureInjectionController,
    val functionCenterController: FunctionCenterController,
    val browserFeatureStateController: BrowserFeatureStateController,
    val browserUrlStateController: BrowserUrlStateController,
    val browserControlsShellController: BrowserControlsShellController,
    val browsingModeThemeController: BrowsingModeThemeController,
    val browserShellUiController: BrowserShellUiController
)

/**
 * 浏览器外壳基础装配控制器。
 *
 * MainActivity 早期初始化阶段有不少互相延迟引用的小控制器；本类集中保存这些 provider，
 * 并把具体构造顺序收拢到 create()，让 Activity 只负责保存返回的组件。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示创建控制器和读取 UI 资源的宿主 Activity。
 * @param views 参数类型为 `MainActivityViews`，表示主界面常用控件的绑定集合。
 * @param settingsManager 参数类型为 `() -> SettingsManager`，表示读取设置管理器的回调；创建时设置尚未初始化，调用时必须已准备好。
 * @param pageFeatureCoordinator 参数类型为 `() -> PageFeatureCoordinator`，表示读取页面增强协调器的回调；功能状态查询时必须已准备好。
 * @param optionalPageFeatureCoordinator 参数类型为 `() -> PageFeatureCoordinator?`，表示安全读取页面增强协调器的回调；早期页面完成回调可以返回 null。
 * @param browserStandardWebViewHostController 参数类型为 `() -> BrowserStandardWebViewHostController`，表示读取标准 WebView 宿主控制器的回调。
 * @param browserSessionStateController 参数类型为 `BrowserSessionStateController`，表示读取当前浏览会话状态的控制器。
 * @param browserControlsController 参数类型为 `() -> BrowserControlsController?`，表示安全读取浏览器按钮/进度条控制器的回调。
 * @param browserControlsScrollController 参数类型为 `() -> BrowserControlsScrollController?`，表示安全读取滚动隐藏工具栏控制器的回调。
 * @param searchProviderController 参数类型为 `() -> SearchProviderController?`，表示安全读取搜索提供商控制器的回调。
 * @param addressSuggestionController 参数类型为 `() -> AddressSuggestionController?`，表示安全读取地址建议控制器的回调。
 * @param siteSecurityController 参数类型为 `() -> SiteSecurityController?`，表示安全读取站点安全控制器的回调。
 * @param isPrivateBrowsingActive 参数类型为 `() -> Boolean`，表示读取当前是否处于无痕模式的回调。
 * @param isVideoFullscreenUiActive 参数类型为 `() -> Boolean`，表示读取网页视频全屏 UI 是否激活的回调。
 * @param isHomePageVisible 参数类型为 `() -> Boolean`，表示读取首页内容是否可见的回调。
 * @param dp 参数类型为 `(Int) -> Int`，表示把 dp 数值转换成像素的回调。
 */
class BrowserShellAssemblyController(
    private val activity: AppCompatActivity,
    private val views: MainActivityViews,
    private val settingsManager: () -> SettingsManager,
    private val pageFeatureCoordinator: () -> PageFeatureCoordinator,
    private val optionalPageFeatureCoordinator: () -> PageFeatureCoordinator?,
    private val browserStandardWebViewHostController: () -> BrowserStandardWebViewHostController,
    private val browserSessionStateController: BrowserSessionStateController,
    private val browserControlsController: () -> BrowserControlsController?,
    private val browserControlsScrollController: () -> BrowserControlsScrollController?,
    private val searchProviderController: () -> SearchProviderController?,
    private val addressSuggestionController: () -> AddressSuggestionController?,
    private val siteSecurityController: () -> SiteSecurityController?,
    private val isPrivateBrowsingActive: () -> Boolean,
    private val isVideoFullscreenUiActive: () -> Boolean,
    private val isHomePageVisible: () -> Boolean,
    private val dp: (Int) -> Int
) {
    /**
     * 创建浏览器外壳基础组件集合。
     *
     * @return 返回 `BrowserShellComponents`，调用方把其中对象保存到 MainActivity 字段后继续创建持久化、搜索和 WebView 相关模块。
     */
    fun create(): BrowserShellComponents {
        val browserKeyboardController = BrowserKeyboardController(
            context = activity,
            addressInput = views.addressInput,
            addressSuggestionController = addressSuggestionController
        )
        val pageFeatureInjectionController = PageFeatureInjectionController(
            pageFeatureCoordinator = optionalPageFeatureCoordinator
        )
        val functionCenterController = FunctionCenterController(activity, views.rootView, dp)
        val browserFeatureStateController = BrowserFeatureStateController(
            settingsManager = settingsManager,
            pageFeatureCoordinator = pageFeatureCoordinator,
            isPrivateBrowsingActive = isPrivateBrowsingActive
        )
        val browserUrlStateController = BrowserUrlStateController(
            currentPageUrl = {
                currentSessionControllerOrNull()?.currentPageUrl
            },
            currentWebViewUrl = {
                if (browserSessionStateController.areBrowserSessionsInitialized()) {
                    browserStandardWebViewHostController().currentBrowserManager().currentUrl()
                } else {
                    null
                }
            }
        )
        val browserControlsShellController = BrowserControlsShellController(
            browserControlsController = browserControlsController,
            browserControlsScrollController = browserControlsScrollController,
            searchProviderController = searchProviderController,
            addressSuggestionController = addressSuggestionController,
            isPageLoading = {
                currentSessionControllerOrNull()?.isPageLoading ?: false
            },
            isVideoFullscreenUiActive = isVideoFullscreenUiActive,
            isHomePageVisible = isHomePageVisible
        )
        val browsingModeThemeController = BrowsingModeThemeController(
            activity = activity,
            views = views,
            isPrivateBrowsingEnabled = browserFeatureStateController::isPrivateBrowsingEnabled,
            currentPageUrl = {
                currentSessionControllerOrNull()?.currentPageUrl
            },
            updateSiteSecurityStatus = { url ->
                siteSecurityController()?.updateStatus(url)
            },
            dp = dp
        )
        val browserShellUiController = BrowserShellUiController(
            browserControlsController = browserControlsController,
            siteSecurityController = siteSecurityController,
            browserControlsScrollController = browserControlsScrollController,
            browserControlsShellController = browserControlsShellController,
            rootView = views.rootView,
            topBar = views.topBar,
            bottomBar = views.bottomBar,
            activeWebView = {
                browserStandardWebViewHostController().currentBrowserManager().activeWebView
            },
            browsingModeThemeController = browsingModeThemeController
        )
        return BrowserShellComponents(
            browserKeyboardController = browserKeyboardController,
            pageFeatureInjectionController = pageFeatureInjectionController,
            functionCenterController = functionCenterController,
            browserFeatureStateController = browserFeatureStateController,
            browserUrlStateController = browserUrlStateController,
            browserControlsShellController = browserControlsShellController,
            browsingModeThemeController = browsingModeThemeController,
            browserShellUiController = browserShellUiController
        )
    }

    /**
     * 安全读取当前会话控制器。
     *
     * @return 返回 `BrowserSessionController?`，浏览会话尚未完成初始化时返回 null。
     */
    private fun currentSessionControllerOrNull(): BrowserSessionController? {
        return if (browserSessionStateController.areBrowserSessionsInitialized()) {
            browserSessionStateController.currentSessionController()
        } else {
            null
        }
    }
}
