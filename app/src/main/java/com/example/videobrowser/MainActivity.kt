package com.example.videobrowser

/**
 * 应用主界面入口。
 *
 * 这个文件负责把“浏览器 App 的各个模块”接到 Android Activity 生命周期上：
 * - 视图层：地址栏、底部按钮、首页区域、全屏容器。
 * - 浏览器层：WebView 创建、页面加载、标签页、网页权限、错误页。
 * - 内容增强：广告拦截、规则清理、JavaScript 注入、元素选择器。
 * - 业务入口：功能中心、下载、本地文件、收藏/历史、原生播放器。
 *
 * 阅读建议：
 * 1. 先看 onCreate()，它展示所有模块如何被创建和连接。
 * 2. 再按下面的“region”分区阅读，例如标签页、权限、导航、站点安全。
 * 3. 遇到具体业务时跳到对应包，例如 browser、video、download、functioncenter。
 */
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.browser.BrowserActivityLifecycleAssemblyController
import com.example.videobrowser.browser.BrowserActivityResultLaunchers
import com.example.videobrowser.browser.BrowserActivityResultLaunchersAssemblyController
import com.example.videobrowser.browser.BrowserAddressBarStateController
import com.example.videobrowser.browser.BrowserBackNavigationAssemblyController
import com.example.videobrowser.browser.BrowserBackNavigationController
import com.example.videobrowser.browser.BrowserChromeClientController
import com.example.videobrowser.browser.BrowserChromeClientStateAssemblyController
import com.example.videobrowser.browser.BrowserClientAssemblyController
import com.example.videobrowser.browser.BrowserControlsAssemblyController
import com.example.videobrowser.browser.BrowserControlsComponents
import com.example.videobrowser.browser.BrowserFeatureStateController
import com.example.videobrowser.browser.BrowserControlsShellController
import com.example.videobrowser.browser.BrowserDisplayModeController
import com.example.videobrowser.browser.BrowserFullscreenAssemblyController
import com.example.videobrowser.browser.BrowserFullscreenUiController
import com.example.videobrowser.browser.BrowserKeyboardController
import com.example.videobrowser.browser.BrowserUrlStateController
import com.example.videobrowser.browser.BrowserExternalNavigator
import com.example.videobrowser.browser.BrowserFindInPageAssemblyController
import com.example.videobrowser.browser.HistoryRecordPolicy
import com.example.videobrowser.browser.BrowserLaunchController
import com.example.videobrowser.browser.BrowserNavigationController
import com.example.videobrowser.browser.BrowserNavigationAssemblyController
import com.example.videobrowser.browser.BrowserPageActionAssemblyController
import com.example.videobrowser.browser.BrowserPageActionComponents
import com.example.videobrowser.browser.BrowserPageFeatureAssemblyController
import com.example.videobrowser.browser.BrowserSessionController
import com.example.videobrowser.browser.BrowserSessionAssemblyController
import com.example.videobrowser.browser.BrowserSessionStateAssemblyController
import com.example.videobrowser.browser.BrowserSessionStateController
import com.example.videobrowser.browser.BrowserShellAssemblyController
import com.example.videobrowser.browser.BrowserShellUiController
import com.example.videobrowser.browser.BrowserSiteSecurityAssemblyController
import com.example.videobrowser.browser.BrowserStandardWebViewHostAssemblyController
import com.example.videobrowser.browser.BrowserStandardWebViewHostController
import com.example.videobrowser.browser.BrowserStartupControllerAssembly
import com.example.videobrowser.browser.BrowserTabActionsController
import com.example.videobrowser.browser.BrowserTabStateAssemblyController
import com.example.videobrowser.browser.BrowserRequestInterceptionProvider
import com.example.videobrowser.browser.BrowserWebClientController
import com.example.videobrowser.browser.BrowserWebViewDebugController
import com.example.videobrowser.browser.BrowserWebViewInteractionAssemblyController
import com.example.videobrowser.browser.BrowserWebViewInteractionComponents
import com.example.videobrowser.browser.BrowserWebRequestAssemblyController
import com.example.videobrowser.browser.BrowsingModeThemeController
import com.example.videobrowser.browser.GeolocationPermissionController
import com.example.videobrowser.browser.PrivateBrowsingSwitchController
import com.example.videobrowser.browser.RenderProcessRecoveryController
import com.example.videobrowser.browser.BrowserRuntimeStateController
import com.example.videobrowser.browser.SiteSecurityController
import com.example.videobrowser.browser.VideoBrowserNativeBridgeController
import com.example.videobrowser.browser.WebFileChooserController
import com.example.videobrowser.browser.WebWindowController
import com.example.videobrowser.browser.WebPermissionRequestController
import com.example.videobrowser.browser.search.AddressSuggestionController
import com.example.videobrowser.browser.search.BrowserSearchAssemblyController
import com.example.videobrowser.browser.search.SearchProviderController
import com.example.videobrowser.element.ElementPickerController
import com.example.videobrowser.functioncenter.FunctionCenterAssemblyController
import com.example.videobrowser.functioncenter.FunctionCenterController
import com.example.videobrowser.functioncenter.FunctionCenterEntryController
import com.example.videobrowser.inject.JsInjector
import com.example.videobrowser.inject.PageFeatureInjectionController
import com.example.videobrowser.inject.PageFeatureCoordinator
import com.example.videobrowser.localfiles.LocalFileAssemblyController
import com.example.videobrowser.localfiles.LocalDocumentEntryController
import com.example.videobrowser.localfiles.LocalFilesController
import com.example.videobrowser.rules.RuleEngine
import com.example.videobrowser.settings.SessionSitePermissionStore
import com.example.videobrowser.storage.BrowserPersistenceAssemblyController
import com.example.videobrowser.storage.BrowserPersistenceComponents
import com.example.videobrowser.video.FullscreenVideoController
import com.example.videobrowser.video.NativePlayerEntryController

