package com.example.videobrowser.settings

/**
 * 初学者阅读提示：
 * 这个文件属于“设置站点功能开关模块”。
 * 文件名 SettingsSiteFeatureFacade 可以拆开理解为“Settings Site Feature Facade”，表示它集中封装站点级功能开关的业务名称。
 * 主要职责：把广告拦截、JS 注入、DOM 拦截、视频增强、省图和用户白名单的站点例外统一映射到同一套 host 集合存储。
 * 阅读顺序：先看公开函数知道每个站点功能对应哪个业务语义，再看 private 辅助函数了解如何复用 contains/set/hosts/clear。
 */
internal class SettingsSiteFeatureFacade(
    private val siteFeatureHosts: SiteFeatureHostSettings
) {
    fun isAdBlockDisabledForSite(host: String?): Boolean {
        return contains(KEY_SITE_AD_BLOCK_DISABLED_HOSTS, host)
    }

    fun setAdBlockDisabledForSite(host: String?, disabled: Boolean): Boolean {
        return set(KEY_SITE_AD_BLOCK_DISABLED_HOSTS, host, disabled)
    }

    fun adBlockDisabledSiteHosts(): Set<String> {
        return hosts(KEY_SITE_AD_BLOCK_DISABLED_HOSTS)
    }

    fun isJsInjectionDisabledForSite(host: String?): Boolean {
        return contains(KEY_SITE_JS_INJECTION_DISABLED_HOSTS, host)
    }

    fun setJsInjectionDisabledForSite(host: String?, disabled: Boolean): Boolean {
        return set(KEY_SITE_JS_INJECTION_DISABLED_HOSTS, host, disabled)
    }

    fun jsInjectionDisabledSiteHosts(): Set<String> {
        return hosts(KEY_SITE_JS_INJECTION_DISABLED_HOSTS)
    }

    fun isDomAdBlockDisabledForSite(host: String?): Boolean {
        return contains(KEY_SITE_DOM_AD_BLOCK_DISABLED_HOSTS, host)
    }

    fun setDomAdBlockDisabledForSite(host: String?, disabled: Boolean): Boolean {
        return set(KEY_SITE_DOM_AD_BLOCK_DISABLED_HOSTS, host, disabled)
    }

    fun domAdBlockDisabledSiteHosts(): Set<String> {
        return hosts(KEY_SITE_DOM_AD_BLOCK_DISABLED_HOSTS)
    }

    fun isVideoEnhancementDisabledForSite(host: String?): Boolean {
        return contains(KEY_SITE_VIDEO_ENHANCEMENT_DISABLED_HOSTS, host)
    }

    fun setVideoEnhancementDisabledForSite(host: String?, disabled: Boolean): Boolean {
        return set(KEY_SITE_VIDEO_ENHANCEMENT_DISABLED_HOSTS, host, disabled)
    }

    fun videoEnhancementDisabledSiteHosts(): Set<String> {
        return hosts(KEY_SITE_VIDEO_ENHANCEMENT_DISABLED_HOSTS)
    }

    fun isSmartNoImageDisabledForSite(host: String?): Boolean {
        return contains(KEY_SITE_SMART_NO_IMAGE_DISABLED_HOSTS, host)
    }

    fun setSmartNoImageDisabledForSite(host: String?, disabled: Boolean): Boolean {
        return set(KEY_SITE_SMART_NO_IMAGE_DISABLED_HOSTS, host, disabled)
    }

    fun smartNoImageDisabledSiteHosts(): Set<String> {
        return hosts(KEY_SITE_SMART_NO_IMAGE_DISABLED_HOSTS)
    }

    fun isUserWhitelistedSite(host: String?): Boolean {
        return contains(KEY_USER_WHITELISTED_SITE_HOSTS, host)
    }

    fun setUserWhitelistedSite(host: String?, whitelisted: Boolean): Boolean {
        return set(KEY_USER_WHITELISTED_SITE_HOSTS, host, whitelisted)
    }

    fun userWhitelistedSiteHosts(): Set<String> {
        return hosts(KEY_USER_WHITELISTED_SITE_HOSTS)
    }

    fun clearUserWhitelistedSites() {
        clear(KEY_USER_WHITELISTED_SITE_HOSTS)
    }

    private fun contains(key: String, host: String?): Boolean {
        return siteFeatureHosts.contains(key, host)
    }

    private fun set(key: String, host: String?, enabled: Boolean): Boolean {
        return siteFeatureHosts.set(key, host, enabled)
    }

    private fun hosts(key: String): Set<String> {
        return siteFeatureHosts.hosts(key)
    }

    private fun clear(key: String) {
        siteFeatureHosts.clear(key)
    }
}
