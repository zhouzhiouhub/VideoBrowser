package com.example.videobrowser.settings

/**
 * 初学者阅读提示：
 * 这个文件属于“设置模块”。
 * 文件名 SettingsManager 可以拆开理解为“Settings Manager”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：封装浏览器设置、站点级开关、权限记录和恢复默认设置逻辑。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import android.content.Context
import com.example.videobrowser.storage.PreferenceStore

/**
 * 应用设置读写入口。
 *
 * 这个类把 PreferenceStore 里的字符串/布尔值包装成有业务含义的函数：
 * 全局开关、站点级开关、站点权限、自定义快捷入口、主页、搜索引擎等都从这里读写。
 */
class SettingsManager(
    private val preferenceStore: PreferenceStore
) {
    private val hostSets = SettingsHostSetStore(preferenceStore)
    private val globalPreferences = SettingsGlobalPreferenceStore(preferenceStore)
    private val siteFeatures = SettingsSiteFeatureFacade(
        SiteFeatureHostSettings(preferenceStore, hostSets)
    )
    private val sitePermissions = PersistentSitePermissionStore(preferenceStore, hostSets)
    private val userElementHideRuleStore = UserElementHideRuleStore(preferenceStore)
    private val customShortcutStore = CustomShortcutStore(preferenceStore)
    private val customSearchEngineStore = CustomSearchEngineStore(preferenceStore)
    private val removedSearchProviderStore = RemovedSearchProviderStore(preferenceStore)
    private val preferenceResetter = SettingsPreferenceResetter(preferenceStore)

    /**
     * 函数 `isAdBlockEnabled`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isAdBlockEnabled(): Boolean {
        return globalPreferences.isAdBlockEnabled()
    }

    /**
     * 函数 `setAdBlockEnabled`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param enabled 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     */
    fun setAdBlockEnabled(enabled: Boolean) {
        globalPreferences.setAdBlockEnabled(enabled)
    }

    /**
     * 函数 `isAdBlockDisabledForSite`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isAdBlockDisabledForSite(host: String?): Boolean {
        return siteFeatures.isAdBlockDisabledForSite(host)
    }

    /**
     * P11-01 只保存站点级请求拦截放行列表，不引入订阅、规则文件或数据库。
     */
    fun setAdBlockDisabledForSite(host: String?, disabled: Boolean): Boolean {
        return siteFeatures.setAdBlockDisabledForSite(host, disabled)
    }

    /**
     * 函数 `adBlockDisabledSiteHosts`：封装 `ad Block Disabled Site Hosts` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun adBlockDisabledSiteHosts(): Set<String> {
        return siteFeatures.adBlockDisabledSiteHosts()
    }

    /**
     * 函数 `isJsInjectionDisabledForSite`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isJsInjectionDisabledForSite(host: String?): Boolean {
        return siteFeatures.isJsInjectionDisabledForSite(host)
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
        return siteFeatures.setJsInjectionDisabledForSite(host, disabled)
    }

    /**
     * 函数 `jsInjectionDisabledSiteHosts`：封装 `js Injection Disabled Site Hosts` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun jsInjectionDisabledSiteHosts(): Set<String> {
        return siteFeatures.jsInjectionDisabledSiteHosts()
    }

    /**
     * 函数 `isDomAdBlockDisabledForSite`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isDomAdBlockDisabledForSite(host: String?): Boolean {
        return siteFeatures.isDomAdBlockDisabledForSite(host)
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
        return siteFeatures.setDomAdBlockDisabledForSite(host, disabled)
    }

    /**
     * 函数 `domAdBlockDisabledSiteHosts`：封装 `dom Ad Block Disabled Site Hosts` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun domAdBlockDisabledSiteHosts(): Set<String> {
        return siteFeatures.domAdBlockDisabledSiteHosts()
    }

    /**
     * 函数 `isVideoEnhancementDisabledForSite`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isVideoEnhancementDisabledForSite(host: String?): Boolean {
        return siteFeatures.isVideoEnhancementDisabledForSite(host)
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
        return siteFeatures.setVideoEnhancementDisabledForSite(host, disabled)
    }

    /**
     * 函数 `videoEnhancementDisabledSiteHosts`：封装 `video Enhancement Disabled Site Hosts` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun videoEnhancementDisabledSiteHosts(): Set<String> {
        return siteFeatures.videoEnhancementDisabledSiteHosts()
    }

    /**
     * 函数 `isSmartNoImageDisabledForSite`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isSmartNoImageDisabledForSite(host: String?): Boolean {
        return siteFeatures.isSmartNoImageDisabledForSite(host)
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
        return siteFeatures.setSmartNoImageDisabledForSite(host, disabled)
    }

    /**
     * 函数 `smartNoImageDisabledSiteHosts`：封装 `smart No Image Disabled Site Hosts` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun smartNoImageDisabledSiteHosts(): Set<String> {
        return siteFeatures.smartNoImageDisabledSiteHosts()
    }

    /**
     * 函数 `isUserWhitelistedSite`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isUserWhitelistedSite(host: String?): Boolean {
        return siteFeatures.isUserWhitelistedSite(host)
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
        return siteFeatures.setUserWhitelistedSite(host, whitelisted)
    }

    /**
     * 函数 `userWhitelistedSiteHosts`：封装 `user Whitelisted Site Hosts` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun userWhitelistedSiteHosts(): Set<String> {
        return siteFeatures.userWhitelistedSiteHosts()
    }

    /**
     * 函数 `clearUserWhitelistedSites`：封装 `clear User Whitelisted Sites` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun clearUserWhitelistedSites() {
        siteFeatures.clearUserWhitelistedSites()
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
        return sitePermissions.decision(host, permission)
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
        return sitePermissions.setDecision(host, permission, decision)
    }

    /**
     * 函数 `allowedSitePermissionHosts`：封装 `allowed Site Permission Hosts` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param permission 参数类型为 `SitePermission`，表示函数执行 `permission` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun allowedSitePermissionHosts(permission: SitePermission): Set<String> {
        return sitePermissions.allowedHosts(permission)
    }

    /**
     * 函数 `blockedSitePermissionHosts`：封装 `blocked Site Permission Hosts` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param permission 参数类型为 `SitePermission`，表示函数执行 `permission` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun blockedSitePermissionHosts(permission: SitePermission): Set<String> {
        return sitePermissions.blockedHosts(permission)
    }

    /**
     * 函数 `sitePermissionRecords`：封装 `site Permission Records` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun sitePermissionRecords(): List<SitePermissionRecord> {
        return sitePermissions.records()
    }

    /**
     * 函数 `clearSitePermissionDecisions`：封装 `clear Site Permission Decisions` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun clearSitePermissionDecisions() {
        sitePermissions.clear()
    }

    /**
     * 函数 `userElementHideSelectorsForSite`：封装 `user Element Hide Selectors For Site` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun userElementHideSelectorsForSite(host: String?): List<String> {
        return userElementHideRuleStore.selectorsForSite(host)
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
        return userElementHideRuleStore.hasSelectorForSite(host, selector)
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
        return userElementHideRuleStore.addSelectorForSite(host, selector)
    }

    /**
     * 函数 `userElementHideRules`：封装 `user Element Hide Rules` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun userElementHideRules(): List<UserElementHideRule> {
        return userElementHideRuleStore.load()
    }

    /**
     * 函数 `removeUserElementHideRule`：封装 `remove User Element Hide Rule` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param rule 参数类型为 `UserElementHideRule`，表示函数执行 `rule` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun removeUserElementHideRule(rule: UserElementHideRule): Boolean {
        return userElementHideRuleStore.remove(rule)
    }

    /**
     * 函数 `clearUserElementHideRules`：封装 `clear User Element Hide Rules` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun clearUserElementHideRules() {
        userElementHideRuleStore.clear()
    }

    /**
     * 函数 `isJsInjectionEnabled`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isJsInjectionEnabled(): Boolean {
        return globalPreferences.isJsInjectionEnabled()
    }

    /**
     * 函数 `setJsInjectionEnabled`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param enabled 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     */
    fun setJsInjectionEnabled(enabled: Boolean) {
        globalPreferences.setJsInjectionEnabled(enabled)
    }

    /**
     * 函数 `isDomAdBlockEnabled`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isDomAdBlockEnabled(): Boolean {
        return globalPreferences.isDomAdBlockEnabled()
    }

    /**
     * 函数 `setDomAdBlockEnabled`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param enabled 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     */
    fun setDomAdBlockEnabled(enabled: Boolean) {
        globalPreferences.setDomAdBlockEnabled(enabled)
    }

    /**
     * 函数 `isVideoEnhancementEnabled`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isVideoEnhancementEnabled(): Boolean {
        return globalPreferences.isVideoEnhancementEnabled()
    }

    /**
     * 函数 `setVideoEnhancementEnabled`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param enabled 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     */
    fun setVideoEnhancementEnabled(enabled: Boolean) {
        globalPreferences.setVideoEnhancementEnabled(enabled)
    }

    /**
     * 函数 `alwaysStartVideosFromBeginning`：封装 `always Start Videos From Beginning` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun alwaysStartVideosFromBeginning(): Boolean {
        return globalPreferences.alwaysStartVideosFromBeginning()
    }

    /**
     * 函数 `setAlwaysStartVideosFromBeginning`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param enabled 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     */
    fun setAlwaysStartVideosFromBeginning(enabled: Boolean) {
        globalPreferences.setAlwaysStartVideosFromBeginning(enabled)
    }

    /**
     * 函数 `isSmartNoImageEnabled`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isSmartNoImageEnabled(): Boolean {
        return globalPreferences.isSmartNoImageEnabled()
    }

    /**
     * 函数 `setSmartNoImageEnabled`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param enabled 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     */
    fun setSmartNoImageEnabled(enabled: Boolean) {
        globalPreferences.setSmartNoImageEnabled(enabled)
    }

    /**
     * 函数 `areThirdPartyCookiesEnabled`：封装 `are Third Party Cookies Enabled` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun areThirdPartyCookiesEnabled(): Boolean {
        return globalPreferences.areThirdPartyCookiesEnabled()
    }

    /**
     * 函数 `setThirdPartyCookiesEnabled`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param enabled 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     */
    fun setThirdPartyCookiesEnabled(enabled: Boolean) {
        globalPreferences.setThirdPartyCookiesEnabled(enabled)
    }

    /**
     * 函数 `isMixedContentBlocked`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isMixedContentBlocked(): Boolean {
        return globalPreferences.isMixedContentBlocked()
    }

    /**
     * 函数 `setMixedContentBlocked`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param blocked 参数类型为 `Boolean`，表示函数执行 `blocked` 相关逻辑时需要读取或处理的输入。
     */
    fun setMixedContentBlocked(blocked: Boolean) {
        globalPreferences.setMixedContentBlocked(blocked)
    }

    /**
     * 函数 `defaultVideoSpeed`：封装 `default Video Speed` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun defaultVideoSpeed(): Float {
        return globalPreferences.defaultVideoSpeed()
    }

    /**
     * 函数 `setDefaultVideoSpeed`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param speed 参数类型为 `Float`，表示函数执行 `speed` 相关逻辑时需要读取或处理的输入。
     */
    fun setDefaultVideoSpeed(speed: Float) {
        globalPreferences.setDefaultVideoSpeed(speed)
    }

    /**
     * 函数 `textZoomPercent`：封装 `text Zoom Percent` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun textZoomPercent(): Int {
        return globalPreferences.textZoomPercent()
    }

    /**
     * 函数 `setTextZoomPercent`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param percent 参数类型为 `Int`，表示函数执行 `percent` 相关逻辑时需要读取或处理的输入。
     */
    fun setTextZoomPercent(percent: Int) {
        globalPreferences.setTextZoomPercent(percent)
    }

    /**
     * 函数 `homeUrl`：封装 `home Url` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun homeUrl(): String {
        return globalPreferences.homeUrl()
    }

    /**
     * 函数 `homeUrlOr`：封装 `home Url Or` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param defaultValue 参数类型为 `String`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun homeUrlOr(defaultValue: String): String {
        return globalPreferences.homeUrlOr(defaultValue)
    }

    /**
     * 函数 `hasHomeUrl`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun hasHomeUrl(): Boolean {
        return globalPreferences.hasHomeUrl()
    }

    /**
     * 函数 `isValidHomeUrl`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isValidHomeUrl(url: String): Boolean {
        return globalPreferences.isValidHomeUrl(url)
    }

    /**
     * 函数 `setHomeUrl`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     */
    fun setHomeUrl(url: String) {
        globalPreferences.setHomeUrl(url)
    }

    /**
     * 函数 `searchEngineId`：封装 `search Engine Id` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun searchEngineId(): String {
        return globalPreferences.searchEngineId()
    }

    /**
     * 函数 `setSearchEngineId`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param id 参数类型为 `String`，表示函数执行 `id` 相关逻辑时需要读取或处理的输入。
     */
    fun setSearchEngineId(id: String) {
        globalPreferences.setSearchEngineId(id)
    }

    /**
     * 函数 `customShortcuts`：封装 `custom Shortcuts` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun customShortcuts(): List<CustomShortcut> {
        return customShortcutStore.load()
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
        return customShortcutStore.add(name, url)
    }

    /**
     * 函数 `removeCustomShortcut`：封装 `remove Custom Shortcut` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param shortcut 参数类型为 `CustomShortcut`，表示函数执行 `shortcut` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun removeCustomShortcut(shortcut: CustomShortcut): Boolean {
        return customShortcutStore.remove(shortcut)
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
        return customShortcutStore.update(shortcut, name, url)
    }

    /**
     * 函数 `customSearchEngines`：封装 `custom Search Engines` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun customSearchEngines(): List<CustomSearchEngine> {
        return customSearchEngineStore.load()
    }

    fun removedSearchProviderIds(): Set<String> {
        return removedSearchProviderStore.load()
    }

    fun removeBuiltInSearchProvider(id: String): Boolean {
        return removedSearchProviderStore.add(id)
    }

    /**
     * 函数 `addCustomSearchEngine`：封装 `add Custom Search Engine` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param name 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param searchUrlPrefix 参数类型为 `String`，表示搜索 URL 前缀，地址栏会在后面追加编码后的关键词。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun addCustomSearchEngine(name: String, searchUrlPrefix: String): Boolean {
        return customSearchEngineStore.add(name, searchUrlPrefix)
    }

    fun addCustomSearchEngine(
        name: String,
        displayUrl: String,
        searchTemplate: String,
        queryParam: String,
        domains: List<String>,
        hideCss: List<String>,
        hidePageSearchBox: Boolean
    ): Boolean {
        return customSearchEngineStore.add(
            name = name,
            displayUrl = displayUrl,
            searchTemplate = searchTemplate,
            queryParam = queryParam,
            domains = domains,
            hideCss = hideCss,
            hidePageSearchBox = hidePageSearchBox
        )
    }

    /**
     * 函数 `removeCustomSearchEngine`：封装 `remove Custom Search Engine` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param engine 参数类型为 `CustomSearchEngine`，表示函数执行 `engine` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun removeCustomSearchEngine(engine: CustomSearchEngine): Boolean {
        return customSearchEngineStore.remove(engine)
    }

    /**
     * 函数 `updateCustomSearchEngine`：根据最新状态刷新 `update Custom Search Engine` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param engine 参数类型为 `CustomSearchEngine`，表示函数执行 `engine` 相关逻辑时需要读取或处理的输入。
     * @param name 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param searchUrlPrefix 参数类型为 `String`，表示搜索 URL 前缀，地址栏会在后面追加编码后的关键词。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun updateCustomSearchEngine(
        engine: CustomSearchEngine,
        name: String,
        searchUrlPrefix: String
    ): Boolean {
        return customSearchEngineStore.update(engine, name, searchUrlPrefix)
    }

    fun updateCustomSearchEngine(
        engine: CustomSearchEngine,
        name: String,
        displayUrl: String,
        searchTemplate: String,
        queryParam: String,
        domains: List<String>,
        hideCss: List<String>,
        hidePageSearchBox: Boolean
    ): Boolean {
        return customSearchEngineStore.update(
            engine = engine,
            name = name,
            displayUrl = displayUrl,
            searchTemplate = searchTemplate,
            queryParam = queryParam,
            domains = domains,
            hideCss = hideCss,
            hidePageSearchBox = hidePageSearchBox
        )
    }

    /**
     * 函数 `isDesktopModeEnabled`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isDesktopModeEnabled(): Boolean {
        return globalPreferences.isDesktopModeEnabled()
    }

    /**
     * 函数 `setDesktopModeEnabled`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param enabled 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     */
    fun setDesktopModeEnabled(enabled: Boolean) {
        globalPreferences.setDesktopModeEnabled(enabled)
    }

    /**
     * 函数 `isPrivateBrowsingEnabled`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isPrivateBrowsingEnabled(): Boolean {
        return globalPreferences.isPrivateBrowsingEnabled()
    }

    /**
     * 函数 `setPrivateBrowsingEnabled`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param enabled 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     */
    fun setPrivateBrowsingEnabled(enabled: Boolean) {
        globalPreferences.setPrivateBrowsingEnabled(enabled)
    }

    /**
     * 函数 `restoreDefaults`：封装 `restore Defaults` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun restoreDefaults(): Boolean {
        return preferenceResetter.restoreDefaults()
    }

    companion object {
        const val DEFAULT_SEARCH_ENGINE_ID = "baidu"
        const val DEFAULT_HOME_URL = "https://m.baidu.com/"
        const val DEFAULT_VIDEO_SPEED = 1f
        const val DEFAULT_TEXT_ZOOM_PERCENT = 100
        val TEXT_ZOOM_OPTIONS = listOf(75, 100, 125, 150, 200)

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
