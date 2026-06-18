package com.example.videobrowser.video

import org.json.JSONArray
import org.json.JSONObject

object PlaybackQueueJsonCodec {
    fun encode(queue: PlaybackQueue): String {
        return JSONObject()
            .put("currentIndex", queue.currentIndex)
            .put("repeatMode", queue.repeatMode.name)
            .put(
                "items",
                JSONArray().apply {
                    queue.items.forEach { item -> put(item.toJson()) }
                }
            )
            .apply {
                queue.originalItems?.let { originalItems ->
                    put(
                        "originalItems",
                        JSONArray().apply {
                            originalItems.forEach { item -> put(item.toJson()) }
                        }
                    )
                }
            }
            .toString()
    }

    fun decode(encodedQueue: String): PlaybackQueue? {
        return runCatching {
            val root = JSONObject(encodedQueue)
            val items = playableItemsFromJson(root.getJSONArray("items"))
            if (items.isEmpty()) {
                return@runCatching null
            }
            val index = root.optInt("currentIndex", 0).coerceIn(0, items.lastIndex)
            val repeat = root.optString("repeatMode")
                .takeIf { it.isNotBlank() }
                ?.let { runCatching { PlaybackRepeatMode.valueOf(it) }.getOrNull() }
                ?: PlaybackRepeatMode.NONE
            val originalItems = playableItemsFromJson(root.optJSONArray("originalItems"))
                .takeIf { it.isNotEmpty() }
            PlaybackQueue(
                items = items,
                currentIndex = index,
                repeatMode = repeat,
                originalItems = originalItems
            )
        }.getOrNull()
    }

    private fun playableItemsFromJson(array: JSONArray?): List<PlayableMediaItem> {
        if (array == null) {
            return emptyList()
        }
        return (0 until array.length()).mapNotNull { index ->
            array.optJSONObject(index)?.toPlayableMediaItem()
        }
    }

    private fun JSONObject.toPlayableMediaItem(): PlayableMediaItem? {
        val uri = optString("uri").takeIf { it.isNotBlank() } ?: return null
        return PlayableMediaItem(
            uri = uri,
            title = optString("title").takeIf { it.isNotBlank() },
            mimeType = optString("mimeType").takeIf { it.isNotBlank() },
            source = sourceFromName(optString("source")),
            userAgent = optString("userAgent").takeIf { it.isNotBlank() },
            referer = optString("referer").takeIf { it.isNotBlank() },
            subtitleCandidates = subtitleArrayFromJson(optJSONArray("subtitles"))
        )
    }

    private fun subtitleArrayFromJson(array: JSONArray?): List<ExternalSubtitleCandidate> {
        if (array == null) {
            return emptyList()
        }
        return (0 until array.length()).mapNotNull { index ->
            val subtitle = array.optJSONObject(index) ?: return@mapNotNull null
            val uri = subtitle.optString("uri").takeIf { it.isNotBlank() }
                ?: return@mapNotNull null
            ExternalSubtitleCandidate(
                uri = uri,
                label = subtitle.optString("label").takeIf { it.isNotBlank() },
                mimeType = subtitle.optString("mimeType").takeIf { it.isNotBlank() },
                language = subtitle.optString("language").takeIf { it.isNotBlank() }
            )
        }
    }

    private fun sourceFromName(name: String): PlayableMediaSource {
        return runCatching { PlayableMediaSource.valueOf(name) }
            .getOrDefault(PlayableMediaSource.REMOTE_URL)
    }

    private fun PlayableMediaItem.toJson(): JSONObject {
        return JSONObject()
            .put("uri", uri)
            .put("title", title.orEmpty())
            .put("mimeType", mimeType.orEmpty())
            .put("source", source.name)
            .put("userAgent", userAgent.orEmpty())
            .put("referer", referer.orEmpty())
            .put(
                "subtitles",
                JSONArray().apply {
                    subtitleCandidates.forEach { put(it.toJson()) }
                }
            )
    }

    private fun ExternalSubtitleCandidate.toJson(): JSONObject {
        return JSONObject()
            .put("uri", uri)
            .put("label", label.orEmpty())
            .put("mimeType", mimeType.orEmpty())
            .put("language", language.orEmpty())
    }
}
