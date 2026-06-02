package com.example.videobrowser

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.CookieManager
import android.webkit.URLUtil
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
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.videobrowser.adblock.AdBlockManager
import com.example.videobrowser.adblock.AdBlockLogger
import com.example.videobrowser.adblock.AdBlockRequestInterceptor
import com.example.videobrowser.browser.BrowserClient
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.browser.ChromeClient
import com.example.videobrowser.browser.PageActionsController
import com.example.videobrowser.browser.VideoBrowserNativeBridge
import com.example.videobrowser.browser.search.SearchProviderController
import com.example.videobrowser.download.DownloadController
import com.example.videobrowser.element.ElementPickerController
import com.example.videobrowser.functioncenter.FunctionCenterController
import com.example.videobrowser.functioncenter.FunctionCenterPages
import com.example.videobrowser.inject.JsInjector
import com.example.videobrowser.inject.PageFeatureConfig
import com.example.videobrowser.inject.ScriptLoader
import com.example.videobrowser.localfiles.LocalFilesController
import com.example.videobrowser.rules.RuleEngine
import com.example.videobrowser.rules.RuleEngineFactory
import com.example.videobrowser.site.SiteHost
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.storage.PreferenceStore
import com.example.videobrowser.storage.SavedPageRepository
import com.example.videobrowser.utils.MediaUrlUtils
import com.example.videobrowser.utils.UrlUtils
import com.example.videobrowser.video.FullscreenVideoController
import com.example.videobrowser.video.PlayerActivity

class MainActivity : AppCompatActivity() {

    private lateinit var rootView: View
    private lateinit var topBar: View
    private lateinit var bottomBar: View
    private lateinit var webView: WebView
    private lateinit var addressInput: EditText
    private lateinit var pageProgress: ProgressBar
    private lateinit var searchProviderScroll: HorizontalScrollView
    private lateinit var searchProviderList: LinearLayout
    private lateinit var privateBrowsingBadge: TextView
    private lateinit var pageToolsButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var refreshButton: ImageButton
    private lateinit var homeButton: ImageButton
    private lateinit var bookmarkButton: ImageButton
    private lateinit var loadButton: ImageButton
    private lateinit var fullscreenContainer: FrameLayout
    private lateinit var preferenceStore: PreferenceStore
    private lateinit var settingsManager: SettingsManager
    private lateinit var savedPageRepository: SavedPageRepository
    private lateinit var ruleEngine: RuleEngine
    private lateinit var browserManager: BrowserManager
    private lateinit var functionCenterController: FunctionCenterController
    private lateinit var functionCenterPages: FunctionCenterPages
    private lateinit var localFilesController: LocalFilesController
    private lateinit var pageActionsController: PageActionsController
    private lateinit var searchProviderController: SearchProviderController
    private lateinit var downloadController: DownloadController
    private lateinit var fullscreenVideoController: FullscreenVideoController
    private lateinit var elementPickerController: ElementPickerController
    private lateinit var jsInjector: JsInjector
    private lateinit var chromeClient: ChromeClient
    private val adBlockLogger = AdBlockLogger()
    private val adBlockManager: AdBlockManager by lazy {
        AdBlockManager(
            isEnabled = ::isAdBlockEnabled,
            isDisabledForCurrentSite = ::isCurrentSiteAdBlockDisabled,
            isUserWhitelistedRequestHost = settingsManager::isUserWhitelistedSite,
            currentPageUrl = { currentPageUrl },
            currentPageHost = ::currentSiteHost,
            logger = adBlockLogger,
            ruleEngine = ruleEngine
        )
    }
    private val adBlockRequestInterceptor: AdBlockRequestInterceptor by lazy {
        AdBlockRequestInterceptor(adBlockManager)
    }

