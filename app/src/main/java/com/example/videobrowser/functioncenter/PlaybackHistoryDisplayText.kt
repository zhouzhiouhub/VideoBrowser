package com.example.videobrowser.functioncenter

/**
 * 初学者阅读提示：
 * 这个文件属于“功能中心模块”。
 * 文件名 PlaybackHistoryDisplayText 可以拆开理解为“Playback History Display Text”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：构建底部功能面板、设置页面、数据管理页面以及各种用户可点击的工具入口。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import com.example.videobrowser.utils.PlaybackSpeedDisplayFormatter
import com.example.videobrowser.utils.ShortDateTimeFormatter
import com.example.videobrowser.utils.UrlUtils
import com.example.videobrowser.utils.DurationLabelFormatter
import com.example.videobrowser.utils.SafeUriParser
import com.example.videobrowser.utils.Utf8UrlCodec
import com.example.videobrowser.video.PlaybackProgress

object PlaybackHistoryDisplayText {
    /**
     * 函数 `title`：封装 `title` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param record 参数类型为 `PlaybackProgress`，表示函数执行 `record` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun title(record: PlaybackProgress): String {
        return record.title?.takeIf { it.isNotBlank() }
            ?: decodedLastPathSegment(record.mediaIdentity)
            ?: UrlUtils.displayUrl(record.mediaIdentity).takeIf { it.isNotBlank() }
            ?: record.mediaIdentity
    }

    /**
     * 函数 `summary`：封装 `summary` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param record 参数类型为 `PlaybackProgress`，表示函数执行 `record` 相关逻辑时需要读取或处理的输入。
     * @param updatedAtFormatter 参数类型为 `(Long) -> String`，表示函数执行 `updatedAtFormatter` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun summary(
        record: PlaybackProgress,
        updatedAtFormatter: (Long) -> String = ::formatUpdatedAt
    ): String {
        val progress = if (record.durationMs > 0L) {
            "${DurationLabelFormatter.formatMillis(record.positionMs)} / " +
                DurationLabelFormatter.formatMillis(record.durationMs)
        } else {
            DurationLabelFormatter.formatMillis(record.positionMs)
        }
        val speedText = PlaybackSpeedDisplayFormatter.format(record.speed)
        return "${updatedAtFormatter(record.updatedAtMillis)} | $progress | $speedText"
    }

    /**
     * 函数 `decodedLastPathSegment`：封装 `decoded Last Path Segment` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param value 参数类型为 `String`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun decodedLastPathSegment(value: String): String? {
        val rawPath = SafeUriParser.parse(value)?.rawPath ?: return null
        val rawSegment = rawPath.substringAfterLast('/').takeIf { it.isNotBlank() } ?: return null
        return Utf8UrlCodec.decodeFormComponentOr(rawSegment, rawSegment)
            .takeIf { it.isNotBlank() }
    }

    /**
     * 函数 `formatUpdatedAt`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param updatedAtMillis 参数类型为 `Long`，表示函数执行 `updatedAtMillis` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun formatUpdatedAt(updatedAtMillis: Long): String {
        return ShortDateTimeFormatter.format(updatedAtMillis)
    }
}
