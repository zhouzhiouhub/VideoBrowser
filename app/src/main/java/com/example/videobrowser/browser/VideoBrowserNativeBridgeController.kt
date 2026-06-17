package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 VideoBrowserNativeBridgeController 可以拆开理解为“Video Browser Native Bridge Controller”，
 * 表示它只负责把网页 JavaScript 回调连接到 Android 侧业务对象。
 * 主要职责：创建 VideoBrowserNativeBridge，并把全屏、元素选择、视频时间线和日志回调转给对应模块。
 * 阅读顺序：先看构造参数知道它依赖谁，再看 createNativeBridge() 了解每个 JS 回调会转到哪里。
 */
import android.util.Log
import com.example.videobrowser.video.FullscreenVideoController
import com.example.videobrowser.video.WebPlaybackHistoryRecorder

/**
 * 网页原生桥控制器。
 *
 * WebView 只认识一个通过 addJavascriptInterface 注入的对象；这个控制器负责创建该对象，
 * 并把桥里的每个回调分发给 MainActivity 已经初始化好的业务控制器。
 *
 * @param postToUi 把桥回调切回主线程执行的函数；参数是最终要在 UI 线程运行的动作。
 * @param currentChromeClient 返回当前 WebView 对应 ChromeClient 的函数；尚未初始化时可以返回 null。
 * @param fullscreenVideoController 处理网页视频全屏手势层和时间线展示的控制器。
 * @param webPlaybackHistoryRecorder 记录网页视频播放进度到播放历史的对象。
 * @param requestElementBlock 处理网页请求选择并屏蔽元素的函数，参数分别是 CSS 选择器和描述文本。
 * @param blockSelectedElement 直接屏蔽网页已选元素的函数，参数是 CSS 选择器。
 * @param cancelElementPicker 取消当前元素选择流程的函数。
 */
class VideoBrowserNativeBridgeController(
    private val postToUi: ((() -> Unit) -> Unit),
    private val currentChromeClient: () -> ChromeClient?,
    private val fullscreenVideoController: FullscreenVideoController,
    private val webPlaybackHistoryRecorder: WebPlaybackHistoryRecorder,
    private val requestElementBlock: (String, String) -> Unit,
    private val blockSelectedElement: (String) -> Unit,
    private val cancelElementPicker: () -> Unit
) {
    /**
     * 创建注入到 WebView 的原生桥对象。
     *
     * @return 返回配置好所有回调的 VideoBrowserNativeBridge，调用方会通过 addJavascriptInterface 注入。
     */
    fun createNativeBridge(): VideoBrowserNativeBridge {
        return VideoBrowserNativeBridge(
            postToUi = postToUi,
            enterFullscreen = ::enterFullscreen,
            exitFullscreen = ::exitFullscreen,
            updatePlaybackTimeline = ::updateWebViewPlaybackTimeline,
            requestElementBlock = requestElementBlock,
            blockSelectedElement = blockSelectedElement,
            cancelElementPicker = cancelElementPicker,
            logVideoEvent = ::logVideoEvent
        )
    }

    /**
     * 进入网页声明的页面全屏状态。
     *
     * @return 无返回值；ChromeClient 尚未初始化时不做任何操作。
     */
    private fun enterFullscreen() {
        currentChromeClient()?.enterPageFullscreen()
    }

    /**
     * 退出网页声明的页面全屏状态。
     *
     * @return 无返回值；ChromeClient 尚未初始化时不做任何操作。
     */
    private fun exitFullscreen() {
        currentChromeClient()?.exitPageFullscreen()
    }

    /**
     * 更新网页视频播放时间线并记录播放历史。
     *
     * @param positionMs 当前播放位置，单位毫秒。
     * @param durationMs 当前视频总时长，单位毫秒。
     * @return 无返回值；函数会同步更新全屏手势层状态并按节流策略写播放历史。
     */
    private fun updateWebViewPlaybackTimeline(positionMs: Double, durationMs: Double) {
        fullscreenVideoController.updatePlaybackTimeline(positionMs, durationMs)
        webPlaybackHistoryRecorder.record(positionMs, durationMs)
    }

    /**
     * 写入网页视频调试日志。
     *
     * @param message 网页侧上报的日志文本，VideoBrowserNativeBridge 已经做过长度和空白清理。
     * @return 无返回值；日志只进入 Android Logcat，不影响用户界面。
     */
    private fun logVideoEvent(message: String) {
        Log.d(VIDEO_LOG_TAG, message)
    }

    private companion object {
        private const val VIDEO_LOG_TAG = "VideoBrowserVideo"
    }
}
