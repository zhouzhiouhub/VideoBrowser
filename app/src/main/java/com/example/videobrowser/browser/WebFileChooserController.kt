package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“网页文件选择模块”。
 * 文件名 WebFileChooserController 可以拆开理解为“Web File Chooser Controller”，表示它专门负责 WebView <input type=file> 和 Android 文件选择器之间的桥接。
 * 主要职责：保存 WebView 文件回调、启动系统文件选择器、解析返回结果，并在取消或失败时通知 WebView。
 * 阅读顺序：先看 showFileChooser，再看 handleActivityResult，最后看 cancelPending 和 defaultFileChooserIntent。
 */
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient.FileChooserParams
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.utils.ChooserIntentFactory
import com.example.videobrowser.utils.ShortToast

/**
 * WebView 文件选择控制器。
 *
 * MainActivity 负责注册 ActivityResultLauncher，本类负责管理 WebView 文件回调和系统文件选择 Intent。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示当前屏幕宿主，用来创建 chooser Intent、读取字符串资源并展示失败 Toast。
 * @param launchChooser 参数类型为 `(Intent) -> Unit`，表示启动系统文件选择器的回调，参数是已经包好的 chooser Intent。
 */
class WebFileChooserController(
    private val activity: AppCompatActivity,
    private val launchChooser: (Intent) -> Unit
) {
    private var pendingFileChooserCallback: ValueCallback<Array<Uri>>? = null

    /**
     * 函数 `handleActivityResult`：把系统文件选择器返回结果交给 WebView。
     *
     * 初学者阅读提示：FileChooserParams.parseResult 会把 resultCode 和 Intent 转成 WebView 期望的 Uri 数组。
     *
     * @param resultCode 参数类型为 `Int`，表示系统文件选择器返回的结果码，用来判断用户是否选中文件。
     * @param data 参数类型为 `Intent?`，表示系统文件选择器返回的数据，里面可能包含一个或多个文件 Uri。
     */
    fun handleActivityResult(resultCode: Int, data: Intent?) {
        pendingFileChooserCallback?.onReceiveValue(
            FileChooserParams.parseResult(resultCode, data)
        )
        pendingFileChooserCallback = null
    }

    /**
     * 函数 `showFileChooser`：启动系统文件选择器并保存 WebView 回调。
     *
     * 初学者阅读提示：如果网页没有提供回调则返回 false；如果系统没有可用选择器，会立即通知 WebView 选择失败。
     *
     * @param filePathCallback 参数类型为 `ValueCallback<Array<Uri>>?`，表示 WebView 等待文件选择结果的回调。
     * @param fileChooserParams 参数类型为 `FileChooserParams?`，表示网页文件输入框提供的选择要求，例如 MIME 类型或是否允许多选。
     * @return 返回是否成功启动了文件选择流程，ChromeClient 会把这个值回传给 WebView。
     */
    fun showFileChooser(
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {
        val callback = filePathCallback ?: return false
        pendingFileChooserCallback?.onReceiveValue(null)
        pendingFileChooserCallback = callback

        val pickerIntent = runCatching {
            fileChooserParams?.createIntent() ?: defaultFileChooserIntent()
        }.getOrDefault(defaultFileChooserIntent())

        return try {
            launchChooser(
                ChooserIntentFactory.create(activity, pickerIntent, R.string.action_open_file)
            )
            true
        } catch (_: ActivityNotFoundException) {
            pendingFileChooserCallback = null
            callback.onReceiveValue(null)
            ShortToast.show(activity, R.string.toast_file_chooser_unavailable)
            false
        }
    }

    /**
     * 函数 `cancelPending`：取消尚未完成的网页文件选择请求。
     *
     * 初学者阅读提示：Activity 销毁或新选择流程开始时调用，传 null 给 WebView 表示没有文件被选中。
     */
    fun cancelPending() {
        pendingFileChooserCallback?.onReceiveValue(null)
        pendingFileChooserCallback = null
    }

    /**
     * 函数 `defaultFileChooserIntent`：创建默认系统文件选择 Intent。
     *
     * 初学者阅读提示：当网页没有提供可用 FileChooserParams 时，使用这个通用 Intent 允许用户选择一个或多个文件。
     *
     * @return 返回 ACTION_GET_CONTENT Intent，调用方会再包一层 chooser 展示给用户。
     */
    private fun defaultFileChooserIntent(): Intent {
        return Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
    }
}
