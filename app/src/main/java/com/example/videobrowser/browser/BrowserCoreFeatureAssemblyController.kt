package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心功能装配模块”。
 * 文件名 BrowserCoreFeatureAssemblyController 可以拆开理解为“Browser Core Feature Assembly Controller”，
 * 表示它只负责创建浏览器启动后最基础的一组组件：外壳、持久化、本地文件、搜索、WebView surface、导航和页面动作。
 * 阅读顺序：先看 BrowserCoreFeatureComponents 了解会返回哪些对象，再看 create() 中的装配顺序。
 */
import android.content.res.AssetManager
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.MainActivityViews
import com.example.videobrowser.browser.search.BrowserSearchAssemblyController
import com.example.videobrowser.browser.search.BrowserSearchComponents
import com.example.videobrowser.localfiles.LocalFileAssemblyController
import com.example.videobrowser.localfiles.LocalFileComponents
import com.example.videobrowser.storage.BrowserPersistenceAssemblyController
import com.example.videobrowser.storage.BrowserPersistenceComponents
import java.io.File

/**
 * 浏览器核心功能组件集合。
 *
 * @param browserShell 参数类型为 `BrowserShellComponents`，表示浏览器外壳、主题、键盘和功能状态组件。
 * @param browserPersistence 参数类型为 `BrowserPersistenceComponents`，表示设置、收藏历史、下载和播放历史等持久化组件。
 * @param localFiles 参数类型为 `LocalFileComponents`，表示本地文件列表和本地文档打开入口组件。
 * @param browserSearch 参数类型为 `BrowserSearchComponents`，表示搜索提供商、地址栏状态、历史策略和地址建议组件。
 * @param browserSurface 参数类型为 `BrowserWebViewSurfaceComponents`，表示标准 WebView 宿主和 WebView 交互 surface。
 * @param browserNavigation 参数类型为 `BrowserNavigationComponents`，表示导航、启动入口、显示模式、规则引擎和原生播放器入口组件。
 * @param pageActions 参数类型为 `BrowserPageActionComponents`，表示下载、当前页面动作、认证、归档、打印和页内查找组件。
 */
data class BrowserCoreFeatureComponents(
    val browserShell: BrowserShellComponents,
    val browserPersistence: BrowserPersistenceComponents,
    val localFiles: LocalFileComponents,
    val browserSearch: BrowserSearchComponents,
    val browserSurface: BrowserWebViewSurfaceComponents,
    val browserNavigation: BrowserNavigationComponents,
    val pageActions: BrowserPageActionComponents
)

/**
 * 浏览器核心功能装配控制器。
 *
 * MainActivity 原本直接按顺序创建核心浏览器组件；本类把这些 wiring 集中起来。
 * 运行期组件和启动功能组件会在本类之后创建，因此这里通过 provider 延迟读取它们。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示创建 UI 控制器、持久化组件和 Android 系统服务时使用的宿主 Activity。
 * @param assets 参数类型为 `AssetManager`，表示规则引擎读取内置规则文件时使用的资源入口。
 * @param filesDir 参数类型为 `File`，表示规则缓存、导出文件和页面归档使用的应用私有目录。
 * @param views 参数类型为 `MainActivityViews`，表示读取地址栏、搜索提供商列表、WebView 容器等控件的绑定集合。
 * @param browserTabState 参数类型为 `BrowserTabStateComponents`，表示标准/无痕标签页 store 和会话绑定组件。
 * @param browserSessionStateController 参数类型为 `BrowserSessionStateController`，表示读取当前浏览模式会话状态的控制器。
 * @param browserRuntimeStateController 参数类型为 `BrowserRuntimeStateController`，表示读取无痕、首页、全屏和默认 User-Agent 状态的控制器。
 * @param browserChromeClientStateController 参数类型为 `BrowserChromeClientStateController`，表示读取和同步当前 ChromeClient 状态的控制器。
 * @param activityResultLaunchers 参数类型为 `BrowserActivityResultLaunchers`，表示功能中心和页面动作使用的 Activity Result launcher 集合。
 * @param findInPageController 参数类型为 `FindInPageController`，表示页内查找弹窗使用的查找控制器。
 * @param browserRuntimeFeatures 参数类型为 `() -> BrowserRuntimeFeatureComponents?`，表示安全读取运行期组件的回调；尚未初始化时返回 null。
 * @param browserStartupFeatures 参数类型为 `() -> BrowserStartupFeatureComponents?`，表示安全读取启动功能组件的回调；尚未初始化时返回 null。
 * @param logTag 参数类型为 `String`，表示本地文件和规则日志使用的日志标签。
 * @param recreateActivity 参数类型为 `() -> Unit`，表示恢复默认设置等场景下重建 Activity 的回调。
 * @param dp 参数类型为 `(Int) -> Int`，表示把 dp 数值转换成当前设备像素值的函数。
 */
