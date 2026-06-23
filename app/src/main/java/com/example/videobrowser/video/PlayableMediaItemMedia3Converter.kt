package com.example.videobrowser.video

import androidx.media3.common.MediaItem
import com.example.videobrowser.utils.AndroidUriParser
import com.example.videobrowser.utils.MimeTypeNormalizer

internal object PlayableMediaItemMedia3Converter {
    fun toMediaItem(item: PlayableMediaItem): MediaItem {
        return MediaItem.Builder()
            .setUri(AndroidUriParser.parse(item.uri))
            .setMimeType(normalizedMimeType(item.mimeType))
            .setSubtitleConfigurations(
                item.subtitleCandidates.map(::toSubtitleConfiguration)
            )
            .build()
    }

    private fun toSubtitleConfiguration(
        candidate: ExternalSubtitleCandidate
    ): MediaItem.SubtitleConfiguration {
        val builder = MediaItem.SubtitleConfiguration.Builder(AndroidUriParser.parse(candidate.uri))
        candidate.mimeType?.takeIf { it.isNotBlank() }?.let(builder::setMimeType)
        candidate.language?.takeIf { it.isNotBlank() }?.let(builder::setLanguage)
        candidate.label?.takeIf { it.isNotBlank() }?.let(builder::setLabel)
        return builder.build()
    }

    private fun normalizedMimeType(mimeType: String?): String? {
        return when (MimeTypeNormalizer.normalize(mimeType)) {
            "application/vnd.apple.mpegurl",
            "application/x-mpegurl",
            "audio/mpegurl",
            "audio/x-mpegurl" -> MIME_HLS
            "application/dash+xml" -> MIME_DASH
            "application/vnd.ms-sstr+xml" -> MIME_SMOOTH_STREAMING
            null,
            "" -> null
            else -> MimeTypeNormalizer.withoutParameters(mimeType)
        }
    }

    private const val MIME_HLS = "application/x-mpegURL"
    private const val MIME_DASH = "application/dash+xml"
    private const val MIME_SMOOTH_STREAMING = "application/vnd.ms-sstr+xml"
}
