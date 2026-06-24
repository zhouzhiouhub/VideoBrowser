package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器外壳 UI 模块”。
 * BrowserControlsController 负责具体按钮和进度条；BrowserControlsShellController 负责控制栏显示关系；
 * 本类把 MainActivity 中分散的外壳刷新入口集中起来。
 * 阅读顺序：先看构造参数了解它拿到哪些 UI 控制器，再看 showHomeContent() 如何统一刷新主页/浏览器内容状态。
 */
import android.graphics.Color
import android.view.View

/**
 * 浏览器外壳 UI 控制器。
 *
 * 这个类只协调已经存在的小控制器，不直接处理导航、标签页、下载或站点规则。
 * 由于 MainActivity 初始化顺序较长，可能较晚创建的依赖使用可空 provider 延迟获取。
 *
 * @param browserControlsController 参数类型为 `() -> BrowserControlsController?`，表示返回浏览器按钮和进度条控制器的函数，尚未初始化时返回 null。
 * @param siteSecurityController 参数类型为 `() -> SiteSecurityController?`，表示返回站点安全图标控制器的函数，尚未初始化时返回 null。
 * @param browserControlsScrollController 参数类型为 `() -> BrowserControlsScrollController?`，表示返回滚动隐藏控制器的函数，尚未初始化时返回 null。
 * @param browserControlsShellController 参数类型为 `BrowserControlsShellController`，表示浏览器控制栏外壳协调器，用来隐藏/显示控制栏和搜索入口。
 * @param rootView 参数类型为 `View`，表示主界面根布局，首页状态会用它重新约束搜索栏位置。
 * @param topBar 参数类型为 `View`，表示顶部地址栏区域，首页状态会把它作为居中搜索栏显示。
 * @param bottomBar 参数类型为 `View`，表示底部工具栏，首页状态会保留轻量入口。
 * @param activeWebView 参数类型为 `() -> View`，表示返回当前活动 WebView 的函数，首页状态会隐藏它，显示网页内容时会把它设为可见。
 * @param browsingModeThemeController 参数类型为 `BrowsingModeThemeController`，表示普通/无痕主题控制器，用于主页状态变化后重新应用颜色。
 */
class BrowserShellUiController(
    private val browserControlsController: () -> BrowserControlsController?,
    private val siteSecurityController: () -> SiteSecurityController?,
    private val browserControlsScrollController: () -> BrowserControlsScrollController?,
    private val browserControlsShellController: BrowserControlsShellController,
    private val rootView: View,
    private val topBar: View,
    private val bottomBar: View,
    private val activeWebView: () -> View,
    private val browsingModeThemeController: BrowsingModeThemeController
) {
    /**
     * 初始化浏览器控制栏和站点安全图标。
     *
     * @return 无返回值；某个依赖尚未初始化时跳过对应 setup。
     */
    fun setupBrowserControls() {
        browserControlsController()?.setup()
        siteSecurityController()?.setup()
    }

    /**
     * 刷新底部导航按钮状态。
     *
     * @return 无返回值；浏览器控制器尚未初始化时不做任何操作。
     */
    fun updateNavigationButtons() {
        browserControlsController()?.updateNavigationButtons()
    }

    /**
     * 刷新当前页面的收藏按钮状态。
     *
     * @return 无返回值；浏览器控制器尚未初始化时不做任何操作。
     */
    fun updateBookmarkButton() {
        browserControlsController()?.updateBookmarkButton()
    }

    /**
     * 按首页状态刷新 WebView 内容区和浏览器外壳。
     *
     * @param show 参数类型为 `Boolean`，true 表示当前显示首页内容，false 表示当前显示 WebView 页面内容。
     * @return 无返回值；函数会重置滚动隐藏状态、显示控制栏、隐藏或显示 WebView、刷新搜索入口/进度条/导航按钮和主题。
     */
    fun showHomeContent(show: Boolean) {
        browserControlsScrollController()?.resetTracking()
        browserControlsShellController.setBrowserControlsHidden(false)
        browserControlsShellController.syncSearchProviderVisibility()
        activeWebView().visibility = if (show) View.GONE else View.VISIBLE
        browserControlsShellController.updatePageProgressVisibility(forceHidden = show)
        updateNavigationButtons()
        browsingModeThemeController.applyBrowsingModeTheme()
        applyHomeChrome(show)
    }

    private fun applyHomeChrome(show: Boolean) {
        bottomBar.visibility = View.VISIBLE
        if (show) {
            topBar.setBackgroundColor(Color.TRANSPARENT)
            topBar.post { positionSearchBarForHome() }
        } else {
            topBar.translationY = 0f
        }
    }

    private fun positionSearchBarForHome() {
        val targetCenterY = rootView.height * HOME_SEARCH_VERTICAL_BIAS
        val currentCenterY = topBar.top + topBar.height / 2f
        topBar.translationY = targetCenterY - currentCenterY
    }

    private companion object {
        private const val HOME_SEARCH_VERTICAL_BIAS = 0.42f
    }
}
