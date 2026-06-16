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
    /**
     * 测试函数 `titlePrefersDecodedMediaFileName`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `title Prefers Decoded Media File Name` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun titlePrefersDecodedMediaFileName() {
        val record = playbackProgress(
            mediaIdentity = "https://cdn.example.com/video/My%20Clip.mp4?token=1"
        )

        assertEquals("My Clip.mp4", PlaybackHistoryDisplayText.title(record))
    }

    @Test
    fun titlePrefersStoredTitle() {
        val record = playbackProgress(
            mediaIdentity = "https://video.example.com/watch/1",
            title = "Episode 1"
        )

        assertEquals("Episode 1", PlaybackHistoryDisplayText.title(record))
    }

    /**
     * 测试函数 `summaryShowsUpdatedTimeProgressDurationAndSpeed`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `summary Shows Updated Time Progress Duration And Speed` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `summaryNormalizesUnknownDurationAndInvalidSpeed`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `summary Normalizes Unknown Duration And Invalid Speed` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `playbackProgress`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `playback Progress` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param mediaIdentity 参数类型为 `String`，表示函数执行 `mediaIdentity` 相关逻辑时需要读取或处理的输入。
     * @param positionMs 参数类型为 `Long`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @param durationMs 参数类型为 `Long`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @param speed 参数类型为 `Float`，表示函数执行 `speed` 相关逻辑时需要读取或处理的输入。
     * @param updatedAtMillis 参数类型为 `Long`，表示函数执行 `updatedAtMillis` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun playbackProgress(
        mediaIdentity: String = "content://media/video/42",
        positionMs: Long = 0L,
        durationMs: Long = 0L,
        speed: Float = 1f,
        updatedAtMillis: Long = 0L,
        title: String? = null
    ): PlaybackProgress {
        return PlaybackProgress(
            mediaIdentity = mediaIdentity,
            positionMs = positionMs,
            durationMs = durationMs,
            speed = speed,
            updatedAtMillis = updatedAtMillis,
            title = title
        )
    }
}
