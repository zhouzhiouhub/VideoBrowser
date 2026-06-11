package com.example.videobrowser.video

import androidx.media3.common.MimeTypes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalSubtitleMatcherTest {
    @Test
    fun `matches same-name subtitle files and common language suffixes`() {
        val candidates = LocalSubtitleMatcher.findSubtitleCandidates(
            mediaName = "Episode 01.mp4",
            documents = listOf(
                LocalSubtitleMatcher.Document(
                    uri = "content://local/Episode%2001.mp4",
                    name = "Episode 01.mp4",
                    mimeType = "video/mp4"
                ),
                LocalSubtitleMatcher.Document(
                    uri = "content://local/Episode%2001.srt",
                    name = "Episode 01.srt",
                    mimeType = null
                ),
                LocalSubtitleMatcher.Document(
                    uri = "content://local/Episode%2001.en.vtt",
                    name = "Episode 01.en.vtt",
                    mimeType = "text/vtt"
                ),
                LocalSubtitleMatcher.Document(
                    uri = "content://local/Episode%2001.zh-Hans.ass",
                    name = "Episode 01.zh-Hans.ass",
                    mimeType = null
                ),
                LocalSubtitleMatcher.Document(
                    uri = "content://local/Episode%2001.notes.txt",
                    name = "Episode 01.notes.txt",
                    mimeType = "text/plain"
                ),
                LocalSubtitleMatcher.Document(
                    uri = "content://local/Episode%2002.srt",
                    name = "Episode 02.srt",
                    mimeType = null
                )
            )
        )

        assertEquals(
            listOf(
                ExternalSubtitleCandidate(
                    uri = "content://local/Episode%2001.en.vtt",
                    label = "Episode 01.en.vtt",
                    mimeType = MimeTypes.TEXT_VTT,
                    language = "en"
                ),
                ExternalSubtitleCandidate(
                    uri = "content://local/Episode%2001.srt",
                    label = "Episode 01.srt",
                    mimeType = MimeTypes.APPLICATION_SUBRIP,
                    language = null
                ),
                ExternalSubtitleCandidate(
                    uri = "content://local/Episode%2001.zh-Hans.ass",
                    label = "Episode 01.zh-Hans.ass",
                    mimeType = MimeTypes.TEXT_SSA,
                    language = "zh-Hans"
                )
            ),
            candidates
        )
    }

    @Test
    fun `ignores subtitle-looking files when media name is missing`() {
        val candidates = LocalSubtitleMatcher.findSubtitleCandidates(
            mediaName = null,
            documents = listOf(
                LocalSubtitleMatcher.Document(
                    uri = "content://local/Movie.srt",
                    name = "Movie.srt",
                    mimeType = null
                )
            )
        )

        assertTrue(candidates.isEmpty())
    }
}
