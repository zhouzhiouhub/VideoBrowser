package com.example.videobrowser.video

import com.example.videobrowser.storage.PreferenceStore

data class PlaybackProgress(
    val mediaIdentity: String,
    val positionMs: Long,
    val durationMs: Long,
    val speed: Float,
    val updatedAtMillis: Long
)

class PlaybackHistoryRepository(
    private val preferenceStore: PreferenceStore
) {
    fun save(progress: PlaybackProgress, privateBrowsing: Boolean = false) {
        if (privateBrowsing) {
            return
        }
        val normalizedProgress = normalize(progress) ?: return
        val records = records()
            .filterNot { it.mediaIdentity == normalizedProgress.mediaIdentity }
            .toMutableList()
        records.add(0, normalizedProgress)
        save(records.take(RECORD_LIMIT))
    }

    fun records(): List<PlaybackProgress> {
        val rawValue = preferenceStore.getString(KEY_PLAYBACK_HISTORY, null) ?: return emptyList()
        return rawValue
            .lineSequence()
            .mapNotNull(::parseProgress)
            .toList()
    }

    fun progressFor(mediaIdentity: String): PlaybackProgress? {
        return records().firstOrNull { it.mediaIdentity == mediaIdentity }
    }

    fun resumePositionFor(mediaIdentity: String): Long? {
        val progress = progressFor(mediaIdentity) ?: return null
        if (progress.positionMs <= 0L) {
            return null
        }
        if (progress.durationMs > 0L &&
            progress.durationMs - progress.positionMs <= RESUME_END_THRESHOLD_MS
        ) {
            return null
        }
        return progress.positionMs
    }

    fun clear() {
        preferenceStore.remove(KEY_PLAYBACK_HISTORY)
    }

    private fun save(records: List<PlaybackProgress>) {
        if (records.isEmpty()) {
            preferenceStore.remove(KEY_PLAYBACK_HISTORY)
            return
        }
        preferenceStore.putString(
            KEY_PLAYBACK_HISTORY,
            records.joinToString(separator = "\n", transform = ::encodeProgress)
        )
    }

    private fun parseProgress(line: String): PlaybackProgress? {
        val fields = splitEscaped(line)
        if (fields.size != FIELD_COUNT) {
            return null
        }
        val mediaIdentity = fields[0].takeIf { it.isNotBlank() } ?: return null
        return PlaybackProgress(
            mediaIdentity = mediaIdentity,
            positionMs = fields[1].toLongOrNull()?.coerceAtLeast(0L) ?: return null,
            durationMs = fields[2].toLongOrNull()?.coerceAtLeast(0L) ?: return null,
            speed = normalizeSpeed(fields[3].toFloatOrNull() ?: 1f),
            updatedAtMillis = fields[4].toLongOrNull()?.coerceAtLeast(0L) ?: return null
        )
    }

    private fun encodeProgress(progress: PlaybackProgress): String {
        return listOf(
            progress.mediaIdentity,
            progress.positionMs.toString(),
            progress.durationMs.toString(),
            progress.speed.toString(),
            progress.updatedAtMillis.toString()
        ).joinToString(separator = "\t", transform = ::escape)
    }

    private fun normalize(progress: PlaybackProgress): PlaybackProgress? {
        val mediaIdentity = progress.mediaIdentity.trim().takeIf { it.isNotEmpty() }
            ?: return null
        return progress.copy(
            mediaIdentity = mediaIdentity,
            positionMs = progress.positionMs.coerceAtLeast(0L),
            durationMs = progress.durationMs.coerceAtLeast(0L),
            speed = normalizeSpeed(progress.speed),
            updatedAtMillis = progress.updatedAtMillis.coerceAtLeast(0L)
        )
    }

    private fun normalizeSpeed(speed: Float): Float {
        return if (!speed.isNaN() && !speed.isInfinite() && speed > 0f) {
            speed
        } else {
            1f
        }
    }

    private fun splitEscaped(line: String): List<String> {
        val fields = mutableListOf<String>()
        val current = StringBuilder()
        var escaping = false
        line.forEach { char ->
            if (escaping) {
                current.append(
                    when (char) {
                        't' -> '\t'
                        'n' -> '\n'
                        'r' -> '\r'
                        else -> char
                    }
                )
                escaping = false
            } else {
                when (char) {
                    '\\' -> escaping = true
                    '\t' -> {
                        fields.add(current.toString())
                        current.clear()
                    }
                    else -> current.append(char)
                }
            }
        }
        if (escaping) {
            current.append('\\')
        }
        fields.add(current.toString())
        return fields
    }

    private fun escape(value: String): String {
        return buildString {
            value.forEach { char ->
                when (char) {
                    '\\' -> append("\\\\")
                    '\t' -> append("\\t")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    else -> append(char)
                }
            }
        }
    }

    private companion object {
        private const val KEY_PLAYBACK_HISTORY = "playback_history"
        private const val FIELD_COUNT = 5
        private const val RECORD_LIMIT = 100
        private const val RESUME_END_THRESHOLD_MS = 5_000L
    }
}
