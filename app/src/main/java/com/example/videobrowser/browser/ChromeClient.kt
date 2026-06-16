package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 ChromeClient 可以拆开理解为“Chrome Client”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：封装 WebView 页面加载、标签页、导航安全、页面工具、权限回调或浏览器控件状态。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Message
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.webkit.GeolocationPermissions
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.ValueCallback
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebChromeClient.CustomViewCallback
import android.webkit.WebChromeClient.FileChooserParams
import android.webkit.WebView
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import com.example.videobrowser.R

/**
 * WebChromeClient 的应用适配层。
 *
 * WebChromeClient 处理的是“浏览器外壳能力”：网页标题、加载进度、文件选择、网页权限、
 * JavaScript 弹窗、新窗口和全屏视频。这个类把这些 Android 回调转成构造参数里的函数，
 * MainActivity 再决定真正的业务处理方式。
 */
class ChromeClient(
    private val activity: Activity,
    private val fullscreenContainer: FrameLayout,
    private val decorView: View,
    private val progressChanged: (Int) -> Unit = {},
    private val titleReceived: (String) -> Unit = {},
    private val fullscreenChanged: (Boolean) -> Unit = {},
    private val fileChooserRequested: (ValueCallback<Array<Uri>>?, FileChooserParams?) -> Boolean =
        { _, _ -> false },
    private val permissionRequested: (PermissionRequest?) -> Unit = {},
    private val permissionRequestCanceled: (PermissionRequest?) -> Unit = {},
    private val geolocationPermissionRequested: (String?, GeolocationPermissions.Callback?) -> Unit =
        { _, _ -> },
    private val geolocationPermissionHidden: () -> Unit = {},
    private val newWindowRequested: (WebView?, Boolean, Boolean, Message?) -> Boolean =
        { _, _, _, _ -> false },
    private val windowClosed: (WebView?) -> Unit = {}
) : WebChromeClient() {
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

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        progressChanged(newProgress.coerceIn(0, 100))
    }

    override fun onReceivedTitle(view: WebView?, title: String?) {
        titleReceived(title?.trim().orEmpty())
    }

    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {
        return fileChooserRequested(filePathCallback, fileChooserParams)
    }

    override fun onPermissionRequest(request: PermissionRequest?) {
        permissionRequested(request)
    }

    override fun onPermissionRequestCanceled(request: PermissionRequest?) {
        permissionRequestCanceled(request)
    }

    override fun onGeolocationPermissionsShowPrompt(
        origin: String?,
        callback: GeolocationPermissions.Callback?
    ) {
        geolocationPermissionRequested(origin, callback)
    }

    override fun onGeolocationPermissionsHidePrompt() {
        geolocationPermissionHidden()
    }

    override fun onCreateWindow(
        view: WebView?,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message?
    ): Boolean {
        return newWindowRequested(view, isDialog, isUserGesture, resultMsg)
    }

    override fun onCloseWindow(window: WebView?) {
        windowClosed(window)
    }

    override fun onJsAlert(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        // 网页弹窗必须调用 confirm/cancel 结束，否则网页里的 JavaScript 会一直等待。
        val jsResult = result ?: return false
        if (!canShowDialog()) {
            jsResult.cancel()
            return true
        }

        AlertDialog.Builder(activity)
            .setTitle(javascriptDialogTitle(view, url, R.string.title_javascript_dialog))
            .setMessage(javascriptDialogMessage(message))
            .setPositiveButton(android.R.string.ok) { _, _ -> jsResult.confirm() }
            .setOnCancelListener { jsResult.cancel() }
            .show()
        return true
    }

    override fun onJsConfirm(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        val jsResult = result ?: return false
        if (!canShowDialog()) {
            jsResult.cancel()
            return true
        }

        AlertDialog.Builder(activity)
            .setTitle(javascriptDialogTitle(view, url, R.string.title_javascript_confirm))
            .setMessage(javascriptDialogMessage(message))
            .setPositiveButton(android.R.string.ok) { _, _ -> jsResult.confirm() }
            .setNegativeButton(android.R.string.cancel) { _, _ -> jsResult.cancel() }
            .setOnCancelListener { jsResult.cancel() }
            .show()
        return true
    }

    override fun onJsPrompt(
        view: WebView?,
        url: String?,
        message: String?,
        defaultValue: String?,
        result: JsPromptResult?
    ): Boolean {
        val jsResult = result ?: return false
        if (!canShowDialog()) {
            jsResult.cancel()
            return true
        }

        val input = EditText(activity).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
            setSingleLine(false)
            setText(defaultValue.orEmpty())
            selectAll()
        }
        AlertDialog.Builder(activity)
            .setTitle(javascriptDialogTitle(view, url, R.string.title_javascript_prompt))
            .setMessage(javascriptDialogMessage(message))
            .setView(input)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                jsResult.confirm(input.text?.toString().orEmpty())
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> jsResult.cancel() }
            .setOnCancelListener { jsResult.cancel() }
            .show()
        return true
    }

    override fun onJsBeforeUnload(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        val jsResult = result ?: return false
        if (!canShowDialog()) {
            jsResult.cancel()
            return true
        }

        AlertDialog.Builder(activity)
            .setTitle(javascriptDialogTitle(view, url, R.string.title_javascript_before_unload))
            .setMessage(R.string.dialog_javascript_before_unload_message)
            .setPositiveButton(R.string.action_leave_page) { _, _ -> jsResult.confirm() }
            .setNegativeButton(R.string.action_stay_on_page) { _, _ -> jsResult.cancel() }
            .setOnCancelListener { jsResult.cancel() }
            .show()
        return true
    }

    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
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

    override fun onHideCustomView() {
        hideCustomView()
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

    private fun canShowDialog(): Boolean {
        return !activity.isFinishing && !activity.isDestroyed
    }

    private fun javascriptDialogTitle(
        view: WebView?,
        url: String?,
        fallbackTitleRes: Int
    ): String {
        val origin = javascriptDialogOrigin(view?.url ?: url)
        return origin ?: activity.getString(fallbackTitleRes)
    }

    private fun javascriptDialogOrigin(url: String?): String? {
        return runCatching { Uri.parse(url).host }
            .getOrNull()
            ?.takeIf { host -> host.isNotBlank() }
    }

    private fun javascriptDialogMessage(message: String?): String {
        return message
            ?.takeIf { value -> value.isNotBlank() }
            ?: activity.getString(R.string.dialog_javascript_message_empty)
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
