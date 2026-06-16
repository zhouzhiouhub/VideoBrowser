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

    /**
     * 函数 `isShowingCustomView`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isShowingCustomView(): Boolean {
        return customView != null
    }

    /**
     * 函数 `isFullscreenModeActive`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isFullscreenModeActive(): Boolean {
        return fullscreenModeActive
    }

    /**
     * 函数 `isFullscreenLandscape`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isFullscreenLandscape(): Boolean {
        return fullscreenLandscape
    }

    /**
     * 函数 `toggleFullscreenOrientation`：封装 `toggle Fullscreen Orientation` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun toggleFullscreenOrientation(): Boolean {
        if (!fullscreenModeActive) {
            return fullscreenLandscape
        }

        fullscreenLandscape = !fullscreenLandscape
        applyFullscreenOrientation()
        return fullscreenLandscape
    }

    /**
     * 函数 `enterPageFullscreen`：封装 `enter Page Fullscreen` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun enterPageFullscreen() {
        enterFullscreenMode()
    }

    /**
     * 函数 `exitPageFullscreen`：封装 `exit Page Fullscreen` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun exitPageFullscreen() {
        if (customView == null) {
            exitFullscreenMode()
        }
    }

    /**
     * 函数 `onProgressChanged`：处理 `on Progress Changed` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param view 参数类型为 `WebView?`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param newProgress 参数类型为 `Int`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     */
    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        progressChanged(newProgress.coerceIn(0, 100))
    }

    /**
     * 函数 `onReceivedTitle`：处理 `on Received Title` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param view 参数类型为 `WebView?`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param title 参数类型为 `String?`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     */
    override fun onReceivedTitle(view: WebView?, title: String?) {
        titleReceived(title?.trim().orEmpty())
    }

    /**
     * 函数 `onShowFileChooser`：处理 `on Show File Chooser` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param webView 参数类型为 `WebView?`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param filePathCallback 参数类型为 `ValueCallback<Array<Uri>>?`，表示回调对象，异步操作完成后用它把结果通知回调用方。
     * @param fileChooserParams 参数类型为 `FileChooserParams?`，表示函数执行 `fileChooserParams` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {
        return fileChooserRequested(filePathCallback, fileChooserParams)
    }

    /**
     * 函数 `onPermissionRequest`：处理 `on Permission Request` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param request 参数类型为 `PermissionRequest?`，表示一次请求或响应，函数会检查它的内容并决定如何继续处理。
     */
    override fun onPermissionRequest(request: PermissionRequest?) {
        permissionRequested(request)
    }

    /**
     * 函数 `onPermissionRequestCanceled`：处理 `on Permission Request Canceled` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param request 参数类型为 `PermissionRequest?`，表示一次请求或响应，函数会检查它的内容并决定如何继续处理。
     */
    override fun onPermissionRequestCanceled(request: PermissionRequest?) {
        permissionRequestCanceled(request)
    }

    /**
     * 函数 `onGeolocationPermissionsShowPrompt`：处理 `on Geolocation Permissions Show Prompt` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param origin 参数类型为 `String?`，表示函数执行 `origin` 相关逻辑时需要读取或处理的输入。
     * @param callback 参数类型为 `GeolocationPermissions.Callback?`，表示回调对象，异步操作完成后用它把结果通知回调用方。
     */
    override fun onGeolocationPermissionsShowPrompt(
        origin: String?,
        callback: GeolocationPermissions.Callback?
    ) {
        geolocationPermissionRequested(origin, callback)
    }

    /**
     * 函数 `onGeolocationPermissionsHidePrompt`：处理 `on Geolocation Permissions Hide Prompt` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    override fun onGeolocationPermissionsHidePrompt() {
        geolocationPermissionHidden()
    }

    /**
     * 函数 `onCreateWindow`：处理 `on Create Window` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param view 参数类型为 `WebView?`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param isDialog 参数类型为 `Boolean`，表示函数执行 `isDialog` 相关逻辑时需要读取或处理的输入。
     * @param isUserGesture 参数类型为 `Boolean`，表示函数执行 `isUserGesture` 相关逻辑时需要读取或处理的输入。
     * @param resultMsg 参数类型为 `Message?`，表示函数执行 `resultMsg` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    override fun onCreateWindow(
        view: WebView?,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message?
    ): Boolean {
        return newWindowRequested(view, isDialog, isUserGesture, resultMsg)
    }

    /**
     * 函数 `onCloseWindow`：处理 `on Close Window` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param window 参数类型为 `WebView?`，表示函数执行 `window` 相关逻辑时需要读取或处理的输入。
     */
    override fun onCloseWindow(window: WebView?) {
        windowClosed(window)
    }

    /**
     * 函数 `onJsAlert`：处理 `on Js Alert` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param view 参数类型为 `WebView?`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param message 参数类型为 `String?`，表示函数执行 `message` 相关逻辑时需要读取或处理的输入。
     * @param result 参数类型为 `JsResult?`，表示函数执行 `result` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `onJsConfirm`：处理 `on Js Confirm` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param view 参数类型为 `WebView?`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param message 参数类型为 `String?`，表示函数执行 `message` 相关逻辑时需要读取或处理的输入。
     * @param result 参数类型为 `JsResult?`，表示函数执行 `result` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `onJsPrompt`：处理 `on Js Prompt` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param view 参数类型为 `WebView?`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param message 参数类型为 `String?`，表示函数执行 `message` 相关逻辑时需要读取或处理的输入。
     * @param defaultValue 参数类型为 `String?`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @param result 参数类型为 `JsPromptResult?`，表示函数执行 `result` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `onJsBeforeUnload`：处理 `on Js Before Unload` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param view 参数类型为 `WebView?`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param message 参数类型为 `String?`，表示函数执行 `message` 相关逻辑时需要读取或处理的输入。
     * @param result 参数类型为 `JsResult?`，表示函数执行 `result` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `onShowCustomView`：处理 `on Show Custom View` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param view 参数类型为 `View?`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param callback 参数类型为 `CustomViewCallback?`，表示回调对象，异步操作完成后用它把结果通知回调用方。
     */
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

    /**
     * 函数 `onHideCustomView`：处理 `on Hide Custom View` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    override fun onHideCustomView() {
        hideCustomView()
    }

    /**
     * 函数 `hideCustomView`：控制 `hide Custom View` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 函数 `canShowDialog`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun canShowDialog(): Boolean {
        return !activity.isFinishing && !activity.isDestroyed
    }

    /**
     * 函数 `javascriptDialogTitle`：封装 `javascript Dialog Title` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param view 参数类型为 `WebView?`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param fallbackTitleRes 参数类型为 `Int`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun javascriptDialogTitle(
        view: WebView?,
        url: String?,
        fallbackTitleRes: Int
    ): String {
        val origin = javascriptDialogOrigin(view?.url ?: url)
        return origin ?: activity.getString(fallbackTitleRes)
    }

    /**
     * 函数 `javascriptDialogOrigin`：封装 `javascript Dialog Origin` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun javascriptDialogOrigin(url: String?): String? {
        return runCatching { Uri.parse(url).host }
            .getOrNull()
            ?.takeIf { host -> host.isNotBlank() }
    }

    /**
     * 函数 `javascriptDialogMessage`：封装 `javascript Dialog Message` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param message 参数类型为 `String?`，表示函数执行 `message` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun javascriptDialogMessage(message: String?): String {
        return message
            ?.takeIf { value -> value.isNotBlank() }
            ?: activity.getString(R.string.dialog_javascript_message_empty)
    }

    /**
     * 函数 `enterFullscreenMode`：封装 `enter Fullscreen Mode` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 函数 `exitFullscreenMode`：封装 `exit Fullscreen Mode` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 函数 `applyFullscreenOrientation`：根据最新状态刷新 `apply Fullscreen Orientation` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun applyFullscreenOrientation() {
        activity.requestedOrientation = if (fullscreenLandscape) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        }
    }
}
