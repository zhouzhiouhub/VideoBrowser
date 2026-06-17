package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器 ChromeClient 装配模块”。
 * ChromeClient 是 WebView 的浏览器外壳回调适配层；本控制器负责创建标准/无痕两套 ChromeClient，
 * 并把文件选择、网页权限、定位权限和网页新窗口回调交给对应的小控制器。
 * 阅读顺序：先看 setupChromeClient，再看 createChromeClient，最后看 cancelPending* 这些生命周期清理函数。
 */
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

/**
 * Browser ChromeClient 控制器。
 *
 * MainActivity 只需要询问当前 ChromeClient 或触发初始化；具体 ChromeClient 的创建、
 * 当前模式选择和 WebChromeClient 回调转发都集中在这里。
 *
 * @param activity 当前 Activity，用于创建 ChromeClient 并处理网页弹窗上下文。
 * @param fullscreenContainer WebView 自定义全屏视图的容器，网页视频进入全屏时会使用它。
 * @param decorView Activity 顶层装饰视图，ChromeClient 通过它设置系统栏全屏标记。
 * @param standardSessionController 普通浏览模式会话控制器，用于接收页面进度和标题。
 * @param privateSessionController 无痕浏览模式会话控制器，用于接收无痕页面进度和标题。
 * @param browserManager 返回当前 BrowserManager 的函数，用来把当前 ChromeClient 绑定到 active WebView。
 * @param isPrivateBrowsingActive 返回当前是否处于无痕浏览模式的函数，用来选择标准或无痕 ChromeClient。
 * @param fullscreenChanged 网页全屏状态变化回调，调用方负责同步工具栏、页面进度和屏幕方向。
 * @param webFileChooserController 网页文件上传选择器控制器，负责启动系统选择器并回传结果。
 * @param webPermissionRequestController 网页相机/麦克风权限控制器，负责站点权限和运行时权限。
 * @param geolocationPermissionController 网页定位权限控制器，负责站点权限、运行时权限和 prompt 生命周期。
 * @param webWindowController 网页新窗口控制器，负责把用户手势触发的新窗口接入标准标签页。
 */
class BrowserChromeClientController(
    private val activity: AppCompatActivity,
    private val fullscreenContainer: FrameLayout,
    private val decorView: View,
    private val standardSessionController: BrowserSessionController,
    private val privateSessionController: BrowserSessionController,
    private val browserManager: () -> BrowserManager,
    private val isPrivateBrowsingActive: () -> Boolean,
    private val fullscreenChanged: (Boolean) -> Unit,
    private val webFileChooserController: WebFileChooserController,
    private val webPermissionRequestController: WebPermissionRequestController,
    private val geolocationPermissionController: GeolocationPermissionController,
    private val webWindowController: WebWindowController
) {
    private var standardChromeClient: ChromeClient? = null
    private var privateChromeClient: ChromeClient? = null

    /**
     * 创建标准/无痕两套 ChromeClient，并把当前模式对应的实例绑定到 active WebView。
     */
    fun setupChromeClient() {
        standardChromeClient = createChromeClient(standardSessionController)
        privateChromeClient = createChromeClient(privateSessionController)
        syncCurrentChromeClient()
    }

    /**
     * 返回当前浏览模式下应该使用的 ChromeClient。
     *
     * @return 返回标准或无痕 ChromeClient；调用前必须先执行 setupChromeClient。
     */
    fun currentChromeClient(): ChromeClient {
        val selectedClient = if (isPrivateBrowsingActive()) {
            privateChromeClient
        } else {
            standardChromeClient
        }
        return checkNotNull(selectedClient) {
            "ChromeClient has not been initialized. Call setupChromeClient() first."
        }
    }

    /**
     * 判断标准和无痕 ChromeClient 是否都已经创建。
     *
     * @return true 表示 currentChromeClient 可以安全调用。
     */
    fun areChromeClientsInitialized(): Boolean {
        return standardChromeClient != null && privateChromeClient != null
    }

    /**
     * 把当前模式对应的 ChromeClient 重新绑定到 active WebView。
     *
     * 切换普通/无痕 WebView 后需要调用本函数，让新 active WebView 继续接收网页外壳回调。
     */
    fun syncCurrentChromeClient() {
        if (!areChromeClientsInitialized()) {
            return
        }
        browserManager().setChromeClient(currentChromeClient())
    }

    /**
     * 取消正在等待系统文件选择器结果的网页文件上传请求。
     */
    fun cancelPendingWebFileChooser() {
        webFileChooserController.cancelPending()
    }

    /**
     * 取消正在等待用户处理或系统运行时权限结果的网页权限请求。
     */
    fun cancelPendingWebPermissionRequest() {
        webPermissionRequestController.cancelPendingRequest()
    }

    /**
     * 取消正在等待用户处理或系统定位权限结果的网页定位请求。
     */
    fun cancelPendingGeolocationPermissionPrompt() {
        geolocationPermissionController.cancelPending()
    }

    /**
     * 创建指定会话控制器对应的 ChromeClient。
     *
     * @param sessionController 接收页面加载进度和标题的会话控制器。
     * @return 返回已经接好各类 WebChromeClient 回调的 ChromeClient。
     */
    private fun createChromeClient(sessionController: BrowserSessionController): ChromeClient {
        return ChromeClient(
            activity = activity,
            fullscreenContainer = fullscreenContainer,
            decorView = decorView,
            progressChanged = sessionController::handlePageProgressChanged,
            titleReceived = sessionController::handlePageTitleReceived,
            fullscreenChanged = fullscreenChanged,
            fileChooserRequested = webFileChooserController::showFileChooser,
            permissionRequested = webPermissionRequestController::handlePermissionRequest,
            permissionRequestCanceled = webPermissionRequestController::handlePermissionRequestCanceled,
            geolocationPermissionRequested = geolocationPermissionController::handlePermissionRequest,
            geolocationPermissionHidden = geolocationPermissionController::handlePermissionHidden,
            newWindowRequested = webWindowController::handleCreateWebWindow,
            windowClosed = webWindowController::handleCloseWebWindow
        )
    }
}
