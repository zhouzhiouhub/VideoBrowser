package com.example.videobrowser

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.URLUtil
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.videobrowser.browser.BrowserClient
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.browser.ChromeClient
import com.example.videobrowser.utils.MediaUrlUtils
import com.example.videobrowser.utils.UrlUtils
import com.example.videobrowser.video.FullscreenVideoGestureOverlay
import com.example.videobrowser.video.PlayerActivity
import java.io.ByteArrayInputStream
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

    private lateinit var rootView: View
    private lateinit var topBar: View
    private lateinit var bottomBar: View
    private lateinit var webView: WebView
    private lateinit var addressInput: EditText
    private lateinit var pageProgress: ProgressBar
    private lateinit var searchProviderScroll: HorizontalScrollView
    private lateinit var searchProviderList: LinearLayout
    private lateinit var anonymousBadge: TextView
    private lateinit var backButton: ImageButton
    private lateinit var forwardButton: ImageButton
    private lateinit var refreshButton: ImageButton
    private lateinit var homeButton: ImageButton
    private lateinit var menuButton: ImageButton
    private lateinit var loadButton: ImageButton
    private lateinit var fullscreenContainer: FrameLayout
    private lateinit var fullscreenGestureOverlay: FullscreenVideoGestureOverlay
    private lateinit var appPreferences: SharedPreferences
    private lateinit var browserManager: BrowserManager
    private lateinit var chromeClient: ChromeClient

    private val searchProviders = listOf(
        SearchProvider(
            id = "baidu",
            name = "百度",
            badge = "百",
            homeUrl = "https://m.baidu.com/",
            searchUrlPrefix = "https://www.baidu.com/s?wd=",
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
    private var defaultUserAgent: String? = null
    private var currentPageTitle = ""
    private var isPageLoading = false
    private var fullscreenPlaybackSpeed = DEFAULT_VIDEO_SPEED
    private var fullscreenVideoPositionMs: Long? = null
    private var fullscreenVideoDurationMs: Long? = null
    private var lastFullscreenControlsWakeAt = 0L
    private val commonScript: String by lazy {
        assets.open(COMMON_SCRIPT_ASSET).bufferedReader().use { it.readText() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rootView = findViewById(R.id.rootView)
        topBar = findViewById(R.id.topBar)
        bottomBar = findViewById(R.id.bottomBar)
        webView = findViewById(R.id.webView)
        addressInput = findViewById(R.id.addressInput)
        pageProgress = findViewById(R.id.pageProgress)
        searchProviderScroll = findViewById(R.id.searchProviderScroll)
        searchProviderList = findViewById(R.id.searchProviderList)
        anonymousBadge = findViewById(R.id.anonymousBadge)
        loadButton = findViewById(R.id.loadButton)
        backButton = findViewById(R.id.backButton)
        forwardButton = findViewById(R.id.forwardButton)
        refreshButton = findViewById(R.id.refreshButton)
        homeButton = findViewById(R.id.homeButton)
        menuButton = findViewById(R.id.menuButton)
        fullscreenContainer = findViewById(R.id.fullscreenContainer)
        appPreferences = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        browserManager = BrowserManager(webView)

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
        if (::chromeClient.isInitialized) {
            chromeClient.hideCustomView()
        }
        browserManager.destroy()
        super.onDestroy()
    }

    private fun setupBrowserControls() {
        ViewCompat.setTooltipText(anonymousBadge, getString(R.string.anonymous_badge))
        ViewCompat.setTooltipText(loadButton, getString(R.string.action_load_url))
        ViewCompat.setTooltipText(backButton, getString(R.string.action_back))
        ViewCompat.setTooltipText(forwardButton, getString(R.string.action_forward))
        ViewCompat.setTooltipText(refreshButton, getString(R.string.action_refresh))
        ViewCompat.setTooltipText(homeButton, getString(R.string.action_home))
        ViewCompat.setTooltipText(menuButton, getString(R.string.action_menu))

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
        forwardButton.setOnClickListener {
            browserManager.goForward()
            updateNavigationButtons()
        }
        refreshButton.setOnClickListener { browserManager.reload() }
        homeButton.setOnClickListener { openHomePage() }
        menuButton.setOnClickListener { showFunctionCenter() }

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
                requestIntercepted = ::handleRequestIntercept,
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
                    resetFullscreenVideoTimeline()
                    lastFullscreenControlsWakeAt = 0L
                    fullscreenPlaybackSpeed = DEFAULT_VIDEO_SPEED
                    fullscreenGestureOverlay.setPlaybackSpeed(DEFAULT_VIDEO_SPEED)
                    fullscreenGestureOverlay.setLandscape(chromeClient.isFullscreenLandscape())
                    fullscreenGestureOverlay.showOverlay()
                    setFullscreenVideoPlaybackSpeed(DEFAULT_VIDEO_SPEED)
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
                    resetFullscreenVideoTimeline()
                    lastFullscreenControlsWakeAt = 0L
                    fullscreenPlaybackSpeed = DEFAULT_VIDEO_SPEED
                    setFullscreenVideoPlaybackSpeed(DEFAULT_VIDEO_SPEED)
                    fullscreenGestureOverlay.hideOverlay()
                }
            }
        }
        ViewCompat.requestApplyInsets(rootView)
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
            DEFAULT_VIDEO_SPEED
        }
        fullscreenPlaybackSpeed = normalizedSpeed
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

    private fun setupWebViewScrollControls() {
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

    private fun resetScrollControlTracking(changeAt: Long = lastScrollControlChangeAt) {
        scrollControlDeltaY = 0
        scrollControlDirection = 0
        lastScrollControlChangeAt = changeAt
    }

    private fun setBrowserControlsHidden(hidden: Boolean) {
        val shouldHide = hidden || isVideoFullscreenUiActive
        if (areBrowserControlsHidden == shouldHide) {
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
        appPreferences.edit()
            .putString(KEY_SEARCH_PROVIDER, provider.id)
            .apply()
        updateSearchProviderSelection()
        if (shouldOpenProviderHome) {
            openHomePage()
        }
    }

    private fun loadSavedSearchProvider(): SearchProvider {
        val savedProviderId = appPreferences
            .getString(KEY_SEARCH_PROVIDER, null)
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
                    if (::chromeClient.isInitialized && chromeClient.isShowingCustomView()) {
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
    }

    private fun showFunctionCenter() {
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(8), dp(16), dp(4))
        }

        addSwitchRow(
            parent = content,
            title = getString(R.string.setting_ad_block),
            summary = getString(R.string.setting_ad_block_summary),
            checked = isAdBlockEnabled()
        ) { enabled ->
            appPreferences.edit().putBoolean(KEY_AD_BLOCK, enabled).apply()
            browserManager.reload()
        }

        addSwitchRow(
            parent = content,
            title = getString(R.string.setting_page_cleanup),
            summary = getString(R.string.setting_page_cleanup_summary),
            checked = isPageCleanupEnabled()
        ) { enabled ->
            appPreferences.edit().putBoolean(KEY_PAGE_CLEANUP, enabled).apply()
            injectPageFeatures()
        }

        addSwitchRow(
            parent = content,
            title = getString(R.string.setting_video_enhancement),
            summary = getString(R.string.setting_video_enhancement_summary),
            checked = isVideoEnhancementEnabled()
        ) { enabled ->
            appPreferences.edit().putBoolean(KEY_VIDEO_ENHANCEMENT, enabled).apply()
            injectPageFeatures()
        }

        addSwitchRow(
            parent = content,
            title = getString(R.string.setting_desktop_mode),
            summary = getString(R.string.setting_desktop_mode_summary),
            checked = isDesktopModeEnabled()
        ) { enabled ->
            appPreferences.edit().putBoolean(KEY_DESKTOP_MODE, enabled).apply()
            applyDesktopMode(reload = true)
        }

        addDivider(content)

        addActionRow(
            parent = content,
            title = getString(R.string.action_open_native_player),
            summary = getString(R.string.action_open_native_player_summary)
        ) {
            openCurrentUrlInNativePlayer()
        }
        addActionRow(
            parent = content,
            title = getString(R.string.action_add_bookmark),
            summary = getString(R.string.action_add_bookmark_summary)
        ) {
            saveCurrentBookmark()
        }
        addActionRow(
            parent = content,
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
            parent = content,
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
            parent = content,
            title = getString(R.string.action_copy_link),
            summary = getString(R.string.action_copy_link_summary)
        ) {
            copyCurrentUrl()
        }
        addActionRow(
            parent = content,
            title = getString(R.string.action_share_page),
            summary = getString(R.string.action_share_page_summary)
        ) {
            shareCurrentUrl()
        }
        addActionRow(
            parent = content,
            title = getString(R.string.action_open_external),
            summary = getString(R.string.action_open_external_summary)
        ) {
            openCurrentUrlExternally()
        }
        addActionRow(
            parent = content,
            title = getString(R.string.action_clear_browser_data),
            summary = getString(R.string.action_clear_browser_data_summary)
        ) {
            clearBrowserData()
        }
        addActionRow(
            parent = content,
            title = getString(R.string.action_restore_default_settings),
            summary = getString(R.string.action_restore_default_settings_summary)
        ) {
            showRestoreDefaultSettingsDialog()
        }

        val scrollView = ScrollView(this).apply {
            addView(
                content,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.title_function_center)
            .setView(scrollView)
            .setNegativeButton(R.string.action_close, null)
            .show()
    }

    private fun addSwitchRow(
        parent: LinearLayout,
        title: String,
        summary: String,
        checked: Boolean,
        onChanged: (Boolean) -> Unit
    ) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            isClickable = true
            isFocusable = true
            minimumHeight = dp(62)
            setPadding(0, dp(8), 0, dp(8))
            setSelectableItemBackground()
        }
        val labels = createRowText(title, summary)
        row.addView(
            labels,
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        )
        val switchView = SwitchCompat(this).apply {
            isChecked = checked
        }
        row.addView(
            switchView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        switchView.setOnCheckedChangeListener { _, isChecked -> onChanged(isChecked) }
        row.setOnClickListener { switchView.isChecked = !switchView.isChecked }
        parent.addView(row)
    }

    private fun addActionRow(
        parent: LinearLayout,
        title: String,
        summary: String,
        onClick: () -> Unit
    ) {
        val row = createRowText(title, summary).apply {
            isClickable = true
            isFocusable = true
            minimumHeight = dp(58)
            setPadding(0, dp(9), 0, dp(9))
            setSelectableItemBackground()
            setOnClickListener { onClick() }
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

    private fun saveCurrentBookmark() {
        val page = currentSavedPage() ?: run {
            Toast.makeText(this, R.string.toast_no_page_url, Toast.LENGTH_SHORT).show()
            return
        }
        addSavedPage(KEY_BOOKMARKS, page, BOOKMARK_LIMIT)
        Toast.makeText(this, R.string.toast_bookmark_saved, Toast.LENGTH_SHORT).show()
    }

    private fun showSavedPageList(key: String, title: String, emptyMessage: String) {
        val pages = loadSavedPages(key)
        if (pages.isEmpty()) {
            Toast.makeText(this, emptyMessage, Toast.LENGTH_SHORT).show()
            return
        }

        val labels = pages.map { page ->
            "${page.title.ifBlank { page.url }}\n${page.url}"
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle(title)
            .setItems(labels) { _, which ->
                loadUrl(pages[which].url)
            }
            .setNeutralButton(R.string.action_clear) { _, _ ->
                appPreferences.edit().remove(key).apply()
                Toast.makeText(this, R.string.toast_saved_pages_cleared, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.action_close, null)
            .show()
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
        appPreferences.edit().remove(KEY_HISTORY).apply()
        Toast.makeText(this, R.string.toast_browser_data_cleared, Toast.LENGTH_SHORT).show()
        updateNavigationButtons()
    }

    private fun showRestoreDefaultSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.action_restore_default_settings)
            .setMessage(R.string.dialog_restore_default_settings_message)
            .setPositiveButton(R.string.action_restore) { _, _ ->
                restoreDefaultSettings()
            }
            .setNegativeButton(R.string.action_close, null)
            .show()
    }

    private fun restoreDefaultSettings() {
        appPreferences.edit()
            .remove(KEY_SEARCH_PROVIDER)
            .remove(KEY_AD_BLOCK)
            .remove(KEY_PAGE_CLEANUP)
            .remove(KEY_VIDEO_ENHANCEMENT)
            .remove(KEY_DESKTOP_MODE)
            .commit()
        Toast.makeText(this, R.string.toast_default_settings_restored, Toast.LENGTH_SHORT).show()
        recreate()
    }

    private fun addHistoryEntry(url: String?) {
        val page = currentSavedPage(url) ?: return
        addSavedPage(KEY_HISTORY, page, HISTORY_LIMIT)
    }

    private fun currentSavedPage(urlOverride: String? = null): SavedPage? {
        val url = urlOverride ?: browserManager.currentUrl()
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

    private fun loadSavedPages(key: String): MutableList<SavedPage> {
        val rawValue = appPreferences.getString(key, null) ?: return mutableListOf()
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
        appPreferences.edit().putString(key, array.toString()).apply()
    }

    private fun isAdBlockEnabled(): Boolean {
        return appPreferences.getBoolean(KEY_AD_BLOCK, true)
    }

    private fun isPageCleanupEnabled(): Boolean {
        return appPreferences.getBoolean(KEY_PAGE_CLEANUP, true)
    }

    private fun isVideoEnhancementEnabled(): Boolean {
        return appPreferences.getBoolean(KEY_VIDEO_ENHANCEMENT, true)
    }

    private fun isDesktopModeEnabled(): Boolean {
        return appPreferences.getBoolean(KEY_DESKTOP_MODE, false)
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
        browserManager.applyDesktopMode(
            enabled = isDesktopModeEnabled(),
            desktopUserAgent = DESKTOP_USER_AGENT,
            defaultUserAgent = defaultUserAgent,
            reload = reload
        )
    }

    private fun injectPageFeatures() {
        val config = JSONObject()
            .put("cleanupEnabled", isPageCleanupEnabled())
            .put("videoEnabled", isVideoEnhancementEnabled())
            .toString()
        val script = """
            (function () {
              window.__VIDEOBROWSER_CONFIG__ = $config;
              $commonScript
              if (window.VideoBrowserEnhancer) {
                window.VideoBrowserEnhancer.apply(window.__VIDEOBROWSER_CONFIG__);
              }
            })();
        """.trimIndent()
        browserManager.evaluateJavascript(script)
    }

    private fun handleRequestIntercept(uri: Uri, isMainFrame: Boolean): WebResourceResponse? {
        return if (shouldBlockAdRequest(uri, isMainFrame)) {
            emptyWebResponse()
        } else {
            null
        }
    }

    private fun shouldBlockAdRequest(uri: Uri, isMainFrame: Boolean): Boolean {
        if (!isAdBlockEnabled() || isMainFrame || !isWebUrl(uri.scheme)) {
            return false
        }

        val host = uri.host?.lowercase().orEmpty()
        val url = uri.toString().lowercase()
        return BLOCKED_AD_HOST_KEYWORDS.any { host.contains(it) } ||
            BLOCKED_AD_URL_KEYWORDS.any { url.contains(it) }
    }

    private fun emptyWebResponse(): WebResourceResponse {
        return WebResourceResponse(
            "text/plain",
            "utf-8",
            ByteArrayInputStream(ByteArray(0))
        ).apply {
            setStatusCodeAndReasonPhrase(204, "No Content")
        }
    }

    private fun currentShareableUrl(): String? {
        return browserManager.currentUrl()?.takeIf { isShareableUrl(it) }
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
        userAgentOverride: String? = null
    ) {
        val title = currentPageTitle
            .takeIf { it.isNotBlank() && !it.equals(url, ignoreCase = true) }
            ?: URLUtil.guessFileName(url, null, mimeType)
        val referer = currentShareableUrl()?.takeIf { !it.equals(url, ignoreCase = true) }
        val cookie = if (isShareableUrl(url)) {
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
        loadUrl(selectedSearchProvider.homeUrl)
    }

    private fun loadUrl(url: String) {
        if (MediaUrlUtils.isPlayableMediaUri(Uri.parse(url))) {
            openNativePlayer(url)
            return
        }

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

        val displayUrl = if (isProviderHomeUrl(url)) "" else url
        if (addressInput.text?.toString() == displayUrl) {
            return
        }
        addressInput.setText(displayUrl)
        addressInput.setSelection(addressInput.text?.length ?: 0)
    }

    private fun updateNavigationButtons() {
        backButton.isEnabled = browserManager.canGoBack()
        forwardButton.isEnabled = browserManager.canGoForward()
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
        private const val PREFERENCES_NAME = "browser_preferences"
        private const val KEY_SEARCH_PROVIDER = "search_provider"
        private const val KEY_AD_BLOCK = "ad_block"
        private const val KEY_PAGE_CLEANUP = "page_cleanup"
        private const val KEY_VIDEO_ENHANCEMENT = "video_enhancement"
        private const val KEY_DESKTOP_MODE = "desktop_mode"
        private const val KEY_BOOKMARKS = "bookmarks"
        private const val KEY_HISTORY = "history"
        private const val JSON_TITLE = "title"
        private const val JSON_URL = "url"
        private const val COMMON_SCRIPT_ASSET = "scripts/common.js"
        private const val NATIVE_BRIDGE_NAME = "VideoBrowserNative"
        private const val EXIT_VIDEO_FULLSCREEN_SCRIPT =
            "if(window.VideoBrowserEnhancer){window.VideoBrowserEnhancer.exitFullscreen();}"
        private const val DEFAULT_VIDEO_SPEED = 1f
        private const val FULLSCREEN_CONTROLS_WAKE_THROTTLE_MS = 250L
        private const val BOOKMARK_LIMIT = 100
        private const val HISTORY_LIMIT = 80
        private const val BROWSER_CONTROLS_SCROLL_THRESHOLD_DP = 48
        private const val BROWSER_CONTROLS_SCROLL_COOLDOWN_MS = 500L
        private const val DESKTOP_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
        private val BLOCKED_AD_HOST_KEYWORDS = listOf(
            "doubleclick.net",
            "googlesyndication.com",
            "googleadservices.com",
            "adservice.google.com",
            "googleads.g.doubleclick.net",
            "adnxs.com",
            "adsystem.com",
            "taboola.com",
            "outbrain.com",
            "ads-twitter.com",
            "analytics.yahoo.com"
        )
        private val BLOCKED_AD_URL_KEYWORDS = listOf(
            "/pagead/",
            "/adservice/",
            "/adserver/",
            "/advert/",
            "/ads/",
            "/adx/",
            "googleads",
            "doubleclick",
            "ad.m3u8",
            "vast",
            "vmap",
            "preroll",
            "midroll"
        )
    }
}
