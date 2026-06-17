package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器后退导航装配模块”。
 * 文件名 BrowserBackNavigationAssemblyController 可以拆开理解为“Browser Back Navigation Assembly Controller”，
 * 表示它只负责创建统一处理系统返回键和底部后退按钮的控制器。
 * 阅读顺序：先看 create() 中哪些回调参与后退优先级，再去 BrowserBackNavigationController 看具体处理顺序。
 */
import androidx.appcompat.app.AppCompatActivity

/**
 * 浏览器后退导航装配控制器。
 *
 * 后退行为需要按优先级处理网页全屏、功能中心、元素选择器、WebView 历史和二次返回退出；
 * 本类只负责把这些外部入口传给 BrowserBackNavigationController。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示注册系统返回键回调和显示退出提示的宿主 Activity。
 * @param browserManager 参数类型为 `() -> BrowserManager`，表示读取当前 active BrowserManager 的回调。
 * @param currentChromeClient 参数类型为 `() -> ChromeClient?`，表示读取当前 ChromeClient 的回调，用来退出网页自定义全屏视图。
 * @param handleFunctionCenterBack 参数类型为 `() -> Boolean`，表示功能中心优先处理返回键的回调。
 * @param isElementPickerActive 参数类型为 `() -> Boolean`，表示读取元素选择器是否激活的回调。
 * @param cancelElementPicker 参数类型为 `() -> Unit`，表示取消元素选择器的回调。
 * @param updateNavigationButtons 参数类型为 `() -> Unit`，表示后退状态变化后刷新导航按钮的回调。
 */
class BrowserBackNavigationAssemblyController(
    private val activity: AppCompatActivity,
    private val browserManager: () -> BrowserManager,
    private val currentChromeClient: () -> ChromeClient?,
    private val handleFunctionCenterBack: () -> Boolean,
    private val isElementPickerActive: () -> Boolean,
    private val cancelElementPicker: () -> Unit,
    private val updateNavigationButtons: () -> Unit
) {
    /**
     * 创建浏览器后退导航控制器。
     *
     * @return 返回 `BrowserBackNavigationController`，调用方保存后供工具栏和启动流程使用。
     */
    fun create(): BrowserBackNavigationController {
        return BrowserBackNavigationController(
            activity = activity,
            browserManager = browserManager,
            currentChromeClient = currentChromeClient,
            handleFunctionCenterBack = handleFunctionCenterBack,
            isElementPickerActive = isElementPickerActive,
            cancelElementPicker = cancelElementPicker,
            updateNavigationButtons = updateNavigationButtons
        )
    }
}
