package com.example.videobrowser.functioncenter

/**
 * 初学者阅读提示：
 * 这个文件属于“功能中心模块”。
 * 文件名 CurrentSiteSettingsPage 可以拆开理解为“Current Site Settings Page”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：构建底部功能面板、设置页面、数据管理页面以及各种用户可点击的工具入口。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import android.widget.LinearLayout
import android.widget.Toast
import com.example.videobrowser.R
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.settings.SitePermission
import com.example.videobrowser.utils.PageUnavailableToast

class CurrentSiteSettingsPage(
    private val host: FunctionCenterPageHost,
    private val settingsManager: SettingsManager,
    private val browserManager: () -> BrowserManager,
    private val currentSiteHost: () -> String?,
    private val isAdBlockEnabled: () -> Boolean,
    private val isSmartNoImageEnabled: () -> Boolean,
    private val isJsInjectionEnabled: () -> Boolean,
    private val isPageCleanupEnabled: () -> Boolean,
    private val isVideoEnhancementEnabled: () -> Boolean,
    private val isPrivateBrowsingEnabled: () -> Boolean,
    private val startElementPicker: () -> Unit,
    private val injectPageFeatures: () -> Unit,
    private val showRootPage: () -> Unit
) {
    private val activity = host.activity
    private val sitePermissionSection = CurrentSitePermissionSection(
        host = host,
        settingsManager = settingsManager,
        currentSiteHost = currentSiteHost,
        onPermissionChanged = { show(replaceCurrent = true) }
    )
    private val siteFeatureSection = CurrentSiteFeatureSection(
        host = host,
        settingsManager = settingsManager,
        browserManager = browserManager,
        currentSiteHost = currentSiteHost,
        isAdBlockEnabled = isAdBlockEnabled,
        isSmartNoImageEnabled = isSmartNoImageEnabled,
        isJsInjectionEnabled = isJsInjectionEnabled,
        isPageCleanupEnabled = isPageCleanupEnabled,
        isVideoEnhancementEnabled = isVideoEnhancementEnabled,
        injectPageFeatures = injectPageFeatures
    )

    /**
     * 函数 `show`：控制 `show` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param replaceCurrent 参数类型为 `Boolean`，表示函数执行 `replaceCurrent` 相关逻辑时需要读取或处理的输入。
     */
    fun show(replaceCurrent: Boolean = false) {
        val siteHost = currentSiteHost()
        host.showBottomSheetPage(
            title = activity.getString(R.string.title_current_site),
            onBack = showRootPage,
            onClose = { host.close() },
            replaceCurrent = replaceCurrent
        ) { content ->
            if (siteHost != null) {
                host.contentFactory.addFunctionMessage(
                    content,
                    activity.getString(R.string.function_center_current_site, siteHost)
                )
            } else {
                host.contentFactory.addEmptyState(
                    content,
                    activity.getString(R.string.function_center_site_action_unavailable)
                )
            }
            addCurrentSiteActionSection(content, siteHost)
        }
    }

    /**
     * 函数 `addCurrentSiteActionSection`：封装 `add Current Site Action Section` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param parent 参数类型为 `LinearLayout`，表示函数执行 `parent` 相关逻辑时需要读取或处理的输入。
     * @param siteHost 参数类型为 `String?`，表示函数执行 `siteHost` 相关逻辑时需要读取或处理的输入。
     */
    private fun addCurrentSiteActionSection(parent: LinearLayout, siteHost: String?) {
        if (isPrivateBrowsingEnabled()) {
            host.contentFactory.addEmptyState(
                parent,
                activity.getString(R.string.function_center_site_action_unavailable)
            )
            return
        }
        val siteSummary = siteHost ?: activity.getString(R.string.function_center_site_action_unavailable)
        val hasSite = siteHost != null
        val isWhitelisted = siteHost?.let(settingsManager::isUserWhitelistedSite) ?: false

        host.contentFactory.addFunctionSection(
            parent,
            activity.getString(R.string.function_center_section_site_actions)
        ) { section ->
            siteFeatureSection.addRows(section, siteHost, hasSite)

            host.contentFactory.addDivider(section)

            sitePermissionSection.addRows(section, siteHost, hasSite)

            host.contentFactory.addDivider(section)

            host.contentFactory.addActionRow(
                parent = section,
                title = activity.getString(R.string.action_add_site_rule),
                summary = siteSummary,
                enabled = hasSite
            ) {
                host.close()
                startElementPicker()
            }
            host.contentFactory.addActionRow(
                parent = section,
                title = activity.getString(
                    if (isWhitelisted) R.string.action_leave_whitelist else R.string.action_join_whitelist
                ),
                summary = siteSummary,
                enabled = hasSite
            ) {
                toggleCurrentSiteWhitelist()
                show(replaceCurrent = true)
            }
            host.contentFactory.addActionRow(
                parent = section,
                title = activity.getString(R.string.action_view_site_config),
                summary = siteSummary,
                enabled = hasSite
            ) {
                showCurrentSiteConfigPage()
            }
        }
    }

    /**
     * 函数 `showCurrentSiteConfigPage`：控制 `show Current Site Config Page` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun showCurrentSiteConfigPage() {
        val siteHost = currentSiteHost()
        host.showBottomSheetPage(
            title = activity.getString(R.string.title_site_config),
            onBack = { show(replaceCurrent = true) },
            onClose = { host.close() }
        ) { content ->
            if (siteHost == null) {
                host.contentFactory.addEmptyState(
                    content,
                    activity.getString(R.string.function_center_site_action_unavailable)
                )
                return@showBottomSheetPage
            }

            host.contentFactory.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_site_actions)
            ) { section ->
                host.contentFactory.addInfoRow(
                    parent = section,
                    title = activity.getString(R.string.function_center_site_host),
                    summary = siteHost
                )
                host.contentFactory.addInfoRow(
                    parent = section,
                    title = activity.getString(R.string.setting_current_site_ad_block),
                    summary = siteFeatureSection.status(
                        globalEnabled = isAdBlockEnabled(),
                        siteDisabled = settingsManager.isAdBlockDisabledForSite(siteHost)
                    )
                )
                host.contentFactory.addInfoRow(
                    parent = section,
                    title = activity.getString(R.string.setting_current_site_smart_no_image),
                    summary = siteFeatureSection.status(
                        globalEnabled = isSmartNoImageEnabled(),
                        siteDisabled = settingsManager.isSmartNoImageDisabledForSite(siteHost)
                    )
                )
                host.contentFactory.addInfoRow(
                    parent = section,
                    title = activity.getString(R.string.setting_current_site_js_injection),
                    summary = siteFeatureSection.status(
                        globalEnabled = isJsInjectionEnabled(),
                        siteDisabled = settingsManager.isJsInjectionDisabledForSite(siteHost)
                    )
                )
                host.contentFactory.addInfoRow(
                    parent = section,
                    title = activity.getString(R.string.setting_page_cleanup),
                    summary = siteFeatureSection.status(
                        globalEnabled = isPageCleanupEnabled(),
                        siteDisabled = settingsManager.isDomAdBlockDisabledForSite(siteHost)
                    )
                )
                host.contentFactory.addInfoRow(
                    parent = section,
                    title = activity.getString(R.string.setting_video_enhancement),
                    summary = siteFeatureSection.status(
                        globalEnabled = isVideoEnhancementEnabled(),
                        siteDisabled = settingsManager.isVideoEnhancementDisabledForSite(siteHost)
                    )
                )
                host.contentFactory.addInfoRow(
                    parent = section,
                    title = activity.getString(R.string.action_join_whitelist),
                    summary = if (settingsManager.isUserWhitelistedSite(siteHost)) {
                        activity.getString(R.string.site_config_whitelisted)
                    } else {
                        activity.getString(R.string.site_config_not_whitelisted)
                    }
                )
                host.contentFactory.addInfoRow(
                    parent = section,
                    title = sitePermissionSection.title(SitePermission.CAMERA),
                    summary = sitePermissionSection.summary(siteHost, SitePermission.CAMERA)
                )
                host.contentFactory.addInfoRow(
                    parent = section,
                    title = sitePermissionSection.title(SitePermission.MICROPHONE),
                    summary = sitePermissionSection.summary(siteHost, SitePermission.MICROPHONE)
                )
                host.contentFactory.addInfoRow(
                    parent = section,
                    title = sitePermissionSection.title(SitePermission.LOCATION),
                    summary = sitePermissionSection.summary(siteHost, SitePermission.LOCATION)
                )
                host.contentFactory.addInfoRow(
                    parent = section,
                    title = activity.getString(R.string.action_add_site_rule),
                    summary = activity.getString(
                        R.string.site_config_rule_count,
                        settingsManager.userElementHideSelectorsForSite(siteHost).size
                    )
                )
            }
        }
    }

    /**
     * 函数 `toggleCurrentSiteWhitelist`：封装 `toggle Current Site Whitelist` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun toggleCurrentSiteWhitelist() {
        val hostName = currentSiteHost() ?: run {
            PageUnavailableToast.showNoPageUrl(activity)
            return
        }
        val shouldWhitelist = !settingsManager.isUserWhitelistedSite(hostName)
        settingsManager.setUserWhitelistedSite(hostName, shouldWhitelist)
        Toast.makeText(
            activity,
            if (shouldWhitelist) {
                activity.getString(R.string.toast_user_whitelist_added, hostName)
            } else {
                activity.getString(R.string.toast_user_whitelist_removed, hostName)
            },
            Toast.LENGTH_SHORT
        ).show()
        browserManager().reload()
    }
}
