package com.example.videobrowser.browser

import com.example.videobrowser.adblock.AdBlockLogger
import com.example.videobrowser.adblock.AdBlockRequestInterceptor
import com.example.videobrowser.browser.search.SearchResultRequestInterceptionPolicy
import com.example.videobrowser.rules.RuleEngine
import com.example.videobrowser.settings.SettingsManager

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器请求拦截访问模块”。
 * 文件名 BrowserRequestInterceptionProvider 可以拆开理解为“Browser Request Interception Provider”，
 * 表示它负责按需创建并提供广告拦截、日志和智能无图拦截对象。
 * 阅读顺序：先看构造参数理解 lazy 创建前需要保存哪些依赖，再看 components 如何延迟调用装配控制器。
 */

/**
 * 请求拦截组件提供器。
 *
 * 广告拦截和智能无图都依赖规则引擎与设置；这些对象在 Activity 初始化中较晚才可用。
 * 本类把 lazy 初始化和派生 getter 收在一起，让 MainActivity 只表达“需要请求拦截提供器”。
 *
 * @param browserFeatureStateController 参数类型为 `() -> BrowserFeatureStateController`，表示读取功能开关和站点例外状态的回调。
 * @param settingsManager 参数类型为 `() -> SettingsManager`，表示读取用户设置和白名单的回调。
 * @param browserSessionStateController 参数类型为 `() -> BrowserSessionStateController`，表示延迟读取当前页面 URL 会话状态控制器的回调。
 * @param browserUrlStateController 参数类型为 `() -> BrowserUrlStateController`，表示读取当前站点 host 的控制器回调。
 * @param ruleEngine 参数类型为 `() -> RuleEngine`，表示读取规则引擎的回调。
 * @param isSearchResultResourceUrl 参数类型为 `(String?, String?) -> Boolean`，表示判断资源是否属于内置搜索结果页同一提供商。
 */
class BrowserRequestInterceptionProvider(
    private val browserFeatureStateController: () -> BrowserFeatureStateController,
    private val settingsManager: () -> SettingsManager,
    private val browserSessionStateController: () -> BrowserSessionStateController,
    private val browserUrlStateController: () -> BrowserUrlStateController,
    private val ruleEngine: () -> RuleEngine,
    private val isSearchResultResourceUrl: (String?, String?) -> Boolean = { _, _ -> false }
) {
    private val components by lazy {
        BrowserRequestInterceptionAssemblyController(
            browserFeatureStateController = browserFeatureStateController,
            settingsManager = settingsManager,
            browserSessionStateController = browserSessionStateController(),
            browserUrlStateController = browserUrlStateController,
            ruleEngine = ruleEngine,
            isSearchResultResourceUrl = isSearchResultResourceUrl
        ).create()
    }

    /**
     * 广告拦截日志。
     *
     * @return 返回 `AdBlockLogger`，功能中心日志页通过它读取和清理拦截记录。
     */
    val adBlockLogger: AdBlockLogger
        get() = components.adBlockLogger

    /**
     * 广告请求拦截器。
     *
     * @return 返回 `AdBlockRequestInterceptor`，BrowserClient 在 WebView 请求到达时调用它。
     */
    val adBlockRequestInterceptor: AdBlockRequestInterceptor
        get() = components.adBlockRequestInterceptor

    /**
     * 智能无图请求拦截器。
     *
     * @return 返回 `SmartNoImageRequestInterceptor`，BrowserClient 在广告拦截未命中时调用它。
     */
    val smartNoImageRequestInterceptor: SmartNoImageRequestInterceptor
        get() = components.smartNoImageRequestInterceptor

    /**
     * 搜索结果页请求快速路径策略。
     *
     * @return 返回 `SearchResultRequestInterceptionPolicy`，BrowserClient 用它跳过搜索页一方资源的重拦截链。
     */
    val searchResultRequestInterceptionPolicy: SearchResultRequestInterceptionPolicy
        get() = components.searchResultRequestInterceptionPolicy
}
