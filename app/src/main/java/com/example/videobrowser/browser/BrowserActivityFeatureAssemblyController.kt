package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“Activity feature 启动管线模块”。
 * 文件名 BrowserActivityFeatureAssemblyController 可以拆开理解为“Browser Activity Feature Assembly Controller”，
 * 表示它只负责按顺序创建核心 feature、运行期 feature 和启动收尾 feature。
 * 阅读顺序：先看 BrowserActivityFeatureComponents 了解会返回哪些组件，再看 create() 中的三段装配顺序。
 */
import android.content.Intent
import android.content.res.AssetManager
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.MainActivityViews
import java.io.File

/**
 * 浏览器 Activity feature 组件集合。
 *
 * @param browserCoreFeatures 参数类型为 `BrowserCoreFeatureComponents`，表示浏览器外壳、持久化、搜索、WebView surface、导航和页面动作等核心组件。
 * @param browserRuntimeFeatures 参数类型为 `BrowserRuntimeFeatureComponents`，表示 Web 请求、控制栏、会话、Client 和全屏等运行期交互组件。
 * @param browserStartupFeatures 参数类型为 `BrowserStartupFeatureComponents`，表示功能中心、站点安全、页面增强和后退导航等启动收尾组件。
 */
data class BrowserActivityFeatureComponents(
    val browserCoreFeatures: BrowserCoreFeatureComponents,
    val browserRuntimeFeatures: BrowserRuntimeFeatureComponents,
    val browserStartupFeatures: BrowserStartupFeatureComponents
)

/**
 * 浏览器 Activity feature 启动管线装配控制器。
 *
 * MainActivity 只负责 Android 生命周期入口；本类集中执行“核心 -> 运行期 -> 启动收尾”的装配顺序。
 * 三组 feature 之间存在少量互相延迟读取的回调，因此这里使用局部 lateinit 变量保持原来的初始化时机。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示创建各 feature 控制器时使用的宿主 Activity。
 * @param intent 参数类型为 `Intent`，表示 Activity 启动 Intent，最终交给启动收尾流程打开初始页面。
 * @param assets 参数类型为 `AssetManager`，表示规则、脚本和资源读取时使用的资源入口。
 * @param filesDir 参数类型为 `File`，表示规则缓存、导出文件和页面归档使用的应用私有目录。
 * @param views 参数类型为 `MainActivityViews`，表示 activity_main.xml 中主界面控件的绑定集合。
 * @param decorView 参数类型为 `View`，表示 Activity 窗口的 decorView，用于 ChromeClient 和全屏 UI 操作。
 * @param activityScaffold 参数类型为 `BrowserActivityScaffoldComponents`，表示生命周期、Activity Result 和会话状态等脚手架组件。
 * @param nativeBridgeName 参数类型为 `String`，表示注入 WebView 的 JavaScript native bridge 名称。
 * @param logTag 参数类型为 `String`，表示本地文件和规则日志使用的日志标签。
 * @param recreateActivity 参数类型为 `() -> Unit`，表示恢复默认设置等场景下重建 Activity 的回调。
 * @param postToUi 参数类型为 `(() -> Unit) -> Unit`，表示把网页线程回调切回 Android UI 线程执行的函数。
 * @param dp 参数类型为 `(Int) -> Int`，表示把 dp 数值转换成当前设备像素值的函数。
 */
