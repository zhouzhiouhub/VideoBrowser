package com.example.videobrowser.storage

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器持久化装配模块”。
 * 文件名 BrowserPersistenceAssemblyController 可以拆开理解为“Browser Persistence Assembly Controller”，
 * 表示它只负责创建依赖 SharedPreferences 的设置、收藏、标签会话、下载记录和播放历史对象。
 * 阅读顺序：先看 BrowserPersistenceComponents 了解会返回哪些对象，再看 create() 的初始化顺序。
 */
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.browser.BrowserFeatureStateController
import com.example.videobrowser.browser.BrowserSessionStateController
import com.example.videobrowser.browser.BrowserStandardTabSessionController
import com.example.videobrowser.browser.BrowserTabSessionRepository
import com.example.videobrowser.browser.BrowserTabStore
import com.example.videobrowser.browser.BrowserUrlStateController
import com.example.videobrowser.browser.BrowserShellUiController
import com.example.videobrowser.download.DownloadRecordRepository
import com.example.videobrowser.settings.BrowserDefaultSettingsResetter
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.video.PlaybackHistoryRepository
import com.example.videobrowser.video.WebPlaybackHistoryRecorder
import java.io.File

/**
 * 浏览器持久化组件集合。
 *
 * 本数据类只是把 create() 一次性创建出的对象带回 MainActivity，避免 Activity 自己逐项知道构造细节。
 *
 * @param preferenceStore 参数类型为 `PreferenceStore`，表示所有本地持久化数据共享的键值存储接口。
 * @param settingsManager 参数类型为 `SettingsManager`，表示浏览器全局设置读写入口。
 * @param savedPageRepository 参数类型为 `SavedPageRepository`，表示收藏夹和历史记录仓库。
 * @param bookmarkImportExportController 参数类型为 `BookmarkImportExportController`，表示收藏夹导入导出控制器。
 * @param browserTabSessionRepository 参数类型为 `BrowserTabSessionRepository`，表示标准标签页会话仓库。
 * @param browserStandardTabSessionController 参数类型为 `BrowserStandardTabSessionController`，表示标准标签页会话恢复和保存控制器。
 * @param downloadRecordRepository 参数类型为 `DownloadRecordRepository`，表示下载记录仓库。
 * @param playbackHistoryRepository 参数类型为 `PlaybackHistoryRepository`，表示原生播放历史仓库。
 * @param webPlaybackHistoryRecorder 参数类型为 `WebPlaybackHistoryRecorder`，表示网页播放进度写入播放历史的记录器。
 * @param browserDefaultSettingsResetter 参数类型为 `BrowserDefaultSettingsResetter`，表示恢复浏览器默认设置的控制器。
 */
data class BrowserPersistenceComponents(
    val preferenceStore: PreferenceStore,
    val settingsManager: SettingsManager,
    val savedPageRepository: SavedPageRepository,
    val bookmarkImportExportController: BookmarkImportExportController,
    val browserTabSessionRepository: BrowserTabSessionRepository,
    val browserStandardTabSessionController: BrowserStandardTabSessionController,
    val downloadRecordRepository: DownloadRecordRepository,
    val playbackHistoryRepository: PlaybackHistoryRepository,
    val webPlaybackHistoryRecorder: WebPlaybackHistoryRecorder,
    val browserDefaultSettingsResetter: BrowserDefaultSettingsResetter
)

/**
 * 浏览器持久化装配控制器。
 *
 * 它集中维护 SharedPreferences 相关对象的创建顺序，并在返回前恢复标准标签页会话。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示用于创建 PreferenceStore 和收藏导入导出控制器的宿主 Activity。
 * @param filesDir 参数类型为 `File`，表示应用私有文件目录，用于恢复默认设置时清理本地缓存。
 * @param standardTabStore 参数类型为 `BrowserTabStore`，表示标准模式标签页数据容器，会在恢复会话时写入。
 * @param browserShellUiController 参数类型为 `BrowserShellUiController`，表示收藏导入导出完成后刷新收藏按钮的控制器。
 * @param browserFeatureStateController 参数类型为 `BrowserFeatureStateController`，表示为播放历史记录器提供无痕模式状态。
 * @param browserUrlStateController 参数类型为 `BrowserUrlStateController`，表示为播放历史记录器提供当前可分享 URL。
 * @param browserSessionStateController 参数类型为 `BrowserSessionStateController`，表示为播放历史记录器提供当前页面标题。
 */
class BrowserPersistenceAssemblyController(
    private val activity: AppCompatActivity,
    private val filesDir: File,
    private val standardTabStore: BrowserTabStore,
    private val browserShellUiController: BrowserShellUiController,
    private val browserFeatureStateController: BrowserFeatureStateController,
    private val browserUrlStateController: BrowserUrlStateController,
    private val browserSessionStateController: BrowserSessionStateController
) {
    /**
     * 创建浏览器持久化组件并恢复标准标签页会话。
     *
     * @return 返回 `BrowserPersistenceComponents`，调用方把其中对象保存到对应字段后继续创建后续控制器。
     */
    fun create(): BrowserPersistenceComponents {
        val preferenceStore = PreferenceStore.from(activity)
        val settingsManager = SettingsManager(preferenceStore)
        val savedPageRepository = SavedPageRepository(preferenceStore)
        val bookmarkImportExportController = BookmarkImportExportController(
            activity = activity,
            savedPageRepository = savedPageRepository,
            updateBookmarkButton = browserShellUiController::updateBookmarkButton
        )
        val browserTabSessionRepository = BrowserTabSessionRepository(preferenceStore)
        val browserStandardTabSessionController = BrowserStandardTabSessionController(
            browserTabSessionRepository = { browserTabSessionRepository },
            standardTabStore = standardTabStore
        )
        browserStandardTabSessionController.restoreStandardTabSession()
        val downloadRecordRepository = DownloadRecordRepository(preferenceStore)
        val playbackHistoryRepository = PlaybackHistoryRepository(preferenceStore)
        val webPlaybackHistoryRecorder = WebPlaybackHistoryRecorder(
            playbackHistoryRepository = playbackHistoryRepository,
            isPrivateBrowsingEnabled = browserFeatureStateController::isPrivateBrowsingEnabled,
            currentShareableUrl = browserUrlStateController::currentShareableUrl,
            isShareableUrl = browserUrlStateController::isShareableUrl,
            defaultVideoSpeed = settingsManager::defaultVideoSpeed,
            currentPageTitle = {
                browserSessionStateController.currentSessionController().currentPageTitle
            }
        )
        val browserDefaultSettingsResetter = BrowserDefaultSettingsResetter(
            settingsManager = settingsManager,
            savedPageRepository = savedPageRepository,
            browserTabSessionRepository = browserTabSessionRepository,
            filesDir = filesDir
        )
        return BrowserPersistenceComponents(
            preferenceStore = preferenceStore,
            settingsManager = settingsManager,
            savedPageRepository = savedPageRepository,
            bookmarkImportExportController = bookmarkImportExportController,
            browserTabSessionRepository = browserTabSessionRepository,
            browserStandardTabSessionController = browserStandardTabSessionController,
            downloadRecordRepository = downloadRecordRepository,
            playbackHistoryRepository = playbackHistoryRepository,
            webPlaybackHistoryRecorder = webPlaybackHistoryRecorder,
            browserDefaultSettingsResetter = browserDefaultSettingsResetter
        )
    }
}
