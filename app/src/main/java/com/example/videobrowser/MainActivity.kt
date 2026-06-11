package com.example.videobrowser

import android.content.Context
import android.content.res.ColorStateList
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
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
import com.example.videobrowser.browser.BrowserExternalNavigator
import com.example.videobrowser.browser.HistoryRecordPolicy
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.browser.BrowserMode
import com.example.videobrowser.browser.BrowserRequest
import com.example.videobrowser.browser.BrowserSessionController
import com.example.videobrowser.browser.BrowserSessionCoordinator
import com.example.videobrowser.browser.ChromeClient
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
import com.example.videobrowser.video.PlaybackQueue
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
            injectPageFeatures = ::injectPageFeatures
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
            injectPageFeatures = ::injectPageFeatures
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
            toggleCurrentBookmark = pageActionsController::toggleCurrentBookmark,
            copyCurrentUrl = pageActionsController::copyCurrentUrl,
            shareCurrentUrl = pageActionsController::shareCurrentUrl,
            openCurrentUrlExternally = pageActionsController::openCurrentUrlExternally,
            openCurrentUrlInNativePlayer = pageActionsController::openCurrentUrlInNativePlayer,
            downloadCurrentUrl = pageActionsController::downloadCurrentUrl,
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

        openHomePage()
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
        if (::standardBrowserManager.isInitialized) {
            standardBrowserManager.destroy()
        }
        super.onDestroy()
    }

    private fun setupBrowserWebViews() {
        standardWebView = views.webView
        standardBrowserManager = BrowserManager(standardWebView)
        browserSessionCoordinator = BrowserSessionCoordinator(
            activity = this,
            webViewContainer = webViewContainer,
            standardWebView = standardWebView,
            browserManager = standardBrowserManager,
            onActiveWebViewChanged = ::handleActiveWebViewChanged
        )
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
            fullscreenChanged = ::handleVideoFullscreenChanged
        )
    }

    private fun setupBrowserClient() {
        currentBrowserManager().setBrowserClient(
            BrowserClient(
                pageStarted = { url -> currentSessionController().handlePageStarted(url) },
                pageFinished = { url -> currentSessionController().handlePageFinished(url) },
                requestIntercepted = ::interceptBrowserRequest,
                urlLoadingRequested = ::shouldBlockUrl
            )
        )
    }

    private fun interceptBrowserRequest(request: BrowserRequest) =
        adBlockRequestInterceptor.intercept(request) ?: smartNoImageRequestInterceptor.intercept(request)

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
                        currentBrowserManager().evaluateJavascript(EXIT_VIDEO_FULLSCREEN_SCRIPT)
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

                MediaRouteAction.BLOCK -> return true
                else -> Unit
            }
        }

        if (isUnavailableUcDownloadUrl(uri)) {
            view?.stopLoading()
            Toast.makeText(this, R.string.toast_uc_download_unavailable, Toast.LENGTH_SHORT).show()
            return true
        }

        if (!isWebUrl(uri.scheme)) {
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

    companion object {
        private const val NATIVE_BRIDGE_NAME = "VideoBrowserNative"
        private const val EXIT_VIDEO_FULLSCREEN_SCRIPT =
            "if(window.VideoBrowserEnhancer){window.VideoBrowserEnhancer.exitFullscreen();}"
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
