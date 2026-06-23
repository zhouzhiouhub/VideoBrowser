package com.example.videobrowser.element

/**
 * 初学者阅读提示：
 * 这个文件属于“元素选择器模块”。
 * 文件名 ElementPickerController 可以拆开理解为“Element Picker Controller”，表示它只负责页面净化链路中的一个小职责。
 * 主要职责：让用户在网页上点选要屏蔽的元素，并把 CSS 选择器保存到站点设置。
 * 阅读顺序：先看数据类/策略类表达什么规则，再看控制器如何把规则接到 WebView 请求或页面脚本上。
 */
import android.os.SystemClock
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.utils.ConfirmationDialog
import com.example.videobrowser.utils.PageUnavailableToast
import com.example.videobrowser.utils.ShortToast

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

    /**
     * 函数 `start`：启动或加载 `start` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun start() {
        // 没有当前站点时不能保存站点级 selector；JS 注入关闭时也无法让网页进入选择模式。
        val host = currentSiteHost()
        if (host == null) {
            PageUnavailableToast.showNoPageUrl(activity)
            return
        }
        if (!isJsInjectionEnabled() || isCurrentSiteJsInjectionDisabled()) {
            ShortToast.show(activity, R.string.toast_element_picker_js_disabled)
            return
        }

        if (isActive) {
            finishSession()
        }
        isActive = true
        startedAt = SystemClock.elapsedRealtime()
        injectPageFeatures()
        browserManager().evaluateJavascript(START_ELEMENT_PICKER_SCRIPT)
        ShortToast.show(activity, R.string.toast_element_picker_started)
    }

    /**
     * 函数 `cancel`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun cancel() {
        if (!isActive) {
            return
        }
        dialog?.dismiss()
        finishSession()
        ShortToast.show(activity, R.string.toast_element_picker_cancelled)
    }

    /**
     * 函数 `handleCancelledFromPage`：处理 `handle Cancelled From Page` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun handleCancelledFromPage() {
        if (!isActive) {
            return
        }
        finishSession()
        ShortToast.show(activity, R.string.toast_element_picker_cancelled)
    }

    /**
     * 函数 `handlePickedElement`：处理 `handle Picked Element` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param selector 参数类型为 `String`，表示函数执行 `selector` 相关逻辑时需要读取或处理的输入。
     * @param description 参数类型为 `String`，表示函数执行 `description` 相关逻辑时需要读取或处理的输入。
     */
    fun handlePickedElement(selector: String, description: String) {
        // 网页回调可能晚到；如果会话超时或已取消，就不再保存 selector。
        if (!isSessionValid()) {
            finishSession()
            return
        }

        val host = currentSiteHost() ?: run {
            finishSession()
            PageUnavailableToast.showNoPageUrl(activity)
            return
        }
        showConfirmElementBlockDialog(host, selector, description)
    }

    /**
     * 函数 `clearState`：封装 `clear State` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun clearState() {
        isActive = false
        startedAt = 0L
        dialog?.dismiss()
        dialog = null
    }

    /**
     * 函数 `dispose`：封装 `dispose` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun dispose() {
        dialog?.dismiss()
        dialog = null
    }

    /**
     * 函数 `showConfirmElementBlockDialog`：控制 `show Confirm Element Block Dialog` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @param selector 参数类型为 `String`，表示函数执行 `selector` 相关逻辑时需要读取或处理的输入。
     * @param description 参数类型为 `String`，表示函数执行 `description` 相关逻辑时需要读取或处理的输入。
     */
    private fun showConfirmElementBlockDialog(host: String, selector: String, description: String) {
        val detail = listOf(description.trim(), selector.trim())
            .filter { value -> value.isNotBlank() }
            .distinct()
            .joinToString(separator = "\n")
        val activeDialog = ConfirmationDialog.create(
            activity = activity,
            titleRes = R.string.title_confirm_element_block,
            message = activity.getString(R.string.dialog_confirm_element_block_message, host, detail),
            positiveButtonRes = R.string.action_block_element,
            onCanceled = {
                cancel()
            }
        ) {
            savePickedElement(host, selector)
        }
        dialog?.dismiss()
        dialog = activeDialog
        activeDialog.setOnDismissListener {
            if (dialog === activeDialog) {
                dialog = null
            }
        }
        activeDialog.show()
    }

    /**
     * 函数 `savePickedElement`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @param selector 参数类型为 `String`，表示函数执行 `selector` 相关逻辑时需要读取或处理的输入。
     */
    private fun savePickedElement(host: String, selector: String) {
        val alreadySaved = settingsManager.hasUserElementHideSelectorForSite(host, selector)
        val saved = alreadySaved || settingsManager.addUserElementHideSelectorForSite(host, selector)
        finishSession()
        if (!saved) {
            ShortToast.show(activity, R.string.toast_element_picker_invalid)
            return
        }

        injectPageFeatures()
        ShortToast.show(
            activity,
            if (alreadySaved) {
                R.string.toast_element_picker_already_saved
            } else {
                R.string.toast_element_picker_saved
            }
        )
    }

    /**
     * 函数 `isSessionValid`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun isSessionValid(): Boolean {
        return isActive &&
            SystemClock.elapsedRealtime() - startedAt <= ELEMENT_PICKER_TIMEOUT_MS
    }

    /**
     * 函数 `finishSession`：封装 `finish Session` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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
