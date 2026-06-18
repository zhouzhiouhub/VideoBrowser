package com.example.videobrowser.localfiles

import com.example.videobrowser.video.ExternalSubtitleCandidate
import com.example.videobrowser.video.LocalPlaybackQueueBuilder
import com.example.videobrowser.video.LocalSubtitleMatcher
import com.example.videobrowser.video.PlaybackQueue

internal data class LocalDocumentOpenRequest(
    val subtitleCandidates: List<ExternalSubtitleCandidate>,
    val playbackQueue: PlaybackQueue?
)

/**
 * 把本地目录中的文件列表转换成播放器入口需要的队列和字幕参数。
 */
internal object LocalDocumentOpenRequestBuilder {
    fun from(
        document: LocalDocument,
        siblingDocuments: List<LocalDocument>
    ): LocalDocumentOpenRequest {
        val playbackQueue = LocalPlaybackQueueBuilder.fromDocuments(
            currentUri = document.uri.toString(),
            currentName = document.name,
            currentMimeType = document.mimeType,
            documents = siblingDocuments.map {
                LocalPlaybackQueueBuilder.Document(
                    uri = it.uri.toString(),
                    name = it.name,
                    mimeType = it.mimeType,
                    isDirectory = it.isDirectory
                )
            }
        )
        val subtitleCandidates = LocalSubtitleMatcher.findSubtitleCandidates(
            mediaName = document.name,
            documents = siblingDocuments
                .filterNot { it.isDirectory }
                .map {
                    LocalSubtitleMatcher.Document(
                        uri = it.uri.toString(),
                        name = it.name,
                        mimeType = it.mimeType
                    )
                }
        )

        return LocalDocumentOpenRequest(
            subtitleCandidates = subtitleCandidates,
            playbackQueue = playbackQueue
        )
    }
}
