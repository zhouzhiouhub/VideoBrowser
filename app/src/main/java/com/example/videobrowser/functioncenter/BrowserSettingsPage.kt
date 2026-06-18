package com.example.videobrowser.functioncenter

/**
 * 初学者阅读提示：
 * 这个文件属于“功能中心模块”。
 * 文件名 BrowserSettingsPage 可以拆开理解为“Browser Settings Page”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：构建底部功能面板、设置页面、数据管理页面以及各种用户可点击的工具入口。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import android.widget.LinearLayout
import com.example.videobrowser.R
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.settings.SettingsManager

class BrowserSettingsPage(
    private val host: FunctionCenterPageHost,
    private val settingsManager: SettingsManager,
    private val browserManager: () -> BrowserManager,
    private val isPrivateBrowsingEnabled: () -> Boolean,
    private val isAdBlockEnabled: () -> Boolean,
    private val isSmartNoImageEnabled: () -> Boolean,
    private val isJsInjectionEnabled: () -> Boolean,
    private val isPageCleanupEnabled: () -> Boolean,
    private val isVideoEnhancementEnabled: () -> Boolean,
    private val setPrivateBrowsingEnabled: (Boolean) -> Unit,
    private val injectPageFeatures: () -> Unit,
    private val showBookmarks: () -> Unit,
    private val showBookmarkManager: () -> Unit,
    private val showHistory: () -> Unit,
    private val showHistoryManager: () -> Unit,
    private val showDownloadManager: () -> Unit,
    private val showFileOperationsPage: () -> Unit,
    private val showAdBlockLog: () -> Unit,
    private val showUserWhitelistManager: () -> Unit,
    private val showUserManualRulesManager: () -> Unit,
    private val showSitePermissionsManager: () -> Unit,
    private val showRuleSubscriptionsManager: () -> Unit,
    private val showCookieManager: () -> Unit,
    private val showCacheManager: () -> Unit,
    private val showSiteDataManager: () -> Unit,
    private val showRestoreDefaultSettingsPage: () -> Unit,
    private val currentSearchProviderName: () -> String,
    private val selectSearchProvider: (String) -> Boolean,
    private val showRootPage: () -> Unit
) {
    private val activity = host.activity
    private val dialogController = BrowserSettingsDialogController(
        activity = activity,
        settingsManager = settingsManager,
        browserManager = browserManager,
        selectSearchProvider = selectSearchProvider,
        onSettingsChanged = { show() }
    )
    private val dataManagementSection = BrowserSettingsDataManagementSection(
        host = host,
        isPrivateBrowsingEnabled = isPrivateBrowsingEnabled,
        showBookmarkManager = showBookmarkManager,
        showHistoryManager = showHistoryManager,
        showDownloadManager = showDownloadManager,
        showAdBlockLog = showAdBlockLog,
        showUserWhitelistManager = showUserWhitelistManager,
        showUserManualRulesManager = showUserManualRulesManager,
        showSitePermissionsManager = showSitePermissionsManager,
        showRuleSubscriptionsManager = showRuleSubscriptionsManager,
        showCookieManager = showCookieManager,
        showCacheManager = showCacheManager,
        showSiteDataManager = showSiteDataManager,
        showRestoreDefaultSettingsPage = showRestoreDefaultSettingsPage
    )

    /**
     * 函数 `show`：控制 `show` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun show() {
        host.showPage(
            title = activity.getString(R.string.title_browser_settings),
            onBack = showRootPage
        ) { content ->
            addBrowserBasicsSection(content)
            addGlobalEnhancementSection(content)
            addToolboxSection(content)
        }
    }

    /**
     * 函数 `addExpandedBrowserSettings`：封装 `add Expanded Browser Settings` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param parent 参数类型为 `LinearLayout`，表示函数执行 `parent` 相关逻辑时需要读取或处理的输入。
     */
    fun addExpandedBrowserSettings(parent: LinearLayout) {
        addGlobalEnhancementSection(parent)
    }

    /**
     * 函数 `addExpandedDataManagement`：封装 `add Expanded Data Management` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param parent 参数类型为 `LinearLayout`，表示函数执行 `parent` 相关逻辑时需要读取或处理的输入。
     */
    fun addExpandedDataManagement(parent: LinearLayout) {
        host.addFunctionSection(
            parent,
            activity.getString(R.string.function_center_section_data)
        ) { section ->
            dataManagementSection.addActions(section, includeSavedPages = true)
        }
    }

    /**
     * 函数 `addProfileDataManagement`：封装 `add Profile Data Management` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param parent 参数类型为 `LinearLayout`，表示函数执行 `parent` 相关逻辑时需要读取或处理的输入。
     */
    fun addProfileDataManagement(parent: LinearLayout) {
        host.addFunctionSection(
            parent,
            activity.getString(R.string.function_center_section_data)
        ) { section ->
            dataManagementSection.addProfileActions(section)
        }
    }

    /**
     * 函数 `addToolboxSection`：封装 `add Toolbox Section` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param parent 参数类型为 `LinearLayout`，表示函数执行 `parent` 相关逻辑时需要读取或处理的输入。
     */
    private fun addToolboxSection(parent: LinearLayout) {
        host.addFunctionSection(
            parent,
            activity.getString(R.string.function_center_section_toolbox)
        ) { section ->
            host.addActionRow(
                parent = section,
                title = activity.getString(R.string.action_show_bookmarks),
                summary = activity.getString(R.string.action_show_bookmarks_summary)
            ) {
                showBookmarks()
            }
            host.addActionRow(
                parent = section,
                title = activity.getString(R.string.action_show_history),
                summary = activity.getString(R.string.action_show_history_summary)
            ) {
                showHistory()
            }
            host.addActionRow(
                parent = section,
                title = activity.getString(R.string.action_file_operations),
                summary = activity.getString(R.string.action_file_operations_summary)
            ) {
                showFileOperationsPage()
            }
            if (isPrivateBrowsingEnabled()) {
                return@addFunctionSection
            }
            addDataManagementRows(section)
        }
    }

    /**
     * 函数 `addDataManagementRows`：封装 `add Data Management Rows` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param section 参数类型为 `LinearLayout`，表示函数执行 `section` 相关逻辑时需要读取或处理的输入。
     */
    private fun addDataManagementRows(section: LinearLayout) {
        dataManagementSection.addActions(section, includeSavedPages = false)
    }

    /**
     * 函数 `addBrowserBasicsSection`：封装 `add Browser Basics Section` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param parent 参数类型为 `LinearLayout`，表示函数执行 `parent` 相关逻辑时需要读取或处理的输入。
     */
    private fun addBrowserBasicsSection(parent: LinearLayout) {
        host.addFunctionSection(
            parent,
            activity.getString(R.string.function_center_section_browser_basics)
        ) { section ->
            host.addActionRow(
                parent = section,
                title = activity.getString(R.string.setting_home_page),
                summary = settingsManager.homeUrl()
            ) {
                dialogController.showHomeUrlDialog()
            }
            host.addActionRow(
                parent = section,
                title = activity.getString(R.string.setting_search_engine),
                summary = currentSearchProviderName()
            ) {
                dialogController.showSearchEngineDialog()
            }
        }
    }

    /**
     * 函数 `addGlobalEnhancementSection`：封装 `add Global Enhancement Section` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param parent 参数类型为 `LinearLayout`，表示函数执行 `parent` 相关逻辑时需要读取或处理的输入。
     */
    private fun addGlobalEnhancementSection(parent: LinearLayout) {
        host.addFunctionSection(
            parent,
            activity.getString(R.string.function_center_section_settings)
        ) { section ->
            host.addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_private_browsing),
                summary = activity.getString(R.string.setting_private_browsing_summary),
                checked = isPrivateBrowsingEnabled()
            ) { enabled ->
                setPrivateBrowsingEnabled(enabled)
            }

            if (isPrivateBrowsingEnabled()) {
                return@addFunctionSection
            }

            host.addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_ad_block),
                summary = activity.getString(R.string.setting_ad_block_summary),
                checked = isAdBlockEnabled()
            ) { enabled ->
                settingsManager.setAdBlockEnabled(enabled)
                browserManager().reload()
            }

            host.addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_smart_no_image),
                summary = activity.getString(R.string.setting_smart_no_image_summary),
                checked = isSmartNoImageEnabled()
            ) { enabled ->
                settingsManager.setSmartNoImageEnabled(enabled)
                browserManager().reload()
            }

            host.addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_third_party_cookies),
                summary = activity.getString(R.string.setting_third_party_cookies_summary),
                checked = settingsManager.areThirdPartyCookiesEnabled()
            ) { enabled ->
                settingsManager.setThirdPartyCookiesEnabled(enabled)
                browserManager().setThirdPartyCookiesEnabled(enabled)
                browserManager().reload()
            }

            host.addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_mixed_content_blocking),
                summary = activity.getString(R.string.setting_mixed_content_blocking_summary),
                checked = settingsManager.isMixedContentBlocked()
            ) { blocked ->
                settingsManager.setMixedContentBlocked(blocked)
                browserManager().setMixedContentBlocked(blocked)
                browserManager().reload()
            }

            host.addActionRow(
                parent = section,
                title = activity.getString(R.string.setting_text_zoom),
                summary = activity.getString(
                    R.string.setting_text_zoom_summary,
                    settingsManager.textZoomPercent()
                )
            ) {
                dialogController.showTextZoomDialog()
            }

            host.addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_js_injection),
                summary = activity.getString(R.string.setting_js_injection_summary),
                checked = isJsInjectionEnabled()
            ) { enabled ->
                settingsManager.setJsInjectionEnabled(enabled)
                browserManager().reload()
            }

            host.addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_page_cleanup),
                summary = activity.getString(R.string.setting_page_cleanup_summary),
                checked = isPageCleanupEnabled()
            ) { enabled ->
                settingsManager.setDomAdBlockEnabled(enabled)
                injectPageFeatures()
            }

            host.addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_video_enhancement),
                summary = activity.getString(R.string.setting_video_enhancement_summary),
                checked = isVideoEnhancementEnabled()
            ) { enabled ->
                settingsManager.setVideoEnhancementEnabled(enabled)
                injectPageFeatures()
            }

            host.addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_always_start_videos_from_beginning),
                summary = activity.getString(
                    R.string.setting_always_start_videos_from_beginning_summary
                ),
                checked = settingsManager.alwaysStartVideosFromBeginning()
            ) { enabled ->
                settingsManager.setAlwaysStartVideosFromBeginning(enabled)
            }
        }
    }

}
