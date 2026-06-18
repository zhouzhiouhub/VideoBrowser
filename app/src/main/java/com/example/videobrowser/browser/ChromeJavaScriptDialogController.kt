package com.example.videobrowser.browser

import android.app.Activity
import android.net.Uri
import android.text.InputType
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.WebView
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.example.videobrowser.R

/**
 * WebChromeClient 的 JavaScript 弹窗控制器。
 *
 * ChromeClient 负责接收 WebView 回调，本类负责把 alert、confirm、prompt 和 beforeunload
 * 显示成原生对话框，并确保每个 JsResult 都会被 confirm 或 cancel。
 */
internal class ChromeJavaScriptDialogController(
    private val activity: Activity
) {
    fun showAlert(
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

    fun showConfirm(
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

    fun showPrompt(
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

    fun showBeforeUnload(
        view: WebView?,
        url: String?,
        @Suppress("UNUSED_PARAMETER") message: String?,
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
}
