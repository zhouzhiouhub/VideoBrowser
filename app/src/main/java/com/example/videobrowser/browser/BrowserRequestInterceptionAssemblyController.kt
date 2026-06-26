package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器请求拦截装配模块”。
 * 文件名 BrowserRequestInterceptionAssemblyController 可以拆开理解为“Browser Request Interception Assembly Controller”，
 * 表示它只负责创建广告拦截、广告拦截日志和智能无图请求拦截相关对象。
 * 阅读顺序：先看 BrowserRequestInterceptionComponents 知道返回哪些对象，再看 create() 中广告拦截和无图拦截各自依赖哪些状态。
 */
import com.example.videobrowser.adblock.AdBlockLogger
import com.example.videobrowser.adblock.AdBlockManager
import com.example.videobrowser.adblock.AdBlockRequestInterceptor
import com.example.videobrowser.browser.search.SearchResultRequestInterceptionPolicy
import com.example.videobrowser.rules.RuleEngine
import com.example.videobrowser.settings.SettingsManager

/**
 * 请求拦截组件集合。
 *
 * @param adBlockLogger 参数类型为 `AdBlockLogger`，表示广告拦截命中和跳过原因的日志记录器。
 * @param adBlockManager 参数类型为 `AdBlockManager`，表示把 WebView 请求交给规则系统评估的广告拦截管理器。
 * @param adBlockRequestInterceptor 参数类型为 `AdBlockRequestInterceptor`，表示 BrowserClient 调用的广告请求拦截器。
 * @param smartNoImageRequestInterceptor 参数类型为 `SmartNoImageRequestInterceptor`，表示智能无图模式下拦截图片请求的拦截器。
 * @param searchResultRequestInterceptionPolicy 参数类型为 `SearchResultRequestInterceptionPolicy`，表示搜索结果页资源请求快速路径策略。
 */
data class BrowserRequestInterceptionComponents(
    val adBlockLogger: AdBlockLogger,
    val adBlockManager: AdBlockManager,
    val adBlockRequestInterceptor: AdBlockRequestInterceptor,
    val smartNoImageRequestInterceptor: SmartNoImageRequestInterceptor,
    val searchResultRequestInterceptionPolicy: SearchResultRequestInterceptionPolicy
)

/**
 * 浏览器请求拦截装配控制器。
 *
 * 广告拦截和智能无图都依赖当前页面、站点例外、用户设置和规则引擎；本类集中创建这些对象，
 * 让 MainActivity 不再直接维护多段 lazy 初始化逻辑。
 *
 * @param browserFeatureStateController 参数类型为 `() -> BrowserFeatureStateController`，表示读取页面增强开关和站点禁用状态的回调。
 * @param settingsManager 参数类型为 `() -> SettingsManager`，表示读取用户白名单等持久设置的回调。
 * @param browserSessionStateController 参数类型为 `BrowserSessionStateController`，表示读取当前页面 URL 的会话状态控制器。
 * @param browserUrlStateController 参数类型为 `() -> BrowserUrlStateController`，表示读取当前站点 host 的控制器回调。
 * @param ruleEngine 参数类型为 `() -> RuleEngine`，表示读取广告拦截和脚本规则引擎的回调。
 * @param isSearchResultResourceUrl 参数类型为 `(String?, String?) -> Boolean`，表示判断资源是否属于内置搜索结果页同一提供商。
 */
class BrowserRequestInterceptionAssemblyController(
    private val browserFeatureStateController: () -> BrowserFeatureStateController,
    private val settingsManager: () -> SettingsManager,
    private val browserSessionStateController: BrowserSessionStateController,
    private val browserUrlStateController: () -> BrowserUrlStateController,
    private val ruleEngine: () -> RuleEngine,
    private val isSearchResultResourceUrl: (String?, String?) -> Boolean = { _, _ -> false }
) {
    /**
     * 创建请求拦截组件集合。
     *
     * @return 返回 `BrowserRequestInterceptionComponents`，调用方通常以 lazy 方式持有，避免在规则引擎初始化前访问。
     */
    fun create(): BrowserRequestInterceptionComponents {
        val adBlockLogger = AdBlockLogger()
        val adBlockManager = AdBlockManager(
            isEnabled = { browserFeatureStateController().isAdBlockEnabled() },
            isDisabledForCurrentSite = {
                browserFeatureStateController().isCurrentSiteAdBlockDisabled()
            },
            isUserWhitelistedRequestHost = settingsManager()::isUserWhitelistedSite,
            currentPageUrl = currentPageUrl(),
            currentPageHost = { browserUrlStateController().currentSiteHost() },
            logger = adBlockLogger,
            ruleEngine = ruleEngine()
        )
        val adBlockRequestInterceptor = AdBlockRequestInterceptor(adBlockManager)
        val smartNoImageRequestInterceptor = SmartNoImageRequestInterceptor(
            isEnabled = { browserFeatureStateController().isSmartNoImageEnabled() },
            isDisabledForCurrentSite = {
                browserFeatureStateController().isCurrentSiteSmartNoImageDisabled()
            },
            currentPageUrl = currentPageUrl()
        )
        val searchResultRequestInterceptionPolicy = SearchResultRequestInterceptionPolicy(
            isSearchResultResourceUrl = isSearchResultResourceUrl
        )
        return BrowserRequestInterceptionComponents(
            adBlockLogger = adBlockLogger,
            adBlockManager = adBlockManager,
            adBlockRequestInterceptor = adBlockRequestInterceptor,
            smartNoImageRequestInterceptor = smartNoImageRequestInterceptor,
            searchResultRequestInterceptionPolicy = searchResultRequestInterceptionPolicy
        )
    }

    /**
     * 返回当前页面 URL 读取回调。
     *
     * @return 返回 `() -> String?`，调用时从当前会话控制器读取页面 URL。
     */
    private fun currentPageUrl(): () -> String? {
        return {
            browserSessionStateController.currentSessionController().currentPageUrl
        }
    }
}
