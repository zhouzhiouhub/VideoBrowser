package com.example.videobrowser.settings

import com.example.videobrowser.storage.PreferenceStore

/**
 * 负责全局浏览器偏好设置的持久化读写。
 *
 * SettingsManager 保留公开入口；这个类只处理 PreferenceStore 中不依赖站点或权限记录的设置项。
 */
internal class SettingsGlobalPreferenceStore(
    private val preferenceStore: PreferenceStore
) {
    fun isAdBlockEnabled(): Boolean {
        return preferenceStore.getBoolean(KEY_AD_BLOCK, DEFAULT_AD_BLOCK_ENABLED)
    }

    fun setAdBlockEnabled(enabled: Boolean) {
        preferenceStore.putBoolean(KEY_AD_BLOCK, enabled)
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

    fun alwaysStartVideosFromBeginning(): Boolean {
        return preferenceStore.getBoolean(
            KEY_ALWAYS_START_VIDEOS_FROM_BEGINNING,
            DEFAULT_ALWAYS_START_VIDEOS_FROM_BEGINNING
        )
    }

    fun setAlwaysStartVideosFromBeginning(enabled: Boolean) {
        preferenceStore.putBoolean(KEY_ALWAYS_START_VIDEOS_FROM_BEGINNING, enabled)
    }

    fun isSmartNoImageEnabled(): Boolean {
        return preferenceStore.getBoolean(KEY_SMART_NO_IMAGE, DEFAULT_SMART_NO_IMAGE_ENABLED)
    }

    fun setSmartNoImageEnabled(enabled: Boolean) {
        preferenceStore.putBoolean(KEY_SMART_NO_IMAGE, enabled)
    }

    fun areThirdPartyCookiesEnabled(): Boolean {
        return preferenceStore.getBoolean(
            KEY_THIRD_PARTY_COOKIES,
            DEFAULT_THIRD_PARTY_COOKIES_ENABLED
        )
    }

    fun setThirdPartyCookiesEnabled(enabled: Boolean) {
        preferenceStore.putBoolean(KEY_THIRD_PARTY_COOKIES, enabled)
    }

    fun isMixedContentBlocked(): Boolean {
        return preferenceStore.getBoolean(KEY_MIXED_CONTENT_BLOCKED, DEFAULT_MIXED_CONTENT_BLOCKED)
    }

    fun setMixedContentBlocked(blocked: Boolean) {
        preferenceStore.putBoolean(KEY_MIXED_CONTENT_BLOCKED, blocked)
    }

    fun defaultVideoSpeed(): Float {
        return SettingsValueNormalizer.videoSpeed(
            preferenceStore.getFloat(KEY_DEFAULT_VIDEO_SPEED, SettingsManager.DEFAULT_VIDEO_SPEED)
        )
    }

    fun setDefaultVideoSpeed(speed: Float) {
        preferenceStore.putFloat(KEY_DEFAULT_VIDEO_SPEED, SettingsValueNormalizer.videoSpeed(speed))
    }

    fun textZoomPercent(): Int {
        return SettingsValueNormalizer.textZoomPercent(
            preferenceStore.getFloat(
                KEY_TEXT_ZOOM_PERCENT,
                SettingsManager.DEFAULT_TEXT_ZOOM_PERCENT.toFloat()
            ).toInt()
        )
    }

    fun setTextZoomPercent(percent: Int) {
        preferenceStore.putFloat(
            KEY_TEXT_ZOOM_PERCENT,
            SettingsValueNormalizer.textZoomPercent(percent).toFloat()
        )
    }

    fun homeUrl(): String {
        return SettingsValueNormalizer.homeUrl(
            preferenceStore.getString(KEY_HOME_URL, null),
            SettingsManager.DEFAULT_HOME_URL
        )
    }

    fun homeUrlOr(defaultValue: String): String {
        return SettingsValueNormalizer.homeUrl(
            preferenceStore.getString(KEY_HOME_URL, null),
            defaultValue
        )
    }

    fun hasHomeUrl(): Boolean {
        return preferenceStore.contains(KEY_HOME_URL)
    }

    fun isValidHomeUrl(url: String): Boolean {
        return SettingsValueNormalizer.homeUrlOrNull(url) != null
    }

    fun setHomeUrl(url: String) {
        preferenceStore.putString(
            KEY_HOME_URL,
            SettingsValueNormalizer.homeUrl(url, SettingsManager.DEFAULT_HOME_URL)
        )
    }

    fun searchEngineId(): String {
        return preferenceStore.getString(
            KEY_SEARCH_ENGINE,
            SettingsManager.DEFAULT_SEARCH_ENGINE_ID
        )
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: SettingsManager.DEFAULT_SEARCH_ENGINE_ID
    }

    fun setSearchEngineId(id: String) {
        val normalizedId = id.trim().takeIf { it.isNotEmpty() }
            ?: SettingsManager.DEFAULT_SEARCH_ENGINE_ID
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
}
