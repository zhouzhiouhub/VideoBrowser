package com.example.videobrowser.functioncenter

/**
 * 初学者阅读提示：
 * 这个文件属于“功能中心模块”。
 * 文件名 PlaybackHistoryDisplayText 可以拆开理解为“Playback History Display Text”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：构建底部功能面板、设置页面、数据管理页面以及各种用户可点击的工具入口。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import com.example.videobrowser.utils.UrlUtils
import com.example.videobrowser.video.PlaybackProgress
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object PlaybackHistoryDisplayText {
    fun title(record: PlaybackProgress): String {
        return decodedLastPathSegment(record.mediaIdentity)
            ?: UrlUtils.displayUrl(record.mediaIdentity).takeIf { it.isNotBlank() }
            ?: record.mediaIdentity
    }

    fun summary(
        record: PlaybackProgress,
        updatedAtFormatter: (Long) -> String = ::formatUpdatedAt
    ): String {
        val progress = if (record.durationMs > 0L) {
            "${formatDuration(record.positionMs)} / ${formatDuration(record.durationMs)}"
        } else {
            formatDuration(record.positionMs)
        }
        return "${updatedAtFormatter(record.updatedAtMillis)} | $progress | ${formatSpeed(record.speed)}"
    }

    private fun decodedLastPathSegment(value: String): String? {
        val rawPath = runCatching { URI(value.trim()).rawPath }.getOrNull() ?: return null
        val rawSegment = rawPath.substringAfterLast('/').takeIf { it.isNotBlank() } ?: return null
        return runCatching {
            URLDecoder.decode(rawSegment, StandardCharsets.UTF_8.name())
        }.getOrElse {
            rawSegment
        }.takeIf { it.isNotBlank() }
    }

    private fun formatUpdatedAt(updatedAtMillis: Long): String {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
            .format(Date(updatedAtMillis))
    }

    private fun formatDuration(durationMs: Long): String {
        val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(durationMs.coerceAtLeast(0L))
        val hours = totalSeconds / SECONDS_PER_HOUR
        val minutes = (totalSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE
        val seconds = totalSeconds % SECONDS_PER_MINUTE

        return if (hours > 0L) {
            String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%d:%02d", minutes, seconds)
        }
    }

    private fun formatSpeed(speed: Float): String {
        val normalized = if (!speed.isNaN() && !speed.isInfinite() && speed > 0f) speed else 1f
        val numeric = if (normalized % 1f == 0f) {
            normalized.toInt().toString()
        } else {
            String.format(Locale.US, "%.2f", normalized).trimEnd('0').trimEnd('.')
        }
        return "${numeric}x"
    }

    private const val SECONDS_PER_MINUTE = 60L
    private const val SECONDS_PER_HOUR = 60L * SECONDS_PER_MINUTE
}
