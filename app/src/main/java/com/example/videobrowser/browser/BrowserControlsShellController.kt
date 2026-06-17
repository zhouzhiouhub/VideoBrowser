package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 BrowserControlsShellController 可以拆开理解为“Browser Controls Shell Controller”，
 * 表示它只负责协调浏览器外壳控件之间的显示关系。
 * 主要职责：刷新页面进度条、设置滚动隐藏工具栏、同步搜索入口和地址建议可见性。
 * 阅读顺序：先看构造参数了解它会读取哪些控制器，再看每个公开函数对应 MainActivity 的哪类 UI 动作。
 */
import com.example.videobrowser.browser.search.AddressSuggestionController
import com.example.videobrowser.browser.search.SearchProviderController

/**
 * 浏览器控制栏外壳控制器。
 *
 * 这个类不直接持有 Android View，而是协调已经存在的控制器。MainActivity 初始化顺序比较长，
 * 因此各依赖用可空 provider 延迟取得；某个控制器尚未初始化时，对应动作会安全跳过。
 *
 * @param browserControlsController 返回浏览器按钮和进度条控制器的函数；未初始化时返回 null。
 * @param browserControlsScrollController 返回滚动隐藏工具栏控制器的函数；未初始化时返回 null。
 * @param searchProviderController 返回搜索引擎入口控制器的函数；未初始化时返回 null。
 * @param addressSuggestionController 返回地址建议控制器的函数；未初始化时返回 null。
 * @param isPageLoading 返回当前页面是否正在加载的函数，用于计算进度条可见性。
 * @param isVideoFullscreenUiActive 返回当前是否处于视频全屏 UI 的函数，用于隐藏搜索和进度条。
 * @param isHomePageVisible 返回当前是否显示首页内容的函数，用于隐藏搜索和进度条。
 */
class BrowserControlsShellController(
    private val browserControlsController: () -> BrowserControlsController?,
    private val browserControlsScrollController: () -> BrowserControlsScrollController?,
    private val searchProviderController: () -> SearchProviderController?,
    private val addressSuggestionController: () -> AddressSuggestionController?,
    private val isPageLoading: () -> Boolean,
    private val isVideoFullscreenUiActive: () -> Boolean,
    private val isHomePageVisible: () -> Boolean
) {
    /**
     * 根据页面加载状态和强制隐藏标记刷新进度条可见性。
     *
     * @param forceHidden true 表示无论页面是否加载都隐藏进度条。
     * @return 无返回值；浏览器控制器尚未初始化时不做任何操作。
     */
    fun updatePageProgressVisibility(forceHidden: Boolean = false) {
        browserControlsController()?.updatePageProgressVisibility(
            isPageLoading(),
            forceHidden
        )
    }

    /**
     * 初始化 WebView 滚动时自动隐藏或显示工具栏的逻辑。
     *
     * @return 无返回值；滚动控制器尚未初始化时不做任何操作。
     */
    fun setupWebViewScrollControls() {
        browserControlsScrollController()?.setup()
    }

    /**
     * 设置浏览器顶部和底部控制栏是否隐藏。
     *
     * @param hidden true 表示隐藏控制栏，false 表示显示控制栏。
     * @param allowDefer true 表示允许滚动控制器按自身节流策略延迟应用变化。
     * @return 无返回值；滚动控制器尚未初始化时不做任何操作。
     */
    fun setBrowserControlsHidden(hidden: Boolean, allowDefer: Boolean = true) {
        browserControlsScrollController()?.setControlsHidden(hidden, allowDefer)
    }

    /**
     * 同步搜索引擎入口和地址建议面板的可见性。
     *
     * @return 无返回值；搜索入口控制器尚未初始化时不做任何操作。
     */
    fun syncSearchProviderVisibility() {
        val controls = browserControlsController() ?: return
        val searchProviders = searchProviderController() ?: return
        searchProviders.syncVisibility(
            areBrowserControlsHidden = controls.areHidden,
            isVideoFullscreenUiActive = isVideoFullscreenUiActive(),
            isHomePageVisible = isHomePageVisible()
        )
        addressSuggestionController()?.syncVisibility()
    }

    /**
     * 处理地址栏焦点变化。
     *
     * @param hasFocus true 表示地址栏获得焦点，false 表示地址栏失去焦点。
     * @return 无返回值；地址建议控制器尚未初始化时不做任何操作。
     */
    fun handleAddressFocusChanged(hasFocus: Boolean) {
        addressSuggestionController()?.handleAddressFocusChanged(hasFocus)
    }

    /**
     * 初始化搜索引擎入口 UI。
     *
     * @return 无返回值；搜索入口控制器尚未初始化时不做任何操作。
     */
    fun setupSearchProviders() {
        searchProviderController()?.setup()
    }
}
