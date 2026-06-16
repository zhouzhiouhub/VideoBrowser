package com.example.videobrowser.video

/**
 * 初学者阅读提示：
 * 这个文件属于“视频播放模块”。
 * 文件名 LocalSubtitleMatcher 可以拆开理解为“Local Subtitle Matcher”，表示它只负责视频链路中的一个小职责。
 * 主要职责：连接网页视频手势、原生 ExoPlayer 播放、播放队列、字幕、播放历史或媒体路由。
 * 阅读顺序：先看数据模型表达什么播放状态，再看控制器如何响应用户手势和播放器回调。
 */
import androidx.media3.common.MimeTypes

/**
 * 本地字幕自动匹配器。
 *
 * 支持两种常见命名：
 * - movie.mp4 对应 movie.srt。
 * - movie.mp4 对应 movie.zh-CN.srt，后缀会作为字幕语言。
 */
object LocalSubtitleMatcher {
    data class Document(
        val uri: String,
        val name: String,
        val mimeType: String?
    )

    private val subtitleMimeTypesByExtension = mapOf(
        "srt" to MimeTypes.APPLICATION_SUBRIP,
        "vtt" to MimeTypes.TEXT_VTT,
        "ass" to MimeTypes.TEXT_SSA,
        "ssa" to MimeTypes.TEXT_SSA,
        "ttml" to MimeTypes.APPLICATION_TTML,
        "dfxp" to MimeTypes.APPLICATION_TTML
    )
    private val supportedSubtitleMimeTypes = subtitleMimeTypesByExtension.values.toSet()
    private val languagePattern = Regex("^[A-Za-z]{2,3}(?:-[A-Za-z0-9]+)*$")

    fun findSubtitleCandidates(
        mediaName: String?,
        documents: List<Document>
    ): List<ExternalSubtitleCandidate> {
        val mediaStem = mediaName
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { it.substringBeforeLast('.', missingDelimiterValue = it) }
            ?: return emptyList()

        return documents
            .mapNotNull { document -> document.toSubtitleCandidate(mediaStem) }
            .sortedBy { it.label?.lowercase().orEmpty() }
    }

    private fun Document.toSubtitleCandidate(mediaStem: String): ExternalSubtitleCandidate? {
        // 只接受和视频同名或带语言后缀的字幕，避免把目录里无关字幕误挂到当前视频上。
        val normalizedName = name.trim().takeIf { it.isNotEmpty() } ?: return null
        val subtitleStem = normalizedName.substringBeforeLast('.', missingDelimiterValue = normalizedName)
        val language = subtitleLanguage(mediaStem, subtitleStem)
        if (subtitleStem != mediaStem && language == null) {
            return null
        }

        val mimeType = subtitleMimeTypeForName(normalizedName)
            ?: supportedSubtitleMimeType(mimeType)
            ?: return null

        return ExternalSubtitleCandidate(
            uri = uri,
            label = normalizedName,
            mimeType = mimeType,
            language = language
        )
    }

    private fun subtitleLanguage(mediaStem: String, subtitleStem: String): String? {
        val prefix = "$mediaStem."
        if (!subtitleStem.startsWith(prefix)) {
            return null
        }

        val suffix = subtitleStem.removePrefix(prefix)
        return suffix.takeIf { it.matches(languagePattern) }
    }

    private fun subtitleMimeTypeForName(name: String): String? {
        val extension = name.substringAfterLast('.', missingDelimiterValue = "")
            .lowercase()
        return subtitleMimeTypesByExtension[extension]
    }

    private fun supportedSubtitleMimeType(mimeType: String?): String? {
        val normalized = mimeType
            ?.substringBefore(';')
            ?.trim()
            ?.lowercase()
            .orEmpty()
        return normalized.takeIf { it in supportedSubtitleMimeTypes }
    }
}
