package com.example.videobrowser.browser

import android.annotation.SuppressLint
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.webkit.WebView
import android.widget.EditText
import kotlin.math.abs

class BrowserControlsScrollController(
    private val webView: WebView,
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
        webView.setOnTouchListener { view, event ->
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
            false
        }

        webView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (isVideoFullscreenUiActive()) {
                return@setOnScrollChangeListener
            }
            if (isHomePageVisible() || addressInput.hasFocus()) {
                resetTracking()
                setControlsHidden(false)
                return@setOnScrollChangeListener
            }

            val deltaY = scrollY - oldScrollY
            if (scrollY <= dp(4)) {
                resetTracking()
                setControlsHidden(false)
                return@setOnScrollChangeListener
            }
            if (abs(deltaY) < dp(2)) {
                return@setOnScrollChangeListener
            }

            val direction = if (deltaY > 0) 1 else -1
            if (direction != scrollDirection) {
                scrollDirection = direction
                scrollDeltaY = 0
            }
            scrollDeltaY += deltaY

            val now = SystemClock.uptimeMillis()
            if (now - lastScrollChangeAt < BROWSER_CONTROLS_SCROLL_COOLDOWN_MS) {
                return@setOnScrollChangeListener
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
