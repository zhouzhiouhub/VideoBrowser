package com.example.videobrowser.inject

/**
 * 初学者阅读提示：
 * 这个文件属于“页面脚本注入模块”。
 * 文件名 PageFeatureCoordinator 可以拆开理解为“Page Feature Coordinator”，表示它只负责页面净化链路中的一个小职责。
 * 主要职责：读取内置 JavaScript，按当前站点和设置组合注入脚本，让页面净化和视频增强生效。
 * 阅读顺序：先看数据类/策略类表达什么规则，再看控制器如何把规则接到 WebView 请求或页面脚本上。
 */
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.settings.SettingsManager

/**
 * 页面功能开关协调器。
 *
 * SettingsManager 只保存“全局是否开启”和“当前站点是否关闭”，JsInjector 只负责注入脚本。
 * PageFeatureCoordinator 站在中间，把两边的信息合并成一次页面注入所需的配置。
 */
class PageFeatureCoordinator(
    private val settingsManager: SettingsManager,
    private val browserManager: () -> BrowserManager,
    private val jsInjector: JsInjector,
    private val currentSiteHost: () -> String?,
    private val currentPageUrl: () -> String?,
    private val isBuiltInSearchResultPage: (String?) -> Boolean = { false },
    private val searchPageHideCssForUrl: (String?) -> List<String> = { emptyList() }
) {
    /**
     * 函数 `isAdBlockEnabled`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isAdBlockEnabled(): Boolean {
        return settingsManager.isAdBlockEnabled()
    }

    /**
     * 函数 `isCurrentSiteAdBlockDisabled`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isCurrentSiteAdBlockDisabled(): Boolean {
        return settingsManager.isAdBlockDisabledForSite(currentSiteHost())
    }

    /**
     * 函数 `isJsInjectionEnabled`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isJsInjectionEnabled(): Boolean {
        return settingsManager.isJsInjectionEnabled()
    }

    /**
     * 函数 `isCurrentSiteJsInjectionDisabled`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isCurrentSiteJsInjectionDisabled(): Boolean {
        return settingsManager.isJsInjectionDisabledForSite(currentSiteHost())
    }

    /**
     * 函数 `isPageCleanupEnabled`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isPageCleanupEnabled(): Boolean {
        return settingsManager.isDomAdBlockEnabled()
    }

    /**
     * 函数 `isCurrentSitePageCleanupDisabled`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isCurrentSitePageCleanupDisabled(): Boolean {
        return settingsManager.isDomAdBlockDisabledForSite(currentSiteHost())
    }

    /**
     * 函数 `isVideoEnhancementEnabled`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isVideoEnhancementEnabled(): Boolean {
        return settingsManager.isVideoEnhancementEnabled()
    }

    /**
     * 函数 `isCurrentSiteVideoEnhancementDisabled`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isCurrentSiteVideoEnhancementDisabled(): Boolean {
        return settingsManager.isVideoEnhancementDisabledForSite(currentSiteHost())
    }

    /**
     * 函数 `isSmartNoImageEnabled`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isSmartNoImageEnabled(): Boolean {
        return settingsManager.isSmartNoImageEnabled()
    }

    /**
     * 函数 `isCurrentSiteSmartNoImageDisabled`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isCurrentSiteSmartNoImageDisabled(): Boolean {
        return settingsManager.isSmartNoImageDisabledForSite(currentSiteHost())
    }

    /**
     * 函数 `injectPageFeatures`：封装 `inject Page Features` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun injectPageFeatures(onInjected: (() -> Unit)? = null) {
        // 每次页面加载完成后重新计算当前站点配置，确保站点级开关立即生效。
        val pageUrl = currentPageUrl() ?: browserManager().currentUrl()
        val builtInSearchResultPage = isBuiltInSearchResultPage(pageUrl)
        jsInjector.inject(
            PageFeatureConfig(
                jsInjectionEnabled = isJsInjectionEnabled() && !isCurrentSiteJsInjectionDisabled(),
                cleanupEnabled = isPageCleanupEnabled() && !isCurrentSitePageCleanupDisabled(),
                videoEnabled = isVideoEnhancementEnabled() && !isCurrentSiteVideoEnhancementDisabled(),
                builtInSearchResultPage = builtInSearchResultPage,
                searchPageHideCss = if (builtInSearchResultPage) {
                    searchPageHideCssForUrl(pageUrl)
                } else {
                    emptyList()
                },
                userCssSelectors = settingsManager.userElementHideSelectorsForSite(currentSiteHost())
            ),
            pageUrl = pageUrl,
            onInjected = onInjected
        )
    }
}
