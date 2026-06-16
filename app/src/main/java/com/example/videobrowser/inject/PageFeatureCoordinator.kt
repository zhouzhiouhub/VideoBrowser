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
    private val currentPageUrl: () -> String?
) {
    fun isAdBlockEnabled(): Boolean {
        return settingsManager.isAdBlockEnabled()
    }

    fun isCurrentSiteAdBlockDisabled(): Boolean {
        return settingsManager.isAdBlockDisabledForSite(currentSiteHost())
    }

    fun isJsInjectionEnabled(): Boolean {
        return settingsManager.isJsInjectionEnabled()
    }

    fun isCurrentSiteJsInjectionDisabled(): Boolean {
        return settingsManager.isJsInjectionDisabledForSite(currentSiteHost())
    }

    fun isPageCleanupEnabled(): Boolean {
        return settingsManager.isDomAdBlockEnabled()
    }

    fun isCurrentSitePageCleanupDisabled(): Boolean {
        return settingsManager.isDomAdBlockDisabledForSite(currentSiteHost())
    }

    fun isVideoEnhancementEnabled(): Boolean {
        return settingsManager.isVideoEnhancementEnabled()
    }

    fun isCurrentSiteVideoEnhancementDisabled(): Boolean {
        return settingsManager.isVideoEnhancementDisabledForSite(currentSiteHost())
    }

    fun isSmartNoImageEnabled(): Boolean {
        return settingsManager.isSmartNoImageEnabled()
    }

    fun isCurrentSiteSmartNoImageDisabled(): Boolean {
        return settingsManager.isSmartNoImageDisabledForSite(currentSiteHost())
    }

    fun injectPageFeatures() {
        // 每次页面加载完成后重新计算当前站点配置，确保站点级开关立即生效。
        jsInjector.inject(
            PageFeatureConfig(
                jsInjectionEnabled = isJsInjectionEnabled() && !isCurrentSiteJsInjectionDisabled(),
                cleanupEnabled = isPageCleanupEnabled() && !isCurrentSitePageCleanupDisabled(),
                videoEnabled = isVideoEnhancementEnabled() && !isCurrentSiteVideoEnhancementDisabled(),
                userCssSelectors = settingsManager.userElementHideSelectorsForSite(currentSiteHost())
            ),
            pageUrl = currentPageUrl() ?: browserManager().currentUrl()
        )
    }
}
