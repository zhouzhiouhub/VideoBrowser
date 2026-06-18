package com.example.videobrowser.settings

/**
 * 初学者阅读提示：
 * 这个文件属于“设置模块”。
 * 文件名 SettingsManager 可以拆开理解为“Settings Manager”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：封装浏览器设置、站点级开关、权限记录和恢复默认设置逻辑。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
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

/**
 * 应用设置读写入口。
 *
 * 这个类把 PreferenceStore 里的字符串/布尔值包装成有业务含义的函数：
 * 全局开关、站点级开关、站点权限、自定义快捷入口、主页、搜索引擎等都从这里读写。
 */
class SettingsManager(
    private val preferenceStore: PreferenceStore
) {
    /**
     * 函数 `isAdBlockEnabled`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isAdBlockEnabled(): Boolean {
        return preferenceStore.getBoolean(KEY_AD_BLOCK, DEFAULT_AD_BLOCK_ENABLED)
    }

    /**
     * 函数 `setAdBlockEnabled`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param enabled 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     */
    fun setAdBlockEnabled(enabled: Boolean) {
        preferenceStore.putBoolean(KEY_AD_BLOCK, enabled)
    }

    /**
     * 函数 `isAdBlockDisabledForSite`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `adBlockDisabledSiteHosts`：封装 `ad Block Disabled Site Hosts` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun adBlockDisabledSiteHosts(): Set<String> {
        return preferenceStore.getString(KEY_SITE_AD_BLOCK_DISABLED_HOSTS, null)
            ?.lineSequence()
            ?.mapNotNull(SiteHost::normalize)
            ?.toSet()
            ?: emptySet()
    }

    /**
     * 函数 `isJsInjectionDisabledForSite`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isJsInjectionDisabledForSite(host: String?): Boolean {
        val normalizedHost = SiteHost.normalize(host) ?: return false
        return jsInjectionDisabledSiteHosts().contains(normalizedHost)
    }

    /**
     * 函数 `setJsInjectionDisabledForSite`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @param disabled 参数类型为 `Boolean`，表示函数执行 `disabled` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `jsInjectionDisabledSiteHosts`：封装 `js Injection Disabled Site Hosts` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun jsInjectionDisabledSiteHosts(): Set<String> {
        return loadHostSet(KEY_SITE_JS_INJECTION_DISABLED_HOSTS)
    }

    /**
     * 函数 `isDomAdBlockDisabledForSite`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isDomAdBlockDisabledForSite(host: String?): Boolean {
        val normalizedHost = SiteHost.normalize(host) ?: return false
        return domAdBlockDisabledSiteHosts().contains(normalizedHost)
    }

    /**
     * 函数 `setDomAdBlockDisabledForSite`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @param disabled 参数类型为 `Boolean`，表示函数执行 `disabled` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `domAdBlockDisabledSiteHosts`：封装 `dom Ad Block Disabled Site Hosts` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun domAdBlockDisabledSiteHosts(): Set<String> {
        return loadHostSet(KEY_SITE_DOM_AD_BLOCK_DISABLED_HOSTS)
    }

    /**
     * 函数 `isVideoEnhancementDisabledForSite`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isVideoEnhancementDisabledForSite(host: String?): Boolean {
        val normalizedHost = SiteHost.normalize(host) ?: return false
        return videoEnhancementDisabledSiteHosts().contains(normalizedHost)
    }

    /**
     * 函数 `setVideoEnhancementDisabledForSite`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @param disabled 参数类型为 `Boolean`，表示函数执行 `disabled` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `videoEnhancementDisabledSiteHosts`：封装 `video Enhancement Disabled Site Hosts` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun videoEnhancementDisabledSiteHosts(): Set<String> {
        return loadHostSet(KEY_SITE_VIDEO_ENHANCEMENT_DISABLED_HOSTS)
    }

    /**
     * 函数 `isSmartNoImageDisabledForSite`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isSmartNoImageDisabledForSite(host: String?): Boolean {
        val normalizedHost = SiteHost.normalize(host) ?: return false
        return smartNoImageDisabledSiteHosts().contains(normalizedHost)
    }

    /**
     * 函数 `setSmartNoImageDisabledForSite`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @param disabled 参数类型为 `Boolean`，表示函数执行 `disabled` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `smartNoImageDisabledSiteHosts`：封装 `smart No Image Disabled Site Hosts` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun smartNoImageDisabledSiteHosts(): Set<String> {
        return loadHostSet(KEY_SITE_SMART_NO_IMAGE_DISABLED_HOSTS)
    }

    /**
     * 函数 `isUserWhitelistedSite`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isUserWhitelistedSite(host: String?): Boolean {
        val normalizedHost = SiteHost.normalize(host) ?: return false
        return userWhitelistedSiteHosts().contains(normalizedHost)
    }

    /**
     * 函数 `setUserWhitelistedSite`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @param whitelisted 参数类型为 `Boolean`，表示一组待处理数据，函数会遍历、过滤或转换这些内容。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `userWhitelistedSiteHosts`：封装 `user Whitelisted Site Hosts` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun userWhitelistedSiteHosts(): Set<String> {
        return loadHostSet(KEY_USER_WHITELISTED_SITE_HOSTS)
    }

    /**
     * 函数 `clearUserWhitelistedSites`：封装 `clear User Whitelisted Sites` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun clearUserWhitelistedSites() {
        preferenceStore.remove(KEY_USER_WHITELISTED_SITE_HOSTS)
    }

    /**
     * 函数 `sitePermissionDecision`：封装 `site Permission Decision` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @param permission 参数类型为 `SitePermission`，表示函数执行 `permission` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun sitePermissionDecision(host: String?, permission: SitePermission): SitePermissionDecision {
        val normalizedHost = SiteHost.normalize(host) ?: return SitePermissionDecision.ASK
        return when {
            allowedSitePermissionHosts(permission).contains(normalizedHost) -> SitePermissionDecision.ALLOW
            blockedSitePermissionHosts(permission).contains(normalizedHost) -> SitePermissionDecision.BLOCK
            else -> SitePermissionDecision.ASK
        }
    }

    /**
     * 函数 `setSitePermissionDecision`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @param permission 参数类型为 `SitePermission`，表示函数执行 `permission` 相关逻辑时需要读取或处理的输入。
     * @param decision 参数类型为 `SitePermissionDecision`，表示函数执行 `decision` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun setSitePermissionDecision(
        host: String?,
        permission: SitePermission,
        decision: SitePermissionDecision
    ): Boolean {
        // 每个权限只有一种最终状态：允许、阻止或询问。保存新状态前先从两个集合里移除旧状态。
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

    /**
     * 函数 `allowedSitePermissionHosts`：封装 `allowed Site Permission Hosts` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param permission 参数类型为 `SitePermission`，表示函数执行 `permission` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun allowedSitePermissionHosts(permission: SitePermission): Set<String> {
        return loadHostSet(sitePermissionAllowedKey(permission))
    }

    /**
     * 函数 `blockedSitePermissionHosts`：封装 `blocked Site Permission Hosts` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param permission 参数类型为 `SitePermission`，表示函数执行 `permission` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun blockedSitePermissionHosts(permission: SitePermission): Set<String> {
        return loadHostSet(sitePermissionBlockedKey(permission))
    }

    /**
     * 函数 `sitePermissionRecords`：封装 `site Permission Records` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun sitePermissionRecords(): List<SitePermissionRecord> {
        return SitePermission.entries.flatMap { permission ->
            val allowedHosts = allowedSitePermissionHosts(permission)
            val blockedHosts = blockedSitePermissionHosts(permission)
            allowedHosts.map { host ->
                SitePermissionRecord(
                    host = host,
                    permission = permission,
                    decision = SitePermissionDecision.ALLOW
                )
            } + blockedHosts
                .filterNot { host -> host in allowedHosts }
                .map { host ->
                    SitePermissionRecord(
                        host = host,
                        permission = permission,
                        decision = SitePermissionDecision.BLOCK
                    )
                }
        }.distinct()
    }

    /**
     * 函数 `clearSitePermissionDecisions`：封装 `clear Site Permission Decisions` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun clearSitePermissionDecisions() {
        SitePermission.entries.forEach { permission ->
            preferenceStore.remove(sitePermissionAllowedKey(permission))
            preferenceStore.remove(sitePermissionBlockedKey(permission))
        }
    }

    /**
     * 函数 `userElementHideSelectorsForSite`：封装 `user Element Hide Selectors For Site` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun userElementHideSelectorsForSite(host: String?): List<String> {
        val normalizedHost = SiteHost.normalize(host) ?: return emptyList()
        return userElementHideRules()
            .filter { rule -> rule.host == normalizedHost }
            .map { rule -> rule.selector }
    }

    /**
     * 函数 `hasUserElementHideSelectorForSite`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @param selector 参数类型为 `String`，表示函数执行 `selector` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun hasUserElementHideSelectorForSite(host: String?, selector: String): Boolean {
        val normalizedHost = SiteHost.normalize(host) ?: return false
        val normalizedSelector = normalizeUserElementSelector(selector) ?: return false
        return userElementHideRules().any { rule ->
            rule.host == normalizedHost && rule.selector == normalizedSelector
        }
    }

    /**
     * 函数 `addUserElementHideSelectorForSite`：封装 `add User Element Hide Selector For Site` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @param selector 参数类型为 `String`，表示函数执行 `selector` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `userElementHideRules`：封装 `user Element Hide Rules` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun userElementHideRules(): List<UserElementHideRule> {
        return preferenceStore.getString(KEY_USER_ELEMENT_HIDE_RULES, null)
            ?.lineSequence()
            ?.mapNotNull(::parseUserElementHideRule)
            ?.distinct()
            ?.toList()
            ?: emptyList()
    }

    /**
     * 函数 `removeUserElementHideRule`：封装 `remove User Element Hide Rule` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param rule 参数类型为 `UserElementHideRule`，表示函数执行 `rule` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `clearUserElementHideRules`：封装 `clear User Element Hide Rules` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun clearUserElementHideRules() {
        preferenceStore.remove(KEY_USER_ELEMENT_HIDE_RULES)
    }

    /**
     * 函数 `isJsInjectionEnabled`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isJsInjectionEnabled(): Boolean {
        return preferenceStore.getBoolean(KEY_JS_INJECTION, DEFAULT_JS_INJECTION_ENABLED)
    }

    /**
     * 函数 `setJsInjectionEnabled`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param enabled 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     */
    fun setJsInjectionEnabled(enabled: Boolean) {
        preferenceStore.putBoolean(KEY_JS_INJECTION, enabled)
    }

    /**
     * 函数 `isDomAdBlockEnabled`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isDomAdBlockEnabled(): Boolean {
        return preferenceStore.getBoolean(KEY_DOM_AD_BLOCK, DEFAULT_DOM_AD_BLOCK_ENABLED)
    }

    /**
     * 函数 `setDomAdBlockEnabled`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param enabled 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     */
    fun setDomAdBlockEnabled(enabled: Boolean) {
        preferenceStore.putBoolean(KEY_DOM_AD_BLOCK, enabled)
    }

    /**
     * 函数 `isVideoEnhancementEnabled`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isVideoEnhancementEnabled(): Boolean {
        return preferenceStore.getBoolean(KEY_VIDEO_ENHANCEMENT, DEFAULT_VIDEO_ENHANCEMENT_ENABLED)
    }

    /**
     * 函数 `setVideoEnhancementEnabled`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param enabled 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     */
    fun setVideoEnhancementEnabled(enabled: Boolean) {
        preferenceStore.putBoolean(KEY_VIDEO_ENHANCEMENT, enabled)
    }

    /**
     * 函数 `alwaysStartVideosFromBeginning`：封装 `always Start Videos From Beginning` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun alwaysStartVideosFromBeginning(): Boolean {
        return preferenceStore.getBoolean(
            KEY_ALWAYS_START_VIDEOS_FROM_BEGINNING,
            DEFAULT_ALWAYS_START_VIDEOS_FROM_BEGINNING
        )
    }

    /**
     * 函数 `setAlwaysStartVideosFromBeginning`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param enabled 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     */
    fun setAlwaysStartVideosFromBeginning(enabled: Boolean) {
        preferenceStore.putBoolean(KEY_ALWAYS_START_VIDEOS_FROM_BEGINNING, enabled)
    }

    /**
     * 函数 `isSmartNoImageEnabled`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isSmartNoImageEnabled(): Boolean {
        return preferenceStore.getBoolean(KEY_SMART_NO_IMAGE, DEFAULT_SMART_NO_IMAGE_ENABLED)
    }

    /**
     * 函数 `setSmartNoImageEnabled`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param enabled 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     */
    fun setSmartNoImageEnabled(enabled: Boolean) {
        preferenceStore.putBoolean(KEY_SMART_NO_IMAGE, enabled)
    }

    /**
     * 函数 `areThirdPartyCookiesEnabled`：封装 `are Third Party Cookies Enabled` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun areThirdPartyCookiesEnabled(): Boolean {
        return preferenceStore.getBoolean(
            KEY_THIRD_PARTY_COOKIES,
            DEFAULT_THIRD_PARTY_COOKIES_ENABLED
        )
    }

    /**
     * 函数 `setThirdPartyCookiesEnabled`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param enabled 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     */
    fun setThirdPartyCookiesEnabled(enabled: Boolean) {
        preferenceStore.putBoolean(KEY_THIRD_PARTY_COOKIES, enabled)
    }

    /**
     * 函数 `isMixedContentBlocked`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isMixedContentBlocked(): Boolean {
        return preferenceStore.getBoolean(KEY_MIXED_CONTENT_BLOCKED, DEFAULT_MIXED_CONTENT_BLOCKED)
    }

    /**
     * 函数 `setMixedContentBlocked`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param blocked 参数类型为 `Boolean`，表示函数执行 `blocked` 相关逻辑时需要读取或处理的输入。
     */
    fun setMixedContentBlocked(blocked: Boolean) {
        preferenceStore.putBoolean(KEY_MIXED_CONTENT_BLOCKED, blocked)
    }

    /**
     * 函数 `defaultVideoSpeed`：封装 `default Video Speed` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun defaultVideoSpeed(): Float {
        return normalizeVideoSpeed(
            preferenceStore.getFloat(KEY_DEFAULT_VIDEO_SPEED, DEFAULT_VIDEO_SPEED)
        )
    }

    /**
     * 函数 `setDefaultVideoSpeed`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param speed 参数类型为 `Float`，表示函数执行 `speed` 相关逻辑时需要读取或处理的输入。
     */
    fun setDefaultVideoSpeed(speed: Float) {
        preferenceStore.putFloat(KEY_DEFAULT_VIDEO_SPEED, normalizeVideoSpeed(speed))
    }

    /**
     * 函数 `textZoomPercent`：封装 `text Zoom Percent` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun textZoomPercent(): Int {
        return normalizeTextZoomPercent(
            preferenceStore.getFloat(
                KEY_TEXT_ZOOM_PERCENT,
                DEFAULT_TEXT_ZOOM_PERCENT.toFloat()
            ).toInt()
        )
    }

    /**
     * 函数 `setTextZoomPercent`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param percent 参数类型为 `Int`，表示函数执行 `percent` 相关逻辑时需要读取或处理的输入。
     */
    fun setTextZoomPercent(percent: Int) {
        preferenceStore.putFloat(
            KEY_TEXT_ZOOM_PERCENT,
            normalizeTextZoomPercent(percent).toFloat()
        )
    }

    /**
     * 函数 `homeUrl`：封装 `home Url` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun homeUrl(): String {
        return normalizeHomeUrl(
            preferenceStore.getString(KEY_HOME_URL, null),
            DEFAULT_HOME_URL
        )
    }

    /**
     * 函数 `homeUrlOr`：封装 `home Url Or` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param defaultValue 参数类型为 `String`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun homeUrlOr(defaultValue: String): String {
        return normalizeHomeUrl(
            preferenceStore.getString(KEY_HOME_URL, null),
            defaultValue
        )
    }

    /**
     * 函数 `hasHomeUrl`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun hasHomeUrl(): Boolean {
        return preferenceStore.contains(KEY_HOME_URL)
    }

    /**
     * 函数 `isValidHomeUrl`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isValidHomeUrl(url: String): Boolean {
        return normalizeHomeUrlOrNull(url) != null
    }

    /**
     * 函数 `setHomeUrl`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     */
    fun setHomeUrl(url: String) {
        preferenceStore.putString(KEY_HOME_URL, normalizeHomeUrl(url, DEFAULT_HOME_URL))
    }

    /**
     * 函数 `searchEngineId`：封装 `search Engine Id` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun searchEngineId(): String {
        return preferenceStore.getString(KEY_SEARCH_ENGINE, DEFAULT_SEARCH_ENGINE_ID)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: DEFAULT_SEARCH_ENGINE_ID
    }

    /**
     * 函数 `setSearchEngineId`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param id 参数类型为 `String`，表示函数执行 `id` 相关逻辑时需要读取或处理的输入。
     */
    fun setSearchEngineId(id: String) {
        val normalizedId = id.trim().takeIf { it.isNotEmpty() } ?: DEFAULT_SEARCH_ENGINE_ID
        preferenceStore.putString(KEY_SEARCH_ENGINE, normalizedId)
    }

    /**
     * 函数 `customShortcuts`：封装 `custom Shortcuts` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun customShortcuts(): List<CustomShortcut> {
        return preferenceStore.getString(KEY_CUSTOM_SHORTCUTS, null)
            ?.lineSequence()
            ?.mapNotNull(::parseCustomShortcut)
            ?.distinct()
            ?.toList()
            ?.takeLast(MAX_CUSTOM_SHORTCUTS)
            ?: emptyList()
    }

    /**
     * 函数 `addCustomShortcut`：封装 `add Custom Shortcut` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param name 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun addCustomShortcut(name: String, url: String): Boolean {
        val shortcut = normalizeCustomShortcut(name, url) ?: return false
        val shortcuts = customShortcuts()
            .filterNot { existing -> existing == shortcut }
            .plus(shortcut)
            .takeLast(MAX_CUSTOM_SHORTCUTS)
        saveCustomShortcuts(shortcuts)
        return true
    }

    /**
     * 函数 `removeCustomShortcut`：封装 `remove Custom Shortcut` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param shortcut 参数类型为 `CustomShortcut`，表示函数执行 `shortcut` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun removeCustomShortcut(shortcut: CustomShortcut): Boolean {
        val normalizedShortcut = normalizeCustomShortcut(shortcut.name, shortcut.url) ?: return false
        val shortcuts = customShortcuts()
        val remainingShortcuts = shortcuts.filterNot { existing -> existing == normalizedShortcut }
        if (remainingShortcuts.size == shortcuts.size) {
            return false
        }
        saveCustomShortcuts(remainingShortcuts)
        return true
    }

    /**
     * 函数 `updateCustomShortcut`：根据最新状态刷新 `update Custom Shortcut` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param shortcut 参数类型为 `CustomShortcut`，表示函数执行 `shortcut` 相关逻辑时需要读取或处理的输入。
     * @param name 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun updateCustomShortcut(shortcut: CustomShortcut, name: String, url: String): Boolean {
        val normalizedShortcut = normalizeCustomShortcut(shortcut.name, shortcut.url) ?: return false
        val updatedShortcut = normalizeCustomShortcut(name, url) ?: return false
        val shortcuts = customShortcuts().toMutableList()
        val index = shortcuts.indexOf(normalizedShortcut)
        if (index < 0) {
            return false
        }

        shortcuts[index] = updatedShortcut
        saveCustomShortcuts(shortcuts.distinct())
        return true
    }

    /**
     * 函数 `isDesktopModeEnabled`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isDesktopModeEnabled(): Boolean {
        return preferenceStore.getBoolean(KEY_DESKTOP_MODE, DEFAULT_DESKTOP_MODE_ENABLED)
    }

    /**
     * 函数 `setDesktopModeEnabled`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param enabled 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     */
    fun setDesktopModeEnabled(enabled: Boolean) {
        preferenceStore.putBoolean(KEY_DESKTOP_MODE, enabled)
    }

    /**
     * 函数 `isPrivateBrowsingEnabled`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isPrivateBrowsingEnabled(): Boolean {
        preferenceStore.remove(KEY_PRIVATE_BROWSING)
        return DEFAULT_PRIVATE_BROWSING_ENABLED
    }

    /**
     * 函数 `setPrivateBrowsingEnabled`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param enabled 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     */
    fun setPrivateBrowsingEnabled(enabled: Boolean) {
        preferenceStore.remove(KEY_PRIVATE_BROWSING)
    }

    /**
     * 函数 `restoreDefaults`：封装 `restore Defaults` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun restoreDefaults(): Boolean {
        return preferenceStore.remove(RESET_KEYS, commit = true)
    }

    /**
     * 函数 `loadHostSet`：启动或加载 `load Host Set` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun loadHostSet(key: String): Set<String> {
        return preferenceStore.getString(key, null)
            ?.lineSequence()
            ?.mapNotNull(SiteHost::normalize)
            ?.toSet()
            ?: emptySet()
    }

    /**
     * 函数 `saveHostSet`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param hosts 参数类型为 `Set<String>`，表示函数执行 `hosts` 相关逻辑时需要读取或处理的输入。
     */
    private fun saveHostSet(key: String, hosts: Set<String>) {
        // 站点集合按行保存，写入前排序，能让存储内容稳定，测试和导出都更容易比较。
        if (hosts.isEmpty()) {
            preferenceStore.remove(key)
        } else {
            preferenceStore.putString(
                key,
                hosts.sorted().joinToString(separator = "\n")
            )
        }
    }

    /**
     * 函数 `sitePermissionAllowedKey`：封装 `site Permission Allowed Key` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param permission 参数类型为 `SitePermission`，表示函数执行 `permission` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun sitePermissionAllowedKey(permission: SitePermission): String {
        return when (permission) {
            SitePermission.CAMERA -> KEY_SITE_PERMISSION_CAMERA_ALLOWED_HOSTS
            SitePermission.MICROPHONE -> KEY_SITE_PERMISSION_MICROPHONE_ALLOWED_HOSTS
            SitePermission.LOCATION -> KEY_SITE_PERMISSION_LOCATION_ALLOWED_HOSTS
        }
    }

    /**
     * 函数 `sitePermissionBlockedKey`：封装 `site Permission Blocked Key` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param permission 参数类型为 `SitePermission`，表示函数执行 `permission` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun sitePermissionBlockedKey(permission: SitePermission): String {
        return when (permission) {
            SitePermission.CAMERA -> KEY_SITE_PERMISSION_CAMERA_BLOCKED_HOSTS
            SitePermission.MICROPHONE -> KEY_SITE_PERMISSION_MICROPHONE_BLOCKED_HOSTS
            SitePermission.LOCATION -> KEY_SITE_PERMISSION_LOCATION_BLOCKED_HOSTS
        }
    }

    /**
     * 函数 `parseUserElementHideRule`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param line 参数类型为 `String`，表示函数执行 `line` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun parseUserElementHideRule(line: String): UserElementHideRule? {
        val separatorIndex = line.indexOf('\t')
        if (separatorIndex <= 0 || separatorIndex >= line.lastIndex) {
            return null
        }
        val host = SiteHost.normalize(line.substring(0, separatorIndex)) ?: return null
        val selector = normalizeUserElementSelector(line.substring(separatorIndex + 1)) ?: return null
        return UserElementHideRule(host = host, selector = selector)
    }

    /**
     * 函数 `saveUserElementHideRules`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param rules 参数类型为 `List<UserElementHideRule>`，表示一组待处理数据，函数会遍历、过滤或转换这些内容。
     */
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

    /**
     * 函数 `normalizeUserElementSelector`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param selector 参数类型为 `String`，表示函数执行 `selector` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `stabilizeUserElementSelector`：封装 `stabilize User Element Selector` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param selector 参数类型为 `String`，表示函数执行 `selector` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `normalizeVideoSpeed`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param speed 参数类型为 `Float`，表示函数执行 `speed` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun normalizeVideoSpeed(speed: Float): Float {
        return if (!speed.isNaN() && !speed.isInfinite() && speed > 0f) {
            speed
        } else {
            DEFAULT_VIDEO_SPEED
        }
    }

    /**
     * 函数 `normalizeTextZoomPercent`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param percent 参数类型为 `Int`，表示函数执行 `percent` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun normalizeTextZoomPercent(percent: Int): Int {
        return percent.takeIf { value -> value in TEXT_ZOOM_OPTIONS }
            ?: DEFAULT_TEXT_ZOOM_PERCENT
    }

    /**
     * 函数 `normalizeHomeUrl`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param value 参数类型为 `String?`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @param defaultValue 参数类型为 `String`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun normalizeHomeUrl(value: String?, defaultValue: String): String {
        return normalizeHomeUrlOrNull(value) ?: defaultValue
    }

    /**
     * 函数 `normalizeHomeUrlOrNull`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param value 参数类型为 `String?`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun normalizeHomeUrlOrNull(value: String?): String? {
        return value
            ?.trim()
            ?.takeIf(::isHttpUrl)
    }

    /**
     * 函数 `parseCustomShortcut`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param line 参数类型为 `String`，表示函数执行 `line` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `saveCustomShortcuts`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param shortcuts 参数类型为 `List<CustomShortcut>`，表示函数执行 `shortcuts` 相关逻辑时需要读取或处理的输入。
     */
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

    /**
     * 函数 `normalizeCustomShortcut`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param name 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `isHttpUrl`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

        private val USER_ELEMENT_POSITIONAL_SEGMENT_REGEX =
            Regex(":nth-of-type\\(\\s*\\d+\\s*\\)", RegexOption.IGNORE_CASE)
        private val USER_ELEMENT_STABLE_TOKEN_REGEX = Regex("[#.][A-Za-z_][A-Za-z0-9_-]*")

        /**
         * 函数 `from`：封装 `from` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param context 参数类型为 `Context`，表示 Android 上下文，用来读取资源、启动系统服务或访问应用环境。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        fun from(context: Context): SettingsManager {
            return SettingsManager(PreferenceStore.from(context))
        }
    }
}
