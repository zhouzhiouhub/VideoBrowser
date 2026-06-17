package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器 Activity 生命周期模块”。
 * 文件名 BrowserActivityLifecycleController 可以拆开理解为“Browser Activity Lifecycle Controller”，
 * 表示它只负责 Activity 暂停、恢复和销毁时需要通知的浏览器子模块。
 * 阅读顺序：先看构造参数了解它会管理哪些控制器，再看 handlePause()/handleResume()/handleDestroy()
 * 对应 Android 生命周期的三个阶段。
 */
import com.example.videobrowser.browser.search.AddressSuggestionController
import com.example.videobrowser.download.DownloadController
import com.example.videobrowser.element.ElementPickerController
import com.example.videobrowser.functioncenter.FunctionCenterEntryController

/**
 * 浏览器 Activity 生命周期控制器。
 *
 * MainActivity 仍然接收 Android 生命周期回调；本类集中处理这些回调里需要转发给各个控制器的清理动作。
 * 构造参数使用可空 provider，是为了兼容 Activity 初始化中途触发生命周期回调的情况。
 *
 * @param browserChromeClientController 返回 ChromeClient 装配控制器的函数，尚未初始化时返回 null。
 * @param browserWebClientController 返回 WebViewClient 装配控制器的函数，尚未初始化时返回 null。
 * @param pageArchiveController 返回页面归档控制器的函数，尚未初始化时返回 null。
 * @param addressSuggestionController 返回地址建议控制器的函数，尚未初始化时返回 null。
 * @param downloadController 返回下载控制器的函数，尚未初始化时返回 null。
 * @param elementPickerController 返回元素选择器控制器的函数，尚未初始化时返回 null。
 * @param functionCenterEntryController 返回功能中心入口控制器的函数，尚未初始化时返回 null。
 * @param browserChromeClientStateController ChromeClient 状态控制器，用于安全读取当前 ChromeClient。
 * @param browserStandardTabSessionController 返回标准标签页会话控制器的函数，尚未初始化时返回 null。
 * @param browserStandardWebViewHostController 返回标准 WebView 宿主控制器的函数，尚未初始化时返回 null。
 */
class BrowserActivityLifecycleController(
    private val browserChromeClientController: () -> BrowserChromeClientController?,
    private val browserWebClientController: () -> BrowserWebClientController?,
    private val pageArchiveController: () -> PageArchiveController?,
    private val addressSuggestionController: () -> AddressSuggestionController?,
    private val downloadController: () -> DownloadController?,
    private val elementPickerController: () -> ElementPickerController?,
    private val functionCenterEntryController: () -> FunctionCenterEntryController?,
    private val browserChromeClientStateController: BrowserChromeClientStateController,
    private val browserStandardTabSessionController: () -> BrowserStandardTabSessionController?,
    private val browserStandardWebViewHostController: () -> BrowserStandardWebViewHostController?
) {
    /**
     * 处理 Activity 暂停。
     *
     * @return 无返回值；函数会保存标准标签页会话，并暂停当前 WebView。
     */
    fun handlePause() {
        browserStandardTabSessionController()?.saveStandardTabSession()
        browserStandardWebViewHostController()?.currentBrowserManager()?.onPause()
    }

    /**
     * 处理 Activity 恢复。
     *
     * @return 无返回值；函数会恢复当前 WebView。
     */
    fun handleResume() {
        browserStandardWebViewHostController()?.currentBrowserManager()?.onResume()
    }

    /**
     * 处理 Activity 销毁。
     *
     * @return 无返回值；函数会取消挂起请求、释放控制器资源、保存标签页会话并销毁 WebView。
     */
    fun handleDestroy() {
        browserChromeClientController()?.cancelPendingWebFileChooser()
        browserChromeClientController()?.cancelPendingWebPermissionRequest()
        browserChromeClientController()?.cancelPendingGeolocationPermissionPrompt()
        browserWebClientController()?.cancelPendingHttpAuthRequest()
        browserWebClientController()?.cancelPendingClientCertRequest()
        pageArchiveController()?.dispose()
        addressSuggestionController()?.dispose()
        downloadController()?.dispose()
        elementPickerController()?.dispose()
        functionCenterEntryController()?.closeFunctionCenter()
        browserChromeClientStateController.currentChromeClientOrNull()?.hideCustomView()
        browserStandardTabSessionController()?.saveStandardTabSession()
        browserStandardWebViewHostController()?.destroyAll()
    }
}
