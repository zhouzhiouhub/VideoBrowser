package com.example.videobrowser.browser

import com.example.videobrowser.browser.search.AddressSuggestionController
import com.example.videobrowser.download.DownloadController
import com.example.videobrowser.element.ElementPickerController
import com.example.videobrowser.functioncenter.FunctionCenterEntryController

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器 Activity 生命周期装配模块”。
 * 文件名 BrowserActivityLifecycleAssemblyController 可以拆开理解为“Browser Activity Lifecycle Assembly Controller”，
 * 表示它只负责创建接收 Activity pause/resume/destroy/newIntent 转发的生命周期控制器。
 * 阅读顺序：先看 create() 如何保留可空 provider，再去 BrowserActivityLifecycleController 看每个生命周期阶段的清理动作。
 */
/**
 * Activity 生命周期控制器装配器。
 *
 * Activity 生命周期回调可能在初始化中途到达；本类把所有可选依赖的 provider 交给
 * BrowserActivityLifecycleController，让它只对已经初始化完成的控制器执行清理或保存动作。
 *
 * @param browserChromeClientController 参数类型为 `() -> BrowserChromeClientController?`，表示安全读取 ChromeClient 装配控制器的回调。
 * @param browserWebClientController 参数类型为 `() -> BrowserWebClientController?`，表示安全读取 WebViewClient 装配控制器的回调。
 * @param pageArchiveController 参数类型为 `() -> PageArchiveController?`，表示安全读取页面归档控制器的回调。
 * @param addressSuggestionController 参数类型为 `() -> AddressSuggestionController?`，表示安全读取地址建议控制器的回调。
 * @param downloadController 参数类型为 `() -> DownloadController?`，表示安全读取下载控制器的回调。
 * @param elementPickerController 参数类型为 `() -> ElementPickerController?`，表示安全读取元素选择器控制器的回调。
 * @param functionCenterEntryController 参数类型为 `() -> FunctionCenterEntryController?`，表示安全读取功能中心入口控制器的回调。
 * @param browserChromeClientStateController 参数类型为 `BrowserChromeClientStateController`，表示安全读取当前 ChromeClient 的状态控制器。
 * @param browserStandardTabSessionController 参数类型为 `() -> BrowserStandardTabSessionController?`，表示安全读取标准标签页会话控制器的回调。
 * @param browserStandardWebViewHostController 参数类型为 `() -> BrowserStandardWebViewHostController?`，表示安全读取标准 WebView 宿主控制器的回调。
 * @param browserLaunchController 参数类型为 `() -> BrowserLaunchController?`，表示安全读取浏览器启动导航控制器的回调。
 */
class BrowserActivityLifecycleAssemblyController(
    private val browserChromeClientController: () -> BrowserChromeClientController?,
    private val browserWebClientController: () -> BrowserWebClientController?,
    private val pageArchiveController: () -> PageArchiveController?,
    private val addressSuggestionController: () -> AddressSuggestionController?,
    private val downloadController: () -> DownloadController?,
    private val elementPickerController: () -> ElementPickerController?,
    private val functionCenterEntryController: () -> FunctionCenterEntryController?,
    private val browserChromeClientStateController: BrowserChromeClientStateController,
    private val browserStandardTabSessionController: () -> BrowserStandardTabSessionController?,
    private val browserStandardWebViewHostController: () -> BrowserStandardWebViewHostController?,
    private val browserLaunchController: () -> BrowserLaunchController?
) {
    /**
     * 创建 Activity 生命周期控制器。
     *
     * @return 返回 `BrowserActivityLifecycleController`，调用方在 Activity 生命周期方法中继续转发事件。
     */
    fun create(): BrowserActivityLifecycleController {
        return BrowserActivityLifecycleController(
            browserChromeClientController = browserChromeClientController,
            browserWebClientController = browserWebClientController,
            pageArchiveController = pageArchiveController,
            addressSuggestionController = addressSuggestionController,
            downloadController = downloadController,
            elementPickerController = elementPickerController,
            functionCenterEntryController = functionCenterEntryController,
            browserChromeClientStateController = browserChromeClientStateController,
            browserStandardTabSessionController = browserStandardTabSessionController,
            browserStandardWebViewHostController = browserStandardWebViewHostController,
            browserLaunchController = browserLaunchController
        )
    }
}
