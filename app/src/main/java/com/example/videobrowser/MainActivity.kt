package com.example.videobrowser

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var addressInput: EditText
    private lateinit var pageProgress: ProgressBar
    private lateinit var backButton: ImageButton
    private lateinit var forwardButton: ImageButton
    private lateinit var refreshButton: ImageButton
    private lateinit var homeButton: ImageButton
    private lateinit var menuButton: ImageButton
    private lateinit var loadButton: ImageButton

    private val homePage = "https://www.baidu.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rootView = findViewById<View>(R.id.rootView)
        webView = findViewById(R.id.webView)
        addressInput = findViewById(R.id.addressInput)
        pageProgress = findViewById(R.id.pageProgress)
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
                pageProgress.progress = 0
                pageProgress.visibility = View.VISIBLE
                updateNavigationButtons()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                updateAddressBar(url)
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

        loadUrl(homePage)
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
    }

    private fun setupBrowserControls() {
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
        homeButton.setOnClickListener { loadUrl(homePage) }
        menuButton.setOnClickListener {
            Toast.makeText(this, R.string.toast_settings_pending, Toast.LENGTH_SHORT).show()
        }

        updateNavigationButtons()
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

    private fun loadUrl(url: String) {
        updateAddressBar(url)
        hideKeyboard()
        webView.loadUrl(url)
    }

    private fun updateAddressBar(url: String?) {
        if (url.isNullOrBlank() || addressInput.text?.toString() == url) {
            return
        }
        addressInput.setText(url)
        addressInput.setSelection(addressInput.text?.length ?: 0)
    }

    private fun updateNavigationButtons() {
        backButton.isEnabled = webView.canGoBack()
        forwardButton.isEnabled = webView.canGoForward()
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
                "$DEFAULT_SEARCH_URL$encodedQuery"
            }
        }
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
        private const val DEFAULT_SEARCH_URL = "https://www.baidu.com/s?wd="
    }
}
