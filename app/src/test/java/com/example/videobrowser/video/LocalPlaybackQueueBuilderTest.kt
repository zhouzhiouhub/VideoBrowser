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
    /**
     * 测试函数 `builds`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `builds` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `falls`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `falls` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `document`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `document` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param uri 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param name 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param mimeType 参数类型为 `String?`，表示函数执行 `mimeType` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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
