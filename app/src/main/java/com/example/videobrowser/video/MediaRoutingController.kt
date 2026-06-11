package com.example.videobrowser.video

import com.example.videobrowser.utils.MediaUrlUtils

object MediaRoutingController {
    fun route(request: MediaRouteRequest): MediaRouteDecision {
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

    private fun MediaRouteRequest.toPlayableMediaItem(): PlayableMediaItem {
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
