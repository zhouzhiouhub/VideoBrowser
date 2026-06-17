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
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.ClientCertRequest
import android.webkit.GeolocationPermissions
import android.webkit.HttpAuthHandler
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebView
import android.webkit.WebChromeClient.FileChooserParams
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.videobrowser.adblock.AdBlockManager
import com.example.videobrowser.adblock.AdBlockLogger
import com.example.videobrowser.adblock.AdBlockRequestInterceptor
import com.example.videobrowser.browser.BrowserBackNavigationController
import com.example.videobrowser.browser.BrowserClient
import com.example.videobrowser.browser.BrowserFeatureStateController
import com.example.videobrowser.browser.BrowserControlsController
import com.example.videobrowser.browser.BrowserControlsShellController
import com.example.videobrowser.browser.BrowserControlsScrollController
import com.example.videobrowser.browser.BrowserDisplayModeController
import com.example.videobrowser.browser.BrowserPageError
import com.example.videobrowser.browser.BrowserUrlStateController
import com.example.videobrowser.browser.BrowserExternalNavigator
import com.example.videobrowser.browser.HistoryRecordPolicy
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.browser.BrowserLaunchController
import com.example.videobrowser.browser.BrowserMode
import com.example.videobrowser.browser.BrowserNavigationController
import com.example.videobrowser.browser.BrowserRequest
import com.example.videobrowser.browser.BrowserPageToolEntryController
import com.example.videobrowser.browser.BrowserSessionController
import com.example.videobrowser.browser.BrowserSessionCoordinator
import com.example.videobrowser.browser.BrowserTabActionsController
import com.example.videobrowser.browser.BrowserTabSessionRepository
import com.example.videobrowser.browser.BrowserTabSessionBinding
import com.example.videobrowser.browser.BrowserTabStore
import com.example.videobrowser.browser.BrowserTabWebViewRegistry
import com.example.videobrowser.browser.BrowsingModeThemeController
import com.example.videobrowser.browser.ChromeClient
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
import com.example.videobrowser.browser.search.SearchSuggestionClient
import com.example.videobrowser.browser.search.SearchProviderController
import com.example.videobrowser.browser.search.SearchProviders
import com.example.videobrowser.download.DownloadController
import com.example.videobrowser.download.DownloadRecordRepository
import com.example.videobrowser.element.ElementPickerController
import com.example.videobrowser.functioncenter.FunctionCenterController
import com.example.videobrowser.functioncenter.FunctionCenterEntryController
import com.example.videobrowser.functioncenter.FunctionCenterPages
import com.example.videobrowser.inject.JsInjector
import com.example.videobrowser.inject.PageFeatureCoordinator
import com.example.videobrowser.inject.ScriptLoader
import com.example.videobrowser.localfiles.LocalDocumentEntryController
import com.example.videobrowser.localfiles.LocalFilesController
import com.example.videobrowser.rules.RuleEngine
import com.example.videobrowser.rules.RuleEngineFactory
import com.example.videobrowser.settings.BrowserDefaultSettingsResetter
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.settings.SessionSitePermissionStore
import com.example.videobrowser.storage.BookmarkImportExportController
import com.example.videobrowser.storage.PreferenceStore
import com.example.videobrowser.storage.SavedPageRepository
import com.example.videobrowser.video.ExternalSubtitleCandidate
import com.example.videobrowser.video.FullscreenVideoController
import com.example.videobrowser.video.NativePlayerEntryController
import com.example.videobrowser.video.PlaybackHistoryRepository
import com.example.videobrowser.video.PlaybackQueue
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
    private val webView: WebView get() = currentBrowserManager().activeWebView
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
    private lateinit var standardWebView: WebView
    private lateinit var standardBrowserManager: BrowserManager
    private lateinit var browserSessionCoordinator: BrowserSessionCoordinator
    private lateinit var browserControlsController: BrowserControlsController
    private lateinit var browserControlsShellController: BrowserControlsShellController
    private lateinit var browserControlsScrollController: BrowserControlsScrollController
    private lateinit var standardSessionController: BrowserSessionController
    private lateinit var privateSessionController: BrowserSessionController
    private lateinit var browserTabSessionRepository: BrowserTabSessionRepository
    private lateinit var functionCenterController: FunctionCenterController
    private lateinit var functionCenterPages: FunctionCenterPages
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
    private lateinit var browserBackNavigationController: BrowserBackNavigationController
    private lateinit var nativeBridgeController: VideoBrowserNativeBridgeController
    private lateinit var fullscreenVideoController: FullscreenVideoController
    private lateinit var webFileChooserController: WebFileChooserController
    private lateinit var webPermissionRequestController: WebPermissionRequestController
    private lateinit var geolocationPermissionController: GeolocationPermissionController
    private lateinit var elementPickerController: ElementPickerController
    private lateinit var jsInjector: JsInjector
    private lateinit var pageFeatureCoordinator: PageFeatureCoordinator
    private lateinit var standardChromeClient: ChromeClient
    private lateinit var privateChromeClient: ChromeClient
    private lateinit var externalNavigator: BrowserExternalNavigator
    private lateinit var nativePlayerEntryController: NativePlayerEntryController
    // endregion

    // region 标签页与会话状态
    // 标准模式和无痕模式各有自己的标签页列表，避免无痕页面写入普通会话。
    private val standardTabStore = BrowserTabStore()
    private val privateTabStore = BrowserTabStore()
    private lateinit var standardTabWebViews: BrowserTabWebViewRegistry<WebView>
    private val standardTabSessionBinding = BrowserTabSessionBinding(standardTabStore)
    private val privateTabSessionBinding = BrowserTabSessionBinding(privateTabStore)
    private val findInPageController = FindInPageController(
        findAll = { query -> currentBrowserManager().findAllAsync(query) },
        findNext = { forward -> currentBrowserManager().findNext(forward) },
        clearMatches = { currentBrowserManager().clearFindMatches() }
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
            currentPageUrl = { currentSessionController().currentPageUrl },
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
            currentPageUrl = { currentSessionController().currentPageUrl }
        )
    }
    // endregion

    // region Android 系统交互状态
    // 这些字段保存系统弹窗或系统 Activity 返回前的临时状态，例如文件选择、权限申请、证书选择。
    private val webFileChooserLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            webFileChooserController.handleActivityResult(result.resultCode, result.data)
        }
    private val bookmarkExportLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
            if (uri != null) {
                bookmarkImportExportController.exportToUri(uri)
            }
        }
    private val bookmarkImportLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                bookmarkImportExportController.importFromUri(uri)
            }
        }
    private val pageArchiveExportLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument(PageArchiveController.MIME_TYPE)) { uri ->
            pageArchiveController.handleExportResult(uri)
        }
    private val webPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
            webPermissionRequestController.handleAndroidPermissionResult(grants)
        }
    private val sessionSitePermissionStore = SessionSitePermissionStore()
    private val geolocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
            geolocationPermissionController.handleAndroidPermissionResult(grants)
        }
    // endregion

    // region 当前页面运行状态
    private var privateBrowsingActive = false
    private val isHomePageVisible: Boolean
        get() = currentSessionController().isHomePageVisible
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
        if ((applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        setContentView(R.layout.activity_main)

        // 先绑定界面控件，再创建依赖这些控件的控制器。
        views = MainActivityViews.bind(this)
        functionCenterController = FunctionCenterController(this, rootView, ::dp)
        browserFeatureStateController = BrowserFeatureStateController(
            settingsManager = { settingsManager },
            pageFeatureCoordinator = { pageFeatureCoordinator },
            isPrivateBrowsingActive = { privateBrowsingActive }
        )
        browserUrlStateController = BrowserUrlStateController(
            currentPageUrl = {
                if (areBrowserSessionsInitialized()) {
                    currentSessionController().currentPageUrl
                } else {
                    null
                }
            },
            currentWebViewUrl = {
                if (areBrowserSessionsInitialized()) {
                    currentBrowserManager().currentUrl()
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
                if (areBrowserSessionsInitialized()) {
                    currentSessionController().isPageLoading
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
                if (areBrowserSessionsInitialized()) {
                    currentSessionController().currentPageUrl
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

        // 本地持久化层：设置、收藏/历史、标签会话、下载记录和播放历史都放在 SharedPreferences。
        preferenceStore = PreferenceStore.from(this)
        settingsManager = SettingsManager(preferenceStore)
        savedPageRepository = SavedPageRepository(preferenceStore)
        bookmarkImportExportController = BookmarkImportExportController(
            activity = this,
            savedPageRepository = savedPageRepository,
            updateBookmarkButton = ::updateBookmarkButton
        )
        browserTabSessionRepository = BrowserTabSessionRepository(preferenceStore)
        restoreStandardTabSession()
        downloadRecordRepository = DownloadRecordRepository(preferenceStore)
        playbackHistoryRepository = PlaybackHistoryRepository(preferenceStore)
        webPlaybackHistoryRecorder = WebPlaybackHistoryRecorder(
            playbackHistoryRepository = playbackHistoryRepository,
            isPrivateBrowsingEnabled = browserFeatureStateController::isPrivateBrowsingEnabled,
            currentShareableUrl = browserUrlStateController::currentShareableUrl,
            isShareableUrl = browserUrlStateController::isShareableUrl,
            defaultVideoSpeed = settingsManager::defaultVideoSpeed,
            currentPageTitle = { currentSessionController().currentPageTitle }
        )
        browserDefaultSettingsResetter = BrowserDefaultSettingsResetter(
            settingsManager = settingsManager,
            savedPageRepository = savedPageRepository,
            browserTabSessionRepository = browserTabSessionRepository,
            filesDir = filesDir
        )

        // 本地文件模块负责选择目录、读取文件列表，并把可播放文件交给浏览器或原生播放器。
        localFilesController = LocalFilesController(
            activity = this,
            preferenceStore = preferenceStore,
            functionCenter = functionCenterController,
            logTag = RULE_LOG_TAG,
            showMainFunctionCenterPage = ::showFunctionCenterRootPage,
            onOpenDocumentUri = ::openLocalDocumentUri
        )
        localDocumentEntryController = LocalDocumentEntryController(
            localFilesController = localFilesController,
            pageActionsController = { pageActionsController },
            closeFunctionCenter = ::closeFunctionCenter,
            currentSessionController = ::currentSessionController,
            currentBrowserManager = ::currentBrowserManager,
            updateAddressBar = ::updateAddressBar,
            hideKeyboard = ::hideKeyboard,
            showHomeContent = ::showHomeContent
        )

        // 搜索入口和地址建议拆成两个控制器：前者管理搜索引擎，后者管理输入提示列表。
        searchProviderController = SearchProviderController(
            activity = this,
            providerScroll = searchProviderScroll,
            providerList = searchProviderList,
            addressInput = addressInput,
            addressProviderBadge = views.addressProviderBadge,
            settingsManager = settingsManager,
            savedPageRepository = savedPageRepository,
            dp = ::dp,
            isHomePageVisible = { isHomePageVisible },
            isPrivateBrowsingEnabled = browserFeatureStateController::isPrivateBrowsingEnabled,
            openProviderHome = ::openHomePage,
            openCustomShortcut = ::loadUrl
        )
        historyRecordPolicy = HistoryRecordPolicy(
            homeUrls = {
                SearchProviders.defaults.map { provider -> provider.homeUrl } +
                    settingsManager.homeUrlOr(searchProviderController.selectedProvider.homeUrl)
            }
        )
        addressSuggestionController = AddressSuggestionController(
            activity = this,
            panel = addressSuggestionPanel,
            addressInput = addressInput,
            savedPageRepository = savedPageRepository,
            suggestionClient = SearchSuggestionClient(),
            selectedProvider = { searchProviderController.selectedProvider },
            isPrivateBrowsingEnabled = browserFeatureStateController::isPrivateBrowsingEnabled,
            areBrowserControlsHidden = {
                ::browserControlsController.isInitialized && browserControlsController.areHidden
            },
            isVideoFullscreenUiActive = { isVideoFullscreenUiActive },
            openUrl = ::loadUrl,
            searchKeyword = ::searchAddressKeyword,
            dp = ::dp
        )
        linkContextMenuController = LinkContextMenuController(
            activity = this,
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
            currentUserAgent = { currentBrowserManager().userAgentString() },
            isShareableUrl = browserUrlStateController::isShareableUrl
        )

        // WebView 和标签页要先建好，后面的浏览器控制器才能拿到当前 activeWebView。
        setupBrowserWebViews()
        localDocumentEntryController.setupFileOperationLaunchers()

        // 规则引擎读取 assets/rules 和用户订阅缓存，供广告拦截、URL 清理、脚本注入使用。
        ruleEngine = RuleEngineFactory.create(assets, filesDir)
        externalNavigator = BrowserExternalNavigator(
            activity = this,
            browserManager = ::currentBrowserManager,
            currentPageTitle = {
                if (areBrowserSessionsInitialized()) {
                    currentSessionController().currentPageTitle
                } else {
                    ""
                }
            },
            currentShareableUrl = browserUrlStateController::currentShareableUrl,
            isShareableUrl = browserUrlStateController::isShareableUrl
        )
        nativePlayerEntryController = NativePlayerEntryController(
            externalNavigator = externalNavigator,
            isPrivateBrowsingEnabled = browserFeatureStateController::isPrivateBrowsingEnabled
        )
        browserNavigationController = BrowserNavigationController(
            activity = this,
            ruleEngine = { if (::ruleEngine.isInitialized) ruleEngine else null },
            browserManager = ::currentBrowserManager,
            sessionController = ::currentSessionController,
            externalNavigator = externalNavigator,
            closeFunctionCenter = ::closeFunctionCenter,
            openNativePlayer = nativePlayerEntryController::openNativePlayer,
            isProviderHomeUrl = ::isProviderHomeUrl,
            updateAddressBar = ::updateAddressBar,
            hideKeyboard = ::hideKeyboard,
            showHomeContent = ::showHomeContent
        )
        browserLaunchController = BrowserLaunchController(
            addressText = { addressInput.text?.toString().orEmpty() },
            runWithSuggestionsSuppressed = addressSuggestionController::runWithSuggestionsSuppressed,
            searchUrlPrefix = { searchProviderController.selectedProvider.searchUrlPrefix },
            homeUrl = {
                settingsManager.homeUrlOr(searchProviderController.selectedProvider.homeUrl)
            },
            activeStandardTabUrl = { standardTabStore.activeTab().url },
            loadUrl = ::loadUrl,
            isShareableUrl = browserUrlStateController::isShareableUrl
        )
        browserDisplayModeController = BrowserDisplayModeController(
            activity = this,
            browserManager = ::currentBrowserManager,
            isDesktopModeEnabled = browserFeatureStateController::isDesktopModeEnabled,
            isFullscreenModeActive = {
                areChromeClientsInitialized() && currentChromeClient().isFullscreenModeActive()
            },
            defaultUserAgent = { defaultUserAgent }
        )

        // 下载控制器负责接收 WebView 下载回调，并把记录写入本地仓库。
        downloadController = DownloadController(
            activity = this,
            browserManager = ::currentBrowserManager,
            downloadRecordRepository = downloadRecordRepository,
            openNativePlayer = { url, mimeType, userAgentOverride, titleOverride ->
                nativePlayerEntryController.openNativePlayer(url, mimeType, userAgentOverride, titleOverride)
            }
        )

        // 页面动作控制器收拢收藏、分享、保存归档、打开原生播放器等“当前页面动作”。
        pageActionsController = PageActionsController(
            activity = this,
            browserManager = ::currentBrowserManager,
            browserManagers = ::browserManagers,
            downloadController = downloadController,
            settingsManager = settingsManager,
            savedPageRepository = savedPageRepository,
            currentActionableUrl = browserUrlStateController::currentActionableUrl,
            currentShareableUrl = browserUrlStateController::currentShareableUrl,
            currentPageTitle = { currentSessionController().currentPageTitle },
            isShareableUrl = browserUrlStateController::isShareableUrl,
            shouldRecordHistoryUrl = historyRecordPolicy::shouldRecord,
            openNativePlayer = nativePlayerEntryController::openNativePlayer,
            openLocalArchiveInBrowser = localDocumentEntryController::loadLocalDocumentUrlInBrowser,
            isPrivateBrowsingEnabled = browserFeatureStateController::isPrivateBrowsingEnabled,
            switchPrivateBrowsing = { enabled ->
                privateBrowsingSwitchController.setPrivateBrowsingActive(enabled)
            },
            updateBookmarkButton = ::updateBookmarkButton,
            updateNavigationButtons = ::updateNavigationButtons,
            updatePrivateBrowsingUi = ::updatePrivateBrowsingUi,
            recreateActivity = { recreate() },
            restoreBrowserDefaults = browserDefaultSettingsResetter::restoreDefaults
        )
        httpAuthController = HttpAuthController(
            activity = this,
            dp = ::dp
        )
        clientCertificateController = ClientCertificateController(
            activity = this
        )
        pageArchiveController = PageArchiveController(
            activity = this,
            currentActionableUrl = browserUrlStateController::currentActionableUrl,
            currentPageTitle = { currentSessionController().currentPageTitle },
            activeWebView = { currentBrowserManager().activeWebView },
            launchArchiveExport = pageArchiveExportLauncher::launch
        )
        pagePrintController = PagePrintController(
            activity = this,
            currentActionableUrl = browserUrlStateController::currentActionableUrl,
            currentPageTitle = { currentSessionController().currentPageTitle },
            activeWebView = { currentBrowserManager().activeWebView }
        )
        findInPageDialogController = FindInPageDialogController(
            activity = this,
            findInPageController = findInPageController,
            setFindResultListener = { listener -> currentBrowserManager().setFindResultListener(listener) },
            closeFunctionCenter = { closeFunctionCenter() },
            dp = ::dp
        )
        browserPageToolEntryController = BrowserPageToolEntryController(
            findInPageDialogController = findInPageDialogController,
            pageArchiveController = pageArchiveController,
            pagePrintController = pagePrintController,
            loadUrl = ::loadUrl,
            openNativePlayer = { url, title ->
                nativePlayerEntryController.openNativePlayer(
                    url = url,
                    titleOverride = title
                )
            }
        )
        webFileChooserController = WebFileChooserController(
            activity = this,
            launchChooser = webFileChooserLauncher::launch
        )
        webPermissionRequestController = WebPermissionRequestController(
            activity = this,
            settingsManager = settingsManager,
            sessionSitePermissionStore = sessionSitePermissionStore,
            isPrivateBrowsingEnabled = browserFeatureStateController::isPrivateBrowsingEnabled,
            hasAndroidPermission = ::hasAndroidPermission,
            requestAndroidPermissions = webPermissionLauncher::launch
        )
        geolocationPermissionController = GeolocationPermissionController(
            activity = this,
            settingsManager = settingsManager,
            sessionSitePermissionStore = sessionSitePermissionStore,
            isPrivateBrowsingEnabled = browserFeatureStateController::isPrivateBrowsingEnabled,
            hasAndroidPermission = ::hasAndroidPermission,
            requestAndroidPermissions = geolocationPermissionLauncher::launch
        )

        // 浏览器控件控制器只关心按钮、地址栏和进度条，不直接了解规则或下载细节。
        browserControlsController = BrowserControlsController(
            activity = this,
            browserManager = ::currentBrowserManager,
            topBar = topBar,
            bottomBar = bottomBar,
            addressInput = addressInput,
            pageProgress = pageProgress,
            pageToolsButton = pageToolsButton,
            wenxinButton = wenxinButton,
            profileButton = profileButton,
            backButton = backButton,
            refreshButton = refreshButton,
            bookmarkButton = bookmarkButton,
            loadButton = loadButton,
            savedPageRepository = savedPageRepository,
            currentActionableUrl = browserUrlStateController::currentActionableUrl,
            isHomePageVisible = { isHomePageVisible },
            isVideoFullscreenUiActive = { isVideoFullscreenUiActive },
            onLoadAddress = ::loadAddressInput,
            onBack = ::handleBrowserBack,
            onOpenWenxin = ::openWenxinPage,
            onShowFunctionCenter = ::showFunctionCenter,
            onShowProfilePage = ::showProfilePage,
            onToggleBookmark = pageActionsController::toggleCurrentBookmark,
            onShowControlsRequested = {
                browserControlsShellController.setBrowserControlsHidden(false)
            },
            onAddressFocusChanged = browserControlsShellController::handleAddressFocusChanged,
            onVisibilityChanged = browserControlsShellController::syncSearchProviderVisibility
        )

        // 页面滚动时自动收起/显示顶部与底部工具栏。
        browserControlsScrollController = BrowserControlsScrollController(
            webView = standardWebView,
            addressInput = addressInput,
            dp = ::dp,
            areControlsHidden = { browserControlsController.areHidden },
            isHomePageVisible = { isHomePageVisible },
            isVideoFullscreenUiActive = { isVideoFullscreenUiActive },
            applyControlsHidden = browserControlsController::setHidden,
            updatePageProgressVisibility = browserControlsShellController::updatePageProgressVisibility
        )

        // 标准会话会写历史；无痕会话不写历史，并使用独立 WebView。
        standardSessionController = BrowserSessionController(
            activity = this,
            isActive = { !privateBrowsingActive },
            clearElementPickerState = {
                if (::elementPickerController.isInitialized) {
                    elementPickerController.clearState()
                }
            },
            exitPageFullscreenIfNeeded = ::exitPageFullscreenIfNeeded,
            isProviderHomeUrl = ::isProviderHomeUrl,
            updateAddressBar = ::updateAddressBar,
            showHomeContent = ::showHomeContent,
            setPageProgress = browserControlsController::setProgress,
            updatePageProgressVisibility = browserControlsShellController::updatePageProgressVisibility,
            updateNavigationButtons = ::updateNavigationButtons,
            addHistoryEntry = pageActionsController::addHistoryEntry,
            injectPageFeatures = ::injectPageFeatures,
            onPageMetadataChanged = { url, title ->
                standardTabSessionBinding.handlePageMetadataChanged(url, title)
                saveStandardTabSession()
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
            exitPageFullscreenIfNeeded = ::exitPageFullscreenIfNeeded,
            isProviderHomeUrl = ::isProviderHomeUrl,
            updateAddressBar = ::updateAddressBar,
            showHomeContent = ::showHomeContent,
            setPageProgress = browserControlsController::setProgress,
            updatePageProgressVisibility = browserControlsShellController::updatePageProgressVisibility,
            updateNavigationButtons = ::updateNavigationButtons,
            addHistoryEntry = {},
            injectPageFeatures = ::injectPageFeatures,
            onPageMetadataChanged = privateTabSessionBinding::handlePageMetadataChanged
        )
        privateBrowsingSwitchController = PrivateBrowsingSwitchController(
            activity = this,
            isPrivateBrowsingActive = { privateBrowsingActive },
            closeFunctionCenter = { closeFunctionCenter() },
            cancelElementPickerIfActive = {
                if (::elementPickerController.isInitialized && elementPickerController.isActive) {
                    elementPickerController.cancel()
                }
            },
            exitPageFullscreenIfNeeded = ::exitPageFullscreenIfNeeded,
            sessionSitePermissionStore = sessionSitePermissionStore,
            browserSessionCoordinator = browserSessionCoordinator,
            privateSessionController = privateSessionController,
            standardSessionController = standardSessionController,
            openHomePage = ::openHomePage,
            updatePrivateBrowsingUi = ::updatePrivateBrowsingUi,
            updateNavigationButtons = ::updateNavigationButtons
        )
        browserTabActionsController = BrowserTabActionsController(
            standardTabStore = standardTabStore,
            privateTabStore = privateTabStore,
            standardTabWebViews = standardTabWebViews,
            standardSessionController = standardSessionController,
            isPrivateBrowsingActive = { privateBrowsingActive },
            createStandardTabWebView = ::createStandardTabWebView,
            showStandardTabWebView = ::showStandardTabWebView,
            hideStandardTabWebView = ::hideStandardTabWebView,
            destroyStandardTabWebView = ::destroyStandardTabWebView,
            closeFunctionCenter = ::closeFunctionCenter,
            saveStandardTabSession = ::saveStandardTabSession,
            loadUrl = ::loadUrl,
            openHomePage = ::openHomePage
        )
        renderProcessRecoveryController = RenderProcessRecoveryController(
            webViewContainer = webViewContainer,
            sessionCoordinator = browserSessionCoordinator,
            standardTabWebViews = standardTabWebViews,
            currentPageUrl = { currentSessionController().currentPageUrl },
            isPrivateBrowsingActive = { privateBrowsingActive },
            createStandardTabWebView = ::createStandardTabWebView,
            showStandardTabWebView = { tabWebView, detachCurrent ->
                showStandardTabWebView(tabWebView, detachCurrent)
            },
            saveStandardTabSession = ::saveStandardTabSession,
            showBrowserErrorPage = ::showBrowserErrorPage
        )
        webWindowController = WebWindowController(
            isPrivateBrowsingActive = { privateBrowsingActive },
            standardTabStore = standardTabStore,
            standardTabWebViews = standardTabWebViews,
            standardSessionController = standardSessionController,
            closeFunctionCenter = ::closeFunctionCenter,
            saveStandardTabSession = ::saveStandardTabSession,
            closeTab = browserTabActionsController::closeTab
        )

        // 网页全屏视频控制器处理 WebChromeClient 自定义视图和网页视频手势协议。
        fullscreenVideoController = FullscreenVideoController(
            activity = this,
            rootView = rootView as ViewGroup,
            browserManager = ::currentBrowserManager,
            settingsManager = { settingsManager },
            chromeClient = { if (areChromeClientsInitialized()) currentChromeClient() else null },
            dp = ::dp
        )

        // 功能中心是底部弹出的工具面板。这里把 MainActivity 能提供的动作注入进去。
        functionCenterPages = FunctionCenterPages(
            activity = this,
            functionCenter = functionCenterController,
            settingsManager = settingsManager,
            browserManager = ::currentBrowserManager,
            browserManagers = ::browserManagers,
            savedPageRepository = savedPageRepository,
            downloadRecordRepository = downloadRecordRepository,
            playbackHistoryRepository = playbackHistoryRepository,
            adBlockLogger = adBlockLogger,
            filesDir = filesDir,
            currentSiteHost = browserUrlStateController::currentSiteHost,
            currentActionableUrl = browserUrlStateController::currentActionableUrl,
            isDesktopModeEnabled = browserFeatureStateController::isDesktopModeEnabled,
            isPrivateBrowsingEnabled = browserFeatureStateController::isPrivateBrowsingEnabled,
            isAdBlockEnabled = browserFeatureStateController::isAdBlockEnabled,
            isSmartNoImageEnabled = browserFeatureStateController::isSmartNoImageEnabled,
            isJsInjectionEnabled = browserFeatureStateController::isJsInjectionEnabled,
            isPageCleanupEnabled = browserFeatureStateController::isPageCleanupEnabled,
            isVideoEnhancementEnabled = browserFeatureStateController::isVideoEnhancementEnabled,
            currentTabs = browserTabActionsController::currentTabs,
            activeTabId = browserTabActionsController::activeTabId,
            openNewTab = browserTabActionsController::openNewTab,
            openHomePage = ::openHomePage,
            canReopenClosedTab = browserTabActionsController::canReopenClosedTab,
            reopenClosedTab = browserTabActionsController::reopenClosedTab,
            switchTab = browserTabActionsController::switchTab,
            closeTab = browserTabActionsController::closeTab,
            closeOtherTabs = browserTabActionsController::closeOtherTabs,
            closeAllTabs = browserTabActionsController::closeAllTabs,
            duplicateTab = browserTabActionsController::duplicateTab,
            toggleCurrentBookmark = pageActionsController::toggleCurrentBookmark,
            setCurrentPageAsHomePage = pageActionsController::setCurrentPageAsHomePage,
            copyCurrentUrl = pageActionsController::copyCurrentUrl,
            shareCurrentUrl = pageActionsController::shareCurrentUrl,
            saveCurrentPageArchive = browserPageToolEntryController::saveCurrentPageArchive,
            printCurrentPage = browserPageToolEntryController::printCurrentPage,
            findInPage = browserPageToolEntryController::showFindInPageDialog,
            openCurrentUrlInNativePlayer = pageActionsController::openCurrentUrlInNativePlayer,
            openPlaybackHistoryItem = browserPageToolEntryController::openPlaybackHistoryItem,
            downloadCurrentUrl = pageActionsController::downloadCurrentUrl,
            retryDownload = downloadController::retry,
            exportBookmarks = ::exportBookmarks,
            importBookmarks = ::importBookmarks,
            currentSearchProviderName = { searchProviderController.selectedProvider.name },
            selectSearchProvider = searchProviderController::selectDefaultSearchProvider,
            setPrivateBrowsingEnabled = pageActionsController::setPrivateBrowsingEnabled,
            restoreDefaultSettings = pageActionsController::restoreDefaultSettings,
            showFileOperationsPage = localDocumentEntryController::showFileOperationsPage,
            startElementPicker = ::startElementPicker,
            applyDesktopMode = ::applyDesktopMode,
            injectPageFeatures = ::injectPageFeatures,
            openUrlInNewTab = browserTabActionsController::openUrlInNewTab,
            loadUrl = ::loadUrl,
            recreateActivity = { recreate() }
        )
        functionCenterEntryController = FunctionCenterEntryController(
            functionCenterPages = functionCenterPages,
            hideKeyboard = ::hideKeyboard
        )

        // 站点安全控制器负责地址栏锁/警告图标与详情弹窗，MainActivity 只在 URL 或主题变化时通知它刷新。
        siteSecurityController = SiteSecurityController(
            activity = this,
            siteSecurityIcon = siteSecurityIcon,
            settingsManager = settingsManager,
            currentPageUrl = { currentSessionController().currentPageUrl },
            currentWebViewUrl = { currentBrowserManager().currentUrl() },
            isPrivateBrowsingEnabled = browserFeatureStateController::isPrivateBrowsingEnabled,
            currentSiteHost = browserUrlStateController::currentSiteHost,
            showCurrentSiteSettingsPage = ::showCurrentSiteSettingsPage
        )

        // JS 注入链路：ScriptLoader 读 assets/scripts，JsInjector 组合脚本，PageFeatureCoordinator 判断是否启用。
        jsInjector = JsInjector(
            scriptLoader = ScriptLoader(assets),
            evaluateJavascript = { script -> currentBrowserManager().evaluateJavascript(script) },
            ruleEngine = ruleEngine
        )
        pageFeatureCoordinator = PageFeatureCoordinator(
            settingsManager = settingsManager,
            browserManager = ::currentBrowserManager,
            jsInjector = jsInjector,
            currentSiteHost = browserUrlStateController::currentSiteHost,
            currentPageUrl = { currentSessionController().currentPageUrl }
        )
        elementPickerController = ElementPickerController(
            activity = this,
            browserManager = ::currentBrowserManager,
            settingsManager = settingsManager,
            currentSiteHost = browserUrlStateController::currentSiteHost,
            isJsInjectionEnabled = browserFeatureStateController::isJsInjectionEnabled,
            isCurrentSiteJsInjectionDisabled =
                browserFeatureStateController::isCurrentSiteJsInjectionDisabled,
            injectPageFeatures = ::injectPageFeatures
        )
        nativeBridgeController = VideoBrowserNativeBridgeController(
            postToUi = { action -> runOnUiThread { action() } },
            currentChromeClient = {
                if (areChromeClientsInitialized()) {
                    currentChromeClient()
                } else {
                    null
                }
            },
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
            browserManager = ::currentBrowserManager,
            currentChromeClient = {
                if (areChromeClientsInitialized()) {
                    currentChromeClient()
                } else {
                    null
                }
            },
            handleFunctionCenterBack = ::handleFunctionCenterBack,
            isElementPickerActive = { elementPickerController.isActive },
            cancelElementPicker = elementPickerController::cancel,
            updateNavigationButtons = ::updateNavigationButtons
        )

        // 系统栏/刘海安全区变化时，主界面留出边距；视频全屏时则交给全屏容器占满屏幕。
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val safeArea = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or
                    WindowInsetsCompat.Type.displayCutout()
            )
            if (isVideoFullscreenUiActive) {
                view.setPadding(0, 0, 0, 0)
            } else {
                view.setPadding(safeArea.left, safeArea.top, safeArea.right, safeArea.bottom)
            }
            insets
        }
        ViewCompat.requestApplyInsets(rootView)

        browserControlsShellController.setupSearchProviders()
        addressSuggestionController.setup()
        updatePrivateBrowsingUi()
        setupBrowserControls()
        browserControlsShellController.setupWebViewScrollControls()
        setupBackNavigation()
        standardBrowserManager.setup()
        standardBrowserManager.setThirdPartyCookiesEnabled(settingsManager.areThirdPartyCookiesEnabled())
        standardBrowserManager.setMixedContentBlocked(settingsManager.isMixedContentBlocked())
        standardBrowserManager.setTextZoomPercent(settingsManager.textZoomPercent())
        standardBrowserManager.setPrivateBrowsingEnabled(false)
        defaultUserAgent = standardBrowserManager.userAgentString()
        applyDesktopMode(reload = false)
        setupDownloadHandling()
        setupChromeClient()
        setupFullscreenGestureOverlay()
        standardBrowserManager.addJavascriptInterface(
            nativeBridgeController.createNativeBridge(),
            NATIVE_BRIDGE_NAME
        )
        setupBrowserClient()

        // 如果外部 Intent 带了 URL 就打开外部 URL，否则恢复标签页或打开主页。
        if (!handleLaunchIntent(intent)) {
            openInitialStandardPage()
        }
    }

    /**
     * 函数 `onNewIntent`：处理 `on New Intent` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param intent 参数类型为 `Intent`，表示函数执行 `intent` 相关逻辑时需要读取或处理的输入。
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleLaunchIntent(intent)
    }

    /**
     * 函数 `onPause`：处理 `on Pause` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    override fun onPause() {
        saveStandardTabSession()
        currentBrowserManager().onPause()
        super.onPause()
    }

    /**
     * 函数 `onResume`：处理 `on Resume` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    override fun onResume() {
        super.onResume()
        currentBrowserManager().onResume()
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
        cancelPendingWebFileChooser()
        cancelPendingWebPermissionRequest()
        cancelPendingGeolocationPermissionPrompt()
        cancelPendingHttpAuthRequest()
        cancelPendingClientCertRequest()
        if (::pageArchiveController.isInitialized) {
            pageArchiveController.dispose()
        }
        if (::addressSuggestionController.isInitialized) {
            addressSuggestionController.dispose()
        }
        if (::downloadController.isInitialized) {
            downloadController.dispose()
        }
        if (::elementPickerController.isInitialized) {
            elementPickerController.dispose()
        }
        closeFunctionCenter()
        if (areChromeClientsInitialized()) {
            currentChromeClient().hideCustomView()
        }
        saveStandardTabSession()
        if (::browserSessionCoordinator.isInitialized) {
            browserSessionCoordinator.destroyPrivateSession()
        }
        if (::standardTabWebViews.isInitialized) {
            standardTabWebViews.destroyAll(::destroyStandardTabWebView)
        } else if (::standardBrowserManager.isInitialized) {
            standardBrowserManager.destroy()
        }
        super.onDestroy()
    }

    // endregion

    // region WebView、ChromeClient 和 BrowserClient 组装
    // 这一组函数负责创建 WebView、绑定 WebChromeClient/WebViewClient，
    // 并处理网页弹窗、新窗口、渲染进程退出、证书和 HTTP 认证等浏览器外壳能力。
    /**
     * 函数 `setupBrowserWebViews`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun setupBrowserWebViews() {
        standardWebView = views.webView
        configureLinkContextMenu(standardWebView)
        standardBrowserManager = BrowserManager(standardWebView)
        standardTabWebViews = BrowserTabWebViewRegistry(
            tabs = standardTabStore,
            initialView = standardWebView,
            createWebView = ::createStandardTabWebView,
            showWebView = ::showStandardTabWebView,
            hideWebView = ::hideStandardTabWebView,
            destroyWebView = ::destroyStandardTabWebView
        )
        browserSessionCoordinator = BrowserSessionCoordinator(
            activity = this,
            webViewContainer = webViewContainer,
            standardWebView = standardWebView,
            browserManager = standardBrowserManager,
            onActiveWebViewChanged = ::handleActiveWebViewChanged
        )
    }

    /**
     * 函数 `restoreStandardTabSession`：封装 `restore Standard Tab Session` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun restoreStandardTabSession() {
        browserTabSessionRepository.restore()?.let { session ->
            standardTabStore.restore(session.tabs, session.activeTabId)
        }
    }

    /**
     * 函数 `saveStandardTabSession`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun saveStandardTabSession() {
        if (!::browserTabSessionRepository.isInitialized) {
            return
        }
        browserTabSessionRepository.save(
            tabs = standardTabStore.tabs(),
            activeTabId = standardTabStore.activeTabId
        )
    }

    /**
     * 函数 `createStandardTabWebView`：创建 `create Standard Tab Web View` 需要的对象、视图或配置，并返回给后续流程使用。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun createStandardTabWebView(): WebView {
        return WebView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            overScrollMode = standardWebView.overScrollMode
            setBackgroundColor(0x00000000)
            visibility = View.GONE
        }
    }

    /**
     * 函数 `showStandardTabWebView`：控制 `show Standard Tab Web View` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param tabWebView 参数类型为 `WebView`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     */
    private fun showStandardTabWebView(tabWebView: WebView) {
        showStandardTabWebView(tabWebView, detachCurrent = true)
    }

    /**
     * 函数 `showStandardTabWebView`：控制 `show Standard Tab Web View` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param tabWebView 参数类型为 `WebView`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param detachCurrent 参数类型为 `Boolean`，表示函数执行 `detachCurrent` 相关逻辑时需要读取或处理的输入。
     */
    private fun showStandardTabWebView(tabWebView: WebView, detachCurrent: Boolean) {
        if (tabWebView.parent == null) {
            webViewContainer.addView(tabWebView)
        }
        tabWebView.visibility = View.VISIBLE
        browserSessionCoordinator.setStandardWebView(tabWebView)
        standardBrowserManager.switchWebView(
            nextWebView = tabWebView,
            privateBrowsingEnabled = false,
            detachCurrent = detachCurrent
        )
        handleActiveWebViewChanged(tabWebView, BrowserMode.STANDARD)
    }

    /**
     * 函数 `hideStandardTabWebView`：控制 `hide Standard Tab Web View` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param tabWebView 参数类型为 `WebView`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     */
    private fun hideStandardTabWebView(tabWebView: WebView) {
        tabWebView.visibility = View.GONE
    }

    /**
     * 函数 `destroyStandardTabWebView`：封装 `destroy Standard Tab Web View` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param tabWebView 参数类型为 `WebView`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     */
    private fun destroyStandardTabWebView(tabWebView: WebView) {
        if (tabWebView.parent == webViewContainer) {
            webViewContainer.removeView(tabWebView)
        }
        standardBrowserManager.destroyWebView(tabWebView, clearSharedStores = false)
    }

    /**
     * 函数 `currentBrowserManager`：从现有状态、缓存或输入对象中取得目标数据，并把结果交给调用方继续处理。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun currentBrowserManager(): BrowserManager {
        return standardBrowserManager
    }

    /**
     * 函数 `browserManagers`：封装 `browser Managers` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun browserManagers(): List<BrowserManager> {
        return listOf(standardBrowserManager)
    }

    /**
     * 函数 `currentSessionController`：从现有状态、缓存或输入对象中取得目标数据，并把结果交给调用方继续处理。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun currentSessionController(): BrowserSessionController {
        return if (privateBrowsingActive) privateSessionController else standardSessionController
    }

    /**
     * 函数 `areBrowserSessionsInitialized`：封装 `are Browser Sessions Initialized` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun areBrowserSessionsInitialized(): Boolean {
        return ::standardSessionController.isInitialized && ::privateSessionController.isInitialized
    }

    /**
     * 函数 `currentChromeClient`：从现有状态、缓存或输入对象中取得目标数据，并把结果交给调用方继续处理。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun currentChromeClient(): ChromeClient {
        return if (privateBrowsingActive) privateChromeClient else standardChromeClient
    }

    /**
     * 函数 `areChromeClientsInitialized`：封装 `are Chrome Clients Initialized` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun areChromeClientsInitialized(): Boolean {
        return ::standardChromeClient.isInitialized && ::privateChromeClient.isInitialized
    }

    /**
     * 函数 `handleActiveWebViewChanged`：处理 `handle Active Web View Changed` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param activeWebView 参数类型为 `WebView`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param mode 参数类型为 `BrowserMode`，表示函数执行 `mode` 相关逻辑时需要读取或处理的输入。
     */
    private fun handleActiveWebViewChanged(activeWebView: WebView, mode: BrowserMode) {
        privateBrowsingActive = mode == BrowserMode.PRIVATE
        configureLinkContextMenu(activeWebView)
        if (::browserControlsScrollController.isInitialized) {
            browserControlsScrollController.attachToWebView(activeWebView)
        }
        if (areChromeClientsInitialized()) {
            currentBrowserManager().setChromeClient(currentChromeClient())
        }
        updatePrivateBrowsingUi()
        browserControlsShellController.syncSearchProviderVisibility()
        applyBrowsingModeTheme()
        if (areBrowserSessionsInitialized()) {
            currentSessionController().renderCurrentState()
        }
    }

    /**
     * 函数 `setupBrowserControls`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun setupBrowserControls() {
        browserControlsController.setup()
        siteSecurityController.setup()
    }

    /**
     * 函数 `setupChromeClient`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun setupChromeClient() {
        standardChromeClient = createChromeClient(standardSessionController)
        privateChromeClient = createChromeClient(privateSessionController)
        currentBrowserManager().setChromeClient(currentChromeClient())
    }

    /**
     * 函数 `createChromeClient`：创建 `create Chrome Client` 需要的对象、视图或配置，并返回给后续流程使用。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param sessionController 参数类型为 `BrowserSessionController`，表示函数执行 `sessionController` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun createChromeClient(sessionController: BrowserSessionController): ChromeClient {
        return ChromeClient(
            activity = this,
            fullscreenContainer = fullscreenContainer,
            decorView = window.decorView,
            progressChanged = sessionController::handlePageProgressChanged,
            titleReceived = sessionController::handlePageTitleReceived,
            fullscreenChanged = ::handleVideoFullscreenChanged,
            fileChooserRequested = ::showWebFileChooser,
            permissionRequested = ::handleWebPermissionRequest,
            permissionRequestCanceled = ::handleWebPermissionRequestCanceled,
            geolocationPermissionRequested = ::handleGeolocationPermissionRequest,
            geolocationPermissionHidden = ::handleGeolocationPermissionHidden,
            newWindowRequested = ::handleCreateWebWindow,
            windowClosed = ::handleCloseWebWindow
        )
    }

    /**
     * 函数 `handleCreateWebWindow`：处理 `handle Create Web Window` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param view 参数类型为 `WebView?`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param isDialog 参数类型为 `Boolean`，表示函数执行 `isDialog` 相关逻辑时需要读取或处理的输入。
     * @param isUserGesture 参数类型为 `Boolean`，表示函数执行 `isUserGesture` 相关逻辑时需要读取或处理的输入。
     * @param resultMsg 参数类型为 `Message?`，表示函数执行 `resultMsg` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun handleCreateWebWindow(
        view: WebView?,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message?
    ): Boolean {
        return webWindowController.handleCreateWebWindow(view, isDialog, isUserGesture, resultMsg)
    }

    /**
     * 函数 `handleCloseWebWindow`：处理 `handle Close Web Window` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param window 参数类型为 `WebView?`，表示函数执行 `window` 相关逻辑时需要读取或处理的输入。
     */
    private fun handleCloseWebWindow(window: WebView?) {
        if (::webWindowController.isInitialized) {
            webWindowController.handleCloseWebWindow(window)
        }
    }

    /**
     * 函数 `setupBrowserClient`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun setupBrowserClient() {
        currentBrowserManager().setBrowserClient(
            BrowserClient(
                pageStarted = { url ->
                    resetBackExitConfirmation()
                    currentSessionController().handlePageStarted(url)
                },
                pageFinished = { url -> currentSessionController().handlePageFinished(url) },
                pageLoadFailed = ::showBrowserErrorPage,
                requestIntercepted = ::interceptBrowserRequest,
                urlLoadingRequested = ::shouldBlockUrl,
                clientCertRequested = ::handleClientCertRequest,
                renderProcessGone = ::handleRenderProcessGone,
                httpAuthRequested = ::handleHttpAuthRequest
            )
        )
    }

    /**
     * 函数 `handleRenderProcessGone`：处理 `handle Render Process Gone` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param view 参数类型为 `WebView?`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param didCrash 参数类型为 `Boolean`，表示函数执行 `didCrash` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun handleRenderProcessGone(view: WebView?, didCrash: Boolean): Boolean {
        return renderProcessRecoveryController.handleRenderProcessGone(view, didCrash)
    }

    /**
     * 函数 `handleClientCertRequest`：处理 `handle Client Cert Request` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param view 参数类型为 `WebView?`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param request 参数类型为 `ClientCertRequest?`，表示一次请求或响应，函数会检查它的内容并决定如何继续处理。
     */
    private fun handleClientCertRequest(
        view: WebView?,
        request: ClientCertRequest?
    ) {
        clientCertificateController.handleRequest(request)
    }

    /**
     * 函数 `cancelPendingClientCertRequest`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun cancelPendingClientCertRequest() {
        if (::clientCertificateController.isInitialized) {
            clientCertificateController.cancelPending()
        }
    }

    /**
     * 函数 `interceptBrowserRequest`：封装 `intercept Browser Request` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param request 参数类型为 `BrowserRequest`，表示一次请求或响应，函数会检查它的内容并决定如何继续处理。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun interceptBrowserRequest(request: BrowserRequest) =
        adBlockRequestInterceptor.intercept(request) ?: smartNoImageRequestInterceptor.intercept(request)

    /**
     * 函数 `showBrowserErrorPage`：控制 `show Browser Error Page` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param error 参数类型为 `BrowserPageError`，表示函数执行 `error` 相关逻辑时需要读取或处理的输入。
     */
    private fun showBrowserErrorPage(error: BrowserPageError) {
        currentSessionController().handlePageFailed(error.url)
        currentBrowserManager().loadErrorPage(error)
    }

    // endregion

    // region 网页权限、文件选择、书签导入导出和系统认证
    // WebView 的相机、麦克风、定位、文件上传等能力都要经过 Android 系统授权。
    // 书签导入导出也依赖系统文件选择器，所以放在同一组系统交互逻辑里。
    /**
     * 函数 `handleHttpAuthRequest`：处理 `handle Http Auth Request` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param view 参数类型为 `WebView?`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param handler 参数类型为 `HttpAuthHandler?`，表示回调对象，异步操作完成后用它把结果通知回调用方。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @param realm 参数类型为 `String?`，表示函数执行 `realm` 相关逻辑时需要读取或处理的输入。
     */
    private fun handleHttpAuthRequest(
        view: WebView?,
        handler: HttpAuthHandler?,
        host: String?,
        realm: String?
    ) {
        httpAuthController.handleRequest(handler, host, realm)
    }

    /**
     * 函数 `cancelPendingHttpAuthRequest`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun cancelPendingHttpAuthRequest() {
        if (::httpAuthController.isInitialized) {
            httpAuthController.cancelPending()
        }
    }

    /**
     * 函数 `setupFullscreenGestureOverlay`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun setupFullscreenGestureOverlay() {
        fullscreenVideoController.attachOverlay()
    }

    /**
     * 函数 `exitPageFullscreenIfNeeded`：封装 `exit Page Fullscreen If Needed` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun exitPageFullscreenIfNeeded() {
        if (areChromeClientsInitialized() &&
            currentChromeClient().isFullscreenModeActive() &&
            !currentChromeClient().isShowingCustomView()
        ) {
            currentChromeClient().exitPageFullscreen()
        }
    }

    /**
     * 函数 `handleVideoFullscreenChanged`：处理 `handle Video Fullscreen Changed` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param fullscreen 参数类型为 `Boolean`，表示函数执行 `fullscreen` 相关逻辑时需要读取或处理的输入。
     */
    private fun handleVideoFullscreenChanged(fullscreen: Boolean) {
        fullscreenVideoController.handleFullscreenChanged(fullscreen)
        browserControlsShellController.setBrowserControlsHidden(fullscreen)
        browserControlsShellController.updatePageProgressVisibility(forceHidden = fullscreen)
        ViewCompat.requestApplyInsets(rootView)
        if (!fullscreen) {
            applyBrowserContentOrientation(browserFeatureStateController.isDesktopModeEnabled())
        }
    }

    /**
     * 函数 `showWebFileChooser`：控制 `show Web File Chooser` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param filePathCallback 参数类型为 `ValueCallback<Array<Uri>>?`，表示回调对象，异步操作完成后用它把结果通知回调用方。
     * @param fileChooserParams 参数类型为 `FileChooserParams?`，表示函数执行 `fileChooserParams` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun showWebFileChooser(
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {
        return webFileChooserController.showFileChooser(filePathCallback, fileChooserParams)
    }

    /**
     * 函数 `cancelPendingWebFileChooser`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun cancelPendingWebFileChooser() {
        if (::webFileChooserController.isInitialized) {
            webFileChooserController.cancelPending()
        }
    }

    /**
     * 函数 `exportBookmarks`：封装 `export Bookmarks` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun exportBookmarks() {
        bookmarkExportLauncher.launch(BookmarkImportExportController.EXPORT_FILE_NAME)
    }

    /**
     * 函数 `importBookmarks`：封装 `import Bookmarks` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun importBookmarks() {
        bookmarkImportLauncher.launch(BookmarkImportExportController.IMPORT_MIME_TYPES)
    }

    /**
     * 函数 `handleWebPermissionRequest`：处理 `handle Web Permission Request` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param request 参数类型为 `PermissionRequest?`，表示一次请求或响应，函数会检查它的内容并决定如何继续处理。
     */
    private fun handleWebPermissionRequest(request: PermissionRequest?) {
        webPermissionRequestController.handlePermissionRequest(request)
    }

    /**
     * 函数 `handleWebPermissionRequestCanceled`：处理 `handle Web Permission Request Canceled` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param request 参数类型为 `PermissionRequest?`，表示一次请求或响应，函数会检查它的内容并决定如何继续处理。
     */
    private fun handleWebPermissionRequestCanceled(request: PermissionRequest?) {
        webPermissionRequestController.handlePermissionRequestCanceled(request)
    }

    /**
     * 函数 `cancelPendingWebPermissionRequest`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun cancelPendingWebPermissionRequest() {
        if (::webPermissionRequestController.isInitialized) {
            webPermissionRequestController.cancelPendingRequest()
        }
    }

    /**
     * 函数 `handleGeolocationPermissionRequest`：处理 `handle Geolocation Permission Request` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param origin 参数类型为 `String?`，表示函数执行 `origin` 相关逻辑时需要读取或处理的输入。
     * @param callback 参数类型为 `GeolocationPermissions.Callback?`，表示回调对象，异步操作完成后用它把结果通知回调用方。
     */
    private fun handleGeolocationPermissionRequest(
        origin: String?,
        callback: GeolocationPermissions.Callback?
    ) {
        geolocationPermissionController.handlePermissionRequest(origin, callback)
    }

    /**
     * 函数 `handleGeolocationPermissionHidden`：处理 `handle Geolocation Permission Hidden` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun handleGeolocationPermissionHidden() {
        geolocationPermissionController.handlePermissionHidden()
    }

    /**
     * 函数 `cancelPendingGeolocationPermissionPrompt`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun cancelPendingGeolocationPermissionPrompt() {
        if (::geolocationPermissionController.isInitialized) {
            geolocationPermissionController.cancelPending()
        }
    }

    /**
     * 函数 `hasAndroidPermission`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param permission 参数类型为 `String`，表示函数执行 `permission` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun hasAndroidPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    // endregion

    // region 地址栏、顶部/底部工具栏和返回键
    // 这一组函数只处理用户正在看的浏览器外壳：进度条、搜索入口、滚动隐藏工具栏和返回键行为。
    /**
     * 函数 `setupBackNavigation`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun setupBackNavigation() {
        browserBackNavigationController.setupBackNavigation()
    }

    /**
     * 函数 `handleBrowserBack`：处理 `handle Browser Back` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun handleBrowserBack() {
        browserBackNavigationController.handleBrowserBack()
    }

    /**
     * 函数 `resetBackExitConfirmation`：封装 `reset Back Exit Confirmation` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun resetBackExitConfirmation() {
        if (::browserBackNavigationController.isInitialized) {
            browserBackNavigationController.resetBackExitConfirmation()
        }
    }

    // endregion

    // region 原生桥、功能中心、本地文件和页面工具
    // 原生桥把网页里的 JavaScript 调用转成 Kotlin 回调；功能中心和本地文件入口复用这些动作。
    /**
     * 函数 `startElementPicker`：启动或加载 `start Element Picker` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun startElementPicker() {
        elementPickerController.start()
    }

    /**
     * 函数 `showFunctionCenter`：控制 `show Function Center` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun showFunctionCenter() = functionCenterEntryController.showFunctionCenter()

    /**
     * 函数 `showFunctionCenterRootPage`：控制 `show Function Center Root Page` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun showFunctionCenterRootPage() = functionCenterEntryController.showFunctionCenterRootPage()

    /**
     * 函数 `showProfilePage`：控制 `show Profile Page` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun showProfilePage() = functionCenterEntryController.showProfilePage()

    /**
     * 函数 `handleFunctionCenterBack`：处理 `handle Function Center Back` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun handleFunctionCenterBack(): Boolean = functionCenterEntryController.handleFunctionCenterBack()

    /**
     * 函数 `closeFunctionCenter`：控制 `close Function Center` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun closeFunctionCenter(): Boolean = functionCenterEntryController.closeFunctionCenter()

    /**
     * 函数 `openLocalDocumentUri`：启动或加载 `open Local Document Uri` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param uri 参数类型为 `Uri`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param displayName 参数类型为 `String?`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param mimeType 参数类型为 `String?`，表示函数执行 `mimeType` 相关逻辑时需要读取或处理的输入。
     * @param subtitleCandidates 参数类型为 `List<ExternalSubtitleCandidate>`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param playbackQueue 参数类型为 `PlaybackQueue?`，表示函数执行 `playbackQueue` 相关逻辑时需要读取或处理的输入。
     */
    private fun openLocalDocumentUri(
        uri: Uri,
        displayName: String? = null,
        mimeType: String? = null,
        subtitleCandidates: List<ExternalSubtitleCandidate> = emptyList(),
        playbackQueue: PlaybackQueue? = null
    ) = localDocumentEntryController.openLocalDocumentUri(
        uri,
        displayName,
        mimeType,
        subtitleCandidates,
        playbackQueue
    )

    // endregion

    // region 浏览模式、站点功能开关和标签页管理
    // 普通模式和无痕模式共享大部分 UI，但使用不同的标签页存储和 WebView 会话。
    /**
     * 函数 `updatePrivateBrowsingUi`：根据最新状态刷新 `update Private Browsing Ui` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun updatePrivateBrowsingUi() {
        if (::browsingModeThemeController.isInitialized) {
            browsingModeThemeController.updatePrivateBrowsingUi()
        }
    }

    /**
     * 函数 `applyBrowsingModeTheme`：根据最新状态刷新 `apply Browsing Mode Theme` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun applyBrowsingModeTheme() {
        if (::browsingModeThemeController.isInitialized) {
            browsingModeThemeController.applyBrowsingModeTheme()
        }
    }

    // endregion

    // region 下载、桌面模式、链接菜单和原生播放器入口
    // 这一组函数处理“当前页面之外”的动作：下载资源、切换桌面 UA、长按链接菜单和跳到原生播放器。
    /**
     * 函数 `setupDownloadHandling`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun setupDownloadHandling() {
        downloadController.attachTo(browserManagers())
    }

    /**
     * 函数 `applyDesktopMode`：根据最新状态刷新 `apply Desktop Mode` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param reload 参数类型为 `Boolean`，表示函数执行 `reload` 相关逻辑时需要读取或处理的输入。
     */
    private fun applyDesktopMode(reload: Boolean) {
        browserDisplayModeController.applyDesktopMode(reload)
    }

    /**
     * 函数 `applyBrowserContentOrientation`：根据最新状态刷新 `apply Browser Content Orientation` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param desktopModeEnabled 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     */
    private fun applyBrowserContentOrientation(desktopModeEnabled: Boolean) {
        browserDisplayModeController.applyBrowserContentOrientation(desktopModeEnabled)
    }

    /**
     * 函数 `injectPageFeatures`：封装 `inject Page Features` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun injectPageFeatures() {
        if (!::pageFeatureCoordinator.isInitialized) {
            return
        }
        pageFeatureCoordinator.injectPageFeatures()
    }

    /**
     * 函数 `configureLinkContextMenu`：封装 `configure Link Context Menu` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param targetWebView 参数类型为 `WebView`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     */
    private fun configureLinkContextMenu(targetWebView: WebView) {
        linkContextMenuController.configure(targetWebView)
    }

    // endregion

    // region 地址解析、页面加载和站点安全提示
    // 地址栏输入先被解析为 URL 或搜索词；真正加载前还会经过媒体路由、HTTP 降级确认和规则清理。
    /**
     * 函数 `loadAddressInput`：启动或加载 `load Address Input` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun loadAddressInput() {
        browserLaunchController.loadAddressInput()
    }

    /**
     * 函数 `searchAddressKeyword`：封装 `search Address Keyword` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param keyword 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     */
    private fun searchAddressKeyword(keyword: String) {
        browserLaunchController.searchAddressKeyword(keyword)
    }

    /**
     * 函数 `openHomePage`：启动或加载 `open Home Page` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun openHomePage() {
        browserLaunchController.openHomePage()
    }

    /**
     * 函数 `openInitialStandardPage`：启动或加载 `open Initial Standard Page` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun openInitialStandardPage() {
        browserLaunchController.openInitialStandardPage()
    }

    /**
     * 函数 `openWenxinPage`：启动或加载 `open Wenxin Page` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun openWenxinPage() {
        browserLaunchController.openWenxinPage()
    }

    /**
     * 函数 `handleLaunchIntent`：处理 `handle Launch Intent` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param intent 参数类型为 `Intent?`，表示函数执行 `intent` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun handleLaunchIntent(intent: Intent?): Boolean {
        return browserLaunchController.handleLaunchIntent(intent)
    }

    /**
     * 函数 `loadUrl`：启动或加载 `load Url` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     */
    private fun loadUrl(url: String) {
        browserNavigationController.loadUrl(url)
    }

    /**
     * 函数 `loadUrlAfterInsecureNavigationConfirmation`：启动或加载 `load Url After Insecure Navigation Confirmation` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     */
    private fun loadUrlAfterInsecureNavigationConfirmation(url: String) {
        browserNavigationController.loadUrlAfterInsecureNavigationConfirmation(url)
    }

    /**
     * 函数 `updateAddressBar`：根据最新状态刷新 `update Address Bar` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     */
    private fun updateAddressBar(url: String?) {
        siteSecurityController.updateStatus(url)
        if (url.isNullOrBlank()) {
            return
        }

        val displayUrl = addressBarDisplayText(url)
        if (addressInput.text?.toString() == displayUrl) {
            return
        }
        addressInput.setText(displayUrl)
        addressInput.setSelection(addressInput.text?.length ?: 0)
    }

    /**
     * 函数 `showCurrentSiteSettingsPage`：控制 `show Current Site Settings Page` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun showCurrentSiteSettingsPage() =
        functionCenterEntryController.showCurrentSiteSettingsPage()

    // endregion

    // region 小工具函数和 WebView 跳转拦截
    // 这里放跨多个小流程复用的辅助函数，例如 dp 转换、键盘隐藏、URL 类型判断和 shouldOverrideUrlLoading 判断。
    /**
     * 函数 `addressBarDisplayText`：封装 `address Bar Display Text` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun addressBarDisplayText(url: String): String {
        return searchProviderController.addressBarDisplayText(url)
    }

    /**
     * 函数 `updateNavigationButtons`：根据最新状态刷新 `update Navigation Buttons` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun updateNavigationButtons() {
        browserControlsController.updateNavigationButtons()
    }

    /**
     * 函数 `updateBookmarkButton`：根据最新状态刷新 `update Bookmark Button` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun updateBookmarkButton() {
        if (::browserControlsController.isInitialized) {
            browserControlsController.updateBookmarkButton()
        }
    }

    /**
     * 函数 `showHomeContent`：控制 `show Home Content` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param show 参数类型为 `Boolean`，表示函数执行 `show` 相关逻辑时需要读取或处理的输入。
     */
    private fun showHomeContent(show: Boolean) {
        browserControlsScrollController.resetTracking()
        browserControlsShellController.setBrowserControlsHidden(false)
        browserControlsShellController.syncSearchProviderVisibility()
        webView.visibility = View.VISIBLE
        browserControlsShellController.updatePageProgressVisibility(forceHidden = show)
        updateNavigationButtons()
        applyBrowsingModeTheme()
    }

    /**
     * 函数 `hideKeyboard`：控制 `hide Keyboard` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun hideKeyboard() {
        if (::addressSuggestionController.isInitialized) {
            addressSuggestionController.hide()
        }
        addressInput.clearFocus()
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(addressInput.windowToken, 0)
    }

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

    /**
     * 函数 `shouldBlockUrl`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param view 参数类型为 `WebView?`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param uri 参数类型为 `Uri`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param openMedia 参数类型为 `Boolean`，表示函数执行 `openMedia` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun shouldBlockUrl(view: WebView?, uri: Uri, openMedia: Boolean = true): Boolean {
        return browserNavigationController.shouldBlockUrl(view, uri, openMedia)
    }

    /**
     * 函数 `isProviderHomeUrl`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun isProviderHomeUrl(url: String?): Boolean {
        return searchProviderController.isProviderHomeUrl(url)
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
