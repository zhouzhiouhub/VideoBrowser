package com.example.videobrowser.video

import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackQueueLabelFormatterTest {
    @Test
    fun labelsUseExplicitTitleAndMarkCurrentItem() {
        val queue = PlaybackQueue(
            items = listOf(
                media("https://example.com/first.mp4", title = "First"),
                media("https://example.com/second.mp4", title = "Second")
            ),
            currentIndex = 1
        )

        assertEquals(
            listOf(
                "1. First",
                "2. Second - Now"
            ),
            PlaybackQueueLabelFormatter.labels(queue, nowPlayingLabel = "Now")
        )
    }

    @Test
    fun labelsFallbackToFileNameOrFullUri() {
        val queue = PlaybackQueue(
            items = listOf(
                media("content://videos/clip.mp4", title = " "),
                media("content://videos/")
            )
        )

        assertEquals(
            listOf(
                "1. clip.mp4 - Playing",
                "2. content://videos/"
            ),
            PlaybackQueueLabelFormatter.labels(queue, nowPlayingLabel = "Playing")
        )
    }

    private fun media(uri: String, title: String? = null): PlayableMediaItem {
        return PlayableMediaItem(
            uri = uri,
            title = title,
            source = PlayableMediaSource.REMOTE_URL
        )
    }
}
