package com.example.videobrowser.browser

import android.app.Activity
import android.net.Uri
import android.text.InputType
import android.view.View
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
        val jsResult = activeJavaScriptResult(result) ?: return result != null
        showJavaScriptDialog(
            title = javascriptDialogTitle(view, url, R.string.title_javascript_dialog),
            message = javascriptDialogMessage(message),
            positiveButtonRes = android.R.string.ok,
            onConfirmed = { jsResult.confirm() },
            onCanceled = { jsResult.cancel() }
        )
        return true
    }

    fun showConfirm(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        val jsResult = activeJavaScriptResult(result) ?: return result != null
        showJavaScriptDialog(
            title = javascriptDialogTitle(view, url, R.string.title_javascript_confirm),
            message = javascriptDialogMessage(message),
            positiveButtonRes = android.R.string.ok,
            negativeButtonRes = android.R.string.cancel,
            onConfirmed = { jsResult.confirm() },
            onCanceled = { jsResult.cancel() }
        )
        return true
    }

    fun showPrompt(
        view: WebView?,
        url: String?,
        message: String?,
        defaultValue: String?,
        result: JsPromptResult?
    ): Boolean {
        val jsResult = activeJavaScriptResult(result) ?: return result != null

        val input = EditText(activity).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
            setSingleLine(false)
            setText(defaultValue.orEmpty())
            selectAll()
        }
        showJavaScriptDialog(
            title = javascriptDialogTitle(view, url, R.string.title_javascript_prompt),
            message = javascriptDialogMessage(message),
            customView = input,
            positiveButtonRes = android.R.string.ok,
            negativeButtonRes = android.R.string.cancel,
            onConfirmed = { jsResult.confirm(input.text?.toString().orEmpty()) },
            onCanceled = { jsResult.cancel() }
        )
        return true
    }

    fun showBeforeUnload(
        view: WebView?,
        url: String?,
        @Suppress("UNUSED_PARAMETER") message: String?,
        result: JsResult?
    ): Boolean {
        val jsResult = activeJavaScriptResult(result) ?: return result != null
        showJavaScriptDialog(
            title = javascriptDialogTitle(view, url, R.string.title_javascript_before_unload),
            message = activity.getString(R.string.dialog_javascript_before_unload_message),
            positiveButtonRes = R.string.action_leave_page,
            negativeButtonRes = R.string.action_stay_on_page,
            onConfirmed = { jsResult.confirm() },
            onCanceled = { jsResult.cancel() }
        )
        return true
    }

    private fun <T : JsResult> activeJavaScriptResult(result: T?): T? {
        val jsResult = result ?: return null
        if (!canShowDialog()) {
            jsResult.cancel()
            return null
        }
        return jsResult
    }

    private fun showJavaScriptDialog(
        title: String,
        message: String,
        positiveButtonRes: Int,
        negativeButtonRes: Int? = null,
        customView: View? = null,
        onConfirmed: () -> Unit,
        onCanceled: () -> Unit
    ) {
        val builder = AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButtonRes) { _, _ -> onConfirmed() }
            .setOnCancelListener { onCanceled() }

        customView?.let { view -> builder.setView(view) }
        negativeButtonRes?.let { buttonRes ->
            builder.setNegativeButton(buttonRes) { _, _ -> onCanceled() }
        }
        builder.show()
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
