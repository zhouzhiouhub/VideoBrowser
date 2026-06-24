package com.example.videobrowser.settings

/**
 * 设置模块对外共享的数据模型。
 *
 * SettingsManager 负责读写 PreferenceStore；这些类型只表达调用方会传递或展示的设置数据。
 */
data class UserElementHideRule(
    val host: String,
    val selector: String
)

data class CustomShortcut(
    val name: String,
    val url: String
)

data class CustomSearchEngine(
    val id: String,
    val name: String,
    val searchUrlPrefix: String
)

data class SitePermissionRecord(
    val host: String,
    val permission: SitePermission,
    val decision: SitePermissionDecision
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
