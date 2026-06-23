package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 BrowserBackNavigationController 可以拆开理解为“Browser Back Navigation Controller”，
 * 表示它只负责浏览器返回键这一条流程。
 * 主要职责：统一处理系统返回键和底部返回按钮，包括功能中心返回、元素选择取消、全屏退出、
 * WebView 后退，以及连续两次返回退出应用。
 * 阅读顺序：先看构造参数知道它依赖谁，再看 handleBrowserBack() 的分支顺序。
 */
import android.os.SystemClock
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.utils.ShortToast
import com.example.videobrowser.video.WebViewVideoCommand

/**
 * 浏览器返回导航控制器。
 *
 * MainActivity 负责创建各个业务对象；这个控制器只接收回调并按固定顺序消费一次返回事件。
 *
 * @param activity 当前承载浏览器界面的 Activity，用于注册系统返回键回调、显示 Toast 和结束页面。
 * @param browserManager 返回当前浏览器管理器的函数，用于执行 WebView 后退和注入退出全屏脚本。
 * @param currentChromeClient 返回当前 ChromeClient 的函数；尚未初始化时可以返回 null。
 * @param handleFunctionCenterBack 尝试让功能中心消费返回键的函数，返回 true 表示事件已处理。
 * @param isElementPickerActive 判断元素选择器是否处于激活状态的函数。
 * @param cancelElementPicker 取消当前元素选择流程的函数，只会在元素选择器激活时调用。
 * @param updateNavigationButtons WebView 后退成功后刷新底部导航按钮状态的函数。
 */
class BrowserBackNavigationController(
    private val activity: AppCompatActivity,
    private val browserManager: () -> BrowserManager,
    private val currentChromeClient: () -> ChromeClient?,
    private val handleFunctionCenterBack: () -> Boolean,
    private val isElementPickerActive: () -> Boolean,
    private val cancelElementPicker: () -> Unit,
    private val updateNavigationButtons: () -> Unit
) {
    private var lastBackExitPromptElapsedRealtime = 0L

    /**
     * 注册 Android 系统返回键回调。
     *
     * @return 无返回值；调用后系统返回键会进入 handleBrowserBack() 的统一流程。
     */
    fun setupBackNavigation() {
        activity.onBackPressedDispatcher.addCallback(
            activity,
            object : OnBackPressedCallback(true) {
                /**
                 * 系统返回键被按下时调用。
                 *
                 * @return 无返回值；具体处理结果由 handleBrowserBack() 内部的分支决定。
                 */
                override fun handleOnBackPressed() {
                    handleBrowserBack()
                }
            }
        )
    }

    /**
     * 处理一次浏览器返回请求。
     *
     * 返回请求可能来自系统返回键，也可能来自底部返回按钮。分支顺序很重要：先关闭覆盖层，
     * 再处理网页全屏，最后才让 WebView 后退或触发二次返回退出。
     *
     * @return 无返回值；函数内部会执行最先匹配到的返回行为。
     */
    fun handleBrowserBack() {
        if (handleFunctionCenterBack()) {
            resetBackExitConfirmation()
            return
        }
        if (isElementPickerActive()) {
            cancelElementPicker()
            resetBackExitConfirmation()
            return
        }

        val chromeClient = currentChromeClient()
        if (chromeClient?.isShowingCustomView() == true) {
            chromeClient.hideCustomView()
            resetBackExitConfirmation()
            return
        }
        if (chromeClient?.isFullscreenModeActive() == true) {
            browserManager().evaluateJavascript(
                WebViewVideoCommand.ExitFullscreen.toJavascript()
            )
            chromeClient.exitPageFullscreen()
            resetBackExitConfirmation()
            return
        }
        if (browserManager().goBack()) {
            updateNavigationButtons()
            resetBackExitConfirmation()
            return
        }

        confirmExitOnSecondBack()
    }

    /**
     * 清除“再按一次退出”的等待状态。
     *
     * @return 无返回值；下一次返回键会重新显示退出提示。
     */
    fun resetBackExitConfirmation() {
        lastBackExitPromptElapsedRealtime = 0L
    }

    /**
     * 执行连续两次返回退出应用的确认逻辑。
     *
     * @return 无返回值；如果两次返回间隔足够短会结束 Activity，否则显示提示。
     */
    private fun confirmExitOnSecondBack() {
        val now = SystemClock.elapsedRealtime()
        if (lastBackExitPromptElapsedRealtime != 0L &&
            now - lastBackExitPromptElapsedRealtime <= BACK_EXIT_CONFIRM_WINDOW_MS
        ) {
            resetBackExitConfirmation()
            activity.finish()
            return
        }

        lastBackExitPromptElapsedRealtime = now
        ShortToast.show(activity, R.string.toast_press_back_again_to_exit)
    }

    private companion object {
        private const val BACK_EXIT_CONFIRM_WINDOW_MS = 2000L
    }
}
