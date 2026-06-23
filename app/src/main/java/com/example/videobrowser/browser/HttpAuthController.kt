package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“HTTP 认证弹窗模块”。
 * 文件名 HttpAuthController 可以拆开理解为“HTTP Auth Controller”，表示它专门负责 WebView 遇到 HTTP Basic Auth 时的用户名/密码弹窗。
 * 主要职责：只保留一个待处理认证弹窗，把用户输入交给 HttpAuthHandler.proceed，或在取消/销毁时调用 cancel。
 * 阅读顺序：先看 handleRequest，再看 cancelPending。
 */
import android.text.InputType
import android.webkit.HttpAuthHandler
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.utils.TextInputDialogField
import com.example.videobrowser.utils.TwoTextInputDialog

/**
 * HTTP Basic Auth 弹窗控制器。
 *
 * MainActivity 只负责把 BrowserClient 的 HTTP 认证回调委托给本类；本类负责 pending handler 和对话框生命周期。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示当前屏幕宿主，用来创建对话框、输入框并读取字符串资源。
 * @param dp 参数类型为 `(Int) -> Int`，表示把 dp 数值转换为像素的回调，用来设置认证表单边距。
 */
class HttpAuthController(
    private val activity: AppCompatActivity,
    private val dp: (Int) -> Int
) {
    private var pendingHandler: HttpAuthHandler? = null
    private var pendingDialog: AlertDialog? = null

    /**
     * 函数 `handleRequest`：展示 HTTP 认证弹窗并保存待处理 handler。
     *
     * 初学者阅读提示：新的认证请求到来时会先取消旧请求，确保同一时间只有一个 HTTP Basic Auth 弹窗。
     *
     * @param handler 参数类型为 `HttpAuthHandler?`，表示 WebView 等待认证结果的回调；为空时直接忽略。
     * @param host 参数类型为 `String?`，表示请求认证的主机名，用于展示给用户确认。
     * @param realm 参数类型为 `String?`，表示服务端提供的认证域，可为空。
     */
    fun handleRequest(
        handler: HttpAuthHandler?,
        host: String?,
        realm: String?
    ) {
        val authHandler = handler ?: return
        cancelPending()
        pendingHandler = authHandler
        val displayHost = host?.takeIf { it.isNotBlank() }
            ?: activity.getString(R.string.permission_origin_unknown)
        val displayRealm = realm?.takeIf { it.isNotBlank() }
        val message = displayRealm?.let { value ->
            activity.getString(R.string.dialog_http_auth_request_message_with_realm, displayHost, value)
        } ?: activity.getString(R.string.dialog_http_auth_request_message, displayHost)
        var completed = false
        val dialog = TwoTextInputDialog.create(
            activity = activity,
            titleRes = R.string.title_http_auth_request,
            message = message,
            firstField = TextInputDialogField(
                hintRes = R.string.hint_http_auth_username,
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
            ),
            secondField = TextInputDialogField(
                hintRes = R.string.hint_http_auth_password,
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            ),
            positiveButtonRes = R.string.action_http_auth_sign_in,
            dp = dp
        ) { values ->
            completed = true
            pendingHandler = null
            pendingDialog = null
            authHandler.proceed(
                values.first,
                values.second
            )
            true
        }
        dialog.setOnDismissListener {
            if (!completed) {
                completed = true
                if (pendingHandler == authHandler) {
                    pendingHandler = null
                }
                if (pendingDialog == dialog) {
                    pendingDialog = null
                }
                authHandler.cancel()
            }
        }
        pendingDialog = dialog
        dialog.show()
    }

    /**
     * 函数 `cancelPending`：取消当前 HTTP 认证弹窗和待处理 handler。
     *
     * 初学者阅读提示：Activity 销毁或新认证请求到来时调用，避免旧 handler 一直等待认证结果。
     */
    fun cancelPending() {
        val dialog = pendingDialog
        val handler = pendingHandler
        pendingDialog = null
        pendingHandler = null
        dialog?.setOnDismissListener(null)
        dialog?.dismiss()
        handler?.cancel()
    }

}