    private var isHomePageVisible = true
    private val isVideoFullscreenUiActive: Boolean
        get() = ::fullscreenVideoController.isInitialized &&
            fullscreenVideoController.isFullscreenUiActive
    private var areBrowserControlsHidden = false
    private var scrollControlDeltaY = 0
    private var scrollControlDirection = 0
    private var lastScrollControlChangeAt = 0L
    private var isWebViewTouchActive = false
    private var pendingBrowserControlsHidden: Boolean? = null
    private var defaultUserAgent: String? = null
    private var currentPageTitle = ""
    // shouldInterceptRequest 运行在 WebView 后台线程，站点级判断只能读取这个缓存。
    @Volatile
    private var currentPageUrl: String? = null
    private var isPageLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if ((applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        setContentView(R.layout.activity_main)

        rootView = findViewById(R.id.rootView)
        topBar = findViewById(R.id.topBar)
        bottomBar = findViewById(R.id.bottomBar)
        webView = findViewById(R.id.webView)
        addressInput = findViewById(R.id.addressInput)
        pageProgress = findViewById(R.id.pageProgress)
        searchProviderScroll = findViewById(R.id.searchProviderScroll)
        searchProviderList = findViewById(R.id.searchProviderList)
        privateBrowsingBadge = findViewById(R.id.privateBrowsingBadge)
        pageToolsButton = findViewById(R.id.pageToolsButton)
        loadButton = findViewById(R.id.loadButton)
        backButton = findViewById(R.id.backButton)
        refreshButton = findViewById(R.id.refreshButton)
        homeButton = findViewById(R.id.homeButton)
        bookmarkButton = findViewById(R.id.bookmarkButton)
        fullscreenContainer = findViewById(R.id.fullscreenContainer)
        functionCenterController = FunctionCenterController(this, rootView, ::dp)
        preferenceStore = PreferenceStore.from(this)
        settingsManager = SettingsManager(preferenceStore)
        savedPageRepository = SavedPageRepository(preferenceStore)
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
            settingsManager = settingsManager,
            dp = ::dp,
            isHomePageVisible = { isHomePageVisible },
            openProviderHome = ::openHomePage
        )
        setupFileOperationLaunchers()
        ruleEngine = RuleEngineFactory.create(assets, filesDir)
        browserManager = BrowserManager(webView)
        downloadController = DownloadController(
            activity = this,
            browserManager = browserManager,
            openNativePlayer = ::openNativePlayer,
            openExternalUrl = ::openExternalUrl
        )
        pageActionsController = PageActionsController(
            activity = this,
            browserManager = browserManager,
            downloadController = downloadController,
            settingsManager = settingsManager,
            savedPageRepository = savedPageRepository,
            currentActionableUrl = ::currentActionableUrl,
            currentShareableUrl = ::currentShareableUrl,
            currentPageTitle = { currentPageTitle },
            isShareableUrl = ::isShareableUrl,
            openNativePlayer = ::openNativePlayer,
            openExternalUrl = ::openExternalUrl,
            updateBookmarkButton = ::updateBookmarkButton,
            updateNavigationButtons = ::updateNavigationButtons,
            updatePrivateBrowsingUi = ::updatePrivateBrowsingUi,
            recreateActivity = { recreate() }
        )
        fullscreenVideoController = FullscreenVideoController(
            activity = this,
            rootView = rootView as ViewGroup,
            browserManager = browserManager,
            settingsManager = { settingsManager },
            chromeClient = { if (::chromeClient.isInitialized) chromeClient else null },
            dp = ::dp
        )
        functionCenterPages = FunctionCenterPages(
            activity = this,
            functionCenter = functionCenterController,
            settingsManager = settingsManager,
            browserManager = browserManager,
            savedPageRepository = savedPageRepository,
            adBlockLogger = adBlockLogger,
            currentSiteHost = ::currentSiteHost,
            currentActionableUrl = ::currentActionableUrl,
            isDesktopModeEnabled = ::isDesktopModeEnabled,
            isPrivateBrowsingEnabled = ::isPrivateBrowsingEnabled,
            isAdBlockEnabled = ::isAdBlockEnabled,
            isJsInjectionEnabled = ::isJsInjectionEnabled,
            isPageCleanupEnabled = ::isPageCleanupEnabled,
            isVideoEnhancementEnabled = ::isVideoEnhancementEnabled,
            toggleCurrentBookmark = pageActionsController::toggleCurrentBookmark,
            copyCurrentUrl = pageActionsController::copyCurrentUrl,
            shareCurrentUrl = pageActionsController::shareCurrentUrl,
            openCurrentUrlExternally = pageActionsController::openCurrentUrlExternally,
            openCurrentUrlInNativePlayer = pageActionsController::openCurrentUrlInNativePlayer,
            downloadCurrentUrl = pageActionsController::downloadCurrentUrl,
            clearBrowserData = pageActionsController::clearBrowserData,
            setPrivateBrowsingEnabled = pageActionsController::setPrivateBrowsingEnabled,
            restoreDefaultSettings = pageActionsController::restoreDefaultSettings,
            showFileOperationsPage = ::showFileOperationsPage,
            startElementPicker = ::startElementPicker,
            applyDesktopMode = ::applyDesktopMode,
            injectPageFeatures = ::injectPageFeatures,
            loadUrl = ::loadUrl
        )
        jsInjector = JsInjector(
            scriptLoader = ScriptLoader(assets),
            evaluateJavascript = browserManager::evaluateJavascript,
            ruleEngine = ruleEngine
        )
        elementPickerController = ElementPickerController(
            activity = this,
            browserManager = browserManager,
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
        browserManager.setup()
        browserManager.setPrivateBrowsingEnabled(isPrivateBrowsingEnabled())
        if (isPrivateBrowsingEnabled()) {
            browserManager.clearBrowsingData()
        }
        updatePrivateBrowsingUi()
        setupBrowserControls()
        setupWebViewScrollControls()
        setupBackNavigation()
        defaultUserAgent = browserManager.userAgentString()
        applyDesktopMode(reload = false)
        setupDownloadHandling()
        setupChromeClient()
        setupFullscreenGestureOverlay()
        browserManager.addJavascriptInterface(createNativeBridge(), NATIVE_BRIDGE_NAME)
        setupBrowserClient()

        openHomePage()
    }

    override fun onPause() {
        browserManager.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        browserManager.onResume()
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
        if (::chromeClient.isInitialized) {
            chromeClient.hideCustomView()
        }
        if (::settingsManager.isInitialized && settingsManager.isPrivateBrowsingEnabled()) {
            browserManager.clearBrowsingData()
        }
        browserManager.destroy()
        super.onDestroy()
    }

    private fun setupBrowserControls() {
        ViewCompat.setTooltipText(pageToolsButton, getString(R.string.title_page_tools))
        ViewCompat.setTooltipText(loadButton, getString(R.string.action_load_url))
        ViewCompat.setTooltipText(backButton, getString(R.string.action_back))
        ViewCompat.setTooltipText(refreshButton, getString(R.string.action_refresh))
        ViewCompat.setTooltipText(homeButton, getString(R.string.action_home))
        ViewCompat.setTooltipText(bookmarkButton, getString(R.string.action_add_bookmark))

        addressInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                setBrowserControlsHidden(false)
                addressInput.selectAll()
            }
        }

