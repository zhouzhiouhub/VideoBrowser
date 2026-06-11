package com.example.videobrowser.video

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
