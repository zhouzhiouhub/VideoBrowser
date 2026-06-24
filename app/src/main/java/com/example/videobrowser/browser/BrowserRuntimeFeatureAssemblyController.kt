package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器运行期交互装配模块”。
 * 文件名 BrowserRuntimeFeatureAssemblyController 可以拆开理解为“Browser Runtime Feature Assembly Controller”，
 * 表示它只负责创建已经进入浏览器运行期后需要的 Web 请求、底部控制栏、标签会话、Client 和全屏组件。
 * 阅读顺序：先看 BrowserRuntimeFeatureComponents 了解会返回哪些对象，再看 create() 中的创建顺序。
 */
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.MainActivityViews
import com.example.videobrowser.browser.search.BrowserSearchComponents
import com.example.videobrowser.settings.SessionSitePermissionStore
import com.example.videobrowser.storage.BrowserPersistenceComponents

/**
 * 浏览器运行期交互组件集合。
 *
 * @param webRequests 参数类型为 `BrowserWebRequestComponents`，表示文件选择、网页权限和定位权限控制器集合。
 * @param browserControls 参数类型为 `BrowserControlsComponents`，表示底部控制栏和滚动联动控制器集合。
 * @param browserSessions 参数类型为 `BrowserSessionComponents`，表示标准/无痕会话、标签页动作和无痕切换控制器集合。
 * @param browserClients 参数类型为 `BrowserClientComponents`，表示 BrowserClient、ChromeClient、新窗口和渲染恢复控制器集合。
 * @param browserFullscreen 参数类型为 `BrowserFullscreenComponents`，表示网页视频全屏和全屏 UI 控制器集合。
 */
data class BrowserRuntimeFeatureComponents(
    val webRequests: BrowserWebRequestComponents,
    val browserControls: BrowserControlsComponents,
    val browserSessions: BrowserSessionComponents,
    val browserClients: BrowserClientComponents,
    val browserFullscreen: BrowserFullscreenComponents
)

/**
 * 浏览器运行期交互装配控制器。
 *
 * MainActivity 原本在 onCreate() 中连续创建 Web 请求、底部控制栏、标签会话、Client 和全屏组件。
 * 本类把这段顺序收拢到一个模块中，保留原来的延迟回调，避免提前访问尚未初始化的启动功能组件。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示创建权限弹窗、控制器和 Android 系统服务时使用的宿主 Activity。
 * @param views 参数类型为 `MainActivityViews`，表示读取底部控制栏、WebView 容器和全屏容器等控件的绑定集合。
 * @param decorView 参数类型为 `View`，表示 Activity 窗口的 decorView，用于 ChromeClient 和全屏 UI 操作。
 * @param browserPersistence 参数类型为 `BrowserPersistenceComponents`，表示设置、收藏历史、下载记录和标签页会话持久化组件。
 * @param browserSurface 参数类型为 `BrowserWebViewSurfaceComponents`，表示标准 WebView 宿主和 WebView 交互 surface。
 * @param browserShell 参数类型为 `BrowserShellComponents`，表示浏览器外壳、主题、键盘和功能状态组件。
 * @param browserSearch 参数类型为 `BrowserSearchComponents`，表示地址栏、搜索提供商、地址建议和历史记录策略组件。
 * @param browserNavigation 参数类型为 `BrowserNavigationComponents`，表示导航、启动入口、显示模式、规则引擎和原生播放器入口组件。
 * @param pageActions 参数类型为 `BrowserPageActionComponents`，表示下载、当前页面动作、认证、归档、打印和页内查找组件。
 * @param browserTabState 参数类型为 `BrowserTabStateComponents`，表示标准/无痕标签页 store 和会话绑定组件。
 * @param sessionSitePermissionStore 参数类型为 `SessionSitePermissionStore`，表示单次会话内记住的网站权限决定。
 * @param browserSessionStateController 参数类型为 `BrowserSessionStateController`，表示读取当前浏览模式会话状态的控制器。
 * @param browserRuntimeStateController 参数类型为 `BrowserRuntimeStateController`，表示读取无痕、首页、全屏和默认 User-Agent 状态的控制器。
 * @param browserChromeClientStateController 参数类型为 `BrowserChromeClientStateController`，表示读取和同步当前 ChromeClient 状态的控制器。
 * @param requestInterceptionProvider 参数类型为 `BrowserRequestInterceptionProvider`，表示广告拦截和智能无图请求拦截 provider。
 * @param activityResultLaunchers 参数类型为 `BrowserActivityResultLaunchers`，表示文件选择、权限申请和导入导出使用的 launcher 集合。
 * @param findInPageController 参数类型为 `FindInPageController`，表示页内查找弹窗使用的查找控制器。
 * @param browserStartupFeatures 参数类型为 `() -> BrowserStartupFeatureComponents?`，表示安全读取启动功能组件的回调；尚未初始化时返回 null。
 * @param recreateActivity 参数类型为 `() -> Unit`，表示恢复默认设置等场景下重建 Activity 的回调。
 * @param dp 参数类型为 `(Int) -> Int`，表示把 dp 数值转换成当前设备像素值的函数。
 */
