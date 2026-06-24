package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器启动功能收尾装配模块”。
 * 文件名 BrowserStartupFeatureAssemblyController 可以拆开理解为“Browser Startup Feature Assembly Controller”，
 * 表示它只负责创建功能中心入口、站点安全、页面增强、后退导航，并执行最后的浏览器启动流程。
 * 阅读顺序：先看 BrowserStartupFeatureComponents 知道 MainActivity 会拿到哪些对象，再看 start() 中的最终装配顺序。
 */
import android.content.Intent
import android.content.res.AssetManager
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.MainActivityViews
import com.example.videobrowser.browser.search.BrowserSearchComponents
import com.example.videobrowser.functioncenter.FunctionCenterAssemblyController
import com.example.videobrowser.functioncenter.FunctionCenterEntryController
import com.example.videobrowser.localfiles.LocalFileComponents
import com.example.videobrowser.storage.BrowserPersistenceComponents
import java.io.File

/**
 * 浏览器启动功能组件集合。
 *
 * @param functionCenterEntryController 参数类型为 `FunctionCenterEntryController`，表示功能中心页面入口和返回处理控制器。
 * @param siteSecurityController 参数类型为 `SiteSecurityController`，表示地址栏站点安全状态和站点设置入口控制器。
 * @param pageFeatures 参数类型为 `BrowserPageFeatureComponents`，表示页面增强、元素选择和 native bridge 组件。
 * @param browserBackNavigationController 参数类型为 `BrowserBackNavigationController`，表示系统返回键和浏览器后退动作控制器。
 */
data class BrowserStartupFeatureComponents(
    val functionCenterEntryController: FunctionCenterEntryController,
    val siteSecurityController: SiteSecurityController,
    val pageFeatures: BrowserPageFeatureComponents,
    val browserBackNavigationController: BrowserBackNavigationController
)

/**
 * 浏览器启动功能收尾装配控制器。
 *
 * MainActivity 中最后一段装配依赖已经创建好的核心浏览器组件；本类把功能中心、站点安全、页面增强、
 * 后退导航和 BrowserStartupControllerAssembly 的启动调用集中起来，减少 Activity 对收尾 wiring 的了解。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示创建功能中心、弹窗和页面增强控制器的宿主 Activity。
 * @param intent 参数类型为 `Intent`，表示 Activity 启动 Intent，可能包含外部打开的 URL。
 * @param assets 参数类型为 `AssetManager`，表示页面增强读取脚本资源时使用的资源入口。
 * @param filesDir 参数类型为 `File`，表示功能中心导出、页面归档和调试文件使用的应用私有目录。
 * @param views 参数类型为 `MainActivityViews`，表示读取根视图和站点安全图标等主界面控件的绑定集合。
 * @param browserPersistence 参数类型为 `BrowserPersistenceComponents`，表示设置、收藏历史、下载和播放历史等持久化组件。
 * @param browserSurface 参数类型为 `BrowserWebViewSurfaceComponents`，表示标准 WebView 宿主和 WebView 交互 surface。
 * @param browserShell 参数类型为 `BrowserShellComponents`，表示浏览器外壳、主题、键盘和功能状态组件。
 * @param browserSessions 参数类型为 `BrowserSessionComponents`，表示标准/无痕会话、标签页动作和无痕切换组件。
 * @param browserSearch 参数类型为 `BrowserSearchComponents`，表示搜索提供商、地址栏状态、历史策略和地址建议组件。
 * @param browserNavigation 参数类型为 `BrowserNavigationComponents`，表示导航、启动入口、显示模式、规则引擎和原生播放器入口组件。
 * @param pageActions 参数类型为 `BrowserPageActionComponents`，表示下载、当前页面动作、认证、归档、打印和页内查找组件。
 * @param browserClients 参数类型为 `BrowserClientComponents`，表示 BrowserClient、ChromeClient、新窗口和渲染恢复组件。
 * @param browserFullscreen 参数类型为 `BrowserFullscreenComponents`，表示网页视频全屏和全屏 UI 组件。
 * @param requestInterceptionProvider 参数类型为 `BrowserRequestInterceptionProvider`，表示广告拦截和请求拦截相关 provider。
 * @param activityResultLaunchers 参数类型为 `BrowserActivityResultLaunchers`，表示功能中心和页面动作使用的 Activity Result launcher 集合。
 * @param localFiles 参数类型为 `LocalFileComponents`，表示本地文件列表和本地文档打开入口组件。
 * @param browserSessionStateController 参数类型为 `BrowserSessionStateController`，表示读取当前模式会话状态的控制器。
 * @param browserRuntimeStateController 参数类型为 `BrowserRuntimeStateController`，表示读取启动期首页、无痕和全屏运行状态的控制器。
 * @param browserChromeClientStateController 参数类型为 `BrowserChromeClientStateController`，表示读取当前 ChromeClient 状态的控制器。
 * @param nativeBridgeName 参数类型为 `String`，表示注入 WebView 的 JavaScript native bridge 名称。
 * @param recreateActivity 参数类型为 `() -> Unit`，表示恢复默认设置等场景下重建 Activity 的回调。
 * @param postToUi 参数类型为 `(() -> Unit) -> Unit`，表示把网页线程回调切回 Android UI 线程执行的函数。
 */
