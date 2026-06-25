package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器导航装配模块”。
 * 文件名 BrowserNavigationAssemblyController 可以拆开理解为“Browser Navigation Assembly Controller”，
 * 表示它只负责创建规则引擎、外部导航、原生播放器入口、页面导航、启动入口和显示模式控制器。
 * 阅读顺序：先看 BrowserNavigationComponents 知道返回哪些对象，再看 create() 中导航链路如何按顺序连接。
 */
import android.content.res.AssetManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.browser.search.AddressSuggestionController
import com.example.videobrowser.browser.search.SearchProviderController
import com.example.videobrowser.rules.RuleEngine
import com.example.videobrowser.rules.RuleEngineFactory
import com.example.videobrowser.video.NativePlayerEntryController
import java.io.File

/**
 * 浏览器导航组件集合。
 *
 * @param ruleEngine 参数类型为 `RuleEngine`，表示广告拦截、URL 清理、脚本注入共用的规则引擎。
 * @param externalNavigator 参数类型为 `BrowserExternalNavigator`，表示外部协议、分享和系统打开能力的导航器。
 * @param nativePlayerEntryController 参数类型为 `NativePlayerEntryController`，表示把媒体 URL 交给原生播放器的入口控制器。
 * @param browserNavigationController 参数类型为 `BrowserNavigationController`，表示处理地址栏加载和 WebView 跳转拦截的控制器。
 * @param browserLaunchController 参数类型为 `BrowserLaunchController`，表示处理地址栏输入、主页和外部 Intent 的入口控制器。
 * @param browserDisplayModeController 参数类型为 `BrowserDisplayModeController`，表示处理桌面模式 User-Agent 和屏幕方向的控制器。
 */
data class BrowserNavigationComponents(
    val ruleEngine: RuleEngine,
    val externalNavigator: BrowserExternalNavigator,
    val nativePlayerEntryController: NativePlayerEntryController,
    val browserNavigationController: BrowserNavigationController,
    val browserLaunchController: BrowserLaunchController,
    val browserDisplayModeController: BrowserDisplayModeController
)

/**
 * 浏览器导航装配控制器。
 *
 * 这里的对象彼此依赖较紧：导航控制器需要外部导航器和原生播放器入口，启动控制器又依赖导航控制器。
 * 将它们集中装配可以让 MainActivity 保留初始化顺序，而不承担具体 wiring 细节。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示显示确认弹窗、Toast 和设置屏幕方向的宿主 Activity。
 * @param assets 参数类型为 `AssetManager`，表示读取内置规则资源的 Android assets 入口。
 * @param filesDir 参数类型为 `File`，表示应用私有目录，用于读取用户规则订阅缓存。
 * @param addressInput 参数类型为 `EditText`，表示地址栏输入框，用于启动控制器读取用户输入。
 * @param standardTabStore 参数类型为 `BrowserTabStore`，表示标准模式标签页数据源，用于启动时恢复当前标签 URL。
 * @param browserStandardWebViewHostController 参数类型为 `BrowserStandardWebViewHostController`，表示当前 WebView 和所有 BrowserManager 的宿主控制器。
 * @param browserSessionStateController 参数类型为 `BrowserSessionStateController`，表示读取当前模式页面会话状态的控制器。
 * @param browserUrlStateController 参数类型为 `BrowserUrlStateController`，表示提供可分享 URL 和 URL 类型判断的控制器。
 * @param browserFeatureStateController 参数类型为 `BrowserFeatureStateController`，表示提供无痕状态和桌面模式状态的控制器。
 * @param browserAddressBarStateController 参数类型为 `BrowserAddressBarStateController`，表示刷新地址栏展示文本和站点安全状态的控制器。
 * @param browserKeyboardController 参数类型为 `BrowserKeyboardController`，表示加载页面前隐藏软键盘的控制器。
 * @param browserShellUiController 参数类型为 `BrowserShellUiController`，表示切换首页和 WebView 内容区域的控制器。
 * @param browserChromeClientStateController 参数类型为 `BrowserChromeClientStateController`，表示读取当前 ChromeClient 全屏状态的控制器。
 * @param homePageUrlPolicy 参数类型为 `BrowserHomePageUrlPolicy`，表示识别启动恢复 URL 是否应该回到 App 自定义首页的策略。
 * @param addressSuggestionController 参数类型为 `AddressSuggestionController`，表示地址栏建议控制器，用于加载地址栏时临时压制建议刷新。
 * @param searchProviderController 参数类型为 `SearchProviderController`，表示读取当前搜索引擎并构造搜索 URL 的控制器。
 * @param closeFunctionCenter 参数类型为 `() -> Boolean`，表示页面加载前关闭功能中心面板的回调。
 * @param defaultUserAgent 参数类型为 `() -> String?`，表示读取应用启动时默认 User-Agent 的回调。
 */
