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
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.videobrowser.adblock.AdBlockManager
import com.example.videobrowser.adblock.AdBlockLogger
import com.example.videobrowser.adblock.AdBlockRequestInterceptor
import com.example.videobrowser.browser.BrowserActiveWebViewController
import com.example.videobrowser.browser.BrowserActivityLifecycleController
import com.example.videobrowser.browser.BrowserActivityResultLaunchers
import com.example.videobrowser.browser.BrowserAddressBarStateController
import com.example.videobrowser.browser.BrowserBackNavigationController
import com.example.videobrowser.browser.BrowserChromeClientController
import com.example.videobrowser.browser.BrowserChromeClientStateController
import com.example.videobrowser.browser.BrowserControlsAssemblyController
import com.example.videobrowser.browser.BrowserFeatureStateController
import com.example.videobrowser.browser.BrowserControlsController
import com.example.videobrowser.browser.BrowserControlsShellController
import com.example.videobrowser.browser.BrowserControlsScrollController
import com.example.videobrowser.browser.BrowserDisplayModeController
import com.example.videobrowser.browser.BrowserFullscreenUiController
import com.example.videobrowser.browser.BrowserKeyboardController
import com.example.videobrowser.browser.BrowserUrlStateController
import com.example.videobrowser.browser.BrowserExternalNavigator
import com.example.videobrowser.browser.HistoryRecordPolicy
import com.example.videobrowser.browser.BrowserLaunchController
import com.example.videobrowser.browser.BrowserNavigationController
import com.example.videobrowser.browser.BrowserNavigationAssemblyController
import com.example.videobrowser.browser.BrowserPageActionAssemblyController
import com.example.videobrowser.browser.BrowserPageToolEntryController
import com.example.videobrowser.browser.BrowserSessionController
import com.example.videobrowser.browser.BrowserSessionStateController
import com.example.videobrowser.browser.BrowserShellUiController
import com.example.videobrowser.browser.BrowserStandardTabSessionController
import com.example.videobrowser.browser.BrowserStandardWebViewHostController
import com.example.videobrowser.browser.BrowserStartupController
import com.example.videobrowser.browser.BrowserTabActionsController
import com.example.videobrowser.browser.BrowserTabSessionRepository
import com.example.videobrowser.browser.BrowserTabSessionBinding
import com.example.videobrowser.browser.BrowserTabStore
import com.example.videobrowser.browser.BrowserWebClientController
import com.example.videobrowser.browser.BrowserWebViewDebugController
import com.example.videobrowser.browser.BrowserWebViewInteractionAssemblyController
import com.example.videobrowser.browser.BrowserWebRequestAssemblyController
import com.example.videobrowser.browser.BrowserWindowInsetsController
import com.example.videobrowser.browser.BrowsingModeThemeController
import com.example.videobrowser.browser.ClientCertificateController
import com.example.videobrowser.browser.FindInPageController
import com.example.videobrowser.browser.FindInPageDialogController
import com.example.videobrowser.browser.GeolocationPermissionController
import com.example.videobrowser.browser.HttpAuthController
import com.example.videobrowser.browser.LinkContextMenuController
import com.example.videobrowser.browser.PageActionsController
import com.example.videobrowser.browser.PageArchiveController
import com.example.videobrowser.browser.PagePrintController
import com.example.videobrowser.browser.PrivateBrowsingSwitchController
import com.example.videobrowser.browser.RenderProcessRecoveryController
import com.example.videobrowser.browser.SiteSecurityController
import com.example.videobrowser.browser.SmartNoImageRequestInterceptor
import com.example.videobrowser.browser.VideoBrowserNativeBridgeController
import com.example.videobrowser.browser.WebFileChooserController
import com.example.videobrowser.browser.WebWindowController
import com.example.videobrowser.browser.WebPermissionRequestController
import com.example.videobrowser.browser.search.AddressSuggestionController
import com.example.videobrowser.browser.search.BrowserSearchAssemblyController
import com.example.videobrowser.browser.search.SearchProviderController
import com.example.videobrowser.download.DownloadController
import com.example.videobrowser.download.DownloadRecordRepository
import com.example.videobrowser.element.ElementPickerController
import com.example.videobrowser.functioncenter.FunctionCenterAssemblyController
import com.example.videobrowser.functioncenter.FunctionCenterController
import com.example.videobrowser.functioncenter.FunctionCenterEntryController
import com.example.videobrowser.inject.JsInjector
import com.example.videobrowser.inject.PageFeatureInjectionController
import com.example.videobrowser.inject.PageFeatureCoordinator
import com.example.videobrowser.inject.ScriptLoader
import com.example.videobrowser.localfiles.LocalFileAssemblyController
import com.example.videobrowser.localfiles.LocalDocumentEntryController
import com.example.videobrowser.localfiles.LocalFilesController
import com.example.videobrowser.rules.RuleEngine
import com.example.videobrowser.settings.BrowserDefaultSettingsResetter
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.settings.SessionSitePermissionStore
import com.example.videobrowser.storage.BookmarkImportExportController
import com.example.videobrowser.storage.BrowserPersistenceAssemblyController
import com.example.videobrowser.storage.PreferenceStore
import com.example.videobrowser.storage.SavedPageRepository
import com.example.videobrowser.video.FullscreenVideoController
import com.example.videobrowser.video.NativePlayerEntryController
import com.example.videobrowser.video.PlaybackHistoryRepository
import com.example.videobrowser.video.WebPlaybackHistoryRecorder

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
    // 下面这些只读属性是为了让主流程代码更短，例如直接写 addressInput 而不是 views.addressInput。
    private lateinit var views: MainActivityViews
    private val rootView: View get() = views.rootView
    private val topBar: View get() = views.topBar
    private val bottomBar: ConstraintLayout get() = views.bottomBar
    private val addressInput: EditText get() = views.addressInput
    private val pageProgress: ProgressBar get() = views.pageProgress
    private val addressSuggestionPanel: LinearLayout get() = views.addressSuggestionPanel
    private val searchProviderScroll: HorizontalScrollView get() = views.searchProviderScroll
    private val searchProviderList: LinearLayout get() = views.searchProviderList
    private val webViewContainer: FrameLayout get() = views.webViewContainer
    private val pageToolsButton: ImageButton get() = views.pageToolsButton
    private val wenxinButton: ImageButton get() = views.wenxinButton
    private val profileButton: ImageButton get() = views.profileButton
    private val backButton: ImageButton get() = views.backButton
    private val refreshButton: ImageButton get() = views.refreshButton
    private val bookmarkButton: ImageButton get() = views.bookmarkButton
    private val loadButton: ImageButton get() = views.loadButton
    private val siteSecurityIcon: ImageView get() = views.siteSecurityIcon
    private val fullscreenContainer: FrameLayout get() = views.fullscreenContainer
    // endregion

    // region 应用级控制器和仓库
    // Repository 负责读写本机数据；Controller 负责连接 UI、WebView 和业务动作。
    // 这些 lateinit 属性会在 onCreate() 里按依赖顺序初始化。
    private lateinit var preferenceStore: PreferenceStore
    private lateinit var settingsManager: SettingsManager
    private lateinit var browserDefaultSettingsResetter: BrowserDefaultSettingsResetter
    private lateinit var savedPageRepository: SavedPageRepository
    private lateinit var bookmarkImportExportController: BookmarkImportExportController
    private lateinit var browserFeatureStateController: BrowserFeatureStateController
    private lateinit var browserUrlStateController: BrowserUrlStateController
    private lateinit var downloadRecordRepository: DownloadRecordRepository
    private lateinit var playbackHistoryRepository: PlaybackHistoryRepository
    private lateinit var webPlaybackHistoryRecorder: WebPlaybackHistoryRecorder
    private lateinit var ruleEngine: RuleEngine
    private lateinit var browserStandardWebViewHostController: BrowserStandardWebViewHostController
    private lateinit var browserControlsController: BrowserControlsController
    private lateinit var browserControlsShellController: BrowserControlsShellController
    private lateinit var browserControlsScrollController: BrowserControlsScrollController
    private lateinit var standardSessionController: BrowserSessionController
    private lateinit var privateSessionController: BrowserSessionController
    private lateinit var browserTabSessionRepository: BrowserTabSessionRepository
    private lateinit var browserStandardTabSessionController: BrowserStandardTabSessionController
    private lateinit var functionCenterController: FunctionCenterController
    private lateinit var functionCenterEntryController: FunctionCenterEntryController
    private lateinit var localFilesController: LocalFilesController
    private lateinit var localDocumentEntryController: LocalDocumentEntryController
    private lateinit var pageActionsController: PageActionsController
    private lateinit var httpAuthController: HttpAuthController
    private lateinit var clientCertificateController: ClientCertificateController
    private lateinit var renderProcessRecoveryController: RenderProcessRecoveryController
    private lateinit var webWindowController: WebWindowController
    private lateinit var linkContextMenuController: LinkContextMenuController
    private lateinit var findInPageDialogController: FindInPageDialogController
    private lateinit var historyRecordPolicy: HistoryRecordPolicy
    private lateinit var searchProviderController: SearchProviderController
    private lateinit var browserAddressBarStateController: BrowserAddressBarStateController
    private lateinit var addressSuggestionController: AddressSuggestionController
    private lateinit var browserLaunchController: BrowserLaunchController
    private lateinit var browserTabActionsController: BrowserTabActionsController
    private lateinit var privateBrowsingSwitchController: PrivateBrowsingSwitchController
    private lateinit var downloadController: DownloadController
    private lateinit var siteSecurityController: SiteSecurityController
    private lateinit var pageArchiveController: PageArchiveController
    private lateinit var pagePrintController: PagePrintController
    private lateinit var browserNavigationController: BrowserNavigationController
    private lateinit var browserPageToolEntryController: BrowserPageToolEntryController
    private lateinit var browserDisplayModeController: BrowserDisplayModeController
    private lateinit var browsingModeThemeController: BrowsingModeThemeController
    private lateinit var browserShellUiController: BrowserShellUiController
    private lateinit var browserBackNavigationController: BrowserBackNavigationController
    private lateinit var browserKeyboardController: BrowserKeyboardController
    private lateinit var nativeBridgeController: VideoBrowserNativeBridgeController
    private lateinit var fullscreenVideoController: FullscreenVideoController
    private lateinit var browserFullscreenUiController: BrowserFullscreenUiController
    private lateinit var browserActiveWebViewController: BrowserActiveWebViewController
    private lateinit var webFileChooserController: WebFileChooserController
    private lateinit var webPermissionRequestController: WebPermissionRequestController
    private lateinit var geolocationPermissionController: GeolocationPermissionController
    private lateinit var elementPickerController: ElementPickerController
    private lateinit var jsInjector: JsInjector
    private lateinit var pageFeatureInjectionController: PageFeatureInjectionController
    private lateinit var pageFeatureCoordinator: PageFeatureCoordinator
    private lateinit var browserChromeClientController: BrowserChromeClientController
    private val browserChromeClientStateController = BrowserChromeClientStateController(
        browserChromeClientController = {
            if (::browserChromeClientController.isInitialized) {
                browserChromeClientController
            } else {
                null
            }
        }
    )
    private lateinit var browserWebClientController: BrowserWebClientController
    private lateinit var externalNavigator: BrowserExternalNavigator
    private lateinit var nativePlayerEntryController: NativePlayerEntryController
    private val browserActivityLifecycleController = BrowserActivityLifecycleController(
        browserChromeClientController = {
            if (::browserChromeClientController.isInitialized) browserChromeClientController else null
        },
        browserWebClientController = {
            if (::browserWebClientController.isInitialized) browserWebClientController else null
        },
        pageArchiveController = {
            if (::pageArchiveController.isInitialized) pageArchiveController else null
        },
        addressSuggestionController = {
            if (::addressSuggestionController.isInitialized) addressSuggestionController else null
        },
        downloadController = {
            if (::downloadController.isInitialized) downloadController else null
        },
        elementPickerController = {
            if (::elementPickerController.isInitialized) elementPickerController else null
        },
        functionCenterEntryController = {
            if (::functionCenterEntryController.isInitialized) functionCenterEntryController else null
        },
        browserChromeClientStateController = browserChromeClientStateController,
        browserStandardTabSessionController = {
            if (::browserStandardTabSessionController.isInitialized) {
                browserStandardTabSessionController
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
    )
    // endregion

    // region 标签页与会话状态
    // 标准模式和无痕模式各有自己的标签页列表，避免无痕页面写入普通会话。
    private val standardTabStore = BrowserTabStore()
    private val privateTabStore = BrowserTabStore()
    private val standardTabSessionBinding = BrowserTabSessionBinding(standardTabStore)
    private val privateTabSessionBinding = BrowserTabSessionBinding(privateTabStore)
    private val findInPageController = FindInPageController(
        findAll = { query ->
            browserStandardWebViewHostController.currentBrowserManager().findAllAsync(query)
        },
        findNext = { forward ->
            browserStandardWebViewHostController.currentBrowserManager().findNext(forward)
        },
        clearMatches = {
            browserStandardWebViewHostController.currentBrowserManager().clearFindMatches()
        }
    )
    // endregion

    // region 网页内容增强和拦截
    // 广告拦截、无图模式和 JS 注入都依赖当前站点、用户设置和规则引擎。
    private val adBlockLogger = AdBlockLogger()
    private val adBlockManager: AdBlockManager by lazy {
        AdBlockManager(
            isEnabled = browserFeatureStateController::isAdBlockEnabled,
            isDisabledForCurrentSite = browserFeatureStateController::isCurrentSiteAdBlockDisabled,
            isUserWhitelistedRequestHost = settingsManager::isUserWhitelistedSite,
            currentPageUrl = {
                browserSessionStateController.currentSessionController().currentPageUrl
            },
            currentPageHost = browserUrlStateController::currentSiteHost,
            logger = adBlockLogger,
            ruleEngine = ruleEngine
        )
    }
    private val adBlockRequestInterceptor: AdBlockRequestInterceptor by lazy {
        AdBlockRequestInterceptor(adBlockManager)
    }
    private val smartNoImageRequestInterceptor: SmartNoImageRequestInterceptor by lazy {
        SmartNoImageRequestInterceptor(
            isEnabled = browserFeatureStateController::isSmartNoImageEnabled,
            isDisabledForCurrentSite =
                browserFeatureStateController::isCurrentSiteSmartNoImageDisabled,
            currentPageUrl = {
                browserSessionStateController.currentSessionController().currentPageUrl
            }
        )
    }
    // endregion

    // region Android 系统交互状态
    // 这些字段保存系统弹窗或系统 Activity 返回前的临时状态，例如文件选择、权限申请、证书选择。
    private val activityResultLaunchers = BrowserActivityResultLaunchers(
        activity = this,
        webFileChooserController = {
            if (::webFileChooserController.isInitialized) webFileChooserController else null
        },
        bookmarkImportExportController = {
            if (::bookmarkImportExportController.isInitialized) bookmarkImportExportController else null
        },
        pageArchiveController = {
            if (::pageArchiveController.isInitialized) pageArchiveController else null
        },
        webPermissionRequestController = {
            if (::webPermissionRequestController.isInitialized) webPermissionRequestController else null
        },
        geolocationPermissionController = {
            if (::geolocationPermissionController.isInitialized) geolocationPermissionController else null
        }
    )
    private val sessionSitePermissionStore = SessionSitePermissionStore()
    // endregion

    // region 当前页面运行状态
    private var privateBrowsingActive = false
    private val browserSessionStateController = BrowserSessionStateController(
        isPrivateBrowsingActive = { privateBrowsingActive },
        standardSessionController = {
            if (::standardSessionController.isInitialized) standardSessionController else null
        },
        privateSessionController = {
            if (::privateSessionController.isInitialized) privateSessionController else null
        }
    )
    private val isHomePageVisible: Boolean
        get() = browserSessionStateController.currentSessionController().isHomePageVisible
    private val isVideoFullscreenUiActive: Boolean
        get() = ::fullscreenVideoController.isInitialized &&
            fullscreenVideoController.isFullscreenUiActive
    private var defaultUserAgent: String? = null
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
        browserKeyboardController = BrowserKeyboardController(
            context = this,
            addressInput = addressInput,
            addressSuggestionController = {
                if (::addressSuggestionController.isInitialized) addressSuggestionController else null
            }
        )
        pageFeatureInjectionController = PageFeatureInjectionController(
            pageFeatureCoordinator = {
                if (::pageFeatureCoordinator.isInitialized) pageFeatureCoordinator else null
            }
        )
        functionCenterController = FunctionCenterController(this, rootView, ::dp)
        browserFeatureStateController = BrowserFeatureStateController(
            settingsManager = { settingsManager },
            pageFeatureCoordinator = { pageFeatureCoordinator },
            isPrivateBrowsingActive = { privateBrowsingActive }
        )
        browserUrlStateController = BrowserUrlStateController(
            currentPageUrl = {
                if (browserSessionStateController.areBrowserSessionsInitialized()) {
                    browserSessionStateController.currentSessionController().currentPageUrl
                } else {
                    null
                }
            },
            currentWebViewUrl = {
                if (browserSessionStateController.areBrowserSessionsInitialized()) {
                    browserStandardWebViewHostController.currentBrowserManager().currentUrl()
                } else {
                    null
                }
            }
        )
        browserControlsShellController = BrowserControlsShellController(
            browserControlsController = {
                if (::browserControlsController.isInitialized) browserControlsController else null
            },
            browserControlsScrollController = {
                if (::browserControlsScrollController.isInitialized) {
                    browserControlsScrollController
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
            isPageLoading = {
                if (browserSessionStateController.areBrowserSessionsInitialized()) {
                    browserSessionStateController.currentSessionController().isPageLoading
                } else {
                    false
                }
            },
            isVideoFullscreenUiActive = { isVideoFullscreenUiActive },
            isHomePageVisible = { isHomePageVisible }
        )
        browsingModeThemeController = BrowsingModeThemeController(
            activity = this,
            views = views,
            isPrivateBrowsingEnabled = browserFeatureStateController::isPrivateBrowsingEnabled,
            currentPageUrl = {
                if (browserSessionStateController.areBrowserSessionsInitialized()) {
                    browserSessionStateController.currentSessionController().currentPageUrl
                } else {
                    null
                }
            },
            updateSiteSecurityStatus = { url ->
                if (::siteSecurityController.isInitialized) {
                    siteSecurityController.updateStatus(url)
                }
            },
            dp = ::dp
        )
        browserShellUiController = BrowserShellUiController(
            browserControlsController = {
                if (::browserControlsController.isInitialized) browserControlsController else null
            },
            siteSecurityController = {
                if (::siteSecurityController.isInitialized) siteSecurityController else null
            },
            browserControlsScrollController = {
                if (::browserControlsScrollController.isInitialized) {
                    browserControlsScrollController
                } else {
                    null
                }
            },
            browserControlsShellController = browserControlsShellController,
            activeWebView = {
                browserStandardWebViewHostController.currentBrowserManager().activeWebView
            },
            browsingModeThemeController = browsingModeThemeController
        )

        // 本地持久化层：设置、收藏/历史、标签会话、下载记录和播放历史都放在 SharedPreferences。
        val persistenceComponents = BrowserPersistenceAssemblyController(
            activity = this,
            filesDir = filesDir,
            standardTabStore = standardTabStore,
            browserShellUiController = browserShellUiController,
            browserFeatureStateController = browserFeatureStateController,
            browserUrlStateController = browserUrlStateController,
            browserSessionStateController = browserSessionStateController
        ).create()
        preferenceStore = persistenceComponents.preferenceStore
        settingsManager = persistenceComponents.settingsManager
        savedPageRepository = persistenceComponents.savedPageRepository
        bookmarkImportExportController = persistenceComponents.bookmarkImportExportController
        browserTabSessionRepository = persistenceComponents.browserTabSessionRepository
        browserStandardTabSessionController = persistenceComponents.browserStandardTabSessionController
        downloadRecordRepository = persistenceComponents.downloadRecordRepository
        playbackHistoryRepository = persistenceComponents.playbackHistoryRepository
        webPlaybackHistoryRecorder = persistenceComponents.webPlaybackHistoryRecorder
        browserDefaultSettingsResetter = persistenceComponents.browserDefaultSettingsResetter

        // 本地文件模块负责选择目录、读取文件列表，并把可播放文件交给浏览器或原生播放器。
        val localFileComponents = LocalFileAssemblyController(
            activity = this,
            preferenceStore = preferenceStore,
            functionCenter = functionCenterController,
            logTag = RULE_LOG_TAG,
            showMainFunctionCenterPage = {
                functionCenterEntryController.showFunctionCenterRootPage()
            },
            pageActionsController = { pageActionsController },
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
            providerScroll = searchProviderScroll,
            providerList = searchProviderList,
            addressInput = addressInput,
            addressProviderBadge = views.addressProviderBadge,
            addressSuggestionPanel = addressSuggestionPanel,
            settingsManager = settingsManager,
            savedPageRepository = savedPageRepository,
            siteSecurityController = {
                if (::siteSecurityController.isInitialized) siteSecurityController else null
            },
            dp = ::dp,
            isHomePageVisible = { isHomePageVisible },
            isPrivateBrowsingEnabled = browserFeatureStateController::isPrivateBrowsingEnabled,
            areBrowserControlsHidden = {
                ::browserControlsController.isInitialized && browserControlsController.areHidden
            },
            isVideoFullscreenUiActive = { isVideoFullscreenUiActive },
            openProviderHome = { browserLaunchController.openHomePage() },
            openCustomShortcut = { url -> browserNavigationController.loadUrl(url) },
            openUrl = { url -> browserNavigationController.loadUrl(url) },
            searchKeyword = { keyword -> browserLaunchController.searchAddressKeyword(keyword) }
        ).create()
        searchProviderController = browserSearchComponents.searchProviderController
        browserAddressBarStateController = browserSearchComponents.browserAddressBarStateController
        historyRecordPolicy = browserSearchComponents.historyRecordPolicy
        addressSuggestionController = browserSearchComponents.addressSuggestionController
        val webViewInteractionComponents = BrowserWebViewInteractionAssemblyController(
            activity = this,
            setPrivateBrowsingActive = { active -> privateBrowsingActive = active },
            openUrlInNewTab = { url ->
                browserTabActionsController.openUrlInNewTab(url)
            },
            downloadUrl = { url, userAgent ->
                downloadController.enqueue(
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
                if (::browserControlsScrollController.isInitialized) {
                    browserControlsScrollController.attachToWebView(activeWebView)
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
        linkContextMenuController = webViewInteractionComponents.linkContextMenuController
        browserActiveWebViewController = webViewInteractionComponents.browserActiveWebViewController

        // WebView 和标签页要先建好，后面的浏览器控制器才能拿到当前 activeWebView。
        browserStandardWebViewHostController = BrowserStandardWebViewHostController(
            activity = this,
            webViewContainer = webViewContainer,
            standardTabStore = standardTabStore,
            initialStandardWebView = views.webView,
            configureLinkContextMenu = linkContextMenuController::configure,
            handleActiveWebViewChanged =
                browserActiveWebViewController::handleActiveWebViewChanged
        )
        browserStandardWebViewHostController.setup()
        localDocumentEntryController.setupFileOperationLaunchers()

        // 规则引擎读取 assets/rules 和用户订阅缓存，供广告拦截、URL 清理、脚本注入使用。
        val navigationComponents = BrowserNavigationAssemblyController(
            activity = this,
            assets = assets,
            filesDir = filesDir,
            settingsManager = settingsManager,
            addressInput = addressInput,
            standardTabStore = standardTabStore,
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
            defaultUserAgent = { defaultUserAgent }
        ).create()
        ruleEngine = navigationComponents.ruleEngine
        externalNavigator = navigationComponents.externalNavigator
        nativePlayerEntryController = navigationComponents.nativePlayerEntryController
        browserNavigationController = navigationComponents.browserNavigationController
        browserLaunchController = navigationComponents.browserLaunchController
        browserDisplayModeController = navigationComponents.browserDisplayModeController

        // 下载控制器负责接收 WebView 下载回调，并把记录写入本地仓库。
        val pageActionComponents = BrowserPageActionAssemblyController(
            activity = this,
            downloadRecordRepository = downloadRecordRepository,
            settingsManager = settingsManager,
            savedPageRepository = savedPageRepository,
            browserDefaultSettingsResetter = browserDefaultSettingsResetter,
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
        downloadController = pageActionComponents.downloadController
        pageActionsController = pageActionComponents.pageActionsController
        httpAuthController = pageActionComponents.httpAuthController
        clientCertificateController = pageActionComponents.clientCertificateController
        pageArchiveController = pageActionComponents.pageArchiveController
        pagePrintController = pageActionComponents.pagePrintController
        findInPageDialogController = pageActionComponents.findInPageDialogController
        browserPageToolEntryController = pageActionComponents.browserPageToolEntryController
        val webRequestComponents = BrowserWebRequestAssemblyController(
            activity = this,
            settingsManager = settingsManager,
            sessionSitePermissionStore = sessionSitePermissionStore,
            browserFeatureStateController = browserFeatureStateController,
            activityResultLaunchers = activityResultLaunchers
        ).create()
        webFileChooserController = webRequestComponents.webFileChooserController
        webPermissionRequestController = webRequestComponents.webPermissionRequestController
        geolocationPermissionController = webRequestComponents.geolocationPermissionController

        val browserControlsComponents = BrowserControlsAssemblyController(
            activity = this,
            views = views,
            savedPageRepository = savedPageRepository,
            browserStandardWebViewHostController = browserStandardWebViewHostController,
            browserUrlStateController = browserUrlStateController,
            browserLaunchController = browserLaunchController,
            pageActionsController = pageActionsController,
            browserControlsShellController = browserControlsShellController,
            isHomePageVisible = { isHomePageVisible },
            isVideoFullscreenUiActive = { isVideoFullscreenUiActive },
            onBack = { browserBackNavigationController.handleBrowserBack() },
            showFunctionCenter = { functionCenterEntryController.showFunctionCenter() },
            showProfilePage = { functionCenterEntryController.showProfilePage() },
            dp = ::dp
        ).create()
        browserControlsController = browserControlsComponents.browserControlsController
        browserControlsScrollController = browserControlsComponents.browserControlsScrollController

        // 标准会话会写历史；无痕会话不写历史，并使用独立 WebView。
        standardSessionController = BrowserSessionController(
            activity = this,
            isActive = { !privateBrowsingActive },
            clearElementPickerState = {
                if (::elementPickerController.isInitialized) {
                    elementPickerController.clearState()
                }
            },
            exitPageFullscreenIfNeeded = {
                browserFullscreenUiController.exitPageFullscreenIfNeeded()
            },
            isProviderHomeUrl = browserAddressBarStateController::isProviderHomeUrl,
            updateAddressBar = browserAddressBarStateController::updateAddressBar,
            showHomeContent = browserShellUiController::showHomeContent,
            setPageProgress = browserControlsController::setProgress,
            updatePageProgressVisibility = browserControlsShellController::updatePageProgressVisibility,
            updateNavigationButtons = browserShellUiController::updateNavigationButtons,
            addHistoryEntry = pageActionsController::addHistoryEntry,
            injectPageFeatures = pageFeatureInjectionController::injectPageFeatures,
            onPageMetadataChanged = { url, title ->
                standardTabSessionBinding.handlePageMetadataChanged(url, title)
                browserStandardTabSessionController.saveStandardTabSession()
            }
        )
        privateSessionController = BrowserSessionController(
            activity = this,
            isActive = { privateBrowsingActive },
            clearElementPickerState = {
                if (::elementPickerController.isInitialized) {
                    elementPickerController.clearState()
                }
            },
            exitPageFullscreenIfNeeded = {
                browserFullscreenUiController.exitPageFullscreenIfNeeded()
            },
            isProviderHomeUrl = browserAddressBarStateController::isProviderHomeUrl,
            updateAddressBar = browserAddressBarStateController::updateAddressBar,
            showHomeContent = browserShellUiController::showHomeContent,
            setPageProgress = browserControlsController::setProgress,
            updatePageProgressVisibility = browserControlsShellController::updatePageProgressVisibility,
            updateNavigationButtons = browserShellUiController::updateNavigationButtons,
            addHistoryEntry = {},
            injectPageFeatures = pageFeatureInjectionController::injectPageFeatures,
            onPageMetadataChanged = privateTabSessionBinding::handlePageMetadataChanged
        )
        privateBrowsingSwitchController = PrivateBrowsingSwitchController(
            activity = this,
            isPrivateBrowsingActive = { privateBrowsingActive },
            closeFunctionCenter = { functionCenterEntryController.closeFunctionCenter() },
            cancelElementPickerIfActive = {
                if (::elementPickerController.isInitialized && elementPickerController.isActive) {
                    elementPickerController.cancel()
                }
            },
            exitPageFullscreenIfNeeded = {
                browserFullscreenUiController.exitPageFullscreenIfNeeded()
            },
            sessionSitePermissionStore = sessionSitePermissionStore,
            browserSessionCoordinator = browserStandardWebViewHostController.sessionCoordinator,
            privateSessionController = privateSessionController,
            standardSessionController = standardSessionController,
            openHomePage = browserLaunchController::openHomePage,
            updatePrivateBrowsingUi = browsingModeThemeController::updatePrivateBrowsingUi,
            updateNavigationButtons = browserShellUiController::updateNavigationButtons
        )
        browserTabActionsController = BrowserTabActionsController(
            standardTabStore = standardTabStore,
            privateTabStore = privateTabStore,
            standardTabWebViews = browserStandardWebViewHostController.standardTabWebViews,
            standardSessionController = standardSessionController,
            isPrivateBrowsingActive = { privateBrowsingActive },
            createStandardTabWebView =
                browserStandardWebViewHostController::createStandardTabWebView,
            showStandardTabWebView =
                browserStandardWebViewHostController::showStandardTabWebView,
            hideStandardTabWebView =
                browserStandardWebViewHostController::hideStandardTabWebView,
            destroyStandardTabWebView =
                browserStandardWebViewHostController::destroyStandardTabWebView,
            closeFunctionCenter = { functionCenterEntryController.closeFunctionCenter() },
            saveStandardTabSession = browserStandardTabSessionController::saveStandardTabSession,
            loadUrl = browserNavigationController::loadUrl,
            openHomePage = browserLaunchController::openHomePage
        )
        renderProcessRecoveryController = RenderProcessRecoveryController(
            webViewContainer = webViewContainer,
            sessionCoordinator = browserStandardWebViewHostController.sessionCoordinator,
            standardTabWebViews = browserStandardWebViewHostController.standardTabWebViews,
            currentPageUrl = {
                browserSessionStateController.currentSessionController().currentPageUrl
            },
            isPrivateBrowsingActive = { privateBrowsingActive },
            createStandardTabWebView =
                browserStandardWebViewHostController::createStandardTabWebView,
            showStandardTabWebView =
                browserStandardWebViewHostController::showStandardTabWebView,
            saveStandardTabSession = browserStandardTabSessionController::saveStandardTabSession,
            showBrowserErrorPage = { error ->
                browserWebClientController.showBrowserErrorPage(error)
            }
        )
        browserWebClientController = BrowserWebClientController(
            browserManager = {
                browserStandardWebViewHostController.currentBrowserManager()
            },
            sessionController = browserSessionStateController::currentSessionController,
            resetBackExitConfirmation = {
                if (::browserBackNavigationController.isInitialized) {
                    browserBackNavigationController.resetBackExitConfirmation()
                }
            },
            renderProcessRecoveryController = renderProcessRecoveryController,
            clientCertificateController = clientCertificateController,
            httpAuthController = httpAuthController,
            adBlockRequestInterceptor = adBlockRequestInterceptor,
            smartNoImageRequestInterceptor = smartNoImageRequestInterceptor,
            shouldBlockUrl = browserNavigationController::shouldBlockUrl
        )
        webWindowController = WebWindowController(
            isPrivateBrowsingActive = { privateBrowsingActive },
            standardTabStore = standardTabStore,
            standardTabWebViews = browserStandardWebViewHostController.standardTabWebViews,
            standardSessionController = standardSessionController,
            closeFunctionCenter = { functionCenterEntryController.closeFunctionCenter() },
            saveStandardTabSession = browserStandardTabSessionController::saveStandardTabSession,
            closeTab = browserTabActionsController::closeTab
        )
        browserChromeClientController = BrowserChromeClientController(
            activity = this,
            fullscreenContainer = fullscreenContainer,
            decorView = window.decorView,
            standardSessionController = standardSessionController,
            privateSessionController = privateSessionController,
            browserManager = {
                browserStandardWebViewHostController.currentBrowserManager()
            },
            isPrivateBrowsingActive = { privateBrowsingActive },
            fullscreenChanged = { fullscreen ->
                browserFullscreenUiController.handleVideoFullscreenChanged(fullscreen)
            },
            webFileChooserController = webFileChooserController,
            webPermissionRequestController = webPermissionRequestController,
            geolocationPermissionController = geolocationPermissionController,
            webWindowController = webWindowController
        )

        // 网页全屏视频控制器处理 WebChromeClient 自定义视图和网页视频手势协议。
        fullscreenVideoController = FullscreenVideoController(
            activity = this,
            rootView = rootView as ViewGroup,
            browserManager = {
                browserStandardWebViewHostController.currentBrowserManager()
            },
            settingsManager = { settingsManager },
            chromeClient = browserChromeClientStateController::currentChromeClientOrNull,
            dp = ::dp
        )
        browserFullscreenUiController = BrowserFullscreenUiController(
            rootView = rootView,
            fullscreenVideoController = fullscreenVideoController,
            browserControlsShellController = browserControlsShellController,
            browserDisplayModeController = browserDisplayModeController,
            currentChromeClient = browserChromeClientStateController::currentChromeClientOrNull,
            isDesktopModeEnabled = browserFeatureStateController::isDesktopModeEnabled
        )

        // 功能中心是底部弹出的工具面板。装配类负责把各控制器动作接入页面。
        functionCenterEntryController = FunctionCenterAssemblyController(
            activity = this,
            functionCenter = functionCenterController,
            settingsManager = settingsManager,
            browserManager = {
                browserStandardWebViewHostController.currentBrowserManager()
            },
            browserManagers = {
                browserStandardWebViewHostController.browserManagers()
            },
            savedPageRepository = savedPageRepository,
            downloadRecordRepository = downloadRecordRepository,
            playbackHistoryRepository = playbackHistoryRepository,
            adBlockLogger = adBlockLogger,
            filesDir = filesDir,
            browserUrlStateController = browserUrlStateController,
            browserFeatureStateController = browserFeatureStateController,
            browserTabActionsController = browserTabActionsController,
            browserLaunchController = browserLaunchController,
            pageActionsController = pageActionsController,
            browserPageToolEntryController = browserPageToolEntryController,
            downloadController = downloadController,
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

        // 站点安全控制器负责地址栏锁/警告图标与详情弹窗，MainActivity 只在 URL 或主题变化时通知它刷新。
        siteSecurityController = SiteSecurityController(
            activity = this,
            siteSecurityIcon = siteSecurityIcon,
            settingsManager = settingsManager,
            currentPageUrl = {
                browserSessionStateController.currentSessionController().currentPageUrl
            },
            currentWebViewUrl = {
                browserStandardWebViewHostController.currentBrowserManager().currentUrl()
            },
            isPrivateBrowsingEnabled = browserFeatureStateController::isPrivateBrowsingEnabled,
            currentSiteHost = browserUrlStateController::currentSiteHost,
            showCurrentSiteSettingsPage = functionCenterEntryController::showCurrentSiteSettingsPage
        )

        // JS 注入链路：ScriptLoader 读 assets/scripts，JsInjector 组合脚本，PageFeatureCoordinator 判断是否启用。
        jsInjector = JsInjector(
            scriptLoader = ScriptLoader(assets),
            evaluateJavascript = { script ->
                browserStandardWebViewHostController.currentBrowserManager()
                    .evaluateJavascript(script)
            },
            ruleEngine = ruleEngine
        )
        pageFeatureCoordinator = PageFeatureCoordinator(
            settingsManager = settingsManager,
            browserManager = {
                browserStandardWebViewHostController.currentBrowserManager()
            },
            jsInjector = jsInjector,
            currentSiteHost = browserUrlStateController::currentSiteHost,
            currentPageUrl = {
                browserSessionStateController.currentSessionController().currentPageUrl
            }
        )
        elementPickerController = ElementPickerController(
            activity = this,
            browserManager = {
                browserStandardWebViewHostController.currentBrowserManager()
            },
            settingsManager = settingsManager,
            currentSiteHost = browserUrlStateController::currentSiteHost,
            isJsInjectionEnabled = browserFeatureStateController::isJsInjectionEnabled,
            isCurrentSiteJsInjectionDisabled =
                browserFeatureStateController::isCurrentSiteJsInjectionDisabled,
            injectPageFeatures = pageFeatureInjectionController::injectPageFeatures
        )
        nativeBridgeController = VideoBrowserNativeBridgeController(
            postToUi = { action -> runOnUiThread { action() } },
            currentChromeClient = browserChromeClientStateController::currentChromeClientOrNull,
            fullscreenVideoController = fullscreenVideoController,
            webPlaybackHistoryRecorder = webPlaybackHistoryRecorder,
            requestElementBlock = elementPickerController::handlePickedElement,
            blockSelectedElement = { selector ->
                elementPickerController.handlePickedElement(selector, "")
            },
            cancelElementPicker = elementPickerController::handleCancelledFromPage
        )
        browserBackNavigationController = BrowserBackNavigationController(
            activity = this,
            browserManager = {
                browserStandardWebViewHostController.currentBrowserManager()
            },
            currentChromeClient = browserChromeClientStateController::currentChromeClientOrNull,
            handleFunctionCenterBack = functionCenterEntryController::handleFunctionCenterBack,
            isElementPickerActive = { elementPickerController.isActive },
            cancelElementPicker = elementPickerController::cancel,
            updateNavigationButtons = browserShellUiController::updateNavigationButtons
        )

        BrowserWindowInsetsController(
            rootView = rootView,
            isVideoFullscreenUiActive = { isVideoFullscreenUiActive }
        ).setupSystemWindowInsets()

        BrowserStartupController(
            browserControlsShellController = browserControlsShellController,
            addressSuggestionController = addressSuggestionController,
            browsingModeThemeController = browsingModeThemeController,
            browserShellUiController = browserShellUiController,
            browserBackNavigationController = browserBackNavigationController,
            browserStandardWebViewHostController = browserStandardWebViewHostController,
            settingsManager = settingsManager,
            setDefaultUserAgent = { userAgent -> defaultUserAgent = userAgent },
            browserDisplayModeController = browserDisplayModeController,
            downloadController = downloadController,
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
        if (event.action == KeyEvent.ACTION_DOWN && isVideoFullscreenUiActive) {
            fullscreenVideoController.wakeControls()
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
        private const val BROWSER_CONTROLS_SCROLL_THRESHOLD_DP = 48
        private const val BROWSER_CONTROLS_SCROLL_COOLDOWN_MS = 500L
    }
}
