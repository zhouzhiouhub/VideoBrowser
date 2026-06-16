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

    /**
     * 函数 `allow`：封装 `allow` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @param permission 参数类型为 `SitePermission`，表示函数执行 `permission` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun allow(host: String?, permission: SitePermission): Boolean {
        val normalizedHost = SiteHost.normalize(host) ?: return false
        grants += SessionSitePermissionGrant(normalizedHost, permission)
        return true
    }

    /**
     * 函数 `isAllowed`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @param permission 参数类型为 `SitePermission`，表示函数执行 `permission` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isAllowed(host: String?, permission: SitePermission): Boolean {
        val normalizedHost = SiteHost.normalize(host) ?: return false
        return SessionSitePermissionGrant(normalizedHost, permission) in grants
    }

    /**
     * 函数 `clear`：封装 `clear` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun clear() {
        grants.clear()
    }

    private data class SessionSitePermissionGrant(
        val host: String,
        val permission: SitePermission
    )
}
