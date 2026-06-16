package com.example.videobrowser.video

/**
 * 初学者阅读提示：
 * 这个文件属于“视频播放模块”。
 * 文件名 MediaRoutingController 可以拆开理解为“Media Routing Controller”，表示它只负责视频链路中的一个小职责。
 * 主要职责：连接网页视频手势、原生 ExoPlayer 播放、播放队列、字幕、播放历史或媒体路由。
 * 阅读顺序：先看数据模型表达什么播放状态，再看控制器如何响应用户手势和播放器回调。
 */
import com.example.videobrowser.utils.MediaUrlUtils

/**
 * 媒体路由决策器。
 *
 * 同一个 URL 可能来自地址栏、WebView 跳转、下载回调或本地文件。
 * 这里根据来源和 URL 类型决定：交给原生播放器、继续用 WebView 加载、下载、交给系统或直接阻止。
 */
object MediaRoutingController {
    /**
     * 函数 `route`：封装 `route` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param request 参数类型为 `MediaRouteRequest`，表示一次请求或响应，函数会检查它的内容并决定如何继续处理。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun route(request: MediaRouteRequest): MediaRouteDecision {
        // route 是纯函数，方便测试；它只返回决策，不直接打开页面或播放器。
        val url = request.url.trim()
        if (url.isEmpty()) {
            return MediaRouteDecision(MediaRouteAction.BLOCK)
        }

        val playable = MediaUrlUtils.isPlayableMediaUri(url, request.mimeType)
        return when (request.source) {
            MediaRouteSource.ADDRESS_BAR -> {
                if (playable) {
                    MediaRouteDecision(
                        action = MediaRouteAction.OPEN_NATIVE_PLAYER,
                        mediaItem = request.toPlayableMediaItem()
                    )
                } else {
                    MediaRouteDecision(
                        action = if (isWebUrl(url)) {
                            MediaRouteAction.LOAD_IN_WEBVIEW
                        } else {
                            MediaRouteAction.BLOCK
                        }
                    )
                }
            }

            MediaRouteSource.WEBVIEW_OVERRIDE -> {
                when {
                    playable -> MediaRouteDecision(
                        action = MediaRouteAction.OPEN_NATIVE_PLAYER,
                        mediaItem = request.toPlayableMediaItem()
                    )

                    isWebUrl(url) -> MediaRouteDecision(MediaRouteAction.LOAD_IN_WEBVIEW)
                    else -> MediaRouteDecision(MediaRouteAction.BLOCK)
                }
            }

            MediaRouteSource.DOWNLOAD -> {
                if (playable) {
                    MediaRouteDecision(
                        action = MediaRouteAction.OPEN_NATIVE_PLAYER,
                        mediaItem = request.toPlayableMediaItem()
                    )
                } else {
                    MediaRouteDecision(MediaRouteAction.DOWNLOAD)
                }
            }

            MediaRouteSource.LOCAL_DOCUMENT -> {
                if (playable) {
                    MediaRouteDecision(
                        action = MediaRouteAction.OPEN_NATIVE_PLAYER,
                        mediaItem = request.toPlayableMediaItem()
                    )
                } else {
                    MediaRouteDecision(MediaRouteAction.OPEN_EXTERNAL_APP)
                }
            }
        }
    }

    /**
     * 函数 `toPlayableMediaItem`：封装 `to Playable Media Item` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun MediaRouteRequest.toPlayableMediaItem(): PlayableMediaItem {
        // 把路由请求转换成播放器需要的媒体项，同时尽量带上标题、类型、UA 和来源页。
        return PlayableMediaItem(
            uri = url.trim(),
            title = displayName?.takeIf { it.isNotBlank() }
                ?: currentPageTitle?.takeIf { it.isNotBlank() },
            mimeType = mimeType?.takeIf { it.isNotBlank() },
            source = if (source == MediaRouteSource.LOCAL_DOCUMENT) {
                PlayableMediaSource.LOCAL_DOCUMENT
            } else {
                PlayableMediaSource.REMOTE_URL
            },
            userAgent = userAgent?.takeIf { it.isNotBlank() },
            referer = currentPageUrl?.takeIf {
                it.isNotBlank() && !it.equals(url, ignoreCase = true) && isWebUrl(it)
            }
        )
    }

    /**
     * 函数 `isWebUrl`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun isWebUrl(url: String): Boolean {
        val scheme = url.substringBefore(':', missingDelimiterValue = "")
        return scheme.equals("http", ignoreCase = true) ||
            scheme.equals("https", ignoreCase = true) ||
            scheme.equals("about", ignoreCase = true)
    }
}

data class MediaRouteRequest(
    val source: MediaRouteSource,
    val url: String,
    val mimeType: String? = null,
    val displayName: String? = null,
    val currentPageUrl: String? = null,
    val currentPageTitle: String? = null,
    val userAgent: String? = null
)

data class MediaRouteDecision(
    val action: MediaRouteAction,
    val mediaItem: PlayableMediaItem? = null
)

enum class MediaRouteSource {
    ADDRESS_BAR,
    WEBVIEW_OVERRIDE,
    DOWNLOAD,
    LOCAL_DOCUMENT
}

enum class MediaRouteAction {
    OPEN_NATIVE_PLAYER,
    LOAD_IN_WEBVIEW,
    DOWNLOAD,
    OPEN_EXTERNAL_APP,
    BLOCK
}
