package com.example.videobrowser

import android.Manifest
import android.content.ActivityNotFoundException
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
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebView
import android.webkit.WebChromeClient.FileChooserParams
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageButton
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
import com.example.videobrowser.browser.BrowserTabSessionBinding
import com.example.videobrowser.browser.BrowserTabStore
import com.example.videobrowser.browser.BrowserTabWebViewRegistry
import com.example.videobrowser.browser.ChromeClient
import com.example.videobrowser.browser.FindInPageController
import com.example.videobrowser.browser.PageActionsController
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
    private var pendingWebPermissionRequest: PermissionRequest? = null
    private val webPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
            val request = pendingWebPermissionRequest ?: return@registerForActivityResult
            pendingWebPermissionRequest = null
            val requiredPermissions = androidPermissionsForWebResources(request.resources)
            if (requiredPermissions != null && requiredPermissions.all { permission ->
                    grants[permission] == true || hasAndroidPermission(permission)
                }
            ) {
                request.grant(request.resources)
            } else {
                request.deny()
            }
        }
    private var pendingGeolocationPermissionPrompt: GeolocationPermissionPrompt? = null
    private val geolocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
            val prompt = pendingGeolocationPermissionPrompt ?: return@registerForActivityResult
            pendingGeolocationPermissionPrompt = null
            val allowed = geolocationAndroidPermissions().any { permission ->
                grants[permission] == true || hasAndroidPermission(permission)
            }
            prompt.callback.invoke(prompt.origin, allowed, false)
        }

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
        downloadRecordRepository = DownloadRecordRepository(preferenceStore)
        playbackHistoryRepository = PlaybackHistoryRepository(preferenceStore)
        browserDefaultSettingsResetter = BrowserDefaultSettingsResetter(
            settingsManager = settingsManager,
            savedPageRepository = savedPageRepository,
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
            onPageMetadataChanged = standardTabSessionBinding::handlePageMetadataChanged
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
            switchTab = ::switchTab,
            closeTab = ::closeTab,
            toggleCurrentBookmark = pageActionsController::toggleCurrentBookmark,
            copyCurrentUrl = pageActionsController::copyCurrentUrl,
            shareCurrentUrl = pageActionsController::shareCurrentUrl,
            openCurrentUrlExternally = pageActionsController::openCurrentUrlExternally,
            findInPage = ::showFindInPageDialog,
            openCurrentUrlInNativePlayer = pageActionsController::openCurrentUrlInNativePlayer,
            openPlaybackHistoryItem = ::openPlaybackHistoryItem,
            downloadCurrentUrl = pageActionsController::downloadCurrentUrl,
            retryDownload = downloadController::retry,
            setPrivateBrowsingEnabled = pageActionsController::setPrivateBrowsingEnabled,
            restoreDefaultSettings = pageActionsController::restoreDefaultSettings,
            showFileOperationsPage = ::showFileOperationsPage,
            startElementPicker = ::startElementPicker,
            applyDesktopMode = ::applyDesktopMode,
            injectPageFeatures = ::injectPageFeatures,
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
        standardBrowserManager.setPrivateBrowsingEnabled(false)
        defaultUserAgent = standardBrowserManager.userAgentString()
        applyDesktopMode(reload = false)
        setupDownloadHandling()
        setupChromeClient()
        setupFullscreenGestureOverlay()
        standardBrowserManager.addJavascriptInterface(createNativeBridge(), NATIVE_BRIDGE_NAME)
        setupBrowserClient()

        if (!handleLaunchIntent(intent)) {
            openHomePage()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleLaunchIntent(intent)
    }

    override fun onPause() {
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
        if (tabWebView.parent == null) {
            webViewContainer.addView(tabWebView)
        }
        tabWebView.visibility = View.VISIBLE
        browserSessionCoordinator.setStandardWebView(tabWebView)
        standardBrowserManager.switchWebView(
            nextWebView = tabWebView,
            privateBrowsingEnabled = false
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
            newWindowRequested = ::handleCreateWebWindow
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
        transport.webView = tabWebView
        resultMsg?.sendToTarget()
        return true
    }

    private fun setupBrowserClient() {
        currentBrowserManager().setBrowserClient(
            BrowserClient(
                pageStarted = { url -> currentSessionController().handlePageStarted(url) },
                pageFinished = { url -> currentSessionController().handlePageFinished(url) },
                pageLoadFailed = ::showBrowserErrorPage,
                requestIntercepted = ::interceptBrowserRequest,
                urlLoadingRequested = ::shouldBlockUrl
            )
        )
    }

    private fun interceptBrowserRequest(request: BrowserRequest) =
        adBlockRequestInterceptor.intercept(request) ?: smartNoImageRequestInterceptor.intercept(request)

    private fun showBrowserErrorPage(error: BrowserPageError) {
        currentSessionController().handlePageFailed(error.url)
        currentBrowserManager().loadErrorPage(error)
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
        val missingPermissions = requiredPermissions
            .filterNot(::hasAndroidPermission)
            .toTypedArray()
        if (missingPermissions.isEmpty()) {
            request.grant(request.resources)
            return
        }

        pendingWebPermissionRequest?.deny()
        pendingWebPermissionRequest = request
        webPermissionLauncher.launch(missingPermissions)
    }

    private fun handleWebPermissionRequestCanceled(request: PermissionRequest?) {
        if (request == null || request == pendingWebPermissionRequest) {
            pendingWebPermissionRequest = null
        }
    }

    private fun cancelPendingWebPermissionRequest() {
        pendingWebPermissionRequest?.deny()
        pendingWebPermissionRequest = null
    }

    private fun handleGeolocationPermissionRequest(
        origin: String?,
        callback: GeolocationPermissions.Callback?
    ) {
        callback ?: return
        val permissions = geolocationAndroidPermissions()
        if (permissions.any(::hasAndroidPermission)) {
            callback.invoke(origin, true, false)
            return
        }

        cancelPendingGeolocationPermissionPrompt()
        pendingGeolocationPermissionPrompt = GeolocationPermissionPrompt(origin, callback)
        geolocationPermissionLauncher.launch(permissions)
    }

    private fun handleGeolocationPermissionHidden() {
        cancelPendingGeolocationPermissionPrompt()
    }

    private fun cancelPendingGeolocationPermissionPrompt() {
        val prompt = pendingGeolocationPermissionPrompt ?: return
        pendingGeolocationPermissionPrompt = null
        denyGeolocationPermissionPrompt(prompt.origin, prompt.callback)
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
        return permissions
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
        AlertDialog.Builder(this)
            .setTitle(R.string.action_find_in_page)
            .setView(input)
            .setPositiveButton(R.string.action_find) { _, _ ->
                val started = findInPageController.search(input.text?.toString().orEmpty())
                if (!started) {
                    Toast.makeText(this, R.string.toast_find_query_empty, Toast.LENGTH_SHORT).show()
                }
            }
            .setNeutralButton(R.string.action_find_next) { _, _ ->
                val moved = findInPageController.findNext()
                if (!moved) {
                    Toast.makeText(this, R.string.toast_find_query_empty, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.action_clear) { _, _ ->
                findInPageController.clear()
            }
            .show()
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
        openHomePage()
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
            destroyStandardTabWebView(result.closedView)
            if (closingActiveTab) {
                showActiveTab(result.activeTab)
            }
            return
        }

        val tabStore = currentTabStore()
        val closingActiveTab = tabStore.activeTabId == tabId
        if (!tabStore.closeTab(tabId) || !closingActiveTab) {
            return
        }
        showActiveTab(tabStore.activeTab())
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
        var fallbackLoaded = false
        val handled = externalNavigator.openExternalProtocolUrl(uri.toString()) { fallbackUrl ->
            fallbackLoaded = true
            loadUrl(fallbackUrl)
        }
        if (handled && !fallbackLoaded) {
            view?.stopLoading()
        }
        return handled
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

    companion object {
        private const val NATIVE_BRIDGE_NAME = "VideoBrowserNative"
        private const val RULE_LOG_TAG = "VideoBrowserRules"
        private const val VIDEO_LOG_TAG = "VideoBrowserVideo"
        private const val BROWSER_CONTROLS_SCROLL_THRESHOLD_DP = 48
        private const val BROWSER_CONTROLS_SCROLL_COOLDOWN_MS = 500L
        private const val BAIDU_WENXIN_URL = "https://chat.baidu.com/"
        private val WHITESPACE_SEQUENCE = Regex("\\s+")
        private const val DESKTOP_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
    }
}
