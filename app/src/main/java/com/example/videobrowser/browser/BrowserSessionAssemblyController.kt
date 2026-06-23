package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器会话与标签页装配模块”。
 * 文件名 BrowserSessionAssemblyController 可以拆开理解为“Browser Session Assembly Controller”，
 * 表示它只负责创建标准/无痕会话、无痕切换和标签页动作控制器。
 * 阅读顺序：先看 BrowserSessionComponents 知道返回哪些对象，再看 create() 中标准会话和无痕会话的差异。
 */
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.inject.PageFeatureInjectionController
import com.example.videobrowser.settings.SessionSitePermissionStore

/**
 * 浏览器会话组件集合。
 *
 * @param standardSessionController 参数类型为 `BrowserSessionController`，表示标准浏览模式的页面状态控制器。
 * @param privateSessionController 参数类型为 `BrowserSessionController`，表示无痕浏览模式的页面状态控制器。
 * @param privateBrowsingSwitchController 参数类型为 `PrivateBrowsingSwitchController`，表示标准/无痕模式切换控制器。
 * @param browserTabActionsController 参数类型为 `BrowserTabActionsController`，表示新建、切换、关闭、恢复和复制标签页的动作控制器。
 */
data class BrowserSessionComponents(
    val standardSessionController: BrowserSessionController,
    val privateSessionController: BrowserSessionController,
    val privateBrowsingSwitchController: PrivateBrowsingSwitchController,
    val browserTabActionsController: BrowserTabActionsController
)

/**
 * 浏览器会话与标签页装配控制器。
 *
 * 标准会话会写入历史和标签页会话；无痕会话只维护内存状态。本类集中创建两套会话，
 * 并把无痕切换和标签页动作连接到同一组 WebView/标签页存储回调上。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示会话控制器读取字符串资源和创建 UI 反馈的宿主 Activity。
 * @param standardTabStore 参数类型为 `BrowserTabStore`，表示标准模式标签页数据源。
 * @param privateTabStore 参数类型为 `BrowserTabStore`，表示无痕模式标签页数据源。
 * @param standardTabWebViews 参数类型为 `BrowserTabWebViewRegistry<WebView>`，表示标准标签页 ID 到 WebView 的映射表。
 * @param browserSessionCoordinator 参数类型为 `BrowserSessionCoordinator`，表示标准/无痕 WebView 切换协调器。
 * @param browserAddressBarStateController 参数类型为 `BrowserAddressBarStateController`，表示更新地址栏和站点安全状态的控制器。
 * @param browserShellUiController 参数类型为 `BrowserShellUiController`，表示更新首页内容和导航按钮状态的控制器。
 * @param browserControlsController 参数类型为 `BrowserControlsController`，表示更新页面加载进度的控制器。
 * @param browserControlsShellController 参数类型为 `BrowserControlsShellController`，表示更新进度条可见性的控制器。
 * @param pageActionsController 参数类型为 `PageActionsController`，表示标准会话写入浏览历史的控制器。
 * @param pageFeatureInjectionController 参数类型为 `PageFeatureInjectionController`，表示页面完成加载后注入增强脚本的控制器。
 * @param browsingModeThemeController 参数类型为 `BrowsingModeThemeController`，表示切换无痕模式时刷新主题 UI 的控制器。
 * @param sessionSitePermissionStore 参数类型为 `SessionSitePermissionStore`，表示切换无痕模式时清理会话站点权限的存储。
 * @param isPrivateBrowsingActive 参数类型为 `() -> Boolean`，表示读取当前是否处于无痕模式的回调。
 * @param clearElementPickerState 参数类型为 `() -> Unit`，表示页面开始加载时清理元素选择器状态的回调。
 * @param cancelElementPickerIfActive 参数类型为 `() -> Unit`，表示切换无痕模式前取消元素选择器的回调。
 * @param exitPageFullscreenIfNeeded 参数类型为 `() -> Unit`，表示页面变化或切换模式时退出网页全屏的回调。
 * @param closeFunctionCenter 参数类型为 `() -> Boolean`，表示执行模式切换或标签页动作前关闭功能中心的回调。
 * @param openHomePage 参数类型为 `() -> Unit`，表示打开当前模式主页的回调。
 * @param loadUrl 参数类型为 `(String) -> Unit`，表示在当前标签页加载 URL 的回调。
 * @param createStandardTabWebView 参数类型为 `() -> WebView`，表示创建标准标签页 WebView 的回调。
 * @param showStandardTabWebView 参数类型为 `(WebView) -> Unit`，表示把标准标签页 WebView 切到前台的回调。
 * @param hideStandardTabWebView 参数类型为 `(WebView) -> Unit`，表示隐藏标准标签页 WebView 的回调。
 * @param destroyStandardTabWebView 参数类型为 `(WebView) -> Unit`，表示销毁标准标签页 WebView 的回调。
 * @param saveStandardTabSession 参数类型为 `() -> Unit`，表示保存标准标签页会话的回调。
 * @param onStandardPageMetadataChanged 参数类型为 `(String?, String?) -> Unit`，表示标准会话 URL/标题变化后同步标签页元数据的回调。
 * @param onPrivatePageMetadataChanged 参数类型为 `(String?, String?) -> Unit`，表示无痕会话 URL/标题变化后同步标签页元数据的回调。
 */
