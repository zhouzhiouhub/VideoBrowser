package com.example.videobrowser.browser

import android.os.Build
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import java.util.Collections
import java.util.WeakHashMap

/**
 * WebView 设置控制器。
 *
 * BrowserManager 负责当前 WebView 的生命周期和导航，本类集中管理基础 WebSettings、
 * Cookie 策略、混合内容、文字缩放和页面查找结果监听。
 */
internal class BrowserWebViewSettingsController {
    private val configuredWebViews = Collections.newSetFromMap(WeakHashMap<WebView, Boolean>())
    private var findResultListener: ((Int, Int, Boolean) -> Unit)? = null
    private var privateBrowsingEnabled = false
    private var thirdPartyCookiesEnabled = true
    private var mixedContentBlocked = true
    private var textZoomPercent = 100

    val isPrivateBrowsingEnabled: Boolean
        get() = privateBrowsingEnabled

    fun setup(webView: WebView) {
        if (!configuredWebViews.add(webView)) {
            return
        }

        applyCookiePolicy(webView)
        applyFindResultListener(webView)

        webView.settings.apply {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = false
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            cacheMode = WebSettings.LOAD_DEFAULT
            loadWithOverviewMode = false
            useWideViewPort = false
            loadsImagesAutomatically = true
            blockNetworkImage = false
            textZoom = textZoomPercent
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            setSupportMultipleWindows(true)
            setGeolocationEnabled(true)
            allowFileAccess = false
            allowContentAccess = true
            @Suppress("DEPRECATION")
            allowFileAccessFromFileURLs = false
            @Suppress("DEPRECATION")
            allowUniversalAccessFromFileURLs = false
            applyMixedContentMode(webView)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                safeBrowsingEnabled = true
            }
            @Suppress("DEPRECATION")
            databaseEnabled = true
            @Suppress("DEPRECATION")
            saveFormData = false
        }
    }

    fun setFindResultListener(listener: ((Int, Int, Boolean) -> Unit)?) {
        findResultListener = listener
        configuredWebViews.forEach(::applyFindResultListener)
    }

    fun setPrivateBrowsingEnabled(enabled: Boolean, activeWebView: WebView) {
        privateBrowsingEnabled = enabled
        configuredWebViews.forEach(::applyCookiePolicy)
        activeWebView.settings.domStorageEnabled = !enabled
        @Suppress("DEPRECATION")
        activeWebView.settings.databaseEnabled = !enabled
        activeWebView.settings.cacheMode = if (enabled) {
            WebSettings.LOAD_NO_CACHE
        } else {
            WebSettings.LOAD_DEFAULT
        }
    }

    fun setThirdPartyCookiesEnabled(enabled: Boolean) {
        thirdPartyCookiesEnabled = enabled
        configuredWebViews.forEach(::applyCookiePolicy)
    }

    fun setMixedContentBlocked(blocked: Boolean) {
        mixedContentBlocked = blocked
        configuredWebViews.forEach(::applyMixedContentMode)
    }

    fun setTextZoomPercent(percent: Int) {
        textZoomPercent = percent
        configuredWebViews.forEach(::applyTextZoom)
    }

    fun applyDesktopMode(
        webView: WebView,
        enabled: Boolean,
        desktopUserAgent: String,
        defaultUserAgent: String?
    ) {
        webView.settings.userAgentString = if (enabled) {
            desktopUserAgent
        } else {
            defaultUserAgent
        }
        webView.settings.useWideViewPort = enabled
        webView.settings.loadWithOverviewMode = enabled
    }

    fun forget(webView: WebView) {
        configuredWebViews.remove(webView)
    }

    private fun applyCookiePolicy(targetWebView: WebView) {
        CookieManager.getInstance().apply {
            setAcceptCookie(!privateBrowsingEnabled)
            setAcceptThirdPartyCookies(
                targetWebView,
                !privateBrowsingEnabled && thirdPartyCookiesEnabled
            )
        }
    }

    private fun applyMixedContentMode(targetWebView: WebView) {
        targetWebView.settings.mixedContentMode = if (mixedContentBlocked) {
            WebSettings.MIXED_CONTENT_NEVER_ALLOW
        } else {
            WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }
    }

    private fun applyTextZoom(targetWebView: WebView) {
        targetWebView.settings.textZoom = textZoomPercent
    }

    private fun applyFindResultListener(targetWebView: WebView) {
        targetWebView.setFindListener(
            findResultListener?.let { listener ->
                WebView.FindListener { activeMatchOrdinal, numberOfMatches, isDoneCounting ->
                    listener(activeMatchOrdinal, numberOfMatches, isDoneCounting)
                }
            }
        )
    }
}
