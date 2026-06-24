package com.example.videobrowser.functioncenter

/**
 * 初学者阅读提示：
 * 这个文件属于“功能中心装配模块”。
 * 文件名 FunctionCenterAssemblyController 可以拆开理解为“Function Center Assembly Controller”，
 * 表示它只负责把浏览器、下载、收藏、设置等控制器连接到功能中心页面。
 * 阅读顺序：先看构造参数知道每类动作来自哪里，再看 createEntryController() 如何生成入口控制器。
 */
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.adblock.AdBlockLogger
import com.example.videobrowser.browser.BrowserActivityResultLaunchers
import com.example.videobrowser.browser.BrowserDisplayModeController
import com.example.videobrowser.browser.BrowserFeatureStateController
import com.example.videobrowser.browser.BrowserLaunchController
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.browser.BrowserNavigationController
import com.example.videobrowser.browser.BrowserPageToolEntryController
import com.example.videobrowser.browser.BrowserTabActionsController
import com.example.videobrowser.browser.BrowserUrlStateController
import com.example.videobrowser.browser.PageActionsController
import com.example.videobrowser.browser.search.SearchProviderController
import com.example.videobrowser.download.DownloadController
import com.example.videobrowser.download.DownloadRecordRepository
import com.example.videobrowser.inject.PageFeatureInjectionController
import com.example.videobrowser.localfiles.LocalDocumentEntryController
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.storage.SavedPageRepository
import com.example.videobrowser.video.PlaybackHistoryRepository
import java.io.File

/**
 * 功能中心装配控制器。
 *
 * MainActivity 仍然决定各个浏览器子控制器的初始化顺序；本类只集中描述这些控制器如何映射到
 * FunctionCenterPages 的页面动作，避免主 Activity 持有一大段功能中心路由表。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示功能中心页面需要使用的宿主 Activity。
 * @param functionCenter 参数类型为 `FunctionCenterController`，表示负责显示和关闭功能中心容器的控制器。
 * @param settingsManager 参数类型为 `SettingsManager`，表示功能中心设置页面读写全局设置的数据源。
 * @param browserManager 参数类型为 `() -> BrowserManager`，表示返回当前浏览模式 BrowserManager 的函数。
 * @param browserManagers 参数类型为 `() -> List<BrowserManager>`，表示返回所有 BrowserManager 的函数，用于批量应用设置。
 * @param savedPageRepository 参数类型为 `SavedPageRepository`，表示收藏夹和历史记录的数据仓库。
 * @param downloadRecordRepository 参数类型为 `DownloadRecordRepository`，表示下载记录页面使用的数据仓库。
 * @param playbackHistoryRepository 参数类型为 `PlaybackHistoryRepository`，表示原生播放历史页面使用的数据仓库。
 * @param adBlockLogger 参数类型为 `AdBlockLogger`，表示规则调试和拦截日志页面读取的日志来源。
 * @param filesDir 参数类型为 `File`，表示应用私有文件目录，用于功能中心展示规则订阅等文件状态。
 * @param browserUrlStateController 参数类型为 `BrowserUrlStateController`，表示提供当前站点 host 和可操作 URL 的控制器。
 * @param browserFeatureStateController 参数类型为 `BrowserFeatureStateController`，表示提供桌面模式、无痕和页面增强开关状态的控制器。
 * @param browserTabActionsController 参数类型为 `BrowserTabActionsController`，表示提供新建、切换、关闭和复制标签页动作的控制器。
 * @param browserLaunchController 参数类型为 `BrowserLaunchController`，表示提供打开主页等浏览器启动动作的控制器。
 * @param pageActionsController 参数类型为 `PageActionsController`，表示提供收藏、分享、下载当前页和恢复默认设置等页面动作的控制器。
 * @param browserPageToolEntryController 参数类型为 `BrowserPageToolEntryController`，表示提供保存归档、打印、页内查找和播放历史打开动作的控制器。
 * @param downloadController 参数类型为 `DownloadController`，表示提供下载重试动作的控制器。
 * @param activityResultLaunchers 参数类型为 `BrowserActivityResultLaunchers`，表示提供收藏夹导入导出系统选择器入口的启动器集合。
 * @param searchProviderController 参数类型为 `SearchProviderController`，表示提供当前搜索引擎名称和默认搜索引擎选择动作的控制器。
 * @param localDocumentEntryController 参数类型为 `LocalDocumentEntryController`，表示提供本地文件操作页入口的控制器。
 * @param startElementPicker 参数类型为 `() -> Unit`，表示启动页面元素选择器的回调。
 * @param browserDisplayModeController 参数类型为 `BrowserDisplayModeController`，表示提供桌面模式应用动作的控制器。
 * @param pageFeatureInjectionController 参数类型为 `PageFeatureInjectionController`，表示提供页面增强脚本重新注入动作的控制器。
 * @param browserNavigationController 参数类型为 `BrowserNavigationController`，表示提供 URL 加载动作的控制器。
 * @param hideKeyboard 参数类型为 `() -> Unit`，表示进入功能中心前隐藏软键盘的回调。
 * @param recreateActivity 参数类型为 `() -> Unit`，表示需要重建 Activity 时调用的回调。
 */
