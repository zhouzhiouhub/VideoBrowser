package com.example.videobrowser

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.print.PrintAttributes
import android.print.PrintManager
import android.security.KeyChain
import android.text.InputType
import android.util.Log
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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.videobrowser.adblock.AdBlockManager
import com.example.videobrowser.adblock.AdBlockLogger
import com.example.videobrowser.adblock.AdBlockRequestInterceptor
import com.example.videobrowser.browser.BrowserClient
import com.example.videobrowser.browser.BrowserControlsController
import com.example.videobrowser.browser.BrowserControlsScrollController
import com.example.videobrowser.browser.BrowserPageError
import com.example.videobrowser.browser.BrowserExternalNavigator
import com.example.videobrowser.browser.HistoryRecordPolicy
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.browser.BrowserMode
import com.example.videobrowser.browser.BrowserRequest
import com.example.videobrowser.browser.BrowserSessionController
import com.example.videobrowser.browser.BrowserSessionCoordinator
import com.example.videobrowser.browser.BrowserTab
import com.example.videobrowser.browser.BrowserTabSessionRepository
import com.example.videobrowser.browser.BrowserTabSessionBinding
import com.example.videobrowser.browser.BrowserTabStore
import com.example.videobrowser.browser.BrowserTabWebViewRegistry
import com.example.videobrowser.browser.ChromeClient
import com.example.videobrowser.browser.ExternalProtocolPolicy
import com.example.videobrowser.browser.FindInPageController
import com.example.videobrowser.browser.PageActionsController
import com.example.videobrowser.browser.SiteSecurityStatus
import com.example.videobrowser.browser.SmartNoImageRequestInterceptor
import com.example.videobrowser.browser.VideoBrowserNativeBridge
import com.example.videobrowser.browser.search.AddressSuggestionController
import com.example.videobrowser.browser.search.SearchSuggestionClient
import com.example.videobrowser.browser.search.SearchProviderController
import com.example.videobrowser.browser.search.SearchProviders
import com.example.videobrowser.download.DownloadController
import com.example.videobrowser.download.DownloadRecordRepository
import com.example.videobrowser.element.ElementPickerController
import com.example.videobrowser.functioncenter.FunctionCenterController
import com.example.videobrowser.functioncenter.FunctionCenterPages
import com.example.videobrowser.functioncenter.PlaybackHistoryDisplayText
import com.example.videobrowser.inject.JsInjector
import com.example.videobrowser.inject.PageFeatureCoordinator
import com.example.videobrowser.inject.ScriptLoader
import com.example.videobrowser.localfiles.LocalFilesController
import com.example.videobrowser.rules.RuleEngine
import com.example.videobrowser.rules.RuleEngineFactory
import com.example.videobrowser.site.SiteHost
import com.example.videobrowser.settings.BrowserDefaultSettingsResetter
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.settings.SitePermission
import com.example.videobrowser.settings.SitePermissionDecision
import com.example.videobrowser.storage.PreferenceStore
import com.example.videobrowser.storage.SavedPageRepository
import com.example.videobrowser.utils.UrlUtils
import com.example.videobrowser.video.ExternalSubtitleCandidate
import com.example.videobrowser.video.FullscreenVideoController
import com.example.videobrowser.video.MediaRouteAction
import com.example.videobrowser.video.MediaRouteDecision
import com.example.videobrowser.video.MediaRouteRequest
import com.example.videobrowser.video.MediaRouteSource
import com.example.videobrowser.video.MediaRoutingController
import com.example.videobrowser.video.PlaybackHistoryRepository
import com.example.videobrowser.video.PlaybackProgress
import com.example.videobrowser.video.PlaybackQueue
import com.example.videobrowser.video.WebViewVideoCommand
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.PrivateKey
import java.security.cert.X509Certificate

