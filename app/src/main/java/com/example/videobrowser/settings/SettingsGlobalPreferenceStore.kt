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
    private val defaultVideoSpeedPreference = FloatPreference(
        KEY_DEFAULT_VIDEO_SPEED,
        SettingsManager.DEFAULT_VIDEO_SPEED
    )
    private val textZoomPercentPreference = FloatPreference(
        KEY_TEXT_ZOOM_PERCENT,
        SettingsManager.DEFAULT_TEXT_ZOOM_PERCENT.toFloat()
    )
    private val homeUrlPreference = StringPreference(KEY_HOME_URL, null)
    private val searchEnginePreference = StringPreference(
        KEY_SEARCH_ENGINE,
        SettingsManager.DEFAULT_SEARCH_ENGINE_ID
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
            getFloatPreference(defaultVideoSpeedPreference)
        )
    }

    fun setDefaultVideoSpeed(speed: Float) {
        setFloatPreference(defaultVideoSpeedPreference, SettingsValueNormalizer.videoSpeed(speed))
    }

    fun textZoomPercent(): Int {
        return SettingsValueNormalizer.textZoomPercent(
            getFloatPreference(textZoomPercentPreference).toInt()
        )
    }

    fun setTextZoomPercent(percent: Int) {
        setFloatPreference(
            textZoomPercentPreference,
            SettingsValueNormalizer.textZoomPercent(percent).toFloat()
        )
    }

    fun homeUrl(): String {
        return SettingsValueNormalizer.homeUrl(
            getStringPreference(homeUrlPreference),
            SettingsManager.DEFAULT_HOME_URL
        )
    }

    fun homeUrlOr(defaultValue: String): String {
        return SettingsValueNormalizer.homeUrl(
            getStringPreference(homeUrlPreference),
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
        setStringPreference(
            homeUrlPreference,
            SettingsValueNormalizer.homeUrl(url, SettingsManager.DEFAULT_HOME_URL)
        )
    }

    fun searchEngineId(): String {
        return getStringPreference(searchEnginePreference)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: SettingsManager.DEFAULT_SEARCH_ENGINE_ID
    }

    fun setSearchEngineId(id: String) {
        val normalizedId = id.trim().takeIf { it.isNotEmpty() }
            ?: SettingsManager.DEFAULT_SEARCH_ENGINE_ID
        setStringPreference(searchEnginePreference, normalizedId)
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

    private fun getFloatPreference(preference: FloatPreference): Float {
        return preferenceStore.getFloat(preference.key, preference.defaultValue)
    }

    private fun setFloatPreference(preference: FloatPreference, value: Float) {
        preferenceStore.putFloat(preference.key, value)
    }

    private fun getStringPreference(preference: StringPreference): String? {
        return preferenceStore.getString(preference.key, preference.defaultValue)
    }

    private fun setStringPreference(preference: StringPreference, value: String) {
        preferenceStore.putString(preference.key, value)
    }

    private data class BooleanPreference(
        val key: String,
        val defaultValue: Boolean
    )

    private data class FloatPreference(
        val key: String,
        val defaultValue: Float
    )

    private data class StringPreference(
        val key: String,
        val defaultValue: String?
    )
}
