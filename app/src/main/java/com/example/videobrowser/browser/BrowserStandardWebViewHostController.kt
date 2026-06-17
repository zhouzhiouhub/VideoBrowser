package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“标准浏览 WebView 宿主模块”。
 * 文件名 BrowserStandardWebViewHostController 可以拆开理解为“Browser Standard WebView Host Controller”，
 * 表示它只负责普通浏览模式下 WebView 的创建、展示、隐藏、销毁，以及和标签页映射表的连接。
 * 阅读顺序：先看构造参数了解宿主需要哪些 Android 视图和回调，再看 setup()，最后看
 * create/show/hide/destroy 这一组标签页 WebView 生命周期函数。
 */
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

/**
 * 标准浏览 WebView 宿主控制器。
 *
 * MainActivity 只负责把本类创建出来并把公开对象交给其它控制器；标准标签页 WebView 的
 * 具体创建、切换、隐藏和销毁细节集中在这里，避免 Activity 继续膨胀。
 *
 * @param activity 当前 Activity，用于创建新的标准标签页 WebView。
 * @param webViewContainer 承载标准/无痕 WebView 的父容器，切换标签页时会向其中添加或移除 WebView。
 * @param standardTabStore 标准模式标签页数据源，用于把标签页 ID 映射到对应 WebView。
 * @param initialStandardWebView 布局里声明的初始标准 WebView，作为第一个标准标签页使用。
 * @param configureLinkContextMenu 为指定 WebView 配置长按链接/图片菜单的函数。
 * @param handleActiveWebViewChanged 通知浏览器外壳当前 active WebView 变化的函数，参数分别是新的 WebView 和浏览模式。
 */
class BrowserStandardWebViewHostController(
    private val activity: AppCompatActivity,
    private val webViewContainer: FrameLayout,
    private val standardTabStore: BrowserTabStore,
    private val initialStandardWebView: WebView,
    private val configureLinkContextMenu: (WebView) -> Unit,
    private val handleActiveWebViewChanged: (WebView, BrowserMode) -> Unit
) {
    lateinit var standardWebView: WebView
        private set

    lateinit var browserManager: BrowserManager
        private set

    lateinit var standardTabWebViews: BrowserTabWebViewRegistry<WebView>
        private set

    lateinit var sessionCoordinator: BrowserSessionCoordinator
        private set

    /**
     * 初始化标准 WebView、BrowserManager、标签页 WebView 映射表和普通/无痕切换协调器。
     *
     * @return 无返回值；后续其它控制器会通过公开属性读取初始化后的对象。
     */
    fun setup() {
        if (::browserManager.isInitialized) {
            return
        }

        standardWebView = initialStandardWebView
        configureLinkContextMenu(standardWebView)
        browserManager = BrowserManager(standardWebView)
        standardTabWebViews = BrowserTabWebViewRegistry(
            tabs = standardTabStore,
            initialView = standardWebView,
            createWebView = ::createStandardTabWebView,
            showWebView = ::showStandardTabWebView,
            hideWebView = ::hideStandardTabWebView,
            destroyWebView = ::destroyStandardTabWebView
        )
        sessionCoordinator = BrowserSessionCoordinator(
            activity = activity,
            webViewContainer = webViewContainer,
            standardWebView = standardWebView,
            browserManager = browserManager,
            onActiveWebViewChanged = handleActiveWebViewChanged
        )
    }

    /**
     * 为标准标签页创建新的 WebView。
     *
     * @return 返回已经设置布局参数、背景和初始隐藏状态的新 WebView。
     */
    fun createStandardTabWebView(): WebView {
        return WebView(activity).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            overScrollMode = standardWebView.overScrollMode
            setBackgroundColor(0x00000000)
            visibility = View.GONE
        }
    }

    /**
     * 把标准标签页 WebView 切到前台。
     *
     * @param tabWebView 要显示的标准标签页 WebView。
     * @return 无返回值；函数会把当前 BrowserManager 切换到该 WebView 并通知 active WebView 变化。
     */
    fun showStandardTabWebView(tabWebView: WebView) {
        showStandardTabWebView(tabWebView, detachCurrent = true)
    }

    /**
     * 把标准标签页 WebView 切到前台，并控制是否先解绑当前 WebView 的回调。
     *
     * @param tabWebView 要显示的标准标签页 WebView。
     * @param detachCurrent true 表示切换前先从当前 WebView 上解绑 ChromeClient、WebViewClient 和下载监听。
     * @return 无返回值；函数会同步容器、可见性、BrowserManager、sessionCoordinator 和 active WebView 状态。
     */
    fun showStandardTabWebView(tabWebView: WebView, detachCurrent: Boolean) {
        if (tabWebView.parent == null) {
            webViewContainer.addView(tabWebView)
        }
        tabWebView.visibility = View.VISIBLE
        sessionCoordinator.setStandardWebView(tabWebView)
        browserManager.switchWebView(
            nextWebView = tabWebView,
            privateBrowsingEnabled = false,
            detachCurrent = detachCurrent
        )
        handleActiveWebViewChanged(tabWebView, BrowserMode.STANDARD)
    }

    /**
     * 隐藏标准标签页 WebView。
     *
     * @param tabWebView 要隐藏的标准标签页 WebView。
     * @return 无返回值；函数只改变可见性，不销毁 WebView。
     */
    fun hideStandardTabWebView(tabWebView: WebView) {
        tabWebView.visibility = View.GONE
    }

    /**
     * 销毁标准标签页 WebView。
     *
     * @param tabWebView 要从容器移除并销毁的标准标签页 WebView。
     * @return 无返回值；函数会保留共享浏览数据，只销毁这个 WebView 实例。
     */
    fun destroyStandardTabWebView(tabWebView: WebView) {
        if (tabWebView.parent == webViewContainer) {
            webViewContainer.removeView(tabWebView)
        }
        browserManager.destroyWebView(tabWebView, clearSharedStores = false)
    }

    /**
     * 销毁宿主负责的全部 WebView。
     *
     * @return 无返回值；函数会先清理无痕临时 WebView，再销毁标准标签页映射表里的所有 WebView。
     */
    fun destroyAll() {
        if (::sessionCoordinator.isInitialized) {
            sessionCoordinator.destroyPrivateSession()
        }
        if (::standardTabWebViews.isInitialized) {
            standardTabWebViews.destroyAll(::destroyStandardTabWebView)
        } else if (::browserManager.isInitialized) {
            browserManager.destroy()
        }
    }

    /**
     * 返回当前标准浏览 BrowserManager。
     *
     * @return 当前负责 active WebView 的 BrowserManager。
     */
    fun currentBrowserManager(): BrowserManager {
        return browserManager
    }

    /**
     * 返回当前需要统一执行浏览器数据操作的 BrowserManager 列表。
     *
     * @return 包含标准浏览 BrowserManager 的列表；调用方按列表批量执行缓存清理等操作。
     */
    fun browserManagers(): List<BrowserManager> {
        return listOf(browserManager)
    }
}
