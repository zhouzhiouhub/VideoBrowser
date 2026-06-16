package com.example.videobrowser.video

/**
 * 初学者阅读提示：
 * 这个文件属于“视频播放模块”。
 * 文件名 PlayableMediaItem 可以拆开理解为“Playable Media Item”，表示它只负责视频链路中的一个小职责。
 * 主要职责：连接网页视频手势、原生 ExoPlayer 播放、播放队列、字幕、播放历史或媒体路由。
 * 阅读顺序：先看数据模型表达什么播放状态，再看控制器如何响应用户手势和播放器回调。
 */
data class PlayableMediaItem(
    val uri: String,
    val title: String? = null,
    val mimeType: String? = null,
    val source: PlayableMediaSource,
    val userAgent: String? = null,
    val referer: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val subtitleCandidates: List<ExternalSubtitleCandidate> = emptyList()
)

data class ExternalSubtitleCandidate(
    val uri: String,
    val label: String? = null,
    val mimeType: String? = null,
    val language: String? = null
)

enum class PlayableMediaSource {
    REMOTE_URL,
    LOCAL_DOCUMENT
}