/**
 * VideoBrowser 的主 Activity。
 *
 * Activity 是 Android 应用里的“屏幕控制器”。MainActivity 不直接实现所有业务细节，
 * 而是把 WebView、设置、功能中心、下载、播放等控制器组合起来，并处理必须留在
 * Activity 层的系统回调，例如权限申请、文件选择、页面生命周期和返回键。
 */
class MainActivity : AppCompatActivity() {

    // region 视图引用
    // views 来自 MainActivityViews.bind(this)，集中保存 activity_main.xml 中的控件。
    private lateinit var views: MainActivityViews
    // endregion

    // region 应用级控制器和仓库
    // Repository 负责读写本机数据；Controller 负责连接 UI、WebView 和业务动作。
    // 这些 lateinit 属性会在 onCreate() 里按依赖顺序初始化。
    private lateinit var browserPersistence: BrowserPersistenceComponents
    private lateinit var browserFeatureStateController: BrowserFeatureStateController
    private lateinit var browserUrlStateController: BrowserUrlStateController
    private lateinit var ruleEngine: RuleEngine
    private lateinit var browserStandardWebViewHostController: BrowserStandardWebViewHostController
    private lateinit var browserControls: BrowserControlsComponents
    private lateinit var browserControlsShellController: BrowserControlsShellController
    private lateinit var standardSessionController: BrowserSessionController
    private lateinit var privateSessionController: BrowserSessionController
    private lateinit var functionCenterController: FunctionCenterController
    private lateinit var functionCenterEntryController: FunctionCenterEntryController
    private lateinit var localFilesController: LocalFilesController
    private lateinit var localDocumentEntryController: LocalDocumentEntryController
    private lateinit var pageActions: BrowserPageActionComponents
    private lateinit var renderProcessRecoveryController: RenderProcessRecoveryController
    private lateinit var webWindowController: WebWindowController
    private lateinit var historyRecordPolicy: HistoryRecordPolicy
    private lateinit var searchProviderController: SearchProviderController
    private lateinit var browserAddressBarStateController: BrowserAddressBarStateController
    private lateinit var addressSuggestionController: AddressSuggestionController
    private lateinit var browserLaunchController: BrowserLaunchController
    private lateinit var browserTabActionsController: BrowserTabActionsController
    private lateinit var privateBrowsingSwitchController: PrivateBrowsingSwitchController
    private lateinit var siteSecurityController: SiteSecurityController
    private lateinit var browserNavigationController: BrowserNavigationController
    private lateinit var browserDisplayModeController: BrowserDisplayModeController
    private lateinit var browsingModeThemeController: BrowsingModeThemeController
    private lateinit var browserShellUiController: BrowserShellUiController
    private lateinit var browserBackNavigationController: BrowserBackNavigationController
    private lateinit var browserKeyboardController: BrowserKeyboardController
    private lateinit var nativeBridgeController: VideoBrowserNativeBridgeController
    private lateinit var fullscreenVideoController: FullscreenVideoController
    private lateinit var browserFullscreenUiController: BrowserFullscreenUiController
    private lateinit var webViewInteraction: BrowserWebViewInteractionComponents
    private lateinit var webFileChooserController: WebFileChooserController
    private lateinit var webPermissionRequestController: WebPermissionRequestController
    private lateinit var geolocationPermissionController: GeolocationPermissionController
    private lateinit var elementPickerController: ElementPickerController
    private lateinit var jsInjector: JsInjector
    private lateinit var pageFeatureInjectionController: PageFeatureInjectionController
    private lateinit var pageFeatureCoordinator: PageFeatureCoordinator
    private lateinit var browserChromeClientController: BrowserChromeClientController
    private val browserChromeClientStateController = BrowserChromeClientStateAssemblyController(
        browserChromeClientController = {
            if (::browserChromeClientController.isInitialized) {
                browserChromeClientController
            } else {
                null
            }
        }
    ).create()
    private lateinit var browserWebClientController: BrowserWebClientController
    private lateinit var externalNavigator: BrowserExternalNavigator
    private lateinit var nativePlayerEntryController: NativePlayerEntryController
    private val browserActivityLifecycleController = BrowserActivityLifecycleAssemblyController(
        browserChromeClientController = {
            if (::browserChromeClientController.isInitialized) browserChromeClientController else null
        },
        browserWebClientController = {
            if (::browserWebClientController.isInitialized) browserWebClientController else null
        },
        pageArchiveController = {
            if (::pageActions.isInitialized) pageActions.pageArchiveController else null
        },
        addressSuggestionController = {
            if (::addressSuggestionController.isInitialized) addressSuggestionController else null
        },
        downloadController = {
            if (::pageActions.isInitialized) pageActions.downloadController else null
        },
        elementPickerController = {
            if (::elementPickerController.isInitialized) elementPickerController else null
        },
        functionCenterEntryController = {
            if (::functionCenterEntryController.isInitialized) functionCenterEntryController else null
        },
        browserChromeClientStateController = browserChromeClientStateController,
        browserStandardTabSessionController = {
            if (::browserPersistence.isInitialized) {
                browserPersistence.browserStandardTabSessionController
            } else {
                null
            }
        },
        browserStandardWebViewHostController = {
            if (::browserStandardWebViewHostController.isInitialized) {
                browserStandardWebViewHostController
            } else {
                null
            }
        },
        browserLaunchController = {
            if (::browserLaunchController.isInitialized) browserLaunchController else null
        }
    ).create()
    // endregion

