package com.example.videobrowser.localfiles

/**
 * 初学者阅读提示：
 * 这个文件属于“本地文件装配模块”。
 * 文件名 LocalFileAssemblyController 可以拆开理解为“Local File Assembly Controller”，
 * 表示它只负责创建本地文件入口和本地文档打开入口这两个互相回调的控制器。
 * 阅读顺序：先看 LocalFileComponents 知道返回哪些对象，再看 create() 里两个控制器如何连接。
 */
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.browser.BrowserSessionController
import com.example.videobrowser.browser.PageActionsController
import com.example.videobrowser.functioncenter.FunctionCenterController
import com.example.videobrowser.storage.PreferenceStore

/**
 * 本地文件组件集合。
 *
 * @param localFilesController 参数类型为 `LocalFilesController`，表示负责目录授权、文件列表和文件选择入口的控制器。
 * @param localDocumentEntryController 参数类型为 `LocalDocumentEntryController`，表示负责把本地文档交给浏览器或播放器打开的控制器。
 */
data class LocalFileComponents(
    val localFilesController: LocalFilesController,
    val localDocumentEntryController: LocalDocumentEntryController
)

/**
 * 本地文件装配控制器。
 *
 * LocalFilesController 选中文档后需要回调 LocalDocumentEntryController，而后者构造时又需要前者。
 * 本类把这个双向连接留在 localfiles 包内，MainActivity 只接收装配好的两个控制器。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示注册本地文件选择器和显示 UI 的宿主 Activity。
 * @param preferenceStore 参数类型为 `PreferenceStore`，表示保存本地目录授权等配置的键值存储。
 * @param functionCenter 参数类型为 `FunctionCenterController`，表示本地文件页所在的功能中心容器控制器。
 * @param logTag 参数类型为 `String`，表示本地文件访问失败时写入日志使用的标签。
 * @param showMainFunctionCenterPage 参数类型为 `() -> Unit`，表示从本地文件页面返回功能中心根页的回调。
 * @param pageActionsController 参数类型为 `() -> PageActionsController`，表示延迟返回当前页面动作控制器的函数。
 * @param closeFunctionCenter 参数类型为 `() -> Boolean`，表示关闭功能中心面板的回调。
 * @param currentSessionController 参数类型为 `() -> BrowserSessionController`，表示返回当前浏览模式页面会话控制器的函数。
 * @param currentBrowserManager 参数类型为 `() -> BrowserManager`，表示返回当前浏览模式 BrowserManager 的函数。
 * @param updateAddressBar 参数类型为 `(String?) -> Unit`，表示本地文档加载后同步地址栏文本的回调，参数为空时清空地址栏。
 * @param hideKeyboard 参数类型为 `() -> Unit`，表示打开本地文档前隐藏软键盘的回调。
 * @param showHomeContent 参数类型为 `(Boolean) -> Unit`，表示控制首页内容显示状态的回调，参数 true 表示显示首页。
 */
class LocalFileAssemblyController(
    private val activity: AppCompatActivity,
    private val preferenceStore: PreferenceStore,
    private val functionCenter: FunctionCenterController,
    private val logTag: String,
    private val showMainFunctionCenterPage: () -> Unit,
    private val pageActionsController: () -> PageActionsController,
    private val closeFunctionCenter: () -> Boolean,
    private val currentSessionController: () -> BrowserSessionController,
    private val currentBrowserManager: () -> BrowserManager,
    private val updateAddressBar: (String?) -> Unit,
    private val hideKeyboard: () -> Unit,
    private val showHomeContent: (Boolean) -> Unit
) {
    /**
     * 创建本地文件控制器集合。
     *
     * @return 返回 `LocalFileComponents`，其中两个控制器已经完成互相回调连接。
     */
    fun create(): LocalFileComponents {
        lateinit var localDocumentEntryController: LocalDocumentEntryController
        val localFilesController = LocalFilesController(
            activity = activity,
            preferenceStore = preferenceStore,
            functionCenter = functionCenter,
            logTag = logTag,
            showMainFunctionCenterPage = showMainFunctionCenterPage,
            onOpenDocumentUri = { uri, displayName, mimeType, subtitleCandidates, playbackQueue ->
                localDocumentEntryController.openLocalDocumentUri(
                    uri,
                    displayName,
                    mimeType,
                    subtitleCandidates,
                    playbackQueue
                )
            }
        )
        localDocumentEntryController = LocalDocumentEntryController(
            localFilesController = localFilesController,
            pageActionsController = pageActionsController,
            closeFunctionCenter = closeFunctionCenter,
            currentSessionController = currentSessionController,
            currentBrowserManager = currentBrowserManager,
            updateAddressBar = updateAddressBar,
            hideKeyboard = hideKeyboard,
            showHomeContent = showHomeContent
        )
        return LocalFileComponents(
            localFilesController = localFilesController,
            localDocumentEntryController = localDocumentEntryController
        )
    }
}
