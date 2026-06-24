package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器 client 装配模块”。
 * 文件名 BrowserClientAssemblyController 可以拆开理解为“Browser Client Assembly Controller”，
 * 表示它只负责创建 WebViewClient、ChromeClient、网页新窗口和渲染进程恢复相关控制器。
 * 阅读顺序：先看 BrowserClientComponents 知道返回哪些对象，再看 create() 中 BrowserClient 和 ChromeClient 的依赖如何连接。
 */
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.adblock.AdBlockRequestInterceptor

/**
 * 浏览器 client 组件集合。
 *
 * @param renderProcessRecoveryController 参数类型为 `RenderProcessRecoveryController`，表示 WebView 渲染进程退出后的恢复控制器。
 * @param browserWebClientController 参数类型为 `BrowserWebClientController`，表示创建并绑定 BrowserClient 的控制器。
 * @param webWindowController 参数类型为 `WebWindowController`，表示处理网页新窗口和关闭窗口请求的控制器。
 * @param browserChromeClientController 参数类型为 `BrowserChromeClientController`，表示创建并绑定标准/无痕 ChromeClient 的控制器。
 */
data class BrowserClientComponents(
    val renderProcessRecoveryController: RenderProcessRecoveryController,
    val browserWebClientController: BrowserWebClientController,
    val webWindowController: WebWindowController,
    val browserChromeClientController: BrowserChromeClientController
)

/**
 * 浏览器 client 装配控制器。
 *
 * BrowserClient 负责页面生命周期、请求拦截、认证和渲染进程恢复；ChromeClient 负责网页全屏、
 * 文件选择、网页权限、定位和新窗口。本类把这两条 WebView 回调链路集中创建。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示创建 ChromeClient 和相关 UI 回调的宿主 Activity。
 * @param fullscreenContainer 参数类型为 `FrameLayout`，表示网页自定义全屏视图的容器。
 * @param decorView 参数类型为 `View`，表示 Activity 顶层装饰视图，用来设置网页全屏系统栏标记。
 * @param webViewContainer 参数类型为 `ViewGroup`，表示承载标准/无痕 WebView 的父容器。
 * @param standardTabStore 参数类型为 `BrowserTabStore`，表示标准模式标签页数据源。
 * @param standardTabWebViews 参数类型为 `BrowserTabWebViewRegistry<WebView>`，表示标准标签页 ID 到 WebView 的映射表。
 * @param browserSessionCoordinator 参数类型为 `BrowserSessionCoordinator`，表示标准/无痕 WebView 切换协调器。
 * @param standardSessionController 参数类型为 `BrowserSessionController`，表示标准浏览模式页面状态控制器。
 * @param privateSessionController 参数类型为 `BrowserSessionController`，表示无痕浏览模式页面状态控制器。
 * @param browserManager 参数类型为 `() -> BrowserManager`，表示读取当前 active BrowserManager 的回调。
 * @param sessionController 参数类型为 `() -> BrowserSessionController`，表示读取当前浏览模式页面状态控制器的回调。
 * @param currentPageUrl 参数类型为 `() -> String?`，表示读取当前页面 URL 的回调，用来构造渲染进程错误页。
 * @param isPrivateBrowsingActive 参数类型为 `() -> Boolean`，表示读取当前是否处于无痕模式的回调。
 * @param createStandardTabWebView 参数类型为 `() -> WebView`，表示创建标准标签页 WebView 的回调。
 * @param showStandardTabWebView 参数类型为 `(WebView, Boolean) -> Unit`，表示展示标准标签页 WebView 的回调。
 * @param saveStandardTabSession 参数类型为 `() -> Unit`，表示保存标准标签页会话的回调。
 * @param showBrowserErrorPage 参数类型为 `(BrowserPageError) -> Unit`，表示把渲染恢复失败显示为浏览器错误页的回调。
 * @param resetBackExitConfirmation 参数类型为 `() -> Unit`，表示页面开始加载时重置“再次返回退出”状态的回调。
 * @param clientCertificateController 参数类型为 `ClientCertificateController`，表示处理客户端证书请求的控制器。
 * @param httpAuthController 参数类型为 `HttpAuthController`，表示处理 HTTP Basic Auth 请求的控制器。
 * @param adBlockRequestInterceptor 参数类型为 `AdBlockRequestInterceptor`，表示广告拦截请求处理器。
 * @param smartNoImageRequestInterceptor 参数类型为 `SmartNoImageRequestInterceptor`，表示智能无图请求处理器。
 * @param browserNavigationController 参数类型为 `BrowserNavigationController`，表示判断 URL 是否应拦截或外部打开的导航控制器。
 * @param pageFeatureVisibilityController 参数类型为 `BrowserPageFeatureVisibilityController`，表示页面增强首屏遮罩控制器。
 * @param closeFunctionCenter 参数类型为 `() -> Boolean`，表示网页新窗口打开标签页前关闭功能中心的回调。
 * @param closeTab 参数类型为 `(Long) -> Unit`，表示网页关闭窗口时关闭对应标签页的回调。
 * @param fullscreenChanged 参数类型为 `(Boolean) -> Unit`，表示网页进入或退出全屏时同步全屏 UI 的回调。
 * @param webFileChooserController 参数类型为 `WebFileChooserController`，表示网页文件上传选择器控制器。
 * @param webPermissionRequestController 参数类型为 `WebPermissionRequestController`，表示网页相机/麦克风权限控制器。
 * @param geolocationPermissionController 参数类型为 `GeolocationPermissionController`，表示网页定位权限控制器。
 */
