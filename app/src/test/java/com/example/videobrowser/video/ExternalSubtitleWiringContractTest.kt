package com.example.videobrowser.video

/**
 * 测试阅读提示：
 * 这个测试文件验证“External Subtitle Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
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
        ).readText().replace("\r\n", "\n")

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
