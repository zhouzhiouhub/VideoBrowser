package com.example.videobrowser

import android.content.Context
import android.graphics.Color
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
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
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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

    private lateinit var webView: WebView
    private lateinit var addressInput: EditText
    private lateinit var pageProgress: ProgressBar
    private lateinit var searchProviderList: LinearLayout
    private lateinit var anonymousBadge: TextView
    private lateinit var backButton: ImageButton
    private lateinit var forwardButton: ImageButton
    private lateinit var refreshButton: ImageButton
    private lateinit var homeButton: ImageButton
    private lateinit var menuButton: ImageButton
    private lateinit var loadButton: ImageButton

    private val searchProviders = listOf(
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
            name = "神马搜索",
            badge = "神",
            homeUrl = "https://so.m.sm.cn/",
            searchUrlPrefix = "https://so.m.sm.cn/s?q=",
            accentColor = Color.parseColor("#F28C20")
        ),
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rootView = findViewById<View>(R.id.rootView)
        webView = findViewById(R.id.webView)
        addressInput = findViewById(R.id.addressInput)
        pageProgress = findViewById(R.id.pageProgress)
        searchProviderList = findViewById(R.id.searchProviderList)
        anonymousBadge = findViewById(R.id.anonymousBadge)
        loadButton = findViewById(R.id.loadButton)
        backButton = findViewById(R.id.backButton)
        forwardButton = findViewById(R.id.forwardButton)
        refreshButton = findViewById(R.id.refreshButton)
        homeButton = findViewById(R.id.homeButton)
        menuButton = findViewById(R.id.menuButton)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val safeArea = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or
                    WindowInsetsCompat.Type.displayCutout()
            )
            view.setPadding(safeArea.left, safeArea.top, safeArea.right, safeArea.bottom)
            insets
        }
        ViewCompat.requestApplyInsets(rootView)

        setupSearchProviders()
        setupBrowserControls()
        setupBackNavigation()
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            cacheMode = WebSettings.LOAD_DEFAULT
            loadWithOverviewMode = true
            useWideViewPort = true
            loadsImagesAutomatically = true
            blockNetworkImage = false
            setSupportMultipleWindows(false)
            setGeolocationEnabled(false)
            @Suppress("DEPRECATION")
            databaseEnabled = true
            @Suppress("DEPRECATION")
            saveFormData = false
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                pageProgress.progress = newProgress
                pageProgress.visibility =
                    if (newProgress in 1..99) View.VISIBLE else View.INVISIBLE
                updateNavigationButtons()
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                val isProviderHome = isProviderHomeUrl(url)
                updateAddressBar(url)
                showHomeContent(isProviderHome)
                pageProgress.progress = 0
                pageProgress.visibility = if (isProviderHome) View.INVISIBLE else View.VISIBLE
                updateNavigationButtons()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                val isProviderHome = isProviderHomeUrl(url)
                updateAddressBar(url)
                showHomeContent(isProviderHome)
                pageProgress.visibility = View.INVISIBLE
                updateNavigationButtons()
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url ?: return false
                return !isWebUrl(url.scheme)
            }

            @Suppress("OVERRIDE_DEPRECATION")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                val scheme = url?.substringBefore(":", missingDelimiterValue = "")
                return !isWebUrl(scheme)
            }
        }

        openHomePage()
    }

    override fun onDestroy() {
        webView.webChromeClient = null
        webView.stopLoading()
        webView.loadUrl("about:blank")
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
            if (webView.canGoBack()) {
                webView.goBack()
            }
            updateNavigationButtons()
        }
        forwardButton.setOnClickListener {
            if (webView.canGoForward()) {
                webView.goForward()
            }
            updateNavigationButtons()
        }
        refreshButton.setOnClickListener { webView.reload() }
        homeButton.setOnClickListener { openHomePage() }
        menuButton.setOnClickListener {
            Toast.makeText(this, R.string.toast_settings_pending, Toast.LENGTH_SHORT).show()
        }

        updateNavigationButtons()
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
        getPreferences(Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SEARCH_PROVIDER, provider.id)
            .apply()
        updateSearchProviderSelection()
        if (shouldOpenProviderHome) {
            openHomePage()
        }
    }

    private fun loadSavedSearchProvider(): SearchProvider {
        val savedProviderId = getPreferences(Context.MODE_PRIVATE)
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
                    if (webView.canGoBack()) {
                        webView.goBack()
                        updateNavigationButtons()
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )
    }

    private fun loadAddressInput() {
        val input = addressInput.text?.toString()?.trim().orEmpty()
        if (input.isEmpty()) {
            return
        }
        loadUrl(resolveAddressInput(input))
    }

    private fun openHomePage() {
        loadUrl(selectedSearchProvider.homeUrl)
    }

    private fun loadUrl(url: String) {
        val isProviderHome = isProviderHomeUrl(url)
        updateAddressBar(url)
        hideKeyboard()
        showHomeContent(isProviderHome)
        webView.loadUrl(url)
    }

    private fun updateAddressBar(url: String?) {
        if (url.isNullOrBlank()) {
            return
        }

        if (addressInput.text?.toString() == url) {
            return
        }
        addressInput.setText(url)
        addressInput.setSelection(addressInput.text?.length ?: 0)
    }

    private fun updateNavigationButtons() {
        backButton.isEnabled = !isHomePageVisible && webView.canGoBack()
        forwardButton.isEnabled = !isHomePageVisible && webView.canGoForward()
    }

    private fun showHomeContent(show: Boolean) {
        isHomePageVisible = show
        webView.visibility = View.VISIBLE
        pageProgress.visibility = if (show) View.INVISIBLE else pageProgress.visibility
        updateNavigationButtons()
    }

    private fun hideKeyboard() {
        addressInput.clearFocus()
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(addressInput.windowToken, 0)
    }

    private fun resolveAddressInput(input: String): String {
        val value = input.trim()
        return when {
            value.startsWith("http://", ignoreCase = true) ||
                value.startsWith("https://", ignoreCase = true) ||
                value.startsWith("about:", ignoreCase = true) -> value

            looksLikeLocalAddress(value) || looksLikeIpAddress(value) -> "http://$value"
            looksLikeDomain(value) -> "https://$value"
            else -> {
                val encodedQuery = URLEncoder.encode(value, StandardCharsets.UTF_8.name())
                "${selectedSearchProvider.searchUrlPrefix}$encodedQuery"
            }
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun looksLikeLocalAddress(value: String): Boolean {
        if (value.hasWhitespace()) {
            return false
        }

        val lowerValue = value.lowercase()
        return lowerValue == "localhost" ||
            lowerValue.startsWith("localhost:") ||
            lowerValue.startsWith("localhost/") ||
            lowerValue.startsWith("10.0.2.2") ||
            lowerValue.startsWith("127.0.0.1")
    }

    private fun looksLikeIpAddress(value: String): Boolean {
        val parts = extractHost(value).split(".")
        return parts.size == 4 && parts.all { part ->
            part.toIntOrNull()?.let { it in 0..255 } == true
        }
    }

    private fun looksLikeDomain(value: String): Boolean {
        if (value.hasWhitespace()) {
            return false
        }

        val host = extractHost(value)
        return host.contains(".") &&
            host.any { it.isLetter() } &&
            host.split(".").all { it.isNotEmpty() }
    }

    private fun extractHost(value: String): String {
        return value
            .substringBefore("/")
            .substringBefore("?")
            .substringBefore("#")
            .substringBefore(":")
    }

    private fun String.hasWhitespace(): Boolean {
        return any { it.isWhitespace() }
    }

    private fun isWebUrl(scheme: String?): Boolean {
        return scheme.equals("http", ignoreCase = true) ||
            scheme.equals("https", ignoreCase = true) ||
            scheme.equals("about", ignoreCase = true)
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
        private const val KEY_SEARCH_PROVIDER = "search_provider"
    }
}