        addressInput.setOnEditorActionListener { _, actionId, event ->
            val isEnterUp =
                event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP
            if (actionId == EditorInfo.IME_ACTION_SEARCH || isEnterUp) {
                loadAddressInput()
                true
            } else {
                false
            }
        }

        loadButton.setOnClickListener { loadAddressInput() }
        backButton.setOnClickListener {
            browserManager.goBack()
            updateNavigationButtons()
        }
        refreshButton.setOnClickListener { browserManager.reload() }
        homeButton.setOnClickListener { openHomePage() }
        bookmarkButton.setOnClickListener { pageActionsController.toggleCurrentBookmark() }
        pageToolsButton.setOnClickListener { showFunctionCenter() }

        updateNavigationButtons()
    }

    private fun setupChromeClient() {
        chromeClient =
            ChromeClient(
                activity = this,
                fullscreenContainer = fullscreenContainer,
                decorView = window.decorView,
                progressChanged = ::handlePageProgressChanged,
                titleReceived = ::handlePageTitleReceived,
                fullscreenChanged = ::handleVideoFullscreenChanged
            )
        browserManager.setChromeClient(chromeClient)
    }

    private fun setupBrowserClient() {
        browserManager.setBrowserClient(
            BrowserClient(
                pageStarted = ::handlePageStarted,
                pageFinished = ::handlePageFinished,
                requestIntercepted = adBlockRequestInterceptor::intercept,
                urlLoadingRequested = ::shouldBlockUrl
            )
        )
    }

    private fun setupFullscreenGestureOverlay() {
        fullscreenVideoController.attachOverlay()
    }

    private fun handlePageStarted(url: String?) {
        if (::elementPickerController.isInitialized) {
            elementPickerController.clearState()
        }
        currentPageUrl = url ?: currentPageUrl
        if (::chromeClient.isInitialized &&
            chromeClient.isFullscreenModeActive() &&
            !chromeClient.isShowingCustomView()
        ) {
            chromeClient.exitPageFullscreen()
        }
        val isProviderHome = isProviderHomeUrl(url)
        resetPageTitle()
        updateAddressBar(url)
        showHomeContent(isProviderHome)
        isPageLoading = true
        pageProgress.progress = 0
        updatePageProgressVisibility()
        updateNavigationButtons()
    }

    private fun handlePageFinished(url: String?) {
        currentPageUrl = url ?: currentPageUrl
        val isProviderHome = isProviderHomeUrl(url)
        updateAddressBar(url)
        showHomeContent(isProviderHome)
        isPageLoading = false
        pageProgress.progress = 100
        updatePageProgressVisibility(forceHidden = true)
        pageActionsController.addHistoryEntry(url)
        injectPageFeatures()
        updateNavigationButtons()
    }

    private fun handlePageProgressChanged(newProgress: Int) {
        val normalizedProgress = newProgress.coerceIn(0, 100)
        isPageLoading = normalizedProgress in 1..99
        pageProgress.progress = normalizedProgress
        updatePageProgressVisibility()
        updateNavigationButtons()
    }

    private fun handlePageTitleReceived(title: String) {
        val normalizedTitle = title.trim()
        currentPageTitle = normalizedTitle
        this.title = normalizedTitle.takeIf { it.isNotBlank() } ?: getString(R.string.app_name)
    }

    private fun resetPageTitle() {
        currentPageTitle = ""
        this.title = getString(R.string.app_name)
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
        pageProgress.visibility = when {
            forceHidden || isVideoFullscreenUiActive || areBrowserControlsHidden -> View.GONE
            isPageLoading && pageProgress.progress in 1..99 && !isHomePageVisible -> View.VISIBLE
            else -> View.INVISIBLE
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupWebViewScrollControls() {
        webView.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    isWebViewTouchActive = true
                    pendingBrowserControlsHidden = null
                    view.parent?.requestDisallowInterceptTouchEvent(true)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isWebViewTouchActive = false
                    view.parent?.requestDisallowInterceptTouchEvent(false)
                    applyPendingBrowserControlsAfterTouch(view)
                }
            }
            false
        }

        webView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (isVideoFullscreenUiActive) {
                return@setOnScrollChangeListener
            }
            if (isHomePageVisible || addressInput.hasFocus()) {
                resetScrollControlTracking()
                setBrowserControlsHidden(false)
                return@setOnScrollChangeListener
            }

            val deltaY = scrollY - oldScrollY
            if (scrollY <= dp(4)) {
                resetScrollControlTracking()
                setBrowserControlsHidden(false)
                return@setOnScrollChangeListener
            }
            if (kotlin.math.abs(deltaY) < dp(2)) {
                return@setOnScrollChangeListener
            }

            val direction = if (deltaY > 0) 1 else -1
            if (direction != scrollControlDirection) {
                scrollControlDirection = direction
                scrollControlDeltaY = 0
            }
            scrollControlDeltaY += deltaY

            val now = SystemClock.uptimeMillis()
            if (now - lastScrollControlChangeAt < BROWSER_CONTROLS_SCROLL_COOLDOWN_MS) {
                return@setOnScrollChangeListener
            }

            when {
                scrollControlDeltaY >= dp(BROWSER_CONTROLS_SCROLL_THRESHOLD_DP) -> {
                    resetScrollControlTracking(now)
                    setBrowserControlsHidden(true)
                }
                scrollControlDeltaY <= -dp(BROWSER_CONTROLS_SCROLL_THRESHOLD_DP) -> {
                    resetScrollControlTracking(now)
                    setBrowserControlsHidden(false)
                }
            }
        }
    }

    private fun applyPendingBrowserControlsAfterTouch(view: View) {
        val pendingHidden = pendingBrowserControlsHidden ?: return
        pendingBrowserControlsHidden = null
        view.post {
            if (isWebViewTouchActive) {
                pendingBrowserControlsHidden = pendingHidden
            } else {
                setBrowserControlsHidden(pendingHidden, allowDefer = false)
            }
        }
    }

    private fun resetScrollControlTracking(changeAt: Long = lastScrollControlChangeAt) {
        scrollControlDeltaY = 0
        scrollControlDirection = 0
        lastScrollControlChangeAt = changeAt
    }

    private fun setBrowserControlsHidden(hidden: Boolean, allowDefer: Boolean = true) {
        val shouldHide = hidden || isVideoFullscreenUiActive
        if (allowDefer &&
            isWebViewTouchActive &&
            !isVideoFullscreenUiActive &&
            areBrowserControlsHidden != shouldHide
        ) {
            pendingBrowserControlsHidden = shouldHide
            return
        }

        if (areBrowserControlsHidden == shouldHide) {
            pendingBrowserControlsHidden = null
            syncSearchProviderVisibility()
            return
        }

        areBrowserControlsHidden = shouldHide
        topBar.visibility = if (shouldHide) View.GONE else View.VISIBLE
        bottomBar.visibility = if (shouldHide) View.GONE else View.VISIBLE
        syncSearchProviderVisibility()
        updatePageProgressVisibility(forceHidden = shouldHide)
    }

    private fun syncSearchProviderVisibility() {
        if (!::searchProviderController.isInitialized) {
            return
        }
        searchProviderController.syncVisibility(
            areBrowserControlsHidden = areBrowserControlsHidden,
            isVideoFullscreenUiActive = isVideoFullscreenUiActive,
            isHomePageVisible = isHomePageVisible
        )
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
                    } else if (::chromeClient.isInitialized && chromeClient.isShowingCustomView()) {
                        chromeClient.hideCustomView()
                    } else if (::chromeClient.isInitialized && chromeClient.isFullscreenModeActive()) {
                        browserManager.evaluateJavascript(EXIT_VIDEO_FULLSCREEN_SCRIPT)
                        chromeClient.exitPageFullscreen()
                    } else if (browserManager.goBack()) {
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
                if (::chromeClient.isInitialized) {
                    chromeClient.enterPageFullscreen()
                }
            },
            exitFullscreen = {
                if (::chromeClient.isInitialized) {
                    chromeClient.exitPageFullscreen()
                }
            },
            updatePlaybackTimeline = fullscreenVideoController::updatePlaybackTimeline,
            requestElementBlock = elementPickerController::handlePickedElement,
            blockSelectedElement = { selector ->
                elementPickerController.handlePickedElement(selector, "")
            },
            cancelElementPicker = elementPickerController::handleCancelledFromPage
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
        mimeType: String? = null
    ) {
        pageActionsController.openLocalDocumentUri(uri, displayName, mimeType)
    }

    private fun updatePrivateBrowsingUi() {
        if (!::privateBrowsingBadge.isInitialized || !::settingsManager.isInitialized) {
            return
        }
        privateBrowsingBadge.visibility = if (isPrivateBrowsingEnabled()) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun isAdBlockEnabled(): Boolean {
        return settingsManager.isAdBlockEnabled()
    }

    private fun isCurrentSiteAdBlockDisabled(): Boolean {
        return settingsManager.isAdBlockDisabledForSite(currentSiteHost())
    }

    private fun isJsInjectionEnabled(): Boolean {
        return settingsManager.isJsInjectionEnabled()
    }

    private fun isCurrentSiteJsInjectionDisabled(): Boolean {
        return settingsManager.isJsInjectionDisabledForSite(currentSiteHost())
    }

    private fun isPageCleanupEnabled(): Boolean {
        return settingsManager.isDomAdBlockEnabled()
    }

    private fun isCurrentSitePageCleanupDisabled(): Boolean {
        return settingsManager.isDomAdBlockDisabledForSite(currentSiteHost())
    }

    private fun isVideoEnhancementEnabled(): Boolean {
        return settingsManager.isVideoEnhancementEnabled()
    }

    private fun isCurrentSiteVideoEnhancementDisabled(): Boolean {
        return settingsManager.isVideoEnhancementDisabledForSite(currentSiteHost())
    }

    private fun isPageCleanupEnabledForCurrentSite(): Boolean {
        return isPageCleanupEnabled() && !isCurrentSitePageCleanupDisabled()
    }

    private fun isVideoEnhancementEnabledForCurrentSite(): Boolean {
        return isVideoEnhancementEnabled() && !isCurrentSiteVideoEnhancementDisabled()
    }

    private fun isDesktopModeEnabled(): Boolean {
        return settingsManager.isDesktopModeEnabled()
    }

    private fun isPrivateBrowsingEnabled(): Boolean {
        return settingsManager.isPrivateBrowsingEnabled()
    }

    private fun setupDownloadHandling() {
        downloadController.attach()
    }

    private fun applyDesktopMode(reload: Boolean) {
        val desktopModeEnabled = isDesktopModeEnabled()
        applyBrowserContentOrientation(desktopModeEnabled)
        browserManager.applyDesktopMode(
            enabled = desktopModeEnabled,
            desktopUserAgent = DESKTOP_USER_AGENT,
            defaultUserAgent = defaultUserAgent,
            reload = reload
        )
    }

    private fun applyBrowserContentOrientation(desktopModeEnabled: Boolean) {
        if (::chromeClient.isInitialized && chromeClient.isFullscreenModeActive()) {
            return
        }
        requestedOrientation = if (desktopModeEnabled) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    private fun injectPageFeatures() {
        if (!::jsInjector.isInitialized) {
            return
        }
        jsInjector.inject(
            PageFeatureConfig(
                jsInjectionEnabled = isJsInjectionEnabled() && !isCurrentSiteJsInjectionDisabled(),
                cleanupEnabled = isPageCleanupEnabledForCurrentSite(),
                videoEnabled = isVideoEnhancementEnabledForCurrentSite(),
                userCssSelectors = settingsManager.userElementHideSelectorsForSite(currentSiteHost())
            ),
            pageUrl = currentPageUrl ?: browserManager.currentUrl()
        )
    }

    private fun currentShareableUrl(): String? {
        return currentActionableUrl()
    }

    private fun currentActionableUrl(): String? {
        return listOf(currentPageUrl, browserManager.currentUrl())
            .firstOrNull { url -> !url.isNullOrBlank() && isShareableUrl(url) }
    }

    private fun currentSiteHost(): String? {
        return SiteHost.fromUrl(currentPageUrl)
    }

    private fun isShareableUrl(url: String): Boolean {
        val scheme = Uri.parse(url).scheme
        return scheme.equals("http", ignoreCase = true) ||
            scheme.equals("https", ignoreCase = true)
    }

    private fun openExternalUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, R.string.toast_no_external_browser, Toast.LENGTH_SHORT).show()
        }
    }

    private fun openNativePlayer(
        url: String,
        mimeType: String? = null,
        userAgentOverride: String? = null,
        titleOverride: String? = null
    ) {
        val title = titleOverride
            ?.takeIf { it.isNotBlank() }
            ?: currentPageTitle
            .takeIf { it.isNotBlank() && !it.equals(url, ignoreCase = true) }
            ?: URLUtil.guessFileName(url, null, mimeType)
        val isRemoteMedia = isShareableUrl(url)
        val referer = if (isRemoteMedia) {
            currentShareableUrl()?.takeIf { !it.equals(url, ignoreCase = true) }
        } else {
            null
        }
        val cookie = if (isRemoteMedia) {
            CookieManager.getInstance().getCookie(url)
        } else {
            null
        }
        val intent = PlayerActivity.createIntent(
            context = this,
            mediaUri = url,
            title = title,
            mimeType = mimeType,
            userAgent = userAgentOverride ?: browserManager.userAgentString(),
            cookie = cookie,
            referer = referer
        )
        startActivity(intent)
    }

    private fun loadAddressInput() {
        val input = addressInput.text?.toString()?.trim().orEmpty()
        UrlUtils.resolveAddressInput(
            input,
            searchProviderController.selectedProvider.searchUrlPrefix
        )
            ?.let { loadUrl(it) }
    }

    private fun openHomePage() {
        loadUrl(settingsManager.homeUrlOr(searchProviderController.selectedProvider.homeUrl))
    }

    private fun loadUrl(url: String) {
        closeFunctionCenter()
        if (MediaUrlUtils.isPlayableMediaUri(Uri.parse(url))) {
            openNativePlayer(url)
            return
        }

        currentPageUrl = url
        val isProviderHome = isProviderHomeUrl(url)
        updateAddressBar(url)
        hideKeyboard()
        showHomeContent(isProviderHome)
        browserManager.load(url)
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
        val canGoBack = browserManager.canGoBack()
        backButton.isEnabled = canGoBack
        backButton.visibility = if (canGoBack) View.VISIBLE else View.GONE
        updateBookmarkButton()
    }

    private fun updateBookmarkButton() {
        if (!::bookmarkButton.isInitialized || !::preferenceStore.isInitialized) {
            return
        }

        // 页面级收藏入口直接跟随 WebView 当前地址，避免依赖功能中心上下文。
        val url = currentActionableUrl()
        val isEnabled = url != null
        val isBookmarked = url?.let(savedPageRepository::isBookmarked) ?: false
        val actionText = getString(
            if (isBookmarked) R.string.action_remove_bookmark else R.string.action_add_bookmark
        )

        bookmarkButton.isEnabled = isEnabled
        bookmarkButton.contentDescription = actionText
        bookmarkButton.setImageResource(
            if (isBookmarked) R.drawable.ic_star_filled_24 else R.drawable.ic_star_24
        )
        bookmarkButton.setColorFilter(
            ContextCompat.getColor(
                this,
                when {
                    !isEnabled -> R.color.browser_icon_disabled
                    isBookmarked -> R.color.bookmark_active
                    else -> R.color.browser_icon
                }
            )
        )
        ViewCompat.setTooltipText(bookmarkButton, actionText)
    }

    private fun showHomeContent(show: Boolean) {
        isHomePageVisible = show
        resetScrollControlTracking()
        setBrowserControlsHidden(false)
        syncSearchProviderVisibility()
        webView.visibility = View.VISIBLE
        updatePageProgressVisibility(forceHidden = show)
        updateNavigationButtons()
    }

    private fun hideKeyboard() {
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
        if (openMedia && MediaUrlUtils.isPlayableMediaUri(uri)) {
            view?.stopLoading()
            openNativePlayer(uri.toString())
            return true
        }

        if (isUnavailableUcDownloadUrl(uri)) {
            view?.stopLoading()
            Toast.makeText(this, R.string.toast_uc_download_unavailable, Toast.LENGTH_SHORT).show()
            return true
        }

        return !isWebUrl(uri.scheme)
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

    companion object {
        private const val NATIVE_BRIDGE_NAME = "VideoBrowserNative"
        private const val EXIT_VIDEO_FULLSCREEN_SCRIPT =
            "if(window.VideoBrowserEnhancer){window.VideoBrowserEnhancer.exitFullscreen();}"
        private const val RULE_LOG_TAG = "VideoBrowserRules"
        private const val BROWSER_CONTROLS_SCROLL_THRESHOLD_DP = 48
        private const val BROWSER_CONTROLS_SCROLL_COOLDOWN_MS = 500L
        private const val DESKTOP_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
    }
}
