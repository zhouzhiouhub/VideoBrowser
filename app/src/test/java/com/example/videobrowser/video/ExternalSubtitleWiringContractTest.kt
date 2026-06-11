package com.example.videobrowser.video

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class ExternalSubtitleWiringContractTest {
    @Test
    fun playableMediaItemCarriesSubtitleCandidates() {
        val source = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayableMediaItem.kt"
        ).readText()

        assertTrue(source.contains("data class ExternalSubtitleCandidate"))
        assertTrue(source.contains("val subtitleCandidates: List<ExternalSubtitleCandidate> = emptyList()"))
    }

    @Test
    fun playerActivitySerializesSubtitleCandidatesAndAttachesThemToMediaItem() {
        val source = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()

        assertTrue(source.contains("EXTRA_SUBTITLE_URIS"))
        assertTrue(source.contains("putStringArrayListExtra("))
        assertTrue(source.contains("subtitleCandidatesFromIntent()"))
        assertTrue(source.contains("setSubtitleConfigurations("))
        assertTrue(source.contains("MediaItem.SubtitleConfiguration.Builder"))
    }

    @Test
    fun localDirectoryOpenPassesMatchedSubtitlesIntoNativePlayer() {
        val localFiles = projectFile(
            "src/main/java/com/example/videobrowser/localfiles/LocalFilesController.kt"
        ).readText()
        val pageActions = projectFile(
            "src/main/java/com/example/videobrowser/browser/PageActionsController.kt"
        ).readText()

        assertTrue(localFiles.contains("LocalSubtitleMatcher.findSubtitleCandidates"))
        assertTrue(localFiles.contains("onOpenDocumentUri("))
        assertTrue(localFiles.contains("subtitleCandidates"))
        assertTrue(pageActions.contains("subtitleCandidates: List<ExternalSubtitleCandidate> = emptyList()"))
        assertTrue(pageActions.contains("mediaItem?.title ?: title,\n                subtitleCandidates"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
