package com.example.videobrowser.video

/**
 * 初学者阅读提示：
 * 这个文件属于“视频播放模块”。
 * 文件名 NativePlayerEntryController 可以拆开理解为“Native Player Entry Controller”，
 * 表示它只负责把浏览器、下载、本地文件等入口收敛到原生播放器。
 * 阅读顺序：先看构造参数知道它依赖谁，再看两个 openNativePlayer 重载如何分别处理直接 URL 和媒体路由决策。
 */
import com.example.videobrowser.browser.BrowserExternalNavigator

/**
 * 原生播放器入口控制器。
 *
 * MainActivity 只保留 Activity 生命周期和模块装配；播放器打开逻辑集中在这里，
 * 这样下载、页面工具、播放历史和地址栏路由都能复用同一套私密浏览参数传递规则。
 *
 * @param externalNavigator 浏览器跳出 WebView 的出口，真正负责创建并启动 PlayerActivity。
 * @param isPrivateBrowsingEnabled 读取当前是否处于私密浏览模式的函数，打开播放器时会把这个状态写入 Intent。
 */
class NativePlayerEntryController(
    private val externalNavigator: BrowserExternalNavigator,
    private val isPrivateBrowsingEnabled: () -> Boolean
) {
    /**
     * 打开原生播放器并播放给定媒体地址。
     *
     * @param url 要播放的媒体地址，可以是远程 URL，也可以是 content:// 本地文档地址。
     * @param mimeType 调用方已知的媒体 MIME 类型；为空时播放器会根据地址或系统能力继续推断。
     * @param userAgentOverride 调用方指定的 User-Agent；为空时由当前浏览器 WebView 的 User-Agent 补齐。
     * @param titleOverride 调用方指定的展示标题；为空时由当前页面标题或文件名生成。
     * @param subtitleCandidates 本地文件夹或播放队列中匹配到的外挂字幕候选列表。
     * @param playbackQueue 连续播放队列；为空时只播放当前媒体。
     */
    fun openNativePlayer(
        url: String,
        mimeType: String? = null,
        userAgentOverride: String? = null,
        titleOverride: String? = null,
        subtitleCandidates: List<ExternalSubtitleCandidate> = emptyList(),
        playbackQueue: PlaybackQueue? = null
    ) {
        externalNavigator.openNativePlayer(
            url = url,
            mimeType = mimeType,
            userAgentOverride = userAgentOverride,
            titleOverride = titleOverride,
            privateBrowsing = isPrivateBrowsingEnabled(),
            subtitleCandidates = subtitleCandidates,
            playbackQueue = playbackQueue
        )
    }

    /**
     * 根据媒体路由决策打开原生播放器。
     *
     * @param decision 媒体路由器给出的处理结果；如果其中没有可播放媒体，本函数会直接返回。
     */
    fun openNativePlayer(decision: MediaRouteDecision) {
        val mediaItem = decision.mediaItem ?: return
        openNativePlayer(
            mediaItem.uri,
            mediaItem.mimeType,
            mediaItem.userAgent,
            mediaItem.title,
            mediaItem.subtitleCandidates,
            null
        )
    }
}