class BrowserCoreFeatureAssemblyController(
    private val activity: AppCompatActivity,
    private val assets: AssetManager,
    private val filesDir: File,
    private val views: MainActivityViews,
    private val browserTabState: BrowserTabStateComponents,
    private val browserSessionStateController: BrowserSessionStateController,
    private val browserRuntimeStateController: BrowserRuntimeStateController,
    private val browserChromeClientStateController: BrowserChromeClientStateController,
    private val activityResultLaunchers: BrowserActivityResultLaunchers,
    private val findInPageController: FindInPageController,
    private val browserRuntimeFeatures: () -> BrowserRuntimeFeatureComponents?,
    private val browserStartupFeatures: () -> BrowserStartupFeatureComponents?,
    private val logTag: String,
    private val recreateActivity: () -> Unit,
    private val dp: (Int) -> Int
) {
    /**
     * 创建核心浏览器组件。
     *
     * @return 返回 `BrowserCoreFeatureComponents`，调用方保存后供运行期交互和启动收尾流程继续装配。
     */
    fun create(): BrowserCoreFeatureComponents {
        lateinit var browserShell: BrowserShellComponents
        lateinit var browserPersistence: BrowserPersistenceComponents
        lateinit var localFiles: LocalFileComponents
        lateinit var browserSearch: BrowserSearchComponents
        var browserSearchOrNull: BrowserSearchComponents? = null
        lateinit var browserSurface: BrowserWebViewSurfaceComponents
        lateinit var browserNavigation: BrowserNavigationComponents
        lateinit var pageActions: BrowserPageActionComponents

        browserShell = BrowserShellAssemblyController(
            activity = activity,
            views = views,
            settingsManager = { browserPersistence.settingsManager },
            pageFeatureCoordinator = {
                requireStartupFeatures().pageFeatures.pageFeatureCoordinator
            },
            optionalPageFeatureCoordinator = {
                browserStartupFeatures()?.pageFeatures?.pageFeatureCoordinator
            },
            browserStandardWebViewHostController = {
                browserSurface.browserStandardWebViewHostController
            },
            browserSessionStateController = browserSessionStateController,
            browserControlsController = {
                browserRuntimeFeatures()?.browserControls?.browserControlsController
            },
            browserControlsScrollController = {
                browserRuntimeFeatures()?.browserControls?.browserControlsScrollController
            },
            searchProviderController = {
                browserSearchOrNull?.searchProviderController
            },
            addressSuggestionController = {
                browserSearchOrNull?.addressSuggestionController
            },
            siteSecurityController = {
                browserStartupFeatures()?.siteSecurityController
            },
            isPrivateBrowsingActive = browserRuntimeStateController::isPrivateBrowsingActive,
            isVideoFullscreenUiActive = browserRuntimeStateController::isVideoFullscreenUiActive,
            isHomePageVisible = browserRuntimeStateController::isHomePageVisible,
            dp = dp
        ).create()

        browserPersistence = BrowserPersistenceAssemblyController(
            activity = activity,
            filesDir = filesDir,
            standardTabStore = browserTabState.standardTabStore,
            browserShellUiController = browserShell.browserShellUiController,
            browserFeatureStateController = browserShell.browserFeatureStateController,
            browserUrlStateController = browserShell.browserUrlStateController,
            browserSessionStateController = browserSessionStateController
        ).create()

        localFiles = LocalFileAssemblyController(
            activity = activity,
            preferenceStore = browserPersistence.preferenceStore,
            functionCenter = browserShell.functionCenterController,
            logTag = logTag,
            showMainFunctionCenterPage = {
                requireStartupFeatures().functionCenterEntryController
                    .showFunctionCenterRootPage()
            },
            pageActionsController = { pageActions.pageActionsController },
            closeFunctionCenter = {
                browserStartupFeatures()?.functionCenterEntryController?.closeFunctionCenter()
                    ?: false
            },
            currentSessionController = browserSessionStateController::currentSessionController,
            currentBrowserManager = {
                browserSurface.browserStandardWebViewHostController.currentBrowserManager()
            },
            updateAddressBar = { url ->
                browserSearch.browserAddressBarStateController.updateAddressBar(url)
            },
            hideKeyboard = browserShell.browserKeyboardController::hideKeyboard,
            showHomeContent = browserShell.browserShellUiController::showHomeContent
        ).create()

        browserSearch = BrowserSearchAssemblyController(
            activity = activity,
            providerScroll = views.searchProviderScroll,
            providerList = views.searchProviderList,
            addressInput = views.addressInput,
            addressProviderBadge = views.addressProviderBadge,
            addressSuggestionPanel = views.addressSuggestionPanel,
            settingsManager = browserPersistence.settingsManager,
            savedPageRepository = browserPersistence.savedPageRepository,
            siteSecurityController = {
                browserStartupFeatures()?.siteSecurityController
            },
            dp = dp,
            isPrivateBrowsingEnabled =
                browserShell.browserFeatureStateController::isPrivateBrowsingEnabled,
            areBrowserControlsHidden = {
                (
                    browserRuntimeFeatures()?.browserControls?.browserControlsController?.areHidden
                        == true
                    )
            },
            isVideoFullscreenUiActive = browserRuntimeStateController::isVideoFullscreenUiActive,
            openUrl = { url -> browserNavigation.browserNavigationController.loadUrl(url) },
            searchKeyword = { keyword ->
                browserNavigation.browserLaunchController.searchAddressKeyword(keyword)
            }
        ).create()
        browserSearchOrNull = browserSearch

        browserSurface = BrowserWebViewSurfaceAssemblyController(
            activity = activity,
            views = views,
            standardTabStore = browserTabState.standardTabStore,
            setPrivateBrowsingActive = browserRuntimeStateController::setPrivateBrowsingActive,
            openUrlInNewTab = { url ->
                requireRuntimeFeatures().browserSessions.browserTabActionsController
                    .openUrlInNewTab(url)
            },
            downloadUrl = { url, userAgent ->
                pageActions.downloadController.enqueue(
                    url = url,
                    userAgent = userAgent,
                    contentDisposition = null,
                    mimeType = null
                )
            },
            isShareableUrl = browserShell.browserUrlStateController::isShareableUrl,
            attachBrowserControlsScrollIfReady = { activeWebView ->
                browserRuntimeFeatures()?.browserControls?.browserControlsScrollController
                    ?.attachToWebView(activeWebView)
            },
            syncCurrentChromeClientIfReady =
                browserChromeClientStateController::syncCurrentChromeClientIfReady,
            updatePrivateBrowsingUi =
                browserShell.browsingModeThemeController::updatePrivateBrowsingUi,
            syncSearchProviderVisibility =
                browserShell.browserControlsShellController::syncSearchProviderVisibility,
            applyBrowsingModeTheme =
                browserShell.browsingModeThemeController::applyBrowsingModeTheme,
            areBrowserSessionsInitialized =
                browserSessionStateController::areBrowserSessionsInitialized,
            currentSessionController = browserSessionStateController::currentSessionController
        ).create()
        localFiles.localDocumentEntryController.setupFileOperationLaunchers()

        browserNavigation = BrowserNavigationAssemblyController(
            activity = activity,
            assets = assets,
            filesDir = filesDir,
            addressInput = views.addressInput,
            standardTabStore = browserTabState.standardTabStore,
            browserStandardWebViewHostController =
                browserSurface.browserStandardWebViewHostController,
            browserSessionStateController = browserSessionStateController,
            browserUrlStateController = browserShell.browserUrlStateController,
            browserFeatureStateController = browserShell.browserFeatureStateController,
            browserAddressBarStateController = browserSearch.browserAddressBarStateController,
            browserKeyboardController = browserShell.browserKeyboardController,
            browserShellUiController = browserShell.browserShellUiController,
            browserChromeClientStateController = browserChromeClientStateController,
            homePageUrlPolicy = browserSearch.homePageUrlPolicy,
            addressSuggestionController = browserSearch.addressSuggestionController,
            searchProviderController = browserSearch.searchProviderController,
            closeFunctionCenter = {
                browserStartupFeatures()?.functionCenterEntryController?.closeFunctionCenter()
                    ?: false
            },
            defaultUserAgent = browserRuntimeStateController::defaultUserAgent
        ).create()

        pageActions = BrowserPageActionAssemblyController(
            activity = activity,
            downloadRecordRepository = browserPersistence.downloadRecordRepository,
            settingsManager = browserPersistence.settingsManager,
            savedPageRepository = browserPersistence.savedPageRepository,
            browserDefaultSettingsResetter = browserPersistence.browserDefaultSettingsResetter,
            browserStandardWebViewHostController =
                browserSurface.browserStandardWebViewHostController,
            browserSessionStateController = browserSessionStateController,
            browserUrlStateController = browserShell.browserUrlStateController,
            historyRecordPolicy = browserSearch.historyRecordPolicy,
            nativePlayerEntryController = browserNavigation.nativePlayerEntryController,
            localDocumentEntryController = localFiles.localDocumentEntryController,
            browserFeatureStateController = browserShell.browserFeatureStateController,
            switchPrivateBrowsing = { enabled ->
                requireRuntimeFeatures().browserSessions.privateBrowsingSwitchController
                    .setPrivateBrowsingActive(enabled)
            },
            browserShellUiController = browserShell.browserShellUiController,
            browsingModeThemeController = browserShell.browsingModeThemeController,
            activityResultLaunchers = activityResultLaunchers,
            findInPageController = findInPageController,
            browserNavigationController = browserNavigation.browserNavigationController,
            closeFunctionCenter = {
                browserStartupFeatures()?.functionCenterEntryController?.closeFunctionCenter()
                    ?: false
            },
            recreateActivity = recreateActivity,
            dp = dp
        ).create()

        return BrowserCoreFeatureComponents(
            browserShell = browserShell,
            browserPersistence = browserPersistence,
            localFiles = localFiles,
            browserSearch = browserSearch,
            browserSurface = browserSurface,
            browserNavigation = browserNavigation,
            pageActions = pageActions
        )
    }

    private fun requireRuntimeFeatures(): BrowserRuntimeFeatureComponents {
        return requireNotNull(browserRuntimeFeatures()) {
            "BrowserRuntimeFeatureComponents has not been initialized."
        }
    }

    private fun requireStartupFeatures(): BrowserStartupFeatureComponents {
        return requireNotNull(browserStartupFeatures()) {
            "BrowserStartupFeatureComponents has not been initialized."
        }
    }
}
