package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“页面内查找 UI 模块”。
 * 文件名 FindInPageDialogController 可以拆开理解为“Find In Page Dialog Controller”，表示它专门负责页面内查找的弹窗和按钮交互。
 * 主要职责：创建查找输入框、绑定查找/下一处/上一处按钮、显示匹配数量，并在弹窗关闭时清理 WebView 查找状态。
 * 阅读顺序：先看 showDialog，再看 statusText，最后看 showEmptyQueryToast。
 */
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.utils.ShortToast

/**
 * 页面内查找弹窗控制器。
 *
 * MainActivity 只负责把功能中心入口委托给本类；本类负责弹窗 UI 和 FindInPageController 之间的事件连接。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示当前屏幕宿主，用来创建 Android 对话框、输入框、文本视图和 Toast。
 * @param findInPageController 参数类型为 `FindInPageController`，表示页面内查找业务控制器，用来执行搜索、下一处、上一处和清理匹配。
 * @param setFindResultListener 参数类型为 `(((Int, Int, Boolean) -> Unit)?) -> Unit`，表示设置 WebView 查找结果监听器的回调；传 null 可取消监听。
 * @param closeFunctionCenter 参数类型为 `() -> Unit`，表示关闭功能中心面板的回调，打开查找弹窗前调用以避免 UI 重叠。
 * @param dp 参数类型为 `(Int) -> Int`，表示把 dp 数值转换成像素的回调，用来设置弹窗内容边距。
 */
class FindInPageDialogController(
    private val activity: AppCompatActivity,
    private val findInPageController: FindInPageController,
    private val setFindResultListener: (((Int, Int, Boolean) -> Unit)?) -> Unit,
    private val closeFunctionCenter: () -> Unit,
    private val dp: (Int) -> Int
) {
    /**
     * 函数 `showDialog`：展示页面内查找弹窗并绑定查找按钮事件。
     *
     * 初学者阅读提示：按钮使用 setOnClickListener 覆盖默认关闭行为，这样用户可以连续查找而不关闭弹窗。
     */
    fun showDialog() {
        closeFunctionCenter()
        val input = EditText(activity).apply {
            hint = activity.getString(R.string.hint_find_in_page)
            setSingleLine(true)
        }
        val status = TextView(activity).apply {
            text = activity.getString(R.string.find_in_page_status_idle)
            setPadding(0, dp(8), 0, 0)
        }
        val content = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
            addView(input)
            addView(status)
        }
        val dialog = AlertDialog.Builder(activity)
            .setTitle(R.string.action_find_in_page)
            .setView(content)
            .setPositiveButton(R.string.action_find, null)
            .setNeutralButton(R.string.action_find_next, null)
            .setNegativeButton(R.string.action_find_previous, null)
            .create()
        dialog.setOnShowListener {
            setFindResultListener { activeMatchOrdinal, numberOfMatches, isDoneCounting ->
                status.text = statusText(activeMatchOrdinal, numberOfMatches, isDoneCounting)
            }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val started = findInPageController.search(input.text?.toString().orEmpty())
                if (!started) {
                    showEmptyQueryToast()
                    status.text = activity.getString(R.string.find_in_page_status_idle)
                }
            }
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                val moved = findInPageController.findNext()
                if (!moved) {
                    showEmptyQueryToast()
                }
            }
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
                val moved = findInPageController.findPrevious()
                if (!moved) {
                    showEmptyQueryToast()
                }
            }
        }
        dialog.setOnDismissListener {
            setFindResultListener(null)
            findInPageController.clear()
        }
        dialog.show()
    }

    /**
     * 函数 `statusText`：根据 WebView 查找回调生成匹配状态文案。
     *
     * 初学者阅读提示：WebView 计数未完成时显示“正在统计”；没有匹配时显示“无匹配”；有匹配时显示当前序号和总数。
     *
     * @param activeMatchOrdinal 参数类型为 `Int`，表示当前匹配项的零基序号，展示给用户前会加一。
     * @param numberOfMatches 参数类型为 `Int`，表示页面中匹配项总数，小于等于零时显示无匹配。
     * @param isDoneCounting 参数类型为 `Boolean`，表示 WebView 是否完成匹配数量统计，未完成时不展示最终数量。
     * @return 返回本地化后的状态文案，用来更新查找弹窗里的状态 TextView。
     */
    private fun statusText(
        activeMatchOrdinal: Int,
        numberOfMatches: Int,
        isDoneCounting: Boolean
    ): String {
        return when {
            !isDoneCounting -> activity.getString(R.string.find_in_page_status_counting)
            numberOfMatches <= 0 -> activity.getString(R.string.find_in_page_status_no_matches)
            else -> activity.getString(
                R.string.find_in_page_status_matches,
                activeMatchOrdinal + 1,
                numberOfMatches
            )
        }
    }

    /**
     * 函数 `showEmptyQueryToast`：提示用户需要先输入查找关键字。
     *
     * 初学者阅读提示：查找、下一处和上一处都会复用这个提示，避免三个按钮各自写一遍 Toast。
     */
    private fun showEmptyQueryToast() {
        ShortToast.show(activity, R.string.toast_find_query_empty)
    }
}
