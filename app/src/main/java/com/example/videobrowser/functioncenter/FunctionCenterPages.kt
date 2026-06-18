package com.example.videobrowser.functioncenter

/**
 * 初学者阅读提示：
 * 这个文件属于“功能中心模块”。
 * 文件名 FunctionCenterPages 可以拆开理解为“Function Center Pages”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：构建底部功能面板、设置页面、数据管理页面以及各种用户可点击的工具入口。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.adblock.AdBlockLogger
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.browser.BrowserTab
import com.example.videobrowser.download.DownloadRecord
import com.example.videobrowser.download.DownloadRecordRepository
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.storage.SavedPageRepository
import com.example.videobrowser.storage.SavedPageRepository.SavedPageCollection
import com.example.videobrowser.utils.UrlUtils
import com.example.videobrowser.video.PlaybackHistoryRepository
import com.example.videobrowser.video.PlaybackProgress
import java.io.File

/**
 * 功能中心页面编排器。
 *
 * FunctionCenterController 只负责显示页面；各个页面类只负责单页内容。
 * FunctionCenterPages 把 MainActivity 传进来的动作和这些页面连接起来，是功能中心的“路由表”。
 */
class FunctionCenterPages(
    activity: AppCompatActivity,
    functionCenter: FunctionCenterController,
    private val settingsManager: SettingsManager,
    private val browserManager: () -> BrowserManager,
    private val browserManagers: () -> List<BrowserManager>,
    private val savedPageRepository: SavedPageRepository,
    private val downloadRecordRepository: DownloadRecordRepository,
    private val playbackHistoryRepository: PlaybackHistoryRepository,
    adBlockLogger: AdBlockLogger,
    filesDir: File,
    private val currentSiteHost: () -> String?,
    private val currentActionableUrl: () -> String?,
    private val isDesktopModeEnabled: () -> Boolean,
    private val isPrivateBrowsingEnabled: () -> Boolean,
    private val isAdBlockEnabled: () -> Boolean,
    private val isSmartNoImageEnabled: () -> Boolean,
    private val isJsInjectionEnabled: () -> Boolean,
    private val isPageCleanupEnabled: () -> Boolean,
    private val isVideoEnhancementEnabled: () -> Boolean,
    private val currentTabs: () -> List<BrowserTab>,
    private val activeTabId: () -> Long,
    private val openNewTab: () -> Unit,
    private val openHomePage: () -> Unit,
    private val canReopenClosedTab: () -> Boolean,
    private val reopenClosedTab: () -> Unit,
    private val switchTab: (Long) -> Unit,
    private val closeTab: (Long) -> Unit,
    private val closeOtherTabs: (Long) -> Unit,
    private val closeAllTabs: () -> Unit,
    private val duplicateTab: (Long) -> Unit,
    private val toggleCurrentBookmark: () -> Unit,
    private val setCurrentPageAsHomePage: () -> Unit,
    private val copyCurrentUrl: () -> Unit,
    private val shareCurrentUrl: () -> Unit,
    private val saveCurrentPageArchive: () -> Unit,
    private val printCurrentPage: () -> Unit,
    private val findInPage: () -> Unit,
    private val openCurrentUrlInNativePlayer: () -> Unit,
    private val openPlaybackHistoryItem: (PlaybackProgress) -> Unit,
    private val downloadCurrentUrl: () -> Unit,
    private val retryDownload: (DownloadRecord) -> Unit,
    private val exportBookmarks: () -> Unit,
    private val importBookmarks: () -> Unit,
    private val currentSearchProviderName: () -> String,
    private val selectSearchProvider: (String) -> Boolean,
    setPrivateBrowsingEnabled: (Boolean) -> Unit,
    restoreDefaultSettings: () -> Unit,
    private val showFileOperationsPage: () -> Unit,
    private val startElementPicker: () -> Unit,
    private val applyDesktopMode: (Boolean) -> Unit,
    private val injectPageFeatures: () -> Unit,
    private val openUrlInNewTab: (String) -> Unit,
    private val loadUrl: (String) -> Unit,
    private val recreateActivity: () -> Unit
) {
    private val host = FunctionCenterPageHost(activity, functionCenter)
    private val currentSiteSettingsPage = CurrentSiteSettingsPage(
        host = host,
        settingsManager = settingsManager,
        browserManager = browserManager,
        currentSiteHost = currentSiteHost,
        isAdBlockEnabled = isAdBlockEnabled,
        isSmartNoImageEnabled = isSmartNoImageEnabled,
        isJsInjectionEnabled = isJsInjectionEnabled,
        isPageCleanupEnabled = isPageCleanupEnabled,
        isVideoEnhancementEnabled = isVideoEnhancementEnabled,
        isPrivateBrowsingEnabled = isPrivateBrowsingEnabled,
        startElementPicker = startElementPicker,
        injectPageFeatures = injectPageFeatures,
        showRootPage = ::showRootPage
    )
    private val savedPagesPage = SavedPagesPage(
        host = host,
        savedPageRepository = savedPageRepository,
        openUrlInNewTab = openUrlInNewTab,
        loadUrl = loadUrl,
        showRootPage = ::showRootPage
    )
    private val downloadsPage = DownloadsPage(
        host = host,
        downloadRecordRepository = downloadRecordRepository,
        retryDownload = retryDownload,
        showRootPage = ::showRootPage
    )
    private val browserTabsPage = BrowserTabsPage(
        host = host,
        currentTabs = currentTabs,
        activeTabId = activeTabId,
        openNewTab = openNewTab,
        canReopenClosedTab = canReopenClosedTab,
        reopenClosedTab = reopenClosedTab,
        switchTab = switchTab,
        closeTab = closeTab,
        closeOtherTabs = closeOtherTabs,
        closeAllTabs = closeAllTabs,
        duplicateTab = duplicateTab,
        showRootPage = ::showRootPage
    )
    private val playbackHistoryPage = PlaybackHistoryPage(
        host = host,
        playbackHistoryRepository = playbackHistoryRepository,
        openPlaybackHistoryItem = openPlaybackHistoryItem,
        showRootPage = ::showRootPage
    )
    private val adBlockLogPage = AdBlockLogPage(
        host = host,
        settingsManager = settingsManager,
        browserManager = browserManager,
        adBlockLogger = adBlockLogger,
        showRootPage = ::showRootPage
    )
    private val userWhitelistPage = UserWhitelistPage(
        host = host,
        settingsManager = settingsManager,
        browserManager = browserManager,
        currentSiteHost = currentSiteHost,
        showRootPage = ::showRootPage
    )
    private val userManualRulesPage = UserManualRulesPage(
        host = host,
        settingsManager = settingsManager,
        browserManager = browserManager,
        showRootPage = ::showRootPage
    )
    private val sitePermissionsPage = SitePermissionsPage(
        host = host,
        settingsManager = settingsManager,
        showRootPage = ::showRootPage
    )
    private val ruleSubscriptionPage = RuleSubscriptionPage(
        host = host,
        filesDir = filesDir,
        onRulesChanged = recreateActivity,
        showRootPage = ::showRootPage
    )
    private val aboutPage = AboutPage(
        host = host,
        showProfilePage = ::showProfilePage
    )
    private val profileShortcutSection = FunctionCenterProfileShortcutSection(
        host = host,
        isPrivateBrowsingEnabled = isPrivateBrowsingEnabled,
        showHistory = ::showHistory,
        showPlaybackHistory = { playbackHistoryPage.show() },
        showBookmarks = ::showBookmarks,
        showDownloads = { downloadsPage.show() },
        showFileOperationsPage = showFileOperationsPage,
        showUserManualRules = { userManualRulesPage.show() },
        showAbout = { aboutPage.show() }
    )
    private val browserDataManagementPage = BrowserDataManagementPage(
        host = host,
        browserManager = browserManager,
        browserManagers = browserManagers,
        savedPageRepository = savedPageRepository,
        downloadRecordRepository = downloadRecordRepository,
        currentActionableUrl = currentActionableUrl,
        showBookmarkList = ::showBookmarks,
        showHistoryList = ::showHistory,
        showDownloadList = { downloadsPage.show() },
        exportBookmarks = exportBookmarks,
        importBookmarks = importBookmarks,
        showRootPage = ::showRootPage
    )
    private val restoreDefaultSettingsPage = RestoreDefaultSettingsPage(
        host = host,
        restoreDefaultSettings = restoreDefaultSettings
    )
    private val browserSettingsPage = BrowserSettingsPage(
        host = host,
        settingsManager = settingsManager,
        browserManager = browserManager,
        isPrivateBrowsingEnabled = isPrivateBrowsingEnabled,
        isAdBlockEnabled = isAdBlockEnabled,
        isSmartNoImageEnabled = isSmartNoImageEnabled,
        isJsInjectionEnabled = isJsInjectionEnabled,
        isPageCleanupEnabled = isPageCleanupEnabled,
        isVideoEnhancementEnabled = isVideoEnhancementEnabled,
        setPrivateBrowsingEnabled = setPrivateBrowsingEnabled,
        injectPageFeatures = injectPageFeatures,
        showBookmarks = ::showBookmarks,
        showBookmarkManager = { browserDataManagementPage.showBookmarkData() },
        showHistory = ::showHistory,
        showHistoryManager = { browserDataManagementPage.showBrowsingHistoryData() },
        showDownloadManager = { browserDataManagementPage.showDownloadData() },
        showFileOperationsPage = showFileOperationsPage,
        showAdBlockLog = { adBlockLogPage.show() },
        showUserWhitelistManager = { userWhitelistPage.show() },
        showUserManualRulesManager = { userManualRulesPage.show() },
        showSitePermissionsManager = { sitePermissionsPage.show() },
        showRuleSubscriptionsManager = { ruleSubscriptionPage.show() },
        showCookieManager = { browserDataManagementPage.showCookies() },
        showCacheManager = { browserDataManagementPage.showCache() },
        showSiteDataManager = { browserDataManagementPage.showSiteData() },
        showRestoreDefaultSettingsPage = restoreDefaultSettingsPage::show,
        currentSearchProviderName = currentSearchProviderName,
        selectSearchProvider = selectSearchProvider,
        showRootPage = ::showRootPage
    )

    private val activity = host.activity

    /**
     * 函数 `showRootPage`：控制 `show Root Page` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun showRootPage() {
        // 根页面是底部弹出的第一页，包含常用浏览器动作和当前页面相关工具。
        host.showBottomSheetPage(
            title = activity.getString(R.string.title_page_tools),
            onClose = { close() }
        ) { content ->
            val siteHost = currentSiteHost()
            val pageUrl = currentActionableUrl()
            FunctionCenterRootSheetLayout.blocks().forEach { block ->
                when (block) {
                    FunctionCenterRootSheetBlock.ACTION_GRID -> {
                        addBaiduBrowserActionGrid(content, pageUrl, siteHost)
                    }

                    FunctionCenterRootSheetBlock.HISTORY_PREVIEW -> {
                        host.addHistoryPreview(
                            parent = content,
                            title = activity.getString(R.string.menu_history_title),
                            emptyMessage = activity.getString(R.string.menu_history_empty),
                            pages = savedPageRepository.history(),
                            onOpenPage = { page ->
                                close()
                                loadUrl(page.url)
                            },
                            onShowHistory = ::showHistory
                        )
                    }

                    FunctionCenterRootSheetBlock.EXPANDED_BROWSER_SETTINGS -> {
                        browserSettingsPage.addExpandedBrowserSettings(content)
                    }

                    FunctionCenterRootSheetBlock.EXPANDED_DATA_MANAGEMENT -> {
                        browserSettingsPage.addExpandedDataManagement(content)
                    }
                }
            }
        }
    }

    /**
     * 函数 `showProfilePage`：控制 `show Profile Page` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun showProfilePage() {
        host.showPage(
            title = activity.getString(R.string.title_profile_page),
            onBack = { close() }
        ) { content ->
            FunctionCenterProfilePageLayout.blocks().forEach { block ->
                when (block) {
                    FunctionCenterProfilePageBlock.PROFILE_HEADER -> {
                        host.addProfileHeader(
                            parent = content,
                            title = activity.getString(R.string.function_center_profile_name),
                            summary = activity.getString(R.string.function_center_profile_summary)
                        ) {
                            browserSettingsPage.show()
                        }
                    }

                    FunctionCenterProfilePageBlock.SHORTCUTS -> addProfileShortcutSection(content)
                    FunctionCenterProfilePageBlock.FEATURES -> addProfileFeatureSection(content)
                }
            }
        }
    }

    /**
     * 函数 `showCurrentSiteSettingsPage`：控制 `show Current Site Settings Page` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun showCurrentSiteSettingsPage() {
        currentSiteSettingsPage.show()
    }

    /**
     * 函数 `handleBack`：处理 `handle Back` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun handleBack(): Boolean {
        return host.handleBack()
    }

    /**
     * 函数 `close`：控制 `close` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun close(): Boolean {
        return host.close()
    }

    /**
     * 函数 `addFunctionNavigationSection`：封装 `add Function Navigation Section` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param parent 参数类型为 `LinearLayout`，表示函数执行 `parent` 相关逻辑时需要读取或处理的输入。
     * @param siteHost 参数类型为 `String?`，表示函数执行 `siteHost` 相关逻辑时需要读取或处理的输入。
     */
    private fun addFunctionNavigationSection(parent: LinearLayout, siteHost: String?) {
        host.addFunctionSection(
            parent,
            activity.getString(R.string.function_center_section_more)
        ) { section ->
            if (!isPrivateBrowsingEnabled()) {
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_site_settings),
                    summary = siteHost ?: activity.getString(R.string.function_center_site_action_unavailable),
                    enabled = siteHost != null
                ) {
                    currentSiteSettingsPage.show()
                }
            }
            host.addActionRow(
                parent = section,
                title = activity.getString(R.string.action_browser_settings),
                summary = activity.getString(R.string.action_browser_settings_summary)
            ) {
                browserSettingsPage.show()
            }
        }
    }

    /**
     * 函数 `addBaiduBrowserActionGrid`：封装 `add Baidu Browser Action Grid` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param parent 参数类型为 `LinearLayout`，表示函数执行 `parent` 相关逻辑时需要读取或处理的输入。
     * @param pageUrl 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param siteHost 参数类型为 `String?`，表示函数执行 `siteHost` 相关逻辑时需要读取或处理的输入。
     */
    private fun addBaiduBrowserActionGrid(
        parent: LinearLayout,
        pageUrl: String?,
        siteHost: String?
    ) {
        val pageSummary = pageUrl
            ?.let(UrlUtils::displayUrl)
            ?: activity.getString(R.string.function_center_page_action_unavailable)
        val siteSummary = siteHost
            ?: activity.getString(R.string.function_center_site_action_unavailable)
        val hasPage = pageUrl != null

        host.addFunctionSection(parent, "") { section ->
            host.addActionGrid(
                section,
                FunctionCenterRootActionCatalog.actions(
                    hasPage = hasPage,
                    hasSite = siteHost != null,
                    isPrivateBrowsing = isPrivateBrowsingEnabled()
                ).map { action ->
                    createRootGridAction(action, pageSummary, siteSummary, hasPage)
                }
            )
        }
    }

    /**
     * 函数 `createRootGridAction`：创建 `create Root Grid Action` 需要的对象、视图或配置，并返回给后续流程使用。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param action 参数类型为 `FunctionCenterRootAction`，表示函数执行 `action` 相关逻辑时需要读取或处理的输入。
     * @param pageSummary 参数类型为 `String`，表示函数执行 `pageSummary` 相关逻辑时需要读取或处理的输入。
     * @param siteSummary 参数类型为 `String`，表示函数执行 `siteSummary` 相关逻辑时需要读取或处理的输入。
     * @param hasPage 参数类型为 `Boolean`，表示函数执行 `hasPage` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun createRootGridAction(
        action: FunctionCenterRootAction,
        pageSummary: String,
        siteSummary: String,
        hasPage: Boolean
    ): FunctionCenterGridAction {
        return when (action) {
            FunctionCenterRootAction.TABS -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.action_show_tabs),
                    summary = activity.getString(R.string.action_show_tabs_summary),
                    iconResId = R.drawable.ic_tabs_24
                ) {
                    browserTabsPage.show()
                }
            }

            FunctionCenterRootAction.HOME -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.setting_home_page),
                    summary = activity.getString(R.string.action_open_home_page_summary),
                    iconResId = R.drawable.ic_home_24,
                    enabled = hasPage
                ) {
                    runPageAction(openHomePage)
                }
            }

            FunctionCenterRootAction.SHARE_PAGE -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.action_share_page),
                    summary = activity.getString(R.string.action_share_page_summary),
                    iconResId = R.drawable.ic_share_24,
                    enabled = hasPage
                ) {
                    runPageAction(shareCurrentUrl)
                }
            }

            FunctionCenterRootAction.SAVE_PAGE_ARCHIVE -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.action_save_page_archive),
                    summary = activity.getString(R.string.action_save_page_archive_summary),
                    iconResId = R.drawable.ic_file_24,
                    enabled = hasPage
                ) {
                    runPageAction(saveCurrentPageArchive)
                }
            }

            FunctionCenterRootAction.PRINT_PAGE -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.action_print_page),
                    summary = activity.getString(R.string.action_print_page_summary),
                    iconResId = R.drawable.ic_print_24,
                    enabled = hasPage
                ) {
                    runPageAction(printCurrentPage)
                }
            }

            FunctionCenterRootAction.BOOKMARKS -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.title_bookmarks),
                    summary = activity.getString(R.string.action_show_bookmarks_summary),
                    iconResId = R.drawable.ic_star_24
                ) {
                    showBookmarks()
                }
            }

            FunctionCenterRootAction.HISTORY -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.title_history),
                    summary = activity.getString(R.string.action_show_history_summary),
                    iconResId = R.drawable.ic_history_24
                ) {
                    showHistory()
                }
            }

            FunctionCenterRootAction.PLAYBACK_HISTORY -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.title_playback_history),
                    summary = activity.getString(R.string.action_show_playback_history_summary),
                    iconResId = R.drawable.ic_history_24
                ) {
                    playbackHistoryPage.show()
                }
            }

            FunctionCenterRootAction.DOWNLOADS -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.title_downloads),
                    summary = activity.getString(R.string.action_show_downloads_summary),
                    iconResId = R.drawable.ic_download_24
                ) {
                    downloadsPage.show()
                }
            }

            FunctionCenterRootAction.FILE_OPERATIONS -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.action_file_operations),
                    summary = activity.getString(R.string.action_file_operations_summary),
                    iconResId = R.drawable.ic_file_24
                ) {
                    showFileOperationsPage()
                }
            }

            FunctionCenterRootAction.REFRESH -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.action_refresh),
                    summary = pageSummary,
                    iconResId = R.drawable.ic_refresh_24,
                    enabled = hasPage
                ) {
                    browserManager().reload()
                    close()
                }
            }

            FunctionCenterRootAction.DESKTOP_MODE -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.setting_desktop_mode),
                    summary = activity.getString(R.string.setting_desktop_mode_summary),
                    iconResId = R.drawable.ic_tabs_24,
                    enabled = hasPage
                ) {
                    val enabled = !isDesktopModeEnabled()
                    settingsManager.setDesktopModeEnabled(enabled)
                    applyDesktopMode(true)
                    showFeatureToggleToast(activity.getString(R.string.setting_desktop_mode), enabled)
                    close()
                }
            }

            FunctionCenterRootAction.ADD_BOOKMARK -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.action_add_bookmark),
                    summary = pageSummary,
                    iconResId = R.drawable.ic_star_filled_24,
                    enabled = hasPage
                ) {
                    runPageAction(toggleCurrentBookmark)
                }
            }

            FunctionCenterRootAction.PICK_ELEMENT -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.action_pick_element),
                    summary = siteSummary,
                    iconResId = R.drawable.ic_search_24,
                    enabled = hasPage
                ) {
                    close()
                    startElementPicker()
                }
            }

            FunctionCenterRootAction.MORE -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.function_center_section_more),
                    summary = siteSummary,
                    iconResId = R.drawable.ic_more_vert_24
                ) {
                    currentSiteSettingsPage.show()
                }
            }
        }
    }

    /**
     * 函数 `addProfileShortcutSection`：封装 `add Profile Shortcut Section` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param parent 参数类型为 `LinearLayout`，表示函数执行 `parent` 相关逻辑时需要读取或处理的输入。
     */
    private fun addProfileShortcutSection(parent: LinearLayout) {
        profileShortcutSection.add(parent)
    }

    /**
     * 函数 `addProfileFeatureSection`：封装 `add Profile Feature Section` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param parent 参数类型为 `LinearLayout`，表示函数执行 `parent` 相关逻辑时需要读取或处理的输入。
     */
    private fun addProfileFeatureSection(parent: LinearLayout) {
        browserSettingsPage.addExpandedBrowserSettings(parent)
        browserSettingsPage.addProfileDataManagement(parent)
    }

    /**
     * 函数 `addCurrentPageActionSection`：封装 `add Current Page Action Section` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param parent 参数类型为 `LinearLayout`，表示函数执行 `parent` 相关逻辑时需要读取或处理的输入。
     * @param pageUrl 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param siteHost 参数类型为 `String?`，表示函数执行 `siteHost` 相关逻辑时需要读取或处理的输入。
     */
    private fun addCurrentPageActionSection(
        parent: LinearLayout,
        pageUrl: String?,
        siteHost: String?
    ) {
        val pageSummary = pageUrl
            ?.let(UrlUtils::displayUrl)
            ?: activity.getString(R.string.function_center_page_action_unavailable)
        val siteSummary = siteHost
            ?: activity.getString(R.string.function_center_site_action_unavailable)
        val hasPage = pageUrl != null
        val bookmarkTitle = if (pageUrl?.let(savedPageRepository::isBookmarked) == true) {
            activity.getString(R.string.action_remove_bookmark)
        } else {
            activity.getString(R.string.action_add_bookmark)
        }

        host.addFunctionSection(
            parent,
            activity.getString(R.string.function_center_section_page_actions)
        ) { section ->
            val actions = mutableListOf(
                FunctionCenterGridAction(
                    title = bookmarkTitle,
                    summary = pageSummary,
                    iconResId = R.drawable.ic_star_24,
                    enabled = hasPage
                ) {
                    runPageAction(toggleCurrentBookmark)
                },
                FunctionCenterGridAction(
                    title = activity.getString(R.string.action_set_current_page_as_home),
                    summary = activity.getString(R.string.action_set_current_page_as_home_summary),
                    iconResId = R.drawable.ic_home_24,
                    enabled = hasPage
                ) {
                    runPageAction(setCurrentPageAsHomePage)
                },
            )
            if (!isPrivateBrowsingEnabled()) {
                actions.add(
                    FunctionCenterGridAction(
                        title = activity.getString(R.string.action_pick_element),
                        summary = siteSummary,
                        iconResId = R.drawable.ic_search_24,
                        enabled = siteHost != null
                    ) {
                        close()
                        startElementPicker()
                    },
                )
            }
            actions.addAll(
                listOf(
                    FunctionCenterGridAction(
                        title = activity.getString(R.string.action_forward),
                        summary = activity.getString(R.string.action_forward_summary),
                        iconResId = R.drawable.ic_arrow_forward_24,
                        enabled = hasPage && browserManager().canGoForward()
                    ) {
                        runPageAction { browserManager().goForward() }
                    },
                    FunctionCenterGridAction(
                        title = activity.getString(R.string.action_find_in_page),
                        summary = activity.getString(R.string.action_find_in_page_summary),
                        iconResId = R.drawable.ic_search_24,
                        enabled = hasPage
                    ) {
                        runPageAction(findInPage)
                    },
                    FunctionCenterGridAction(
                        title = activity.getString(R.string.action_copy_link),
                        summary = activity.getString(R.string.action_copy_link_summary),
                        iconResId = R.drawable.ic_file_24,
                        enabled = hasPage
                    ) {
                        runPageAction(copyCurrentUrl)
                    },
                    FunctionCenterGridAction(
                        title = activity.getString(R.string.action_share_page),
                        summary = activity.getString(R.string.action_share_page_summary),
                        iconResId = R.drawable.ic_share_24,
                        enabled = hasPage
                    ) {
                        runPageAction(shareCurrentUrl)
                    },
                    FunctionCenterGridAction(
                        title = activity.getString(R.string.action_save_page_archive),
                        summary = activity.getString(R.string.action_save_page_archive_summary),
                        iconResId = R.drawable.ic_file_24,
                        enabled = hasPage
                    ) {
                        runPageAction(saveCurrentPageArchive)
                    },
                    FunctionCenterGridAction(
                        title = activity.getString(R.string.action_print_page),
                        summary = activity.getString(R.string.action_print_page_summary),
                        iconResId = R.drawable.ic_print_24,
                        enabled = hasPage
                    ) {
                        runPageAction(printCurrentPage)
                    },
                    FunctionCenterGridAction(
                        title = activity.getString(R.string.action_download_current_url),
                        summary = activity.getString(R.string.action_download_current_url_summary),
                        iconResId = R.drawable.ic_download_24,
                        enabled = hasPage
                    ) {
                        runPageAction(downloadCurrentUrl)
                    },
                    FunctionCenterGridAction(
                        title = activity.getString(R.string.action_open_native_player),
                        summary = activity.getString(R.string.action_open_native_player_summary),
                        iconResId = R.drawable.ic_wenxin_wave_24,
                        enabled = hasPage
                    ) {
                        runPageAction(openCurrentUrlInNativePlayer)
                    }
                )
            )
            host.addActionGrid(section, actions)
            if (isPrivateBrowsingEnabled()) {
                return@addFunctionSection
            }
            host.addDivider(section)
            host.addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_desktop_mode),
                summary = activity.getString(R.string.setting_desktop_mode_summary),
                checked = isDesktopModeEnabled(),
                enabled = hasPage
            ) { enabled ->
                settingsManager.setDesktopModeEnabled(enabled)
                applyDesktopMode(true)
            }
            host.addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_smart_no_image),
                summary = activity.getString(R.string.setting_smart_no_image_summary),
                checked = isSmartNoImageEnabled(),
                enabled = hasPage
            ) { enabled ->
                settingsManager.setSmartNoImageEnabled(enabled)
                browserManager().reload()
                showFeatureToggleToast(activity.getString(R.string.setting_smart_no_image), enabled)
            }
            host.addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_page_cleanup),
                summary = activity.getString(R.string.setting_page_cleanup_summary),
                checked = isPageCleanupEnabled(),
                enabled = hasPage
            ) { enabled ->
                settingsManager.setDomAdBlockEnabled(enabled)
                injectPageFeatures()
                showFeatureToggleToast(activity.getString(R.string.setting_page_cleanup), enabled)
            }
            host.addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_video_enhancement),
                summary = activity.getString(R.string.setting_video_enhancement_summary),
                checked = isVideoEnhancementEnabled(),
                enabled = hasPage
            ) { enabled ->
                settingsManager.setVideoEnhancementEnabled(enabled)
                injectPageFeatures()
                showFeatureToggleToast(activity.getString(R.string.setting_video_enhancement), enabled)
            }
        }
    }

    /**
     * 函数 `runPageAction`：封装 `run Page Action` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param action 参数类型为 `() -> Unit`，表示函数执行 `action` 相关逻辑时需要读取或处理的输入。
     */
    private fun runPageAction(action: () -> Unit) {
        action()
        close()
    }

    /**
     * 函数 `showFeatureToggleToast`：控制 `show Feature Toggle Toast` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param featureName 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param enabled 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     */
    private fun showFeatureToggleToast(featureName: String, enabled: Boolean) {
        Toast.makeText(
            activity,
            activity.getString(
                if (enabled) R.string.toast_feature_enabled else R.string.toast_feature_disabled,
                featureName
            ),
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * 函数 `showBookmarks`：控制 `show Bookmarks` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun showBookmarks() {
        savedPagesPage.show(
            collection = SavedPageCollection.BOOKMARKS,
            title = activity.getString(R.string.title_bookmarks),
            emptyMessage = activity.getString(R.string.toast_bookmarks_empty)
        )
    }

    /**
     * 函数 `showHistory`：控制 `show History` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun showHistory() {
        savedPagesPage.show(
            collection = SavedPageCollection.HISTORY,
            title = activity.getString(R.string.title_history),
            emptyMessage = activity.getString(R.string.toast_history_empty)
        )
    }
}
