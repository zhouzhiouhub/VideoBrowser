package com.example.videobrowser.video

/**
 * 测试阅读提示：
 * 这个测试文件验证“Local Playback Queue Builder Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import androidx.media3.common.MimeTypes
import org.junit.Assert.assertEquals
import org.junit.Test

class LocalPlaybackQueueBuilderTest {
    @Test
    fun `builds a sibling video queue with per-item subtitle candidates`() {
        val queue = LocalPlaybackQueueBuilder.fromDocuments(
            currentUri = "content://local/Episode%2002.mkv",
            documents = listOf(
                document("content://local/Episode%2001.mp4", "Episode 01.mp4", "video/mp4"),
                document("content://local/Episode%2001.srt", "Episode 01.srt", null),
                document("content://local/Notes.pdf", "Notes.pdf", "application/pdf"),
                document("content://local/Episode%2002.mkv", "Episode 02.mkv", "video/x-matroska"),
                document("content://local/Episode%2002.en.vtt", "Episode 02.en.vtt", "text/vtt")
            )
        )

        assertEquals(1, queue.currentIndex)
        assertEquals(
            listOf("content://local/Episode%2001.mp4", "content://local/Episode%2002.mkv"),
            queue.items.map { it.uri }
        )
        assertEquals(
            listOf(
                ExternalSubtitleCandidate(
                    uri = "content://local/Episode%2001.srt",
                    label = "Episode 01.srt",
                    mimeType = MimeTypes.APPLICATION_SUBRIP,
                    language = null
                )
            ),
            queue.items[0].subtitleCandidates
        )
        assertEquals(
            listOf(
                ExternalSubtitleCandidate(
                    uri = "content://local/Episode%2002.en.vtt",
                    label = "Episode 02.en.vtt",
                    mimeType = MimeTypes.TEXT_VTT,
                    language = "en"
                )
            ),
            queue.items[1].subtitleCandidates
        )
    }

    @Test
    fun `falls back to the current media item when siblings do not include it`() {
        val queue = LocalPlaybackQueueBuilder.fromDocuments(
            currentUri = "content://external/Clip.mp4",
            currentName = "Clip.mp4",
            currentMimeType = "video/mp4",
            documents = listOf(
                document("content://local/Other.mp4", "Other.mp4", "video/mp4")
            )
        )

        assertEquals(0, queue.currentIndex)
        assertEquals(listOf("content://external/Clip.mp4"), queue.items.map { it.uri })
        assertEquals("Clip.mp4", queue.currentItem()?.title)
    }

    private fun document(
        uri: String,
        name: String,
        mimeType: String?
    ): LocalPlaybackQueueBuilder.Document {
        return LocalPlaybackQueueBuilder.Document(
            uri = uri,
            name = name,
            mimeType = mimeType,
            isDirectory = false
        )
    }
}
