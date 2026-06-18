package com.example.videobrowser.settings

/**
 * 设置模块的 SharedPreferences 键和内部默认值。
 *
 * SettingsManager 负责业务读写流程；这个文件只描述持久化字段名、内部默认开关和
 * “恢复默认设置”需要清理的键，避免主读写类被配置清单占满。
 */
internal const val DEFAULT_AD_BLOCK_ENABLED = true
internal const val DEFAULT_JS_INJECTION_ENABLED = true
internal const val DEFAULT_DOM_AD_BLOCK_ENABLED = true
internal const val DEFAULT_VIDEO_ENHANCEMENT_ENABLED = true
internal const val DEFAULT_ALWAYS_START_VIDEOS_FROM_BEGINNING = false
internal const val DEFAULT_SMART_NO_IMAGE_ENABLED = false
internal const val DEFAULT_THIRD_PARTY_COOKIES_ENABLED = false
internal const val DEFAULT_MIXED_CONTENT_BLOCKED = true
internal const val DEFAULT_DESKTOP_MODE_ENABLED = false
internal const val DEFAULT_PRIVATE_BROWSING_ENABLED = false

internal const val KEY_AD_BLOCK = "ad_block"
internal const val KEY_SITE_AD_BLOCK_DISABLED_HOSTS = "site_ad_block_disabled_hosts"
internal const val KEY_SITE_JS_INJECTION_DISABLED_HOSTS = "site_js_injection_disabled_hosts"
internal const val KEY_SITE_DOM_AD_BLOCK_DISABLED_HOSTS = "site_dom_ad_block_disabled_hosts"
internal const val KEY_SITE_VIDEO_ENHANCEMENT_DISABLED_HOSTS =
    "site_video_enhancement_disabled_hosts"
internal const val KEY_SITE_SMART_NO_IMAGE_DISABLED_HOSTS =
    "site_smart_no_image_disabled_hosts"
internal const val KEY_USER_WHITELISTED_SITE_HOSTS = "user_whitelisted_site_hosts"
internal const val KEY_SITE_PERMISSION_CAMERA_ALLOWED_HOSTS =
    "site_permission_camera_allowed_hosts"
internal const val KEY_SITE_PERMISSION_CAMERA_BLOCKED_HOSTS =
    "site_permission_camera_blocked_hosts"
internal const val KEY_SITE_PERMISSION_MICROPHONE_ALLOWED_HOSTS =
    "site_permission_microphone_allowed_hosts"
internal const val KEY_SITE_PERMISSION_MICROPHONE_BLOCKED_HOSTS =
    "site_permission_microphone_blocked_hosts"
internal const val KEY_SITE_PERMISSION_LOCATION_ALLOWED_HOSTS =
    "site_permission_location_allowed_hosts"
internal const val KEY_SITE_PERMISSION_LOCATION_BLOCKED_HOSTS =
    "site_permission_location_blocked_hosts"
internal const val KEY_USER_ELEMENT_HIDE_RULES = "user_element_hide_rules"
internal const val KEY_JS_INJECTION = "js_injection"
internal const val KEY_DOM_AD_BLOCK = "page_cleanup"
internal const val KEY_VIDEO_ENHANCEMENT = "video_enhancement"
internal const val KEY_ALWAYS_START_VIDEOS_FROM_BEGINNING =
    "always_start_videos_from_beginning"
internal const val KEY_SMART_NO_IMAGE = "smart_no_image"
internal const val KEY_THIRD_PARTY_COOKIES = "third_party_cookies"
internal const val KEY_MIXED_CONTENT_BLOCKED = "mixed_content_blocked"
internal const val KEY_DEFAULT_VIDEO_SPEED = "default_video_speed"
internal const val KEY_TEXT_ZOOM_PERCENT = "text_zoom_percent"
internal const val KEY_HOME_URL = "home_url"
internal const val KEY_SEARCH_ENGINE = "search_provider"
internal const val KEY_CUSTOM_SHORTCUTS = "custom_shortcuts"
internal const val KEY_DESKTOP_MODE = "desktop_mode"
internal const val KEY_PRIVATE_BROWSING = "private_browsing"

internal const val MAX_USER_ELEMENT_SELECTOR_LENGTH = 200
internal const val MAX_CUSTOM_SHORTCUTS = 10

internal val RESET_KEYS = listOf(
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