class MainActivity : AppCompatActivity() {

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
    private val privateBrowsingBadge: TextView get() = views.privateBrowsingBadge
    private val pageToolsButton: ImageButton get() = views.pageToolsButton
    private val wenxinButton: ImageButton get() = views.wenxinButton
    private val profileButton: ImageButton get() = views.profileButton
    private val backButton: ImageButton get() = views.backButton
    private val refreshButton: ImageButton get() = views.refreshButton
    private val bookmarkButton: ImageButton get() = views.bookmarkButton
    private val loadButton: ImageButton get() = views.loadButton
    private val siteSecurityIcon: ImageView get() = views.siteSecurityIcon
    private val fullscreenContainer: FrameLayout get() = views.fullscreenContainer
    private lateinit var preferenceStore: PreferenceStore
    private lateinit var settingsManager: SettingsManager
    private lateinit var browserDefaultSettingsResetter: BrowserDefaultSettingsResetter
    private lateinit var savedPageRepository: SavedPageRepository
    private lateinit var downloadRecordRepository: DownloadRecordRepository
    private lateinit var playbackHistoryRepository: PlaybackHistoryRepository
    private lateinit var ruleEngine: RuleEngine
    private lateinit var standardWebView: WebView
    private lateinit var standardBrowserManager: BrowserManager
    private lateinit var browserSessionCoordinator: BrowserSessionCoordinator
    private lateinit var browserControlsController: BrowserControlsController
    private lateinit var browserControlsScrollController: BrowserControlsScrollController
    private lateinit var standardSessionController: BrowserSessionController
    private lateinit var privateSessionController: BrowserSessionController
    private lateinit var browserTabSessionRepository: BrowserTabSessionRepository
    private lateinit var functionCenterController: FunctionCenterController
    private lateinit var functionCenterPages: FunctionCenterPages
    private lateinit var localFilesController: LocalFilesController
    private lateinit var pageActionsController: PageActionsController
    private lateinit var historyRecordPolicy: HistoryRecordPolicy
    private lateinit var searchProviderController: SearchProviderController
    private lateinit var addressSuggestionController: AddressSuggestionController
    private lateinit var downloadController: DownloadController
    private lateinit var fullscreenVideoController: FullscreenVideoController
    private lateinit var elementPickerController: ElementPickerController
    private lateinit var jsInjector: JsInjector
    private lateinit var pageFeatureCoordinator: PageFeatureCoordinator
    private lateinit var standardChromeClient: ChromeClient
    private lateinit var privateChromeClient: ChromeClient
    private lateinit var externalNavigator: BrowserExternalNavigator
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
    private val adBlockLogger = AdBlockLogger()
    private val adBlockManager: AdBlockManager by lazy {
        AdBlockManager(
            isEnabled = { pageFeatureCoordinator.isAdBlockEnabled() },
            isDisabledForCurrentSite = { pageFeatureCoordinator.isCurrentSiteAdBlockDisabled() },
            isUserWhitelistedRequestHost = settingsManager::isUserWhitelistedSite,
            currentPageUrl = { currentSessionController().currentPageUrl },
            currentPageHost = ::currentSiteHost,
            logger = adBlockLogger,
            ruleEngine = ruleEngine
        )
    }
    private val adBlockRequestInterceptor: AdBlockRequestInterceptor by lazy {
        AdBlockRequestInterceptor(adBlockManager)
    }
    private val smartNoImageRequestInterceptor: SmartNoImageRequestInterceptor by lazy {
        SmartNoImageRequestInterceptor(
            isEnabled = { pageFeatureCoordinator.isSmartNoImageEnabled() },
            isDisabledForCurrentSite = { pageFeatureCoordinator.isCurrentSiteSmartNoImageDisabled() },
            currentPageUrl = { currentSessionController().currentPageUrl }
        )
    }
    private var pendingFileChooserCallback: ValueCallback<Array<Uri>>? = null
    private val webFileChooserLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            pendingFileChooserCallback?.onReceiveValue(
                FileChooserParams.parseResult(result.resultCode, result.data)
            )
            pendingFileChooserCallback = null
        }
    private val bookmarkExportLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
            if (uri != null) {
                exportBookmarksToUri(uri)
            }
        }
    private val bookmarkImportLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                importBookmarksFromUri(uri)
            }
        }
    private var pendingWebPermissionRequest: PermissionRequest? = null
    private var pendingWebPermissionPromptRequest: PermissionRequest? = null
    private var pendingWebPermissionDialog: AlertDialog? = null
    private val webPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
            val request = pendingWebPermissionRequest ?: return@registerForActivityResult
            pendingWebPermissionRequest = null
            val requiredPermissions = androidPermissionsForWebResources(request.resources)
            if (requiredPermissions != null && requiredPermissions.all { permission ->
                    grants[permission] == true || hasAndroidPermission(permission)
                }
            ) {
                handleWebPermissionRequestAfterAndroidPermission(request)
            } else {
                request.deny()
            }
        }
    private var pendingGeolocationPermissionPrompt: GeolocationPermissionPrompt? = null
    private var pendingGeolocationSitePrompt: GeolocationPermissionPrompt? = null
    private var pendingGeolocationDialog: AlertDialog? = null
    private val geolocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
            val prompt = pendingGeolocationPermissionPrompt ?: return@registerForActivityResult
            pendingGeolocationPermissionPrompt = null
            val allowed = geolocationAndroidPermissions().any { permission ->
                grants[permission] == true || hasAndroidPermission(permission)
            }
            if (allowed) {
                handleGeolocationPermissionAfterAndroidPermission(prompt)
            } else {
                denyGeolocationPermissionPrompt(prompt.origin, prompt.callback)
            }
        }
    private var pendingClientCertRequest: ClientCertRequest? = null
    private var pendingHttpAuthHandler: HttpAuthHandler? = null
    private var pendingHttpAuthDialog: AlertDialog? = null

    private var privateBrowsingActive = false
    private val isHomePageVisible: Boolean
        get() = currentSessionController().isHomePageVisible
    private val isVideoFullscreenUiActive: Boolean
        get() = ::fullscreenVideoController.isInitialized &&
            fullscreenVideoController.isFullscreenUiActive
    private var defaultUserAgent: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if ((applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        setContentView(R.layout.activity_main)

        views = MainActivityViews.bind(this)
        functionCenterController = FunctionCenterController(this, rootView, ::dp)
        preferenceStore = PreferenceStore.from(this)
        settingsManager = SettingsManager(preferenceStore)
        savedPageRepository = SavedPageRepository(preferenceStore)
        browserTabSessionRepository = BrowserTabSessionRepository(preferenceStore)
        restoreStandardTabSession()
        downloadRecordRepository = DownloadRecordRepository(preferenceStore)
        playbackHistoryRepository = PlaybackHistoryRepository(preferenceStore)
        browserDefaultSettingsResetter = BrowserDefaultSettingsResetter(
            settingsManager = settingsManager,
            savedPageRepository = savedPageRepository,
            browserTabSessionRepository = browserTabSessionRepository,
            filesDir = filesDir
        )
        localFilesController = LocalFilesController(
            activity = this,
            preferenceStore = preferenceStore,
            functionCenter = functionCenterController,
            logTag = RULE_LOG_TAG,
            showMainFunctionCenterPage = ::showFunctionCenterRootPage,
            onOpenDocumentUri = ::openLocalDocumentUri
        )
        searchProviderController = SearchProviderController(
            activity = this,
            providerScroll = searchProviderScroll,
            providerList = searchProviderList,
            addressInput = addressInput,
            addressProviderBadge = views.addressProviderBadge,
            settingsManager = settingsManager,
            dp = ::dp,
            isHomePageVisible = { isHomePageVisible },
            isPrivateBrowsingEnabled = ::isPrivateBrowsingEnabled,
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
            isPrivateBrowsingEnabled = ::isPrivateBrowsingEnabled,
            areBrowserControlsHidden = {
                ::browserControlsController.isInitialized && browserControlsController.areHidden
            },
            isVideoFullscreenUiActive = { isVideoFullscreenUiActive },
            openUrl = ::loadUrl,
            searchKeyword = ::searchAddressKeyword,
            dp = ::dp
        )
        setupBrowserWebViews()
        setupFileOperationLaunchers()
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
            currentShareableUrl = ::currentShareableUrl,
            isShareableUrl = ::isShareableUrl
        )
        downloadController = DownloadController(
            activity = this,
            browserManager = ::currentBrowserManager,
            downloadRecordRepository = downloadRecordRepository,
            openNativePlayer = { url, mimeType, userAgentOverride, titleOverride ->
                openNativePlayer(url, mimeType, userAgentOverride, titleOverride)
            },
            openExternalUrl = ::openExternalUrl
        )
        pageActionsController = PageActionsController(
            activity = this,
            browserManager = ::currentBrowserManager,
            browserManagers = ::browserManagers,
            downloadController = downloadController,
            settingsManager = settingsManager,
            savedPageRepository = savedPageRepository,
            currentActionableUrl = ::currentActionableUrl,
            currentShareableUrl = ::currentShareableUrl,
            currentPageTitle = { currentSessionController().currentPageTitle },
            isShareableUrl = ::isShareableUrl,
            shouldRecordHistoryUrl = historyRecordPolicy::shouldRecord,
            openNativePlayer = {
                    url,
                    mimeType,
                    userAgentOverride,
                    titleOverride,
                    subtitleCandidates,
                    playbackQueue ->
                openNativePlayer(
                    url = url,
                    mimeType = mimeType,
                    userAgentOverride = userAgentOverride,
                    titleOverride = titleOverride,
                    subtitleCandidates = subtitleCandidates,
                    playbackQueue = playbackQueue
                )
            },
            openExternalUrl = ::openExternalUrl,
            isPrivateBrowsingEnabled = ::isPrivateBrowsingEnabled,
            switchPrivateBrowsing = ::setPrivateBrowsingActive,
            updateBookmarkButton = ::updateBookmarkButton,
            updateNavigationButtons = ::updateNavigationButtons,
            updatePrivateBrowsingUi = ::updatePrivateBrowsingUi,
            recreateActivity = { recreate() },
            restoreBrowserDefaults = browserDefaultSettingsResetter::restoreDefaults
        )
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
            currentActionableUrl = ::currentActionableUrl,
            isHomePageVisible = { isHomePageVisible },
            isVideoFullscreenUiActive = { isVideoFullscreenUiActive },
            onLoadAddress = ::loadAddressInput,
            onOpenWenxin = ::openWenxinPage,
            onShowFunctionCenter = ::showFunctionCenter,
            onShowProfilePage = ::showProfilePage,
            onToggleBookmark = pageActionsController::toggleCurrentBookmark,
            onShowControlsRequested = { setBrowserControlsHidden(false) },
            onAddressFocusChanged = ::handleAddressFocusChanged,
            onVisibilityChanged = ::syncSearchProviderVisibility
        )
        browserControlsScrollController = BrowserControlsScrollController(
            webView = standardWebView,
            addressInput = addressInput,
            dp = ::dp,
            areControlsHidden = { browserControlsController.areHidden },
            isHomePageVisible = { isHomePageVisible },
            isVideoFullscreenUiActive = { isVideoFullscreenUiActive },
            applyControlsHidden = browserControlsController::setHidden,
            updatePageProgressVisibility = ::updatePageProgressVisibility
        )
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
            updatePageProgressVisibility = ::updatePageProgressVisibility,
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
            updatePageProgressVisibility = ::updatePageProgressVisibility,
            updateNavigationButtons = ::updateNavigationButtons,
            addHistoryEntry = {},
            injectPageFeatures = ::injectPageFeatures,
            onPageMetadataChanged = privateTabSessionBinding::handlePageMetadataChanged
        )
        fullscreenVideoController = FullscreenVideoController(
            activity = this,
            rootView = rootView as ViewGroup,
            browserManager = ::currentBrowserManager,
            settingsManager = { settingsManager },
            chromeClient = { if (areChromeClientsInitialized()) currentChromeClient() else null },
            dp = ::dp
        )
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
            currentSiteHost = ::currentSiteHost,
            currentActionableUrl = ::currentActionableUrl,
            isDesktopModeEnabled = ::isDesktopModeEnabled,
            isPrivateBrowsingEnabled = ::isPrivateBrowsingEnabled,
            isAdBlockEnabled = ::isAdBlockEnabled,
            isSmartNoImageEnabled = ::isSmartNoImageEnabled,
            isJsInjectionEnabled = ::isJsInjectionEnabled,
            isPageCleanupEnabled = ::isPageCleanupEnabled,
            isVideoEnhancementEnabled = ::isVideoEnhancementEnabled,
            currentTabs = ::currentTabs,
            activeTabId = ::activeTabId,
            openNewTab = ::openNewTab,
            openHomePage = ::openHomePage,
            canReopenClosedTab = ::canReopenClosedTab,
            reopenClosedTab = ::reopenClosedTab,
            switchTab = ::switchTab,
            closeTab = ::closeTab,
            closeOtherTabs = ::closeOtherTabs,
            closeAllTabs = ::closeAllTabs,
            duplicateTab = ::duplicateTab,
            toggleCurrentBookmark = pageActionsController::toggleCurrentBookmark,
            copyCurrentUrl = pageActionsController::copyCurrentUrl,
            shareCurrentUrl = pageActionsController::shareCurrentUrl,
            printCurrentPage = ::printCurrentPage,
            openCurrentUrlExternally = pageActionsController::openCurrentUrlExternally,
            findInPage = ::showFindInPageDialog,
            openCurrentUrlInNativePlayer = pageActionsController::openCurrentUrlInNativePlayer,
            openPlaybackHistoryItem = ::openPlaybackHistoryItem,
            downloadCurrentUrl = pageActionsController::downloadCurrentUrl,
            retryDownload = downloadController::retry,
            exportBookmarks = ::exportBookmarks,
            importBookmarks = ::importBookmarks,
            currentSearchProviderName = { searchProviderController.selectedProvider.name },
            selectSearchProvider = searchProviderController::selectDefaultSearchProvider,
            setPrivateBrowsingEnabled = pageActionsController::setPrivateBrowsingEnabled,
            restoreDefaultSettings = pageActionsController::restoreDefaultSettings,
            showFileOperationsPage = ::showFileOperationsPage,
            startElementPicker = ::startElementPicker,
            applyDesktopMode = ::applyDesktopMode,
            injectPageFeatures = ::injectPageFeatures,
            openUrlInNewTab = ::openUrlInNewTab,
            loadUrl = ::loadUrl,
            recreateActivity = { recreate() }
        )
        jsInjector = JsInjector(
            scriptLoader = ScriptLoader(assets),
            evaluateJavascript = { script -> currentBrowserManager().evaluateJavascript(script) },
            ruleEngine = ruleEngine
        )
        pageFeatureCoordinator = PageFeatureCoordinator(
            settingsManager = settingsManager,
            browserManager = ::currentBrowserManager,
            jsInjector = jsInjector,
            currentSiteHost = ::currentSiteHost,
            currentPageUrl = { currentSessionController().currentPageUrl }
        )
        elementPickerController = ElementPickerController(
            activity = this,
            browserManager = ::currentBrowserManager,
            settingsManager = settingsManager,
            currentSiteHost = ::currentSiteHost,
            isJsInjectionEnabled = ::isJsInjectionEnabled,
            isCurrentSiteJsInjectionDisabled = ::isCurrentSiteJsInjectionDisabled,
            injectPageFeatures = ::injectPageFeatures
        )

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

        setupSearchProviders()
        addressSuggestionController.setup()
        updatePrivateBrowsingUi()
        setupBrowserControls()
        setupWebViewScrollControls()
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
        standardBrowserManager.addJavascriptInterface(createNativeBridge(), NATIVE_BRIDGE_NAME)
        setupBrowserClient()

        if (!handleLaunchIntent(intent)) {
            openInitialStandardPage()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleLaunchIntent(intent)
    }

    override fun onPause() {
        saveStandardTabSession()
        currentBrowserManager().onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        currentBrowserManager().onResume()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && isVideoFullscreenUiActive) {
            fullscreenVideoController.wakeControls()
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onDestroy() {
        cancelPendingWebFileChooser()
        cancelPendingWebPermissionRequest()
        cancelPendingGeolocationPermissionPrompt()
        cancelPendingHttpAuthRequest()
        cancelPendingClientCertRequest()
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

    private fun restoreStandardTabSession() {
        browserTabSessionRepository.restore()?.let { session ->
            standardTabStore.restore(session.tabs, session.activeTabId)
        }
    }

    private fun saveStandardTabSession() {
        if (!::browserTabSessionRepository.isInitialized) {
            return
        }
        browserTabSessionRepository.save(
            tabs = standardTabStore.tabs(),
            activeTabId = standardTabStore.activeTabId
        )
    }

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

    private fun showStandardTabWebView(tabWebView: WebView) {
        showStandardTabWebView(tabWebView, detachCurrent = true)
    }

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

    private fun hideStandardTabWebView(tabWebView: WebView) {
        tabWebView.visibility = View.GONE
    }

    private fun destroyStandardTabWebView(tabWebView: WebView) {
        if (tabWebView.parent == webViewContainer) {
            webViewContainer.removeView(tabWebView)
        }
        standardBrowserManager.destroyWebView(tabWebView, clearSharedStores = false)
    }

    private fun currentBrowserManager(): BrowserManager {
        return standardBrowserManager
    }

    private fun browserManagers(): List<BrowserManager> {
        return listOf(standardBrowserManager)
    }

    private fun currentSessionController(): BrowserSessionController {
        return if (privateBrowsingActive) privateSessionController else standardSessionController
    }

    private fun areBrowserSessionsInitialized(): Boolean {
        return ::standardSessionController.isInitialized && ::privateSessionController.isInitialized
    }

    private fun currentChromeClient(): ChromeClient {
        return if (privateBrowsingActive) privateChromeClient else standardChromeClient
    }

    private fun areChromeClientsInitialized(): Boolean {
        return ::standardChromeClient.isInitialized && ::privateChromeClient.isInitialized
    }

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
        syncSearchProviderVisibility()
        applyBrowsingModeTheme()
        if (areBrowserSessionsInitialized()) {
            currentSessionController().renderCurrentState()
        }
    }

    private fun setupBrowserControls() {
        browserControlsController.setup()
        siteSecurityIcon.setOnClickListener {
            showSiteSecurityInfoDialog()
        }
    }

    private fun setupChromeClient() {
        standardChromeClient = createChromeClient(standardSessionController)
        privateChromeClient = createChromeClient(privateSessionController)
        currentBrowserManager().setChromeClient(currentChromeClient())
    }

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

    private fun handleCreateWebWindow(
        view: WebView?,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message?
    ): Boolean {
        if (privateBrowsingActive || !isUserGesture) {
            return false
        }
        val transport = resultMsg?.obj as? WebView.WebViewTransport ?: return false
        closeFunctionCenter()
        val tab = standardTabStore.openTab()
        val tabWebView = standardTabWebViews.activate(tab.id)
        standardSessionController.restorePageMetadata(tab.url, tab.title)
        saveStandardTabSession()
        transport.webView = tabWebView
        resultMsg?.sendToTarget()
        return true
    }

    private fun handleCloseWebWindow(window: WebView?) {
        if (privateBrowsingActive || window == null) {
            return
        }
        val tabId = standardTabWebViews.tabIdFor(window) ?: return
        closeTab(tabId)
    }

    private fun setupBrowserClient() {
        currentBrowserManager().setBrowserClient(
            BrowserClient(
                pageStarted = { url -> currentSessionController().handlePageStarted(url) },
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

    private fun handleRenderProcessGone(view: WebView?, didCrash: Boolean): Boolean {
        val goneWebView = view ?: return true
        val pageUrl = currentSessionController().currentPageUrl
            ?: goneWebView.url

        if (privateBrowsingActive && browserSessionCoordinator.activeWebView === goneWebView) {
            val previousWebView = browserSessionCoordinator.replacePrivateWebView()
            if (previousWebView != null) {
                disposeGoneWebView(previousWebView)
                showBrowserErrorPage(
                    BrowserPageError.RenderProcessGone(
                        url = pageUrl,
                        didCrash = didCrash
                    )
                )
            }
            return true
        }

        val tabId = standardTabWebViews.tabIdFor(goneWebView)
        if (tabId != null) {
            val replacementWebView = createStandardTabWebView()
            val result = standardTabWebViews.replaceView(tabId, replacementWebView)
            if (result != null && result.replacedActiveView && !privateBrowsingActive) {
                showStandardTabWebView(replacementWebView, detachCurrent = false)
                showBrowserErrorPage(
                    BrowserPageError.RenderProcessGone(
                        url = pageUrl,
                        didCrash = didCrash
                    )
                )
            }
            disposeGoneWebView(goneWebView)
            saveStandardTabSession()
            return true
        }

        disposeGoneWebView(goneWebView)
        return true
    }

    private fun disposeGoneWebView(goneWebView: WebView) {
        if (goneWebView.parent == webViewContainer) {
            webViewContainer.removeView(goneWebView)
        } else {
            (goneWebView.parent as? ViewGroup)?.removeView(goneWebView)
        }
        goneWebView.webChromeClient = null
        goneWebView.webViewClient = android.webkit.WebViewClient()
        goneWebView.setDownloadListener(null)
        goneWebView.removeAllViews()
        goneWebView.destroy()
    }

    private fun handleClientCertRequest(
        view: WebView?,
        request: ClientCertRequest?
    ) {
        val certRequest = request ?: return
        pendingClientCertRequest?.cancel()
        pendingClientCertRequest = certRequest
        KeyChain.choosePrivateKeyAlias(
            this,
            { alias -> handleClientCertAliasSelected(certRequest, alias) },
            certRequest.keyTypes,
            certRequest.principals,
            certRequest.host,
            certRequest.port,
            null
        )
    }

    private fun handleClientCertAliasSelected(
        request: ClientCertRequest,
        alias: String?
    ) {
        if (pendingClientCertRequest != request) {
            return
        }
        if (alias.isNullOrBlank()) {
            pendingClientCertRequest = null
            request.cancel()
            return
        }

        val appContext = applicationContext
        Thread {
            val credential = runCatching {
                val privateKey = KeyChain.getPrivateKey(appContext, alias)
                    ?: error("Client certificate private key is unavailable.")
                val certificateChain = KeyChain.getCertificateChain(appContext, alias)
                    ?: emptyArray()
                ClientCertificateCredential(privateKey, certificateChain)
            }.getOrElse { error ->
                if (error is InterruptedException) {
                    Thread.currentThread().interrupt()
                }
                null
            }

            runOnUiThread {
                if (pendingClientCertRequest != request) {
                    return@runOnUiThread
                }
                pendingClientCertRequest = null
                if (credential != null && credential.certificateChain.isNotEmpty()) {
                    request.proceed(credential.privateKey, credential.certificateChain)
                } else {
                    request.cancel()
                    Toast.makeText(
                        this,
                        R.string.toast_client_certificate_unavailable,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }.start()
    }

    private fun cancelPendingClientCertRequest() {
        pendingClientCertRequest?.cancel()
        pendingClientCertRequest = null
    }

    private fun interceptBrowserRequest(request: BrowserRequest) =
        adBlockRequestInterceptor.intercept(request) ?: smartNoImageRequestInterceptor.intercept(request)

    private fun showBrowserErrorPage(error: BrowserPageError) {
        currentSessionController().handlePageFailed(error.url)
        currentBrowserManager().loadErrorPage(error)
    }

    private fun handleHttpAuthRequest(
        view: WebView?,
        handler: HttpAuthHandler?,
        host: String?,
        realm: String?
    ) {
        val authHandler = handler ?: return
        cancelPendingHttpAuthRequest()
        pendingHttpAuthHandler = authHandler
        val usernameInput = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
            setSingleLine(true)
            hint = getString(R.string.hint_http_auth_username)
        }
        val passwordInput = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setSingleLine(true)
            hint = getString(R.string.hint_http_auth_password)
        }
        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
            addView(usernameInput)
            addView(passwordInput)
        }
        val displayHost = host?.takeIf { it.isNotBlank() }
            ?: getString(R.string.permission_origin_unknown)
        val displayRealm = realm?.takeIf { it.isNotBlank() }
        var completed = false
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.title_http_auth_request)
            .setMessage(
                displayRealm?.let { value ->
                    getString(R.string.dialog_http_auth_request_message_with_realm, displayHost, value)
                } ?: getString(R.string.dialog_http_auth_request_message, displayHost)
            )
            .setView(form)
            .setPositiveButton(R.string.action_http_auth_sign_in, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                completed = true
                pendingHttpAuthHandler = null
                pendingHttpAuthDialog = null
                authHandler.proceed(
                    usernameInput.text?.toString().orEmpty(),
                    passwordInput.text?.toString().orEmpty()
                )
                dialog.dismiss()
            }
        }
        dialog.setOnDismissListener {
            if (!completed) {
                completed = true
                if (pendingHttpAuthHandler == authHandler) {
                    pendingHttpAuthHandler = null
                }
                if (pendingHttpAuthDialog == dialog) {
                    pendingHttpAuthDialog = null
                }
                authHandler.cancel()
            }
        }
        pendingHttpAuthDialog = dialog
        dialog.show()
    }

    private fun cancelPendingHttpAuthRequest() {
        val dialog = pendingHttpAuthDialog
        val handler = pendingHttpAuthHandler
        pendingHttpAuthDialog = null
        pendingHttpAuthHandler = null
        dialog?.setOnDismissListener(null)
        dialog?.dismiss()
        handler?.cancel()
    }

    private fun setupFullscreenGestureOverlay() {
        fullscreenVideoController.attachOverlay()
    }

    private fun exitPageFullscreenIfNeeded() {
        if (areChromeClientsInitialized() &&
            currentChromeClient().isFullscreenModeActive() &&
            !currentChromeClient().isShowingCustomView()
        ) {
            currentChromeClient().exitPageFullscreen()
        }
    }

    private fun handleVideoFullscreenChanged(fullscreen: Boolean) {
        fullscreenVideoController.handleFullscreenChanged(fullscreen)
        setBrowserControlsHidden(fullscreen)
        updatePageProgressVisibility(forceHidden = fullscreen)
        ViewCompat.requestApplyInsets(rootView)
        if (!fullscreen) {
            applyBrowserContentOrientation(isDesktopModeEnabled())
        }
    }

    private fun showWebFileChooser(
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {
        val callback = filePathCallback ?: return false
        pendingFileChooserCallback?.onReceiveValue(null)
        pendingFileChooserCallback = callback

        val pickerIntent = runCatching {
            fileChooserParams?.createIntent() ?: defaultWebFileChooserIntent()
        }.getOrDefault(defaultWebFileChooserIntent())

        return try {
            webFileChooserLauncher.launch(
                Intent.createChooser(pickerIntent, getString(R.string.action_open_file))
            )
            true
        } catch (_: ActivityNotFoundException) {
            pendingFileChooserCallback = null
            callback.onReceiveValue(null)
            Toast.makeText(this, R.string.toast_file_chooser_unavailable, Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun cancelPendingWebFileChooser() {
        pendingFileChooserCallback?.onReceiveValue(null)
        pendingFileChooserCallback = null
    }

    private fun exportBookmarks() {
        bookmarkExportLauncher.launch(BOOKMARK_EXPORT_FILE_NAME)
    }

    private fun exportBookmarksToUri(uri: Uri) {
        val exported = runCatching {
            val payload = savedPageRepository.exportBookmarks().toByteArray(StandardCharsets.UTF_8)
            contentResolver.openOutputStream(uri)?.use { output ->
                output.write(payload)
            } ?: error("Unable to open bookmark export target")
        }.isSuccess

        Toast.makeText(
            this,
            if (exported) R.string.toast_bookmarks_exported else R.string.toast_bookmarks_export_failed,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun importBookmarks() {
        bookmarkImportLauncher.launch(arrayOf("text/plain", "application/json", "*/*"))
    }

    private fun importBookmarksFromUri(uri: Uri) {
        val result = runCatching {
            val payload = contentResolver.openInputStream(uri)?.use { input ->
                input.bufferedReader(StandardCharsets.UTF_8).readText()
            } ?: error("Unable to open bookmark import source")
            savedPageRepository.importBookmarks(payload)
        }.getOrElse {
            Toast.makeText(this, R.string.toast_bookmarks_import_failed, Toast.LENGTH_SHORT).show()
            return
        }

        val toastText = if (result.importedCount > 0) {
            getString(R.string.toast_bookmarks_imported, result.importedCount)
        } else {
            getString(R.string.toast_bookmarks_import_empty)
        }
        Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show()
        updateBookmarkButton()
    }

    private fun defaultWebFileChooserIntent(): Intent {
        return Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
    }

    private fun handleWebPermissionRequest(request: PermissionRequest?) {
        request ?: return
        val requiredPermissions = androidPermissionsForWebResources(request.resources)
        if (requiredPermissions == null) {
            request.deny()
            return
        }
        if (webPermissionDecision(request) == SitePermissionDecision.BLOCK) {
            request.deny()
            return
        }
        val missingPermissions = requiredPermissions
            .filterNot(::hasAndroidPermission)
            .toTypedArray()
        if (missingPermissions.isEmpty()) {
            handleWebPermissionRequestAfterAndroidPermission(request)
            return
        }

        pendingWebPermissionRequest?.deny()
        cancelPendingWebPermissionPrompt()
        pendingWebPermissionRequest = request
        webPermissionLauncher.launch(missingPermissions)
    }

    private fun handleWebPermissionRequestAfterAndroidPermission(request: PermissionRequest) {
        when (webPermissionDecision(request)) {
            SitePermissionDecision.ALLOW -> grantSupportedWebPermissionResources(request)
            SitePermissionDecision.BLOCK -> request.deny()
            SitePermissionDecision.ASK -> showWebPermissionPrompt(request)
        }
    }

    private fun handleWebPermissionRequestCanceled(request: PermissionRequest?) {
        if (request == null) {
            pendingWebPermissionRequest?.deny()
            pendingWebPermissionRequest = null
            cancelPendingWebPermissionPrompt()
            return
        }
        if (request == pendingWebPermissionRequest) {
            pendingWebPermissionRequest = null
        }
        if (request == pendingWebPermissionPromptRequest) {
            cancelPendingWebPermissionPrompt()
        }
    }

    private fun cancelPendingWebPermissionRequest() {
        pendingWebPermissionRequest?.deny()
        pendingWebPermissionRequest = null
        cancelPendingWebPermissionPrompt()
    }

    private fun showWebPermissionPrompt(request: PermissionRequest) {
        cancelPendingWebPermissionPrompt()
        pendingWebPermissionPromptRequest = request
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.title_web_permission_request)
            .setMessage(
                getString(
                    R.string.dialog_web_permission_request_message,
                    webPermissionOrigin(request),
                    webPermissionResourceSummary(request.resources)
                )
            )
            .setPositiveButton(R.string.action_allow) { _, _ ->
                answerWebPermissionPrompt(request, allowed = true)
            }
            .setNegativeButton(R.string.action_deny) { _, _ ->
                answerWebPermissionPrompt(request, allowed = false)
            }
            .create()
        dialog.setOnCancelListener {
            answerWebPermissionPrompt(request, allowed = false)
        }
        pendingWebPermissionDialog = dialog
        dialog.show()
    }

    private fun answerWebPermissionPrompt(request: PermissionRequest, allowed: Boolean) {
        if (pendingWebPermissionPromptRequest != request) {
            return
        }
        pendingWebPermissionPromptRequest = null
        pendingWebPermissionDialog = null
        if (allowed) {
            saveWebPermissionDecision(request, allowed = true)
            grantSupportedWebPermissionResources(request)
        } else {
            saveWebPermissionDecision(request, allowed = false)
            request.deny()
        }
    }

    private fun cancelPendingWebPermissionPrompt() {
        val request = pendingWebPermissionPromptRequest
        pendingWebPermissionPromptRequest = null
        pendingWebPermissionDialog?.dismiss()
        pendingWebPermissionDialog = null
        request?.deny()
    }

    private fun webPermissionOrigin(request: PermissionRequest): String {
        return request.origin
            ?.toString()
            ?.takeIf { origin -> origin.isNotBlank() }
            ?: getString(R.string.permission_origin_unknown)
    }

    private fun webPermissionResourceSummary(resources: Array<String>): String {
        return resources
            .mapNotNull { resource -> webPermissionResourceLabel(resource) }
            .distinct()
            .joinToString(", ")
    }

    private fun webPermissionResourceLabel(resource: String): String? {
        return when (resource) {
            PermissionRequest.RESOURCE_VIDEO_CAPTURE -> getString(R.string.web_permission_camera)
            PermissionRequest.RESOURCE_AUDIO_CAPTURE -> getString(R.string.web_permission_microphone)
            else -> null
        }
    }

    private fun webPermissionDecision(request: PermissionRequest): SitePermissionDecision {
        val hostName = SiteHost.fromUrl(request.origin?.toString()) ?: return SitePermissionDecision.ASK
        val decisions = request.resources
            .mapNotNull(::sitePermissionForWebResource)
            .map { permission -> settingsManager.sitePermissionDecision(hostName, permission) }
        return when {
            decisions.any { decision -> decision == SitePermissionDecision.BLOCK } -> SitePermissionDecision.BLOCK
            decisions.isNotEmpty() &&
                decisions.all { decision -> decision == SitePermissionDecision.ALLOW } -> SitePermissionDecision.ALLOW
            else -> SitePermissionDecision.ASK
        }
    }

    private fun saveWebPermissionDecision(request: PermissionRequest, allowed: Boolean) {
        val hostName = SiteHost.fromUrl(request.origin?.toString()) ?: return
        val decision = if (allowed) SitePermissionDecision.ALLOW else SitePermissionDecision.BLOCK
        request.resources
            .mapNotNull(::sitePermissionForWebResource)
            .forEach { permission ->
                settingsManager.setSitePermissionDecision(hostName, permission, decision)
            }
    }

    private fun sitePermissionForWebResource(resource: String): SitePermission? {
        return when (resource) {
            PermissionRequest.RESOURCE_VIDEO_CAPTURE -> SitePermission.CAMERA
            PermissionRequest.RESOURCE_AUDIO_CAPTURE -> SitePermission.MICROPHONE
            else -> null
        }
    }

    private fun grantSupportedWebPermissionResources(request: PermissionRequest) {
        val resources = supportedWebPermissionResources(request.resources)
        if (resources == null) {
            request.deny()
            return
        }

        request.grant(resources)
    }

    private fun supportedWebPermissionResources(resources: Array<String>): Array<String>? {
        val supportedResources = mutableListOf<String>()
        resources.forEach { resource ->
            when (resource) {
                PermissionRequest.RESOURCE_VIDEO_CAPTURE,
                PermissionRequest.RESOURCE_AUDIO_CAPTURE -> {
                    if (resource !in supportedResources) {
                        supportedResources += resource
                    }
                }
                else -> return null
            }
        }
        return supportedResources
            .takeIf { it.isNotEmpty() }
            ?.toTypedArray()
    }

    private fun handleGeolocationPermissionRequest(
        origin: String?,
        callback: GeolocationPermissions.Callback?
    ) {
        callback ?: return
        val siteDecision = geolocationPermissionDecision(origin)
        if (siteDecision == SitePermissionDecision.BLOCK) {
            denyGeolocationPermissionPrompt(origin, callback)
            return
        }
        val permissions = geolocationAndroidPermissions()
        if (permissions.any(::hasAndroidPermission)) {
            handleGeolocationPermissionAfterAndroidPermission(
                GeolocationPermissionPrompt(origin, callback)
            )
            return
        }

        cancelPendingGeolocationPermissionPrompt()
        pendingGeolocationPermissionPrompt = GeolocationPermissionPrompt(origin, callback)
        geolocationPermissionLauncher.launch(permissions)
    }

    private fun handleGeolocationPermissionAfterAndroidPermission(prompt: GeolocationPermissionPrompt) {
        when (geolocationPermissionDecision(prompt.origin)) {
            SitePermissionDecision.ALLOW -> prompt.callback.invoke(prompt.origin, true, false)
            SitePermissionDecision.BLOCK -> denyGeolocationPermissionPrompt(prompt.origin, prompt.callback)
            SitePermissionDecision.ASK -> showGeolocationPermissionPrompt(prompt)
        }
    }

    private fun handleGeolocationPermissionHidden() {
        cancelPendingGeolocationPermissionPrompt()
    }

    private fun cancelPendingGeolocationPermissionPrompt() {
        val prompt = pendingGeolocationPermissionPrompt
        pendingGeolocationPermissionPrompt = null
        prompt?.let { pendingPrompt ->
            denyGeolocationPermissionPrompt(pendingPrompt.origin, pendingPrompt.callback)
        }

        val sitePrompt = pendingGeolocationSitePrompt
        pendingGeolocationSitePrompt = null
        pendingGeolocationDialog?.dismiss()
        pendingGeolocationDialog = null
        sitePrompt?.let { pendingPrompt ->
            denyGeolocationPermissionPrompt(pendingPrompt.origin, pendingPrompt.callback)
        }
    }

    private fun showGeolocationPermissionPrompt(prompt: GeolocationPermissionPrompt) {
        cancelPendingGeolocationPermissionPrompt()
        pendingGeolocationSitePrompt = prompt
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.title_geolocation_permission_request)
            .setMessage(
                getString(
                    R.string.dialog_geolocation_permission_request_message,
                    prompt.origin?.takeIf { origin -> origin.isNotBlank() }
                        ?: getString(R.string.permission_origin_unknown)
                )
            )
            .setPositiveButton(R.string.action_allow) { _, _ ->
                answerGeolocationPermissionPrompt(prompt, allowed = true)
            }
            .setNegativeButton(R.string.action_deny) { _, _ ->
                answerGeolocationPermissionPrompt(prompt, allowed = false)
            }
            .create()
        dialog.setOnCancelListener {
            answerGeolocationPermissionPrompt(prompt, allowed = false)
        }
        pendingGeolocationDialog = dialog
        dialog.show()
    }

    private fun answerGeolocationPermissionPrompt(
        prompt: GeolocationPermissionPrompt,
        allowed: Boolean
    ) {
        if (pendingGeolocationSitePrompt != prompt) {
            return
        }
        pendingGeolocationSitePrompt = null
        pendingGeolocationDialog = null
        saveGeolocationPermissionDecision(prompt.origin, allowed)
        prompt.callback.invoke(prompt.origin, allowed, false)
    }

    private fun geolocationPermissionDecision(origin: String?): SitePermissionDecision {
        val hostName = SiteHost.fromUrl(origin) ?: return SitePermissionDecision.ASK
        return settingsManager.sitePermissionDecision(hostName, SitePermission.LOCATION)
    }

    private fun saveGeolocationPermissionDecision(origin: String?, allowed: Boolean) {
        val hostName = SiteHost.fromUrl(origin) ?: return
        settingsManager.setSitePermissionDecision(
            host = hostName,
            permission = SitePermission.LOCATION,
            decision = if (allowed) SitePermissionDecision.ALLOW else SitePermissionDecision.BLOCK
        )
    }

    private fun denyGeolocationPermissionPrompt(
        origin: String?,
        callback: GeolocationPermissions.Callback
    ) {
        callback.invoke(origin, false, false)
    }

    private fun geolocationAndroidPermissions(): Array<String> {
        return arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    private fun androidPermissionsForWebResources(resources: Array<String>): List<String>? {
        val permissions = mutableListOf<String>()
        resources.forEach { resource ->
            val permission = when (resource) {
                PermissionRequest.RESOURCE_VIDEO_CAPTURE -> Manifest.permission.CAMERA
                PermissionRequest.RESOURCE_AUDIO_CAPTURE -> Manifest.permission.RECORD_AUDIO
                else -> return null
            }
            if (permission !in permissions) {
                permissions += permission
            }
        }
        return permissions.takeIf { it.isNotEmpty() }
    }

    private fun hasAndroidPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun updatePageProgressVisibility(forceHidden: Boolean = false) {
        browserControlsController.updatePageProgressVisibility(
            currentSessionController().isPageLoading,
            forceHidden
        )
    }

    private fun setupWebViewScrollControls() {
        browserControlsScrollController.setup()
    }

    private fun setBrowserControlsHidden(hidden: Boolean, allowDefer: Boolean = true) {
        browserControlsScrollController.setControlsHidden(hidden, allowDefer)
    }

    private fun syncSearchProviderVisibility() {
        if (!::searchProviderController.isInitialized) {
            return
        }
        searchProviderController.syncVisibility(
            areBrowserControlsHidden = browserControlsController.areHidden,
            isVideoFullscreenUiActive = isVideoFullscreenUiActive,
            isHomePageVisible = isHomePageVisible
        )
        if (::addressSuggestionController.isInitialized) {
            addressSuggestionController.syncVisibility()
        }
    }

    private fun handleAddressFocusChanged(hasFocus: Boolean) {
        if (::addressSuggestionController.isInitialized) {
            addressSuggestionController.handleAddressFocusChanged(hasFocus)
        }
    }

    private fun setupSearchProviders() {
        searchProviderController.setup()
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (handleFunctionCenterBack()) {
                        return
                    } else if (elementPickerController.isActive) {
                        elementPickerController.cancel()
                    } else if (areChromeClientsInitialized() && currentChromeClient().isShowingCustomView()) {
                        currentChromeClient().hideCustomView()
                    } else if (areChromeClientsInitialized() && currentChromeClient().isFullscreenModeActive()) {
                        currentBrowserManager().evaluateJavascript(
                            WebViewVideoCommand.ExitFullscreen.toJavascript()
                        )
                        currentChromeClient().exitPageFullscreen()
                    } else if (currentBrowserManager().goBack()) {
                        updateNavigationButtons()
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )
    }

    private fun createNativeBridge(): VideoBrowserNativeBridge {
        return VideoBrowserNativeBridge(
            postToUi = { action -> runOnUiThread { action() } },
            enterFullscreen = {
                if (areChromeClientsInitialized()) {
                    currentChromeClient().enterPageFullscreen()
                }
            },
            exitFullscreen = {
                if (areChromeClientsInitialized()) {
                    currentChromeClient().exitPageFullscreen()
                }
            },
            updatePlaybackTimeline = fullscreenVideoController::updatePlaybackTimeline,
            requestElementBlock = elementPickerController::handlePickedElement,
            blockSelectedElement = { selector ->
                elementPickerController.handlePickedElement(selector, "")
            },
            cancelElementPicker = elementPickerController::handleCancelledFromPage,
            logVideoEvent = { message ->
                Log.d(VIDEO_LOG_TAG, message)
            }
        )
    }

    private fun startElementPicker() {
        elementPickerController.start()
    }

    private fun showFunctionCenter() {
        hideKeyboard()
        functionCenterPages.showRootPage()
    }

    private fun showFunctionCenterRootPage() {
        functionCenterPages.showRootPage()
    }

    private fun showFindInPageDialog() {
        closeFunctionCenter()
        val input = EditText(this).apply {
            hint = getString(R.string.hint_find_in_page)
            setSingleLine(true)
        }
        val status = TextView(this).apply {
            text = getString(R.string.find_in_page_status_idle)
            setPadding(0, dp(8), 0, 0)
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
            addView(input)
            addView(status)
        }
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.action_find_in_page)
            .setView(content)
            .setPositiveButton(R.string.action_find, null)
            .setNeutralButton(R.string.action_find_next, null)
            .setNegativeButton(R.string.action_find_previous, null)
            .create()
        dialog.setOnShowListener {
            currentBrowserManager().setFindResultListener { activeMatchOrdinal, numberOfMatches, isDoneCounting ->
                status.text = findInPageStatusText(activeMatchOrdinal, numberOfMatches, isDoneCounting)
            }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val started = findInPageController.search(input.text?.toString().orEmpty())
                if (!started) {
                    Toast.makeText(this, R.string.toast_find_query_empty, Toast.LENGTH_SHORT).show()
                    status.text = getString(R.string.find_in_page_status_idle)
                }
            }
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                val moved = findInPageController.findNext()
                if (!moved) {
                    Toast.makeText(this, R.string.toast_find_query_empty, Toast.LENGTH_SHORT).show()
                }
            }
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
                val moved = findInPageController.findPrevious()
                if (!moved) {
                    Toast.makeText(this, R.string.toast_find_query_empty, Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.setOnDismissListener {
            currentBrowserManager().setFindResultListener(null)
            findInPageController.clear()
        }
        dialog.show()
    }

    private fun findInPageStatusText(
        activeMatchOrdinal: Int,
        numberOfMatches: Int,
        isDoneCounting: Boolean
    ): String {
        return when {
            !isDoneCounting -> getString(R.string.find_in_page_status_counting)
            numberOfMatches <= 0 -> getString(R.string.find_in_page_status_no_matches)
            else -> getString(
                R.string.find_in_page_status_matches,
                activeMatchOrdinal + 1,
                numberOfMatches
            )
        }
    }

    private fun printCurrentPage() {
        val pageUrl = currentActionableUrl()
        if (pageUrl == null) {
            Toast.makeText(this, R.string.toast_print_page_unavailable, Toast.LENGTH_SHORT).show()
            return
        }
        val jobName = currentPrintJobName(pageUrl)
        runCatching {
            val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
            val printAdapter = currentBrowserManager().activeWebView.createPrintDocumentAdapter(jobName)
            printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
        }.onFailure {
            Toast.makeText(this, R.string.toast_print_page_unavailable, Toast.LENGTH_SHORT).show()
        }
    }

    private fun currentPrintJobName(pageUrl: String): String {
        val title = currentSessionController().currentPageTitle
            .replace(WHITESPACE_SEQUENCE, " ")
            .trim()
            .ifBlank { Uri.parse(pageUrl).host.orEmpty() }
            .ifBlank { getString(R.string.app_name) }
            .take(MAX_PRINT_JOB_TITLE_LENGTH)
        return getString(R.string.print_job_name, title)
    }

    private fun showProfilePage() {
        hideKeyboard()
        functionCenterPages.showProfilePage()
    }

    private fun handleFunctionCenterBack(): Boolean {
        return functionCenterPages.handleBack()
    }

    private fun closeFunctionCenter(): Boolean {
        return functionCenterPages.close()
    }

    private fun setupFileOperationLaunchers() {
        localFilesController.setupLaunchers()
    }

    private fun showFileOperationsPage() {
        localFilesController.showFileOperationsPage()
    }

    private fun openLocalDocumentUri(
        uri: Uri,
        displayName: String? = null,
        mimeType: String? = null,
        subtitleCandidates: List<ExternalSubtitleCandidate> = emptyList(),
        playbackQueue: PlaybackQueue? = null
    ) {
        pageActionsController.openLocalDocumentUri(
            uri,
            displayName,
            mimeType,
            subtitleCandidates,
            playbackQueue
        )
    }

    private fun openPlaybackHistoryItem(progress: PlaybackProgress) {
        openNativePlayer(
            url = progress.mediaIdentity,
            titleOverride = PlaybackHistoryDisplayText.title(progress)
        )
    }

    private fun updatePrivateBrowsingUi() {
        if (!::views.isInitialized || !::settingsManager.isInitialized) {
            return
        }
        privateBrowsingBadge.visibility = View.GONE
        applyBrowsingModeTheme()
    }

    private fun applyBrowsingModeTheme() {
        if (!::views.isInitialized) {
            return
        }

        val colors = if (isPrivateBrowsingEnabled()) {
            BrowserUiColors(
                background = Color.parseColor("#11151B"),
                surface = Color.parseColor("#181D25"),
                webViewBackground = Color.parseColor("#0B0F14"),
                addressBackground = Color.parseColor("#222936"),
                addressStroke = Color.parseColor("#303948"),
                text = Color.parseColor("#F4F7FB"),
                hint = Color.parseColor("#8F9BAD"),
                icon = Color.parseColor("#E9EEF7"),
                mutedIcon = Color.parseColor("#AAB4C3"),
                progress = Color.parseColor("#4D8DFF")
            )
        } else {
            BrowserUiColors(
                background = ContextCompat.getColor(this, R.color.browser_background),
                surface = ContextCompat.getColor(this, R.color.browser_surface),
                webViewBackground = ContextCompat.getColor(this, R.color.webview_background),
                addressBackground = ContextCompat.getColor(this, R.color.address_bar_background),
                addressStroke = ContextCompat.getColor(this, R.color.address_bar_stroke),
                text = ContextCompat.getColor(this, R.color.browser_text),
                hint = ContextCompat.getColor(this, R.color.browser_text_hint),
                icon = ContextCompat.getColor(this, R.color.browser_icon),
                mutedIcon = ContextCompat.getColor(this, R.color.browser_icon_muted),
                progress = ContextCompat.getColor(this, R.color.progress_active)
            )
        }

        rootView.setBackgroundColor(colors.background)
        topBar.setBackgroundColor(colors.surface)
        bottomBar.setBackgroundColor(colors.surface)
        searchProviderScroll.setBackgroundColor(colors.background)
        addressSuggestionPanel.setBackgroundColor(colors.surface)
        webViewContainer.setBackgroundColor(colors.webViewBackground)
        addressInput.setTextColor(colors.text)
        addressInput.setHintTextColor(colors.hint)
        views.addressBar.background = GradientDrawable().apply {
            cornerRadius = dp(22).toFloat()
            setColor(colors.addressBackground)
            setStroke(dp(1), colors.addressStroke)
        }
        if (areBrowserSessionsInitialized()) {
            updateSiteSecurityStatus(currentSessionController().currentPageUrl)
        }
        listOf(backButton, refreshButton, pageToolsButton, bookmarkButton, wenxinButton, profileButton).forEach { button ->
            button.setColorFilter(colors.icon)
        }
        pageProgress.progressTintList = ColorStateList.valueOf(colors.progress)
        WindowInsetsControllerCompat(window, rootView).isAppearanceLightStatusBars =
            !isPrivateBrowsingEnabled()
    }

    private fun isAdBlockEnabled(): Boolean {
        return pageFeatureCoordinator.isAdBlockEnabled()
    }

    private fun isCurrentSiteAdBlockDisabled(): Boolean {
        return pageFeatureCoordinator.isCurrentSiteAdBlockDisabled()
    }

    private fun isSmartNoImageEnabled(): Boolean {
        return pageFeatureCoordinator.isSmartNoImageEnabled()
    }

    private fun isJsInjectionEnabled(): Boolean {
        return pageFeatureCoordinator.isJsInjectionEnabled()
    }

    private fun isCurrentSiteJsInjectionDisabled(): Boolean {
        return pageFeatureCoordinator.isCurrentSiteJsInjectionDisabled()
    }

    private fun isPageCleanupEnabled(): Boolean {
        return pageFeatureCoordinator.isPageCleanupEnabled()
    }

    private fun isCurrentSitePageCleanupDisabled(): Boolean {
        return pageFeatureCoordinator.isCurrentSitePageCleanupDisabled()
    }

    private fun isVideoEnhancementEnabled(): Boolean {
        return pageFeatureCoordinator.isVideoEnhancementEnabled()
    }

    private fun isCurrentSiteVideoEnhancementDisabled(): Boolean {
        return pageFeatureCoordinator.isCurrentSiteVideoEnhancementDisabled()
    }

    private fun isDesktopModeEnabled(): Boolean {
        return settingsManager.isDesktopModeEnabled()
    }

    private fun isPrivateBrowsingEnabled(): Boolean {
        return privateBrowsingActive
    }

    private fun currentTabStore(): BrowserTabStore {
        return if (privateBrowsingActive) privateTabStore else standardTabStore
    }

    private fun currentTabs(): List<BrowserTab> {
        return currentTabStore().tabs()
    }

    private fun activeTabId(): Long {
        return currentTabStore().activeTabId
    }

    private fun openNewTab() {
        closeFunctionCenter()
        if (!privateBrowsingActive) {
            val result = standardTabWebViews.openTab(createStandardTabWebView())
            hideStandardTabWebView(result.previousView)
            showStandardTabWebView(result.activeView)
        } else {
            currentTabStore().openTab()
        }
        saveStandardTabSession()
        openHomePage()
    }

    private fun canReopenClosedTab(): Boolean {
        return currentTabStore().canReopenClosedTab()
    }

    private fun reopenClosedTab() {
        if (!privateBrowsingActive) {
            val reopenedTab = standardTabStore.reopenClosedTab() ?: return
            standardTabWebViews.activate(reopenedTab.id)
            saveStandardTabSession()
            reopenedTab.url?.let(::loadUrl) ?: openHomePage()
            return
        }

        val reopenedTab = currentTabStore().reopenClosedTab() ?: return
        reopenedTab.url?.let(::loadUrl) ?: openHomePage()
    }

    private fun switchTab(tabId: Long) {
        closeFunctionCenter()
        if (!privateBrowsingActive) {
            val result = standardTabWebViews.switchTo(tabId) ?: return
            if (result.previousView !== result.activeView) {
                hideStandardTabWebView(result.previousView)
                showStandardTabWebView(result.activeView)
            }
            showActiveTab(result.activeTab)
            saveStandardTabSession()
        } else {
            val tabStore = currentTabStore()
            if (!tabStore.switchTo(tabId)) {
                return
            }
            showActiveTab(tabStore.activeTab())
        }
    }

    private fun closeTab(tabId: Long) {
        if (!privateBrowsingActive) {
            val closingActiveTab = standardTabStore.activeTabId == tabId
            val result = standardTabWebViews.closeTab(tabId) ?: return
            if (closingActiveTab && result.closedView !== result.activeView) {
                showStandardTabWebView(result.activeView)
            }
            result.closedView?.let(::destroyStandardTabWebView)
            if (closingActiveTab) {
                showActiveTab(result.activeTab)
            }
            saveStandardTabSession()
            return
        }

        val tabStore = currentTabStore()
        val closingActiveTab = tabStore.activeTabId == tabId
        if (!tabStore.closeTab(tabId) || !closingActiveTab) {
            return
        }
        showActiveTab(tabStore.activeTab())
    }

    private fun closeOtherTabs(tabId: Long) {
        if (!privateBrowsingActive) {
            val previousView = standardTabWebViews.activeWebView()
            val result = standardTabWebViews.closeOtherTabs(tabId) ?: return
            if (previousView !== result.activeView) {
                hideStandardTabWebView(previousView)
                showStandardTabWebView(result.activeView)
            }
            result.closedViews.forEach(::destroyStandardTabWebView)
            showActiveTab(result.activeTab)
            saveStandardTabSession()
            return
        }

        val tabStore = currentTabStore()
        val closedTabs = tabStore.closeOtherTabs(tabId)
        if (closedTabs.isEmpty()) {
            return
        }
        showActiveTab(tabStore.activeTab())
    }

    private fun closeAllTabs() {
        if (!privateBrowsingActive) {
            val result = standardTabWebViews.closeAllTabs()
            showStandardTabWebView(result.activeView)
            result.closedViews.forEach(::destroyStandardTabWebView)
            saveStandardTabSession()
            openHomePage()
            return
        }

        currentTabStore().closeAllTabs()
        openHomePage()
    }

    private fun duplicateTab(tabId: Long) {
        val sourceTab = currentTabStore().tabs().firstOrNull { tab -> tab.id == tabId } ?: return
        if (!privateBrowsingActive) {
            val result = standardTabWebViews.openTab(
                view = createStandardTabWebView(),
                url = sourceTab.url,
                title = sourceTab.title
            )
            hideStandardTabWebView(result.previousView)
            showStandardTabWebView(result.activeView)
            saveStandardTabSession()
        } else {
            currentTabStore().openTab(url = sourceTab.url, title = sourceTab.title)
        }
        sourceTab.url?.let(::loadUrl) ?: openHomePage()
    }

    private fun openUrlInNewTab(url: String) {
        if (!privateBrowsingActive) {
            val result = standardTabWebViews.openTab(
                view = createStandardTabWebView(),
                url = url
            )
            hideStandardTabWebView(result.previousView)
            showStandardTabWebView(result.activeView)
            saveStandardTabSession()
        } else {
            currentTabStore().openTab(url = url)
        }
        loadUrl(url)
    }

    private fun showActiveTab(tab: BrowserTab) {
        if (!privateBrowsingActive) {
            standardTabWebViews.viewFor(tab.id)?.let(::showStandardTabWebView)
            standardSessionController.restorePageMetadata(tab.url, tab.title)
            return
        }

        tab.url?.let(::loadUrl) ?: openHomePage()
    }

    private fun setPrivateBrowsingActive(enabled: Boolean) {
        if (enabled == privateBrowsingActive) {
            updatePrivateBrowsingUi()
            return
        }

        closeFunctionCenter()
        if (::elementPickerController.isInitialized && elementPickerController.isActive) {
            elementPickerController.cancel()
        }
        exitPageFullscreenIfNeeded()

        if (enabled) {
            val started = browserSessionCoordinator.enterPrivate()
            if (!started) {
                Toast.makeText(this, R.string.toast_private_browsing_failed, Toast.LENGTH_SHORT).show()
                return
            }
            privateSessionController.reset()
            openHomePage()
        } else {
            browserSessionCoordinator.exitPrivate()
            standardSessionController.renderCurrentState(forceProgressHidden = true)
        }
        updatePrivateBrowsingUi()
        updateNavigationButtons()
    }

    private fun setupDownloadHandling() {
        downloadController.attachTo(browserManagers())
    }

    private fun applyDesktopMode(reload: Boolean) {
        val desktopModeEnabled = isDesktopModeEnabled()
        applyBrowserContentOrientation(desktopModeEnabled)
        currentBrowserManager().applyDesktopMode(
            enabled = desktopModeEnabled,
            desktopUserAgent = DESKTOP_USER_AGENT,
            defaultUserAgent = defaultUserAgent,
            reload = reload
        )
    }

    private fun applyBrowserContentOrientation(desktopModeEnabled: Boolean) {
        if (areChromeClientsInitialized() && currentChromeClient().isFullscreenModeActive()) {
            return
        }
        requestedOrientation = if (desktopModeEnabled) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    private fun injectPageFeatures() {
        if (!::pageFeatureCoordinator.isInitialized) {
            return
        }
        pageFeatureCoordinator.injectPageFeatures()
    }

    private fun currentShareableUrl(): String? {
        return currentActionableUrl()
    }

    private fun currentActionableUrl(): String? {
        return listOf(currentSessionController().currentPageUrl, currentBrowserManager().currentUrl())
            .firstOrNull { url -> !url.isNullOrBlank() && isShareableUrl(url) }
    }

    private fun currentSiteHost(): String? {
        return SiteHost.fromUrl(currentSessionController().currentPageUrl)
    }

    private fun isShareableUrl(url: String): Boolean {
        val scheme = Uri.parse(url).scheme
        return scheme.equals("http", ignoreCase = true) ||
            scheme.equals("https", ignoreCase = true)
    }

    private fun openExternalUrl(url: String) {
        externalNavigator.openExternalUrl(url)
    }

    private fun configureLinkContextMenu(targetWebView: WebView) {
        targetWebView.setOnLongClickListener { view ->
            val hitTestResult = (view as? WebView)?.hitTestResult
                ?: return@setOnLongClickListener false
            linkHitTestUrl(hitTestResult)?.let { linkUrl ->
                showLinkContextMenu(linkUrl)
                return@setOnLongClickListener true
            }
            val imageUrl = imageHitTestUrl(hitTestResult)
                ?: return@setOnLongClickListener false
            showImageContextMenu(imageUrl)
            true
        }
    }

    private fun imageHitTestUrl(hitTestResult: WebView.HitTestResult?): String? {
        if (hitTestResult?.type != WebView.HitTestResult.IMAGE_TYPE) {
            return null
        }
        return hitTestResult.extra
            ?.trim()
            ?.takeIf(::isShareableUrl)
    }

    private fun showImageContextMenu(url: String) {
        val actions = arrayOf(
            getString(R.string.action_open_image_new_tab),
            getString(R.string.action_download_image),
            getString(R.string.action_copy_image_link),
            getString(R.string.action_share_image_link),
            getString(R.string.action_open_external)
        )
        AlertDialog.Builder(this)
            .setTitle(R.string.title_image_context_menu)
            .setItems(actions) { dialog, which ->
                when (which) {
                    0 -> openUrlInNewTab(url)
                    1 -> downloadImageUrl(url)
                    2 -> copyImageUrl(url)
                    3 -> shareImageUrl(url)
                    4 -> openExternalUrl(url)
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun downloadImageUrl(url: String) {
        downloadLinkUrl(url)
    }

    private fun copyImageUrl(url: String) {
        copyLinkUrl(url)
    }

    private fun shareImageUrl(url: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.action_share_image_link)))
    }

    private fun linkHitTestUrl(hitTestResult: WebView.HitTestResult?): String? {
        val type = hitTestResult?.type ?: return null
        if (type != WebView.HitTestResult.SRC_ANCHOR_TYPE &&
            type != WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE
        ) {
            return null
        }
        return hitTestResult.extra
            ?.trim()
            ?.takeIf(::isShareableUrl)
    }

    private fun showLinkContextMenu(url: String) {
        val actions = arrayOf(
            getString(R.string.action_open_link_new_tab),
            getString(R.string.action_download_link),
            getString(R.string.action_copy_link),
            getString(R.string.action_share_link),
            getString(R.string.action_open_external)
        )
        AlertDialog.Builder(this)
            .setTitle(R.string.title_link_context_menu)
            .setItems(actions) { dialog, which ->
                when (which) {
                    0 -> openUrlInNewTab(url)
                    1 -> downloadLinkUrl(url)
                    2 -> copyLinkUrl(url)
                    3 -> shareLinkUrl(url)
                    4 -> openExternalUrl(url)
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun downloadLinkUrl(url: String) {
        downloadController.enqueue(
            url = url,
            userAgent = currentBrowserManager().userAgentString(),
            contentDisposition = null,
            mimeType = null
        )
    }

    private fun copyLinkUrl(url: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(
            ClipData.newPlainText(getString(R.string.clipboard_page_url), url)
        )
        Toast.makeText(this, R.string.toast_link_copied, Toast.LENGTH_SHORT).show()
    }

    private fun shareLinkUrl(url: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.action_share_link)))
    }

    private fun openNativePlayer(
        url: String,
        mimeType: String? = null,
        userAgentOverride: String? = null,
        titleOverride: String? = null,
        subtitleCandidates: List<ExternalSubtitleCandidate> = emptyList(),
        playbackQueue: PlaybackQueue? = null
    ) {
        externalNavigator.openNativePlayer(
            url = url,
            mimeType = mimeType,
            userAgentOverride = userAgentOverride,
            titleOverride = titleOverride,
            privateBrowsing = isPrivateBrowsingEnabled(),
            subtitleCandidates = subtitleCandidates,
            playbackQueue = playbackQueue
        )
    }

    private fun openNativePlayer(decision: MediaRouteDecision) {
        val mediaItem = decision.mediaItem ?: return
        openNativePlayer(
            mediaItem.uri,
            mediaItem.mimeType,
            mediaItem.userAgent,
            mediaItem.title,
            mediaItem.subtitleCandidates,
            null
        )
    }

    private fun loadAddressInput() {
        val input = addressInput.text?.toString()?.trim().orEmpty()
        addressSuggestionController.runWithSuggestionsSuppressed {
            UrlUtils.resolveAddressInput(
                input,
                searchProviderController.selectedProvider.searchUrlPrefix
            )
                ?.let { loadUrl(it) }
        }
    }

    private fun searchAddressKeyword(keyword: String) {
        val query = keyword.replace(WHITESPACE_SEQUENCE, " ").trim()
        if (query.isEmpty()) {
            return
        }
        val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.name())
        loadUrl("${searchProviderController.selectedProvider.searchUrlPrefix}$encodedQuery")
    }

    private fun openHomePage() {
        loadUrl(settingsManager.homeUrlOr(searchProviderController.selectedProvider.homeUrl))
    }

    private fun openInitialStandardPage() {
        val restoredUrl = standardTabStore.activeTab().url
        if (restoredUrl.isNullOrBlank()) {
            openHomePage()
        } else {
            loadUrl(restoredUrl)
        }
    }

    private fun openWenxinPage() {
        loadUrl(BAIDU_WENXIN_URL)
    }

    private fun handleLaunchIntent(intent: Intent?): Boolean {
        val launchUrl = externalWebUrlFromIntent(intent) ?: return false
        loadUrl(launchUrl)
        return true
    }

    private fun externalWebUrlFromIntent(intent: Intent?): String? {
        if (intent?.action != Intent.ACTION_VIEW) {
            return null
        }
        return intent.dataString
            ?.trim()
            ?.takeIf { it.isNotEmpty() && isShareableUrl(it) }
    }

    private fun loadUrl(url: String) {
        val cleanedUrl = if (::ruleEngine.isInitialized) {
            ruleEngine.cleanNavigationUrl(url, currentSessionController().currentPageUrl)
        } else {
            url
        }
        closeFunctionCenter()
        val mediaDecision = MediaRoutingController.route(
            MediaRouteRequest(
                source = MediaRouteSource.ADDRESS_BAR,
                url = cleanedUrl,
                currentPageUrl = currentSessionController().currentPageUrl,
                currentPageTitle = currentSessionController().currentPageTitle,
                userAgent = currentBrowserManager().userAgentString()
            )
        )
        when (mediaDecision.action) {
            MediaRouteAction.OPEN_NATIVE_PLAYER -> {
                openNativePlayer(mediaDecision)
                return
            }

            MediaRouteAction.BLOCK -> return
            else -> Unit
        }

        currentSessionController().currentPageUrl = cleanedUrl
        val isProviderHome = isProviderHomeUrl(cleanedUrl)
        updateAddressBar(cleanedUrl)
        hideKeyboard()
        showHomeContent(isProviderHome)
        currentBrowserManager().load(cleanedUrl)
    }

    private fun updateAddressBar(url: String?) {
        updateSiteSecurityStatus(url)
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

    private fun updateSiteSecurityStatus(url: String?) {
        when (SiteSecurityStatus.fromUrl(url)) {
            SiteSecurityStatus.SECURE -> showSiteSecurityStatus(
                iconResId = R.drawable.ic_lock_24,
                colorResId = R.color.site_security_secure,
                descriptionResId = R.string.site_security_secure
            )

            SiteSecurityStatus.NOT_SECURE -> showSiteSecurityStatus(
                iconResId = R.drawable.ic_warning_24,
                colorResId = R.color.site_security_insecure,
                descriptionResId = R.string.site_security_not_secure
            )

            SiteSecurityStatus.UNKNOWN -> {
                siteSecurityIcon.visibility = View.GONE
                siteSecurityIcon.contentDescription = null
                ViewCompat.setTooltipText(siteSecurityIcon, null)
                siteSecurityIcon.isEnabled = false
            }
        }
    }

    private fun showSiteSecurityStatus(
        iconResId: Int,
        colorResId: Int,
        descriptionResId: Int
    ) {
        val description = getString(descriptionResId)
        val actionDescription = getString(R.string.site_security_icon_description, description)
        siteSecurityIcon.visibility = View.VISIBLE
        siteSecurityIcon.isEnabled = true
        siteSecurityIcon.setImageResource(iconResId)
        siteSecurityIcon.setColorFilter(ContextCompat.getColor(this, colorResId))
        siteSecurityIcon.contentDescription = actionDescription
        ViewCompat.setTooltipText(siteSecurityIcon, actionDescription)
    }

    private fun showSiteSecurityInfoDialog() {
        val pageUrl = listOf(
            currentSessionController().currentPageUrl,
            currentBrowserManager().currentUrl()
        ).firstOrNull { url -> !url.isNullOrBlank() } ?: return
        val status = SiteSecurityStatus.fromUrl(pageUrl)
        if (status == SiteSecurityStatus.UNKNOWN) {
            return
        }

        val statusTitleResId = when (status) {
            SiteSecurityStatus.SECURE -> R.string.site_security_secure
            SiteSecurityStatus.NOT_SECURE -> R.string.site_security_not_secure
            SiteSecurityStatus.UNKNOWN -> R.string.title_site_security_info
        }
        val messageResId = when (status) {
            SiteSecurityStatus.SECURE -> R.string.site_security_secure_message
            SiteSecurityStatus.NOT_SECURE -> R.string.site_security_not_secure_message
            SiteSecurityStatus.UNKNOWN -> R.string.site_security_unknown_message
        }
        val displayUrl = UrlUtils.displayUrl(pageUrl)
        val host = SiteHost.fromUrl(pageUrl)
            ?: Uri.parse(pageUrl).host.orEmpty().ifBlank { displayUrl }
        val message = listOf(
            getString(statusTitleResId),
            getString(R.string.site_security_host, host),
            getString(R.string.site_security_url, displayUrl),
            getString(R.string.site_security_protocol, status.protocolDisplayName()),
            getString(messageResId),
            siteSecurityCertificateSummary(status),
            siteSecurityMixedContentSummary(status)
        ).joinToString(separator = "\n\n")

        val builder = AlertDialog.Builder(this)
            .setTitle(R.string.title_site_security_info)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
        if (!isPrivateBrowsingEnabled() && currentSiteHost() != null) {
            builder.setNeutralButton(R.string.action_site_settings) { _, _ ->
                showCurrentSiteSettingsPage()
            }
        }
        builder.show()
    }

    private fun siteSecurityCertificateSummary(status: SiteSecurityStatus): String {
        return when (status) {
            SiteSecurityStatus.SECURE -> getString(R.string.site_security_certificate_validated)
            SiteSecurityStatus.NOT_SECURE -> getString(R.string.site_security_certificate_not_used)
            SiteSecurityStatus.UNKNOWN -> getString(R.string.site_security_unknown_message)
        }
    }

    private fun siteSecurityMixedContentSummary(status: SiteSecurityStatus): String {
        return when (status) {
            SiteSecurityStatus.SECURE -> if (settingsManager.isMixedContentBlocked()) {
                getString(R.string.site_security_mixed_content_blocked)
            } else {
                getString(R.string.site_security_mixed_content_compatibility)
            }

            SiteSecurityStatus.NOT_SECURE -> getString(R.string.site_security_mixed_content_not_applicable)
            SiteSecurityStatus.UNKNOWN -> getString(R.string.site_security_unknown_message)
        }
    }

    private fun showCurrentSiteSettingsPage() {
        hideKeyboard()
        functionCenterPages.showCurrentSiteSettingsPage()
    }

    private fun addressBarDisplayText(url: String): String {
        return searchProviderController.addressBarDisplayText(url)
    }

    private fun updateNavigationButtons() {
        browserControlsController.updateNavigationButtons()
    }

    private fun updateBookmarkButton() {
        if (::browserControlsController.isInitialized) {
            browserControlsController.updateBookmarkButton()
        }
    }

    private fun showHomeContent(show: Boolean) {
        browserControlsScrollController.resetTracking()
        setBrowserControlsHidden(false)
        syncSearchProviderVisibility()
        webView.visibility = View.VISIBLE
        updatePageProgressVisibility(forceHidden = show)
        updateNavigationButtons()
        applyBrowsingModeTheme()
    }

    private fun hideKeyboard() {
        if (::addressSuggestionController.isInitialized) {
            addressSuggestionController.hide()
        }
        addressInput.clearFocus()
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(addressInput.windowToken, 0)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun isWebUrl(scheme: String?): Boolean {
        return scheme.equals("http", ignoreCase = true) ||
            scheme.equals("https", ignoreCase = true) ||
            scheme.equals("about", ignoreCase = true)
    }

    private fun shouldBlockUrl(view: WebView?, uri: Uri, openMedia: Boolean = true): Boolean {
        if (openMedia) {
            val mediaDecision = MediaRoutingController.route(
                MediaRouteRequest(
                    source = MediaRouteSource.WEBVIEW_OVERRIDE,
                    url = uri.toString(),
                    currentPageUrl = currentSessionController().currentPageUrl,
                    currentPageTitle = currentSessionController().currentPageTitle,
                    userAgent = currentBrowserManager().userAgentString()
                )
            )
            when (mediaDecision.action) {
                MediaRouteAction.OPEN_NATIVE_PLAYER -> {
                    view?.stopLoading()
                    openNativePlayer(mediaDecision)
                    return true
                }

                MediaRouteAction.BLOCK -> {
                    if (openExternalProtocolNavigation(view, uri)) {
                        return true
                    }
                    return true
                }
                else -> Unit
            }
        }

        if (isUnavailableUcDownloadUrl(uri)) {
            view?.stopLoading()
            Toast.makeText(this, R.string.toast_uc_download_unavailable, Toast.LENGTH_SHORT).show()
            return true
        }

        if (!isWebUrl(uri.scheme)) {
            if (openExternalProtocolNavigation(view, uri)) {
                return true
            }
            return true
        }

        if (openMedia && ::ruleEngine.isInitialized) {
            val originalUrl = uri.toString()
            val cleanedUrl = ruleEngine.cleanNavigationUrl(
                url = originalUrl,
                pageUrl = currentSessionController().currentPageUrl
            )
            if (cleanedUrl != originalUrl) {
                view?.stopLoading()
                loadUrl(cleanedUrl)
                return true
            }
        }

        return false
    }

    private fun openExternalProtocolNavigation(view: WebView?, uri: Uri): Boolean {
        if (!ExternalProtocolPolicy.shouldOpenExternally(uri.scheme)) {
            return false
        }
        view?.stopLoading()
        showExternalProtocolConfirmation(uri)
        return true
    }

    private fun showExternalProtocolConfirmation(uri: Uri) {
        AlertDialog.Builder(this)
            .setTitle(R.string.title_external_protocol_request)
            .setMessage(
                getString(
                    R.string.dialog_external_protocol_request_message,
                    externalProtocolDisplay(uri)
                )
            )
            .setPositiveButton(R.string.action_open_external_app) { _, _ ->
                openConfirmedExternalProtocol(uri)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun openConfirmedExternalProtocol(uri: Uri) {
        externalNavigator.openExternalProtocolUrl(uri.toString()) { fallbackUrl ->
            loadUrl(fallbackUrl)
        }
    }

    private fun externalProtocolDisplay(uri: Uri): String {
        return uri.scheme
            ?.takeIf { scheme -> scheme.isNotBlank() }
            ?.let { scheme -> "$scheme:" }
            ?: uri.toString()
    }

    private fun isUnavailableUcDownloadUrl(uri: Uri): Boolean {
        val host = uri.host?.lowercase().orEmpty()
        val path = uri.path.orEmpty()
        return (host == "down2.uc.cn" && path == "/ucbrowser/v2/down.php") ||
            (host == "umcdn-oss.oss-cn-beijing.aliyuncs.com" &&
                path.contains("/gongyp/shenmainuc8/") &&
                path.endsWith(".apk", ignoreCase = true))
    }

    private fun isProviderHomeUrl(url: String?): Boolean {
        return searchProviderController.isProviderHomeUrl(url)
    }

    private data class BrowserUiColors(
        val background: Int,
        val surface: Int,
        val webViewBackground: Int,
        val addressBackground: Int,
        val addressStroke: Int,
        val text: Int,
        val hint: Int,
        val icon: Int,
        val mutedIcon: Int,
        val progress: Int
    )

    private data class GeolocationPermissionPrompt(
        val origin: String?,
        val callback: GeolocationPermissions.Callback
    )

    private data class ClientCertificateCredential(
        val privateKey: PrivateKey,
        val certificateChain: Array<X509Certificate>
    )

    companion object {
        private const val NATIVE_BRIDGE_NAME = "VideoBrowserNative"
        private const val RULE_LOG_TAG = "VideoBrowserRules"
        private const val VIDEO_LOG_TAG = "VideoBrowserVideo"
        private const val BROWSER_CONTROLS_SCROLL_THRESHOLD_DP = 48
        private const val BROWSER_CONTROLS_SCROLL_COOLDOWN_MS = 500L
        private const val BAIDU_WENXIN_URL = "https://chat.baidu.com/"
        private const val BOOKMARK_EXPORT_FILE_NAME = "videobrowser-bookmarks.txt"
        private const val MAX_PRINT_JOB_TITLE_LENGTH = 80
        private val WHITESPACE_SEQUENCE = Regex("\\s+")
        private const val DESKTOP_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
    }
}
