package com.example.videobrowser.browser

import com.example.videobrowser.video.FullscreenVideoController

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器运行时状态模块”。
 * 文件名 BrowserRuntimeStateController 可以拆开理解为“Browser Runtime State Controller”，
 * 表示它只负责保存或读取运行期间会变化、但不应该散落在 MainActivity 字段里的状态。
 * 阅读顺序：先看构造参数了解它如何延迟读取当前会话和全屏控制器，再看 privateBrowsing/defaultUserAgent 这两组状态。
 */

/**
 * 浏览器运行时状态控制器。
 *
 * MainActivity 仍然负责接收 Android 生命周期和按键事件；本类负责把“当前是否无痕、首页是否可见、
 * 视频全屏 UI 是否激活、默认 User-Agent 是什么”这些运行时状态集中管理。
 *
 * @param currentSessionController 参数类型为 `() -> BrowserSessionController`，表示读取当前浏览模式会话控制器的回调。
 * @param fullscreenVideoController 参数类型为 `() -> FullscreenVideoController?`，表示安全读取视频全屏控制器的回调。
 */
class BrowserRuntimeStateController(
    private val currentSessionController: () -> BrowserSessionController,
    private val fullscreenVideoController: () -> FullscreenVideoController?
) {
    private var privateBrowsingActive = false
    private var defaultUserAgent: String? = null

    /**
     * 返回当前是否处于无痕浏览模式。
     *
     * @return true 表示当前应该使用无痕会话和无痕 UI。
     */
    fun isPrivateBrowsingActive(): Boolean {
        return privateBrowsingActive
    }

    /**
     * 更新当前无痕浏览状态。
     *
     * @param active 参数类型为 `Boolean`，true 表示切换到无痕浏览模式，false 表示切回标准浏览模式。
     */
    fun setPrivateBrowsingActive(active: Boolean) {
        privateBrowsingActive = active
    }

    /**
     * 返回当前会话是否显示首页内容。
     *
     * @return true 表示当前页面仍在首页内容状态，false 表示已经显示普通网页或本地内容。
     */
    fun isHomePageVisible(): Boolean {
        return currentSessionController().isHomePageVisible
    }

    /**
     * 返回视频全屏 UI 是否激活。
     *
     * @return true 表示视频全屏控制器已经初始化且处于全屏 UI 状态。
     */
    fun isVideoFullscreenUiActive(): Boolean {
        return fullscreenVideoController()?.isFullscreenUiActive == true
    }

    /**
     * 返回启动时记录的默认 User-Agent。
     *
     * @return 默认 User-Agent；尚未由启动流程写入时返回 null。
     */
    fun defaultUserAgent(): String? {
        return defaultUserAgent
    }

    /**
     * 保存启动流程读取到的默认 User-Agent。
     *
     * @param userAgent 参数类型为 `String?`，表示 BrowserManager 启动时读取到的默认 User-Agent。
     */
    fun setDefaultUserAgent(userAgent: String?) {
        defaultUserAgent = userAgent
    }

    /**
     * 如果视频全屏 UI 正在显示，则唤醒全屏控制层。
     *
     * @return true 表示本次确实唤醒了全屏控制层，false 表示当前没有可唤醒的全屏 UI。
     */
    fun wakeVideoFullscreenControlsIfActive(): Boolean {
        val fullscreenController = fullscreenVideoController() ?: return false
        if (!fullscreenController.isFullscreenUiActive) {
            return false
        }
        fullscreenController.wakeControls()
        return true
    }
}