    // region 标签页与会话状态
    // 标准模式和无痕模式各有自己的标签页列表，避免无痕页面写入普通会话。
    private val browserTabState = BrowserTabStateAssemblyController().create()
    private val findInPageController = BrowserFindInPageAssemblyController(
        browserStandardWebViewHostController = { browserStandardWebViewHostController }
    ).create()
    // endregion

    // region 网页内容增强和拦截
    // 请求拦截提供器内部使用 lazy，确保规则引擎和设置管理器完成初始化后才创建拦截对象。
    private val requestInterceptionProvider = BrowserRequestInterceptionProvider(
        browserFeatureStateController = { browserFeatureStateController },
        settingsManager = { browserPersistence.settingsManager },
        browserSessionStateController = { browserSessionStateController },
        browserUrlStateController = { browserUrlStateController },
        ruleEngine = { ruleEngine }
    )
    // endregion

    // region Android 系统交互状态
    // 这些字段保存系统弹窗或系统 Activity 返回前的临时状态，例如文件选择、权限申请、证书选择。
    private val activityResultLaunchers = BrowserActivityResultLaunchersAssemblyController(
        activity = this,
        webFileChooserController = {
            if (::webFileChooserController.isInitialized) webFileChooserController else null
        },
        bookmarkImportExportController = {
            if (::browserPersistence.isInitialized) {
                browserPersistence.bookmarkImportExportController
            } else {
                null
            }
        },
        pageArchiveController = {
            if (::pageActions.isInitialized) pageActions.pageArchiveController else null
        },
        webPermissionRequestController = {
            if (::webPermissionRequestController.isInitialized) webPermissionRequestController else null
        },
        geolocationPermissionController = {
            if (::geolocationPermissionController.isInitialized) geolocationPermissionController else null
        }
    ).create()
    private val sessionSitePermissionStore = SessionSitePermissionStore()
    // endregion

    // region 当前页面运行状态
    private val browserRuntimeStateController = BrowserRuntimeStateController(
        currentSessionController = {
            browserSessionStateController.currentSessionController()
        },
        fullscreenVideoController = {
            if (::fullscreenVideoController.isInitialized) fullscreenVideoController else null
        }
    )
    private val browserSessionStateController: BrowserSessionStateController =
        BrowserSessionStateAssemblyController(
            isPrivateBrowsingActive = browserRuntimeStateController::isPrivateBrowsingActive,
            standardSessionController = {
                if (::standardSessionController.isInitialized) standardSessionController else null
            },
            privateSessionController = {
                if (::privateSessionController.isInitialized) privateSessionController else null
            }
        ).create()
    // endregion

