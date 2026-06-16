package com.example.videobrowser.element

/**
 * 初学者阅读提示：
 * 这个文件属于“元素选择器模块”。
 * 文件名 ElementPickerController 可以拆开理解为“Element Picker Controller”，表示它只负责页面净化链路中的一个小职责。
 * 主要职责：让用户在网页上点选要屏蔽的元素，并把 CSS 选择器保存到站点设置。
 * 阅读顺序：先看数据类/策略类表达什么规则，再看控制器如何把规则接到 WebView 请求或页面脚本上。
 */
import android.os.SystemClock
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.settings.SettingsManager

/**
 * 用户手动屏蔽网页元素的控制器。
 *
 * 流程是：开始选择 -> 注入选择脚本 -> 网页回传 CSS selector -> 用户确认 -> 保存到当前站点设置。
 * 下一次页面注入时，PageFeatureCoordinator 会把这些 selector 传给 JsInjector。
 */
class ElementPickerController(
    private val activity: AppCompatActivity,
    private val browserManager: () -> BrowserManager,
    private val settingsManager: SettingsManager,
    private val currentSiteHost: () -> String?,
    private val isJsInjectionEnabled: () -> Boolean,
    private val isCurrentSiteJsInjectionDisabled: () -> Boolean,
    private val injectPageFeatures: () -> Unit
) {
    private var startedAt = 0L
    private var dialog: AlertDialog? = null

    var isActive = false
        private set

    fun start() {
        // 没有当前站点时不能保存站点级 selector；JS 注入关闭时也无法让网页进入选择模式。
        val host = currentSiteHost()
        if (host == null) {
            Toast.makeText(activity, R.string.toast_no_page_url, Toast.LENGTH_SHORT).show()
            return
        }
        if (!isJsInjectionEnabled() || isCurrentSiteJsInjectionDisabled()) {
            Toast.makeText(
                activity,
                R.string.toast_element_picker_js_disabled,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (isActive) {
            finishSession()
        }
        isActive = true
        startedAt = SystemClock.elapsedRealtime()
        injectPageFeatures()
        browserManager().evaluateJavascript(START_ELEMENT_PICKER_SCRIPT)
        Toast.makeText(activity, R.string.toast_element_picker_started, Toast.LENGTH_SHORT).show()
    }

    fun cancel() {
        if (!isActive) {
            return
        }
        dialog?.dismiss()
        finishSession()
        Toast.makeText(activity, R.string.toast_element_picker_cancelled, Toast.LENGTH_SHORT).show()
    }

    fun handleCancelledFromPage() {
        if (!isActive) {
            return
        }
        finishSession()
        Toast.makeText(activity, R.string.toast_element_picker_cancelled, Toast.LENGTH_SHORT).show()
    }

    fun handlePickedElement(selector: String, description: String) {
        // 网页回调可能晚到；如果会话超时或已取消，就不再保存 selector。
        if (!isSessionValid()) {
            finishSession()
            return
        }

        val host = currentSiteHost() ?: run {
            finishSession()
            Toast.makeText(activity, R.string.toast_no_page_url, Toast.LENGTH_SHORT).show()
            return
        }
        showConfirmElementBlockDialog(host, selector, description)
    }

    fun clearState() {
        isActive = false
        startedAt = 0L
        dialog?.dismiss()
        dialog = null
    }

    fun dispose() {
        dialog?.dismiss()
        dialog = null
    }

    private fun showConfirmElementBlockDialog(host: String, selector: String, description: String) {
        val detail = listOf(description.trim(), selector.trim())
            .filter { value -> value.isNotBlank() }
            .distinct()
            .joinToString(separator = "\n")
        val activeDialog = AlertDialog.Builder(activity)
            .setTitle(R.string.title_confirm_element_block)
            .setMessage(activity.getString(R.string.dialog_confirm_element_block_message, host, detail))
            .setPositiveButton(R.string.action_block_element) { _, _ ->
                savePickedElement(host, selector)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                cancel()
            }
            .create()
        dialog?.dismiss()
        dialog = activeDialog
        activeDialog.setOnCancelListener {
            cancel()
        }
        activeDialog.setOnDismissListener {
            if (dialog === activeDialog) {
                dialog = null
            }
        }
        activeDialog.show()
    }

    private fun savePickedElement(host: String, selector: String) {
        val alreadySaved = settingsManager.hasUserElementHideSelectorForSite(host, selector)
        val saved = alreadySaved || settingsManager.addUserElementHideSelectorForSite(host, selector)
        finishSession()
        if (!saved) {
            Toast.makeText(activity, R.string.toast_element_picker_invalid, Toast.LENGTH_SHORT).show()
            return
        }

        injectPageFeatures()
        Toast.makeText(
            activity,
            if (alreadySaved) {
                R.string.toast_element_picker_already_saved
            } else {
                R.string.toast_element_picker_saved
            },
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun isSessionValid(): Boolean {
        return isActive &&
            SystemClock.elapsedRealtime() - startedAt <= ELEMENT_PICKER_TIMEOUT_MS
    }

    private fun finishSession() {
        clearState()
        browserManager().evaluateJavascript(FINISH_ELEMENT_PICKER_SCRIPT)
    }

    private companion object {
        private const val START_ELEMENT_PICKER_SCRIPT =
            "if(window.VideoBrowserEnhancer&&typeof window.VideoBrowserEnhancer.startElementPicker==='function'){" +
                "window.VideoBrowserEnhancer.startElementPicker();" +
                "}"
        private const val FINISH_ELEMENT_PICKER_SCRIPT =
            "if(window.VideoBrowserEnhancer&&typeof window.VideoBrowserEnhancer.finishElementPicker==='function'){" +
                "window.VideoBrowserEnhancer.finishElementPicker();" +
                "}"
        private const val ELEMENT_PICKER_TIMEOUT_MS = 60_000L
    }
}