class BrowserSessionAssemblyController(
    private val activity: AppCompatActivity,
    private val standardTabStore: BrowserTabStore,
    private val privateTabStore: BrowserTabStore,
    private val standardTabWebViews: BrowserTabWebViewRegistry<WebView>,
    private val browserSessionCoordinator: BrowserSessionCoordinator,
    private val browserAddressBarStateController: BrowserAddressBarStateController,
    private val browserShellUiController: BrowserShellUiController,
    private val browserControlsController: BrowserControlsController,
    private val browserControlsShellController: BrowserControlsShellController,
    private val pageActionsController: PageActionsController,
    private val pageFeatureInjectionController: PageFeatureInjectionController,
    private val browsingModeThemeController: BrowsingModeThemeController,
    private val sessionSitePermissionStore: SessionSitePermissionStore,
    private val isPrivateBrowsingActive: () -> Boolean,
    private val clearElementPickerState: () -> Unit,
    private val cancelElementPickerIfActive: () -> Unit,
    private val exitPageFullscreenIfNeeded: () -> Unit,
    private val closeFunctionCenter: () -> Boolean,
    private val openHomePage: () -> Unit,
    private val loadUrl: (String) -> Unit,
    private val createStandardTabWebView: () -> WebView,
    private val showStandardTabWebView: (WebView) -> Unit,
    private val hideStandardTabWebView: (WebView) -> Unit,
    private val destroyStandardTabWebView: (WebView) -> Unit,
    private val saveStandardTabSession: () -> Unit,
    private val onStandardPageMetadataChanged: (String?, String?) -> Unit,
    private val onPrivatePageMetadataChanged: (String?, String?) -> Unit
) {
    /**
     * 创建浏览器会话组件集合。
     *
     * @return 返回 `BrowserSessionComponents`，调用方把其中对象保存到 MainActivity 字段后供 WebView client、功能中心和启动流程使用。
     */
    fun create(): BrowserSessionComponents {
        val standardSessionController = BrowserSessionController(
            activity = activity,
            isActive = { !isPrivateBrowsingActive() },
            clearElementPickerState = clearElementPickerState,
            exitPageFullscreenIfNeeded = exitPageFullscreenIfNeeded,
            updateAddressBar = browserAddressBarStateController::updateAddressBar,
            showHomeContent = browserShellUiController::showHomeContent,
            setPageProgress = browserControlsController::setProgress,
            updatePageProgressVisibility = browserControlsShellController::updatePageProgressVisibility,
            updateNavigationButtons = browserShellUiController::updateNavigationButtons,
            addHistoryEntry = pageActionsController::addHistoryEntry,
            injectPageFeatures = pageFeatureInjectionController::injectPageFeatures,
            onPageMetadataChanged = onStandardPageMetadataChanged
        )
        val privateSessionController = BrowserSessionController(
            activity = activity,
            isActive = isPrivateBrowsingActive,
            clearElementPickerState = clearElementPickerState,
            exitPageFullscreenIfNeeded = exitPageFullscreenIfNeeded,
            updateAddressBar = browserAddressBarStateController::updateAddressBar,
            showHomeContent = browserShellUiController::showHomeContent,
            setPageProgress = browserControlsController::setProgress,
            updatePageProgressVisibility = browserControlsShellController::updatePageProgressVisibility,
            updateNavigationButtons = browserShellUiController::updateNavigationButtons,
            addHistoryEntry = {},
            injectPageFeatures = pageFeatureInjectionController::injectPageFeatures,
            onPageMetadataChanged = onPrivatePageMetadataChanged
        )
        val privateBrowsingSwitchController = PrivateBrowsingSwitchController(
            activity = activity,
            isPrivateBrowsingActive = isPrivateBrowsingActive,
            closeFunctionCenter = { closeFunctionCenter() },
            cancelElementPickerIfActive = cancelElementPickerIfActive,
            exitPageFullscreenIfNeeded = exitPageFullscreenIfNeeded,
            sessionSitePermissionStore = sessionSitePermissionStore,
            browserSessionCoordinator = browserSessionCoordinator,
            privateSessionController = privateSessionController,
            standardSessionController = standardSessionController,
            openHomePage = openHomePage,
            updatePrivateBrowsingUi = browsingModeThemeController::updatePrivateBrowsingUi,
            updateNavigationButtons = browserShellUiController::updateNavigationButtons
        )
        val browserTabActionsController = BrowserTabActionsController(
            standardTabStore = standardTabStore,
            privateTabStore = privateTabStore,
            standardTabWebViews = standardTabWebViews,
            standardSessionController = standardSessionController,
            isPrivateBrowsingActive = isPrivateBrowsingActive,
            createStandardTabWebView = createStandardTabWebView,
            showStandardTabWebView = showStandardTabWebView,
            hideStandardTabWebView = hideStandardTabWebView,
            destroyStandardTabWebView = destroyStandardTabWebView,
            closeFunctionCenter = closeFunctionCenter,
            saveStandardTabSession = saveStandardTabSession,
            loadUrl = loadUrl,
            openHomePage = openHomePage
        )
        return BrowserSessionComponents(
            standardSessionController = standardSessionController,
            privateSessionController = privateSessionController,
            privateBrowsingSwitchController = privateBrowsingSwitchController,
            browserTabActionsController = browserTabActionsController
        )
    }
}