    // region 生命周期
    /**
     * Android 创建主界面时调用。
     *
     * 这个函数很长，但可以按“创建依赖 -> 连接控制器 -> 配置 WebView -> 打开初始页面”理解。
     * 后续如果要排查启动问题，通常从这里的初始化顺序开始看。
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Debug 包开启 WebView 远程调试，方便在 Chrome DevTools 里查看网页和注入脚本。
        BrowserWebViewDebugController(applicationInfo.flags).enableForDebuggableBuild()
        setContentView(R.layout.activity_main)

        // 先绑定界面控件，再创建依赖这些控件的控制器。
        views = MainActivityViews.bind(this)
        val browserShellComponents = BrowserShellAssemblyController(
            activity = this,
            views = views,
            settingsManager = { browserPersistence.settingsManager },
            pageFeatureCoordinator = { pageFeatureCoordinator },
            optionalPageFeatureCoordinator = {
                if (::pageFeatureCoordinator.isInitialized) pageFeatureCoordinator else null
            },
            browserStandardWebViewHostController = { browserStandardWebViewHostController },
            browserSessionStateController = browserSessionStateController,
            browserControlsController = {
                if (::browserControls.isInitialized) {
                    browserControls.browserControlsController
                } else {
                    null
                }
            },
            browserControlsScrollController = {
                if (::browserControls.isInitialized) {
                    browserControls.browserControlsScrollController
                } else {
                    null
                }
            },
            searchProviderController = {
                if (::searchProviderController.isInitialized) searchProviderController else null
            },
            addressSuggestionController = {
                if (::addressSuggestionController.isInitialized) addressSuggestionController else null
            },
            siteSecurityController = {
                if (::siteSecurityController.isInitialized) {
                    siteSecurityController
                } else {
                    null
                }
            },
            isPrivateBrowsingActive = browserRuntimeStateController::isPrivateBrowsingActive,
            isVideoFullscreenUiActive = browserRuntimeStateController::isVideoFullscreenUiActive,
            isHomePageVisible = browserRuntimeStateController::isHomePageVisible,
            dp = ::dp
        ).create()
        browserKeyboardController = browserShellComponents.browserKeyboardController
        pageFeatureInjectionController = browserShellComponents.pageFeatureInjectionController
        functionCenterController = browserShellComponents.functionCenterController
        browserFeatureStateController = browserShellComponents.browserFeatureStateController
        browserUrlStateController = browserShellComponents.browserUrlStateController
        browserControlsShellController = browserShellComponents.browserControlsShellController
        browsingModeThemeController = browserShellComponents.browsingModeThemeController
        browserShellUiController = browserShellComponents.browserShellUiController

        // 本地持久化层：设置、收藏/历史、标签会话、下载记录和播放历史都放在 SharedPreferences。
        browserPersistence = BrowserPersistenceAssemblyController(
            activity = this,
            filesDir = filesDir,
            standardTabStore = browserTabState.standardTabStore,
            browserShellUiController = browserShellUiController,
            browserFeatureStateController = browserFeatureStateController,
            browserUrlStateController = browserUrlStateController,
            browserSessionStateController = browserSessionStateController
        ).create()

        // 本地文件模块负责选择目录、读取文件列表，并把可播放文件交给浏览器或原生播放器。
        val localFileComponents = LocalFileAssemblyController(
            activity = this,
            preferenceStore = browserPersistence.preferenceStore,
            functionCenter = functionCenterController,
            logTag = RULE_LOG_TAG,
            showMainFunctionCenterPage = {
                functionCenterEntryController.showFunctionCenterRootPage()
            },
            pageActionsController = { pageActions.pageActionsController },
            closeFunctionCenter = { functionCenterEntryController.closeFunctionCenter() },
            currentSessionController = browserSessionStateController::currentSessionController,
            currentBrowserManager = {
                browserStandardWebViewHostController.currentBrowserManager()
            },
            updateAddressBar = { url -> browserAddressBarStateController.updateAddressBar(url) },
            hideKeyboard = browserKeyboardController::hideKeyboard,
            showHomeContent = browserShellUiController::showHomeContent
        ).create()
        localFilesController = localFileComponents.localFilesController
        localDocumentEntryController = localFileComponents.localDocumentEntryController

        // 搜索入口和地址建议拆成两个控制器：前者管理搜索引擎，后者管理输入提示列表。
        val browserSearchComponents = BrowserSearchAssemblyController(
            activity = this,
            providerScroll = views.searchProviderScroll,
            providerList = views.searchProviderList,
            addressInput = views.addressInput,
            addressProviderBadge = views.addressProviderBadge,
            addressSuggestionPanel = views.addressSuggestionPanel,
            settingsManager = browserPersistence.settingsManager,
            savedPageRepository = browserPersistence.savedPageRepository,
            siteSecurityController = {
                if (::siteSecurityController.isInitialized) siteSecurityController else null
            },
            dp = ::dp,
            isHomePageVisible = browserRuntimeStateController::isHomePageVisible,
            isPrivateBrowsingEnabled = browserFeatureStateController::isPrivateBrowsingEnabled,
            areBrowserControlsHidden = {
                ::browserControls.isInitialized && browserControls.browserControlsController.areHidden
            },
            isVideoFullscreenUiActive = browserRuntimeStateController::isVideoFullscreenUiActive,
            openProviderHome = { browserLaunchController.openHomePage() },
            openCustomShortcut = { url -> browserNavigationController.loadUrl(url) },
            openUrl = { url -> browserNavigationController.loadUrl(url) },
            searchKeyword = { keyword -> browserLaunchController.searchAddressKeyword(keyword) }
        ).create()
        searchProviderController = browserSearchComponents.searchProviderController
        browserAddressBarStateController = browserSearchComponents.browserAddressBarStateController
        historyRecordPolicy = browserSearchComponents.historyRecordPolicy
        addressSuggestionController = browserSearchComponents.addressSuggestionController
        webViewInteraction = BrowserWebViewInteractionAssemblyController(
            activity = this,
            setPrivateBrowsingActive = browserRuntimeStateController::setPrivateBrowsingActive,
            openUrlInNewTab = { url ->
                browserTabActionsController.openUrlInNewTab(url)
            },
            downloadUrl = { url, userAgent ->
                pageActions.downloadController.enqueue(
                    url = url,
                    userAgent = userAgent,
                    contentDisposition = null,
                    mimeType = null
                )
            },
            currentUserAgent = {
                browserStandardWebViewHostController.currentBrowserManager().userAgentString()
            },
            isShareableUrl = browserUrlStateController::isShareableUrl,
            attachBrowserControlsScrollIfReady = { activeWebView ->
                if (::browserControls.isInitialized) {
                    browserControls.browserControlsScrollController.attachToWebView(activeWebView)
                }
            },
            syncCurrentChromeClientIfReady =
                browserChromeClientStateController::syncCurrentChromeClientIfReady,
            updatePrivateBrowsingUi = browsingModeThemeController::updatePrivateBrowsingUi,
            syncSearchProviderVisibility = browserControlsShellController::syncSearchProviderVisibility,
            applyBrowsingModeTheme = browsingModeThemeController::applyBrowsingModeTheme,
            areBrowserSessionsInitialized =
                browserSessionStateController::areBrowserSessionsInitialized,
            currentSessionController = browserSessionStateController::currentSessionController
        ).create()

        // WebView 和标签页要先建好，后面的浏览器控制器才能拿到当前 activeWebView。
        browserStandardWebViewHostController = BrowserStandardWebViewHostAssemblyController(
            activity = this,
            views = views,
            standardTabStore = browserTabState.standardTabStore,
            configureLinkContextMenu = webViewInteraction.linkContextMenuController::configure,
            handleActiveWebViewChanged =
                webViewInteraction.browserActiveWebViewController::handleActiveWebViewChanged
        ).create()
        browserStandardWebViewHostController.setup()
        localDocumentEntryController.setupFileOperationLaunchers()

        // 规则引擎读取 assets/rules 和用户订阅缓存，供广告拦截、URL 清理、脚本注入使用。
        val navigationComponents = BrowserNavigationAssemblyController(
            activity = this,
            assets = assets,
            filesDir = filesDir,
            settingsManager = browserPersistence.settingsManager,
            addressInput = views.addressInput,
            standardTabStore = browserTabState.standardTabStore,
            browserStandardWebViewHostController = browserStandardWebViewHostController,
            browserSessionStateController = browserSessionStateController,
            browserUrlStateController = browserUrlStateController,
            browserFeatureStateController = browserFeatureStateController,
            browserAddressBarStateController = browserAddressBarStateController,
            browserKeyboardController = browserKeyboardController,
            browserShellUiController = browserShellUiController,
            browserChromeClientStateController = browserChromeClientStateController,
            addressSuggestionController = addressSuggestionController,
            searchProviderController = searchProviderController,
            closeFunctionCenter = { functionCenterEntryController.closeFunctionCenter() },
            defaultUserAgent = browserRuntimeStateController::defaultUserAgent
        ).create()
        ruleEngine = navigationComponents.ruleEngine
        externalNavigator = navigationComponents.externalNavigator
        nativePlayerEntryController = navigationComponents.nativePlayerEntryController
        browserNavigationController = navigationComponents.browserNavigationController
        browserLaunchController = navigationComponents.browserLaunchController
        browserDisplayModeController = navigationComponents.browserDisplayModeController

        // 下载控制器负责接收 WebView 下载回调，并把记录写入本地仓库。
        pageActions = BrowserPageActionAssemblyController(
            activity = this,
            downloadRecordRepository = browserPersistence.downloadRecordRepository,
            settingsManager = browserPersistence.settingsManager,
            savedPageRepository = browserPersistence.savedPageRepository,
            browserDefaultSettingsResetter = browserPersistence.browserDefaultSettingsResetter,
            browserStandardWebViewHostController = browserStandardWebViewHostController,
            browserSessionStateController = browserSessionStateController,
            browserUrlStateController = browserUrlStateController,
            historyRecordPolicy = historyRecordPolicy,
            nativePlayerEntryController = nativePlayerEntryController,
            localDocumentEntryController = localDocumentEntryController,
            browserFeatureStateController = browserFeatureStateController,
            switchPrivateBrowsing = { enabled ->
                privateBrowsingSwitchController.setPrivateBrowsingActive(enabled)
            },
            browserShellUiController = browserShellUiController,
            browsingModeThemeController = browsingModeThemeController,
            activityResultLaunchers = activityResultLaunchers,
            findInPageController = findInPageController,
            browserNavigationController = browserNavigationController,
            closeFunctionCenter = { functionCenterEntryController.closeFunctionCenter() },
            recreateActivity = { recreate() },
            dp = ::dp
        ).create()
        val webRequestComponents = BrowserWebRequestAssemblyController(
            activity = this,
            settingsManager = browserPersistence.settingsManager,
            sessionSitePermissionStore = sessionSitePermissionStore,
            browserFeatureStateController = browserFeatureStateController,
            activityResultLaunchers = activityResultLaunchers
        ).create()
        webFileChooserController = webRequestComponents.webFileChooserController
        webPermissionRequestController = webRequestComponents.webPermissionRequestController
        geolocationPermissionController = webRequestComponents.geolocationPermissionController

        browserControls = BrowserControlsAssemblyController(
            activity = this,
            views = views,
            savedPageRepository = browserPersistence.savedPageRepository,
            browserStandardWebViewHostController = browserStandardWebViewHostController,
            browserUrlStateController = browserUrlStateController,
            browserLaunchController = browserLaunchController,
            pageActionsController = pageActions.pageActionsController,
            browserControlsShellController = browserControlsShellController,
            isHomePageVisible = browserRuntimeStateController::isHomePageVisible,
            isVideoFullscreenUiActive = browserRuntimeStateController::isVideoFullscreenUiActive,
            onBack = { browserBackNavigationController.handleBrowserBack() },
            showFunctionCenter = { functionCenterEntryController.showFunctionCenter() },
            showProfilePage = { functionCenterEntryController.showProfilePage() },
            dp = ::dp
        ).create()

        val browserSessionComponents = BrowserSessionAssemblyController(
            activity = this,
            standardTabStore = browserTabState.standardTabStore,
            privateTabStore = browserTabState.privateTabStore,
            standardTabWebViews = browserStandardWebViewHostController.standardTabWebViews,
            browserSessionCoordinator = browserStandardWebViewHostController.sessionCoordinator,
            browserAddressBarStateController = browserAddressBarStateController,
            browserShellUiController = browserShellUiController,
            browserControlsController = browserControls.browserControlsController,
            browserControlsShellController = browserControlsShellController,
            pageActionsController = pageActions.pageActionsController,
            pageFeatureInjectionController = pageFeatureInjectionController,
            browsingModeThemeController = browsingModeThemeController,
            sessionSitePermissionStore = sessionSitePermissionStore,
            isPrivateBrowsingActive = browserRuntimeStateController::isPrivateBrowsingActive,
            clearElementPickerState = {
                if (::elementPickerController.isInitialized) {
                    elementPickerController.clearState()
                }
            },
            cancelElementPickerIfActive = {
                if (::elementPickerController.isInitialized && elementPickerController.isActive) {
                    elementPickerController.cancel()
                }
            },
            exitPageFullscreenIfNeeded = {
                browserFullscreenUiController.exitPageFullscreenIfNeeded()
            },
            closeFunctionCenter = { functionCenterEntryController.closeFunctionCenter() },
            openHomePage = browserLaunchController::openHomePage,
            loadUrl = browserNavigationController::loadUrl,
            createStandardTabWebView =
                browserStandardWebViewHostController::createStandardTabWebView,
            showStandardTabWebView =
                browserStandardWebViewHostController::showStandardTabWebView,
            hideStandardTabWebView =
                browserStandardWebViewHostController::hideStandardTabWebView,
            destroyStandardTabWebView =
                browserStandardWebViewHostController::destroyStandardTabWebView,
            saveStandardTabSession =
                browserPersistence.browserStandardTabSessionController::saveStandardTabSession,
            onStandardPageMetadataChanged = { url, title ->
                browserTabState.standardTabSessionBinding.handlePageMetadataChanged(url, title)
                browserPersistence.browserStandardTabSessionController.saveStandardTabSession()
            },
            onPrivatePageMetadataChanged =
                browserTabState.privateTabSessionBinding::handlePageMetadataChanged
        ).create()
        standardSessionController = browserSessionComponents.standardSessionController
        privateSessionController = browserSessionComponents.privateSessionController
        privateBrowsingSwitchController = browserSessionComponents.privateBrowsingSwitchController
        browserTabActionsController = browserSessionComponents.browserTabActionsController
        val browserClientComponents = BrowserClientAssemblyController(
            activity = this,
            fullscreenContainer = views.fullscreenContainer,
            decorView = window.decorView,
            webViewContainer = views.webViewContainer,
            standardTabStore = browserTabState.standardTabStore,
            standardTabWebViews = browserStandardWebViewHostController.standardTabWebViews,
            browserSessionCoordinator = browserStandardWebViewHostController.sessionCoordinator,
            standardSessionController = standardSessionController,
            privateSessionController = privateSessionController,
            browserManager = {
                browserStandardWebViewHostController.currentBrowserManager()
            },
            sessionController = browserSessionStateController::currentSessionController,
            currentPageUrl = {
                browserSessionStateController.currentSessionController().currentPageUrl
            },
            isPrivateBrowsingActive = browserRuntimeStateController::isPrivateBrowsingActive,
            createStandardTabWebView =
                browserStandardWebViewHostController::createStandardTabWebView,
            showStandardTabWebView =
                browserStandardWebViewHostController::showStandardTabWebView,
            saveStandardTabSession =
                browserPersistence.browserStandardTabSessionController::saveStandardTabSession,
            showBrowserErrorPage = { error ->
                browserWebClientController.showBrowserErrorPage(error)
            },
            resetBackExitConfirmation = {
                if (::browserBackNavigationController.isInitialized) {
                    browserBackNavigationController.resetBackExitConfirmation()
                }
            },
            clientCertificateController = pageActions.clientCertificateController,
            httpAuthController = pageActions.httpAuthController,
            adBlockRequestInterceptor = requestInterceptionProvider.adBlockRequestInterceptor,
            smartNoImageRequestInterceptor =
                requestInterceptionProvider.smartNoImageRequestInterceptor,
            browserNavigationController = browserNavigationController,
            closeFunctionCenter = { functionCenterEntryController.closeFunctionCenter() },
            closeTab = browserTabActionsController::closeTab,
            fullscreenChanged = { fullscreen ->
                browserFullscreenUiController.handleVideoFullscreenChanged(fullscreen)
            },
            webFileChooserController = webFileChooserController,
            webPermissionRequestController = webPermissionRequestController,
            geolocationPermissionController = geolocationPermissionController
        ).create()
        renderProcessRecoveryController = browserClientComponents.renderProcessRecoveryController
        browserWebClientController = browserClientComponents.browserWebClientController
        webWindowController = browserClientComponents.webWindowController
        browserChromeClientController = browserClientComponents.browserChromeClientController

        val browserFullscreenComponents = BrowserFullscreenAssemblyController(
            activity = this,
            rootView = views.rootView as ViewGroup,
            browserManager = {
                browserStandardWebViewHostController.currentBrowserManager()
            },
            settingsManager = { browserPersistence.settingsManager },
            browserChromeClientStateController = browserChromeClientStateController,
            browserControlsShellController = browserControlsShellController,
            browserDisplayModeController = browserDisplayModeController,
            browserFeatureStateController = browserFeatureStateController,
            dp = ::dp
        ).create()
        fullscreenVideoController = browserFullscreenComponents.fullscreenVideoController
        browserFullscreenUiController = browserFullscreenComponents.browserFullscreenUiController

        // 功能中心是底部弹出的工具面板。装配类负责把各控制器动作接入页面。
        functionCenterEntryController = FunctionCenterAssemblyController(
            activity = this,
            functionCenter = functionCenterController,
            settingsManager = browserPersistence.settingsManager,
            browserManager = {
                browserStandardWebViewHostController.currentBrowserManager()
            },
            browserManagers = {
                browserStandardWebViewHostController.browserManagers()
            },
            savedPageRepository = browserPersistence.savedPageRepository,
            downloadRecordRepository = browserPersistence.downloadRecordRepository,
            playbackHistoryRepository = browserPersistence.playbackHistoryRepository,
            adBlockLogger = requestInterceptionProvider.adBlockLogger,
            filesDir = filesDir,
            browserUrlStateController = browserUrlStateController,
            browserFeatureStateController = browserFeatureStateController,
            browserTabActionsController = browserTabActionsController,
            browserLaunchController = browserLaunchController,
            pageActionsController = pageActions.pageActionsController,
            browserPageToolEntryController = pageActions.browserPageToolEntryController,
            downloadController = pageActions.downloadController,
            activityResultLaunchers = activityResultLaunchers,
            searchProviderController = searchProviderController,
            localDocumentEntryController = localDocumentEntryController,
            startElementPicker = { elementPickerController.start() },
            browserDisplayModeController = browserDisplayModeController,
            pageFeatureInjectionController = pageFeatureInjectionController,
            browserNavigationController = browserNavigationController,
            hideKeyboard = browserKeyboardController::hideKeyboard,
            recreateActivity = { recreate() }
        ).createEntryController()

        siteSecurityController = BrowserSiteSecurityAssemblyController(
            activity = this,
            siteSecurityIcon = views.siteSecurityIcon,
            settingsManager = browserPersistence.settingsManager,
            browserSessionStateController = browserSessionStateController,
            browserStandardWebViewHostController = browserStandardWebViewHostController,
            browserFeatureStateController = browserFeatureStateController,
            browserUrlStateController = browserUrlStateController,
            showCurrentSiteSettingsPage = functionCenterEntryController::showCurrentSiteSettingsPage
        ).create()

        val browserPageFeatureComponents = BrowserPageFeatureAssemblyController(
            activity = this,
            assets = assets,
            settingsManager = browserPersistence.settingsManager,
            ruleEngine = ruleEngine,
            browserManager = {
                browserStandardWebViewHostController.currentBrowserManager()
            },
            browserSessionStateController = browserSessionStateController,
            browserUrlStateController = browserUrlStateController,
            browserFeatureStateController = browserFeatureStateController,
            pageFeatureInjectionController = pageFeatureInjectionController,
            browserChromeClientStateController = browserChromeClientStateController,
            fullscreenVideoController = fullscreenVideoController,
            webPlaybackHistoryRecorder = browserPersistence.webPlaybackHistoryRecorder,
            postToUi = { action -> runOnUiThread { action() } },
        ).create()
        jsInjector = browserPageFeatureComponents.jsInjector
        pageFeatureCoordinator = browserPageFeatureComponents.pageFeatureCoordinator
        elementPickerController = browserPageFeatureComponents.elementPickerController
        nativeBridgeController = browserPageFeatureComponents.nativeBridgeController
        browserBackNavigationController = BrowserBackNavigationAssemblyController(
            activity = this,
            browserManager = {
                browserStandardWebViewHostController.currentBrowserManager()
            },
            currentChromeClient = browserChromeClientStateController::currentChromeClientOrNull,
            handleFunctionCenterBack = functionCenterEntryController::handleFunctionCenterBack,
            isElementPickerActive = { elementPickerController.isActive },
            cancelElementPicker = elementPickerController::cancel,
            updateNavigationButtons = browserShellUiController::updateNavigationButtons
        ).create()

        BrowserStartupControllerAssembly(
            rootView = views.rootView,
            isVideoFullscreenUiActive = browserRuntimeStateController::isVideoFullscreenUiActive,
            browserControlsShellController = browserControlsShellController,
            addressSuggestionController = addressSuggestionController,
            browsingModeThemeController = browsingModeThemeController,
            browserShellUiController = browserShellUiController,
            browserBackNavigationController = browserBackNavigationController,
            browserStandardWebViewHostController = browserStandardWebViewHostController,
            settingsManager = browserPersistence.settingsManager,
            setDefaultUserAgent = browserRuntimeStateController::setDefaultUserAgent,
            browserDisplayModeController = browserDisplayModeController,
            downloadController = pageActions.downloadController,
            browserChromeClientController = browserChromeClientController,
            browserFullscreenUiController = browserFullscreenUiController,
            nativeBridgeController = nativeBridgeController,
            nativeBridgeName = NATIVE_BRIDGE_NAME,
            browserWebClientController = browserWebClientController,
            browserLaunchController = browserLaunchController
        ).start(intent)
    }

    /**
     * 函数 `onNewIntent`：处理 `on New Intent` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param intent 参数类型为 `Intent`，表示函数执行 `intent` 相关逻辑时需要读取或处理的输入。
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        browserActivityLifecycleController.handleNewIntent(
            intent = intent,
            setActivityIntent = { newIntent -> setIntent(newIntent) }
        )
    }

    /**
     * 函数 `onPause`：处理 `on Pause` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    override fun onPause() {
        browserActivityLifecycleController.handlePause()
        super.onPause()
    }

    /**
     * 函数 `onResume`：处理 `on Resume` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    override fun onResume() {
        super.onResume()
        browserActivityLifecycleController.handleResume()
    }

    /**
     * 函数 `dispatchKeyEvent`：封装 `dispatch Key Event` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param event 参数类型为 `KeyEvent`，表示函数执行 `event` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            browserRuntimeStateController.wakeVideoFullscreenControlsIfActive()
        }
        return super.dispatchKeyEvent(event)
    }

    /**
     * 函数 `onDestroy`：处理 `on Destroy` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    override fun onDestroy() {
        browserActivityLifecycleController.handleDestroy()
        super.onDestroy()
    }

    // endregion

    // region WebView、ChromeClient 和 BrowserClient 组装
    // 这一组函数负责创建 WebView、绑定 WebChromeClient/WebViewClient，
    // 并处理网页弹窗、新窗口、渲染进程退出、证书和 HTTP 认证等浏览器外壳能力。
    // endregion

    // region 地址解析、页面加载和站点安全提示
    // 地址栏输入先被解析为 URL 或搜索词；真正加载前还会经过媒体路由、HTTP 降级确认和规则清理。
    // endregion

    // region 小工具函数和 WebView 跳转拦截
    // 这里放跨多个小流程复用的辅助函数，例如 dp 转换、键盘隐藏、URL 类型判断和 shouldOverrideUrlLoading 判断。
    /**
     * 函数 `dp`：封装 `dp` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param value 参数类型为 `Int`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    // endregion

    companion object {
        // 所有只在 MainActivity 内使用的常量集中放在 companion object，避免魔法数字散落在函数里。
        private const val NATIVE_BRIDGE_NAME = "VideoBrowserNative"
        private const val RULE_LOG_TAG = "VideoBrowserRules"
    }
}
