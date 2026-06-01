package com.example.videobrowser

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.database.Cursor
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.URLUtil
import android.webkit.WebView
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.videobrowser.adblock.AdBlockManager
import com.example.videobrowser.adblock.AdBlockLogAction
import com.example.videobrowser.adblock.AdBlockLogEntry
import com.example.videobrowser.adblock.AdBlockLogger
import com.example.videobrowser.adblock.AdBlockRequestInterceptor
import com.example.videobrowser.adblock.BuiltInAdBlockRules
import com.example.videobrowser.browser.BrowserClient
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.browser.ChromeClient
import com.example.videobrowser.inject.JsInjector
import com.example.videobrowser.inject.PageFeatureConfig
import com.example.videobrowser.inject.ScriptLoader
import com.example.videobrowser.rules.RuleEngine
import com.example.videobrowser.rules.RuleFileLoader
import com.example.videobrowser.rules.SkippedRule
import com.example.videobrowser.site.SiteHost
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.storage.PreferenceStore
import com.example.videobrowser.utils.MediaUrlUtils
import com.example.videobrowser.utils.UrlUtils
import com.example.videobrowser.video.FullscreenVideoGestureOverlay
import com.example.videobrowser.video.PlayerActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private data class SearchProvider(
        val id: String,
        val name: String,
        val badge: String,
        val homeUrl: String,
        val searchUrlPrefix: String,
        val accentColor: Int
    )

    private data class SearchProviderViews(
        val item: LinearLayout,
        val badge: TextView,
        val label: TextView
    )

    private data class SavedPage(
        val title: String,
        val url: String
    )

    private data class LocalDirectoryPathItem(
        val documentId: String,
        val title: String
    )

    private data class LocalDocument(
        val uri: Uri,
        val documentId: String,
        val name: String,
        val mimeType: String?,
        val size: Long?,
        val modifiedAt: Long?,
        val flags: Int
    ) {
        val isDirectory: Boolean
            get() = mimeType == DocumentsContract.Document.MIME_TYPE_DIR

        val canDelete: Boolean
            get() = flags and DocumentsContract.Document.FLAG_SUPPORTS_DELETE != 0

        val canRename: Boolean
            get() = flags and DocumentsContract.Document.FLAG_SUPPORTS_RENAME != 0
    }

    private lateinit var rootView: View
    private lateinit var topBar: View
    private lateinit var bottomBar: View
    private lateinit var webView: WebView
    private lateinit var addressInput: EditText
    private lateinit var pageProgress: ProgressBar
    private lateinit var searchProviderScroll: HorizontalScrollView
    private lateinit var searchProviderList: LinearLayout
    private lateinit var pageToolsButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var refreshButton: ImageButton
    private lateinit var homeButton: ImageButton
    private lateinit var bookmarkButton: ImageButton
    private lateinit var loadButton: ImageButton
    private lateinit var fullscreenContainer: FrameLayout
    private lateinit var fullscreenGestureOverlay: FullscreenVideoGestureOverlay
    private lateinit var preferenceStore: PreferenceStore
    private lateinit var settingsManager: SettingsManager
    private lateinit var ruleEngine: RuleEngine
    private lateinit var browserManager: BrowserManager
    private lateinit var jsInjector: JsInjector
    private lateinit var chromeClient: ChromeClient
    private lateinit var openLocalFileLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var openLocalDirectoryLauncher: ActivityResultLauncher<Uri?>
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

    private val searchProviders = listOf(
        SearchProvider(
            id = "baidu",
            name = "百度",
            badge = "百",
            homeUrl = "https://m.baidu.com/",
            searchUrlPrefix = "https://m.baidu.com/s?ie=utf-8&word=",
            accentColor = Color.parseColor("#315EFB")
        ),
        SearchProvider(
            id = "sogou",
            name = "搜狗",
            badge = "搜",
            homeUrl = "https://m.sogou.com/",
            searchUrlPrefix = "https://www.sogou.com/web?query=",
            accentColor = Color.parseColor("#13B56B")
        ),
        SearchProvider(
            id = "so",
            name = "360搜索",
            badge = "360",
            homeUrl = "https://m.so.com/",
            searchUrlPrefix = "https://www.so.com/s?q=",
            accentColor = Color.parseColor("#20A052")
        ),
        SearchProvider(
            id = "quark",
            name = "夸克搜索",
            badge = "夸",
            homeUrl = "https://quark.sm.cn/",
            searchUrlPrefix = "https://quark.sm.cn/s?q=",
            accentColor = Color.parseColor("#2F6FED")
        ),
        SearchProvider(
            id = "uc",
            name = "UC",
            badge = "UC",
            homeUrl = "https://so.m.sm.cn/",
            searchUrlPrefix = "https://so.m.sm.cn/s?q=",
            accentColor = Color.parseColor("#F28C20")
        ),
        SearchProvider(
            id = "edge",
            name = "Bing",
            badge = "B",
            homeUrl = "https://www.bing.com/",
            searchUrlPrefix = "https://www.bing.com/search?q=",
            accentColor = Color.parseColor("#12837A")
        )
    )
    private val searchProviderViews = mutableMapOf<String, SearchProviderViews>()
    private lateinit var selectedSearchProvider: SearchProvider
    private var isHomePageVisible = true
    private var isVideoFullscreenUiActive = false
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
    private var fullscreenPlaybackSpeed = SettingsManager.DEFAULT_VIDEO_SPEED
    private var fullscreenVideoPositionMs: Long? = null
    private var fullscreenVideoDurationMs: Long? = null
    private var lastFullscreenControlsWakeAt = 0L
    private var isElementPickerActive = false
    private var elementPickerStartedAt = 0L
    private var elementPickerDialog: AlertDialog? = null
    private var functionCenterPage: View? = null
    private var functionCenterBackAction: (() -> Unit)? = null
    private var isFunctionCenterVisible = false
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
        pageToolsButton = findViewById(R.id.pageToolsButton)
        loadButton = findViewById(R.id.loadButton)
        backButton = findViewById(R.id.backButton)
        refreshButton = findViewById(R.id.refreshButton)
        homeButton = findViewById(R.id.homeButton)
        bookmarkButton = findViewById(R.id.bookmarkButton)
        fullscreenContainer = findViewById(R.id.fullscreenContainer)
        preferenceStore = PreferenceStore.from(this)
        settingsManager = SettingsManager(preferenceStore)
        setupFileOperationLaunchers()
        ruleEngine = createRuleEngine()
        browserManager = BrowserManager(webView)
        jsInjector = JsInjector(
            scriptLoader = ScriptLoader(assets),
            evaluateJavascript = browserManager::evaluateJavascript,
            ruleEngine = ruleEngine
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
        setupBrowserControls()
        setupWebViewScrollControls()
        setupBackNavigation()
        defaultUserAgent = browserManager.userAgentString()
        applyDesktopMode(reload = false)
        setupDownloadHandling()
        setupChromeClient()
        setupFullscreenGestureOverlay()
        browserManager.addJavascriptInterface(VideoFullscreenBridge(), NATIVE_BRIDGE_NAME)
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
            wakeFullscreenVideoControls()
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onDestroy() {
        elementPickerDialog?.dismiss()
        closeFunctionCenter()
        if (::chromeClient.isInitialized) {
            chromeClient.hideCustomView()
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
        bookmarkButton.setOnClickListener { toggleCurrentBookmark() }
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
        fullscreenGestureOverlay = FullscreenVideoGestureOverlay(this).apply {
            elevation = dp(28).toFloat()
            onSeekBy = ::seekFullscreenVideoBy
            onSeekTo = ::seekFullscreenVideoTo
            onSeekPreviewStart = ::currentFullscreenVideoSeekPosition
            onTogglePlayPause = ::toggleFullscreenVideoPlayback
            onPlaybackSpeedSelected = ::setFullscreenVideoPlaybackSpeed
            onDirectionalLongPressStart = ::startFullscreenDirectionalLongPress
            onDirectionalLongPressEnd = ::stopFullscreenDirectionalLongPress
            onUserInteraction = ::wakeFullscreenVideoControls
            onToggleOrientation = {
                if (::chromeClient.isInitialized) {
                    chromeClient.toggleFullscreenOrientation()
                } else {
                    true
                }
            }
        }

        (rootView as ViewGroup).addView(
            fullscreenGestureOverlay,
            ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            }
        )
    }

    private fun handlePageStarted(url: String?) {
        clearElementPickerState()
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
        addHistoryEntry(url)
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
        val wasFullscreen = isVideoFullscreenUiActive
        isVideoFullscreenUiActive = fullscreen
        setBrowserControlsHidden(fullscreen)
        updatePageProgressVisibility(forceHidden = fullscreen)
        if (::fullscreenGestureOverlay.isInitialized) {
            when {
                fullscreen && !wasFullscreen -> {
                    val defaultSpeed = defaultVideoSpeed()
                    resetFullscreenVideoTimeline()
                    lastFullscreenControlsWakeAt = 0L
                    fullscreenPlaybackSpeed = defaultSpeed
                    fullscreenGestureOverlay.setPlaybackSpeed(defaultSpeed)
                    fullscreenGestureOverlay.setLandscape(chromeClient.isFullscreenLandscape())
                    fullscreenGestureOverlay.showOverlay()
                    setFullscreenVideoPlaybackSpeed(defaultSpeed)
                    wakeFullscreenVideoControls()
                    requestFullscreenVideoTimeline()
                }
                fullscreen -> {
                    fullscreenGestureOverlay.setLandscape(chromeClient.isFullscreenLandscape())
                    fullscreenGestureOverlay.bringToFront()
                    wakeFullscreenVideoControls()
                    requestFullscreenVideoTimeline()
                }
                wasFullscreen -> {
                    val defaultSpeed = defaultVideoSpeed()
                    resetFullscreenVideoTimeline()
                    lastFullscreenControlsWakeAt = 0L
                    fullscreenPlaybackSpeed = defaultSpeed
                    setFullscreenVideoPlaybackSpeed(defaultSpeed)
                    fullscreenGestureOverlay.hideOverlay()
                }
            }
        }
        ViewCompat.requestApplyInsets(rootView)
        if (!fullscreen) {
            applyBrowserContentOrientation(isDesktopModeEnabled())
        }
    }

    private fun seekFullscreenVideoBy(offsetMs: Long) {
        val seconds = String.format(Locale.US, "%.3f", offsetMs / 1000.0)
        fullscreenVideoPositionMs = boundedFullscreenVideoPosition(offsetMs)
        browserManager.evaluateJavascript(
            "(function(){if(window.VideoBrowserEnhancer&&" +
                "typeof window.VideoBrowserEnhancer.seekBy==='function'){" +
                "window.VideoBrowserEnhancer.seekBy($seconds);" +
                "}})();"
        )
    }

    private fun seekFullscreenVideoTo(positionMs: Long) {
        val duration = fullscreenVideoDurationMs
        val boundedPositionMs = if (duration != null && duration > 0L) {
            positionMs.coerceIn(0L, duration)
        } else {
            positionMs.coerceAtLeast(0L)
        }
        fullscreenVideoPositionMs = boundedPositionMs
        val seconds = String.format(Locale.US, "%.3f", boundedPositionMs / 1000.0)
        browserManager.evaluateJavascript(
            "(function(){if(window.VideoBrowserEnhancer&&" +
                "typeof window.VideoBrowserEnhancer.seekTo==='function'){" +
                "window.VideoBrowserEnhancer.seekTo($seconds);" +
                "}})();"
        )
    }

    private fun currentFullscreenVideoSeekPosition(): FullscreenVideoGestureOverlay.SeekPosition {
        requestFullscreenVideoTimeline()
        return FullscreenVideoGestureOverlay.SeekPosition(
            positionMs = fullscreenVideoPositionMs,
            durationMs = fullscreenVideoDurationMs
        )
    }

    private fun boundedFullscreenVideoPosition(offsetMs: Long): Long? {
        val current = fullscreenVideoPositionMs ?: return null
        val target = current + offsetMs
        val duration = fullscreenVideoDurationMs
        return if (duration != null && duration > 0L) {
            target.coerceIn(0L, duration)
        } else {
            target.coerceAtLeast(0L)
        }
    }

    private fun requestFullscreenVideoTimeline() {
        browserManager.evaluateJavascript(
            "(function(){if(window.VideoBrowserEnhancer&&" +
                "typeof window.VideoBrowserEnhancer.reportPlaybackTimeline==='function'){" +
                "window.VideoBrowserEnhancer.reportPlaybackTimeline();" +
                "}})();"
        )
    }

    private fun resetFullscreenVideoTimeline() {
        fullscreenVideoPositionMs = null
        fullscreenVideoDurationMs = null
    }

    private fun toggleFullscreenVideoPlayback(): Boolean? {
        browserManager.evaluateJavascript(
            "(function(){var enhancer=window.VideoBrowserEnhancer;" +
                "if(!enhancer)return;" +
                "if(typeof enhancer.togglePlayPause==='function'){" +
                "enhancer.togglePlayPause();" +
                "}" +
                "if(typeof enhancer.wakeControls==='function'){" +
                "enhancer.wakeControls();" +
                "}})();"
        )
        return null
    }

    private fun wakeFullscreenVideoControls() {
        if (!isVideoFullscreenUiActive) {
            return
        }

        val now = SystemClock.elapsedRealtime()
        if (now - lastFullscreenControlsWakeAt < FULLSCREEN_CONTROLS_WAKE_THROTTLE_MS) {
            return
        }
        lastFullscreenControlsWakeAt = now

        browserManager.evaluateJavascript(
            "(function(){if(window.VideoBrowserEnhancer&&" +
                "typeof window.VideoBrowserEnhancer.wakeControls==='function'){" +
                "window.VideoBrowserEnhancer.wakeControls();" +
                "}})();"
        )
    }

    private fun setFullscreenVideoPlaybackSpeed(speed: Float) {
        val normalizedSpeed = if (!speed.isNaN() && !speed.isInfinite() && speed > 0f) {
            speed
        } else {
            SettingsManager.DEFAULT_VIDEO_SPEED
        }
        fullscreenPlaybackSpeed = normalizedSpeed
        if (::settingsManager.isInitialized) {
            settingsManager.setDefaultVideoSpeed(normalizedSpeed)
        }
        if (::fullscreenGestureOverlay.isInitialized) {
            fullscreenGestureOverlay.setPlaybackSpeed(fullscreenPlaybackSpeed)
        }
        val speedValue = String.format(Locale.US, "%.2f", normalizedSpeed)
        browserManager.evaluateJavascript(
            "(function(){if(window.VideoBrowserEnhancer&&" +
                "typeof window.VideoBrowserEnhancer.setPlaybackSpeed==='function'){" +
                "window.VideoBrowserEnhancer.setPlaybackSpeed($speedValue);" +
                "}})();"
        )
    }

    private fun startFullscreenDirectionalLongPress(direction: Int) {
        val normalizedDirection = if (direction < 0) -1 else 1
        browserManager.evaluateJavascript(
            "(function(){if(window.VideoBrowserEnhancer&&" +
                "typeof window.VideoBrowserEnhancer.startDirectionalPlayback==='function'){" +
                "window.VideoBrowserEnhancer.startDirectionalPlayback($normalizedDirection);" +
                "}})();"
        )
    }

    private fun stopFullscreenDirectionalLongPress() {
        browserManager.evaluateJavascript(
            "(function(){if(window.VideoBrowserEnhancer&&" +
                "typeof window.VideoBrowserEnhancer.stopDirectionalPlayback==='function'){" +
                "window.VideoBrowserEnhancer.stopDirectionalPlayback();" +
                "}})();"
        )
        setFullscreenVideoPlaybackSpeed(fullscreenPlaybackSpeed)
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
        searchProviderScroll.visibility =
            if (!areBrowserControlsHidden && !isVideoFullscreenUiActive && isHomePageVisible) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    private fun setupSearchProviders() {
        selectedSearchProvider = loadSavedSearchProvider()
        if (!settingsManager.hasHomeUrl()) {
            settingsManager.setHomeUrl(selectedSearchProvider.homeUrl)
        }
        searchProviderViews.clear()
        searchProviderList.removeAllViews()

        searchProviders.forEach { provider ->
            val item = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                isClickable = true
                isFocusable = true
                contentDescription = getString(R.string.action_select_search_provider, provider.name)
                setPadding(dp(4), 0, dp(4), 0)
                setSelectableItemBackground()
                setOnClickListener { selectSearchProvider(provider) }
            }
            val badge = TextView(this).apply {
                gravity = Gravity.CENTER
                includeFontPadding = false
                text = provider.badge
                setTypeface(typeface, Typeface.BOLD)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, if (provider.badge.length > 1) 12f else 16f)
            }
            item.addView(
                badge,
                LinearLayout.LayoutParams(dp(48), dp(48))
            )

            val label = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(6)
                }
                ellipsize = TextUtils.TruncateAt.END
                gravity = Gravity.CENTER
                includeFontPadding = false
                maxLines = 1
                text = provider.name
                textSize = 12f
            }
            item.addView(label)

            searchProviderViews[provider.id] = SearchProviderViews(item, badge, label)
            searchProviderList.addView(
                item,
                LinearLayout.LayoutParams(dp(78), ViewGroup.LayoutParams.MATCH_PARENT)
            )
        }

        updateSearchProviderSelection()
    }

    private fun selectSearchProvider(provider: SearchProvider) {
        val shouldOpenProviderHome = isHomePageVisible
        selectedSearchProvider = provider
        settingsManager.setSearchEngineId(provider.id)
        settingsManager.setHomeUrl(provider.homeUrl)
        updateSearchProviderSelection()
        if (shouldOpenProviderHome) {
            openHomePage()
        }
    }

    private fun loadSavedSearchProvider(): SearchProvider {
        val savedProviderId = settingsManager.searchEngineId()
        return searchProviders.firstOrNull { it.id == savedProviderId } ?: searchProviders.first()
    }

    private fun updateSearchProviderSelection() {
        searchProviders.forEach { provider ->
            val views = searchProviderViews[provider.id] ?: return@forEach
            val selected = provider.id == selectedSearchProvider.id
            views.item.isSelected = selected
            views.badge.background = createProviderBadgeBackground(provider, selected)
            views.badge.setTextColor(
                if (selected) {
                    Color.WHITE
                } else {
                    ContextCompat.getColor(this, R.color.browser_icon)
                }
            )
            views.label.setTextColor(
                ContextCompat.getColor(
                    this,
                    if (selected) R.color.browser_text else R.color.browser_text_hint
                )
            )
            views.label.setTypeface(null, if (selected) Typeface.BOLD else Typeface.NORMAL)
        }
        addressInput.hint = getString(
            R.string.hint_search_with_provider,
            selectedSearchProvider.name
        )
    }

    private fun createProviderBadgeBackground(
        provider: SearchProvider,
        selected: Boolean
    ): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            if (selected) {
                setColor(provider.accentColor)
                setStroke(
                    dp(2),
                    ContextCompat.getColor(
                        this@MainActivity,
                        R.color.browser_provider_selected_stroke
                    )
                )
            } else {
                setColor(ContextCompat.getColor(this@MainActivity, R.color.browser_provider_circle))
            }
        }
    }

    private fun View.setSelectableItemBackground() {
        val outValue = TypedValue()
        theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
        setBackgroundResource(outValue.resourceId)
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (handleFunctionCenterBack()) {
                        return
                    } else if (isElementPickerActive) {
                        cancelElementPicker()
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

    private inner class VideoFullscreenBridge {
        @JavascriptInterface
        fun enterFullscreen() {
            runOnUiThread {
                if (::chromeClient.isInitialized) {
                    chromeClient.enterPageFullscreen()
                }
            }
        }

        @JavascriptInterface
        fun exitFullscreen() {
            runOnUiThread {
                if (::chromeClient.isInitialized) {
                    chromeClient.exitPageFullscreen()
                }
            }
        }

        @JavascriptInterface
        fun updatePlaybackTimeline(positionMs: Double, durationMs: Double) {
            runOnUiThread {
                fullscreenVideoPositionMs = positionMs
                    .takeIf { it.isFinite() && it >= 0.0 }
                    ?.toLong()
                fullscreenVideoDurationMs = durationMs
                    .takeIf { it.isFinite() && it > 0.0 }
                    ?.toLong()
            }
        }

        @JavascriptInterface
        fun requestElementBlock(selector: String, description: String) {
            runOnUiThread {
                handlePickedElement(selector, description)
            }
        }

        @JavascriptInterface
        fun blockSelectedElement(selector: String) {
            runOnUiThread {
                handlePickedElement(selector, "")
            }
        }

        @JavascriptInterface
        fun cancelElementPicker() {
            runOnUiThread {
                handleElementPickerCancelledFromPage()
            }
        }
    }

    private fun startElementPicker() {
        val host = currentSiteHost()
        if (host == null) {
            Toast.makeText(this, R.string.toast_no_page_url, Toast.LENGTH_SHORT).show()
            return
        }
        if (!isJsInjectionEnabled() || isCurrentSiteJsInjectionDisabled()) {
            Toast.makeText(this, R.string.toast_element_picker_js_disabled, Toast.LENGTH_SHORT).show()
            return
        }

        if (isElementPickerActive) {
            finishElementPickerSession()
        }
        isElementPickerActive = true
        elementPickerStartedAt = SystemClock.elapsedRealtime()
        injectPageFeatures()
        browserManager.evaluateJavascript(START_ELEMENT_PICKER_SCRIPT)
        Toast.makeText(this, R.string.toast_element_picker_started, Toast.LENGTH_SHORT).show()
    }

    private fun cancelElementPicker() {
        if (!isElementPickerActive) {
            return
        }
        elementPickerDialog?.dismiss()
        finishElementPickerSession()
        Toast.makeText(this, R.string.toast_element_picker_cancelled, Toast.LENGTH_SHORT).show()
    }

    private fun handleElementPickerCancelledFromPage() {
        if (!isElementPickerActive) {
            return
        }
        finishElementPickerSession()
        Toast.makeText(this, R.string.toast_element_picker_cancelled, Toast.LENGTH_SHORT).show()
    }

    private fun handlePickedElement(selector: String, description: String) {
        if (!isElementPickerSessionValid()) {
            finishElementPickerSession()
            return
        }

        val host = currentSiteHost() ?: run {
            finishElementPickerSession()
            Toast.makeText(this, R.string.toast_no_page_url, Toast.LENGTH_SHORT).show()
            return
        }
        showConfirmElementBlockDialog(host, selector, description)
    }

    private fun showConfirmElementBlockDialog(host: String, selector: String, description: String) {
        val detail = listOf(description.trim(), selector.trim())
            .filter { value -> value.isNotBlank() }
            .distinct()
            .joinToString(separator = "\n")
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.title_confirm_element_block)
            .setMessage(getString(R.string.dialog_confirm_element_block_message, host, detail))
            .setPositiveButton(R.string.action_block_element) { _, _ ->
                savePickedElement(host, selector)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                cancelElementPicker()
            }
            .create()
        elementPickerDialog?.dismiss()
        elementPickerDialog = dialog
        dialog.setOnCancelListener {
            cancelElementPicker()
        }
        dialog.setOnDismissListener {
            if (elementPickerDialog === dialog) {
                elementPickerDialog = null
            }
        }
        dialog.show()
    }

    private fun savePickedElement(host: String, selector: String) {
        val alreadySaved = settingsManager.hasUserElementHideSelectorForSite(host, selector)
        val saved = alreadySaved || settingsManager.addUserElementHideSelectorForSite(host, selector)
        finishElementPickerSession()
        if (!saved) {
            Toast.makeText(this, R.string.toast_element_picker_invalid, Toast.LENGTH_SHORT).show()
            return
        }

        injectPageFeatures()
        Toast.makeText(
            this,
            if (alreadySaved) {
                R.string.toast_element_picker_already_saved
            } else {
                R.string.toast_element_picker_saved
            },
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun isElementPickerSessionValid(): Boolean {
        return isElementPickerActive &&
            SystemClock.elapsedRealtime() - elementPickerStartedAt <= ELEMENT_PICKER_TIMEOUT_MS
    }

    private fun clearElementPickerState() {
        isElementPickerActive = false
        elementPickerStartedAt = 0L
    }

    private fun finishElementPickerSession() {
        clearElementPickerState()
        browserManager.evaluateJavascript(FINISH_ELEMENT_PICKER_SCRIPT)
    }

    private fun showFunctionCenter() {
        hideKeyboard()
        showFunctionCenterRootPage()
    }

    private fun showFunctionCenterRootPage() {
        val onBack: () -> Unit = { closeFunctionCenter() }
        attachFunctionCenterPage(
            page = createFunctionCenterRootPage(onBack),
            onBack = onBack
        )
    }

    private fun showFunctionCenterSubPage(
        title: String,
        onBack: () -> Unit = { showFunctionCenterRootPage() },
        buildContent: (LinearLayout) -> Unit
    ) {
        attachFunctionCenterPage(
            page = createFunctionCenterPage(title, onBack, buildContent),
            onBack = onBack
        )
    }

    private fun attachFunctionCenterPage(page: View, onBack: () -> Unit) {
        val container = rootView as? ViewGroup ?: return
        functionCenterPage?.let { currentPage ->
            (currentPage.parent as? ViewGroup)?.removeView(currentPage)
        }

        functionCenterPage = page
        functionCenterBackAction = onBack
        isFunctionCenterVisible = true
        container.addView(
            page,
            ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            }
        )
        page.bringToFront()
        page.requestFocus()
    }

    private fun handleFunctionCenterBack(): Boolean {
        if (!isFunctionCenterVisible || functionCenterPage == null) {
            return false
        }
        functionCenterBackAction?.invoke() ?: closeFunctionCenter()
        return true
    }

    private fun closeFunctionCenter(): Boolean {
        val page = functionCenterPage ?: return false
        (page.parent as? ViewGroup)?.removeView(page)
        functionCenterPage = null
        functionCenterBackAction = null
        isFunctionCenterVisible = false
        return true
    }

    private fun createFunctionCenterRootPage(onBack: () -> Unit): View {
        val siteHost = currentSiteHost()
        val pageUrl = currentActionableUrl()
        return createFunctionCenterPage(
            title = getString(R.string.title_page_tools),
            onBack = onBack
        ) { content ->
            addCurrentPageActionSection(content, pageUrl, siteHost)
            addFunctionNavigationSection(content, siteHost)
        }
    }

    private fun addFunctionNavigationSection(parent: LinearLayout, siteHost: String?) {
        addFunctionSection(parent, getString(R.string.function_center_section_more)) { section ->
            addActionRow(
                parent = section,
                title = getString(R.string.action_site_settings),
                summary = siteHost ?: getString(R.string.function_center_site_action_unavailable),
                enabled = siteHost != null
            ) {
                showCurrentSiteSettingsPage()
            }
            addActionRow(
                parent = section,
                title = getString(R.string.action_browser_settings),
                summary = getString(R.string.action_browser_settings_summary)
            ) {
                showBrowserSettingsPage()
            }
        }
    }

    private fun addCurrentPageActionSection(
        parent: LinearLayout,
        pageUrl: String?,
        siteHost: String?
    ) {
        val pageSummary = pageUrl
            ?.let(UrlUtils::displayUrl)
            ?: getString(R.string.function_center_page_action_unavailable)
        val hasPage = pageUrl != null
        val bookmarkTitle = if (pageUrl?.let { isSavedPage(KEY_BOOKMARKS, it) } == true) {
            getString(R.string.action_remove_bookmark)
        } else {
            getString(R.string.action_add_bookmark)
        }

        addFunctionSection(parent, getString(R.string.function_center_section_page_actions)) { section ->
            addActionRow(section, bookmarkTitle, pageSummary, enabled = hasPage) {
                toggleCurrentBookmark()
                showFunctionCenterRootPage()
            }
            addActionRow(section, getString(R.string.action_copy_link), pageSummary, enabled = hasPage) {
                copyCurrentUrl()
            }
            addActionRow(section, getString(R.string.action_share_page), pageSummary, enabled = hasPage) {
                shareCurrentUrl()
            }
            addSwitchRow(
                parent = section,
                title = getString(R.string.setting_desktop_mode),
                summary = getString(R.string.setting_desktop_mode_summary),
                checked = isDesktopModeEnabled(),
                enabled = hasPage
            ) { enabled ->
                settingsManager.setDesktopModeEnabled(enabled)
                applyDesktopMode(reload = true)
            }
            addActionRow(section, getString(R.string.action_open_external), pageSummary, enabled = hasPage) {
                openCurrentUrlExternally()
            }
            addActionRow(section, getString(R.string.action_open_native_player), pageSummary, enabled = hasPage) {
                openCurrentUrlInNativePlayer()
            }
            addActionRow(section, getString(R.string.action_download_current_url), pageSummary, enabled = hasPage) {
                downloadCurrentUrl()
            }
            addActionRow(
                parent = section,
                title = getString(R.string.action_pick_element),
                summary = siteHost ?: getString(R.string.function_center_site_action_unavailable),
                enabled = siteHost != null
            ) {
                closeFunctionCenter()
                startElementPicker()
            }
        }
    }

    private fun showCurrentSiteSettingsPage() {
        val siteHost = currentSiteHost()
        showFunctionCenterSubPage(getString(R.string.title_current_site)) { content ->
            if (siteHost != null) {
                addFunctionMessage(content, getString(R.string.function_center_current_site, siteHost))
            } else {
                addEmptyState(content, getString(R.string.function_center_site_action_unavailable))
            }
            addCurrentSiteActionSection(content, siteHost)
        }
    }

    private fun addCurrentSiteActionSection(parent: LinearLayout, siteHost: String?) {
        val siteSummary = siteHost ?: getString(R.string.function_center_site_action_unavailable)
        val hasSite = siteHost != null
        val isWhitelisted = siteHost?.let(settingsManager::isUserWhitelistedSite) ?: false

        addFunctionSection(parent, getString(R.string.function_center_section_site_actions)) { section ->
            addSwitchRow(
                parent = section,
                title = getString(R.string.setting_current_site_ad_block),
                summary = currentSiteFeatureSummary(siteHost, isAdBlockEnabled()),
                checked = isAdBlockEnabled() &&
                    !(siteHost?.let(settingsManager::isAdBlockDisabledForSite) ?: false),
                enabled = hasSite && isAdBlockEnabled()
            ) { enabled ->
                val host = currentSiteHost() ?: return@addSwitchRow
                settingsManager.setAdBlockDisabledForSite(host, !enabled)
                showCurrentSiteFeatureToast(
                    enabled = enabled,
                    featureName = getString(R.string.setting_current_site_ad_block),
                    host = host
                )
                browserManager.reload()
            }

            addSwitchRow(
                parent = section,
                title = getString(R.string.setting_current_site_js_injection),
                summary = currentSiteFeatureSummary(siteHost, isJsInjectionEnabled()),
                checked = isJsInjectionEnabled() &&
                    !(siteHost?.let(settingsManager::isJsInjectionDisabledForSite) ?: false),
                enabled = hasSite && isJsInjectionEnabled()
            ) { enabled ->
                val host = currentSiteHost() ?: return@addSwitchRow
                settingsManager.setJsInjectionDisabledForSite(host, !enabled)
                showCurrentSiteFeatureToast(
                    enabled = enabled,
                    featureName = getString(R.string.setting_current_site_js_injection),
                    host = host
                )
                browserManager.reload()
            }

            addSwitchRow(
                parent = section,
                title = getString(R.string.setting_page_cleanup),
                summary = currentSiteFeatureSummary(siteHost, isPageCleanupEnabled()),
                checked = isPageCleanupEnabled() &&
                    !(siteHost?.let(settingsManager::isDomAdBlockDisabledForSite) ?: false),
                enabled = hasSite && isPageCleanupEnabled()
            ) { enabled ->
                val host = currentSiteHost() ?: return@addSwitchRow
                settingsManager.setDomAdBlockDisabledForSite(host, !enabled)
                showCurrentSiteFeatureToast(
                    enabled = enabled,
                    featureName = getString(R.string.setting_page_cleanup),
                    host = host
                )
                injectPageFeatures()
            }

            addSwitchRow(
                parent = section,
                title = getString(R.string.setting_video_enhancement),
                summary = currentSiteFeatureSummary(siteHost, isVideoEnhancementEnabled()),
                checked = isVideoEnhancementEnabled() &&
                    !(siteHost?.let(settingsManager::isVideoEnhancementDisabledForSite) ?: false),
                enabled = hasSite && isVideoEnhancementEnabled()
            ) { enabled ->
                val host = currentSiteHost() ?: return@addSwitchRow
                settingsManager.setVideoEnhancementDisabledForSite(host, !enabled)
                showCurrentSiteFeatureToast(
                    enabled = enabled,
                    featureName = getString(R.string.setting_video_enhancement),
                    host = host
                )
                injectPageFeatures()
            }

            addDivider(section)

            addActionRow(
                parent = section,
                title = getString(R.string.action_add_site_rule),
                summary = siteSummary,
                enabled = hasSite
            ) {
                closeFunctionCenter()
                startElementPicker()
            }
            addActionRow(
                parent = section,
                title = getString(
                    if (isWhitelisted) R.string.action_leave_whitelist else R.string.action_join_whitelist
                ),
                summary = siteSummary,
                enabled = hasSite
            ) {
                toggleCurrentSiteWhitelist()
                showCurrentSiteSettingsPage()
            }
            addActionRow(
                parent = section,
                title = getString(R.string.action_view_site_config),
                summary = siteSummary,
                enabled = hasSite
            ) {
                showCurrentSiteConfigPage()
            }
        }
    }

    private fun currentSiteFeatureSummary(siteHost: String?, globalEnabled: Boolean): String {
        return when {
            siteHost == null -> getString(R.string.function_center_site_action_unavailable)
            !globalEnabled -> getString(R.string.setting_disabled_in_browser_settings)
            else -> siteHost
        }
    }

    private fun showCurrentSiteFeatureToast(enabled: Boolean, featureName: String, host: String) {
        Toast.makeText(
            this,
            getString(
                if (enabled) {
                    R.string.toast_current_site_feature_enabled
                } else {
                    R.string.toast_current_site_feature_disabled
                },
                featureName,
                host
            ),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showCurrentSiteConfigPage() {
        val siteHost = currentSiteHost()
        showFunctionCenterSubPage(
            title = getString(R.string.title_site_config),
            onBack = { showCurrentSiteSettingsPage() }
        ) { content ->
            if (siteHost == null) {
                addEmptyState(content, getString(R.string.function_center_site_action_unavailable))
                return@showFunctionCenterSubPage
            }

            addFunctionSection(content, getString(R.string.function_center_section_site_actions)) { section ->
                addInfoRow(
                    parent = section,
                    title = getString(R.string.function_center_site_host),
                    summary = siteHost
                )
                addInfoRow(
                    parent = section,
                    title = getString(R.string.setting_current_site_ad_block),
                    summary = currentSiteFeatureStatus(
                        globalEnabled = isAdBlockEnabled(),
                        siteDisabled = settingsManager.isAdBlockDisabledForSite(siteHost)
                    )
                )
                addInfoRow(
                    parent = section,
                    title = getString(R.string.setting_current_site_js_injection),
                    summary = currentSiteFeatureStatus(
                        globalEnabled = isJsInjectionEnabled(),
                        siteDisabled = settingsManager.isJsInjectionDisabledForSite(siteHost)
                    )
                )
                addInfoRow(
                    parent = section,
                    title = getString(R.string.setting_page_cleanup),
                    summary = currentSiteFeatureStatus(
                        globalEnabled = isPageCleanupEnabled(),
                        siteDisabled = settingsManager.isDomAdBlockDisabledForSite(siteHost)
                    )
                )
                addInfoRow(
                    parent = section,
                    title = getString(R.string.setting_video_enhancement),
                    summary = currentSiteFeatureStatus(
                        globalEnabled = isVideoEnhancementEnabled(),
                        siteDisabled = settingsManager.isVideoEnhancementDisabledForSite(siteHost)
                    )
                )
                addInfoRow(
                    parent = section,
                    title = getString(R.string.action_join_whitelist),
                    summary = if (settingsManager.isUserWhitelistedSite(siteHost)) {
                        getString(R.string.site_config_whitelisted)
                    } else {
                        getString(R.string.site_config_not_whitelisted)
                    }
                )
                addInfoRow(
                    parent = section,
                    title = getString(R.string.action_add_site_rule),
                    summary = getString(
                        R.string.site_config_rule_count,
                        settingsManager.userElementHideSelectorsForSite(siteHost).size
                    )
                )
            }
        }
    }

    private fun currentSiteFeatureStatus(globalEnabled: Boolean, siteDisabled: Boolean): String {
        return when {
            !globalEnabled -> getString(R.string.site_config_disabled_by_global)
            siteDisabled -> getString(R.string.site_config_disabled)
            else -> getString(R.string.site_config_enabled)
        }
    }

    private fun showBrowserSettingsPage() {
        showFunctionCenterSubPage(getString(R.string.title_browser_settings)) { content ->
            addGlobalEnhancementSection(content)
            addToolboxSection(content)
        }
    }

    private fun addToolboxSection(parent: LinearLayout) {
        addFunctionSection(parent, getString(R.string.function_center_section_toolbox)) { section ->
            addActionRow(
                parent = section,
                title = getString(R.string.action_show_bookmarks),
                summary = getString(R.string.action_show_bookmarks_summary)
            ) {
                showSavedPageList(
                    key = KEY_BOOKMARKS,
                    title = getString(R.string.title_bookmarks),
                    emptyMessage = getString(R.string.toast_bookmarks_empty)
                )
            }
            addActionRow(
                parent = section,
                title = getString(R.string.action_show_history),
                summary = getString(R.string.action_show_history_summary)
            ) {
                showSavedPageList(
                    key = KEY_HISTORY,
                    title = getString(R.string.title_history),
                    emptyMessage = getString(R.string.toast_history_empty)
                )
            }
            addActionRow(
                parent = section,
                title = getString(R.string.action_file_operations),
                summary = getString(R.string.action_file_operations_summary)
            ) {
                showFileOperationsPage()
            }
            addActionRow(
                parent = section,
                title = getString(R.string.action_show_ad_block_log),
                summary = getString(R.string.action_show_ad_block_log_summary)
            ) {
                showAdBlockLog()
            }
            addActionRow(
                parent = section,
                title = getString(R.string.action_manage_user_whitelist),
                summary = getString(R.string.action_manage_user_whitelist_summary)
            ) {
                showUserWhitelistManager()
            }
            addDivider(section)
            addActionRow(
                parent = section,
                title = getString(R.string.action_clear_browser_data),
                summary = getString(R.string.action_clear_browser_data_summary)
            ) {
                clearBrowserData()
            }
            addActionRow(
                parent = section,
                title = getString(R.string.action_restore_default_settings),
                summary = getString(R.string.action_restore_default_settings_summary)
            ) {
                showRestoreDefaultSettingsPage()
            }
        }
    }

    private fun addGlobalEnhancementSection(parent: LinearLayout) {
        addFunctionSection(parent, getString(R.string.function_center_section_settings)) { section ->
            addSwitchRow(
                parent = section,
                title = getString(R.string.setting_ad_block),
                summary = getString(R.string.setting_ad_block_summary),
                checked = isAdBlockEnabled()
            ) { enabled ->
                settingsManager.setAdBlockEnabled(enabled)
                browserManager.reload()
            }

            addSwitchRow(
                parent = section,
                title = getString(R.string.setting_js_injection),
                summary = getString(R.string.setting_js_injection_summary),
                checked = isJsInjectionEnabled()
            ) { enabled ->
                settingsManager.setJsInjectionEnabled(enabled)
                browserManager.reload()
            }

            addSwitchRow(
                parent = section,
                title = getString(R.string.setting_page_cleanup),
                summary = getString(R.string.setting_page_cleanup_summary),
                checked = isPageCleanupEnabled()
            ) { enabled ->
                settingsManager.setDomAdBlockEnabled(enabled)
                injectPageFeatures()
            }

            addSwitchRow(
                parent = section,
                title = getString(R.string.setting_video_enhancement),
                summary = getString(R.string.setting_video_enhancement_summary),
                checked = isVideoEnhancementEnabled()
            ) { enabled ->
                settingsManager.setVideoEnhancementEnabled(enabled)
                injectPageFeatures()
            }
        }
    }

    private fun addDataManagementSection(parent: LinearLayout) {
        addFunctionSection(parent, getString(R.string.function_center_section_data)) { section ->
            addActionRow(
                parent = section,
                title = getString(R.string.action_clear_browser_data),
                summary = getString(R.string.action_clear_browser_data_summary)
            ) {
                clearBrowserData()
            }
            addActionRow(
                parent = section,
                title = getString(R.string.action_restore_default_settings),
                summary = getString(R.string.action_restore_default_settings_summary)
            ) {
                showRestoreDefaultSettingsPage()
            }
        }
    }

    private fun createFunctionCenterPage(
        title: String,
        onBack: () -> Unit,
        buildContent: (LinearLayout) -> Unit
    ): View {
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            isClickable = true
            isFocusable = true
            setBackgroundColor(
                ContextCompat.getColor(this@MainActivity, R.color.browser_background)
            )
        }
        page.addView(
            createFunctionCenterToolbar(title, onBack),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(56)
            )
        )

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(24))
        }
        buildContent(content)

        val scrollView = ScrollView(this).apply {
            overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS
            addView(
                content,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }
        page.addView(
            scrollView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )
        return page
    }

    private fun createFunctionCenterToolbar(title: String, onBack: () -> Unit): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            elevation = dp(4).toFloat()
            setPadding(dp(4), 0, dp(12), 0)
            setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.browser_surface))

            val pageBackButton = ImageButton(this@MainActivity).apply {
                setImageResource(R.drawable.ic_arrow_back_24)
                setColorFilter(ContextCompat.getColor(this@MainActivity, R.color.browser_icon))
                background = ContextCompat.getDrawable(this@MainActivity, R.drawable.bg_icon_button)
                contentDescription = getString(R.string.action_back)
                scaleType = ImageView.ScaleType.CENTER
                setPadding(dp(16), dp(16), dp(16), dp(16))
                setOnClickListener { onBack() }
            }
            ViewCompat.setTooltipText(pageBackButton, getString(R.string.action_back))
            addView(
                pageBackButton,
                LinearLayout.LayoutParams(dp(52), ViewGroup.LayoutParams.MATCH_PARENT)
            )

            val titleView = TextView(this@MainActivity).apply {
                text = title
                setTextColor(ContextCompat.getColor(this@MainActivity, R.color.browser_text))
                textSize = 18f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER_VERTICAL
                includeFontPadding = false
            }
            addView(
                titleView,
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
            )
        }
    }

    private fun addFunctionCenterHeader(parent: LinearLayout, siteHost: String?) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = createRoundedBackground(
                ContextCompat.getColor(this@MainActivity, R.color.browser_surface)
            )
        }

        val avatar = TextView(this).apply {
            text = getString(R.string.function_center_profile_badge)
            gravity = Gravity.CENTER
            includeFontPadding = false
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            background = createCircleBackground(
                ContextCompat.getColor(this@MainActivity, R.color.browser_primary)
            )
        }
        card.addView(
            avatar,
            LinearLayout.LayoutParams(dp(52), dp(52))
        )

        val labels = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), 0, 0, 0)
        }
        labels.addView(
            TextView(this).apply {
                text = getString(R.string.function_center_profile_name)
                setTextColor(ContextCompat.getColor(this@MainActivity, R.color.browser_text))
                textSize = 18f
                typeface = Typeface.DEFAULT_BOLD
            }
        )
        labels.addView(
            TextView(this).apply {
                text = getString(R.string.function_center_profile_summary)
                setTextColor(ContextCompat.getColor(this@MainActivity, R.color.browser_text_hint))
                textSize = 13f
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
            }
        )
        labels.addView(
            TextView(this).apply {
                text = if (siteHost != null) {
                    getString(R.string.function_center_current_site, siteHost)
                } else {
                    getString(R.string.function_center_no_site)
                }
                setTextColor(ContextCompat.getColor(this@MainActivity, R.color.browser_icon_muted))
                textSize = 12f
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
            }
        )
        card.addView(
            labels,
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        )

        parent.addView(
            card,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun addFunctionSection(
        parent: LinearLayout,
        title: String,
        buildContent: (LinearLayout) -> Unit
    ) {
        addSectionTitle(parent, title)
        val section = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(4), dp(14), dp(4))
            background = createRoundedBackground(
                ContextCompat.getColor(this@MainActivity, R.color.browser_surface)
            )
        }
        buildContent(section)
        parent.addView(
            section,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun addSectionTitle(parent: LinearLayout, title: String) {
        parent.addView(
            TextView(this).apply {
                text = title
                setTextColor(ContextCompat.getColor(this@MainActivity, R.color.browser_text_hint))
                textSize = 13f
                typeface = Typeface.DEFAULT_BOLD
                includeFontPadding = false
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(18)
                bottomMargin = dp(8)
                marginStart = dp(4)
                marginEnd = dp(4)
            }
        )
    }

    private fun createRoundedBackground(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(8).toFloat()
        }
    }

    private fun createCircleBackground(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
        }
    }

    private fun showAdBlockLog() {
        val entries = adBlockLogger.entries()
        if (entries.isEmpty()) {
            Toast.makeText(this, R.string.toast_ad_block_log_empty, Toast.LENGTH_SHORT).show()
            return
        }

        showFunctionCenterSubPage(getString(R.string.title_ad_block_log)) { content ->
            addFunctionSection(content, getString(R.string.function_center_section_actions)) { section ->
                addActionRow(
                    parent = section,
                    title = getString(R.string.action_clear),
                    summary = getString(R.string.action_clear_ad_block_log_summary)
                ) {
                    adBlockLogger.clear()
                    Toast.makeText(
                        this,
                        R.string.toast_ad_block_log_cleared,
                        Toast.LENGTH_SHORT
                    ).show()
                    showFunctionCenterRootPage()
                }
            }

            addFunctionSection(content, getString(R.string.function_center_section_records)) { section ->
                entries.forEach { entry ->
                    val host = adBlockLogHost(entry)
                    val source = entry.ruleSource ?: entry.reason.name.lowercase(Locale.US)
                    val rule = entry.ruleId ?: entry.rulePattern ?: entry.reason.name
                    val rowTitle = "${adBlockLogTime(entry)} ${adBlockLogActionLabel(entry)} · $host"
                    val rowSummary = "$source  $rule"
                    val whitelistHost = entry.host?.takeIf { value -> value.isNotBlank() }
                    if (
                        entry.action == AdBlockLogAction.BLOCK &&
                        whitelistHost != null &&
                        !settingsManager.isUserWhitelistedSite(whitelistHost)
                    ) {
                        addActionRow(section, rowTitle, rowSummary) {
                            showAddWhitelistFromLogPage(whitelistHost)
                        }
                    } else {
                        addInfoRow(section, rowTitle, rowSummary)
                    }
                }
            }
        }
    }

    private fun adBlockLogTime(entry: AdBlockLogEntry): String {
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            .format(Date(entry.timestampMillis))
    }

    private fun adBlockLogActionLabel(entry: AdBlockLogEntry): String {
        return when (entry.action) {
            AdBlockLogAction.BLOCK -> getString(R.string.ad_block_log_action_blocked)
            AdBlockLogAction.ALLOW -> getString(R.string.ad_block_log_action_allowed)
        }
    }

    private fun adBlockLogHost(entry: AdBlockLogEntry): String {
        return entry.host ?: Uri.parse(entry.url).host ?: entry.url
    }

    private fun showAddWhitelistFromLogPage(host: String) {
        if (settingsManager.isUserWhitelistedSite(host)) {
            Toast.makeText(
                this,
                getString(R.string.toast_user_whitelist_already_added, host),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        showFunctionCenterSubPage(
            title = getString(R.string.title_add_user_whitelist),
            onBack = { showAdBlockLog() }
        ) { content ->
            addFunctionMessage(content, getString(R.string.dialog_add_user_whitelist_message, host))
            addFunctionSection(content, getString(R.string.function_center_section_actions)) { section ->
                addFunctionActionButton(section, getString(R.string.action_add)) {
                    settingsManager.setUserWhitelistedSite(host, true)
                    Toast.makeText(
                        this,
                        getString(R.string.toast_user_whitelist_added, host),
                        Toast.LENGTH_SHORT
                    ).show()
                    browserManager.reload()
                    showAdBlockLog()
                }
            }
        }
    }

    private fun showUserWhitelistManager() {
        val hosts = settingsManager.userWhitelistedSiteHosts().sorted()
        val currentHost = currentSiteHost()

        showFunctionCenterSubPage(getString(R.string.title_user_whitelist)) { content ->
            if (currentHost != null && !settingsManager.isUserWhitelistedSite(currentHost)) {
                addFunctionSection(content, getString(R.string.function_center_section_actions)) { section ->
                    addActionRow(
                        parent = section,
                        title = getString(R.string.action_add_current_site),
                        summary = currentHost
                    ) {
                        settingsManager.setUserWhitelistedSite(currentHost, true)
                        Toast.makeText(
                            this,
                            getString(R.string.toast_user_whitelist_added, currentHost),
                            Toast.LENGTH_SHORT
                        ).show()
                        browserManager.reload()
                        showUserWhitelistManager()
                    }
                }
            }

            if (hosts.isEmpty()) {
                addEmptyState(content, getString(R.string.dialog_user_whitelist_empty))
            } else {
                addFunctionSection(content, getString(R.string.function_center_section_sites)) { section ->
                    hosts.forEach { host ->
                        addActionRow(
                            parent = section,
                            title = host,
                            summary = getString(R.string.user_whitelist_host_summary)
                        ) {
                            showRemoveUserWhitelistHostPage(host)
                        }
                    }
                }
            }
        }
    }

    private fun toggleCurrentSiteWhitelist() {
        val host = currentSiteHost() ?: run {
            Toast.makeText(this, R.string.toast_no_page_url, Toast.LENGTH_SHORT).show()
            return
        }
        val shouldWhitelist = !settingsManager.isUserWhitelistedSite(host)
        settingsManager.setUserWhitelistedSite(host, shouldWhitelist)
        Toast.makeText(
            this,
            if (shouldWhitelist) {
                getString(R.string.toast_user_whitelist_added, host)
            } else {
                getString(R.string.toast_user_whitelist_removed, host)
            },
            Toast.LENGTH_SHORT
        ).show()
        browserManager.reload()
    }

    private fun showRemoveUserWhitelistHostPage(host: String) {
        showFunctionCenterSubPage(
            title = getString(R.string.title_remove_user_whitelist),
            onBack = { showUserWhitelistManager() }
        ) { content ->
            addFunctionMessage(content, getString(R.string.dialog_remove_user_whitelist_message, host))
            addFunctionSection(content, getString(R.string.function_center_section_actions)) { section ->
                addFunctionActionButton(
                    parent = section,
                    title = getString(R.string.action_remove),
                    backgroundColor = Color.parseColor("#D92D20")
                ) {
                    settingsManager.setUserWhitelistedSite(host, false)
                    Toast.makeText(
                        this,
                        getString(R.string.toast_user_whitelist_removed, host),
                        Toast.LENGTH_SHORT
                    ).show()
                    browserManager.reload()
                    showUserWhitelistManager()
                }
            }
        }
    }

    private fun addInfoRow(
        parent: LinearLayout,
        title: String,
        summary: String
    ) {
        val row = createRowText(title, summary).apply {
            minimumHeight = dp(58)
            setPadding(0, dp(9), 0, dp(9))
        }
        parent.addView(
            row,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun addFunctionMessage(parent: LinearLayout, message: String) {
        parent.addView(
            TextView(this).apply {
                text = message
                setTextColor(ContextCompat.getColor(this@MainActivity, R.color.browser_text))
                textSize = 15f
                setLineSpacing(dp(2).toFloat(), 1f)
                setPadding(dp(16), dp(16), dp(16), dp(16))
                background = createRoundedBackground(
                    ContextCompat.getColor(this@MainActivity, R.color.browser_surface)
                )
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(10)
            }
        )
    }

    private fun addEmptyState(parent: LinearLayout, message: String) {
        parent.addView(
            TextView(this).apply {
                text = message
                gravity = Gravity.CENTER
                setTextColor(ContextCompat.getColor(this@MainActivity, R.color.browser_text_hint))
                textSize = 14f
                setPadding(dp(16), dp(28), dp(16), dp(28))
                background = createRoundedBackground(
                    ContextCompat.getColor(this@MainActivity, R.color.browser_surface)
                )
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(18)
            }
        )
    }

    private fun addFunctionActionButton(
        parent: LinearLayout,
        title: String,
        backgroundColor: Int = ContextCompat.getColor(this, R.color.browser_primary),
        onClick: () -> Unit
    ) {
        parent.addView(
            TextView(this).apply {
                text = title
                gravity = Gravity.CENTER
                includeFontPadding = false
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.WHITE)
                textSize = 15f
                isClickable = true
                isFocusable = true
                background = createRoundedBackground(backgroundColor)
                setOnClickListener { onClick() }
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(46)
            ).apply {
                topMargin = dp(8)
                bottomMargin = dp(8)
            }
        )
    }

    private fun addSwitchRow(
        parent: LinearLayout,
        title: String,
        summary: String,
        checked: Boolean,
        enabled: Boolean = true,
        onChanged: (Boolean) -> Unit
    ) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            isClickable = enabled
            isFocusable = enabled
            isEnabled = enabled
            minimumHeight = dp(62)
            setPadding(0, dp(8), 0, dp(8))
            setSelectableItemBackground()
        }
        val labels = createRowText(title, summary)
        labels.isEnabled = enabled
        labels.alpha = if (enabled) 1f else 0.48f
        row.addView(
            labels,
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        )
        val switchView = SwitchCompat(this).apply {
            isChecked = checked
            isEnabled = enabled
        }
        row.addView(
            switchView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        switchView.setOnCheckedChangeListener { _, isChecked -> onChanged(isChecked) }
        row.setOnClickListener {
            if (enabled) {
                switchView.isChecked = !switchView.isChecked
            }
        }
        parent.addView(
            row,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun addActionRow(
        parent: LinearLayout,
        title: String,
        summary: String,
        enabled: Boolean = true,
        onClick: () -> Unit
    ) {
        val row = createRowText(title, summary).apply {
            isClickable = enabled
            isFocusable = enabled
            isEnabled = enabled
            alpha = if (enabled) 1f else 0.48f
            minimumHeight = dp(58)
            setPadding(0, dp(9), 0, dp(9))
            setSelectableItemBackground()
            if (enabled) {
                setOnClickListener { onClick() }
            }
        }
        parent.addView(
            row,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun createRowText(title: String, summary: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            val titleView = TextView(this@MainActivity).apply {
                text = title
                setTextColor(ContextCompat.getColor(this@MainActivity, R.color.browser_text))
                textSize = 15f
                typeface = Typeface.DEFAULT_BOLD
            }
            val summaryView = TextView(this@MainActivity).apply {
                text = summary
                setTextColor(ContextCompat.getColor(this@MainActivity, R.color.browser_text_hint))
                textSize = 12f
                maxLines = 2
                ellipsize = TextUtils.TruncateAt.END
            }
            addView(titleView)
            addView(summaryView)
        }
    }

    private fun addDivider(parent: LinearLayout) {
        parent.addView(
            View(this).apply {
                setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.browser_control_pressed))
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(1)
            ).apply {
                topMargin = dp(6)
                bottomMargin = dp(6)
            }
        )
    }

    private fun setupFileOperationLaunchers() {
        openLocalFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) {
                return@registerForActivityResult
            }
            persistUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            openLocalDocumentUri(uri)
        }

        openLocalDirectoryLauncher =
            registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
                if (uri == null) {
                    return@registerForActivityResult
                }
                persistUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                preferenceStore.putString(KEY_LOCAL_DIRECTORY_URI, uri.toString())
                showLocalDirectoryPage(uri)
            }
    }

    private fun persistUriPermission(uri: Uri, flags: Int) {
        runCatching {
            contentResolver.takePersistableUriPermission(uri, flags)
        }.onFailure {
            Log.w(RULE_LOG_TAG, "Unable to persist URI permission for $uri", it)
        }
    }

    private fun showFileOperationsPage() {
        showFunctionCenterSubPage(getString(R.string.title_file_operations)) { content ->
            addFunctionSection(content, getString(R.string.function_center_section_actions)) { section ->
                addActionRow(
                    parent = section,
                    title = getString(R.string.action_open_local_file),
                    summary = getString(R.string.action_open_local_file_summary)
                ) {
                    openLocalFileLauncher.launch(arrayOf("*/*"))
                }

                addActionRow(
                    parent = section,
                    title = getString(R.string.action_browse_local_folder),
                    summary = getString(R.string.action_browse_local_folder_summary)
                ) {
                    val uri = savedLocalDirectoryUri()
                    if (uri == null) {
                        openLocalDirectoryLauncher.launch(null)
                    } else {
                        showLocalDirectoryPage(uri)
                    }
                }

                addActionRow(
                    parent = section,
                    title = getString(R.string.action_choose_local_folder),
                    summary = getString(R.string.action_choose_local_folder_summary)
                ) {
                    openLocalDirectoryLauncher.launch(savedLocalDirectoryUri())
                }
            }
        }
    }

    private fun savedLocalDirectoryUri(): Uri? {
        return preferenceStore.getString(KEY_LOCAL_DIRECTORY_URI, null)
            ?.takeIf { it.isNotBlank() }
            ?.let(Uri::parse)
    }

    private fun showLocalDirectoryPage(treeUri: Uri) {
        val rootDocumentId = runCatching {
            DocumentsContract.getTreeDocumentId(treeUri)
        }.getOrNull()

        if (rootDocumentId == null) {
            preferenceStore.remove(KEY_LOCAL_DIRECTORY_URI)
            Toast.makeText(this, R.string.toast_local_folder_unavailable, Toast.LENGTH_SHORT).show()
            showFileOperationsPage()
            return
        }

        showLocalDirectoryPage(
            treeUri = treeUri,
            path = listOf(
                LocalDirectoryPathItem(
                    documentId = rootDocumentId,
                    title = getString(R.string.title_local_files)
                )
            )
        )
    }

    private fun showLocalDirectoryPage(treeUri: Uri, path: List<LocalDirectoryPathItem>) {
        val current = path.lastOrNull() ?: return showLocalDirectoryPage(treeUri)
        val onBack: () -> Unit = if (path.size > 1) {
            { showLocalDirectoryPage(treeUri, path.dropLast(1)) }
        } else {
            { showFileOperationsPage() }
        }

        showFunctionCenterSubPage(
            title = current.title,
            onBack = onBack
        ) { content ->
            addFunctionSection(content, getString(R.string.function_center_section_actions)) { section ->
                addActionRow(
                    parent = section,
                    title = getString(R.string.action_new_folder),
                    summary = getString(R.string.action_new_folder_summary)
                ) {
                    promptCreateLocalDocument(
                        treeUri = treeUri,
                        path = path,
                        mimeType = DocumentsContract.Document.MIME_TYPE_DIR,
                        defaultName = getString(R.string.default_new_folder_name),
                        dialogTitle = getString(R.string.title_new_folder)
                    )
                }

                addActionRow(
                    parent = section,
                    title = getString(R.string.action_new_text_file),
                    summary = getString(R.string.action_new_text_file_summary)
                ) {
                    promptCreateLocalDocument(
                        treeUri = treeUri,
                        path = path,
                        mimeType = "text/plain",
                        defaultName = getString(R.string.default_new_text_file_name),
                        dialogTitle = getString(R.string.title_new_text_file)
                    )
                }

                addActionRow(
                    parent = section,
                    title = getString(R.string.action_refresh),
                    summary = getString(R.string.action_refresh_local_folder_summary)
                ) {
                    showLocalDirectoryPage(treeUri, path)
                }
            }

            val documents = runCatching {
                queryLocalDocuments(treeUri, current.documentId)
            }.onFailure {
                Log.w(RULE_LOG_TAG, "Unable to query local directory $treeUri", it)
                Toast.makeText(
                    this,
                    R.string.toast_local_folder_unavailable,
                    Toast.LENGTH_SHORT
                ).show()
            }.getOrDefault(emptyList())

            if (documents.isEmpty()) {
                addEmptyState(content, getString(R.string.dialog_local_folder_empty))
                return@showFunctionCenterSubPage
            }

            addFunctionSection(content, getString(R.string.function_center_section_files)) { section ->
                documents.forEach { document ->
                    addActionRow(
                        parent = section,
                        title = document.name,
                        summary = localDocumentSummary(document)
                    ) {
                        if (document.isDirectory) {
                            showLocalDirectoryPage(
                                treeUri = treeUri,
                                path = path + LocalDirectoryPathItem(document.documentId, document.name)
                            )
                        } else {
                            showLocalDocumentActionsPage(document, treeUri, path)
                        }
                    }
                }
            }
        }
    }

    private fun queryLocalDocuments(treeUri: Uri, parentDocumentId: String): List<LocalDocument> {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            treeUri,
            parentDocumentId
        )
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_FLAGS
        )
        val documents = mutableListOf<LocalDocument>()
        contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
            val idIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
            val sizeIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)
            val modifiedIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
            val flagsIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_FLAGS)

            while (cursor.moveToNext()) {
                val documentId = cursor.getStringOrNull(idIndex) ?: continue
                val name = cursor.getStringOrNull(nameIndex)
                    ?.takeIf { it.isNotBlank() }
                    ?: documentId.substringAfterLast(':')
                documents += LocalDocument(
                    uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId),
                    documentId = documentId,
                    name = name,
                    mimeType = cursor.getStringOrNull(mimeIndex),
                    size = cursor.getLongOrNull(sizeIndex),
                    modifiedAt = cursor.getLongOrNull(modifiedIndex),
                    flags = cursor.getIntOrNull(flagsIndex) ?: 0
                )
            }
        }
        return documents.sortedWith(
            compareBy<LocalDocument> { !it.isDirectory }
                .thenBy { it.name.lowercase(Locale.getDefault()) }
        )
    }

    private fun showLocalDocumentActionsPage(
        document: LocalDocument,
        treeUri: Uri,
        path: List<LocalDirectoryPathItem>
    ) {
        showFunctionCenterSubPage(
            title = document.name,
            onBack = { showLocalDirectoryPage(treeUri, path) }
        ) { content ->
            addFunctionMessage(content, localDocumentSummary(document))
            addFunctionSection(content, getString(R.string.function_center_section_actions)) { section ->
                addActionRow(
                    parent = section,
                    title = getString(R.string.action_open_file),
                    summary = getString(R.string.action_open_file_summary)
                ) {
                    openLocalDocument(document)
                }

                addActionRow(
                    parent = section,
                    title = getString(R.string.action_share_file),
                    summary = getString(R.string.action_share_file_summary)
                ) {
                    shareLocalDocument(document)
                }

                if (document.canRename) {
                    addActionRow(
                        parent = section,
                        title = getString(R.string.action_rename_file),
                        summary = getString(R.string.action_rename_file_summary)
                    ) {
                        promptRenameLocalDocument(document, treeUri, path)
                    }
                }

                if (document.canDelete) {
                    addActionRow(
                        parent = section,
                        title = getString(R.string.action_delete_file),
                        summary = getString(R.string.action_delete_file_summary)
                    ) {
                        confirmDeleteLocalDocument(document, treeUri, path)
                    }
                }
            }
        }
    }

    private fun promptCreateLocalDocument(
        treeUri: Uri,
        path: List<LocalDirectoryPathItem>,
        mimeType: String,
        defaultName: String,
        dialogTitle: String
    ) {
        showNameInputDialog(
            title = dialogTitle,
            initialValue = defaultName,
            positiveButtonText = getString(R.string.action_create)
        ) { name ->
            val parent = path.lastOrNull() ?: return@showNameInputDialog
            val parentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, parent.documentId)
            val createdUri = runCatching {
                DocumentsContract.createDocument(contentResolver, parentUri, mimeType, name)
            }.getOrNull()

            if (createdUri == null) {
                Toast.makeText(this, R.string.toast_local_file_operation_failed, Toast.LENGTH_SHORT).show()
                return@showNameInputDialog
            }

            Toast.makeText(this, R.string.toast_local_file_created, Toast.LENGTH_SHORT).show()
            showLocalDirectoryPage(treeUri, path)
        }
    }

    private fun promptRenameLocalDocument(
        document: LocalDocument,
        treeUri: Uri,
        path: List<LocalDirectoryPathItem>
    ) {
        showNameInputDialog(
            title = getString(R.string.title_rename_file),
            initialValue = document.name,
            positiveButtonText = getString(R.string.action_rename)
        ) { name ->
            val renamedUri = runCatching {
                DocumentsContract.renameDocument(contentResolver, document.uri, name)
            }.getOrNull()

            if (renamedUri == null) {
                Toast.makeText(this, R.string.toast_local_file_operation_failed, Toast.LENGTH_SHORT).show()
                return@showNameInputDialog
            }

            Toast.makeText(this, R.string.toast_local_file_renamed, Toast.LENGTH_SHORT).show()
            showLocalDirectoryPage(treeUri, path)
        }
    }

    private fun confirmDeleteLocalDocument(
        document: LocalDocument,
        treeUri: Uri,
        path: List<LocalDirectoryPathItem>
    ) {
        AlertDialog.Builder(this)
            .setTitle(R.string.title_delete_file)
            .setMessage(getString(R.string.dialog_delete_local_file_message, document.name))
            .setPositiveButton(R.string.action_delete_file) { _, _ ->
                val deleted = runCatching {
                    DocumentsContract.deleteDocument(contentResolver, document.uri)
                }.getOrDefault(false)

                if (!deleted) {
                    Toast.makeText(
                        this,
                        R.string.toast_local_file_operation_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                Toast.makeText(this, R.string.toast_local_file_deleted, Toast.LENGTH_SHORT).show()
                showLocalDirectoryPage(treeUri, path)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showNameInputDialog(
        title: String,
        initialValue: String,
        positiveButtonText: String,
        onConfirm: (String) -> Unit
    ) {
        val input = EditText(this).apply {
            setText(initialValue)
            setSelectAllOnFocus(true)
            selectAll()
            setSingleLine(true)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        }

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(input)
            .setPositiveButton(positiveButtonText) { _, _ ->
                val name = input.text?.toString()?.trim().orEmpty()
                if (name.isBlank()) {
                    Toast.makeText(this, R.string.toast_local_file_name_invalid, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                onConfirm(name)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun openLocalDocument(document: LocalDocument) {
        openLocalDocumentUri(
            uri = document.uri,
            displayName = document.name,
            mimeType = document.mimeType
        )
    }

    private fun openLocalDocumentUri(
        uri: Uri,
        displayName: String? = null,
        mimeType: String? = null
    ) {
        val resolvedMimeType = mimeType ?: contentResolver.getType(uri)
        val title = displayName ?: localDisplayName(uri)
        if (MediaUrlUtils.isPlayableMediaUri(uri, resolvedMimeType)) {
            openNativePlayer(
                url = uri.toString(),
                mimeType = resolvedMimeType,
                titleOverride = title
            )
            return
        }

        openExternalDocument(uri, resolvedMimeType)
    }

    private fun openExternalDocument(uri: Uri, mimeType: String?) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType ?: "*/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(Intent.createChooser(intent, getString(R.string.action_open_file)))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, R.string.toast_no_external_browser, Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareLocalDocument(document: LocalDocument) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = document.mimeType?.takeUnless { document.isDirectory } ?: "*/*"
            putExtra(Intent.EXTRA_STREAM, document.uri)
            clipData = ClipData.newUri(contentResolver, document.name, document.uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(Intent.createChooser(intent, getString(R.string.action_share_file)))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, R.string.toast_no_external_browser, Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadCurrentUrl() {
        val url = currentShareableUrl() ?: run {
            Toast.makeText(this, R.string.toast_no_page_url, Toast.LENGTH_SHORT).show()
            return
        }
        enqueueDownload(
            url = url,
            userAgent = browserManager.userAgentString(),
            contentDisposition = null,
            mimeType = null
        )
    }

    private fun localDocumentSummary(document: LocalDocument): String {
        if (document.isDirectory) {
            return getString(R.string.local_file_type_folder)
        }

        val type = document.mimeType
            ?.takeIf { it.isNotBlank() }
            ?: getString(R.string.local_file_type_unknown)
        return listOf(
            type,
            formatFileSize(document.size),
            formatModifiedTime(document.modifiedAt)
        ).joinToString(separator = " · ")
    }

    private fun formatFileSize(size: Long?): String {
        if (size == null || size < 0) {
            return getString(R.string.local_file_size_unknown)
        }

        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var value = size.toDouble()
        var unitIndex = 0
        while (value >= 1024 && unitIndex < units.lastIndex) {
            value /= 1024
            unitIndex++
        }
        return if (unitIndex == 0) {
            "$size ${units[unitIndex]}"
        } else {
            String.format(Locale.getDefault(), "%.1f %s", value, units[unitIndex])
        }
    }

    private fun formatModifiedTime(modifiedAt: Long?): String {
        if (modifiedAt == null || modifiedAt <= 0L) {
            return getString(R.string.local_file_time_unknown)
        }
        return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            .format(Date(modifiedAt))
    }

    private fun localDisplayName(uri: Uri): String? {
        return contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            if (!cursor.moveToFirst()) {
                null
            } else {
                cursor.getStringOrNull(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        }
    }

    private fun Cursor.getStringOrNull(index: Int): String? {
        return if (index >= 0 && !isNull(index)) getString(index) else null
    }

    private fun Cursor.getLongOrNull(index: Int): Long? {
        return if (index >= 0 && !isNull(index)) getLong(index) else null
    }

    private fun Cursor.getIntOrNull(index: Int): Int? {
        return if (index >= 0 && !isNull(index)) getInt(index) else null
    }

    private fun toggleCurrentBookmark() {
        val page = currentSavedPage() ?: run {
            Toast.makeText(this, R.string.toast_no_page_url, Toast.LENGTH_SHORT).show()
            return
        }

        if (isSavedPage(KEY_BOOKMARKS, page.url)) {
            removeSavedPage(KEY_BOOKMARKS, page.url)
            Toast.makeText(this, R.string.toast_bookmark_removed, Toast.LENGTH_SHORT).show()
        } else {
            addSavedPage(KEY_BOOKMARKS, page, BOOKMARK_LIMIT)
            Toast.makeText(this, R.string.toast_bookmark_saved, Toast.LENGTH_SHORT).show()
        }
        updateBookmarkButton()
    }

    private fun showSavedPageList(key: String, title: String, emptyMessage: String) {
        val pages = loadSavedPages(key)
        if (pages.isEmpty()) {
            Toast.makeText(this, emptyMessage, Toast.LENGTH_SHORT).show()
            return
        }

        showFunctionCenterSubPage(title) { content ->
            addFunctionSection(content, getString(R.string.function_center_section_actions)) { section ->
                addActionRow(
                    parent = section,
                    title = getString(R.string.action_clear),
                    summary = getString(R.string.action_clear_saved_pages_summary)
                ) {
                    preferenceStore.remove(key)
                    Toast.makeText(
                        this,
                        R.string.toast_saved_pages_cleared,
                        Toast.LENGTH_SHORT
                    ).show()
                    showFunctionCenterRootPage()
                }
            }

            addFunctionSection(content, getString(R.string.function_center_section_records)) { section ->
                pages.forEach { page ->
                    addActionRow(
                        parent = section,
                        title = page.title.ifBlank { page.url },
                        summary = UrlUtils.displayUrl(page.url)
                    ) {
                        loadUrl(page.url)
                    }
                }
            }
        }
    }

    private fun copyCurrentUrl() {
        val url = currentShareableUrl() ?: run {
            Toast.makeText(this, R.string.toast_no_page_url, Toast.LENGTH_SHORT).show()
            return
        }
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.clipboard_page_url), url))
        Toast.makeText(this, R.string.toast_link_copied, Toast.LENGTH_SHORT).show()
    }

    private fun shareCurrentUrl() {
        val url = currentShareableUrl() ?: run {
            Toast.makeText(this, R.string.toast_no_page_url, Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.action_share_page)))
    }

    private fun openCurrentUrlExternally() {
        val url = currentShareableUrl() ?: run {
            Toast.makeText(this, R.string.toast_no_page_url, Toast.LENGTH_SHORT).show()
            return
        }
        openExternalUrl(url)
    }

    private fun openCurrentUrlInNativePlayer() {
        val url = currentShareableUrl() ?: run {
            Toast.makeText(this, R.string.toast_no_page_url, Toast.LENGTH_SHORT).show()
            return
        }
        if (!MediaUrlUtils.isPlayableMediaUri(Uri.parse(url))) {
            Toast.makeText(this, R.string.toast_media_url_unsupported, Toast.LENGTH_SHORT).show()
            return
        }
        openNativePlayer(url)
    }

    private fun clearBrowserData() {
        browserManager.clearBrowsingData()
        preferenceStore.remove(KEY_HISTORY)
        Toast.makeText(this, R.string.toast_browser_data_cleared, Toast.LENGTH_SHORT).show()
        updateNavigationButtons()
    }

    private fun showRestoreDefaultSettingsPage() {
        showFunctionCenterSubPage(getString(R.string.action_restore_default_settings)) { content ->
            addFunctionMessage(content, getString(R.string.dialog_restore_default_settings_message))
            addFunctionSection(content, getString(R.string.function_center_section_actions)) { section ->
                addFunctionActionButton(section, getString(R.string.action_restore)) {
                    restoreDefaultSettings()
                }
            }
        }
    }

    private fun restoreDefaultSettings() {
        settingsManager.restoreDefaults()
        Toast.makeText(this, R.string.toast_default_settings_restored, Toast.LENGTH_SHORT).show()
        recreate()
    }

    private fun addHistoryEntry(url: String?) {
        val page = currentSavedPage(url) ?: return
        addSavedPage(KEY_HISTORY, page, HISTORY_LIMIT)
    }

    private fun currentSavedPage(urlOverride: String? = null): SavedPage? {
        val url = urlOverride ?: currentActionableUrl()
        if (url.isNullOrBlank() || !isShareableUrl(url)) {
            return null
        }
        val title = currentPageTitle
            .takeIf { it.isNotBlank() && !it.equals(url, ignoreCase = true) }
            ?: Uri.parse(url).host
            ?: url
        return SavedPage(title = title, url = url)
    }

    private fun addSavedPage(key: String, page: SavedPage, limit: Int) {
        val pages = loadSavedPages(key)
            .filterNot { it.url.equals(page.url, ignoreCase = true) }
            .toMutableList()
        pages.add(0, page)
        saveSavedPages(key, pages.take(limit))
    }

    private fun removeSavedPage(key: String, url: String) {
        val pages = loadSavedPages(key)
            .filterNot { it.url.equals(url, ignoreCase = true) }
        saveSavedPages(key, pages)
    }

    private fun isSavedPage(key: String, url: String): Boolean {
        return loadSavedPages(key).any { it.url.equals(url, ignoreCase = true) }
    }

    private fun loadSavedPages(key: String): MutableList<SavedPage> {
        val rawValue = preferenceStore.getString(key, null) ?: return mutableListOf()
        return runCatching {
            val array = JSONArray(rawValue)
            MutableList(array.length()) { index ->
                val item = array.getJSONObject(index)
                SavedPage(
                    title = item.optString(JSON_TITLE),
                    url = item.optString(JSON_URL)
                )
            }.filter { it.url.isNotBlank() }.toMutableList()
        }.getOrDefault(mutableListOf())
    }

    private fun saveSavedPages(key: String, pages: List<SavedPage>) {
        val array = JSONArray()
        pages.forEach { page ->
            array.put(
                JSONObject()
                    .put(JSON_TITLE, page.title)
                    .put(JSON_URL, page.url)
            )
        }
        preferenceStore.putString(key, array.toString())
    }

    private fun createRuleEngine(): RuleEngine {
        val loader = RuleFileLoader.fromAssets(
            assets = assets,
            cacheDirectory = File(filesDir, RULE_CACHE_DIR)
        )
        val requestResult = loader.loadRequestRules()
        val cssResult = loader.loadCssRules()
        val domResult = loader.loadDomRules()
        logSkippedRules(requestResult.skippedRules + cssResult.skippedRules + domResult.skippedRules)
        val requestRules = BuiltInAdBlockRules.requestRules() + requestResult.rules
        val elementRules = cssResult.rules + domResult.rules
        return RuleEngine(
            rules = requestRules,
            elementRules = elementRules
        )
    }

    private fun logSkippedRules(skippedRules: List<SkippedRule>) {
        skippedRules.forEach { skippedRule ->
            Log.w(
                RULE_LOG_TAG,
                "Skipped ${skippedRule.source}:${skippedRule.lineNumber} " +
                    "(${skippedRule.reason}): ${skippedRule.text}"
            )
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

    private fun setupDownloadHandling() {
        browserManager.setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
            val mediaUri = url?.takeIf {
                MediaUrlUtils.isPlayableMediaUri(Uri.parse(it), mimeType)
            }
            if (mediaUri != null) {
                openNativePlayer(
                    url = mediaUri,
                    mimeType = mimeType,
                    userAgentOverride = userAgent
                )
                return@setDownloadListener
            }

            enqueueDownload(
                url = url,
                userAgent = userAgent,
                contentDisposition = contentDisposition,
                mimeType = mimeType
            )
        }
    }

    private fun enqueueDownload(
        url: String?,
        userAgent: String?,
        contentDisposition: String?,
        mimeType: String?
    ) {
        if (url.isNullOrBlank()) {
            Toast.makeText(this, R.string.toast_download_failed, Toast.LENGTH_SHORT).show()
            return
        }

        val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
        runCatching {
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setTitle(fileName)
                setDescription(getString(R.string.toast_download_started))
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                mimeType?.takeIf { it.isNotBlank() }?.let { setMimeType(it) }
                userAgent?.takeIf { it.isNotBlank() }?.let { addRequestHeader("User-Agent", it) }
                CookieManager.getInstance().getCookie(url)?.let { addRequestHeader("Cookie", it) }
            }
            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
        }.onSuccess {
            Toast.makeText(this, R.string.toast_download_started, Toast.LENGTH_SHORT).show()
        }.onFailure {
            openExternalUrl(url)
        }
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

    private fun defaultVideoSpeed(): Float {
        return if (::settingsManager.isInitialized) {
            settingsManager.defaultVideoSpeed()
        } else {
            SettingsManager.DEFAULT_VIDEO_SPEED
        }
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
        UrlUtils.resolveAddressInput(input, selectedSearchProvider.searchUrlPrefix)
            ?.let { loadUrl(it) }
    }

    private fun openHomePage() {
        loadUrl(settingsManager.homeUrlOr(selectedSearchProvider.homeUrl))
    }

    private fun loadUrl(url: String) {
        if (isFunctionCenterVisible) {
            closeFunctionCenter()
        }
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
        if (isProviderHomeUrl(url)) {
            return ""
        }

        searchProviders.forEach { provider ->
            UrlUtils.searchQueryFromUrl(url, provider.searchUrlPrefix)?.let { return it }
        }
        return UrlUtils.displayUrl(url)
    }

    private fun updateNavigationButtons() {
        backButton.isEnabled = browserManager.canGoBack()
        updateBookmarkButton()
    }

    private fun updateBookmarkButton() {
        if (!::bookmarkButton.isInitialized || !::preferenceStore.isInitialized) {
            return
        }

        // 页面级收藏入口直接跟随 WebView 当前地址，避免依赖功能中心上下文。
        val url = currentActionableUrl()
        val isEnabled = url != null
        val isBookmarked = url?.let { isSavedPage(KEY_BOOKMARKS, it) } ?: false
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
        if (url.isNullOrBlank()) {
            return false
        }

        val currentUri = Uri.parse(url)
        return searchProviders.any { provider ->
            val homeUri = Uri.parse(provider.homeUrl)
            currentUri.scheme.equals(homeUri.scheme, ignoreCase = true) &&
                currentUri.host.equals(homeUri.host, ignoreCase = true) &&
                normalizedPath(currentUri) == normalizedPath(homeUri)
        }
    }

    private fun normalizedPath(uri: Uri): String {
        return uri.path.orEmpty().trim('/')
    }

    companion object {
        private const val KEY_BOOKMARKS = "bookmarks"
        private const val KEY_HISTORY = "history"
        private const val KEY_LOCAL_DIRECTORY_URI = "local_directory_uri"
        private const val JSON_TITLE = "title"
        private const val JSON_URL = "url"
        private const val NATIVE_BRIDGE_NAME = "VideoBrowserNative"
        private const val EXIT_VIDEO_FULLSCREEN_SCRIPT =
            "if(window.VideoBrowserEnhancer){window.VideoBrowserEnhancer.exitFullscreen();}"
        private const val START_ELEMENT_PICKER_SCRIPT =
            "if(window.VideoBrowserEnhancer&&typeof window.VideoBrowserEnhancer.startElementPicker==='function'){" +
                "window.VideoBrowserEnhancer.startElementPicker();" +
                "}"
        private const val FINISH_ELEMENT_PICKER_SCRIPT =
            "if(window.VideoBrowserEnhancer&&typeof window.VideoBrowserEnhancer.finishElementPicker==='function'){" +
                "window.VideoBrowserEnhancer.finishElementPicker();" +
                "}"
        private const val FULLSCREEN_CONTROLS_WAKE_THROTTLE_MS = 250L
        private const val ELEMENT_PICKER_TIMEOUT_MS = 60_000L
        private const val BOOKMARK_LIMIT = 100
        private const val HISTORY_LIMIT = 80
        private const val RULE_CACHE_DIR = "rules"
        private const val RULE_LOG_TAG = "VideoBrowserRules"
        private const val BROWSER_CONTROLS_SCROLL_THRESHOLD_DP = 48
        private const val BROWSER_CONTROLS_SCROLL_COOLDOWN_MS = 500L
        private const val DESKTOP_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
    }
}
