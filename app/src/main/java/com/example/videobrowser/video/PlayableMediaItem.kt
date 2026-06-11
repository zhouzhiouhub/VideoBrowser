package com.example.videobrowser.video

data class PlayableMediaItem(
    val uri: String,
    val title: String? = null,
    val mimeType: String? = null,
    val source: PlayableMediaSource,
    val userAgent: String? = null,
    val referer: String? = null,
    val headers: Map<String, String> = emptyMap()
)

enum class PlayableMediaSource {
    REMOTE_URL,
    LOCAL_DOCUMENT
}
