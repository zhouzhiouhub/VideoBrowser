package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“Activity 脚手架装配模块”。
 * 文件名 BrowserActivityScaffoldAssemblyController 可以拆开理解为“Browser Activity Scaffold Assembly Controller”，
 * 表示它只负责创建不属于某个具体业务 feature、但 MainActivity 生命周期需要长期持有的控制器。
 * 阅读顺序：先看 BrowserActivityScaffoldComponents 了解会返回哪些对象，再看 create() 中如何用 provider 延迟读取三组 feature。
 */
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.settings.SessionSitePermissionStore

/**
 * 浏览器 Activity 脚手架组件集合。
 *
 * @param browserChromeClientStateController 参数类型为 `BrowserChromeClientStateController`，表示安全读取当前 ChromeClient 状态的控制器。
 * @param browserActivityLifecycleController 参数类型为 `BrowserActivityLifecycleController`，表示处理 Activity 生命周期回调的控制器。
 * @param browserTabState 参数类型为 `BrowserTabStateComponents`，表示标准/无痕标签页 store 和会话绑定组件。
 * @param findInPageController 参数类型为 `FindInPageController`，表示页内查找弹窗使用的查找控制器。
 * @param requestInterceptionProvider 参数类型为 `BrowserRequestInterceptionProvider`，表示广告拦截和请求拦截相关 provider。
 * @param activityResultLaunchers 参数类型为 `BrowserActivityResultLaunchers`，表示文件选择、权限申请、导入导出使用的 launcher 集合。
 * @param sessionSitePermissionStore 参数类型为 `SessionSitePermissionStore`，表示单次会话内记住的网站权限决定。
 * @param browserRuntimeStateController 参数类型为 `BrowserRuntimeStateController`，表示无痕、首页、全屏和默认 User-Agent 状态控制器。
 * @param browserSessionStateController 参数类型为 `BrowserSessionStateController`，表示读取当前浏览模式会话状态的控制器。
 */
data class BrowserActivityScaffoldComponents(
    val browserChromeClientStateController: BrowserChromeClientStateController,
    val browserActivityLifecycleController: BrowserActivityLifecycleController,
    val browserTabState: BrowserTabStateComponents,
    val findInPageController: FindInPageController,
    val requestInterceptionProvider: BrowserRequestInterceptionProvider,
    val activityResultLaunchers: BrowserActivityResultLaunchers,
    val sessionSitePermissionStore: SessionSitePermissionStore,
    val browserRuntimeStateController: BrowserRuntimeStateController,
    val browserSessionStateController: BrowserSessionStateController
)

/**
 * 浏览器 Activity 脚手架装配控制器。
 *
 * 这里创建的对象通常被 MainActivity 的生命周期、权限回调或三组 feature 装配共同使用。
 * 因为核心、运行期和启动期 feature 会在 onCreate() 中稍后才完成初始化，本类通过 provider 延迟读取它们。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示注册 Activity Result launcher 和处理系统回调时使用的宿主 Activity。
 * @param browserCoreFeatures 参数类型为 `() -> BrowserCoreFeatureComponents?`，表示安全读取核心浏览器组件的回调；尚未初始化时返回 null。
 * @param browserRuntimeFeatures 参数类型为 `() -> BrowserRuntimeFeatureComponents?`，表示安全读取运行期交互组件的回调；尚未初始化时返回 null。
 * @param browserStartupFeatures 参数类型为 `() -> BrowserStartupFeatureComponents?`，表示安全读取启动收尾组件的回调；尚未初始化时返回 null。
 */