class BrowserRuntimeFeatureAssemblyController(
    private val activity: AppCompatActivity,
    private val views: MainActivityViews,
    private val decorView: View,
    private val browserPersistence: BrowserPersistenceComponents,
    private val browserSurface: BrowserWebViewSurfaceComponents,
    private val browserShell: BrowserShellComponents,
    private val browserSearch: BrowserSearchComponents,
    private val browserNavigation: BrowserNavigationComponents,
    private val pageActions: BrowserPageActionComponents,
    private val browserTabState: BrowserTabStateComponents,
    private val sessionSitePermissionStore: SessionSitePermissionStore,
    private val browserSessionStateController: BrowserSessionStateController,
    private val browserRuntimeStateController: BrowserRuntimeStateController,
    private val browserChromeClientStateController: BrowserChromeClientStateController,
    private val requestInterceptionProvider: BrowserRequestInterceptionProvider,
    private val activityResultLaunchers: BrowserActivityResultLaunchers,
    private val findInPageController: FindInPageController,
    private val browserStartupFeatures: () -> BrowserStartupFeatureComponents?,
    private val recreateActivity: () -> Unit,
    private val dp: (Int) -> Int
) {
    /**
     * 创建运行期交互组件。
     *
     * @return 返回 `BrowserRuntimeFeatureComponents`，调用方保存后供生命周期、权限回调和启动收尾流程读取。
     */
    fun create(): BrowserRuntimeFeatureComponents {
        lateinit var browserFullscreen: BrowserFullscreenComponents
        lateinit var browserClients: BrowserClientComponents
        val webRequests = BrowserWebRequestAssemblyController(
            activity = activity,
            settingsManager = browserPersistence.settingsManager,
            sessionSitePermissionStore = sessionSitePermissionStore,
            browserFeatureStateController = browserShell.browserFeatureStateController,
            activityResultLaunchers = activityResultLaunchers
        ).create()

        val browserControls = BrowserControlsAssemblyController(
            activity = activity,
            views = views,
            savedPageRepository = browserPersistence.savedPageRepository,
            browserStandardWebViewHostController =
                browserSurface.browserStandardWebViewHostController,
            browserUrlStateController = browserShell.browserUrlStateController,
            browserLaunchController = browserNavigation.browserLaunchController,
            pageActionsController = pageActions.pageActionsController,
            browserAddressBarStateController = browserSearch.browserAddressBarStateController,
            browserControlsShellController = browserShell.browserControlsShellController,
            isHomePageVisible = browserRuntimeStateController::isHomePageVisible,
            isVideoFullscreenUiActive = browserRuntimeStateController::isVideoFullscreenUiActive,
            onBack = {
                requireStartupFeatures().browserBackNavigationController.handleBrowserBack()
            },
            showFunctionCenter = {
                requireStartupFeatures().functionCenterEntryController.showFunctionCenter()
            },
            showProfilePage = {
                requireStartupFeatures().functionCenterEntryController.showProfilePage()
            },
            onAddressFocusChanged = browserShell.browserShellUiController::handleAddressFocusChanged,
            dp = dp
        ).create()

        val browserSessions = BrowserSessionAssemblyController(
            activity = activity,
            standardTabStore = browserTabState.standardTabStore,
            privateTabStore = browserTabState.privateTabStore,
            standardTabWebViews =
                browserSurface.browserStandardWebViewHostController.standardTabWebViews,
            browserSessionCoordinator =
                browserSurface.browserStandardWebViewHostController.sessionCoordinator,
            browserAddressBarStateController = browserSearch.browserAddressBarStateController,
            browserShellUiController = browserShell.browserShellUiController,
            browserControlsController = browserControls.browserControlsController,
            browserControlsShellController = browserShell.browserControlsShellController,
            pageActionsController = pageActions.pageActionsController,
            pageFeatureInjectionController = browserShell.pageFeatureInjectionController,
            browsingModeThemeController = browserShell.browsingModeThemeController,
            sessionSitePermissionStore = sessionSitePermissionStore,
            isPrivateBrowsingActive = browserRuntimeStateController::isPrivateBrowsingActive,
            clearElementPickerState = {
                browserStartupFeatures()?.pageFeatures?.elementPickerController?.clearState()
            },
            cancelElementPickerIfActive = {
                val elementPickerController =
                    browserStartupFeatures()?.pageFeatures?.elementPickerController
                if (elementPickerController?.isActive == true) {
                    elementPickerController.cancel()
                }
            },
            exitPageFullscreenIfNeeded = {
                browserFullscreen.browserFullscreenUiController.exitPageFullscreenIfNeeded()
            },
            closeFunctionCenter = {
                requireStartupFeatures().functionCenterEntryController.closeFunctionCenter()
            },
            openHomePage = browserNavigation.browserLaunchController::openHomePage,
            loadUrl = browserNavigation.browserNavigationController::loadUrl,
            createStandardTabWebView =
                browserSurface.browserStandardWebViewHostController::createStandardTabWebView,
            showStandardTabWebView =
                browserSurface.browserStandardWebViewHostController::showStandardTabWebView,
            hideStandardTabWebView =
                browserSurface.browserStandardWebViewHostController::hideStandardTabWebView,
            destroyStandardTabWebView =
                browserSurface.browserStandardWebViewHostController::destroyStandardTabWebView,
            saveStandardTabSession =
                browserPersistence.browserStandardTabSessionController::saveStandardTabSession,
            onStandardPageMetadataChanged = { url, title ->
                browserTabState.standardTabSessionBinding.handlePageMetadataChanged(url, title)
                browserPersistence.browserStandardTabSessionController.saveStandardTabSession()
            },
            onPrivatePageMetadataChanged =
                browserTabState.privateTabSessionBinding::handlePageMetadataChanged
        ).create()

        browserClients = BrowserClientAssemblyController(
            activity = activity,
            fullscreenContainer = views.fullscreenContainer,
            decorView = decorView,
            webViewContainer = views.webViewContainer,
            standardTabStore = browserTabState.standardTabStore,
            standardTabWebViews =
                browserSurface.browserStandardWebViewHostController.standardTabWebViews,
            browserSessionCoordinator =
                browserSurface.browserStandardWebViewHostController.sessionCoordinator,
            standardSessionController = browserSessions.standardSessionController,
            privateSessionController = browserSessions.privateSessionController,
            browserManager = {
                browserSurface.browserStandardWebViewHostController.currentBrowserManager()
            },
            sessionController = browserSessionStateController::currentSessionController,
            currentPageUrl = {
                browserSessionStateController.currentSessionController().currentPageUrl
            },
            isPrivateBrowsingActive = browserRuntimeStateController::isPrivateBrowsingActive,
            createStandardTabWebView =
                browserSurface.browserStandardWebViewHostController::createStandardTabWebView,
            showStandardTabWebView =
                browserSurface.browserStandardWebViewHostController::showStandardTabWebView,
            saveStandardTabSession =
                browserPersistence.browserStandardTabSessionController::saveStandardTabSession,
            showBrowserErrorPage = { error ->
                browserClients.browserWebClientController.showBrowserErrorPage(error)
            },
            resetBackExitConfirmation = {
                browserStartupFeatures()?.browserBackNavigationController
                    ?.resetBackExitConfirmation()
            },
            clientCertificateController = pageActions.clientCertificateController,
            httpAuthController = pageActions.httpAuthController,
            adBlockRequestInterceptor = requestInterceptionProvider.adBlockRequestInterceptor,
            smartNoImageRequestInterceptor =
                requestInterceptionProvider.smartNoImageRequestInterceptor,
            browserNavigationController = browserNavigation.browserNavigationController,
            closeFunctionCenter = {
                requireStartupFeatures().functionCenterEntryController.closeFunctionCenter()
            },
            closeTab = browserSessions.browserTabActionsController::closeTab,
            fullscreenChanged = { fullscreen ->
                browserFullscreen.browserFullscreenUiController
                    .handleVideoFullscreenChanged(fullscreen)
            },
            webFileChooserController = webRequests.webFileChooserController,
            webPermissionRequestController = webRequests.webPermissionRequestController,
            geolocationPermissionController = webRequests.geolocationPermissionController
        ).create()

        browserFullscreen = BrowserFullscreenAssemblyController(
            activity = activity,
            rootView = views.rootView as ViewGroup,
            browserManager = {
                browserSurface.browserStandardWebViewHostController.currentBrowserManager()
            },
            settingsManager = { browserPersistence.settingsManager },
            browserChromeClientStateController = browserChromeClientStateController,
            browserControlsShellController = browserShell.browserControlsShellController,
            browserDisplayModeController = browserNavigation.browserDisplayModeController,
            browserFeatureStateController = browserShell.browserFeatureStateController,
            dp = dp
        ).create()

        return BrowserRuntimeFeatureComponents(
            webRequests = webRequests,
            browserControls = browserControls,
            browserSessions = browserSessions,
            browserClients = browserClients,
            browserFullscreen = browserFullscreen
        )
    }

    private fun requireStartupFeatures(): BrowserStartupFeatureComponents {
        return requireNotNull(browserStartupFeatures()) {
            "BrowserStartupFeatureComponents has not been initialized."
        }
    }
}
