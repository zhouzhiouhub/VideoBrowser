package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 PageActionsController 可以拆开理解为“Page Actions Controller”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：封装 WebView 页面加载、标签页、导航安全、页面工具、权限回调或浏览器控件状态。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
import android.net.Uri
import android.provider.OpenableColumns
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.download.DownloadController
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.site.SiteHost
import com.example.videobrowser.storage.SavedPage
import com.example.videobrowser.storage.SavedPageRepository
import com.example.videobrowser.utils.ChooserIntentLauncher
import com.example.videobrowser.utils.FileOpenIntentFactory
import com.example.videobrowser.utils.MediaUrlUtils
import com.example.videobrowser.utils.PageUrlActions
import com.example.videobrowser.utils.PageUnavailableToast
import com.example.videobrowser.utils.ShortToast
import com.example.videobrowser.utils.columnValueReader
import com.example.videobrowser.video.ExternalSubtitleCandidate
import com.example.videobrowser.video.MediaRouteAction
import com.example.videobrowser.video.MediaRouteRequest
import com.example.videobrowser.video.MediaRouteSource
import com.example.videobrowser.video.MediaRoutingController
import com.example.videobrowser.video.PlaybackQueue

class PageActionsController(
    private val activity: AppCompatActivity,
    private val browserManager: () -> BrowserManager,
    private val browserManagers: () -> List<BrowserManager>,
    private val downloadController: DownloadController,
    private val settingsManager: SettingsManager,
    private val savedPageRepository: SavedPageRepository,
    private val currentActionableUrl: () -> String?,
    private val currentShareableUrl: () -> String?,
    private val currentPageTitle: () -> String,
    private val isShareableUrl: (String) -> Boolean,
    private val shouldRecordHistoryUrl: (String?) -> Boolean = { true },
    private val openNativePlayer: (
        url: String,
        mimeType: String?,
        userAgentOverride: String?,
        titleOverride: String?,
        subtitleCandidates: List<ExternalSubtitleCandidate>,
        playbackQueue: PlaybackQueue?
    ) -> Unit,
    private val openLocalArchiveInBrowser: (String) -> Unit,
    private val isPrivateBrowsingEnabled: () -> Boolean,
    private val switchPrivateBrowsing: (Boolean) -> Unit,
    private val updateBookmarkButton: () -> Unit,
    private val updateNavigationButtons: () -> Unit,
    private val updatePrivateBrowsingUi: () -> Unit,
    private val recreateActivity: () -> Unit,
    private val restoreBrowserDefaults: () -> Boolean = settingsManager::restoreDefaults
) {
    /**
     * 函数 `openLocalDocumentUri`：启动或加载 `open Local Document Uri` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param uri 参数类型为 `Uri`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param displayName 参数类型为 `String?`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param mimeType 参数类型为 `String?`，表示函数执行 `mimeType` 相关逻辑时需要读取或处理的输入。
     * @param subtitleCandidates 参数类型为 `List<ExternalSubtitleCandidate>`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param playbackQueue 参数类型为 `PlaybackQueue?`，表示函数执行 `playbackQueue` 相关逻辑时需要读取或处理的输入。
     */
    fun openLocalDocumentUri(
        uri: Uri,
        displayName: String? = null,
        mimeType: String? = null,
        subtitleCandidates: List<ExternalSubtitleCandidate> = emptyList(),
        playbackQueue: PlaybackQueue? = null
    ) {
        val resolvedMimeType = mimeType ?: activity.contentResolver.getType(uri)
        val title = displayName ?: localDisplayName(uri)
        if (LocalWebArchivePolicy.isWebArchive(title, resolvedMimeType)) {
            openLocalArchiveInBrowser(uri.toString())
            return
        }
        val mediaDecision = MediaRoutingController.route(
            MediaRouteRequest(
                source = MediaRouteSource.LOCAL_DOCUMENT,
                url = uri.toString(),
                mimeType = resolvedMimeType,
                displayName = title
            )
        )
        if (mediaDecision.action == MediaRouteAction.OPEN_NATIVE_PLAYER) {
            val mediaItem = mediaDecision.mediaItem
            openNativePlayer(
                mediaItem?.uri ?: uri.toString(),
                mediaItem?.mimeType ?: resolvedMimeType,
                null,
                mediaItem?.title ?: title,
                subtitleCandidates,
                playbackQueue
            )
            return
        }

        openExternalDocument(uri, resolvedMimeType)
    }

    /**
     * 函数 `downloadCurrentUrl`：封装 `download Current Url` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun downloadCurrentUrl() {
        val url = currentShareableUrlOrShowUnavailable() ?: return
        downloadController.enqueue(
            url = url,
            userAgent = browserManager().userAgentString(),
            contentDisposition = null,
            mimeType = null
        )
    }

    /**
     * 函数 `toggleCurrentBookmark`：封装 `toggle Current Bookmark` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun toggleCurrentBookmark() {
        val page = currentSavedPageOrShowUnavailable() ?: return

        if (savedPageRepository.isBookmarked(page.url)) {
            savedPageRepository.removeBookmark(page.url)
            ShortToast.show(activity, R.string.toast_bookmark_removed)
        } else {
            savedPageRepository.addBookmark(page)
            ShortToast.show(activity, R.string.toast_bookmark_saved)
        }
        updateBookmarkButton()
    }

    /**
     * 函数 `copyCurrentUrl`：封装 `copy Current Url` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun copyCurrentUrl() {
        val url = currentShareableUrlOrShowUnavailable() ?: return
        PageUrlActions.copyPageUrl(activity, url)
    }

    /**
     * 函数 `shareCurrentUrl`：封装 `share Current Url` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun shareCurrentUrl() {
        val url = currentShareableUrlOrShowUnavailable() ?: return
        PageUrlActions.sharePageUrl(activity, url)
    }

    /**
     * 函数 `setCurrentPageAsHomePage`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun setCurrentPageAsHomePage() {
        val url = currentShareableUrlOrShowUnavailable() ?: return
        if (!settingsManager.isValidHomeUrl(url)) {
            ShortToast.show(activity, R.string.toast_home_page_invalid)
            return
        }
        settingsManager.setHomeUrl(url)
        ShortToast.show(activity, R.string.toast_home_page_updated)
    }

    /**
     * 函数 `openCurrentUrlInNativePlayer`：启动或加载 `open Current Url In Native Player` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun openCurrentUrlInNativePlayer() {
        val url = currentShareableUrlOrShowUnavailable() ?: return
        if (!MediaUrlUtils.isPlayableMediaUri(url)) {
            ShortToast.show(activity, R.string.toast_media_url_unsupported)
            return
        }
        openNativePlayer(url, null, null, null, emptyList(), null)
    }

    /**
     * 函数 `setPrivateBrowsingEnabled`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param enabled 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     */
    fun setPrivateBrowsingEnabled(enabled: Boolean) {
        if (isPrivateBrowsingEnabled() == enabled) {
            updatePrivateBrowsingUi()
            return
        }

        switchPrivateBrowsing(enabled)
        updatePrivateBrowsingUi()
        updateNavigationButtons()
        ShortToast.show(
            activity,
            if (enabled) {
                R.string.toast_private_browsing_enabled
            } else {
                R.string.toast_private_browsing_disabled
            }
        )
    }

    /**
     * 函数 `restoreDefaultSettings`：封装 `restore Default Settings` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun restoreDefaultSettings() {
        browserManagers().forEachIndexed { index, manager ->
            manager.clearBrowsingData(clearSharedStores = index == 0)
        }
        restoreBrowserDefaults()
        ShortToast.show(activity, R.string.toast_default_settings_restored)
        recreateActivity()
    }

    /**
     * 函数 `addHistoryEntry`：封装 `add History Entry` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     */
    fun addHistoryEntry(url: String?) {
        if (isPrivateBrowsingEnabled()) {
            return
        }
        val page = currentSavedPage(url) ?: return
        if (!shouldRecordHistoryUrl(page.url)) {
            return
        }
        savedPageRepository.addHistory(page)
    }

    private fun currentShareableUrlOrShowUnavailable(): String? {
        return currentShareableUrl() ?: run {
            PageUnavailableToast.showNoPageUrl(activity)
            null
        }
    }

    private fun currentSavedPageOrShowUnavailable(): SavedPage? {
        return currentSavedPage() ?: run {
            PageUnavailableToast.showNoPageUrl(activity)
            null
        }
    }

    /**
     * 函数 `openExternalDocument`：启动或加载 `open External Document` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param uri 参数类型为 `Uri`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param mimeType 参数类型为 `String?`，表示函数执行 `mimeType` 相关逻辑时需要读取或处理的输入。
     */
    private fun openExternalDocument(uri: Uri, mimeType: String?) {
        val intent = FileOpenIntentFactory.create(uri, mimeType)
        ChooserIntentLauncher.start(
            activity = activity,
            intent = intent,
            chooserTitleRes = R.string.action_open_file
        )
    }

    /**
     * 函数 `localDisplayName`：封装 `local Display Name` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param uri 参数类型为 `Uri`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun localDisplayName(uri: Uri): String? {
        return activity.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            if (!cursor.moveToFirst()) {
                null
            } else {
                cursor.columnValueReader().stringOrNull(OpenableColumns.DISPLAY_NAME)
            }
        }
    }

    /**
     * 函数 `currentSavedPage`：从现有状态、缓存或输入对象中取得目标数据，并把结果交给调用方继续处理。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param urlOverride 参数类型为 `String?`，表示函数执行 `urlOverride` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun currentSavedPage(urlOverride: String? = null): SavedPage? {
        val url = urlOverride ?: currentActionableUrl()
        if (url.isNullOrBlank() || !isShareableUrl(url)) {
            return null
        }
        val title = currentPageTitle()
            .takeIf { it.isNotBlank() && !it.equals(url, ignoreCase = true) }
            ?: SiteHost.fromUrl(url)
            ?: url
        return SavedPage(title = title, url = url)
    }

}
