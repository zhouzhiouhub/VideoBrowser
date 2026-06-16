package com.example.videobrowser.functioncenter

/**
 * 测试阅读提示：
 * 这个测试文件验证“Playback History Display Text Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import com.example.videobrowser.video.PlaybackProgress
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackHistoryDisplayTextTest {
    @Test
    fun titlePrefersDecodedMediaFileName() {
        val record = playbackProgress(
            mediaIdentity = "https://cdn.example.com/video/My%20Clip.mp4?token=1"
        )

        assertEquals("My Clip.mp4", PlaybackHistoryDisplayText.title(record))
    }

    @Test
    fun summaryShowsUpdatedTimeProgressDurationAndSpeed() {
        val record = playbackProgress(
            positionMs = 65_000L,
            durationMs = 3_665_000L,
            speed = 1.5f,
            updatedAtMillis = 123L
        )

        assertEquals(
            "2026/6/11 12:00 | 1:05 / 1:01:05 | 1.5x",
            PlaybackHistoryDisplayText.summary(record) { "2026/6/11 12:00" }
        )
    }

    @Test
    fun summaryNormalizesUnknownDurationAndInvalidSpeed() {
        val record = playbackProgress(
            positionMs = 4_000L,
            durationMs = 0L,
            speed = Float.NaN
        )

        assertEquals(
            "now | 0:04 | 1x",
            PlaybackHistoryDisplayText.summary(record) { "now" }
        )
    }

    private fun playbackProgress(
        mediaIdentity: String = "content://media/video/42",
        positionMs: Long = 0L,
        durationMs: Long = 0L,
        speed: Float = 1f,
        updatedAtMillis: Long = 0L
    ): PlaybackProgress {
        return PlaybackProgress(
            mediaIdentity = mediaIdentity,
            positionMs = positionMs,
            durationMs = durationMs,
            speed = speed,
            updatedAtMillis = updatedAtMillis
        )
    }
}
