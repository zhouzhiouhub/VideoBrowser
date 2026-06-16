package com.example.videobrowser.settings

/**
 * 初学者阅读提示：
 * 这个文件属于“设置模块”。
 * 文件名 SessionSitePermissionStore 可以拆开理解为“Session Site Permission Store”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：封装浏览器设置、站点级开关、权限记录和恢复默认设置逻辑。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import com.example.videobrowser.site.SiteHost

class SessionSitePermissionStore {
    private val grants = mutableSetOf<SessionSitePermissionGrant>()

    fun allow(host: String?, permission: SitePermission): Boolean {
        val normalizedHost = SiteHost.normalize(host) ?: return false
        grants += SessionSitePermissionGrant(normalizedHost, permission)
        return true
    }

    fun isAllowed(host: String?, permission: SitePermission): Boolean {
        val normalizedHost = SiteHost.normalize(host) ?: return false
        return SessionSitePermissionGrant(normalizedHost, permission) in grants
    }

    fun clear() {
        grants.clear()
    }

    private data class SessionSitePermissionGrant(
        val host: String,
        val permission: SitePermission
    )
}
