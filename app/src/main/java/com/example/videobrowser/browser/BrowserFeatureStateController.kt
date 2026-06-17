package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 BrowserFeatureStateController 可以拆开理解为“Browser Feature State Controller”，
 * 表示它只负责回答浏览器功能当前是否启用。
 * 主要职责：集中读取无痕、桌面模式、广告拦截、智能无图、脚本注入、页面清理和视频增强状态。
 * 阅读顺序：先看构造参数了解状态来自哪里，再看公开函数知道外部模块可以查询哪些开关。
 */
import com.example.videobrowser.inject.PageFeatureCoordinator
import com.example.videobrowser.settings.SettingsManager

/**
 * 浏览器功能状态控制器。
 *
 * MainActivity 会很早创建这个对象，因此 SettingsManager 和 PageFeatureCoordinator 通过函数延迟读取。
 * 这样构造阶段不依赖初始化顺序，真正查询功能状态时再访问对应模块。
 *
 * @param settingsManager 返回当前设置管理器的函数，用于读取全局设置，例如桌面模式是否启用。
 * @param pageFeatureCoordinator 返回当前页面功能协调器的函数，用于合并全局设置和当前站点例外。
 * @param isPrivateBrowsingActive 返回当前是否处于无痕浏览模式的函数，来自 MainActivity 的运行时状态。
 */
class BrowserFeatureStateController(
    private val settingsManager: () -> SettingsManager,
    private val pageFeatureCoordinator: () -> PageFeatureCoordinator,
    private val isPrivateBrowsingActive: () -> Boolean
) {
    /**
     * 判断当前是否处于无痕浏览。
     *
     * @return 当前无痕浏览开关状态，true 表示无痕模式正在生效。
     */
    fun isPrivateBrowsingEnabled(): Boolean {
        return isPrivateBrowsingActive()
    }

    /**
     * 判断桌面模式是否启用。
     *
     * @return 当前全局桌面模式设置，true 表示 WebView 应使用桌面模式展示。
     */
    fun isDesktopModeEnabled(): Boolean {
        return settingsManager().isDesktopModeEnabled()
    }

    /**
     * 判断广告拦截是否启用。
     *
     * @return 当前全局广告拦截开关状态。
     */
    fun isAdBlockEnabled(): Boolean {
        return pageFeatureCoordinator().isAdBlockEnabled()
    }

    /**
     * 判断当前站点是否停用了广告拦截。
     *
     * @return true 表示当前站点在例外列表中关闭了广告拦截。
     */
    fun isCurrentSiteAdBlockDisabled(): Boolean {
        return pageFeatureCoordinator().isCurrentSiteAdBlockDisabled()
    }

    /**
     * 判断智能无图是否启用。
     *
     * @return 当前全局智能无图开关状态。
     */
    fun isSmartNoImageEnabled(): Boolean {
        return pageFeatureCoordinator().isSmartNoImageEnabled()
    }

    /**
     * 判断当前站点是否停用了智能无图。
     *
     * @return true 表示当前站点在例外列表中关闭了智能无图。
     */
    fun isCurrentSiteSmartNoImageDisabled(): Boolean {
        return pageFeatureCoordinator().isCurrentSiteSmartNoImageDisabled()
    }

    /**
     * 判断 JavaScript 注入是否启用。
     *
     * @return 当前全局 JavaScript 注入开关状态。
     */
    fun isJsInjectionEnabled(): Boolean {
        return pageFeatureCoordinator().isJsInjectionEnabled()
    }

    /**
     * 判断当前站点是否停用了 JavaScript 注入。
     *
     * @return true 表示当前站点在例外列表中关闭了 JavaScript 注入。
     */
    fun isCurrentSiteJsInjectionDisabled(): Boolean {
        return pageFeatureCoordinator().isCurrentSiteJsInjectionDisabled()
    }

    /**
     * 判断页面清理是否启用。
     *
     * @return 当前全局页面清理开关状态。
     */
    fun isPageCleanupEnabled(): Boolean {
        return pageFeatureCoordinator().isPageCleanupEnabled()
    }

    /**
     * 判断当前站点是否停用了页面清理。
     *
     * @return true 表示当前站点在例外列表中关闭了页面清理。
     */
    fun isCurrentSitePageCleanupDisabled(): Boolean {
        return pageFeatureCoordinator().isCurrentSitePageCleanupDisabled()
    }

    /**
     * 判断网页视频增强是否启用。
     *
     * @return 当前全局视频增强开关状态。
     */
    fun isVideoEnhancementEnabled(): Boolean {
        return pageFeatureCoordinator().isVideoEnhancementEnabled()
    }

    /**
     * 判断当前站点是否停用了网页视频增强。
     *
     * @return true 表示当前站点在例外列表中关闭了网页视频增强。
     */
    fun isCurrentSiteVideoEnhancementDisabled(): Boolean {
        return pageFeatureCoordinator().isCurrentSiteVideoEnhancementDisabled()
    }
}
