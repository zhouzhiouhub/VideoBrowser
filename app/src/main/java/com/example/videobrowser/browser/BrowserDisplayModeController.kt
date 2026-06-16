package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器显示模式模块”。
 * 桌面模式会同时修改 WebView User-Agent，并在普通网页浏览时请求横屏；网页视频全屏时则不抢系统方向。
 * 主要职责：应用桌面 UA、恢复默认 UA、根据桌面模式刷新 Activity 方向。
 * 阅读顺序：先看 applyDesktopMode，再看 applyBrowserContentOrientation。
 */
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity

/**
 * 浏览器显示模式控制器。
 *
 * MainActivity 只负责在设置变化或视频全屏退出时调用本类；本类负责桌面模式和屏幕方向细节。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示当前屏幕宿主，用来设置 requestedOrientation。
 * @param browserManager 参数类型为 `() -> BrowserManager`，表示读取当前浏览器管理器的回调，用来切换 WebView User-Agent。
 * @param isDesktopModeEnabled 参数类型为 `() -> Boolean`，表示读取桌面模式开关的回调。
 * @param isFullscreenModeActive 参数类型为 `() -> Boolean`，表示读取网页全屏状态的回调；全屏时不会改 Activity 方向。
 * @param defaultUserAgent 参数类型为 `() -> String?`，表示读取应用启动时默认 User-Agent 的回调。
 */
class BrowserDisplayModeController(
    private val activity: AppCompatActivity,
    private val browserManager: () -> BrowserManager,
    private val isDesktopModeEnabled: () -> Boolean,
    private val isFullscreenModeActive: () -> Boolean,
    private val defaultUserAgent: () -> String?
) {
    /**
     * 函数 `applyDesktopMode`：把当前桌面模式开关应用到 WebView 和屏幕方向。
     *
     * @param reload 参数类型为 `Boolean`，表示切换 User-Agent 后是否重新加载当前页面。
     */
    fun applyDesktopMode(reload: Boolean) {
        val desktopModeEnabled = isDesktopModeEnabled()
        applyBrowserContentOrientation(desktopModeEnabled)
        browserManager().applyDesktopMode(
            enabled = desktopModeEnabled,
            desktopUserAgent = DESKTOP_USER_AGENT,
            defaultUserAgent = defaultUserAgent(),
            reload = reload
        )
    }

    /**
     * 函数 `applyBrowserContentOrientation`：根据桌面模式刷新 Activity 方向。
     *
     * 初学者阅读提示：网页视频全屏期间方向由全屏控制器接管，所以这里直接返回。
     *
     * @param desktopModeEnabled 参数类型为 `Boolean`，表示当前是否启用桌面模式。
     */
    fun applyBrowserContentOrientation(desktopModeEnabled: Boolean) {
        if (isFullscreenModeActive()) {
            return
        }
        activity.requestedOrientation = if (desktopModeEnabled) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    private companion object {
        private const val DESKTOP_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
    }
}