class BrowserActivityScaffoldAssemblyController(
    private val activity: AppCompatActivity,
    private val browserCoreFeatures: () -> BrowserCoreFeatureComponents?,
    private val browserRuntimeFeatures: () -> BrowserRuntimeFeatureComponents?,
    private val browserStartupFeatures: () -> BrowserStartupFeatureComponents?
) {
    /**
     * 创建 Activity 脚手架组件。
     *
     * @return 返回 `BrowserActivityScaffoldComponents`，调用方保存后把其中的控制器交给核心、运行期和启动期装配继续使用。
     */
    fun create(): BrowserActivityScaffoldComponents {
        lateinit var browserSessionStateController: BrowserSessionStateController

        val browserChromeClientStateController = BrowserChromeClientStateAssemblyController(
            browserChromeClientController = {
                browserRuntimeFeatures()?.browserClients?.browserChromeClientController
            }
        ).create()

        val browserActivityLifecycleController = BrowserActivityLifecycleAssemblyController(
            browserChromeClientController = {
                browserRuntimeFeatures()?.browserClients?.browserChromeClientController
            },
            browserWebClientController = {
                browserRuntimeFeatures()?.browserClients?.browserWebClientController
            },
            pageArchiveController = {
                browserCoreFeatures()?.pageActions?.pageArchiveController
            },
            addressSuggestionController = {
                browserCoreFeatures()?.browserSearch?.addressSuggestionController
            },
            downloadController = {
                browserCoreFeatures()?.pageActions?.downloadController
            },
            elementPickerController = {
                browserStartupFeatures()?.pageFeatures?.elementPickerController
            },
            functionCenterEntryController = {
                browserStartupFeatures()?.functionCenterEntryController
            },
            browserChromeClientStateController = browserChromeClientStateController,
            browserStandardTabSessionController = {
                browserCoreFeatures()?.browserPersistence?.browserStandardTabSessionController
            },
            browserStandardWebViewHostController = {
                browserCoreFeatures()?.browserSurface?.browserStandardWebViewHostController
            },
            browserLaunchController = {
                browserCoreFeatures()?.browserNavigation?.browserLaunchController
            }
        ).create()

        val browserTabState = BrowserTabStateAssemblyController().create()
        val findInPageController = BrowserFindInPageAssemblyController(
            browserStandardWebViewHostController = {
                requireCoreFeatures().browserSurface.browserStandardWebViewHostController
            }
        ).create()
        val requestInterceptionProvider = BrowserRequestInterceptionProvider(
            browserFeatureStateController = {
                requireCoreFeatures().browserShell.browserFeatureStateController
            },
            settingsManager = { requireCoreFeatures().browserPersistence.settingsManager },
            browserSessionStateController = { browserSessionStateController },
            browserUrlStateController = {
                requireCoreFeatures().browserShell.browserUrlStateController
            },
            ruleEngine = { requireCoreFeatures().browserNavigation.ruleEngine }
        )
        val activityResultLaunchers = BrowserActivityResultLaunchersAssemblyController(
            activity = activity,
            webFileChooserController = {
                browserRuntimeFeatures()?.webRequests?.webFileChooserController
            },
            bookmarkImportExportController = {
                browserCoreFeatures()?.browserPersistence?.bookmarkImportExportController
            },
            pageArchiveController = {
                browserCoreFeatures()?.pageActions?.pageArchiveController
            },
            webPermissionRequestController = {
                browserRuntimeFeatures()?.webRequests?.webPermissionRequestController
            },
            geolocationPermissionController = {
                browserRuntimeFeatures()?.webRequests?.geolocationPermissionController
            }
        ).create()
        val sessionSitePermissionStore = SessionSitePermissionStore()
        val browserRuntimeStateController = BrowserRuntimeStateController(
            currentSessionController = {
                browserSessionStateController.currentSessionController()
            },
            fullscreenVideoController = {
                browserRuntimeFeatures()?.browserFullscreen?.fullscreenVideoController
            }
        )
        browserSessionStateController = BrowserSessionStateAssemblyController(
            isPrivateBrowsingActive = browserRuntimeStateController::isPrivateBrowsingActive,
            standardSessionController = {
                browserRuntimeFeatures()?.browserSessions?.standardSessionController
            },
            privateSessionController = {
                browserRuntimeFeatures()?.browserSessions?.privateSessionController
            }
        ).create()

        return BrowserActivityScaffoldComponents(
            browserChromeClientStateController = browserChromeClientStateController,
            browserActivityLifecycleController = browserActivityLifecycleController,
            browserTabState = browserTabState,
            findInPageController = findInPageController,
            requestInterceptionProvider = requestInterceptionProvider,
            activityResultLaunchers = activityResultLaunchers,
            sessionSitePermissionStore = sessionSitePermissionStore,
            browserRuntimeStateController = browserRuntimeStateController,
            browserSessionStateController = browserSessionStateController
        )
    }

    private fun requireCoreFeatures(): BrowserCoreFeatureComponents {
        return requireNotNull(browserCoreFeatures()) {
            "BrowserCoreFeatureComponents has not been initialized."
        }
    }
}