class BrowserClientAssemblyController(
    private val activity: AppCompatActivity,
    private val fullscreenContainer: FrameLayout,
    private val decorView: View,
    private val webViewContainer: ViewGroup,
    private val standardTabStore: BrowserTabStore,
    private val standardTabWebViews: BrowserTabWebViewRegistry<WebView>,
    private val browserSessionCoordinator: BrowserSessionCoordinator,
    private val standardSessionController: BrowserSessionController,
    private val privateSessionController: BrowserSessionController,
    private val browserManager: () -> BrowserManager,
    private val sessionController: () -> BrowserSessionController,
    private val currentPageUrl: () -> String?,
    private val isPrivateBrowsingActive: () -> Boolean,
    private val createStandardTabWebView: () -> WebView,
    private val showStandardTabWebView: (WebView, Boolean) -> Unit,
    private val saveStandardTabSession: () -> Unit,
    private val showBrowserErrorPage: (BrowserPageError) -> Unit,
    private val resetBackExitConfirmation: () -> Unit,
    private val clientCertificateController: ClientCertificateController,
    private val httpAuthController: HttpAuthController,
    private val adBlockRequestInterceptor: AdBlockRequestInterceptor,
    private val smartNoImageRequestInterceptor: SmartNoImageRequestInterceptor,
    private val browserNavigationController: BrowserNavigationController,
    private val pageFeatureVisibilityController: BrowserPageFeatureVisibilityController,
    private val closeFunctionCenter: () -> Boolean,
    private val closeTab: (Long) -> Unit,
    private val fullscreenChanged: (Boolean) -> Unit,
    private val webFileChooserController: WebFileChooserController,
    private val webPermissionRequestController: WebPermissionRequestController,
    private val geolocationPermissionController: GeolocationPermissionController
) {
    /**
     * 创建浏览器 client 组件集合。
     *
     * @return 返回 `BrowserClientComponents`，调用方把其中对象保存到 MainActivity 字段后供生命周期和启动流程使用。
     */
    fun create(): BrowserClientComponents {
        val renderProcessRecoveryController = RenderProcessRecoveryController(
            webViewContainer = webViewContainer,
            sessionCoordinator = browserSessionCoordinator,
            standardTabWebViews = standardTabWebViews,
            currentPageUrl = currentPageUrl,
            isPrivateBrowsingActive = isPrivateBrowsingActive,
            createStandardTabWebView = createStandardTabWebView,
            showStandardTabWebView = showStandardTabWebView,
            saveStandardTabSession = saveStandardTabSession,
            showBrowserErrorPage = showBrowserErrorPage
        )
        val browserWebClientController = BrowserWebClientController(
            browserManager = browserManager,
            sessionController = sessionController,
            resetBackExitConfirmation = resetBackExitConfirmation,
            renderProcessRecoveryController = renderProcessRecoveryController,
            clientCertificateController = clientCertificateController,
            httpAuthController = httpAuthController,
            adBlockRequestInterceptor = adBlockRequestInterceptor,
            smartNoImageRequestInterceptor = smartNoImageRequestInterceptor,
            pageFeatureVisibilityController = pageFeatureVisibilityController,
            shouldBlockUrl = browserNavigationController::shouldBlockUrl
        )
        val webWindowController = WebWindowController(
            isPrivateBrowsingActive = isPrivateBrowsingActive,
            standardTabStore = standardTabStore,
            standardTabWebViews = standardTabWebViews,
            standardSessionController = standardSessionController,
            closeFunctionCenter = closeFunctionCenter,
            saveStandardTabSession = saveStandardTabSession,
            closeTab = closeTab
        )
        val browserChromeClientController = BrowserChromeClientController(
            activity = activity,
            fullscreenContainer = fullscreenContainer,
            decorView = decorView,
            standardSessionController = standardSessionController,
            privateSessionController = privateSessionController,
            browserManager = browserManager,
            isPrivateBrowsingActive = isPrivateBrowsingActive,
            fullscreenChanged = fullscreenChanged,
            webFileChooserController = webFileChooserController,
            webPermissionRequestController = webPermissionRequestController,
            geolocationPermissionController = geolocationPermissionController,
            webWindowController = webWindowController
        )
        return BrowserClientComponents(
            renderProcessRecoveryController = renderProcessRecoveryController,
            browserWebClientController = browserWebClientController,
            webWindowController = webWindowController,
            browserChromeClientController = browserChromeClientController
        )
    }
}
