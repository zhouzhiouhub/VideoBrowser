package com.example.videobrowser.video

import android.content.Context
import android.content.Intent

internal object PlayerIntentFactory {
    fun create(
        context: Context,
        mediaUri: String,
        title: String?,
        mimeType: String?,
        userAgent: String?,
        cookie: String?,
        referer: String?,
        privateBrowsing: Boolean = false,
        subtitleCandidates: List<ExternalSubtitleCandidate> = emptyList(),
        playbackQueue: PlaybackQueue? = null
    ): Intent {
        return Intent(context, PlayerActivity::class.java).apply {
            putExtra(PlayerIntentExtras.MEDIA_URI, mediaUri)
            putExtra(PlayerIntentExtras.MEDIA_TITLE, title)
            putExtra(PlayerIntentExtras.MIME_TYPE, mimeType)
            putExtra(PlayerIntentExtras.USER_AGENT, userAgent)
            putExtra(PlayerIntentExtras.COOKIE, cookie)
            putExtra(PlayerIntentExtras.REFERER, referer)
            putExtra(PlayerIntentExtras.PRIVATE_BROWSING, privateBrowsing)
            putSubtitleCandidates(subtitleCandidates)
            playbackQueue?.let { queue ->
                putExtra(PlayerIntentExtras.PLAYBACK_QUEUE, PlaybackQueueJsonCodec.encode(queue))
            }
        }
    }

    private fun Intent.putSubtitleCandidates(
        subtitleCandidates: List<ExternalSubtitleCandidate>
    ) {
        if (subtitleCandidates.isEmpty()) {
            return
        }

        putStringArrayListExtra(
            PlayerIntentExtras.SUBTITLE_URIS,
            ArrayList(subtitleCandidates.map { candidate -> candidate.uri })
        )
        putStringArrayListExtra(
            PlayerIntentExtras.SUBTITLE_LABELS,
            ArrayList(subtitleCandidates.map { candidate -> candidate.label.orEmpty() })
        )
        putStringArrayListExtra(
            PlayerIntentExtras.SUBTITLE_MIME_TYPES,
            ArrayList(subtitleCandidates.map { candidate -> candidate.mimeType.orEmpty() })
        )
        putStringArrayListExtra(
            PlayerIntentExtras.SUBTITLE_LANGUAGES,
            ArrayList(subtitleCandidates.map { candidate -> candidate.language.orEmpty() })
        )
    }
}
