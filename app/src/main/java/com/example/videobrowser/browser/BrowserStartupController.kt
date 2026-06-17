package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器启动编排模块”。
 * 文件名 BrowserStartupController 可以拆开理解为“Browser Startup Controller”，
 * 表示它只负责 MainActivity 完成依赖创建之后的启动收尾：初始化控件、套用 WebView 设置、
 * 绑定 ChromeClient/WebViewClient/原生桥，并处理首个 Intent。
 * 阅读顺序：先看构造参数知道启动阶段需要哪些控制器，再看 start() 的执行顺序。
 */
import android.content.Intent
import com.example.videobrowser.browser.search.AddressSuggestionController
import com.example.videobrowser.download.DownloadController
import com.example.videobrowser.settings.SettingsManager

/**
 * 浏览器启动控制器。
 *
 * MainActivity 的 onCreate() 负责创建依赖；本类负责在依赖都准备好后执行一次性的启动配置。
 * 这样启动顺序仍然集中可读，但 Activity 不再直接承载所有 WebView 和外壳初始化细节。
 *
 * @param browserControlsShellController 浏览器控制栏外壳控制器，用于初始化搜索入口和 WebView 滚动控制。
 * @param addressSuggestionController 地址建议控制器，用于启动地址栏建议监听。
 * @param browsingModeThemeController 普通/无痕主题控制器，用于刷新初始无痕 UI。
 * @param browserShellUiController 浏览器外壳 UI 控制器，用于初始化导航按钮和站点安全图标。
 * @param browserBackNavigationController 返回键控制器，用于安装系统返回键处理逻辑。
 * @param browserStandardWebViewHostController 标准 WebView 宿主控制器，用于读取 BrowserManager 和 BrowserManager 列表。
 * @param settingsManager 设置管理器，用于读取第三方 Cookie、混合内容和文字缩放初始值。
 * @param setDefaultUserAgent 保存默认 User-Agent 的函数，参数是当前标准 WebView 的 User-Agent。
 * @param browserDisplayModeController 显示模式控制器，用于启动时应用桌面模式设置。
 * @param downloadController 下载控制器，用于把 WebView 下载监听绑定到所有 BrowserManager。
 * @param browserChromeClientController ChromeClient 控制器，用于创建并绑定 ChromeClient。
 * @param browserFullscreenUiController 浏览器全屏 UI 控制器，用于安装全屏手势遮罩。
 * @param nativeBridgeController 原生桥控制器，用于创建注入网页的 JavaScript 接口对象。
 * @param nativeBridgeName 注入 WebView 的 JavaScript 接口名称。
 * @param browserWebClientController WebViewClient 控制器，用于创建并绑定 BrowserClient。
 * @param browserLaunchController 启动导航控制器，用于处理外部 Intent 或打开初始标准页面。
 */
class BrowserStartupController(
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
     * @param intent Activity 启动 Intent，可能包含外部打开的 URL。
     * @return 无返回值；函数会完成控件初始化、WebView 配置、客户端绑定和初始页面打开。
     */
    fun start(intent: Intent) {
        browserControlsShellController.setupSearchProviders()
        addressSuggestionController.setup()
        browsingModeThemeController.updatePrivateBrowsingUi()
        browserShellUiController.setupBrowserControls()
        browserControlsShellController.setupWebViewScrollControls()
        browserBackNavigationController.setupBackNavigation()

        val standardBrowserManager = browserStandardWebViewHostController.browserManager
        standardBrowserManager.setup()
        standardBrowserManager.setThirdPartyCookiesEnabled(settingsManager.areThirdPartyCookiesEnabled())
        standardBrowserManager.setMixedContentBlocked(settingsManager.isMixedContentBlocked())
        standardBrowserManager.setTextZoomPercent(settingsManager.textZoomPercent())
        standardBrowserManager.setPrivateBrowsingEnabled(false)
        setDefaultUserAgent(standardBrowserManager.userAgentString())

        browserDisplayModeController.applyDesktopMode(reload = false)
        downloadController.attachTo(browserStandardWebViewHostController.browserManagers())
        browserChromeClientController.setupChromeClient()
        browserFullscreenUiController.setupFullscreenGestureOverlay()
        standardBrowserManager.addJavascriptInterface(
            nativeBridgeController.createNativeBridge(),
            nativeBridgeName
        )
        browserWebClientController.setupBrowserClient()

        if (!browserLaunchController.handleLaunchIntent(intent)) {
            browserLaunchController.openInitialStandardPage()
        }
    }
}
