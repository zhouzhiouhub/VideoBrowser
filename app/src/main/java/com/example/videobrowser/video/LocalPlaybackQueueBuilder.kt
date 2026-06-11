package com.example.videobrowser.video

import com.example.videobrowser.utils.MediaUrlUtils

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
