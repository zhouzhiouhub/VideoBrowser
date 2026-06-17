package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器全屏 UI 协调模块”。
 * FullscreenVideoController 负责网页视频手势层本身；本类负责把网页全屏状态同步到浏览器外壳，
 * 包括工具栏隐藏、进度条隐藏、安全区刷新和退出全屏时恢复屏幕方向。
 * 阅读顺序：先看构造参数了解它依赖哪些 UI 控制器，再看 handleVideoFullscreenChanged() 的同步顺序。
 */
import android.view.View
import androidx.core.view.ViewCompat
import com.example.videobrowser.video.FullscreenVideoController

/**
 * 浏览器全屏 UI 控制器。
 *
 * MainActivity 创建 WebView、ChromeClient 和视频手势控制器；这个类只负责全屏状态变化时的 UI 联动。
 *
 * @param rootView 参数类型为 `View`，表示 Activity 根视图，用于在全屏状态变化后重新申请系统栏安全区。
 * @param fullscreenVideoController 参数类型为 `FullscreenVideoController`，表示网页视频手势层控制器，用于挂载覆盖层并同步视频全屏状态。
 * @param browserControlsShellController 参数类型为 `BrowserControlsShellController`，表示浏览器顶部/底部工具栏外壳控制器，用于全屏时隐藏工具栏和进度条。
 * @param browserDisplayModeController 参数类型为 `BrowserDisplayModeController`，表示桌面模式与屏幕方向控制器，用于退出全屏后恢复普通浏览方向。
 * @param currentChromeClient 参数类型为 `() -> ChromeClient?`，表示读取当前 ChromeClient 的函数，尚未初始化时返回 null。
 * @param isDesktopModeEnabled 参数类型为 `() -> Boolean`，表示读取桌面模式开关的函数，用于退出全屏后决定横竖屏策略。
 */
class BrowserFullscreenUiController(
    private val rootView: View,
    private val fullscreenVideoController: FullscreenVideoController,
    private val browserControlsShellController: BrowserControlsShellController,
    private val browserDisplayModeController: BrowserDisplayModeController,
    private val currentChromeClient: () -> ChromeClient?,
    private val isDesktopModeEnabled: () -> Boolean
) {
    /**
     * 挂载网页视频全屏手势覆盖层。
     *
     * @return 无返回值；覆盖层会加入根布局，只有网页视频全屏时才显示。
     */
    fun setupFullscreenGestureOverlay() {
        fullscreenVideoController.attachOverlay()
    }

    /**
     * 如果当前网页处于页面级全屏，则退出该状态。
     *
     * 初学者阅读提示：ChromeClient 的自定义视图全屏由 hideCustomView() 处理；这里仅处理页面 video.requestFullscreen()。
     *
     * @return 无返回值；没有可退出的页面级全屏时直接返回。
     */
    fun exitPageFullscreenIfNeeded() {
        val chromeClient = currentChromeClient() ?: return
        if (chromeClient.isFullscreenModeActive() && !chromeClient.isShowingCustomView()) {
            chromeClient.exitPageFullscreen()
        }
    }

    /**
     * 同步一次网页全屏状态变化。
     *
     * @param fullscreen 参数类型为 `Boolean`，true 表示网页进入全屏，false 表示网页退出全屏。
     * @return 无返回值；函数会更新视频手势层、浏览器工具栏、页面进度条、安全区和屏幕方向。
     */
    fun handleVideoFullscreenChanged(fullscreen: Boolean) {
        fullscreenVideoController.handleFullscreenChanged(fullscreen)
        browserControlsShellController.setBrowserControlsHidden(fullscreen)
        browserControlsShellController.updatePageProgressVisibility(forceHidden = fullscreen)
        ViewCompat.requestApplyInsets(rootView)
        if (!fullscreen) {
            browserDisplayModeController.applyBrowserContentOrientation(isDesktopModeEnabled())
        }
    }
}
