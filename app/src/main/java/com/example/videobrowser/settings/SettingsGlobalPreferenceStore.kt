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
    private val adBlockPreference = BooleanPreference(KEY_AD_BLOCK, DEFAULT_AD_BLOCK_ENABLED)
    private val jsInjectionPreference = BooleanPreference(
        KEY_JS_INJECTION,
        DEFAULT_JS_INJECTION_ENABLED
    )
    private val domAdBlockPreference = BooleanPreference(
        KEY_DOM_AD_BLOCK,
        DEFAULT_DOM_AD_BLOCK_ENABLED
    )
    private val videoEnhancementPreference = BooleanPreference(
        KEY_VIDEO_ENHANCEMENT,
        DEFAULT_VIDEO_ENHANCEMENT_ENABLED
    )
    private val startVideosFromBeginningPreference = BooleanPreference(
        KEY_ALWAYS_START_VIDEOS_FROM_BEGINNING,
        DEFAULT_ALWAYS_START_VIDEOS_FROM_BEGINNING
    )
    private val smartNoImagePreference = BooleanPreference(
        KEY_SMART_NO_IMAGE,
        DEFAULT_SMART_NO_IMAGE_ENABLED
    )
    private val thirdPartyCookiesPreference = BooleanPreference(
        KEY_THIRD_PARTY_COOKIES,
        DEFAULT_THIRD_PARTY_COOKIES_ENABLED
    )
    private val mixedContentBlockedPreference = BooleanPreference(
        KEY_MIXED_CONTENT_BLOCKED,
        DEFAULT_MIXED_CONTENT_BLOCKED
    )
    private val desktopModePreference = BooleanPreference(
        KEY_DESKTOP_MODE,
        DEFAULT_DESKTOP_MODE_ENABLED
    )

    fun isAdBlockEnabled(): Boolean {
        return getBooleanPreference(adBlockPreference)
    }

    fun setAdBlockEnabled(enabled: Boolean) {
        setBooleanPreference(adBlockPreference, enabled)
    }

    fun isJsInjectionEnabled(): Boolean {
        return getBooleanPreference(jsInjectionPreference)
    }

    fun setJsInjectionEnabled(enabled: Boolean) {
        setBooleanPreference(jsInjectionPreference, enabled)
    }

    fun isDomAdBlockEnabled(): Boolean {
        return getBooleanPreference(domAdBlockPreference)
    }

    fun setDomAdBlockEnabled(enabled: Boolean) {
        setBooleanPreference(domAdBlockPreference, enabled)
    }

    fun isVideoEnhancementEnabled(): Boolean {
        return getBooleanPreference(videoEnhancementPreference)
    }

    fun setVideoEnhancementEnabled(enabled: Boolean) {
        setBooleanPreference(videoEnhancementPreference, enabled)
    }

    fun alwaysStartVideosFromBeginning(): Boolean {
        return getBooleanPreference(startVideosFromBeginningPreference)
    }

    fun setAlwaysStartVideosFromBeginning(enabled: Boolean) {
        setBooleanPreference(startVideosFromBeginningPreference, enabled)
    }

    fun isSmartNoImageEnabled(): Boolean {
        return getBooleanPreference(smartNoImagePreference)
    }

    fun setSmartNoImageEnabled(enabled: Boolean) {
        setBooleanPreference(smartNoImagePreference, enabled)
    }

    fun areThirdPartyCookiesEnabled(): Boolean {
        return getBooleanPreference(thirdPartyCookiesPreference)
    }

    fun setThirdPartyCookiesEnabled(enabled: Boolean) {
        setBooleanPreference(thirdPartyCookiesPreference, enabled)
    }

    fun isMixedContentBlocked(): Boolean {
        return getBooleanPreference(mixedContentBlockedPreference)
    }

    fun setMixedContentBlocked(blocked: Boolean) {
        setBooleanPreference(mixedContentBlockedPreference, blocked)
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
        return getBooleanPreference(desktopModePreference)
    }

    fun setDesktopModeEnabled(enabled: Boolean) {
        setBooleanPreference(desktopModePreference, enabled)
    }

    fun isPrivateBrowsingEnabled(): Boolean {
        preferenceStore.remove(KEY_PRIVATE_BROWSING)
        return DEFAULT_PRIVATE_BROWSING_ENABLED
    }

    fun setPrivateBrowsingEnabled(enabled: Boolean) {
        preferenceStore.remove(KEY_PRIVATE_BROWSING)
    }

    private fun getBooleanPreference(preference: BooleanPreference): Boolean {
        return preferenceStore.getBoolean(preference.key, preference.defaultValue)
    }

    private fun setBooleanPreference(preference: BooleanPreference, value: Boolean) {
        preferenceStore.putBoolean(preference.key, value)
    }

    private data class BooleanPreference(
        val key: String,
        val defaultValue: Boolean
    )
}
