package com.example.videobrowser.settings

import android.content.Context
import com.example.videobrowser.site.SiteHost
import com.example.videobrowser.storage.PreferenceStore

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

    fun restoreDefaults(): Boolean {
        return preferenceStore.remove(RESET_KEYS, commit = true)
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

        private const val KEY_AD_BLOCK = "ad_block"
        private const val KEY_SITE_AD_BLOCK_DISABLED_HOSTS = "site_ad_block_disabled_hosts"
        private const val KEY_JS_INJECTION = "js_injection"
        private const val KEY_DOM_AD_BLOCK = "page_cleanup"
        private const val KEY_VIDEO_ENHANCEMENT = "video_enhancement"
        private const val KEY_DEFAULT_VIDEO_SPEED = "default_video_speed"
        private const val KEY_HOME_URL = "home_url"
        private const val KEY_SEARCH_ENGINE = "search_provider"
        private const val KEY_DESKTOP_MODE = "desktop_mode"

        private val RESET_KEYS = listOf(
            KEY_AD_BLOCK,
            KEY_SITE_AD_BLOCK_DISABLED_HOSTS,
            KEY_JS_INJECTION,
            KEY_DOM_AD_BLOCK,
            KEY_VIDEO_ENHANCEMENT,
            KEY_DEFAULT_VIDEO_SPEED,
            KEY_HOME_URL,
            KEY_SEARCH_ENGINE,
            KEY_DESKTOP_MODE
        )

        fun from(context: Context): SettingsManager {
            return SettingsManager(PreferenceStore.from(context))
        }
    }
}
