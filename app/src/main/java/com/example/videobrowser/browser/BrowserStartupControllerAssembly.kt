package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器启动收尾装配模块”。
 * 文件名 BrowserStartupControllerAssembly 可以拆开理解为“Browser Startup Controller Assembly”，
 * 表示它负责创建系统窗口 inset 控制器，并把所有启动期依赖交给 BrowserStartupController。
 * 阅读顺序：先看 start() 的两个步骤：设置窗口 inset，然后执行 BrowserStartupController.start(intent)。
 */
import android.content.Intent
import android.view.View
import com.example.videobrowser.browser.search.AddressSuggestionController
import com.example.videobrowser.download.DownloadController
import com.example.videobrowser.settings.SettingsManager

/**
 * 浏览器启动收尾装配器。
 *
 * MainActivity 创建完所有控制器后，本类负责执行最后的一次性启动流程，避免 Activity 末尾继续展开
 * BrowserWindowInsetsController 和 BrowserStartupController 的所有参数。
 *
 * @param rootView 参数类型为 `View`，表示主界面根视图，用来安装系统窗口 inset 处理。
 * @param isVideoFullscreenUiActive 参数类型为 `() -> Boolean`，表示读取当前视频全屏 UI 是否激活的回调。
 * @param browserControlsShellController 参数类型为 `BrowserControlsShellController`，表示初始化搜索入口和滚动控制的控制器。
 * @param addressSuggestionController 参数类型为 `AddressSuggestionController`，表示初始化地址建议监听的控制器。
 * @param browsingModeThemeController 参数类型为 `BrowsingModeThemeController`，表示刷新初始普通/无痕主题的控制器。
 * @param browserShellUiController 参数类型为 `BrowserShellUiController`，表示初始化浏览器按钮和站点安全图标的控制器。
 * @param browserBackNavigationController 参数类型为 `BrowserBackNavigationController`，表示安装系统返回键处理的控制器。
 * @param browserStandardWebViewHostController 参数类型为 `BrowserStandardWebViewHostController`，表示提供标准 BrowserManager 和 BrowserManager 列表的宿主控制器。
 * @param settingsManager 参数类型为 `SettingsManager`，表示读取启动期 WebView 设置的数据源。
 * @param setDefaultUserAgent 参数类型为 `(String?) -> Unit`，表示保存默认 User-Agent 的回调。
 * @param browserDisplayModeController 参数类型为 `BrowserDisplayModeController`，表示启动时应用桌面模式的控制器。
 * @param downloadController 参数类型为 `DownloadController`，表示把下载监听绑定到所有 BrowserManager 的控制器。
 * @param browserChromeClientController 参数类型为 `BrowserChromeClientController`，表示创建并绑定 ChromeClient 的控制器。
 * @param browserFullscreenUiController 参数类型为 `BrowserFullscreenUiController`，表示安装网页全屏手势覆盖层的控制器。
 * @param nativeBridgeController 参数类型为 `VideoBrowserNativeBridgeController`，表示创建注入网页 JavaScript 接口对象的控制器。
 * @param nativeBridgeName 参数类型为 `String`，表示注入 WebView 的 JavaScript 接口名称。
 * @param browserWebClientController 参数类型为 `BrowserWebClientController`，表示创建并绑定 BrowserClient 的控制器。
 * @param browserLaunchController 参数类型为 `BrowserLaunchController`，表示处理启动 Intent 或打开初始页面的控制器。
 */
class BrowserStartupControllerAssembly(
    private val rootView: View,
    private val isVideoFullscreenUiActive: () -> Boolean,
    private val browserControlsShellController: BrowserControlsShellController,
    private val addressSuggestionController: AddressSuggestionController,
    private val browsingModeThemeController: BrowsingModeThemeController,
    private val browserShellUiController: BrowserShellUiController,
    private val browserBackNavigationController: BrowserBackNavigationController,
    private val browserStandardWebViewHostController: BrowserStandardWebViewHostController,
    private val settingsManager: SettingsManager,
    private val setDefaultUserAgent: (String?) -> Unit,
    private val browserDisplayModeController: BrowserDisplayModeController,
    private val downloadController: DownloadController,
    private val browserChromeClientController: BrowserChromeClientController,
    private val browserFullscreenUiController: BrowserFullscreenUiController,
    private val nativeBridgeController: VideoBrowserNativeBridgeController,
    private val nativeBridgeName: String,
    private val browserWebClientController: BrowserWebClientController,
    private val browserLaunchController: BrowserLaunchController
) {
    /**
     * 执行浏览器启动收尾。
     *
     * @param intent 参数类型为 `Intent`，表示 Activity 启动 Intent，可能包含外部打开的 URL。
     */
    fun start(intent: Intent) {
        BrowserWindowInsetsController(
            rootView = rootView,
            isVideoFullscreenUiActive = isVideoFullscreenUiActive
        ).setupSystemWindowInsets()

        BrowserStartupController(
            browserControlsShellController = browserControlsShellController,
            addressSuggestionController = addressSuggestionController,
            browsingModeThemeController = browsingModeThemeController,
            browserShellUiController = browserShellUiController,
            browserBackNavigationController = browserBackNavigationController,
            browserStandardWebViewHostController = browserStandardWebViewHostController,
            settingsManager = settingsManager,
            setDefaultUserAgent = setDefaultUserAgent,
            browserDisplayModeController = browserDisplayModeController,
            downloadController = downloadController,
            browserChromeClientController = browserChromeClientController,
            browserFullscreenUiController = browserFullscreenUiController,
            nativeBridgeController = nativeBridgeController,
            nativeBridgeName = nativeBridgeName,
            browserWebClientController = browserWebClientController,
            browserLaunchController = browserLaunchController
        ).start(intent)
    }
}
