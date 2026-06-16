package com.example.videobrowser.video

/**
 * 初学者阅读提示：
 * 这个文件属于“视频播放模块”。
 * 文件名 LocalPlaybackQueueBuilder 可以拆开理解为“Local Playback Queue Builder”，表示它只负责视频链路中的一个小职责。
 * 主要职责：连接网页视频手势、原生 ExoPlayer 播放、播放队列、字幕、播放历史或媒体路由。
 * 阅读顺序：先看数据模型表达什么播放状态，再看控制器如何响应用户手势和播放器回调。
 */
import com.example.videobrowser.utils.MediaUrlUtils

/**
 * 从本地目录文件列表构建播放队列。
 *
 * 用户打开一个本地视频时，如果同目录还有其他视频文件，就把它们组成队列；
 * 同目录的字幕文件也会通过 LocalSubtitleMatcher 自动关联到对应视频。
 */
object LocalPlaybackQueueBuilder {
    data class Document(
        val uri: String,
        val name: String,
        val mimeType: String?,
        val isDirectory: Boolean
    )

    fun fromDocuments(
        currentUri: String,
        currentName: String? = null,
        currentMimeType: String? = null,
        documents: List<Document>
    ): PlaybackQueue {
        // 目录项先剔除，只保留文件；随后分别识别可播放媒体和字幕候选。
        val files = documents.filterNot { it.isDirectory }
        val subtitleDocuments = files.map {
            LocalSubtitleMatcher.Document(
                uri = it.uri,
                name = it.name,
                mimeType = it.mimeType
            )
        }
        val items = files
            .filter(::isPlayableMediaDocument)
            .map { document ->
                PlayableMediaItem(
                    uri = document.uri,
                    title = document.name,
                    mimeType = document.mimeType,
                    source = PlayableMediaSource.LOCAL_DOCUMENT,
                    subtitleCandidates = LocalSubtitleMatcher.findSubtitleCandidates(
                        mediaName = document.name,
                        documents = subtitleDocuments
                    )
                )
            }
        val currentIndex = items.indexOfFirst { it.uri == currentUri }
        if (currentIndex >= 0) {
            return PlaybackQueue(items = items, currentIndex = currentIndex)
        }

        return PlaybackQueue.single(
            PlayableMediaItem(
                uri = currentUri,
                title = currentName,
                mimeType = currentMimeType,
                source = PlayableMediaSource.LOCAL_DOCUMENT,
                subtitleCandidates = LocalSubtitleMatcher.findSubtitleCandidates(
                    mediaName = currentName,
                    documents = subtitleDocuments
                )
            )
        )
    }

    private fun isPlayableMediaDocument(document: Document): Boolean {
        return MediaUrlUtils.isPlayableMediaUri(document.uri, document.mimeType) ||
            MediaUrlUtils.isPlayableMediaUri("file:///${document.name}", document.mimeType)
    }
}
