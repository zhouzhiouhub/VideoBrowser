package com.example.videobrowser

import android.content.Context
import android.graphics.Color
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
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
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebViewDatabase
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
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
        val searchUrlPrefix: String,
        val accentColor: Int
    )

    private data class SearchProviderViews(
        val item: LinearLayout,
        val badge: TextView,
        val label: TextView
    )

    private data class RecommendationItem(
        val title: String,
        val subtitle: String,
        val query: String
    )

    private lateinit var webView: WebView
    private lateinit var addressInput: EditText
    private lateinit var pageProgress: ProgressBar
    private lateinit var searchProviderList: LinearLayout
    private lateinit var recommendationScroll: ScrollView
    private lateinit var recommendationFeed: LinearLayout
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
            searchUrlPrefix = "https://quark.sm.cn/s?q=",
            accentColor = Color.parseColor("#2F6FED")
        ),
        SearchProvider(
            id = "uc",
            name = "神马搜索",
            badge = "神",
            searchUrlPrefix = "https://so.m.sm.cn/s?q=",
            accentColor = Color.parseColor("#F28C20")
        ),
        SearchProvider(
            id = "baidu",
            name = "百度",
            badge = "百",
            searchUrlPrefix = "https://www.baidu.com/s?wd=",
            accentColor = Color.parseColor("#315EFB")
        ),
        SearchProvider(
            id = "sogou",
            name = "搜狗",
            badge = "搜",
            searchUrlPrefix = "https://www.sogou.com/web?query=",
            accentColor = Color.parseColor("#13B56B")
        ),
        SearchProvider(
            id = "so",
            name = "360搜索",
            badge = "360",
            searchUrlPrefix = "https://www.so.com/s?q=",
            accentColor = Color.parseColor("#20A052")
        ),
        SearchProvider(
            id = "edge",
            name = "Bing",
            badge = "B",
            searchUrlPrefix = "https://www.bing.com/search?q=",
            accentColor = Color.parseColor("#12837A")
        )
    )
    private val searchProviderViews = mutableMapOf<String, SearchProviderViews>()
    private lateinit var selectedSearchProvider: SearchProvider
    private var isHomePageVisible = true

    private val homePage = "about:blank"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rootView = findViewById<View>(R.id.rootView)
        webView = findViewById(R.id.webView)
        addressInput = findViewById(R.id.addressInput)
        pageProgress = findViewById(R.id.pageProgress)
        searchProviderList = findViewById(R.id.searchProviderList)
        recommendationScroll = findViewById(R.id.recommendationScroll)
        recommendationFeed = findViewById(R.id.recommendationFeed)
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
        clearBrowsingData()

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(false)
        cookieManager.setAcceptThirdPartyCookies(webView, false)

        webView.clearCache(true)
        webView.clearFormData()
        webView.clearHistory()

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            cacheMode = WebSettings.LOAD_NO_CACHE
            setSupportMultipleWindows(false)
            setGeolocationEnabled(false)
            @Suppress("DEPRECATION")
            databaseEnabled = false
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
                updateAddressBar(url)
                if (url == homePage) {
                    showHomeContent(true)
                    updateRecommendationFeed()
                } else {
                    showHomeContent(false)
                }
                pageProgress.progress = 0
                pageProgress.visibility = if (url == homePage) View.INVISIBLE else View.VISIBLE
                updateNavigationButtons()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                updateAddressBar(url)
                if (url == homePage) {
                    showHomeContent(true)
                    updateRecommendationFeed()
                }
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
        webView.clearCache(true)
        webView.clearFormData()
        webView.clearHistory()
        clearBrowsingData()
        super.onDestroy()
    }

    private fun clearBrowsingData() {
        CookieManager.getInstance().apply {
            removeAllCookies(null)
            flush()
        }
        WebStorage.getInstance().deleteAllData()
        @Suppress("DEPRECATION")
        WebViewDatabase.getInstance(this).apply {
            clearFormData()
            clearHttpAuthUsernamePassword()
        }
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
        selectedSearchProvider = provider
        getPreferences(Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SEARCH_PROVIDER, provider.id)
            .apply()
        updateSearchProviderSelection()
        updateRecommendationFeed()
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

    private fun updateRecommendationFeed() {
        recommendationFeed.removeAllViews()

        val items = recommendationsFor(selectedSearchProvider)
        items.forEachIndexed { index, item ->
            recommendationFeed.addView(
                createRecommendationCard(item),
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    if (index > 0) {
                        topMargin = dp(10)
                    }
                }
            )
        }
    }

    private fun createRecommendationCard(item: RecommendationItem): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            isClickable = true
            isFocusable = true
            background = createRecommendationCardBackground()
            contentDescription = getString(R.string.action_open_recommendation, item.title)
            setPadding(dp(14), dp(12), dp(14), dp(12))
            setOnClickListener { loadRecommendation(item) }
        }

        val title = TextView(this).apply {
            ellipsize = TextUtils.TruncateAt.END
            includeFontPadding = false
            maxLines = 1
            text = item.title
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.browser_text))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTypeface(typeface, Typeface.BOLD)
        }
        card.addView(title)

        val subtitle = TextView(this).apply {
            ellipsize = TextUtils.TruncateAt.END
            includeFontPadding = false
            maxLines = 2
            text = item.subtitle
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.recommendation_subtitle))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        }
        card.addView(
            subtitle,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(6)
            }
        )

        return card
    }

    private fun createRecommendationCardBackground(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(ContextCompat.getColor(this@MainActivity, R.color.recommendation_card_background))
            setStroke(dp(1), ContextCompat.getColor(this@MainActivity, R.color.recommendation_card_stroke))
            cornerRadius = dp(8).toFloat()
        }
    }

    private fun loadRecommendation(item: RecommendationItem) {
        loadUrl(resolveAddressInput(item.query))
    }

    private fun recommendationsFor(provider: SearchProvider): List<RecommendationItem> {
        return when (provider.id) {
            "quark" -> listOf(
                RecommendationItem("AI 搜索", "资料整理、长问题、学习办公", "AI 搜索"),
                RecommendationItem("夸克网盘", "文件备份、资源查找、在线播放", "夸克网盘"),
                RecommendationItem("文档扫描", "扫描王、PDF、拍照转文字", "夸克 文档扫描"),
                RecommendationItem("视频播放", "视频翻译、字幕、倍速播放", "夸克 视频播放"),
                RecommendationItem("学习资料", "题库、论文、课程资料", "夸克 学习资料")
            )

            "uc" -> listOf(
                RecommendationItem("今日热搜", "热点资讯、社会新闻、娱乐动态", "今日热搜"),
                RecommendationItem("免费小说", "热门小说、完结榜、男频女频", "免费小说"),
                RecommendationItem("影视综艺", "电影、电视剧、综艺片单", "影视综艺"),
                RecommendationItem("游戏攻略", "手游攻略、礼包、更新公告", "游戏攻略"),
                RecommendationItem("本地生活", "天气、同城服务、出行信息", "本地生活")
            )

            "baidu" -> listOf(
                RecommendationItem("百度热搜", "全网热点、实时榜单", "百度热搜"),
                RecommendationItem("生活服务", "天气、快递、路线、办事查询", "生活服务"),
                RecommendationItem("百科问答", "百科知识、经验教程、问答", "百科问答"),
                RecommendationItem("小说视频", "热门小说、短剧、影视资源", "小说视频"),
                RecommendationItem("贴吧讨论", "兴趣社区、话题讨论", "贴吧讨论")
            )

            "sogou" -> listOf(
                RecommendationItem("微信热文", "公众号文章、热点解读", "微信热文"),
                RecommendationItem("搜狗问问", "生活问题、经验回答", "搜狗问问"),
                RecommendationItem("健康资讯", "常见病、用药、养生查询", "健康资讯"),
                RecommendationItem("图片素材", "表情包、壁纸、设计参考", "图片素材"),
                RecommendationItem("翻译学习", "英文翻译、词典、作文", "翻译学习")
            )

            "so" -> listOf(
                RecommendationItem("安全热点", "反诈、防护、风险提示", "安全热点"),
                RecommendationItem("360良医", "健康问答、疾病查询、用药参考", "360良医"),
                RecommendationItem("网址导航", "常用网站、工具入口、分类导航", "网址导航"),
                RecommendationItem("新闻资讯", "时政、社会、财经热点", "新闻资讯"),
                RecommendationItem("软件下载", "装机工具、常用软件、安全下载", "软件下载")
            )

            else -> listOf(
                RecommendationItem("科技趋势", "AI、数码、软件、开发动态", "technology news"),
                RecommendationItem("英文资料", "英文网页、文档、教程", "English learning resources"),
                RecommendationItem("开发文档", "Android、Kotlin、WebView 文档", "Android Kotlin WebView documentation"),
                RecommendationItem("图片素材", "图片、设计灵感、视觉参考", "image inspiration"),
                RecommendationItem("学术搜索", "论文、报告、研究资料", "academic search")
            )
        }
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
        updateAddressBar(homePage)
        hideKeyboard()
        showHomeContent(true)
        updateRecommendationFeed()
        webView.loadUrl(homePage)
    }

    private fun loadUrl(url: String) {
        updateAddressBar(url)
        hideKeyboard()
        showHomeContent(url == homePage)
        webView.loadUrl(url)
    }

    private fun updateAddressBar(url: String?) {
        if (url.isNullOrBlank()) {
            return
        }

        val displayUrl = if (url == homePage) "" else url
        if (addressInput.text?.toString() == displayUrl) {
            return
        }
        addressInput.setText(displayUrl)
        addressInput.setSelection(addressInput.text?.length ?: 0)
    }

    private fun updateNavigationButtons() {
        backButton.isEnabled = !isHomePageVisible && webView.canGoBack()
        forwardButton.isEnabled = !isHomePageVisible && webView.canGoForward()
    }

    private fun showHomeContent(show: Boolean) {
        isHomePageVisible = show
        recommendationScroll.visibility = if (show) View.VISIBLE else View.GONE
        webView.visibility = if (show) View.GONE else View.VISIBLE
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

    companion object {
        private const val KEY_SEARCH_PROVIDER = "search_provider"
    }
}
