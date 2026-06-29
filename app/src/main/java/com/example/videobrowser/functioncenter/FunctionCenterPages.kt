package com.example.videobrowser.functioncenter

/**
 * 初学者阅读提示：
 * 这个文件属于“功能中心模块”。
 * 文件名 FunctionCenterPages 可以拆开理解为“Function Center Pages”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：构建底部功能面板、设置页面、数据管理页面以及各种用户可点击的工具入口。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.adblock.AdBlockLogger
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.browser.BrowserTab
import com.example.videobrowser.browser.search.SearchProvider
import com.example.videobrowser.download.DownloadRecord
import com.example.videobrowser.download.DownloadRecordRepository
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.storage.SavedPageRepository
import com.example.videobrowser.storage.SavedPageRepository.SavedPageCollection
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
    private val shareCurrentUrl: () -> Unit,
    private val saveCurrentPageArchive: () -> Unit,
    private val printCurrentPage: () -> Unit,
    private val openPlaybackHistoryItem: (PlaybackProgress) -> Unit,
    private val retryDownload: (DownloadRecord) -> Unit,
    private val exportBookmarks: () -> Unit,
    private val importBookmarks: () -> Unit,
    private val availableSearchProviders: () -> List<SearchProvider>,
    private val currentSearchProviderId: () -> String,
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
    private val activity = host.activity
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
        startElementPicker = startElementPicker,
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
    private val searchEngineSettingsPage = SearchEngineSettingsPage(
        host = host,
        settingsManager = settingsManager,
        availableSearchProviders = availableSearchProviders,
        currentSearchProviderId = currentSearchProviderId,
        selectSearchProvider = selectSearchProvider,
        showProfilePage = ::showProfilePage
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
        showSearchEngines = { searchEngineSettingsPage.show() },
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
        showRootPage = ::showRootPage
    )
    private val profilePage = FunctionCenterProfilePage(
        host = host,
        profileShortcutSection = profileShortcutSection,
        browserSettingsPage = browserSettingsPage,
        closePage = { close() }
    )
    private val rootActionSection = FunctionCenterRootActionSection(
        host = host,
        settingsManager = settingsManager,
        browserManager = browserManager,
        isDesktopModeEnabled = isDesktopModeEnabled,
        isPrivateBrowsingEnabled = isPrivateBrowsingEnabled,
        openHomePage = openHomePage,
        shareCurrentUrl = shareCurrentUrl,
        saveCurrentPageArchive = saveCurrentPageArchive,
        printCurrentPage = printCurrentPage,
        toggleCurrentBookmark = toggleCurrentBookmark,
        startElementPicker = startElementPicker,
        applyDesktopMode = applyDesktopMode,
        showFeatureToggleToast = { featureName, enabled ->
            FeatureToggleToast.showGlobal(activity, featureName, enabled)
        },
        showBrowserTabs = { browserTabsPage.show() },
        showBookmarks = ::showBookmarks,
        showHistory = ::showHistory,
        showPlaybackHistory = { playbackHistoryPage.show() },
        showDownloads = { downloadsPage.show() },
        showFileOperationsPage = showFileOperationsPage,
        showCurrentSiteSettings = { currentSiteSettingsPage.show() }
    )
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
                        rootActionSection.add(content, pageUrl, siteHost)
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
        profilePage.show()
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
     * 函数 `showBookmarks`：控制 `show Bookmarks` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun showBookmarks() {
        showSavedPagesCollection(SavedPageCollection.BOOKMARKS)
    }

    /**
     * 函数 `showHistory`：控制 `show History` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun showHistory() {
        showSavedPagesCollection(SavedPageCollection.HISTORY)
    }

    private fun showSavedPagesCollection(collection: SavedPageCollection) {
        savedPagesPage.show(
            collection = collection,
            title = SavedPageCollectionDisplayText.title(activity, collection),
            emptyMessage = SavedPageCollectionDisplayText.emptyMessage(activity, collection)
        )
    }
}
