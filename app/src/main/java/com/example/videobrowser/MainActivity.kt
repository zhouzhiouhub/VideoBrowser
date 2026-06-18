package com.example.videobrowser

/**
 * 应用主界面入口。
 *
 * 这个文件负责把“浏览器 App 的各个模块”接到 Android Activity 生命周期上：
 * - 视图层：地址栏、底部按钮、首页区域、全屏容器。
 * - 浏览器层：WebView 创建、页面加载、标签页、网页权限、错误页。
 * - 内容增强：广告拦截、规则清理、JavaScript 注入、元素选择器。
 * - 业务入口：功能中心、下载、本地文件、收藏/历史、原生播放器。
 *
 * 阅读建议：
 * 1. 先看 onCreate()，它展示所有模块如何被创建和连接。
 * 2. 再按下面的“region”分区阅读，例如标签页、权限、导航、站点安全。
 * 3. 遇到具体业务时跳到对应包，例如 browser、video、download、functioncenter。
 */
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.browser.BrowserActivityFeatureAssemblyController
import com.example.videobrowser.browser.BrowserActivityFeatureComponents
import com.example.videobrowser.browser.BrowserActivityScaffoldAssemblyController
import com.example.videobrowser.browser.BrowserActivityScaffoldComponents
import com.example.videobrowser.browser.BrowserWebViewDebugController
import com.example.videobrowser.utils.DensityPixelConverter

/**
 * VideoBrowser 的主 Activity。
 *
 * Activity 是 Android 应用里的“屏幕控制器”。MainActivity 不直接实现所有业务细节，
 * 而是把 WebView、设置、功能中心、下载、播放等控制器组合起来，并处理必须留在
 * Activity 层的系统回调，例如权限申请、文件选择、页面生命周期和返回键。
 */
class MainActivity : AppCompatActivity() {

    // region 视图引用
    // views 来自 MainActivityViews.bind(this)，集中保存 activity_main.xml 中的控件。
    private lateinit var views: MainActivityViews
    // endregion

    // region 应用级控制器和仓库
    // Repository 负责读写本机数据；Controller 负责连接 UI、WebView 和业务动作。
    // 这些 lateinit 属性会在 onCreate() 里按依赖顺序初始化。
    private lateinit var browserFeatures: BrowserActivityFeatureComponents
    private val browserActivityScaffold: BrowserActivityScaffoldComponents =
        BrowserActivityScaffoldAssemblyController(
            activity = this,
            browserCoreFeatures = {
                if (::browserFeatures.isInitialized) {
                    browserFeatures.browserCoreFeatures
                } else {
                    null
                }
            },
            browserRuntimeFeatures = {
                if (::browserFeatures.isInitialized) {
                    browserFeatures.browserRuntimeFeatures
                } else {
                    null
                }
            },
            browserStartupFeatures = {
                if (::browserFeatures.isInitialized) {
                    browserFeatures.browserStartupFeatures
                } else {
                    null
                }
            }
        ).create()
    // endregion

    // region 生命周期
    /**
     * Android 创建主界面时调用。
     *
     * 这个函数很长，但可以按“创建依赖 -> 连接控制器 -> 配置 WebView -> 打开初始页面”理解。
     * 后续如果要排查启动问题，通常从这里的初始化顺序开始看。
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Debug 包开启 WebView 远程调试，方便在 Chrome DevTools 里查看网页和注入脚本。
        BrowserWebViewDebugController(applicationInfo.flags).enableForDebuggableBuild()
        setContentView(R.layout.activity_main)

        // 先绑定界面控件，再创建依赖这些控件的控制器。
        views = MainActivityViews.bind(this)
        browserFeatures = BrowserActivityFeatureAssemblyController(
            activity = this,
            intent = intent,
            assets = assets,
            filesDir = filesDir,
            views = views,
            decorView = window.decorView,
            activityScaffold = browserActivityScaffold,
            nativeBridgeName = NATIVE_BRIDGE_NAME,
            logTag = RULE_LOG_TAG,
            recreateActivity = { recreate() },
            postToUi = { action -> runOnUiThread { action() } },
            dp = ::dp
        ).create()
    }

    /**
     * 函数 `onNewIntent`：处理 `on New Intent` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param intent 参数类型为 `Intent`，表示函数执行 `intent` 相关逻辑时需要读取或处理的输入。
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        browserActivityScaffold.browserActivityLifecycleController.handleNewIntent(
            intent = intent,
            setActivityIntent = { newIntent -> setIntent(newIntent) }
        )
    }

    /**
     * 函数 `onPause`：处理 `on Pause` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    override fun onPause() {
        browserActivityScaffold.browserActivityLifecycleController.handlePause()
        super.onPause()
    }

    /**
     * 函数 `onResume`：处理 `on Resume` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    override fun onResume() {
        super.onResume()
        browserActivityScaffold.browserActivityLifecycleController.handleResume()
    }

    /**
     * 函数 `dispatchKeyEvent`：封装 `dispatch Key Event` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param event 参数类型为 `KeyEvent`，表示函数执行 `event` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            browserActivityScaffold.browserRuntimeStateController.wakeVideoFullscreenControlsIfActive()
        }
        return super.dispatchKeyEvent(event)
    }

    /**
     * 函数 `onDestroy`：处理 `on Destroy` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    override fun onDestroy() {
        browserActivityScaffold.browserActivityLifecycleController.handleDestroy()
        super.onDestroy()
    }

    // endregion

    // region WebView、ChromeClient 和 BrowserClient 组装
    // 这一组函数负责创建 WebView、绑定 WebChromeClient/WebViewClient，
    // 并处理网页弹窗、新窗口、渲染进程退出、证书和 HTTP 认证等浏览器外壳能力。
    // endregion

    // region 地址解析、页面加载和站点安全提示
    // 地址栏输入先被解析为 URL 或搜索词；真正加载前还会经过媒体路由、HTTP 降级确认和规则清理。
    // endregion

    // region 小工具函数和 WebView 跳转拦截
    // 这里放跨多个小流程复用的辅助函数，例如 dp 转换、键盘隐藏、URL 类型判断和 shouldOverrideUrlLoading 判断。
    /**
     * 函数 `dp`：封装 `dp` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param value 参数类型为 `Int`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun dp(value: Int): Int {
        return DensityPixelConverter.truncateDp(value, resources)
    }

    // endregion

    companion object {
        // 所有只在 MainActivity 内使用的常量集中放在 companion object，避免魔法数字散落在函数里。
        private const val NATIVE_BRIDGE_NAME = "VideoBrowserNative"
        private const val RULE_LOG_TAG = "VideoBrowserRules"
    }
}
