package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 BrowserControlsScrollController 可以拆开理解为“Browser Controls Scroll Controller”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：封装 WebView 页面加载、标签页、导航安全、页面工具、权限回调或浏览器控件状态。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
import android.annotation.SuppressLint
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.webkit.WebView
import android.widget.EditText
import kotlin.math.abs

class BrowserControlsScrollController(
    private var webView: WebView,
    private val addressInput: EditText,
    private val dp: (Int) -> Int,
    private val areControlsHidden: () -> Boolean,
    private val isHomePageVisible: () -> Boolean,
    private val isVideoFullscreenUiActive: () -> Boolean,
    private val applyControlsHidden: (Boolean) -> Unit,
    private val updatePageProgressVisibility: (forceHidden: Boolean) -> Unit
) {
    private var scrollDeltaY = 0
    private var scrollDirection = 0
    private var lastScrollChangeAt = 0L
    private var isTouchActive = false
    private var pendingControlsHidden: Boolean? = null

    @SuppressLint("ClickableViewAccessibility")
    fun setup() {
        attachToWebView(webView)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun attachToWebView(nextWebView: WebView) {
        if (webView !== nextWebView) {
            webView.setOnTouchListener(null)
            webView.setOnScrollChangeListener(null)
            webView = nextWebView
        }
        resetTracking()
        isTouchActive = false
        pendingControlsHidden = null

        webView.setOnTouchListener { view, event -> handleTouch(view, event) }
        webView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            handleScroll(scrollY, oldScrollY)
        }
    }

    fun resetTracking(changeAt: Long = lastScrollChangeAt) {
        scrollDeltaY = 0
        scrollDirection = 0
        lastScrollChangeAt = changeAt
    }

    fun setControlsHidden(hidden: Boolean, allowDefer: Boolean = true) {
        val shouldHide = hidden || isVideoFullscreenUiActive()
        if (allowDefer &&
            isTouchActive &&
            !isVideoFullscreenUiActive() &&
            areControlsHidden() != shouldHide
        ) {
            pendingControlsHidden = shouldHide
            return
        }

        if (areControlsHidden() == shouldHide) {
            pendingControlsHidden = null
            applyControlsHidden(shouldHide)
            return
        }

        applyControlsHidden(shouldHide)
        updatePageProgressVisibility(shouldHide)
    }

    private fun handleTouch(view: View, event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isTouchActive = true
                pendingControlsHidden = null
                view.parent?.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isTouchActive = false
                view.parent?.requestDisallowInterceptTouchEvent(false)
                applyPendingControlsAfterTouch(view)
            }
        }
        return false
    }

    private fun handleScroll(scrollY: Int, oldScrollY: Int) {
        if (isVideoFullscreenUiActive()) {
            return
        }
        if (isHomePageVisible() || addressInput.hasFocus()) {
            resetTracking()
            setControlsHidden(false)
            return
        }

        val deltaY = scrollY - oldScrollY
        if (scrollY <= dp(4)) {
            resetTracking()
            setControlsHidden(false)
            return
        }
        if (abs(deltaY) < dp(2)) {
            return
        }

        val direction = if (deltaY > 0) 1 else -1
        if (direction != scrollDirection) {
            scrollDirection = direction
            scrollDeltaY = 0
        }
        scrollDeltaY += deltaY

        val now = SystemClock.uptimeMillis()
        if (now - lastScrollChangeAt < BROWSER_CONTROLS_SCROLL_COOLDOWN_MS) {
            return
        }

        when {
            scrollDeltaY >= dp(BROWSER_CONTROLS_SCROLL_THRESHOLD_DP) -> {
                resetTracking(now)
                setControlsHidden(true)
            }
            scrollDeltaY <= -dp(BROWSER_CONTROLS_SCROLL_THRESHOLD_DP) -> {
                resetTracking(now)
                setControlsHidden(false)
            }
        }
    }

    private fun applyPendingControlsAfterTouch(view: View) {
        val pendingHidden = pendingControlsHidden ?: return
        pendingControlsHidden = null
        view.post {
            if (isTouchActive) {
                pendingControlsHidden = pendingHidden
            } else {
                setControlsHidden(pendingHidden, allowDefer = false)
            }
        }
    }

    private companion object {
        private const val BROWSER_CONTROLS_SCROLL_THRESHOLD_DP = 48
        private const val BROWSER_CONTROLS_SCROLL_COOLDOWN_MS = 500L
    }
}
