package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器页面动作装配模块”。
 * 文件名 BrowserPageActionAssemblyController 可以拆开理解为“Browser Page Action Assembly Controller”，
 * 表示它只负责创建下载、当前页面动作、认证、归档、打印、页内查找和页面工具入口控制器。
 * 阅读顺序：先看 BrowserPageActionComponents 知道返回哪些对象，再看 create() 中页面动作链路如何连接。
 */
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.download.DownloadController
import com.example.videobrowser.download.DownloadRecordRepository
import com.example.videobrowser.localfiles.LocalDocumentEntryController
import com.example.videobrowser.settings.BrowserDefaultSettingsResetter
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.storage.SavedPageRepository
import com.example.videobrowser.video.NativePlayerEntryController

/**
 * 页面动作组件集合。
 *
 * @param downloadController 参数类型为 `DownloadController`，表示 WebView 下载监听和下载记录管理控制器。
 * @param pageActionsController 参数类型为 `PageActionsController`，表示收藏、分享、下载当前页、打开本地文件等当前页面动作控制器。
 * @param httpAuthController 参数类型为 `HttpAuthController`，表示 HTTP Basic Auth 弹窗和待处理请求控制器。
 * @param clientCertificateController 参数类型为 `ClientCertificateController`，表示客户端证书选择和回调控制器。
 * @param pageArchiveController 参数类型为 `PageArchiveController`，表示当前页面 MHTML 归档控制器。
 * @param pagePrintController 参数类型为 `PagePrintController`，表示当前页面打印控制器。
 * @param findInPageDialogController 参数类型为 `FindInPageDialogController`，表示页内查找弹窗控制器。
 * @param browserPageToolEntryController 参数类型为 `BrowserPageToolEntryController`，表示功能中心页面工具入口转发控制器。
 */
data class BrowserPageActionComponents(
    val downloadController: DownloadController,
    val pageActionsController: PageActionsController,
    val httpAuthController: HttpAuthController,
    val clientCertificateController: ClientCertificateController,
    val pageArchiveController: PageArchiveController,
    val pagePrintController: PagePrintController,
    val findInPageDialogController: FindInPageDialogController,
    val browserPageToolEntryController: BrowserPageToolEntryController
)

/**
 * 浏览器页面动作装配控制器。
 *
 * 下载、页面动作、归档、打印和页内查找都围绕“当前页面”工作；本类把这些控制器的构造集中起来，
 * 让 MainActivity 不再持有页面工具链路的具体 wiring。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示创建 Toast、系统弹窗、打印和归档操作的宿主 Activity。
 * @param downloadRecordRepository 参数类型为 `DownloadRecordRepository`，表示下载控制器写入和读取下载记录的数据仓库。
 * @param settingsManager 参数类型为 `SettingsManager`，表示页面动作读取和修改浏览器设置的数据源。
 * @param savedPageRepository 参数类型为 `SavedPageRepository`，表示页面动作读写收藏夹和历史记录的数据仓库。
 * @param browserDefaultSettingsResetter 参数类型为 `BrowserDefaultSettingsResetter`，表示恢复浏览器默认设置的控制器。
 * @param browserStandardWebViewHostController 参数类型为 `BrowserStandardWebViewHostController`，表示提供当前 BrowserManager 和所有 BrowserManager 的宿主控制器。
 * @param browserSessionStateController 参数类型为 `BrowserSessionStateController`，表示提供当前页面标题和会话状态的控制器。
 * @param browserUrlStateController 参数类型为 `BrowserUrlStateController`，表示提供当前可操作 URL、可分享 URL 和 URL 类型判断的控制器。
 * @param historyRecordPolicy 参数类型为 `HistoryRecordPolicy`，表示判断 URL 是否应该写入浏览历史的策略。
 * @param nativePlayerEntryController 参数类型为 `NativePlayerEntryController`，表示把媒体 URL 打开到原生播放器的入口控制器。
 * @param localDocumentEntryController 参数类型为 `LocalDocumentEntryController`，表示把本地 MHTML/MHT 归档加载回浏览器的入口控制器。
 * @param browserFeatureStateController 参数类型为 `BrowserFeatureStateController`，表示提供无痕浏览状态的控制器。
 * @param switchPrivateBrowsing 参数类型为 `(Boolean) -> Unit`，表示切换无痕浏览状态的回调。
 * @param browserShellUiController 参数类型为 `BrowserShellUiController`，表示刷新收藏按钮、导航按钮和首页内容状态的控制器。
 * @param browsingModeThemeController 参数类型为 `BrowsingModeThemeController`，表示刷新无痕主题 UI 的控制器。
 * @param activityResultLaunchers 参数类型为 `BrowserActivityResultLaunchers`，表示启动网页归档导出系统选择器的 Activity Result launcher 集合。
 * @param findInPageController 参数类型为 `FindInPageController`，表示页内查找业务控制器。
 * @param browserNavigationController 参数类型为 `BrowserNavigationController`，表示加载网页 URL 的导航控制器。
 * @param closeFunctionCenter 参数类型为 `() -> Boolean`，表示关闭功能中心面板的回调。
 * @param recreateActivity 参数类型为 `() -> Unit`，表示恢复默认设置等场景下重建 Activity 的回调。
 * @param dp 参数类型为 `(Int) -> Int`，表示把 dp 数值转换成像素的回调。
 */
