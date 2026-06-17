package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器全屏装配模块”。
 * 文件名 BrowserFullscreenAssemblyController 可以拆开理解为“Browser Fullscreen Assembly Controller”，
 * 表示它只负责创建网页视频全屏手势控制器和浏览器全屏 UI 控制器。
 * 阅读顺序：先看 BrowserFullscreenComponents 知道返回哪些对象，再看 create() 中视频全屏与浏览器外壳如何连接。
 */
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.video.FullscreenVideoController

/**
 * 浏览器全屏组件集合。
 *
 * @param fullscreenVideoController 参数类型为 `FullscreenVideoController`，表示网页视频手势层和播放时间线控制器。
 * @param browserFullscreenUiController 参数类型为 `BrowserFullscreenUiController`，表示网页视频全屏时同步工具栏、ChromeClient 和方向策略的控制器。
 */
data class BrowserFullscreenComponents(
    val fullscreenVideoController: FullscreenVideoController,
    val browserFullscreenUiController: BrowserFullscreenUiController
)

/**
 * 浏览器全屏装配控制器。
 *
 * FullscreenVideoController 处理网页视频手势和覆盖层；BrowserFullscreenUiController 负责把全屏状态同步到浏览器外壳。
 * 本类把二者的构造集中起来，让 MainActivity 只保存最终组件。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示网页视频全屏控制器读取资源和创建 UI 的宿主 Activity。
 * @param rootView 参数类型为 `ViewGroup`，表示全屏手势覆盖层挂载的根布局。
 * @param browserManager 参数类型为 `() -> BrowserManager`，表示读取当前 active BrowserManager 的回调。
 * @param settingsManager 参数类型为 `() -> SettingsManager`，表示读取浏览器设置的回调。
 * @param browserChromeClientStateController 参数类型为 `BrowserChromeClientStateController`，表示读取当前 ChromeClient 的状态控制器。
 * @param browserControlsShellController 参数类型为 `BrowserControlsShellController`，表示全屏时隐藏或显示浏览器控制栏的控制器。
 * @param browserDisplayModeController 参数类型为 `BrowserDisplayModeController`，表示恢复全屏后内容方向策略的控制器。
 * @param browserFeatureStateController 参数类型为 `BrowserFeatureStateController`，表示读取桌面模式开关等功能状态的控制器。
 * @param dp 参数类型为 `(Int) -> Int`，表示把 dp 数值转换为像素的回调。
 */
class BrowserFullscreenAssemblyController(
    private val activity: AppCompatActivity,
    private val rootView: ViewGroup,
    private val browserManager: () -> BrowserManager,
    private val settingsManager: () -> SettingsManager,
    private val browserChromeClientStateController: BrowserChromeClientStateController,
    private val browserControlsShellController: BrowserControlsShellController,
    private val browserDisplayModeController: BrowserDisplayModeController,
    private val browserFeatureStateController: BrowserFeatureStateController,
    private val dp: (Int) -> Int
) {
    /**
     * 创建浏览器全屏组件集合。
     *
     * @return 返回 `BrowserFullscreenComponents`，调用方把其中对象保存到 MainActivity 字段后供会话、启动和 JS bridge 使用。
     */
    fun create(): BrowserFullscreenComponents {
        val fullscreenVideoController = FullscreenVideoController(
            activity = activity,
            rootView = rootView,
            browserManager = browserManager,
            settingsManager = settingsManager,
            chromeClient = browserChromeClientStateController::currentChromeClientOrNull,
            dp = dp
        )
        val browserFullscreenUiController = BrowserFullscreenUiController(
            rootView = rootView,
            fullscreenVideoController = fullscreenVideoController,
            browserControlsShellController = browserControlsShellController,
            browserDisplayModeController = browserDisplayModeController,
            currentChromeClient = browserChromeClientStateController::currentChromeClientOrNull,
            isDesktopModeEnabled = browserFeatureStateController::isDesktopModeEnabled
        )
        return BrowserFullscreenComponents(
            fullscreenVideoController = fullscreenVideoController,
            browserFullscreenUiController = browserFullscreenUiController
        )
    }
}
