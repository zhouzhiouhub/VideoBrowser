package com.example.videobrowser.settings

import android.content.Context
import com.example.videobrowser.site.SiteHost
import com.example.videobrowser.storage.PreferenceStore

data class UserElementHideRule(
    val host: String,
    val selector: String
)

class SettingsManager(
    private val preferenceStore: PreferenceStore
) {
    fun isAdBlockEnabled(): Boolean {
        return preferenceStore.getBoolean(KEY_AD_BLOCK, DEFAULT_AD_BLOCK_ENABLED)
    }

    fun setAdBlockEnabled(enabled: Boolean) {
        preferenceStore.putBoolean(KEY_AD_BLOCK, enabled)
    }

    fun isAdBlockDisabledForSite(host: String?): Boolean {
        val normalizedHost = SiteHost.normalize(host) ?: return false
        return adBlockDisabledSiteHosts().contains(normalizedHost)
    }

    /**
     * P11-01 只保存站点级请求拦截放行列表，不引入订阅、规则文件或数据库。
     */
    fun setAdBlockDisabledForSite(host: String?, disabled: Boolean): Boolean {
        val normalizedHost = SiteHost.normalize(host) ?: return false
        val hosts = adBlockDisabledSiteHosts().toMutableSet()
        if (disabled) {
            hosts.add(normalizedHost)
        } else {
            hosts.remove(normalizedHost)
        }

        if (hosts.isEmpty()) {
            preferenceStore.remove(KEY_SITE_AD_BLOCK_DISABLED_HOSTS)
        } else {
            preferenceStore.putString(
                KEY_SITE_AD_BLOCK_DISABLED_HOSTS,
                hosts.sorted().joinToString(separator = "\n")
            )
        }
        return true
    }

    fun adBlockDisabledSiteHosts(): Set<String> {
        return preferenceStore.getString(KEY_SITE_AD_BLOCK_DISABLED_HOSTS, null)
            ?.lineSequence()
            ?.mapNotNull(SiteHost::normalize)
            ?.toSet()
            ?: emptySet()
    }

    fun isJsInjectionDisabledForSite(host: String?): Boolean {
        val normalizedHost = SiteHost.normalize(host) ?: return false
        return jsInjectionDisabledSiteHosts().contains(normalizedHost)
    }

    fun setJsInjectionDisabledForSite(host: String?, disabled: Boolean): Boolean {
        val normalizedHost = SiteHost.normalize(host) ?: return false
        val hosts = jsInjectionDisabledSiteHosts().toMutableSet()
        if (disabled) {
            hosts.add(normalizedHost)
        } else {
            hosts.remove(normalizedHost)
        }

        saveHostSet(KEY_SITE_JS_INJECTION_DISABLED_HOSTS, hosts)
        return true
    }

    fun jsInjectionDisabledSiteHosts(): Set<String> {
        return loadHostSet(KEY_SITE_JS_INJECTION_DISABLED_HOSTS)
    }

    fun isDomAdBlockDisabledForSite(host: String?): Boolean {
        val normalizedHost = SiteHost.normalize(host) ?: return false
        return domAdBlockDisabledSiteHosts().contains(normalizedHost)
    }

    fun setDomAdBlockDisabledForSite(host: String?, disabled: Boolean): Boolean {
        val normalizedHost = SiteHost.normalize(host) ?: return false
        val hosts = domAdBlockDisabledSiteHosts().toMutableSet()
        if (disabled) {
            hosts.add(normalizedHost)
        } else {
            hosts.remove(normalizedHost)
        }

        saveHostSet(KEY_SITE_DOM_AD_BLOCK_DISABLED_HOSTS, hosts)
        return true
    }

    fun domAdBlockDisabledSiteHosts(): Set<String> {
        return loadHostSet(KEY_SITE_DOM_AD_BLOCK_DISABLED_HOSTS)
    }

    fun isVideoEnhancementDisabledForSite(host: String?): Boolean {
        val normalizedHost = SiteHost.normalize(host) ?: return false
        return videoEnhancementDisabledSiteHosts().contains(normalizedHost)
    }

    fun setVideoEnhancementDisabledForSite(host: String?, disabled: Boolean): Boolean {
        val normalizedHost = SiteHost.normalize(host) ?: return false
        val hosts = videoEnhancementDisabledSiteHosts().toMutableSet()
        if (disabled) {
            hosts.add(normalizedHost)
        } else {
            hosts.remove(normalizedHost)
        }

        saveHostSet(KEY_SITE_VIDEO_ENHANCEMENT_DISABLED_HOSTS, hosts)
        return true
    }

    fun videoEnhancementDisabledSiteHosts(): Set<String> {
        return loadHostSet(KEY_SITE_VIDEO_ENHANCEMENT_DISABLED_HOSTS)
    }

    fun isUserWhitelistedSite(host: String?): Boolean {
        val normalizedHost = SiteHost.normalize(host) ?: return false
        return userWhitelistedSiteHosts().contains(normalizedHost)
    }

    fun setUserWhitelistedSite(host: String?, whitelisted: Boolean): Boolean {
        val normalizedHost = SiteHost.normalize(host) ?: return false
        val hosts = userWhitelistedSiteHosts().toMutableSet()
        if (whitelisted) {
            hosts.add(normalizedHost)
        } else {
            hosts.remove(normalizedHost)
        }

        saveHostSet(KEY_USER_WHITELISTED_SITE_HOSTS, hosts)
        return true
    }

    fun userWhitelistedSiteHosts(): Set<String> {
        return loadHostSet(KEY_USER_WHITELISTED_SITE_HOSTS)
    }

    fun userElementHideSelectorsForSite(host: String?): List<String> {
        val normalizedHost = SiteHost.normalize(host) ?: return emptyList()
        return userElementHideRules()
            .filter { rule -> rule.host == normalizedHost }
            .map { rule -> rule.selector }
    }

    fun hasUserElementHideSelectorForSite(host: String?, selector: String): Boolean {
        val normalizedHost = SiteHost.normalize(host) ?: return false
        val normalizedSelector = normalizeUserElementSelector(selector) ?: return false
        return userElementHideRules().any { rule ->
            rule.host == normalizedHost && rule.selector == normalizedSelector
        }
    }

    fun addUserElementHideSelectorForSite(host: String?, selector: String): Boolean {
        val normalizedHost = SiteHost.normalize(host) ?: return false
        val normalizedSelector = normalizeUserElementSelector(selector) ?: return false
        val rules = userElementHideRules().toMutableList()
        if (rules.any { rule -> rule.host == normalizedHost && rule.selector == normalizedSelector }) {
            return false
        }

        rules.add(UserElementHideRule(host = normalizedHost, selector = normalizedSelector))
        saveUserElementHideRules(rules)
        return true
    }

    fun userElementHideRules(): List<UserElementHideRule> {
        return preferenceStore.getString(KEY_USER_ELEMENT_HIDE_RULES, null)
            ?.lineSequence()
            ?.mapNotNull(::parseUserElementHideRule)
            ?.distinct()
            ?.toList()
            ?: emptyList()
    }

    fun isJsInjectionEnabled(): Boolean {
        return preferenceStore.getBoolean(KEY_JS_INJECTION, DEFAULT_JS_INJECTION_ENABLED)
    }

    fun setJsInjectionEnabled(enabled: Boolean) {
        preferenceStore.putBoolean(KEY_JS_INJECTION, enabled)
    }

    fun isDomAdBlockEnabled(): Boolean {
        return preferenceStore.getBoolean(KEY_DOM_AD_BLOCK, DEFAULT_DOM_AD_BLOCK_ENABLED)
    }

    fun setDomAdBlockEnabled(enabled: Boolean) {
        preferenceStore.putBoolean(KEY_DOM_AD_BLOCK, enabled)
    }

    fun isVideoEnhancementEnabled(): Boolean {
        return preferenceStore.getBoolean(KEY_VIDEO_ENHANCEMENT, DEFAULT_VIDEO_ENHANCEMENT_ENABLED)
    }

    fun setVideoEnhancementEnabled(enabled: Boolean) {
        preferenceStore.putBoolean(KEY_VIDEO_ENHANCEMENT, enabled)
    }

    fun defaultVideoSpeed(): Float {
        return normalizeVideoSpeed(
            preferenceStore.getFloat(KEY_DEFAULT_VIDEO_SPEED, DEFAULT_VIDEO_SPEED)
        )
    }

    fun setDefaultVideoSpeed(speed: Float) {
        preferenceStore.putFloat(KEY_DEFAULT_VIDEO_SPEED, normalizeVideoSpeed(speed))
    }

    fun homeUrl(): String {
        return normalizeHomeUrl(
            preferenceStore.getString(KEY_HOME_URL, null),
            DEFAULT_HOME_URL
        )
    }

    fun homeUrlOr(defaultValue: String): String {
        return normalizeHomeUrl(
            preferenceStore.getString(KEY_HOME_URL, null),
            defaultValue
        )
    }

    fun hasHomeUrl(): Boolean {
        return preferenceStore.contains(KEY_HOME_URL)
    }

    fun setHomeUrl(url: String) {
        preferenceStore.putString(KEY_HOME_URL, normalizeHomeUrl(url, DEFAULT_HOME_URL))
    }

    fun searchEngineId(): String {
        return preferenceStore.getString(KEY_SEARCH_ENGINE, DEFAULT_SEARCH_ENGINE_ID)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: DEFAULT_SEARCH_ENGINE_ID
    }

    fun setSearchEngineId(id: String) {
        val normalizedId = id.trim().takeIf { it.isNotEmpty() } ?: DEFAULT_SEARCH_ENGINE_ID
        preferenceStore.putString(KEY_SEARCH_ENGINE, normalizedId)
    }

    fun isDesktopModeEnabled(): Boolean {
        return preferenceStore.getBoolean(KEY_DESKTOP_MODE, DEFAULT_DESKTOP_MODE_ENABLED)
    }

    fun setDesktopModeEnabled(enabled: Boolean) {
        preferenceStore.putBoolean(KEY_DESKTOP_MODE, enabled)
    }

    fun isPrivateBrowsingEnabled(): Boolean {
        preferenceStore.remove(KEY_PRIVATE_BROWSING)
        return DEFAULT_PRIVATE_BROWSING_ENABLED
    }

    fun setPrivateBrowsingEnabled(enabled: Boolean) {
        preferenceStore.remove(KEY_PRIVATE_BROWSING)
    }

    fun restoreDefaults(): Boolean {
        return preferenceStore.remove(RESET_KEYS, commit = true)
    }

    private fun loadHostSet(key: String): Set<String> {
        return preferenceStore.getString(key, null)
            ?.lineSequence()
            ?.mapNotNull(SiteHost::normalize)
            ?.toSet()
            ?: emptySet()
    }

    private fun saveHostSet(key: String, hosts: Set<String>) {
        if (hosts.isEmpty()) {
            preferenceStore.remove(key)
        } else {
            preferenceStore.putString(
                key,
                hosts.sorted().joinToString(separator = "\n")
            )
        }
    }

    private fun parseUserElementHideRule(line: String): UserElementHideRule? {
        val separatorIndex = line.indexOf('\t')
        if (separatorIndex <= 0 || separatorIndex >= line.lastIndex) {
            return null
        }
        val host = SiteHost.normalize(line.substring(0, separatorIndex)) ?: return null
        val selector = normalizeUserElementSelector(line.substring(separatorIndex + 1)) ?: return null
        return UserElementHideRule(host = host, selector = selector)
    }

    private fun saveUserElementHideRules(rules: List<UserElementHideRule>) {
        val lines = rules
            .mapNotNull { rule ->
                val host = SiteHost.normalize(rule.host) ?: return@mapNotNull null
                val selector = normalizeUserElementSelector(rule.selector) ?: return@mapNotNull null
                UserElementHideRule(host = host, selector = selector)
            }
            .distinct()
            .sortedWith(compareBy<UserElementHideRule> { it.host }.thenBy { it.selector })
            .map { rule -> "${rule.host}\t${rule.selector}" }

        if (lines.isEmpty()) {
            preferenceStore.remove(KEY_USER_ELEMENT_HIDE_RULES)
        } else {
            preferenceStore.putString(KEY_USER_ELEMENT_HIDE_RULES, lines.joinToString(separator = "\n"))
        }
    }

    private fun normalizeUserElementSelector(selector: String): String? {
        val normalized = selector.trim().replace(Regex("\\s+"), " ")
        if (normalized.isEmpty() || normalized.length > MAX_USER_ELEMENT_SELECTOR_LENGTH) {
            return null
        }
        if (normalized.any { char -> char == '\t' || char == '\n' || char == '\r' }) {
            return null
        }
        if (Regex("[{};<>]").containsMatchIn(normalized)) {
            return null
        }
        if (Regex(":has\\(|:contains\\(|:matches\\(|:xpath\\(|javascript:|expression\\(", RegexOption.IGNORE_CASE)
                .containsMatchIn(normalized)
        ) {
            return null
        }
        return normalized
    }

    private fun normalizeVideoSpeed(speed: Float): Float {
        return if (!speed.isNaN() && !speed.isInfinite() && speed > 0f) {
            speed
        } else {
            DEFAULT_VIDEO_SPEED
        }
    }

    private fun normalizeHomeUrl(value: String?, defaultValue: String): String {
        return value
            ?.trim()
            ?.takeIf {
                it.startsWith("http://", ignoreCase = true) ||
                    it.startsWith("https://", ignoreCase = true)
            }
            ?: defaultValue
    }

    companion object {
        const val DEFAULT_SEARCH_ENGINE_ID = "baidu"
        const val DEFAULT_HOME_URL = "https://m.baidu.com/"
        const val DEFAULT_VIDEO_SPEED = 1f

        private const val DEFAULT_AD_BLOCK_ENABLED = true
        private const val DEFAULT_JS_INJECTION_ENABLED = true
        private const val DEFAULT_DOM_AD_BLOCK_ENABLED = true
        private const val DEFAULT_VIDEO_ENHANCEMENT_ENABLED = true
        private const val DEFAULT_DESKTOP_MODE_ENABLED = false
        private const val DEFAULT_PRIVATE_BROWSING_ENABLED = false

        private const val KEY_AD_BLOCK = "ad_block"
        private const val KEY_SITE_AD_BLOCK_DISABLED_HOSTS = "site_ad_block_disabled_hosts"
        private const val KEY_SITE_JS_INJECTION_DISABLED_HOSTS = "site_js_injection_disabled_hosts"
        private const val KEY_SITE_DOM_AD_BLOCK_DISABLED_HOSTS = "site_dom_ad_block_disabled_hosts"
        private const val KEY_SITE_VIDEO_ENHANCEMENT_DISABLED_HOSTS =
            "site_video_enhancement_disabled_hosts"
        private const val KEY_USER_WHITELISTED_SITE_HOSTS = "user_whitelisted_site_hosts"
        private const val KEY_USER_ELEMENT_HIDE_RULES = "user_element_hide_rules"
        private const val KEY_JS_INJECTION = "js_injection"
        private const val KEY_DOM_AD_BLOCK = "page_cleanup"
        private const val KEY_VIDEO_ENHANCEMENT = "video_enhancement"
        private const val KEY_DEFAULT_VIDEO_SPEED = "default_video_speed"
        private const val KEY_HOME_URL = "home_url"
        private const val KEY_SEARCH_ENGINE = "search_provider"
        private const val KEY_DESKTOP_MODE = "desktop_mode"
        private const val KEY_PRIVATE_BROWSING = "private_browsing"
        private const val MAX_USER_ELEMENT_SELECTOR_LENGTH = 200

        private val RESET_KEYS = listOf(
            KEY_AD_BLOCK,
            KEY_SITE_AD_BLOCK_DISABLED_HOSTS,
            KEY_SITE_JS_INJECTION_DISABLED_HOSTS,
            KEY_SITE_DOM_AD_BLOCK_DISABLED_HOSTS,
            KEY_SITE_VIDEO_ENHANCEMENT_DISABLED_HOSTS,
            KEY_USER_WHITELISTED_SITE_HOSTS,
            KEY_USER_ELEMENT_HIDE_RULES,
            KEY_JS_INJECTION,
            KEY_DOM_AD_BLOCK,
            KEY_VIDEO_ENHANCEMENT,
            KEY_DEFAULT_VIDEO_SPEED,
            KEY_HOME_URL,
            KEY_SEARCH_ENGINE,
            KEY_DESKTOP_MODE,
            KEY_PRIVATE_BROWSING
        )

        fun from(context: Context): SettingsManager {
            return SettingsManager(PreferenceStore.from(context))
        }
    }
}
