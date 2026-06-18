package com.example.videobrowser.video

/**
 * 初学者阅读提示：
 * 这个文件属于“视频播放模块”。
 * 文件名 PlaybackHistoryRepository 可以拆开理解为“Playback History Repository”，表示它只负责视频链路中的一个小职责。
 * 主要职责：连接网页视频手势、原生 ExoPlayer 播放、播放队列、字幕、播放历史或媒体路由。
 * 阅读顺序：先看数据模型表达什么播放状态，再看控制器如何响应用户手势和播放器回调。
 */
import com.example.videobrowser.settings.TabSeparatedLineCodec
import com.example.videobrowser.storage.PreferenceStore
import com.example.videobrowser.utils.PlaybackSpeedNormalizer
import com.example.videobrowser.utils.TextWhitespaceNormalizer

/**
 * 单个媒体的播放进度记录。
 */
data class PlaybackProgress(
    val mediaIdentity: String,
    val positionMs: Long,
    val durationMs: Long,
    val speed: Float,
    val updatedAtMillis: Long,
    val title: String? = null,
    val source: PlaybackHistorySource = PlaybackHistorySource.NATIVE_MEDIA
)

enum class PlaybackHistorySource {
    NATIVE_MEDIA,
    WEB_PAGE
}

class PlaybackHistoryRepository(
    private val preferenceStore: PreferenceStore
) {
    /**
     * 函数 `save`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param progress 参数类型为 `PlaybackProgress`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @param privateBrowsing 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     */
    fun save(progress: PlaybackProgress, privateBrowsing: Boolean = false) {
        // 无痕播放不写入播放历史；普通模式下同一媒体只保留最新一条。
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

    /**
     * 函数 `records`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun records(): List<PlaybackProgress> {
        val rawValue = preferenceStore.getString(KEY_PLAYBACK_HISTORY, null) ?: return emptyList()
        return rawValue
            .lineSequence()
            .mapNotNull(::parseProgress)
            .toList()
    }

    /**
     * 函数 `progressFor`：封装 `progress For` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param mediaIdentity 参数类型为 `String`，表示函数执行 `mediaIdentity` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun progressFor(mediaIdentity: String): PlaybackProgress? {
        return records().firstOrNull { it.mediaIdentity == mediaIdentity }
    }

    /**
     * 函数 `resumePositionFor`：封装 `resume Position For` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param mediaIdentity 参数类型为 `String`，表示函数执行 `mediaIdentity` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun resumePositionFor(mediaIdentity: String): Long? {
        // 接近片尾时不提示续播，避免用户下次打开已经看完的视频还跳到结尾。
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

    /**
     * 函数 `clear`：封装 `clear` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun clear() {
        preferenceStore.remove(KEY_PLAYBACK_HISTORY)
    }

    /**
     * 函数 `save`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param records 参数类型为 `List<PlaybackProgress>`，表示一组待处理数据，函数会遍历、过滤或转换这些内容。
     */
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

    /**
     * 函数 `parseProgress`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param line 参数类型为 `String`，表示函数执行 `line` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun parseProgress(line: String): PlaybackProgress? {
        val fields = TabSeparatedLineCodec.splitFields(line)
        if (fields.size != LEGACY_FIELD_COUNT && fields.size != FIELD_COUNT) {
            return null
        }
        val mediaIdentity = fields[0].takeIf { it.isNotBlank() } ?: return null
        return PlaybackProgress(
            mediaIdentity = mediaIdentity,
            positionMs = fields[1].toLongOrNull()?.coerceAtLeast(0L) ?: return null,
            durationMs = fields[2].toLongOrNull()?.coerceAtLeast(0L) ?: return null,
            speed = PlaybackSpeedNormalizer.normalize(fields[3].toFloatOrNull() ?: 1f),
            updatedAtMillis = fields[4].toLongOrNull()?.coerceAtLeast(0L) ?: return null,
            title = fields.getOrNull(5)?.takeIf { it.isNotBlank() },
            source = fields.getOrNull(6)
                ?.let { value -> runCatching { PlaybackHistorySource.valueOf(value) }.getOrNull() }
                ?: PlaybackHistorySource.NATIVE_MEDIA
        )
    }

    /**
     * 函数 `encodeProgress`：封装 `encode Progress` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param progress 参数类型为 `PlaybackProgress`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun encodeProgress(progress: PlaybackProgress): String {
        return TabSeparatedLineCodec.joinFields(
            listOf(
                progress.mediaIdentity,
                progress.positionMs.toString(),
                progress.durationMs.toString(),
                progress.speed.toString(),
                progress.updatedAtMillis.toString(),
                progress.title.orEmpty(),
                progress.source.name
            )
        )
    }

    /**
     * 函数 `normalize`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param progress 参数类型为 `PlaybackProgress`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun normalize(progress: PlaybackProgress): PlaybackProgress? {
        val mediaIdentity = progress.mediaIdentity.trim().takeIf { it.isNotEmpty() }
            ?: return null
        return progress.copy(
            mediaIdentity = mediaIdentity,
            positionMs = progress.positionMs.coerceAtLeast(0L),
            durationMs = progress.durationMs.coerceAtLeast(0L),
            speed = PlaybackSpeedNormalizer.normalize(progress.speed),
            updatedAtMillis = progress.updatedAtMillis.coerceAtLeast(0L),
            title = normalizeTitle(progress.title)
        )
    }

    private fun normalizeTitle(title: String?): String? {
        return title
            ?.let(TextWhitespaceNormalizer::collapse)
            ?.take(MAX_TITLE_LENGTH)
            ?.takeIf { it.isNotBlank() }
    }

    private companion object {
        private const val KEY_PLAYBACK_HISTORY = "playback_history"
        private const val LEGACY_FIELD_COUNT = 5
        private const val FIELD_COUNT = 7
        private const val RECORD_LIMIT = 100
        private const val RESUME_END_THRESHOLD_MS = 5_000L
        private const val MAX_TITLE_LENGTH = 200
    }
}
