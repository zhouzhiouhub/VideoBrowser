package com.example.videobrowser.settings

import android.content.Context
import com.example.videobrowser.site.SiteHost
import com.example.videobrowser.storage.PreferenceStore
import java.net.URI

data class UserElementHideRule(
    val host: String,
    val selector: String
)

data class CustomShortcut(
    val name: String,
    val url: String
)

enum class SitePermission {
    CAMERA,
    MICROPHONE,
    LOCATION
}

enum class SitePermissionDecision {
    ASK,
    ALLOW,
    BLOCK
}

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

    fun isSmartNoImageDisabledForSite(host: String?): Boolean {
        val normalizedHost = SiteHost.normalize(host) ?: return false
        return smartNoImageDisabledSiteHosts().contains(normalizedHost)
    }

    fun setSmartNoImageDisabledForSite(host: String?, disabled: Boolean): Boolean {
        val normalizedHost = SiteHost.normalize(host) ?: return false
        val hosts = smartNoImageDisabledSiteHosts().toMutableSet()
        if (disabled) {
            hosts.add(normalizedHost)
        } else {
            hosts.remove(normalizedHost)
        }

        saveHostSet(KEY_SITE_SMART_NO_IMAGE_DISABLED_HOSTS, hosts)
        return true
    }

    fun smartNoImageDisabledSiteHosts(): Set<String> {
        return loadHostSet(KEY_SITE_SMART_NO_IMAGE_DISABLED_HOSTS)
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

    fun clearUserWhitelistedSites() {
        preferenceStore.remove(KEY_USER_WHITELISTED_SITE_HOSTS)
    }

    fun sitePermissionDecision(host: String?, permission: SitePermission): SitePermissionDecision {
        val normalizedHost = SiteHost.normalize(host) ?: return SitePermissionDecision.ASK
        return when {
            allowedSitePermissionHosts(permission).contains(normalizedHost) -> SitePermissionDecision.ALLOW
            blockedSitePermissionHosts(permission).contains(normalizedHost) -> SitePermissionDecision.BLOCK
            else -> SitePermissionDecision.ASK
        }
    }

    fun setSitePermissionDecision(
        host: String?,
        permission: SitePermission,
        decision: SitePermissionDecision
    ): Boolean {
        val normalizedHost = SiteHost.normalize(host) ?: return false
        val allowedHosts = allowedSitePermissionHosts(permission).toMutableSet()
        val blockedHosts = blockedSitePermissionHosts(permission).toMutableSet()

        allowedHosts.remove(normalizedHost)
        blockedHosts.remove(normalizedHost)
        when (decision) {
            SitePermissionDecision.ALLOW -> allowedHosts.add(normalizedHost)
            SitePermissionDecision.BLOCK -> blockedHosts.add(normalizedHost)
            SitePermissionDecision.ASK -> Unit
        }

        saveHostSet(sitePermissionAllowedKey(permission), allowedHosts)
        saveHostSet(sitePermissionBlockedKey(permission), blockedHosts)
        return true
    }

    fun allowedSitePermissionHosts(permission: SitePermission): Set<String> {
        return loadHostSet(sitePermissionAllowedKey(permission))
    }

    fun blockedSitePermissionHosts(permission: SitePermission): Set<String> {
        return loadHostSet(sitePermissionBlockedKey(permission))
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

    fun removeUserElementHideRule(rule: UserElementHideRule): Boolean {
        val normalizedHost = SiteHost.normalize(rule.host) ?: return false
        val normalizedSelector = normalizeUserElementSelector(rule.selector) ?: return false
        val rules = userElementHideRules()
        val remainingRules = rules.filterNot { existingRule ->
            existingRule.host == normalizedHost && existingRule.selector == normalizedSelector
        }
        if (remainingRules.size == rules.size) {
            return false
        }

        saveUserElementHideRules(remainingRules)
        return true
    }

    fun clearUserElementHideRules() {
        preferenceStore.remove(KEY_USER_ELEMENT_HIDE_RULES)
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
        return normalizeVideoSpeed(
            preferenceStore.getFloat(KEY_DEFAULT_VIDEO_SPEED, DEFAULT_VIDEO_SPEED)
        )
    }

    fun setDefaultVideoSpeed(speed: Float) {
        preferenceStore.putFloat(KEY_DEFAULT_VIDEO_SPEED, normalizeVideoSpeed(speed))
    }

    fun textZoomPercent(): Int {
        return normalizeTextZoomPercent(
            preferenceStore.getFloat(
                KEY_TEXT_ZOOM_PERCENT,
                DEFAULT_TEXT_ZOOM_PERCENT.toFloat()
            ).toInt()
        )
    }

    fun setTextZoomPercent(percent: Int) {
        preferenceStore.putFloat(
            KEY_TEXT_ZOOM_PERCENT,
            normalizeTextZoomPercent(percent).toFloat()
        )
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

    fun isValidHomeUrl(url: String): Boolean {
        return normalizeHomeUrlOrNull(url) != null
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

    fun customShortcuts(): List<CustomShortcut> {
        return preferenceStore.getString(KEY_CUSTOM_SHORTCUTS, null)
            ?.lineSequence()
            ?.mapNotNull(::parseCustomShortcut)
            ?.distinct()
            ?.toList()
            ?.takeLast(MAX_CUSTOM_SHORTCUTS)
            ?: emptyList()
    }

    fun addCustomShortcut(name: String, url: String): Boolean {
        val shortcut = normalizeCustomShortcut(name, url) ?: return false
        val shortcuts = customShortcuts()
            .filterNot { existing -> existing == shortcut }
            .plus(shortcut)
            .takeLast(MAX_CUSTOM_SHORTCUTS)
        saveCustomShortcuts(shortcuts)
        return true
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

    private fun sitePermissionAllowedKey(permission: SitePermission): String {
        return when (permission) {
            SitePermission.CAMERA -> KEY_SITE_PERMISSION_CAMERA_ALLOWED_HOSTS
            SitePermission.MICROPHONE -> KEY_SITE_PERMISSION_MICROPHONE_ALLOWED_HOSTS
            SitePermission.LOCATION -> KEY_SITE_PERMISSION_LOCATION_ALLOWED_HOSTS
        }
    }

    private fun sitePermissionBlockedKey(permission: SitePermission): String {
        return when (permission) {
            SitePermission.CAMERA -> KEY_SITE_PERMISSION_CAMERA_BLOCKED_HOSTS
            SitePermission.MICROPHONE -> KEY_SITE_PERMISSION_MICROPHONE_BLOCKED_HOSTS
            SitePermission.LOCATION -> KEY_SITE_PERMISSION_LOCATION_BLOCKED_HOSTS
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
        val collapsed = selector.trim().replace(Regex("\\s+"), " ")
        val normalized = stabilizeUserElementSelector(collapsed)
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

    private fun stabilizeUserElementSelector(selector: String): String {
        if (!selector.contains(":nth-of-type", ignoreCase = true) ||
            !USER_ELEMENT_STABLE_TOKEN_REGEX.containsMatchIn(selector)
        ) {
            return selector
        }
        return USER_ELEMENT_POSITIONAL_SEGMENT_REGEX
            .replace(selector, "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun normalizeVideoSpeed(speed: Float): Float {
        return if (!speed.isNaN() && !speed.isInfinite() && speed > 0f) {
            speed
        } else {
            DEFAULT_VIDEO_SPEED
        }
    }

    private fun normalizeTextZoomPercent(percent: Int): Int {
        return percent.takeIf { value -> value in TEXT_ZOOM_OPTIONS }
            ?: DEFAULT_TEXT_ZOOM_PERCENT
    }

    private fun normalizeHomeUrl(value: String?, defaultValue: String): String {
        return normalizeHomeUrlOrNull(value) ?: defaultValue
    }

    private fun normalizeHomeUrlOrNull(value: String?): String? {
        return value
            ?.trim()
            ?.takeIf(::isHttpUrl)
    }

    private fun parseCustomShortcut(line: String): CustomShortcut? {
        val separatorIndex = line.indexOf('\t')
        if (separatorIndex <= 0 || separatorIndex >= line.lastIndex) {
            return null
        }
        return normalizeCustomShortcut(
            line.substring(0, separatorIndex),
            line.substring(separatorIndex + 1)
        )
    }

    private fun saveCustomShortcuts(shortcuts: List<CustomShortcut>) {
        val lines = shortcuts
            .mapNotNull { shortcut -> normalizeCustomShortcut(shortcut.name, shortcut.url) }
            .distinct()
            .takeLast(MAX_CUSTOM_SHORTCUTS)
            .map { shortcut -> "${shortcut.name}\t${shortcut.url}" }

        if (lines.isEmpty()) {
            preferenceStore.remove(KEY_CUSTOM_SHORTCUTS)
        } else {
            preferenceStore.putString(KEY_CUSTOM_SHORTCUTS, lines.joinToString(separator = "\n"))
        }
    }

    private fun normalizeCustomShortcut(name: String, url: String): CustomShortcut? {
        val normalizedName = name.trim().replace(Regex("\\s+"), " ")
        val normalizedUrl = url.trim()
        if (normalizedName.isEmpty() || normalizedName.any { it == '\t' || it == '\n' || it == '\r' }) {
            return null
        }
        if (!isHttpUrl(normalizedUrl)) {
            return null
        }
        return CustomShortcut(name = normalizedName, url = normalizedUrl)
    }

    private fun isHttpUrl(url: String): Boolean {
        val uri = runCatching { URI(url) }.getOrNull() ?: return false
        val scheme = uri.scheme ?: return false
        return (scheme.equals("http", ignoreCase = true) ||
            scheme.equals("https", ignoreCase = true)) &&
            !uri.host.isNullOrBlank()
    }

    companion object {
        const val DEFAULT_SEARCH_ENGINE_ID = "baidu"
        const val DEFAULT_HOME_URL = "https://m.baidu.com/"
        const val DEFAULT_VIDEO_SPEED = 1f
        const val DEFAULT_TEXT_ZOOM_PERCENT = 100
        val TEXT_ZOOM_OPTIONS = listOf(75, 100, 125, 150, 200)

        private const val DEFAULT_AD_BLOCK_ENABLED = true
        private const val DEFAULT_JS_INJECTION_ENABLED = true
        private const val DEFAULT_DOM_AD_BLOCK_ENABLED = true
        private const val DEFAULT_VIDEO_ENHANCEMENT_ENABLED = true
        private const val DEFAULT_ALWAYS_START_VIDEOS_FROM_BEGINNING = false
        private const val DEFAULT_SMART_NO_IMAGE_ENABLED = false
        private const val DEFAULT_THIRD_PARTY_COOKIES_ENABLED = false
        private const val DEFAULT_MIXED_CONTENT_BLOCKED = true
        private const val DEFAULT_DESKTOP_MODE_ENABLED = false
        private const val DEFAULT_PRIVATE_BROWSING_ENABLED = false

        private const val KEY_AD_BLOCK = "ad_block"
        private const val KEY_SITE_AD_BLOCK_DISABLED_HOSTS = "site_ad_block_disabled_hosts"
        private const val KEY_SITE_JS_INJECTION_DISABLED_HOSTS = "site_js_injection_disabled_hosts"
        private const val KEY_SITE_DOM_AD_BLOCK_DISABLED_HOSTS = "site_dom_ad_block_disabled_hosts"
        private const val KEY_SITE_VIDEO_ENHANCEMENT_DISABLED_HOSTS =
            "site_video_enhancement_disabled_hosts"
        private const val KEY_SITE_SMART_NO_IMAGE_DISABLED_HOSTS =
            "site_smart_no_image_disabled_hosts"
        private const val KEY_USER_WHITELISTED_SITE_HOSTS = "user_whitelisted_site_hosts"
        private const val KEY_SITE_PERMISSION_CAMERA_ALLOWED_HOSTS =
            "site_permission_camera_allowed_hosts"
        private const val KEY_SITE_PERMISSION_CAMERA_BLOCKED_HOSTS =
            "site_permission_camera_blocked_hosts"
        private const val KEY_SITE_PERMISSION_MICROPHONE_ALLOWED_HOSTS =
            "site_permission_microphone_allowed_hosts"
        private const val KEY_SITE_PERMISSION_MICROPHONE_BLOCKED_HOSTS =
            "site_permission_microphone_blocked_hosts"
        private const val KEY_SITE_PERMISSION_LOCATION_ALLOWED_HOSTS =
            "site_permission_location_allowed_hosts"
        private const val KEY_SITE_PERMISSION_LOCATION_BLOCKED_HOSTS =
            "site_permission_location_blocked_hosts"
        private const val KEY_USER_ELEMENT_HIDE_RULES = "user_element_hide_rules"
        private const val KEY_JS_INJECTION = "js_injection"
        private const val KEY_DOM_AD_BLOCK = "page_cleanup"
        private const val KEY_VIDEO_ENHANCEMENT = "video_enhancement"
        private const val KEY_ALWAYS_START_VIDEOS_FROM_BEGINNING =
            "always_start_videos_from_beginning"
        private const val KEY_SMART_NO_IMAGE = "smart_no_image"
        private const val KEY_THIRD_PARTY_COOKIES = "third_party_cookies"
        private const val KEY_MIXED_CONTENT_BLOCKED = "mixed_content_blocked"
        private const val KEY_DEFAULT_VIDEO_SPEED = "default_video_speed"
        private const val KEY_TEXT_ZOOM_PERCENT = "text_zoom_percent"
        private const val KEY_HOME_URL = "home_url"
        private const val KEY_SEARCH_ENGINE = "search_provider"
        private const val KEY_CUSTOM_SHORTCUTS = "custom_shortcuts"
        private const val KEY_DESKTOP_MODE = "desktop_mode"
        private const val KEY_PRIVATE_BROWSING = "private_browsing"
        private const val MAX_USER_ELEMENT_SELECTOR_LENGTH = 200
        private const val MAX_CUSTOM_SHORTCUTS = 10
        private val USER_ELEMENT_POSITIONAL_SEGMENT_REGEX =
            Regex(":nth-of-type\\(\\s*\\d+\\s*\\)", RegexOption.IGNORE_CASE)
        private val USER_ELEMENT_STABLE_TOKEN_REGEX = Regex("[#.][A-Za-z_][A-Za-z0-9_-]*")

        private val RESET_KEYS = listOf(
            KEY_AD_BLOCK,
            KEY_SITE_AD_BLOCK_DISABLED_HOSTS,
            KEY_SITE_JS_INJECTION_DISABLED_HOSTS,
            KEY_SITE_DOM_AD_BLOCK_DISABLED_HOSTS,
            KEY_SITE_VIDEO_ENHANCEMENT_DISABLED_HOSTS,
            KEY_SITE_SMART_NO_IMAGE_DISABLED_HOSTS,
            KEY_USER_WHITELISTED_SITE_HOSTS,
            KEY_SITE_PERMISSION_CAMERA_ALLOWED_HOSTS,
            KEY_SITE_PERMISSION_CAMERA_BLOCKED_HOSTS,
            KEY_SITE_PERMISSION_MICROPHONE_ALLOWED_HOSTS,
            KEY_SITE_PERMISSION_MICROPHONE_BLOCKED_HOSTS,
            KEY_SITE_PERMISSION_LOCATION_ALLOWED_HOSTS,
            KEY_SITE_PERMISSION_LOCATION_BLOCKED_HOSTS,
            KEY_USER_ELEMENT_HIDE_RULES,
            KEY_JS_INJECTION,
            KEY_DOM_AD_BLOCK,
            KEY_VIDEO_ENHANCEMENT,
            KEY_ALWAYS_START_VIDEOS_FROM_BEGINNING,
            KEY_SMART_NO_IMAGE,
            KEY_THIRD_PARTY_COOKIES,
            KEY_MIXED_CONTENT_BLOCKED,
            KEY_DEFAULT_VIDEO_SPEED,
            KEY_TEXT_ZOOM_PERCENT,
            KEY_HOME_URL,
            KEY_SEARCH_ENGINE,
            KEY_CUSTOM_SHORTCUTS,
            KEY_DESKTOP_MODE,
            KEY_PRIVATE_BROWSING
        )

        fun from(context: Context): SettingsManager {
            return SettingsManager(PreferenceStore.from(context))
        }
    }
}
