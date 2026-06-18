package com.example.videobrowser.video

import android.content.Intent

internal class PlayerIntentReader(
    private val intent: Intent
) {
    fun mediaUri(): String {
        return intent.getStringExtra(PlayerIntentExtras.MEDIA_URI).orEmpty()
    }

    fun mediaTitle(): String? {
        return intent.getStringExtra(PlayerIntentExtras.MEDIA_TITLE)
    }

    fun mimeType(): String? {
        return intent.getStringExtra(PlayerIntentExtras.MIME_TYPE)
    }

    fun userAgent(): String? {
        return intent.getStringExtra(PlayerIntentExtras.USER_AGENT)
    }

    fun isPrivateBrowsing(): Boolean {
        return intent.getBooleanExtra(PlayerIntentExtras.PRIVATE_BROWSING, false)
    }

    fun requestHeaders(): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        intent.getStringExtra(PlayerIntentExtras.COOKIE)
            ?.takeIf { it.isNotBlank() }
            ?.let { headers["Cookie"] = it }
        intent.getStringExtra(PlayerIntentExtras.REFERER)
            ?.takeIf { it.isNotBlank() }
            ?.let { headers["Referer"] = it }
        return headers
    }

    fun playbackQueue(): PlaybackQueue {
        val encodedQueue = intent.getStringExtra(PlayerIntentExtras.PLAYBACK_QUEUE)
        return encodedQueue?.let(PlaybackQueueJsonCodec::decode)
            ?: PlaybackQueue.single(currentPlayableMediaItem())
    }

    private fun currentPlayableMediaItem(): PlayableMediaItem {
        val uri = mediaUri()
        return PlayableMediaItem(
            uri = uri,
            title = mediaTitle(),
            mimeType = mimeType(),
            source = if (isLocalMediaUri(uri)) {
                PlayableMediaSource.LOCAL_DOCUMENT
            } else {
                PlayableMediaSource.REMOTE_URL
            },
            userAgent = userAgent(),
            referer = intent.getStringExtra(PlayerIntentExtras.REFERER),
            headers = requestHeaders(),
            subtitleCandidates = subtitleCandidates()
        )
    }

    private fun subtitleCandidates(): List<ExternalSubtitleCandidate> {
        val uris = intent.getStringArrayListExtra(PlayerIntentExtras.SUBTITLE_URIS).orEmpty()
        val labels = intent.getStringArrayListExtra(PlayerIntentExtras.SUBTITLE_LABELS).orEmpty()
        val mimeTypes = intent.getStringArrayListExtra(PlayerIntentExtras.SUBTITLE_MIME_TYPES).orEmpty()
        val languages = intent.getStringArrayListExtra(PlayerIntentExtras.SUBTITLE_LANGUAGES).orEmpty()

        return uris.mapIndexedNotNull { index, uri ->
            val normalizedUri = uri.takeIf { it.isNotBlank() } ?: return@mapIndexedNotNull null
            ExternalSubtitleCandidate(
                uri = normalizedUri,
                label = labels.getOrNull(index)?.takeIf { it.isNotBlank() },
                mimeType = mimeTypes.getOrNull(index)?.takeIf { it.isNotBlank() },
                language = languages.getOrNull(index)?.takeIf { it.isNotBlank() }
            )
        }
    }

    private fun isLocalMediaUri(uri: String): Boolean {
        return uri.startsWith("content:", ignoreCase = true) ||
            uri.startsWith("file:", ignoreCase = true)
    }
}
