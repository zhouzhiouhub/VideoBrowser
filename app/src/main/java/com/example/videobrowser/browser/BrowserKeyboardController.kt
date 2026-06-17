package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器输入法模块”。
 * MainActivity 只负责创建地址栏和地址建议控制器；本类负责在页面跳转或打开功能中心前隐藏软键盘。
 */
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.example.videobrowser.browser.search.AddressSuggestionController

/**
 * 浏览器软键盘控制器。
 *
 * @param context 参数类型为 `Context`，表示用于取得系统 InputMethodManager 的 Android 上下文。
 * @param addressInput 参数类型为 `EditText`，表示地址栏输入框，隐藏键盘前会清除它的焦点并读取窗口 token。
 * @param addressSuggestionController 参数类型为 `() -> AddressSuggestionController?`，表示返回地址建议控制器的函数；尚未初始化时返回 null。
 */
class BrowserKeyboardController(
    private val context: Context,
    private val addressInput: EditText,
    private val addressSuggestionController: () -> AddressSuggestionController?
) {
    /**
     * 隐藏地址建议面板和 Android 软键盘。
     *
     * @return 无返回值；地址建议控制器尚未初始化时只处理地址栏焦点和系统软键盘。
     */
    fun hideKeyboard() {
        addressSuggestionController()?.hide()
        addressInput.clearFocus()
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(addressInput.windowToken, 0)
    }
}