class BrowserStartupFeatureAssemblyController(
    private val activity: AppCompatActivity,
    private val intent: Intent,
    private val assets: AssetManager,
    private val filesDir: File,
    private val views: MainActivityViews,
    private val browserPersistence: BrowserPersistenceComponents,
    private val browserSurface: BrowserWebViewSurfaceComponents,
    private val browserShell: BrowserShellComponents,
    private val browserSessions: BrowserSessionComponents,
    private val browserSearch: BrowserSearchComponents,
    private val browserNavigation: BrowserNavigationComponents,
    private val pageActions: BrowserPageActionComponents,
    private val browserClients: BrowserClientComponents,
    private val browserFullscreen: BrowserFullscreenComponents,
    private val requestInterceptionProvider: BrowserRequestInterceptionProvider,
    private val activityResultLaunchers: BrowserActivityResultLaunchers,
    private val localFiles: LocalFileComponents,
    private val browserSessionStateController: BrowserSessionStateController,
    private val browserRuntimeStateController: BrowserRuntimeStateController,
    private val browserChromeClientStateController: BrowserChromeClientStateController,
    private val nativeBridgeName: String,
    private val recreateActivity: () -> Unit,
    private val postToUi: (() -> Unit) -> Unit
) {
    /**
     * 创建启动期功能组件并执行最终启动流程。
     *
     * @return 返回 `BrowserStartupFeatureComponents`，调用方保存后供 Activity 生命周期和返回键回调用。
     */
    fun start(): BrowserStartupFeatureComponents {
        lateinit var pageFeatures: BrowserPageFeatureComponents
        val functionCenterEntryController = FunctionCenterAssemblyController(
            activity = activity,
            functionCenter = browserShell.functionCenterController,
            settingsManager = browserPersistence.settingsManager,
            browserManager = {
                browserSurface.browserStandardWebViewHostController.currentBrowserManager()
            },
            browserManagers = {
                browserSurface.browserStandardWebViewHostController.browserManagers()
            },
            savedPageRepository = browserPersistence.savedPageRepository,
            downloadRecordRepository = browserPersistence.downloadRecordRepository,
            playbackHistoryRepository = browserPersistence.playbackHistoryRepository,
            adBlockLogger = requestInterceptionProvider.adBlockLogger,
            filesDir = filesDir,
            browserUrlStateController = browserShell.browserUrlStateController,
            browserFeatureStateController = browserShell.browserFeatureStateController,
            browserTabActionsController = browserSessions.browserTabActionsController,
            browserLaunchController = browserNavigation.browserLaunchController,
            pageActionsController = pageActions.pageActionsController,
            browserPageToolEntryController = pageActions.browserPageToolEntryController,
            downloadController = pageActions.downloadController,
            activityResultLaunchers = activityResultLaunchers,
            searchProviderController = browserSearch.searchProviderController,
            localDocumentEntryController = localFiles.localDocumentEntryController,
            startElementPicker = { pageFeatures.elementPickerController.start() },
            browserDisplayModeController = browserNavigation.browserDisplayModeController,
            pageFeatureInjectionController = browserShell.pageFeatureInjectionController,
            browserNavigationController = browserNavigation.browserNavigationController,
            hideKeyboard = browserShell.browserKeyboardController::hideKeyboard,
            recreateActivity = recreateActivity
        ).createEntryController()

        val siteSecurityController = BrowserSiteSecurityAssemblyController(
            activity = activity,
            siteSecurityIcon = views.siteSecurityIcon,
            settingsManager = browserPersistence.settingsManager,
            browserSessionStateController = browserSessionStateController,
            browserStandardWebViewHostController =
                browserSurface.browserStandardWebViewHostController,
            browserFeatureStateController = browserShell.browserFeatureStateController,
            browserUrlStateController = browserShell.browserUrlStateController,
            showCurrentSiteSettingsPage = functionCenterEntryController::showCurrentSiteSettingsPage
        ).create()

        pageFeatures = BrowserPageFeatureAssemblyController(
            activity = activity,
            assets = assets,
            settingsManager = browserPersistence.settingsManager,
            ruleEngine = browserNavigation.ruleEngine,
            browserManager = {
                browserSurface.browserStandardWebViewHostController.currentBrowserManager()
            },
            browserSessionStateController = browserSessionStateController,
            browserUrlStateController = browserShell.browserUrlStateController,
            browserFeatureStateController = browserShell.browserFeatureStateController,
            pageFeatureInjectionController = browserShell.pageFeatureInjectionController,
            browserChromeClientStateController = browserChromeClientStateController,
            fullscreenVideoController = browserFullscreen.fullscreenVideoController,
            webPlaybackHistoryRecorder = browserPersistence.webPlaybackHistoryRecorder,
            isBuiltInSearchResultPage =
                browserSearch.builtInSearchResultPagePolicy::isBuiltInSearchResultUrl,
            postToUi = postToUi
        ).create()

        val browserBackNavigationController = BrowserBackNavigationAssemblyController(
            activity = activity,
            browserManager = {
                browserSurface.browserStandardWebViewHostController.currentBrowserManager()
            },
            currentChromeClient = browserChromeClientStateController::currentChromeClientOrNull,
            handleFunctionCenterBack = functionCenterEntryController::handleFunctionCenterBack,
            isElementPickerActive = { pageFeatures.elementPickerController.isActive },
            cancelElementPicker = pageFeatures.elementPickerController::cancel,
            updateNavigationButtons = browserShell.browserShellUiController::updateNavigationButtons
        ).create()

        BrowserStartupControllerAssembly(
            rootView = views.rootView,
            isVideoFullscreenUiActive = browserRuntimeStateController::isVideoFullscreenUiActive,
            browserControlsShellController = browserShell.browserControlsShellController,
            addressSuggestionController = browserSearch.addressSuggestionController,
            browsingModeThemeController = browserShell.browsingModeThemeController,
            browserShellUiController = browserShell.browserShellUiController,
            browserBackNavigationController = browserBackNavigationController,
            browserStandardWebViewHostController =
                browserSurface.browserStandardWebViewHostController,
            settingsManager = browserPersistence.settingsManager,
            setDefaultUserAgent = browserRuntimeStateController::setDefaultUserAgent,
            browserDisplayModeController = browserNavigation.browserDisplayModeController,
            downloadController = pageActions.downloadController,
            browserChromeClientController = browserClients.browserChromeClientController,
            browserFullscreenUiController = browserFullscreen.browserFullscreenUiController,
            nativeBridgeController = pageFeatures.nativeBridgeController,
            nativeBridgeName = nativeBridgeName,
            browserWebClientController = browserClients.browserWebClientController,
            browserLaunchController = browserNavigation.browserLaunchController
        ).start(intent)

        return BrowserStartupFeatureComponents(
            functionCenterEntryController = functionCenterEntryController,
            siteSecurityController = siteSecurityController,
            pageFeatures = pageFeatures,
            browserBackNavigationController = browserBackNavigationController
        )
    }
}
