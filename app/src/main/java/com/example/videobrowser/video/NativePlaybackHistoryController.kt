package com.example.videobrowser.video

import com.example.videobrowser.utils.PlaybackSpeedNormalizer

internal class NativePlaybackHistoryController(
    private val alwaysStartVideosFromBeginning: () -> Boolean,
    private val progressFor: (String) -> PlaybackProgress?,
    private val resumePositionFor: (String) -> Long?,
    private val saveProgress: (PlaybackProgress, Boolean) -> Unit,
    private val privateBrowsing: () -> Boolean,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis,
    private val speedNormalizer: (Float) -> Float = PlaybackSpeedNormalizer::normalize
) {
    fun restore(mediaIdentity: String): NativePlaybackHistoryRestore {
        if (alwaysStartVideosFromBeginning()) {
            return NativePlaybackHistoryRestore(positionMs = 0L)
        }

        val progress = progressFor(mediaIdentity)
        return NativePlaybackHistoryRestore(
            positionMs = resumePositionFor(mediaIdentity),
            playbackSpeed = progress?.speed?.let(speedNormalizer)
        )
    }

    fun save(snapshot: NativePlaybackHistorySnapshot) {
        val mediaIdentity = snapshot.mediaIdentity.trim()
        if (mediaIdentity.isBlank()) {
            return
        }

        saveProgress(
            PlaybackProgress(
                mediaIdentity = mediaIdentity,
                positionMs = snapshot.positionMs.coerceAtLeast(0L),
                durationMs = snapshot.durationMs.coerceAtLeast(0L),
                speed = snapshot.speed,
                updatedAtMillis = currentTimeMillis(),
                title = snapshot.title,
                source = PlaybackHistorySource.NATIVE_MEDIA
            ),
            privateBrowsing()
        )
    }
}

internal data class NativePlaybackHistoryRestore(
    val positionMs: Long? = null,
    val playbackSpeed: Float? = null
)

internal data class NativePlaybackHistorySnapshot(
    val mediaIdentity: String,
    val positionMs: Long,
    val durationMs: Long,
    val speed: Float,
    val title: String?
)