class BrowserNavigationAssemblyController(
    private val activity: AppCompatActivity,
    private val assets: AssetManager,
    private val filesDir: File,
    private val addressInput: EditText,
    private val standardTabStore: BrowserTabStore,
    private val browserStandardWebViewHostController: BrowserStandardWebViewHostController,
    private val browserSessionStateController: BrowserSessionStateController,
    private val browserUrlStateController: BrowserUrlStateController,
    private val browserFeatureStateController: BrowserFeatureStateController,
    private val browserAddressBarStateController: BrowserAddressBarStateController,
    private val browserKeyboardController: BrowserKeyboardController,
    private val browserShellUiController: BrowserShellUiController,
    private val browserChromeClientStateController: BrowserChromeClientStateController,
    private val homePageUrlPolicy: BrowserHomePageUrlPolicy,
    private val addressSuggestionController: AddressSuggestionController,
    private val searchProviderController: SearchProviderController,
    private val closeFunctionCenter: () -> Boolean,
    private val defaultUserAgent: () -> String?
) {
    /**
     * 创建浏览器导航组件集合。
     *
     * @return 返回 `BrowserNavigationComponents`，调用方把其中对象保存到对应字段后继续创建下载和页面动作控制器。
     */
    fun create(): BrowserNavigationComponents {
        val ruleEngine = RuleEngineFactory.create(assets, filesDir)
        val externalNavigator = BrowserExternalNavigator(
            activity = activity,
            browserManager = {
                browserStandardWebViewHostController.currentBrowserManager()
            },
            currentPageTitle = {
                if (browserSessionStateController.areBrowserSessionsInitialized()) {
                    browserSessionStateController.currentSessionController().currentPageTitle
                } else {
                    ""
                }
            },
            currentShareableUrl = browserUrlStateController::currentShareableUrl,
            isShareableUrl = browserUrlStateController::isShareableUrl
        )
        val nativePlayerEntryController = NativePlayerEntryController(
            externalNavigator = externalNavigator,
            isPrivateBrowsingEnabled = browserFeatureStateController::isPrivateBrowsingEnabled
        )
        val browserNavigationController = BrowserNavigationController(
            activity = activity,
            ruleEngine = { ruleEngine },
            browserManager = {
                browserStandardWebViewHostController.currentBrowserManager()
            },
            sessionController = browserSessionStateController::currentSessionController,
            externalNavigator = externalNavigator,
            closeFunctionCenter = closeFunctionCenter,
            openNativePlayer = nativePlayerEntryController::openNativePlayer,
            updateAddressBar = browserAddressBarStateController::updateAddressBar,
            hideKeyboard = browserKeyboardController::hideKeyboard,
            showHomeContent = browserShellUiController::showHomeContent
        )
        val browserLaunchController = BrowserLaunchController(
            addressText = { addressInput.text?.toString().orEmpty() },
            runWithSuggestionsSuppressed = addressSuggestionController::runWithSuggestionsSuppressed,
            searchUrlForQuery = { keyword ->
                searchProviderController.selectedProvider.searchUrlFor(keyword)
            },
            activeStandardTabUrl = { standardTabStore.activeTab().url },
            shouldOpenAppHome = homePageUrlPolicy::isHomeUrl,
            showHomePage = {
                closeFunctionCenter()
                browserKeyboardController.hideKeyboard()
                if (browserSessionStateController.areBrowserSessionsInitialized()) {
                    browserSessionStateController.currentSessionController().reset()
                } else {
                    browserShellUiController.showHomeContent(true)
                }
            },
            loadUrl = browserNavigationController::loadUrl,
            isShareableUrl = browserUrlStateController::isShareableUrl
        )
        val browserDisplayModeController = BrowserDisplayModeController(
            activity = activity,
            browserManager = {
                browserStandardWebViewHostController.currentBrowserManager()
            },
            isDesktopModeEnabled = browserFeatureStateController::isDesktopModeEnabled,
            isFullscreenModeActive = {
                browserChromeClientStateController.currentChromeClientOrNull()
                    ?.isFullscreenModeActive() == true
            },
            defaultUserAgent = defaultUserAgent
        )
        return BrowserNavigationComponents(
            ruleEngine = ruleEngine,
            externalNavigator = externalNavigator,
            nativePlayerEntryController = nativePlayerEntryController,
            browserNavigationController = browserNavigationController,
            browserLaunchController = browserLaunchController,
            browserDisplayModeController = browserDisplayModeController
        )
    }
}