class FunctionCenterAssemblyController(
    private val activity: AppCompatActivity,
    private val functionCenter: FunctionCenterController,
    private val settingsManager: SettingsManager,
    private val browserManager: () -> BrowserManager,
    private val browserManagers: () -> List<BrowserManager>,
    private val savedPageRepository: SavedPageRepository,
    private val downloadRecordRepository: DownloadRecordRepository,
    private val playbackHistoryRepository: PlaybackHistoryRepository,
    private val adBlockLogger: AdBlockLogger,
    private val filesDir: File,
    private val browserUrlStateController: BrowserUrlStateController,
    private val browserFeatureStateController: BrowserFeatureStateController,
    private val browserTabActionsController: BrowserTabActionsController,
    private val browserLaunchController: BrowserLaunchController,
    private val pageActionsController: PageActionsController,
    private val browserPageToolEntryController: BrowserPageToolEntryController,
    private val downloadController: DownloadController,
    private val activityResultLaunchers: BrowserActivityResultLaunchers,
    private val searchProviderController: SearchProviderController,
    private val localDocumentEntryController: LocalDocumentEntryController,
    private val startElementPicker: () -> Unit,
    private val browserDisplayModeController: BrowserDisplayModeController,
    private val pageFeatureInjectionController: PageFeatureInjectionController,
    private val browserNavigationController: BrowserNavigationController,
    private val hideKeyboard: () -> Unit,
    private val recreateActivity: () -> Unit
) {
    /**
     * 创建功能中心入口控制器。
     *
     * @return 返回 `FunctionCenterEntryController`，调用方可以用它打开根页、个人页、站点设置页或关闭功能中心。
     */
    fun createEntryController(): FunctionCenterEntryController {
        val functionCenterPages = FunctionCenterPages(
            activity = activity,
            functionCenter = functionCenter,
            settingsManager = settingsManager,
            browserManager = browserManager,
            browserManagers = browserManagers,
            savedPageRepository = savedPageRepository,
            downloadRecordRepository = downloadRecordRepository,
            playbackHistoryRepository = playbackHistoryRepository,
            adBlockLogger = adBlockLogger,
            filesDir = filesDir,
            currentSiteHost = browserUrlStateController::currentSiteHost,
            currentActionableUrl = browserUrlStateController::currentActionableUrl,
            isDesktopModeEnabled = browserFeatureStateController::isDesktopModeEnabled,
            isPrivateBrowsingEnabled = browserFeatureStateController::isPrivateBrowsingEnabled,
            isAdBlockEnabled = browserFeatureStateController::isAdBlockEnabled,
            isSmartNoImageEnabled = browserFeatureStateController::isSmartNoImageEnabled,
            isJsInjectionEnabled = browserFeatureStateController::isJsInjectionEnabled,
            isPageCleanupEnabled = browserFeatureStateController::isPageCleanupEnabled,
            isVideoEnhancementEnabled = browserFeatureStateController::isVideoEnhancementEnabled,
            currentTabs = browserTabActionsController::currentTabs,
            activeTabId = browserTabActionsController::activeTabId,
            openNewTab = browserTabActionsController::openNewTab,
            openHomePage = browserLaunchController::openHomePage,
            canReopenClosedTab = browserTabActionsController::canReopenClosedTab,
            reopenClosedTab = browserTabActionsController::reopenClosedTab,
            switchTab = browserTabActionsController::switchTab,
            closeTab = browserTabActionsController::closeTab,
            closeOtherTabs = browserTabActionsController::closeOtherTabs,
            closeAllTabs = browserTabActionsController::closeAllTabs,
            duplicateTab = browserTabActionsController::duplicateTab,
            toggleCurrentBookmark = pageActionsController::toggleCurrentBookmark,
            shareCurrentUrl = pageActionsController::shareCurrentUrl,
            saveCurrentPageArchive = browserPageToolEntryController::saveCurrentPageArchive,
            printCurrentPage = browserPageToolEntryController::printCurrentPage,
            openPlaybackHistoryItem = browserPageToolEntryController::openPlaybackHistoryItem,
            retryDownload = downloadController::retry,
            exportBookmarks = activityResultLaunchers::launchBookmarkExport,
            importBookmarks = activityResultLaunchers::launchBookmarkImport,
            availableSearchProviders = searchProviderController::availableProviders,
            currentSearchProviderId = { searchProviderController.selectedProvider.id },
            selectSearchProvider = searchProviderController::selectDefaultSearchProvider,
            setPrivateBrowsingEnabled = pageActionsController::setPrivateBrowsingEnabled,
            restoreDefaultSettings = pageActionsController::restoreDefaultSettings,
            showFileOperationsPage = localDocumentEntryController::showFileOperationsPage,
            startElementPicker = startElementPicker,
            applyDesktopMode = browserDisplayModeController::applyDesktopMode,
            injectPageFeatures = pageFeatureInjectionController::injectPageFeatures,
            openUrlInNewTab = browserTabActionsController::openUrlInNewTab,
            loadUrl = browserNavigationController::loadUrl,
            recreateActivity = recreateActivity
        )
        return FunctionCenterEntryController(
            functionCenterPages = functionCenterPages,
            hideKeyboard = hideKeyboard
        )
    }
}
