package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器窗口安全区模块”。
 * 文件名 BrowserWindowInsetsController 可以拆开理解为“Browser Window Insets Controller”，
 * 表示它只负责把 Android 系统栏、刘海区域等安全区转换成主界面的 padding。
 * 阅读顺序：先看构造参数，再看 setupSystemWindowInsets() 如何在普通页面和视频全屏之间切换 padding。
 */
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * 浏览器窗口安全区控制器。
 *
 * Activity 不需要直接了解 WindowInsetsCompat 的细节；本类集中处理系统栏和刘海区域，
 * 并在视频全屏 UI 激活时让内容占满屏幕。
 *
 * @param rootView Activity 的根视图，用来接收系统窗口安全区变化并设置 padding。
 * @param isVideoFullscreenUiActive 返回当前视频全屏 UI 是否激活的函数，true 表示根视图不保留系统安全区 padding。
 */
class BrowserWindowInsetsController(
    private val rootView: View,
    private val isVideoFullscreenUiActive: () -> Boolean
) {
    /**
     * 安装系统窗口安全区监听器。
     *
     * @return 无返回值；函数会立即请求一次 insets，并在后续安全区变化时更新 rootView padding。
     */
    fun setupSystemWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val safeArea = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or
                    WindowInsetsCompat.Type.displayCutout()
            )
            if (isVideoFullscreenUiActive()) {
                view.setPadding(0, 0, 0, 0)
            } else {
                view.setPadding(safeArea.left, safeArea.top, safeArea.right, safeArea.bottom)
            }
            insets
        }
        ViewCompat.requestApplyInsets(rootView)
    }
}