class BrowserActivityFeatureAssemblyController(
    private val activity: AppCompatActivity,
    private val intent: Intent,
    private val assets: AssetManager,
    private val filesDir: File,
    private val views: MainActivityViews,
    private val decorView: View,
    private val activityScaffold: BrowserActivityScaffoldComponents,
    private val nativeBridgeName: String,
    private val logTag: String,
    private val recreateActivity: () -> Unit,
    private val postToUi: (() -> Unit) -> Unit,
    private val dp: (Int) -> Int
) {
    /**
     * 创建三组浏览器 feature 并执行启动收尾流程。
     *
     * @return 返回 `BrowserActivityFeatureComponents`，调用方保存后供脚手架 provider 和生命周期回调读取。
     */
    fun create(): BrowserActivityFeatureComponents {
        var browserRuntimeFeatures: BrowserRuntimeFeatureComponents? = null
        var browserStartupFeatures: BrowserStartupFeatureComponents? = null

        val browserCoreFeatures = BrowserCoreFeatureAssemblyController(
            activity = activity,
            assets = assets,
            filesDir = filesDir,
            views = views,
            browserTabState = activityScaffold.browserTabState,
            browserSessionStateController = activityScaffold.browserSessionStateController,
            browserRuntimeStateController = activityScaffold.browserRuntimeStateController,
            browserChromeClientStateController =
                activityScaffold.browserChromeClientStateController,
            activityResultLaunchers = activityScaffold.activityResultLaunchers,
            findInPageController = activityScaffold.findInPageController,
            browserRuntimeFeatures = { browserRuntimeFeatures },
            browserStartupFeatures = { browserStartupFeatures },
            logTag = logTag,
            recreateActivity = recreateActivity,
            dp = dp
        ).create()
        activityScaffold.bindCoreFeatures(browserCoreFeatures)

        val requestInterceptionProvider = BrowserRequestInterceptionProvider(
            browserFeatureStateController = {
                browserCoreFeatures.browserShell.browserFeatureStateController
            },
            settingsManager = { browserCoreFeatures.browserPersistence.settingsManager },
            browserSessionStateController = { activityScaffold.browserSessionStateController },
            browserUrlStateController = {
                browserCoreFeatures.browserShell.browserUrlStateController
            },
            ruleEngine = { browserCoreFeatures.browserNavigation.ruleEngine }
        )

        val createdRuntimeFeatures = BrowserRuntimeFeatureAssemblyController(
            activity = activity,
            views = views,
            decorView = decorView,
            browserPersistence = browserCoreFeatures.browserPersistence,
            browserSurface = browserCoreFeatures.browserSurface,
            browserShell = browserCoreFeatures.browserShell,
            browserSearch = browserCoreFeatures.browserSearch,
            browserNavigation = browserCoreFeatures.browserNavigation,
            pageActions = browserCoreFeatures.pageActions,
            browserTabState = activityScaffold.browserTabState,
            sessionSitePermissionStore = activityScaffold.sessionSitePermissionStore,
            browserSessionStateController = activityScaffold.browserSessionStateController,
            browserRuntimeStateController = activityScaffold.browserRuntimeStateController,
            browserChromeClientStateController =
                activityScaffold.browserChromeClientStateController,
            requestInterceptionProvider = requestInterceptionProvider,
            activityResultLaunchers = activityScaffold.activityResultLaunchers,
            findInPageController = activityScaffold.findInPageController,
            browserStartupFeatures = { browserStartupFeatures },
            recreateActivity = recreateActivity,
            dp = dp
        ).create()
        browserRuntimeFeatures = createdRuntimeFeatures
        activityScaffold.bindRuntimeFeatures(createdRuntimeFeatures)

        val createdStartupFeatures = BrowserStartupFeatureAssemblyController(
            activity = activity,
            intent = intent,
            assets = assets,
            filesDir = filesDir,
            views = views,
            browserPersistence = browserCoreFeatures.browserPersistence,
            browserSurface = browserCoreFeatures.browserSurface,
            browserShell = browserCoreFeatures.browserShell,
            browserSessions = createdRuntimeFeatures.browserSessions,
            browserSearch = browserCoreFeatures.browserSearch,
            browserNavigation = browserCoreFeatures.browserNavigation,
            pageActions = browserCoreFeatures.pageActions,
            browserClients = createdRuntimeFeatures.browserClients,
            browserFullscreen = createdRuntimeFeatures.browserFullscreen,
            requestInterceptionProvider = requestInterceptionProvider,
            activityResultLaunchers = activityScaffold.activityResultLaunchers,
            localFiles = browserCoreFeatures.localFiles,
            browserSessionStateController = activityScaffold.browserSessionStateController,
            browserRuntimeStateController = activityScaffold.browserRuntimeStateController,
            browserChromeClientStateController =
                activityScaffold.browserChromeClientStateController,
            nativeBridgeName = nativeBridgeName,
            recreateActivity = recreateActivity,
            postToUi = postToUi
        ).start()
        browserStartupFeatures = createdStartupFeatures
        activityScaffold.bindStartupFeatures(createdStartupFeatures)

        return BrowserActivityFeatureComponents(
            browserCoreFeatures = browserCoreFeatures,
            browserRuntimeFeatures = createdRuntimeFeatures,
            browserStartupFeatures = createdStartupFeatures
        )
    }
}
