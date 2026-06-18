package com.example.videobrowser.browser

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient.CustomViewCallback
import android.widget.FrameLayout

/**
 * WebChromeClient 的全屏状态控制器。
 *
 * 本类集中管理网页自定义全屏 View、页面 requestFullscreen 状态、横竖屏切换和系统栏标记。
 */
internal class ChromeFullscreenController(
    private val activity: Activity,
    private val fullscreenContainer: FrameLayout,
    private val decorView: View,
    private val fullscreenChanged: (Boolean) -> Unit
) {
    private var customView: View? = null
    private var customViewCallback: CustomViewCallback? = null
    private var previousOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    private var previousSystemUiVisibility = 0
    private var fullscreenModeActive = false
    private var fullscreenLandscape = true

    fun isShowingCustomView(): Boolean {
        return customView != null
    }

    fun isFullscreenModeActive(): Boolean {
        return fullscreenModeActive
    }

    fun isFullscreenLandscape(): Boolean {
        return fullscreenLandscape
    }

    fun toggleFullscreenOrientation(): Boolean {
        if (!fullscreenModeActive) {
            return fullscreenLandscape
        }

        fullscreenLandscape = !fullscreenLandscape
        applyFullscreenOrientation()
        return fullscreenLandscape
    }

    fun enterPageFullscreen() {
        enterFullscreenMode()
    }

    fun exitPageFullscreen() {
        if (customView == null) {
            exitFullscreenMode()
        }
    }

    fun showCustomView(view: View?, callback: CustomViewCallback?) {
        if (view == null) {
            callback?.onCustomViewHidden()
            return
        }
        // WebView 播放器进入全屏时会给一个自定义 View。
        // App 把它移到 fullscreenContainer，让普通浏览器控件暂时让位。
        if (customView != null) {
            callback?.onCustomViewHidden()
            return
        }

        customView = view
        customViewCallback = callback
        enterFullscreenMode()
        (view.parent as? ViewGroup)?.removeView(view)
        view.keepScreenOn = true
        fullscreenContainer.removeAllViews()
        fullscreenContainer.addView(
            view,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        fullscreenContainer.visibility = View.VISIBLE
        fullscreenContainer.bringToFront()
        fullscreenContainer.requestFocus()
    }

    fun hideCustomView() {
        val view = customView ?: return
        val callback = customViewCallback
        customView = null
        customViewCallback = null

        view.keepScreenOn = false
        if (view.parent == fullscreenContainer) {
            fullscreenContainer.removeView(view)
        } else {
            fullscreenContainer.removeAllViews()
        }
        fullscreenContainer.visibility = View.GONE
        fullscreenContainer.clearFocus()
        exitFullscreenMode()
        callback?.onCustomViewHidden()
    }

    private fun enterFullscreenMode() {
        if (!fullscreenModeActive) {
            fullscreenModeActive = true
            previousOrientation = activity.requestedOrientation
            previousSystemUiVisibility = decorView.systemUiVisibility
            fullscreenLandscape = true
        }

        applyFullscreenOrientation()
        decorView.systemUiVisibility =
            previousSystemUiVisibility or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        fullscreenChanged(true)
    }

    private fun exitFullscreenMode() {
        if (!fullscreenModeActive) {
            return
        }

        activity.requestedOrientation = previousOrientation
        decorView.systemUiVisibility = previousSystemUiVisibility
        previousOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        previousSystemUiVisibility = 0
        fullscreenModeActive = false
        fullscreenLandscape = true
        fullscreenChanged(false)
    }

    private fun applyFullscreenOrientation() {
        activity.requestedOrientation = if (fullscreenLandscape) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        }
    }
}