class BrowserPageActionAssemblyController(
    private val activity: AppCompatActivity,
    private val downloadRecordRepository: DownloadRecordRepository,
    private val settingsManager: SettingsManager,
    private val savedPageRepository: SavedPageRepository,
    private val browserDefaultSettingsResetter: BrowserDefaultSettingsResetter,
    private val browserStandardWebViewHostController: BrowserStandardWebViewHostController,
    private val browserSessionStateController: BrowserSessionStateController,
    private val browserUrlStateController: BrowserUrlStateController,
    private val historyRecordPolicy: HistoryRecordPolicy,
    private val nativePlayerEntryController: NativePlayerEntryController,
    private val localDocumentEntryController: LocalDocumentEntryController,
    private val browserFeatureStateController: BrowserFeatureStateController,
    private val switchPrivateBrowsing: (Boolean) -> Unit,
    private val browserShellUiController: BrowserShellUiController,
    private val browsingModeThemeController: BrowsingModeThemeController,
    private val activityResultLaunchers: BrowserActivityResultLaunchers,
    private val findInPageController: FindInPageController,
    private val browserNavigationController: BrowserNavigationController,
    private val closeFunctionCenter: () -> Boolean,
    private val recreateActivity: () -> Unit,
    private val dp: (Int) -> Int
) {
    /**
     * 创建页面动作组件集合。
     *
     * @return 返回 `BrowserPageActionComponents`，调用方把其中对象保存到对应字段后继续创建权限和浏览器客户端控制器。
     */
    fun create(): BrowserPageActionComponents {
        val downloadController = DownloadController(
            activity = activity,
            browserManager = {
                browserStandardWebViewHostController.currentBrowserManager()
            },
            downloadRecordRepository = downloadRecordRepository,
            openNativePlayer = { url, mimeType, userAgentOverride, titleOverride ->
                nativePlayerEntryController.openNativePlayer(
                    url,
                    mimeType,
                    userAgentOverride,
                    titleOverride
                )
            }
        )
        val pageActionsController = PageActionsController(
            activity = activity,
            browserManager = {
                browserStandardWebViewHostController.currentBrowserManager()
            },
            browserManagers = {
                browserStandardWebViewHostController.browserManagers()
            },
            downloadController = downloadController,
            settingsManager = settingsManager,
            savedPageRepository = savedPageRepository,
            currentActionableUrl = browserUrlStateController::currentActionableUrl,
            currentShareableUrl = browserUrlStateController::currentShareableUrl,
            currentPageTitle = currentPageTitle(),
            isShareableUrl = browserUrlStateController::isShareableUrl,
            shouldRecordHistoryUrl = historyRecordPolicy::shouldRecord,
            openNativePlayer = nativePlayerEntryController::openNativePlayer,
            openLocalArchiveInBrowser = localDocumentEntryController::loadLocalDocumentUrlInBrowser,
            isPrivateBrowsingEnabled = browserFeatureStateController::isPrivateBrowsingEnabled,
            switchPrivateBrowsing = switchPrivateBrowsing,
            updateBookmarkButton = browserShellUiController::updateBookmarkButton,
            updateNavigationButtons = browserShellUiController::updateNavigationButtons,
            updatePrivateBrowsingUi = browsingModeThemeController::updatePrivateBrowsingUi,
            recreateActivity = recreateActivity,
            restoreBrowserDefaults = browserDefaultSettingsResetter::restoreDefaults
        )
        val httpAuthController = HttpAuthController(
            activity = activity,
            dp = dp
        )
        val clientCertificateController = ClientCertificateController(
            activity = activity
        )
        val pageArchiveController = PageArchiveController(
            activity = activity,
            currentActionableUrl = browserUrlStateController::currentActionableUrl,
            currentPageTitle = currentPageTitle(),
            activeWebView = activeWebView(),
            launchArchiveExport = activityResultLaunchers::launchPageArchiveExport
        )
        val pagePrintController = PagePrintController(
            activity = activity,
            currentActionableUrl = browserUrlStateController::currentActionableUrl,
            currentPageTitle = currentPageTitle(),
            activeWebView = activeWebView()
        )
        val findInPageDialogController = FindInPageDialogController(
            activity = activity,
            findInPageController = findInPageController,
            setFindResultListener = { listener ->
                browserStandardWebViewHostController.currentBrowserManager()
                    .setFindResultListener(listener)
            },
            closeFunctionCenter = { closeFunctionCenter() },
            dp = dp
        )
        val browserPageToolEntryController = BrowserPageToolEntryController(
            findInPageDialogController = findInPageDialogController,
            pageArchiveController = pageArchiveController,
            pagePrintController = pagePrintController,
            loadUrl = browserNavigationController::loadUrl,
            openNativePlayer = { url, title ->
                nativePlayerEntryController.openNativePlayer(
                    url = url,
                    titleOverride = title
                )
            }
        )
        return BrowserPageActionComponents(
            downloadController = downloadController,
            pageActionsController = pageActionsController,
            httpAuthController = httpAuthController,
            clientCertificateController = clientCertificateController,
            pageArchiveController = pageArchiveController,
            pagePrintController = pagePrintController,
            findInPageDialogController = findInPageDialogController,
            browserPageToolEntryController = browserPageToolEntryController
        )
    }

    /**
     * 返回当前页面标题读取回调。
     *
     * @return 返回 `() -> String`，调用时从当前会话控制器读取页面标题。
     */
    private fun currentPageTitle(): () -> String {
        return {
            browserSessionStateController.currentSessionController().currentPageTitle
        }
    }

    /**
     * 返回当前 active WebView 读取回调。
     *
     * @return 返回 `() -> WebView`，调用时从当前 BrowserManager 读取正在显示的 WebView。
     */
    private fun activeWebView(): () -> WebView {
        return {
            browserStandardWebViewHostController.currentBrowserManager().activeWebView
        }
    }
}
